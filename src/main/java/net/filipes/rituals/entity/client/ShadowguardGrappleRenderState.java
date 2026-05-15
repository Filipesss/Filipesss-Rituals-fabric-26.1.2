package net.filipes.rituals.entity.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ShadowguardGrappleRenderState extends EntityRenderState {
    public float ageInTicks;
    public boolean hooked;
    public ItemStack stack = ItemStack.EMPTY;
    public ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public float yRot;
    public Vec3 ownerPos = Vec3.ZERO;       // for chain start
    public Vec3 entityPos = Vec3.ZERO;      // for chain end
}