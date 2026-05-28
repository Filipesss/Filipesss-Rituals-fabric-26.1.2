package net.filipes.rituals.entity.client;

import net.filipes.rituals.entity.custom.CinderArrowEntity;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

public class CinderArrowRenderState extends ArrowRenderState {
    public final List<Vec3> trail = new ArrayList<>();
    public int arrowType = CinderArrowEntity.TYPE_FIRE;
    public Vec3 cameraOffset = Vec3.ZERO;
}