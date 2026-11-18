package dev.voidechoes.client;

import dev.voidechoes.VoidEchoes;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Draws the normal sword model, then, in hand, two nested end-crystal style glass
 * frames spinning around the blade. Right after a blink they spin fast and
 * slow back down as the cooldown runs out.
 */
public class VoidcrystalSwordRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    public static final Identifier INNER_MODEL = VoidEchoes.id("item/voidcrystal_sword_inner");
    private static final Identifier GLASS_TEXTURE = Identifier.ofVanilla("textures/entity/end_crystal/end_crystal.png");
    private static final float SIN_45 = (float) Math.sin(Math.PI / 4);

    /** degrees per second at rest, and the extra multiplier right after a blink */
    private static final float BASE_SPEED = 70f, BOOST = 10f;

    private final ModelPart glass;
    private float angle;
    private static final boolean DEBUG = false;
    private long lastLog;
    private long lastNanos;

    public VoidcrystalSwordRenderer() {
        ModelData data = new ModelData();
        data.getRoot().addChild("glass", ModelPartBuilder.create().uv(0, 0).cuboid(-4, -4, -4, 8, 8, 8), ModelTransform.NONE);
        this.glass = TexturedModelData.of(data, 64, 32).createModel().getChild("glass");
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertices,
                       int light, int overlay) {
        MinecraftClient mc = MinecraftClient.getInstance();
        BakedModel model = mc.getBakedModelManager().getModel(INNER_MODEL);
        boolean left = mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND;

        matrices.push();
        matrices.translate(0.5f, 0.5f, 0.5f);
        mc.getItemRenderer().renderItem(stack, mode, left, matrices, vertices, light, overlay, model);
        matrices.pop();

        if (!mode.isFirstPerson() && mode != ModelTransformationMode.THIRD_PERSON_LEFT_HAND
                && mode != ModelTransformationMode.THIRD_PERSON_RIGHT_HAND) {
            return;
        }

        // Advance the spin once per frame, faster while the blink is on cooldown.
        long now = System.nanoTime();
        float dt = lastNanos == 0 ? 0f : Math.min(0.1f, (now - lastNanos) / 1.0e9f);
        if (dt < 0f) dt = 0f;   // clock went backwards: just resync
        if (dt > 0.001f || lastNanos == 0 || now < lastNanos) {
            lastNanos = now;
            float cooldown = 0f;
            if (mc.player != null) {
                cooldown = mc.player.getItemCooldownManager().getCooldownProgress(stack.getItem(),
                        mc.getRenderTickCounter().getTickDelta(true));
            }
            float k = cooldown;
            angle = (angle + dt * BASE_SPEED * (1f + BOOST * k)) % 360f;
        }
        if (DEBUG && now - lastLog > 1_000_000_000L) {
            lastLog = now;
            dev.voidechoes.VoidEchoes.LOGGER.info("[sword-debug] mode={} angle={} dt={}", mode, angle, dt);
        }

        matrices.push();
        matrices.translate(0.5f, 0.5f, 0.5f);
        model.getTransformation().getTransformation(mode).apply(left, matrices);
        matrices.translate(-0.5f, -0.5f, -0.5f);
        // Centre of the blade in item-texture space (x right, y up).
        matrices.translate(0.66f, 0.66f, 0.5f);
        VertexConsumer vc = vertices.getBuffer(RenderLayer.getEntityCutoutNoCull(GLASS_TEXTURE));
        int full = 0xF000F0;

        matrices.push();
        // 8px glass cube is 0.5 of the item; a bit smaller in first person where the item fills the view
        float size = mode.isFirstPerson() ? 0.6f : 0.75f;
        matrices.scale(size, size, size);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
        matrices.multiply(new Quaternionf().setAngleAxis((float) Math.PI / 3f, SIN_45, 0f, SIN_45));
        glass.render(matrices, vc, full, OverlayTexture.DEFAULT_UV);
        matrices.scale(0.75f, 0.75f, 0.75f);
        matrices.multiply(new Quaternionf().setAngleAxis((float) Math.PI / 3f, SIN_45, 0f, SIN_45));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-angle * 1.4f));
        glass.render(matrices, vc, full, OverlayTexture.DEFAULT_UV);
        matrices.pop();

        matrices.pop();
    }
}
