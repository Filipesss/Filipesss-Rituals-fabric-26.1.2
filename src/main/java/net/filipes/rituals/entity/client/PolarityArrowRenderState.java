package net.filipes.rituals.entity.client;


import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

public class PolarityArrowRenderState extends ArrowRenderState {
    public final List<Vec3> trail = new ArrayList<>();
    public Vec3 cameraOffset = Vec3.ZERO; // camera pos relative to entity, for billboarding
}
