package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.ScreenShakeEntity;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.filipes.rituals.entity.custom.SparkPresets;
import net.filipes.rituals.entity.custom.TemporalSlowZoneGroundEntity;
import net.filipes.rituals.item.custom.TemporalGlassreaverItem;
import net.filipes.rituals.particle.ModParticles;
import net.filipes.rituals.sound.ModSounds;
import net.filipes.rituals.util.MuteTracker;
import net.filipes.rituals.util.TemporalFreezeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public record TemporalSlowZonePacket() implements CustomPacketPayload {

    public static final Type<TemporalSlowZonePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("rituals", "temporal_slow_zone"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemporalSlowZonePacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new TemporalSlowZonePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    private static final double RADIUS = 8.0;
    private static final int DURATION_TICKS = 100;
    private static final int REQUIRED_STAGE = 7;
    private static final int MAX_PARTICLES_PER_TICK = 2;
    private static final int MAX_PROJECTILE_SPARKS_PER_TICK = 2;
    private static final double ORBIT_RADIUS = 0.65;
    private static final double ORBIT_SPEED = 0.12;
    private static final double ORBIT_VERTICAL_BASE = 0.4;

    private static final double PLAYER_MOVEMENT_SPEED_MULT = -0.65;
    private static final double PLAYER_ATTACK_SPEED_MULT = -0.5;
    private static final double PLAYER_GRAVITY_MULT = -0.75;
    private static final double PLAYER_JUMP_STRENGTH_MULT = -0.5;

    private static final Identifier MOVEMENT_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("rituals", "temporal_slow_movement");
    private static final Identifier ATTACK_SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("rituals", "temporal_slow_attack_speed");
    private static final Identifier GRAVITY_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("rituals", "temporal_slow_gravity");
    private static final Identifier JUMP_STRENGTH_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("rituals", "temporal_slow_jump_strength");

    private static final class TemporalZone {
        final UUID ownerId;
        final ServerLevel level;
        final Vec3 center;
        int ticksRemaining = DURATION_TICKS;
        final Map<Integer, Integer> sparkCooldowns = new HashMap<>();
        final Map<Integer, SparkEntity> orbitingSparks = new HashMap<>();
        final Map<Integer, Double> orbitAngles = new HashMap<>();
        final Map<Integer, Integer> particleCooldowns = new HashMap<>();
        final Set<UUID> slowedPlayers = new HashSet<>();

        final Map<UUID, Boolean> playerWasOnGround = new HashMap<>();
        final Map<UUID, Boolean> playerWasSwinging = new HashMap<>();

        TemporalZone(UUID ownerId, ServerLevel level, Vec3 center) {
            this.ownerId = ownerId;
            this.level = level;
            this.center = center;
        }
    }

    private static final List<TemporalZone> ACTIVE_ZONES = new ArrayList<>();

    public static void handle(TemporalSlowZonePacket pkt, ServerPlayNetworking.Context ctx) {
        ServerPlayer player = ctx.player();
        if (MuteTracker.isMuted(player.getUUID())) return;
        ctx.server().execute(() -> {
            var held = player.getMainHandItem();
            if (!(held.getItem() instanceof TemporalGlassreaverItem)) return;
            if (ModDataComponents.getStage(held) < REQUIRED_STAGE) return;

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.TIME_WARP, SoundSource.PLAYERS, 1.0F, 1.0F);

            ACTIVE_ZONES.add(new TemporalZone(player.getUUID(), (ServerLevel) player.level(), player.position()));

            TemporalSlowZoneGroundEntity decal =
                    new TemporalSlowZoneGroundEntity(ModEntities.TEMPORAL_SLOW_ZONE_GROUND, player.level());
            decal.setPos(player.getX(), player.getY(), player.getZ());
            decal.setRadius((float) RADIUS);
            decal.setDurationTicks(DURATION_TICKS);
            ((ServerLevel) player.level()).addFreshEntity(decal);

            ScreenShakeEntity shake = new ScreenShakeEntity(player.level(),
                    player.position(), (float) RADIUS * 2.5f, 0.4f, 20);
            ((ServerLevel) player.level()).addFreshEntity(shake);
        });
    }

    public static void startServerZones(MinecraftServer server) {
        Set<Integer> slowed           = new HashSet<>();
        Set<Integer> slowedProjectiles = new HashSet<>();

        for (TemporalZone zone : ACTIVE_ZONES) {
            AABB box = new AABB(zone.center, zone.center).inflate(RADIUS);
            List<Entity> nearby = zone.level.getEntities((Entity) null, box,
                    e -> e.isAlive() && !(e instanceof Player) && !(e instanceof SparkEntity)
                            && !e.getUUID().equals(zone.ownerId)
                            && e.position().closerThan(zone.center, RADIUS));

            for (Entity e : nearby) {
                int id = e.getId();
                boolean isNewSlow = false;

                if (e instanceof Projectile proj) {
                    Entity shooter = proj.getOwner();
                    if (shooter != null && shooter.getUUID().equals(zone.ownerId)) continue;
                    slowedProjectiles.add(id);

                    if (!TemporalFreezeRegistry.isSlowedProjectile(id)) {
                        isNewSlow = true;
                    }
                } else {
                    slowed.add(id);

                    if (!TemporalFreezeRegistry.isSlowed(id)) {
                        isNewSlow = true;
                    }
                }

                if (isNewSlow) {
                    float volume = 0.45F + zone.level.getRandom().nextFloat() * 0.1F;
                    float pitch = 0.85F + zone.level.getRandom().nextFloat() * 0.3F;
                    zone.level.playSound(null, e.getX(), e.getY(), e.getZ(),
                            ModSounds.SLOW_SOUND, SoundSource.NEUTRAL, volume, pitch);
                }
            }
        }

        TemporalFreezeRegistry.setSlowedThisTick(slowed);
        TemporalFreezeRegistry.setSlowedProjectilesThisTick(slowedProjectiles);
    }

    public static void tickServerZones(MinecraftServer server) {
        Iterator<TemporalZone> it = ACTIVE_ZONES.iterator();
        while (it.hasNext()) {
            TemporalZone zone = it.next();
            zone.ticksRemaining--;
            spawnZoneVisuals(zone);

            Set<UUID> playersInRangeNow = new HashSet<>();
            for (ServerPlayer p : zone.level.getPlayers(p ->
                    !p.getUUID().equals(zone.ownerId) && p.position().closerThan(zone.center, RADIUS))) {

                UUID uuid = p.getUUID();
                playersInRangeNow.add(uuid);

                if (!zone.slowedPlayers.contains(uuid)) {
                    float volume = 0.6F + zone.level.getRandom().nextFloat() * 0.1F;
                    float pitch = 0.75F + zone.level.getRandom().nextFloat() * 0.2F;
                    zone.level.playSound(null, p.getX(), p.getY(), p.getZ(),
                            ModSounds.SLOW_SOUND, SoundSource.PLAYERS, volume, pitch);
                }

                applySlow(p);
                zone.slowedPlayers.add(uuid);

                // Sound Trigger: Player attacks/swings inside the zone
                boolean isSwinging = p.swinging;
                boolean wasSwinging = zone.playerWasSwinging.getOrDefault(uuid, false);
                zone.playerWasSwinging.put(uuid, isSwinging);

                if (isSwinging && !wasSwinging) {
                    float volume = 0.4F + zone.level.getRandom().nextFloat() * 0.1F;
                    float pitch = 0.6F + zone.level.getRandom().nextFloat() * 0.25F;
                    zone.level.playSound(null, p.getX(), p.getY(), p.getZ(),
                            ModSounds.SLOW_SOUND, SoundSource.PLAYERS, volume, pitch);
                }

                boolean isOnGround = p.onGround();
                boolean wasOnGround = zone.playerWasOnGround.getOrDefault(uuid, true);
                zone.playerWasOnGround.put(uuid, isOnGround);

                if (wasOnGround && !isOnGround && p.getDeltaMovement().y > 0.0) {
                    float volume = 0.4F + zone.level.getRandom().nextFloat() * 0.1F;
                    float pitch = 0.65F + zone.level.getRandom().nextFloat() * 0.25F;
                    zone.level.playSound(null, p.getX(), p.getY(), p.getZ(),
                            ModSounds.SLOW_SOUND, SoundSource.PLAYERS, volume, pitch);
                }
            }

            for (UUID uuid : new HashSet<>(zone.slowedPlayers)) {
                if (!playersInRangeNow.contains(uuid)) {
                    ServerPlayer p = server.getPlayerList().getPlayer(uuid);
                    if (p != null) removeSlow(p);
                    zone.slowedPlayers.remove(uuid);
                    zone.playerWasOnGround.remove(uuid);
                    zone.playerWasSwinging.remove(uuid);
                }
            }

            if (zone.ticksRemaining <= 0) {
                for (UUID uuid : zone.slowedPlayers) {
                    ServerPlayer p = server.getPlayerList().getPlayer(uuid);
                    if (p != null) removeSlow(p);
                }
                it.remove();
            }
        }
    }

    private static void spawnZoneVisuals(TemporalZone zone) {
        AABB box = new AABB(zone.center, zone.center).inflate(RADIUS);
        List<Entity> nearby = zone.level.getEntities((Entity) null, box, e ->
                e.isAlive()
                        && !e.getUUID().equals(zone.ownerId)
                        && e.position().closerThan(zone.center, RADIUS)
                        && (TemporalFreezeRegistry.isSlowed(e.getId())
                        || TemporalFreezeRegistry.isSlowedProjectile(e.getId())));

        List<Entity> affectedProjectiles = new ArrayList<>();
        Set<Integer> affectedEntityIds = new HashSet<>();
        Map<Integer, Entity> affectedEntitiesById = new HashMap<>();

        for (Entity e : nearby) {
            if (TemporalFreezeRegistry.isSlowedProjectile(e.getId())) {
                affectedProjectiles.add(e);
            } else {
                affectedEntityIds.add(e.getId());
                affectedEntitiesById.put(e.getId(), e);
            }
        }

        tickProjectileSparks(zone, affectedProjectiles);
        tickOrbitingSparks(zone, affectedEntityIds, affectedEntitiesById);
        tickEntityParticles(zone, affectedEntityIds, affectedEntitiesById);
    }

    private static void tickProjectileSparks(TemporalZone zone, List<Entity> affectedProjectiles) {
        if (affectedProjectiles.isEmpty()) {
            zone.sparkCooldowns.clear();
            return;
        }

        Set<Integer> currentIds = new HashSet<>();
        List<Entity> due = new ArrayList<>();

        for (Entity e : affectedProjectiles) {
            int id = e.getId();
            currentIds.add(id);

            int cd = zone.sparkCooldowns.getOrDefault(id, 0) - 1;
            if (cd <= 0) {
                due.add(e);
                zone.sparkCooldowns.put(id, 0);
            } else {
                zone.sparkCooldowns.put(id, cd);
            }
        }

        zone.sparkCooldowns.keySet().retainAll(currentIds);

        if (due.isEmpty()) return;

        RandomSource rng = zone.level.getRandom();
        int picks = Math.min(due.size(), MAX_PROJECTILE_SPARKS_PER_TICK);
        for (int i = 0; i < picks; i++) {
            int j = i + rng.nextInt(due.size() - i);
            Collections.swap(due, i, j);
        }

        for (int i = 0; i < picks; i++) {
            Entity e = due.get(i);
            spawnBurstSpark(zone, e, rng);
            zone.sparkCooldowns.put(e.getId(), 1 + rng.nextInt(2));
        }
    }

    private static void spawnBurstSpark(TemporalZone zone, Entity e, RandomSource rng) {
        double midX = e.getX();
        double midY = e.getY() + (e.getBbHeight() / 2.0);
        double midZ = e.getZ();

        double theta = rng.nextDouble() * Math.PI * 2;
        double phi   = rng.nextDouble() * Math.PI;
        double dirX  = Math.sin(phi) * Math.cos(theta);
        double dirY  = Math.cos(phi);
        double dirZ  = Math.sin(phi) * Math.sin(theta);

        double jitterAmount = 0.15;
        double spawnX = midX + (rng.nextDouble() - 0.5) * jitterAmount;
        double spawnY = midY + (rng.nextDouble() - 0.5) * jitterAmount;
        double spawnZ = midZ + (rng.nextDouble() - 0.5) * jitterAmount;

        SparkEntity spark = new SparkEntity(ModEntities.SPARK, zone.level, spawnX, spawnY, spawnZ);
        spark.applyPreset(SparkPresets.TEMPORAL_SLOW_ZONE_SINGLE);

        double speed = 0.1 + rng.nextDouble() * 0.06;
        spark.forcedVelocity = new Vec3(dirX * speed, dirY * speed, dirZ * speed);
        spark.setNoGravity(true);

        zone.level.addFreshEntity(spark);
    }

    private static void tickOrbitingSparks(TemporalZone zone, Set<Integer> affectedEntityIds,
                                           Map<Integer, Entity> affectedEntitiesById) {
        Iterator<Map.Entry<Integer, SparkEntity>> it = zone.orbitingSparks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, SparkEntity> entry = it.next();
            if (!affectedEntityIds.contains(entry.getKey()) || !entry.getValue().isAlive()) {
                if (entry.getValue().isAlive()) entry.getValue().discard();
                it.remove();
                zone.orbitAngles.remove(entry.getKey());
            }
        }

        RandomSource rng = zone.level.getRandom();

        for (int id : affectedEntityIds) {
            Entity target = affectedEntitiesById.get(id);
            if (target == null) continue;

            double angle = zone.orbitAngles.getOrDefault(id, rng.nextDouble() * Math.PI * 2) + ORBIT_SPEED;
            zone.orbitAngles.put(id, angle);

            Vec3 pos = target.position();
            double radius = ORBIT_RADIUS + Math.max(target.getBbWidth() - 0.6, 0) * 0.3;
            double sx = pos.x + Math.cos(angle) * radius;
            double sz = pos.z + Math.sin(angle) * radius;
            double sy = pos.y + ORBIT_VERTICAL_BASE + target.getBbHeight() * 0.25;

            SparkEntity spark = zone.orbitingSparks.get(id);
            if (spark == null || !spark.isAlive()) {
                spark = new SparkEntity(ModEntities.SPARK, zone.level, sx, sy, sz);
                spark.applyPreset(SparkPresets.TEMPORAL_SLOW_ZONE_SINGLE);
                spark.applyPreset(SparkPresets.TEMPORAL_SLOW_ZONE_SINGLE);
                spark.setNoGravity(true);
                spark.setDeltaMovement(Vec3.ZERO);
                spark.forcedVelocity = Vec3.ZERO;
                zone.level.addFreshEntity(spark);
                zone.orbitingSparks.put(id, spark);
            } else {
                spark.setPos(sx, sy, sz);
                spark.setDeltaMovement(Vec3.ZERO);
                spark.forcedVelocity = Vec3.ZERO;
            }
        }
    }

    private static void tickEntityParticles(TemporalZone zone, Set<Integer> affectedEntityIds,
                                            Map<Integer, Entity> affectedEntitiesById) {
        if (affectedEntityIds.isEmpty()) {
            zone.particleCooldowns.clear();
            return;
        }

        List<Entity> due = new ArrayList<>();

        for (int id : affectedEntityIds) {
            int cd = zone.particleCooldowns.getOrDefault(id, 0) - 1;
            if (cd <= 0) {
                due.add(affectedEntitiesById.get(id));
                zone.particleCooldowns.put(id, 0);
            } else {
                zone.particleCooldowns.put(id, cd);
            }
        }

        zone.particleCooldowns.keySet().retainAll(affectedEntityIds);

        if (due.isEmpty()) return;

        RandomSource rng = zone.level.getRandom();
        int picks = Math.min(due.size(), MAX_PARTICLES_PER_TICK);
        for (int i = 0; i < picks; i++) {
            int j = i + rng.nextInt(due.size() - i);
            Collections.swap(due, i, j);
        }

        for (int i = 0; i < picks; i++) {
            Entity e = due.get(i);
            spawnBurstParticle(zone, e, rng);
            zone.particleCooldowns.put(e.getId(), 6 + rng.nextInt(4));
        }
    }

    private static void spawnBurstParticle(TemporalZone zone, Entity e, RandomSource rng) {
        double theta = rng.nextDouble() * Math.PI * 2;
        double phi   = rng.nextDouble() * Math.PI;
        double dirX  = Math.sin(phi) * Math.cos(theta);
        double dirY  = Math.cos(phi);
        double dirZ  = Math.sin(phi) * Math.sin(theta);

        double width  = Math.max(e.getBbWidth(), 0.4) * 0.6;
        double height = Math.max(e.getBbHeight(), 0.4) * 0.5;

        double spawnX = e.getX() + dirX * width;
        double spawnY = e.getY() + height + dirY * height;
        double spawnZ = e.getZ() + dirZ * width;

        double speed = 0.04 + rng.nextDouble() * 0.02;

        zone.level.sendParticles(ModParticles.TEMPORAL_HOURGLASS,
                spawnX, spawnY, spawnZ,
                0,
                dirX * speed, dirY * speed * 0.5 + 0.01, dirZ * speed,
                1.0);
    }

    private static void applySlow(LivingEntity entity) {
        var speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null && speedAttr.getModifier(MOVEMENT_MODIFIER_ID) == null) {
            speedAttr.addTransientModifier(new AttributeModifier(
                    MOVEMENT_MODIFIER_ID, PLAYER_MOVEMENT_SPEED_MULT,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        var atkSpeedAttr = entity.getAttribute(Attributes.ATTACK_SPEED);
        if (atkSpeedAttr != null && atkSpeedAttr.getModifier(ATTACK_SPEED_MODIFIER_ID) == null) {
            atkSpeedAttr.addTransientModifier(new AttributeModifier(
                    ATTACK_SPEED_MODIFIER_ID, PLAYER_ATTACK_SPEED_MULT,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        var gravityAttr = entity.getAttribute(Attributes.GRAVITY);
        if (gravityAttr != null && gravityAttr.getModifier(GRAVITY_MODIFIER_ID) == null) {
            gravityAttr.addTransientModifier(new AttributeModifier(
                    GRAVITY_MODIFIER_ID, PLAYER_GRAVITY_MULT,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        var jumpAttr = entity.getAttribute(Attributes.JUMP_STRENGTH);
        if (jumpAttr != null && jumpAttr.getModifier(JUMP_STRENGTH_MODIFIER_ID) == null) {
            jumpAttr.addTransientModifier(new AttributeModifier(
                    JUMP_STRENGTH_MODIFIER_ID, PLAYER_JUMP_STRENGTH_MULT,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void removeSlow(LivingEntity entity) {
        var speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(MOVEMENT_MODIFIER_ID);
        var atkSpeedAttr = entity.getAttribute(Attributes.ATTACK_SPEED);
        if (atkSpeedAttr != null) atkSpeedAttr.removeModifier(ATTACK_SPEED_MODIFIER_ID);
        var gravityAttr = entity.getAttribute(Attributes.GRAVITY);
        if (gravityAttr != null) gravityAttr.removeModifier(GRAVITY_MODIFIER_ID);
        var jumpAttr = entity.getAttribute(Attributes.JUMP_STRENGTH);
        if (jumpAttr != null) jumpAttr.removeModifier(JUMP_STRENGTH_MODIFIER_ID);
    }
}