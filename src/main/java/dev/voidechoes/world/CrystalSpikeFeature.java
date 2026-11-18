package dev.voidechoes.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

/** A leaning, tapering spire with a rocky collar, optionally tipped with a different block. */
public class CrystalSpikeFeature extends Feature<CrystalSpikeFeature.Config> {
    public record Config(BlockStateProvider body, BlockStateProvider collar, BlockStateProvider tip,
                         IntProvider height, float lean) implements FeatureConfig {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockStateProvider.TYPE_CODEC.fieldOf("body").forGetter(Config::body),
                BlockStateProvider.TYPE_CODEC.fieldOf("collar").forGetter(Config::collar),
                BlockStateProvider.TYPE_CODEC.fieldOf("tip").forGetter(Config::tip),
                IntProvider.createValidatingCodec(2, 48).fieldOf("height").forGetter(Config::height),
                Codec.floatRange(0f, 1.5f).fieldOf("lean").forGetter(Config::lean)
        ).apply(i, Config::new));
    }

    public CrystalSpikeFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<Config> ctx) {
        StructureWorldAccess world = ctx.getWorld();
        Random random = ctx.getRandom();
        Config cfg = ctx.getConfig();
        BlockPos origin = ctx.getOrigin();
        BlockPos ground = origin.down();
        if (!world.getBlockState(ground).isOpaqueFullCube(world, ground)) return false;

        int height = cfg.height().get(random);
        double baseRadius = 1.0 + random.nextDouble() * 1.2 + height * 0.09;
        float angle = random.nextFloat() * MathHelper.TAU;
        double lean = cfg.lean() * (0.4 + random.nextDouble() * 0.6);
        double leanX = MathHelper.cos(angle) * lean, leanZ = MathHelper.sin(angle) * lean;

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int t = -3; t <= height; t++) {
            double frac = Math.max(0, t) / (double) height;
            double radius = baseRadius * Math.pow(1.0 - frac, 0.85) + 0.35;
            double cx = origin.getX() + 0.5 + leanX * Math.max(0, t);
            double cz = origin.getZ() + 0.5 + leanZ * Math.max(0, t);
            int r = MathHelper.ceil(radius);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    double px = Math.floor(cx) + dx + 0.5 - cx, pz = Math.floor(cz) + dz + 0.5 - cz;
                    double d2 = px * px + pz * pz;
                    if (d2 > radius * radius) continue;
                    pos.set(MathHelper.floor(cx) + dx, origin.getY() + t, MathHelper.floor(cz) + dz);
                    BlockState existing = world.getBlockState(pos);
                    if (!existing.isAir() && !existing.isReplaceable() && !existing.isOf(Blocks.END_STONE)) continue;
                    BlockState place;
                    if (t <= 0 || (t < 2 && d2 > (radius - 1) * (radius - 1))) {
                        place = cfg.collar().get(random, pos);
                    } else if (frac > 0.82) {
                        place = cfg.tip().get(random, pos);
                    } else {
                        place = cfg.body().get(random, pos);
                    }
                    this.setBlockState(world, pos, place);
                }
            }
        }
        // A few broken shards scattered around the base.
        for (int i = 0; i < 4 + random.nextInt(5); i++) {
            int ox = origin.getX() + random.nextInt(9) - 4, oz = origin.getZ() + random.nextInt(9) - 4;
            pos.set(ox, origin.getY() + 2, oz);
            for (int k = 0; k < 5 && world.isAir(pos.down()); k++) pos.move(0, -1, 0);
            if (world.isAir(pos) && world.getBlockState(pos.down()).isOpaqueFullCube(world, pos.down())) {
                this.setBlockState(world, pos, cfg.body().get(random, pos));
            }
        }
        return true;
    }
}
