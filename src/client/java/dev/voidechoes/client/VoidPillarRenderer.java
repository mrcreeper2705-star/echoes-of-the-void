package dev.voidechoes.client;

import dev.voidechoes.storm.PillarPayload;
import dev.voidechoes.storm.VoidStorm;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws Void Pillars: a solid black cylinder from the void to the sky that swells in and
 * fades out within a second, with a white rim traced along its silhouette so the outline
 * reads from every side.
 */
public final class VoidPillarRenderer {
    private static final int SEGMENTS = 32;
    private static final List<Pillar> PILLARS = new ArrayList<>();

    private record Pillar(double x, double z, long start) {}

    private VoidPillarRenderer() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(PillarPayload.ID, (payload, ctx) -> {
            MinecraftClient client = ctx.client();
            if (client.world != null) PILLARS.add(new Pillar(payload.x(), payload.z(), client.world.getTime()));
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) {
                PILLARS.clear();
                return;
            }
            long now = client.world.getTime();
            PILLARS.removeIf(p -> now - p.start() > VoidStorm.LIFETIME + 2);
        });
        WorldRenderEvents.AFTER_ENTITIES.register(VoidPillarRenderer::render);
    }

    private static void render(WorldRenderContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (PILLARS.isEmpty() || client.world == null || ctx.consumers() == null) return;
        float tickDelta = ctx.tickCounter().getTickDelta(true);
        double now = client.world.getTime() + tickDelta;
        Vec3d cam = ctx.camera().getPos();
        MatrixStack stack = ctx.matrixStack() != null ? ctx.matrixStack() : new MatrixStack();
        Matrix4f m = stack.peek().getPositionMatrix();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderLayer.getDebugQuads());
        float y0 = (float) (client.world.getBottomY() - 64 - cam.y);
        float y1 = (float) (client.world.getTopY() + 64 - cam.y);

        for (Pillar p : PILLARS) {
            double age = now - p.start();
            if (age < 0 || age > VoidStorm.LIFETIME) continue;
            float t = (float) (age / VoidStorm.LIFETIME);
            // Smooth swell in, brief hold, smooth fade out.
            float alpha = MathHelper.clamp(MathHelper.sin(t * MathHelper.PI) * 1.6f, 0f, 1f);
            alpha = alpha * alpha * (3 - 2 * alpha);
            float r = (float) VoidStorm.RADIUS * (0.6f + 0.4f * MathHelper.clamp(t * 3f, 0f, 1f));
            float cx = (float) (p.x() - cam.x), cz = (float) (p.z() - cam.z);
            int a = (int) (alpha * 245);

            // Black body.
            for (int i = 0; i < SEGMENTS; i++) {
                float a0 = i * MathHelper.TAU / SEGMENTS, a1 = (i + 1) * MathHelper.TAU / SEGMENTS;
                float x0 = cx + MathHelper.cos(a0) * r, z0 = cz + MathHelper.sin(a0) * r;
                float x1 = cx + MathHelper.cos(a1) * r, z1 = cz + MathHelper.sin(a1) * r;
                quad(vc, m, x0, z0, x1, z1, y0, y1, 6, 4, 10, a);
            }

            // White rim along the silhouette as seen from the camera.
            float dist = MathHelper.sqrt(cx * cx + cz * cz);
            if (dist <= r + 0.05f) continue;
            float toCam = (float) Math.atan2(-cz, -cx);          // angle from pillar centre to camera
            float spread = (float) Math.acos(r / dist);           // tangent points either side
            float w = 0.12f + dist * 0.0035f;                     // stays visible from afar
            int wa = (int) (alpha * 255);
            for (int side = -1; side <= 1; side += 2) {
                float ang = toCam + side * spread;
                float nx = MathHelper.cos(ang), nz = MathHelper.sin(ang);
                float ix = cx + nx * r, iz = cz + nz * r;
                float ox = cx + nx * (r + w), oz = cz + nz * (r + w);
                quad(vc, m, ix, iz, ox, oz, y0, y1, 255, 255, 255, wa);
            }
        }
    }

    private static void quad(VertexConsumer vc, Matrix4f m, float x0, float z0, float x1, float z1,
                             float y0, float y1, int r, int g, int b, int a) {
        vc.vertex(m, x0, y0, z0).color(r, g, b, a);
        vc.vertex(m, x1, y0, z1).color(r, g, b, a);
        vc.vertex(m, x1, y1, z1).color(r, g, b, a);
        vc.vertex(m, x0, y1, z0).color(r, g, b, a);
    }
}
