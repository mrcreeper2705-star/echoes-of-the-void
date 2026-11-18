package dev.voidechoes.client;

import dev.voidechoes.VoidEchoes;
import dev.voidechoes.item.AetherWingsItem;
import dev.voidechoes.registry.ModItems;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/** Client side of the Aether Wings: jump-to-flap input, the wing model on the back and a cooldown icon. */
public final class AetherWingsClient {
    private static final Identifier TEXTURE = VoidEchoes.id("textures/entity/aether_wings.png");
    private static boolean jumpWasDown, wasGliding;

    private AetherWingsClient() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.currentScreen != null) {
                jumpWasDown = false;
                return;
            }
            boolean down = client.options.jumpKey.isPressed();
            // Only a fresh press while already gliding: the press that opens the wings is not a flap.
            if (down && !jumpWasDown && wasGliding && AetherWingsItem.canFlap(client.player)) {
                ClientPlayNetworking.send(new AetherWingsItem.FlapPayload());
            }
            jumpWasDown = down;
            wasGliding = client.player.isFallFlying();
        });

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
            if (renderer instanceof PlayerEntityRenderer || renderer instanceof ArmorStandEntityRenderer) {
                helper.register(new WingsFeature<>(renderer, context.getModelLoader()));
            }
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            ItemStack chest = mc.player.getEquippedStack(EquipmentSlot.CHEST);
            if (!chest.isOf(ModItems.AETHER_WINGS)) return;
            int x = ctx.getScaledWindowWidth() / 2 - 91 - 26;
            int y = ctx.getScaledWindowHeight() - 20;
            ctx.drawItem(chest, x, y);
            ctx.drawItemInSlot(mc.textRenderer, chest, x, y);   // draws the cooldown sweep and durability
        });
    }

    private static final class WingsFeature<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
        private final ElytraEntityModel<T> wings;

        WingsFeature(FeatureRendererContext<T, M> context, EntityModelLoader loader) {
            super(context);
            this.wings = new ElytraEntityModel<>(loader.getModelPart(EntityModelLayers.ELYTRA));
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, T entity, float limbAngle,
                           float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
            ItemStack chest = entity.getEquippedStack(EquipmentSlot.CHEST);
            if (!chest.isOf(ModItems.AETHER_WINGS)) return;
            matrices.push();
            matrices.translate(0.0F, 0.0F, 0.125F);
            this.getContextModel().copyStateTo(this.wings);
            this.wings.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
            VertexConsumer vc = ItemRenderer.getArmorGlintConsumer(vertices, RenderLayer.getArmorCutoutNoCull(TEXTURE), chest.hasGlint());
            this.wings.render(matrices, vc, light, OverlayTexture.DEFAULT_UV);
            // A faint glowing pass so the star veins shine in the dark.
            this.wings.render(matrices, vertices.getBuffer(RenderLayer.getEyes(VoidEchoes.id("textures/entity/aether_wings_glow.png"))),
                    light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }
    }
}
