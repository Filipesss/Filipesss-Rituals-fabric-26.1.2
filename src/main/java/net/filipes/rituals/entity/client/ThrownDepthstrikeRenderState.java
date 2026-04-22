package net.filipes.rituals.entity.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ThrownDepthstrikeRenderState extends EntityRenderState {
    public ItemStack stack = ItemStack.EMPTY;
    public ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public float partialTick = 0f;
    public float yRot;
    public float xRot;
    public float yRotO;
    public float xRotO;

    // Trail and impact
    public List<Vec3> trail = new ArrayList<>();
    public boolean inGround = false;
    public float age = 0f;
    public long seed = 0L;
}