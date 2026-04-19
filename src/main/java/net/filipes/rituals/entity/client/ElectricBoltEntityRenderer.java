package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.filipes.rituals.entity.custom.ElectricBoltEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ElectricBoltEntityRenderer extends EntityRenderer<ElectricBoltEntity, ElectricBoltEntityRenderer.ElectricBoltRenderState> {

    public static class ElectricBoltRenderState extends EntityRenderState {
        public Vec3 originLocal = new Vec3(0.0, 0.0, 0.0);
        public Vec3 direction = new Vec3(0.0, 1.0, 0.0);
        public float maxDistance = 0.0f;
        public float currentProgress = 0.0f;
        public float width = 0.12f;
        public int color = 0x98E8FF;
        public long seed = 0L;
    }
    private static final double MIN_VISIBLE_SEGMENT_RATIO = 0.55;
    private static final double SEGMENT_JOIN_INSET = 0.018;


    private static class PathData {
        final List<Vec3> points;
        final List<Double> cumulative;
        final double length;

        PathData(List<Vec3> points, List<Double> cumulative, double length) {
            this.points = points;
            this.cumulative = cumulative;
            this.length = length;
        }
    }

    private static class BoltPath {
        final List<Vec3> points;
        final double attachDistance;
        final float widthFactor;

        BoltPath(List<Vec3> points, double attachDistance, float widthFactor) {
            this.points = points;
            this.attachDistance = attachDistance;
            this.widthFactor = widthFactor;
        }
    }

    public ElectricBoltEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ElectricBoltRenderState createRenderState() {
        return new ElectricBoltRenderState();
    }

    @Override
    public void extractRenderState(ElectricBoltEntity entity, ElectricBoltRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        Vec3 current = new Vec3(state.x, state.y, state.z);

        state.originLocal = entity.getOrigin().subtract(current);

        Vec3 dir = entity.getBoltDirection();
        if (dir.lengthSqr() < 1.0e-6) {
            dir = new Vec3(0.0, 1.0, 0.0);
        } else {
            dir = dir.normalize();
        }
        state.direction = dir;

        state.maxDistance = entity.getDistance();

        float age = entity.tickCount - 1 + partialTicks;
        state.currentProgress = Math.min(state.maxDistance, Math.max(0.0f, age * entity.getSpeed()));

        state.width = entity.getWidth();
        state.color = entity.getColor();

        state.seed = entity.getUUID().getMostSignificantBits() ^ entity.getUUID().getLeastSignificantBits();
    }

    @Override
    public boolean affectedByCulling(ElectricBoltEntity entity) {
        return false;
    }

    @Override
    public void submit(ElectricBoltRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.maxDistance < 0.0001f || state.currentProgress <= 0.0001f) {
            return;
        }

        final Vec3 start = state.originLocal;
        final Vec3 dir = state.direction;
        final Vec3 end = start.add(dir.scale(state.maxDistance));
        final RandomSource random = RandomSource.create(state.seed);

        final List<BoltPath> paths = new ArrayList<>();

        PathData mainPath = generatePath(
                start,
                dir,
                state.maxDistance,
                random,
                14,
                0.12,
                0.90,
                false
        );
        paths.add(new BoltPath(mainPath.points, 0.0, 1.0f));

        addBranchesRecursive(
                paths,
                mainPath,
                0.0,
                dir,
                random,
                0
        );

        Vec3 helper = Math.abs(dir.y) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 right = dir.cross(helper);
        if (right.lengthSqr() < 1.0e-6) {
            helper = new Vec3(0.0, 0.0, 1.0);
            right = dir.cross(helper);
        }
        final Vec3 finalRight = right.normalize();
        final Vec3 finalUp = finalRight.cross(dir).normalize();

        final float outerWidth = state.width * 1.95f;
        final float coreWidth = state.width * 0.72f;
        final int outerColor = fadeColor(state.color, 0.42f);
        final int coreColor = tint(0xFFFFFF, 0.0f, 1.0f);

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.lightning(),
                (pose, vertices) -> {
                    for (BoltPath path : paths) {
                        double visibleLength = state.currentProgress - path.attachDistance;
                        if (visibleLength <= 0.0001) {
                            continue;
                        }

                        List<Vec3> visiblePoints = slicePath(path.points, visibleLength);
                        if (visiblePoints.size() < 2) {
                            continue;
                        }

                        float pathOuterWidth = outerWidth * path.widthFactor;
                        float pathCoreWidth = coreWidth * path.widthFactor;

                        drawBolt(visiblePoints, pose, vertices, finalRight, finalUp, pathOuterWidth, outerColor);
                        drawBolt(visiblePoints, pose, vertices, finalRight, finalUp, pathCoreWidth, coreColor);
                    }
                }
        );
    }

    @Override
    protected float getShadowRadius(ElectricBoltRenderState state) {
        return 0.0f;
    }

    @Override
    protected float getShadowStrength(ElectricBoltRenderState state) {
        return 0.0f;
    }

    private static void addBranchesRecursive(
            List<BoltPath> out,
            PathData parentPath,
            double parentAttachDistance,
            Vec3 parentDir,
            RandomSource random,
            int depth
    ) {
        if (depth >= 2) {
            return;
        }

        double branchChance = depth == 0 ? 0.30 : 0.18;
        int minIndex = 2;
        int maxIndex = parentPath.points.size() - 3;

        if (maxIndex <= minIndex) {
            return;
        }

        for (int i = minIndex; i <= maxIndex; i++) {
            if (random.nextFloat() > branchChance) {
                continue;
            }

            Vec3 attachPoint = parentPath.points.get(i);

            Vec3 tangent;
            if (i > 0 && i < parentPath.points.size() - 1) {
                tangent = parentPath.points.get(i + 1).subtract(parentPath.points.get(i - 1));
            } else {
                tangent = parentDir;
            }

            if (tangent.lengthSqr() < 1.0e-6) {
                tangent = parentDir;
            } else {
                tangent = tangent.normalize();
            }

            Vec3 helper = Math.abs(tangent.y) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
            Vec3 right = tangent.cross(helper);
            if (right.lengthSqr() < 1.0e-6) {
                helper = new Vec3(0.0, 0.0, 1.0);
                right = tangent.cross(helper);
            }
            right = right.normalize();
            Vec3 up = right.cross(tangent).normalize();

            Vec3 branchDir = tangent
                    .add(right.scale((random.nextDouble() - 0.5) * 0.95))
                    .add(up.scale((random.nextDouble() - 0.5) * 0.95))
                    .normalize();

            double branchLength = parentPath.length * (0.18 + random.nextDouble() * 0.30);
            int segments = Math.max(5, Math.min(12, (int) Math.ceil(branchLength / 0.28)));

            PathData branchPath = generatePath(
                    attachPoint,
                    branchDir,
                    branchLength,
                    random,
                    segments,
                    depth == 0 ? 0.18 : 0.14,
                    depth == 0 ? 1.15 : 1.25,
                    true
            );

            double attachDistance = parentAttachDistance + parentPath.cumulative.get(i);
            float widthFactor = depth == 0 ? 0.62f : 0.42f;

            out.add(new BoltPath(branchPath.points, attachDistance, widthFactor));

            if (depth < 1 && random.nextFloat() < 0.32f) {
                addBranchesRecursive(out, branchPath, attachDistance, branchDir, random, depth + 1);
            }
        }
    }

    private static PathData generatePath(
            Vec3 start,
            Vec3 direction,
            double length,
            RandomSource random,
            int segments,
            double jitter,
            double kinkStrength,
            boolean deadEnd
    ) {
        List<Vec3> points = new ArrayList<>();
        List<Double> cumulative = new ArrayList<>();

        if (direction.lengthSqr() < 1.0e-6) {
            direction = new Vec3(0.0, 1.0, 0.0);
        } else {
            direction = direction.normalize();
        }

        Vec3 helper = Math.abs(direction.y) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 right = direction.cross(helper);
        if (right.lengthSqr() < 1.0e-6) {
            helper = new Vec3(0.0, 0.0, 1.0);
            right = direction.cross(helper);
        }
        right = right.normalize();
        Vec3 up = right.cross(direction).normalize();

        points.add(start);
        cumulative.add(0.0);

        double offsetRight = 0.0;
        double offsetUp = 0.0;
        double traveled = 0.0;

        double baseStep = length / segments;

        for (int i = 1; i < segments; i++) {
            double t = (double) i / (double) segments;
            double envelope = 0.30 + 0.70 * Math.sin(Math.PI * t);

            offsetRight += (random.nextDouble() - 0.5) * jitter * kinkStrength;
            offsetUp += (random.nextDouble() - 0.5) * jitter * kinkStrength;

            offsetRight *= 0.74;
            offsetUp *= 0.74;

            double clamp = (jitter * 7.0) * envelope;
            offsetRight = clamp(offsetRight, -clamp, clamp);
            offsetUp = clamp(offsetUp, -clamp, clamp);

            if (random.nextDouble() < (deadEnd ? 0.34 : 0.22)) {
                offsetRight += (random.nextDouble() - 0.5) * clamp * 1.15;
                offsetUp += (random.nextDouble() - 0.5) * clamp * 1.15;
            }

            traveled += baseStep;

            Vec3 ideal = start.add(direction.scale(traveled));
            Vec3 point = ideal
                    .add(right.scale(offsetRight))
                    .add(up.scale(offsetUp));

            points.add(point);
            cumulative.add(traveled);
        }

        Vec3 end = start.add(direction.scale(length));

        if (deadEnd) {
            Vec3 deadKick = right.scale((random.nextDouble() - 0.5) * jitter * 2.0)
                    .add(up.scale((random.nextDouble() - 0.5) * jitter * 2.0));
            end = end.add(deadKick);
        }

        points.add(end);
        cumulative.add(length);

        return new PathData(points, cumulative, length);
    }

    private static List<Vec3> slicePath(List<Vec3> points, double visibleLength) {
        List<Vec3> out = new ArrayList<>();
        if (points.isEmpty()) {
            return out;
        }

        out.add(points.get(0));

        if (visibleLength <= 0.0001) {
            return out;
        }

        double travelled = 0.0;

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 a = points.get(i);
            Vec3 b = points.get(i + 1);
            double segLen = b.distanceTo(a);

            if (segLen < 1.0e-6) {
                continue;
            }

            if (travelled + segLen >= visibleLength) {
                double remain = visibleLength - travelled;
                double ratio = remain / segLen;

                if (ratio < MIN_VISIBLE_SEGMENT_RATIO) {
                    return out;
                }

                double t = clamp(ratio, 0.0, 1.0);
                Vec3 head = a.add(b.subtract(a).scale(t));
                out.add(head);
                return out;
            } else {
                out.add(b);
                travelled += segLen;
            }
        }

        return out;
    }

    private static void drawBolt(List<Vec3> points, PoseStack.Pose pose, VertexConsumer vertices, Vec3 right, Vec3 up, float width, int color) {
        if (points.size() < 2) {
            return;
        }

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 a = points.get(i);
            Vec3 b = points.get(i + 1);
            drawPrismSegment(pose, vertices, a, b, right, up, width, width, color);
        }
    }

    private static void drawPrismSegment(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            Vec3 a,
            Vec3 b,
            Vec3 right,
            Vec3 up,
            float widthA,
            float widthB,
            int color
    ) {
        if (a.subtract(b).lengthSqr() < 1.0e-8) {
            return;
        }

        Vec3 rA = right.scale(widthA * 0.5f);
        Vec3 uA = up.scale(widthA * 0.5f);

        Vec3 rB = right.scale(widthB * 0.5f);
        Vec3 uB = up.scale(widthB * 0.5f);

        Vec3 a1 = a.add(rA).add(uA);
        Vec3 a2 = a.subtract(rA).add(uA);
        Vec3 a3 = a.subtract(rA).subtract(uA);
        Vec3 a4 = a.add(rA).subtract(uA);

        Vec3 b1 = b.add(rB).add(uB);
        Vec3 b2 = b.subtract(rB).add(uB);
        Vec3 b3 = b.subtract(rB).subtract(uB);
        Vec3 b4 = b.add(rB).subtract(uB);

        drawQuad(pose, vertices, a1, a2, b2, b1, color);
        drawQuad(pose, vertices, a2, a3, b3, b2, color);
        drawQuad(pose, vertices, a3, a4, b4, b3, color);
        drawQuad(pose, vertices, a4, a1, b1, b4, color);
    }

    private static void drawQuad(PoseStack.Pose pose, VertexConsumer vertices, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color) {
        int alpha = (color >>> 24) & 0xFF;
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;

        vertices.addVertex(pose, (float) a.x, (float) a.y, (float) a.z).setColor(red, green, blue, alpha);
        vertices.addVertex(pose, (float) b.x, (float) b.y, (float) b.z).setColor(red, green, blue, alpha);
        vertices.addVertex(pose, (float) c.x, (float) c.y, (float) c.z).setColor(red, green, blue, alpha);
        vertices.addVertex(pose, (float) d.x, (float) d.y, (float) d.z).setColor(red, green, blue, alpha);
    }

    private static int tint(int color, float saturationBoost, float alphaScale) {
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;
        int alpha = (color >>> 24) & 0xFF;
        if (alpha == 0) alpha = 255;

        alpha = (int) (alpha * alphaScale);

        red = clamp((int) (red + (255 - red) * saturationBoost));
        green = clamp((int) (green + (255 - green) * saturationBoost));
        blue = clamp((int) (blue + (255 - blue) * saturationBoost));

        return (clamp(alpha) << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int fadeColor(int color, float scale) {
        int alpha = (color >>> 24) & 0xFF;
        if (alpha == 0) alpha = 255;
        alpha = clamp((int) (alpha * scale));
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}