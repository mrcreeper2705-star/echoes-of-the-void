package dev.voidechoes.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

/**
 * The End has no surface rules, so this repaints the top of end-stone islands
 * per column, once per chunk, only where the column belongs to the given biome.
 */
public class SurfaceCoverFeature extends Feature<SurfaceCoverFeature.Config> {
    public record Config(Identifier biome, BlockStateProvider top, BlockStateProvider under, int depth) implements FeatureConfig {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("biome").forGetter(Config::biome),
                BlockStateProvider.TYPE_CODEC.fieldOf("top").forGetter(Config::top),
                BlockStateProvider.TYPE_CODEC.fieldOf("under").forGetter(Config::under),
                Codec.intRange(0, 8).fieldOf("depth").forGetter(Config::depth)
        ).apply(i, Config::new));
    }

    public SurfaceCoverFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<Config> ctx) {
        StructureWorldAccess world = ctx.getWorld();
        Config cfg = ctx.getConfig();
        Random random = ctx.getRandom();
        int baseX = ctx.getOrigin().getX() & ~15;
        int baseZ = ctx.getOrigin().getZ() & ~15;
        BlockPos.Mutable pos = new BlockPos.Mutable();
        boolean any = false;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = baseX + dx, z = baseZ + dz;
                int y = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z) - 1;
                if (y <= world.getBottomY()) continue;
                pos.set(x, y, z);
                if (!world.getBlockState(pos).isOf(Blocks.END_STONE)) continue;
                if (!world.getBiome(pos).matchesId(cfg.biome())) continue;
                world.setBlockState(pos, cfg.top().get(random, pos), 2);
                // Slightly ragged depth so cliff faces show layered strata.
                int depth = cfg.depth() + (cfg.depth() > 0 ? random.nextInt(2) : 0);
                for (int d = 1; d <= depth; d++) {
                    pos.setY(y - d);
                    BlockState below = world.getBlockState(pos);
                    if (!below.isOf(Blocks.END_STONE)) break;
                    world.setBlockState(pos, cfg.under().get(random, pos), 2);
                }
                any = true;
            }
        }
        return any;
    }
}
