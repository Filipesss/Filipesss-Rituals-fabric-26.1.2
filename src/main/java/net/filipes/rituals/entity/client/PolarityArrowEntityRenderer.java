package net.filipes.rituals.entity.client;


import net.filipes.rituals.entity.custom.PolarityArrowEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;

public class PolarityArrowEntityRenderer extends ArrowRenderer<PolarityArrowEntity, PolarityArrowRenderState> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals",
                    "textures/entity/projectiles/polarity_arrow.png");

    public PolarityArrowEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public PolarityArrowRenderState createRenderState() {
        return new PolarityArrowRenderState();
    }

    @Override
    public void extractRenderState(PolarityArrowEntity entity, PolarityArrowRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.trail.clear();
        double cx = entity.getX(), cy = entity.getY(), cz = entity.getZ();
        int size = entity.trailSize;
        for (int i = 0; i < size; i++) {
            int idx = Math.floorMod(entity.trailHead - 1 - i, PolarityArrowEntity.TRAIL_LENGTH);
            Vec3 wp = entity.trailPositions[idx];
            if (wp != null) {
                state.trail.add(new Vec3(wp.x - cx, wp.y - cy, wp.z - cz));
            }
        }

        Vec3 camWorld = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        state.cameraOffset = camWorld.subtract(cx, cy, cz);
    }

    @Override
    public void submit(PolarityArrowRenderState state, PoseStack matrices,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        drawTrail(state, matrices, collector);
        super.submit(state, matrices, collector, camera);
    }

    @Override
    public Identifier getTextureLocation(PolarityArrowRenderState state) {
        return TEXTURE;
    }

    private static void drawTrail(PolarityArrowRenderState state, PoseStack ps, SubmitNodeCollector snc) {
        if (state.trail.size() < 2) return;

        List<Vec3> raw = new ArrayList<>();
        raw.add(Vec3.ZERO);
        raw.addAll(state.trail);

        List<Vec3> smooth = catmullRomSubdivide(raw, 6);
        if (smooth.size() < 2) return;

        Vec3 cam = state.cameraOffset;
        float maxWidth = 0.1f;
        int totalSegs = smooth.size() - 1;

        for (int i = 0; i < totalSegs; i++) {
            Vec3 a = smooth.get(i);
            Vec3 b = smooth.get(i + 1);

            float tA = i       / (float) totalSegs;
            float tB = (i + 1) / (float) totalSegs;
            float alphaA = 1.0f - tA;
            float alphaB = 1.0f - tB;
            if (alphaA < 0.02f && alphaB < 0.02f) continue;

            Vec3 dir = b.subtract(a);
            if (dir.lengthSqr() < 1e-8) continue;
            dir = dir.normalize();

            Vec3 mid    = a.add(b).scale(0.5);
            Vec3 toCam  = cam.subtract(mid);
            double camDistSq = toCam.lengthSqr();
            if (camDistSq < 1e-6) continue;
            toCam = toCam.scale(1.0 / Math.sqrt(camDistSq));

            Vec3 right = dir.cross(toCam);
            double rLen = right.lengthSqr();
            if (rLen < 1e-6) continue;
            right = right.scale(1.0 / Math.sqrt(rLen));

            Vec3 offA = right.scale(maxWidth * alphaA * 0.5);
            Vec3 offB = right.scale(maxWidth * alphaB * 0.5);

            Vec3 a0 = a.add(offA), a1 = a.subtract(offA);
            Vec3 b0 = b.add(offB), b1 = b.subtract(offB);

            final int iaA = (int)(alphaA * 185);
            final int iaB = (int)(alphaB * 185);
            final Vec3 fa0=a0, fa1=a1, fb0=b0, fb1=b1;

            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) -> {

                v.addVertex(pose, (float)fa0.x, (float)fa0.y, (float)fa0.z).setColor(20, 210, 165, iaA);
                v.addVertex(pose, (float)fa1.x, (float)fa1.y, (float)fa1.z).setColor(20, 210, 165, iaA);
                v.addVertex(pose, (float)fb1.x, (float)fb1.y, (float)fb1.z).setColor(20, 210, 165, iaB);
                v.addVertex(pose, (float)fb0.x, (float)fb0.y, (float)fb0.z).setColor(20, 210, 165, iaB);

                v.addVertex(pose, (float)fb0.x, (float)fb0.y, (float)fb0.z).setColor(20, 210, 165, iaB);
                v.addVertex(pose, (float)fb1.x, (float)fb1.y, (float)fb1.z).setColor(20, 210, 165, iaB);
                v.addVertex(pose, (float)fa1.x, (float)fa1.y, (float)fa1.z).setColor(20, 210, 165, iaA);
                v.addVertex(pose, (float)fa0.x, (float)fa0.y, (float)fa0.z).setColor(20, 210, 165, iaA);
            });
        }
    }

    private static List<Vec3> catmullRomSubdivide(List<Vec3> pts, int steps) {
        List<Vec3> padded = new ArrayList<>();
        padded.add(pts.get(0));
        padded.addAll(pts);
        padded.add(pts.get(pts.size() - 1));

        List<Vec3> out = new ArrayList<>();
        for (int i = 1; i < padded.size() - 2; i++) {
            Vec3 p0 = padded.get(i - 1), p1 = padded.get(i);
            Vec3 p2 = padded.get(i + 1), p3 = padded.get(i + 2);
            out.add(p1);
            for (int j = 1; j < steps; j++) {
                out.add(catmullRom(p0, p1, p2, p3, j / (float) steps));
            }
        }
        out.add(padded.get(padded.size() - 2));
        return out;
    }

    private static Vec3 catmullRom(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
        float t2 = t * t, t3 = t2 * t;
        return new Vec3(
                0.5*((2*p1.x)+(-p0.x+p2.x)*t+(2*p0.x-5*p1.x+4*p2.x-p3.x)*t2+(-p0.x+3*p1.x-3*p2.x+p3.x)*t3),
                0.5*((2*p1.y)+(-p0.y+p2.y)*t+(2*p0.y-5*p1.y+4*p2.y-p3.y)*t2+(-p0.y+3*p1.y-3*p2.y+p3.y)*t3),
                0.5*((2*p1.z)+(-p0.z+p2.z)*t+(2*p0.z-5*p1.z+4*p2.z-p3.z)*t2+(-p0.z+3*p1.z-3*p2.z+p3.z)*t3)
        );
    }
}