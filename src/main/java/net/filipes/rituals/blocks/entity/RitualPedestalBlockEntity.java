package net.filipes.rituals.blocks.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class RitualPedestalBlockEntity extends BlockEntity implements Inventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(9999, ItemStack.EMPTY);

    @Nullable
    private UUID displayEntityUuid = null;

    // NEW: multiple floating items
    private final List<UUID> floatingItemUuids = new ArrayList<>();
    private final List<String> floatingItemIds = new ArrayList<>();

    // Config: maximum number of distinct floating items to show
    private static final int MAX_FLOATING = 6;

    private static final boolean ENABLE_PARTICLES = true;

    // removed item particles
    private static final int END_ROD_PARTICLE_COUNT = 1; // keep low
    private static final double END_ROD_OFFSET = 0.02;

    public RitualPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RITUAL_PEDESTAL_BE, pos, state);
    }

    public void tick(World world) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        // --- Manage text display entity (unchanged behaviour) ---
        if (displayEntityUuid != null) {
            Entity existing = serverWorld.getEntity(displayEntityUuid);
            if (existing != null && !existing.isRemoved()) {
                // nothing — updates happen via markDirty() -> updateDisplayText()
            } else {
                displayEntityUuid = null;
            }
        }

        if (displayEntityUuid == null) {
            DisplayEntity.TextDisplayEntity display = new DisplayEntity.TextDisplayEntity(
                    EntityType.TEXT_DISPLAY, serverWorld
            );

            display.setPosition(
                    pos.getX() + 0.5,
                    pos.getY() + 1.8,
                    pos.getZ() + 0.5
            );

            display.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
            display.setNoGravity(true);
            display.setInvulnerable(true);
            display.setSilent(true);
            display.setText(buildDisplayText());

            serverWorld.spawnEntity(display);
            displayEntityUuid = display.getUuid();
        }

        // --- Compute distinct item types in the inventory (preserve insertion order) ---
        LinkedHashMap<String, ItemStack> exemplarMap = new LinkedHashMap<>();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                String id = Registries.ITEM.getId(stack.getItem()).toString();
                if (!exemplarMap.containsKey(id)) {
                    ItemStack ex = stack.copy();
                    ex.setCount(1);
                    exemplarMap.put(id, ex);
                }
                if (exemplarMap.size() >= MAX_FLOATING) break; // limit processed types
            }
        }

        // If no items -> remove all floats and return
        if (exemplarMap.isEmpty()) {
            killAllFloatingItems(serverWorld);
            return;
        }

        // Desired number of floating items
        int desired = exemplarMap.size();

        // --- Ensure floating lists are same size as desired: remove extras ---
        while (floatingItemUuids.size() > desired) {
            int idx = floatingItemUuids.size() - 1;
            UUID u = floatingItemUuids.remove(idx);
            floatingItemIds.remove(idx);
            Entity e = serverWorld.getEntity(u);
            if (e != null) e.discard();
        }

        // Build an ordered list of ids and exemplars
        List<String> desiredIds = new ArrayList<>(exemplarMap.keySet());
        List<ItemStack> desiredExemplars = new ArrayList<>(exemplarMap.values());

        // --- For each desired floating item slot, validate existing or spawn a new one ---
        for (int i = 0; i < desired; i++) {
            String wantId = desiredIds.get(i);
            ItemStack exemplar = desiredExemplars.get(i);

            if (i < floatingItemUuids.size()) {
                UUID uuid = floatingItemUuids.get(i);
                Entity existing = serverWorld.getEntity(uuid);
                if (existing != null && !existing.isRemoved() && existing instanceof ItemEntity itemEnt) {
                    String existingId = Registries.ITEM.getId(itemEnt.getStack().getItem()).toString();
                    if (!existingId.equals(wantId)) {
                        itemEnt.discard();
                        floatingItemUuids.set(i, null);
                        floatingItemIds.set(i, null);
                    } else {
                        // orbit + particles
                        orbitItemEntity(serverWorld, itemEnt, i, desired);
                        // ensure properties & stack
                        itemEnt.setNoGravity(true);
                        itemEnt.setInvulnerable(true);
                        itemEnt.setPickupDelayInfinite();
                        itemEnt.setSilent(true);
                        itemEnt.setStack(exemplar.copy());
                        continue;
                    }
                } else {
                    floatingItemUuids.set(i, null);
                    floatingItemIds.set(i, null);
                }
            }

            // spawn a new item entity at this slot index
            ItemEntity floating = new ItemEntity(serverWorld,
                    pos.getX() + 0.5,
                    pos.getY() + 1.2,
                    pos.getZ() + 0.5,
                    exemplar.copy()
            );
            floating.setNoGravity(true);
            floating.setInvulnerable(true);
            floating.setPickupDelayInfinite();
            floating.setSilent(true);
            floating.setVelocity(0.0, 0.0, 0.0);

            serverWorld.spawnEntity(floating);

            if (i < floatingItemUuids.size()) {
                floatingItemUuids.set(i, floating.getUuid());
                floatingItemIds.set(i, wantId);
            } else {
                floatingItemUuids.add(floating.getUuid());
                floatingItemIds.add(wantId);
            }

            // immediately orbit/position it so there's no pop-in
            orbitItemEntity(serverWorld, floating, i, desired);
        }
    }

    /**
     * Orbit an ItemEntity around the pedestal with index-based offset so multiple items
     * are spread evenly around a circle and bob up/down. Also spawns small particles.
     */
    private void orbitItemEntity(ServerWorld serverWorld, ItemEntity itemEnt, int index, int total) {
        long time = serverWorld.getTime();
        double baseSpeed = 0.06; // tweak for rotation speed
        double angleProgress = time * baseSpeed;
        double spacing = (Math.PI * 2.0) / Math.max(1, total);
        double angle = angleProgress + index * spacing;
        double radius = 0.6 + Math.min(0.35, total * 0.05); // slight radius increase with more items
        double cx = pos.getX() + 0.5 + Math.cos(angle) * radius;
        double cz = pos.getZ() + 0.5 + Math.sin(angle) * radius;
        double cy = pos.getY() + 1.15 + Math.sin(angle * 2.0 + index) * 0.12;

        // update server-side position and yaw so clients render smoothly
        itemEnt.refreshPositionAndAngles(cx, cy, cz, (float) Math.toDegrees(angle), 0f);

        // --- Particles (server -> client packets) ---
        if (ENABLE_PARTICLES) {
            // subtle magic glow only
            serverWorld.spawnParticles(
                    ParticleTypes.END_ROD,
                    cx,
                    cy + 0.1,
                    cz,
                    END_ROD_PARTICLE_COUNT,
                    END_ROD_OFFSET,
                    END_ROD_OFFSET,
                    END_ROD_OFFSET,
                    0.005
            );
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        killDisplayEntity();
        if (world instanceof ServerWorld serverWorld) killAllFloatingItems(serverWorld);
    }

    private void killDisplayEntity() {
        if (world instanceof ServerWorld serverWorld && displayEntityUuid != null) {
            Entity entity = serverWorld.getEntity(displayEntityUuid);
            if (entity != null) entity.discard();
            displayEntityUuid = null;
        }
    }

    private void killAllFloatingItems(ServerWorld serverWorld) {
        for (UUID u : floatingItemUuids) {
            if (u == null) continue;
            Entity e = serverWorld.getEntity(u);
            if (e != null) e.discard();
        }
        floatingItemUuids.clear();
        floatingItemIds.clear();
    }

    private Text buildDisplayText() {
        java.util.Map<String, Integer> countMap = new java.util.LinkedHashMap<>();
        java.util.Map<String, Text> nameMap = new java.util.LinkedHashMap<>();

        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                String id = Registries.ITEM.getId(stack.getItem()).toString();
                countMap.merge(id, stack.getCount(), Integer::sum);
                nameMap.putIfAbsent(id, stack.getName().copy());
            }
        }

        if (countMap.isEmpty()) return Text.literal("(empty)");

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String id : countMap.keySet()) {
            if (!first) sb.append("\n");
            first = false;
            Text name = nameMap.get(id);
            sb.append(name.getString());
            int total = countMap.get(id);
            if (total > 1) sb.append(" x").append(total);
        }

        return Text.literal(sb.toString());
    }

    private void updateDisplayText() {
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (displayEntityUuid == null) return;
        Entity entity = serverWorld.getEntity(displayEntityUuid);
        if (entity instanceof DisplayEntity.TextDisplayEntity display) {
            display.setText(buildDisplayText());
        }
    }

    @Override public int size() { return items.size(); }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getStack(int slot) { return items.get(slot); }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack removed = Inventories.splitStack(items, slot, amount);
        if (!removed.isEmpty()) markDirty();
        return removed;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removed = Inventories.removeStack(items, slot);
        if (!removed.isEmpty()) markDirty();
        return removed;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) stack.setCount(getMaxCountPerStack());
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (world == null) return false;
        return player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clear() { items.clear(); markDirty(); }

    @Override
    protected void writeData(WriteView view) {
        Inventories.writeData(view, items);
        if (displayEntityUuid != null) {
            view.putString("DisplayEntityUUID", displayEntityUuid.toString());
        }
        // write multiple floating UUIDs and IDs as comma-separated strings (empty -> "")
        if (!floatingItemUuids.isEmpty()) {
            StringBuilder uuids = new StringBuilder();
            for (int i = 0; i < floatingItemUuids.size(); i++) {
                if (i != 0) uuids.append(",");
                uuids.append(floatingItemUuids.get(i));
            }
            view.putString("FloatingItemUUIDs", uuids.toString());
        }
        if (!floatingItemIds.isEmpty()) {
            StringBuilder ids = new StringBuilder();
            for (int i = 0; i < floatingItemIds.size(); i++) {
                if (i != 0) ids.append(",");
                ids.append(floatingItemIds.get(i));
            }
            view.putString("FloatingItemIds", ids.toString());
        }
    }

    @Override
    protected void readData(ReadView view) {
        Inventories.readData(view, items);
        String raw = view.getString("DisplayEntityUUID", "");
        if (raw != null && !raw.isEmpty()) {
            this.displayEntityUuid = UUID.fromString(raw);
        }
        String rawUuids = view.getString("FloatingItemUUIDs", "");
        String rawIds = view.getString("FloatingItemIds", "");
        floatingItemUuids.clear();
        floatingItemIds.clear();
        if (rawUuids != null && !rawUuids.isEmpty()) {
            String[] parts = rawUuids.split(",");
            for (String s : parts) {
                if (s == null || s.isEmpty()) continue;
                try {
                    floatingItemUuids.add(UUID.fromString(s));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        if (rawIds != null && !rawIds.isEmpty()) {
            String[] parts = rawIds.split(",");
            for (String s : parts) {
                if (s == null) continue;
                floatingItemIds.add(s);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        updateDisplayText();
        // On inventory change, floating items will be re-evaluated/respawned in tick().
        if (world != null && !world.isClient()) {
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, 3);
        }
    }

    // inside RitualPedestalBlockEntity
    public ItemStack insertStack(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack toInsert = stack.copy();

        // --- 1) Merge into existing matching stacks ---
        for (int i = 0; i < items.size() && !toInsert.isEmpty(); i++) {
            ItemStack slot = items.get(i);
            if (!slot.isEmpty() && ItemStack.areItemsEqual(slot, toInsert)) {
                int slotMax = Math.min(getMaxCountPerStack(), slot.getMaxCount());
                int space = slotMax - slot.getCount();
                if (space > 0) {
                    int put = Math.min(space, toInsert.getCount());
                    slot.increment(put);                 // add into existing slot
                    toInsert.decrement(put);             // reduce what's left to insert
                    items.set(i, slot);
                }
            }
        }

        // --- 2) Fill empty slots ---
        for (int i = 0; i < items.size() && !toInsert.isEmpty(); i++) {
            if (items.get(i).isEmpty()) {
                int put = Math.min(toInsert.getCount(), getMaxCountPerStack());
                // split(put) returns a new ItemStack with 'put' items and reduces toInsert
                items.set(i, toInsert.split(put));
            }
        }

        // mark dirty only if something changed
        if (toInsert.getCount() != stack.getCount()) markDirty();

        return toInsert; // leftover
    }

    public ItemStack removeFirstNonEmpty() {
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                ItemStack copy = items.get(i).copy();
                items.set(i, ItemStack.EMPTY);
                markDirty();
                return copy;
            }
        }
        return ItemStack.EMPTY;
    }

    public List<ItemStack> removeAllOfFirstType() {
        // Find the first non-empty item type
        String targetId = null;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                targetId = Registries.ITEM.getId(stack.getItem()).toString();
                break;
            }
        }
        if (targetId == null) return List.of();

        // Collect and remove all stacks matching that type
        List<ItemStack> removed = new java.util.ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                String id = Registries.ITEM.getId(stack.getItem()).toString();
                if (id.equals(targetId)) {
                    removed.add(stack.copy());
                    items.set(i, ItemStack.EMPTY);
                }
            }
        }

        if (!removed.isEmpty()) markDirty();
        return removed;
    }
}