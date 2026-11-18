package dev.voidechoes.client;

import dev.voidechoes.VoidEchoes;
import dev.voidechoes.entity.VoidWispEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class VoidWispRenderer extends MobEntityRenderer<VoidWispEntity, VoidWispModel> {
    private static final Identifier TEXTURE = VoidEchoes.id("textures/entity/void_wisp.png");
    private static final RenderLayer GLOW = RenderLayer.getEyes(VoidEchoes.id("textures/entity/void_wisp_glow.png"));

    public VoidWispRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new VoidWispModel(ctx.getPart(VoidWispModel.LAYER)), 0.3f);
        this.addFeature(new EyesFeatureRenderer<>(this) {
            @Override
            public RenderLayer getEyesTexture() {
                return GLOW;
            }
        });
    }

    @Override
    public Identifier getTexture(VoidWispEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLight(VoidWispEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected void scale(VoidWispEntity entity, MatrixStack matrices, float tickDelta) {
        float pulse = 1.0f + MathHelper.sin((entity.age + tickDelta) * 0.15f) * 0.04f;
        matrices.scale(pulse, pulse, pulse);
    }
}
