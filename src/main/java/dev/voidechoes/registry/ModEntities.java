package dev.voidechoes.registry;

import dev.voidechoes.VoidEchoes;
import dev.voidechoes.entity.VoidWispEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;

public final class ModEntities {
    public static final EntityType<VoidWispEntity> VOID_WISP = Registry.register(Registries.ENTITY_TYPE,
            VoidEchoes.id("void_wisp"),
            EntityType.Builder.create(VoidWispEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.7f, 0.9f).eyeHeight(0.55f).maxTrackingRange(10)
                    .build("void_wisp"));

    public static void init() {
        FabricDefaultAttributeRegistry.register(VOID_WISP, VoidWispEntity.createAttributes());
        SpawnRestriction.register(VOID_WISP, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (type, world, reason, pos, random) -> world.getDifficulty() != Difficulty.PEACEFUL);
    }
}
