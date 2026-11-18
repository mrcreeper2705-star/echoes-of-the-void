package dev.voidechoes.client;

import dev.voidechoes.VoidEchoes;
import dev.voidechoes.entity.VoidWispEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

/**
 * Crystal core with a spinning halo, three orbiting shards and a two-segment
 * wisp tail. All motion is procedural, driven by age so it never stutters.
 */
public class VoidWispModel extends SinglePartEntityModel<VoidWispEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(VoidEchoes.id("void_wisp"), "main");
    private static final float CORE_Y = 12.0f;

    private final ModelPart root;
    private final ModelPart core;
    private final ModelPart halo;
    private final ModelPart tail;
    private final ModelPart tailTip;
    private final ModelPart[] shards = new ModelPart[3];

    public VoidWispModel(ModelPart root) {
        super(RenderLayer::getEntityTranslucent);
        this.root = root;
        this.core = root.getChild("core");
        this.halo = root.getChild("halo");
        this.tail = root.getChild("tail");
        this.tailTip = tail.getChild("tail_tip");
        for (int i = 0; i < 3; i++) shards[i] = root.getChild("shard" + i);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData r = data.getRoot();
        r.addChild("core", ModelPartBuilder.create().uv(0, 0).cuboid(-4, -4, -4, 8, 8, 8),
                ModelTransform.pivot(0, CORE_Y, 0));
        r.addChild("halo", ModelPartBuilder.create().uv(0, 32).cuboid(-6, 0, -6, 12, 0, 12),
                ModelTransform.pivot(0, CORE_Y, 0));
        for (int i = 0; i < 3; i++) {
            r.addChild("shard" + i, ModelPartBuilder.create().uv(32, 0).cuboid(-1, -2.5f, -1, 2, 5, 2),
                    ModelTransform.pivot(0, CORE_Y, 0));
        }
        ModelPartData tail = r.addChild("tail", ModelPartBuilder.create().uv(0, 16).cuboid(-2.5f, 0, -2.5f, 5, 5, 5),
                ModelTransform.pivot(0, CORE_Y + 3, 0));
        tail.addChild("tail_tip", ModelPartBuilder.create().uv(20, 16).cuboid(-1.5f, 0, -1.5f, 3, 4, 3),
                ModelTransform.pivot(0, 4.5f, 0));
        return TexturedModelData.of(data, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(VoidWispEntity entity, float limbAngle, float limbDistance, float t, float headYaw, float headPitch) {
        float bob = MathHelper.sin(t * 0.1f) * 1.4f;

        core.pivotY = CORE_Y + bob;
        // The eye tracks whatever the wisp is looking at, with only a faint idle sway.
        core.yaw = headYaw * MathHelper.RADIANS_PER_DEGREE + MathHelper.sin(t * 0.03f) * 0.05f;
        core.pitch = headPitch * MathHelper.RADIANS_PER_DEGREE + MathHelper.sin(t * 0.05f) * 0.06f;
        core.roll = MathHelper.cos(t * 0.04f) * 0.06f;

        halo.pivotY = CORE_Y + bob;
        halo.yaw = -t * 0.07f;
        halo.pitch = 0.3f + MathHelper.sin(t * 0.07f) * 0.12f;
        halo.roll = MathHelper.cos(t * 0.05f) * 0.12f;

        for (int i = 0; i < 3; i++) {
            float a = t * 0.09f + i * (MathHelper.TAU / 3f);
            float radius = 7.0f + MathHelper.sin(t * 0.06f + i) * 1.0f + limbDistance * 2.0f;
            ModelPart s = shards[i];
            s.pivotX = MathHelper.cos(a) * radius;
            s.pivotZ = MathHelper.sin(a) * radius;
            s.pivotY = CORE_Y + bob + MathHelper.sin(t * 0.13f + i * 1.7f) * 2.2f;
            s.yaw = -a;
            s.roll = 0.35f * MathHelper.sin(t * 0.1f + i);
        }

        tail.pivotY = CORE_Y + 3 + bob * 0.7f;
        tail.pitch = MathHelper.sin(t * 0.08f) * 0.25f + limbDistance * 0.7f;
        tail.roll = MathHelper.cos(t * 0.06f) * 0.18f;
        tailTip.pitch = MathHelper.sin(t * 0.08f - 0.9f) * 0.4f + limbDistance * 0.4f;
        tailTip.roll = MathHelper.cos(t * 0.06f - 0.9f) * 0.25f;
    }
}
