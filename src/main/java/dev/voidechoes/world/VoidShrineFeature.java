package dev.voidechoes.world;

import dev.voidechoes.VoidEchoes;
import dev.voidechoes.registry.ModBlocks;
import dev.voidechoes.registry.ModEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * A ruined open-air shrine that settles into the terrain: a round, eroded floor
 * laid flush with the ground, four broken glowing pillars, a toppled column and
 * scattered rubble, with a wisp spawner and two loot chests at its heart.
 * Nothing is carved out in a box: only the cells the ruin actually uses change.
 */
public class VoidShrineFeature extends Feature<DefaultFeatureConfig> {
    public static final RegistryKey<LootTable> LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, VoidEchoes.id("chests/void_shrine"));
    private static final int MAX = 5;
    private static final int[][] PILLARS = {{3, 1}, {-1, 3}, {-3, -1}, {1, -3}};

    public VoidShrineFeature() {
        super(DefaultFeatureConfig.CODEC);
    }

    /** Highest solid ground block within [y-4, y+4], ignoring plants, leaves and logs. */
    private static int ground(StructureWorldAccess world, int x, int y, int z) {
        BlockPos.Mutable p = new BlockPos.Mutable();
        for (int yy = y + 4; yy >= y - 4; yy--) {
            p.set(x, yy, z);
            BlockState s = world.getBlockState(p);
            if (s.isOpaqueFullCube(world, p) && !s.isIn(BlockTags.LEAVES) && !s.isIn(BlockTags.LOGS)) return yy;
        }
        return Integer.MIN_VALUE;
    }

    private static boolean soft(BlockState s) {
        return s.isAir() || s.isReplaceable() || s.isIn(BlockTags.LEAVES) || s.isIn(BlockTags.FLOWERS);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> ctx) {
        StructureWorldAccess world = ctx.getWorld();
        Random random = ctx.getRandom();
        BlockPos o = ctx.getOrigin();
        int base = ground(world, o.getX(), o.getY(), o.getZ());
        if (base == Integer.MIN_VALUE || base <= world.getBottomY() + 2) return false;

        // The site must be gently sloped and not hang over an island edge.
        for (int i = 0; i < 8; i++) {
            float a = i * MathHelper.TAU / 8;
            int g = ground(world, o.getX() + Math.round(MathHelper.cos(a) * 4), base, o.getZ() + Math.round(MathHelper.sin(a) * 4));
            if (g == Integer.MIN_VALUE || Math.abs(g - base) > 2) return false;
        }

        BlockState bricks = ModBlocks.VOIDSTONE_BRICKS.getDefaultState();
        BlockState stone = ModBlocks.VOIDSTONE.getDefaultState();
        BlockState chiseled = ModBlocks.CHISELED_VOIDSTONE.getDefaultState();
        BlockState moss = ModBlocks.VOID_MOSS.getDefaultState();
        BlockPos.Mutable p = new BlockPos.Mutable();

        // Irregular outline: a radius per 22.5 degree sector.
        float[] rim = new float[16];
        for (int i = 0; i < 16; i++) rim[i] = 3.4f + random.nextFloat() * 1.4f;

        for (int dx = -MAX; dx <= MAX; dx++) {
            for (int dz = -MAX; dz <= MAX; dz++) {
                float d = MathHelper.sqrt(dx * dx + dz * dz);
                int sector = (int) ((MathHelper.atan2(dz, dx) + Math.PI) / MathHelper.TAU * 16) & 15;
                float r = rim[sector];
                if (d > r) continue;
                int x = o.getX() + dx, z = o.getZ() + dz;
                int g = ground(world, x, base, z);
                if (g == Integer.MIN_VALUE) continue;               // over the void: leave the gap
                float edge = d - (r - 1.3f);
                if (edge > 0 && random.nextFloat() < edge * 0.75f) continue;  // eroded rim

                for (int y = g + 1; y < base; y++) {                // fill shallow dips
                    p.set(x, y, z);
                    if (soft(world.getBlockState(p))) setBlockState(world, p, stone);
                }
                for (int y = base + 1; y <= g; y++) {               // shave small bumps
                    p.set(x, y, z);
                    setBlockState(world, p, Blocks.AIR.getDefaultState());
                }
                BlockState floor;
                if (d < 1.1f) floor = chiseled;
                else if (Math.abs(d - 2.6f) < 0.5f && random.nextInt(3) == 0) floor = stone;
                else if (random.nextInt(9) == 0) floor = moss;      // overgrowth breaking through
                else floor = random.nextInt(7) == 0 ? stone : bricks;
                p.set(x, base, z);
                setBlockState(world, p, floor);
                p.set(x, base + 1, z);                              // clear grass and flowers on the floor
                if (world.getBlockState(p).isReplaceable()) setBlockState(world, p, Blocks.AIR.getDefaultState());
            }
        }

        // Broken pillars of different heights.
        for (int[] off : PILLARS) {
            int x = o.getX() + off[0], z = o.getZ() + off[1];
            p.set(x, base, z);
            setBlockState(world, p, bricks);
            int h = 1 + random.nextInt(5);
            for (int k = 1; k <= h; k++) {
                p.set(x, base + k, z);
                if (!soft(world.getBlockState(p))) break;
                setBlockState(world, p, k == h && h >= 3 ? chiseled : (random.nextInt(5) == 0 ? stone : bricks));
            }
            if (h >= 3 && random.nextInt(5) < 2) {
                p.set(x, base + h + 1, z);
                if (soft(world.getBlockState(p))) setBlockState(world, p, ModBlocks.RESONANT_CRYSTAL.getDefaultState());
            }
        }

        // One toppled column lying in the grass just outside the ring.
        Direction fall = Direction.Type.HORIZONTAL.random(random);
        Direction along = random.nextBoolean() ? fall.rotateYClockwise() : fall.rotateYCounterclockwise();
        int fx = o.getX() + fall.getOffsetX() * 5, fz = o.getZ() + fall.getOffsetZ() * 5;
        for (int k = 0; k < 2 + random.nextInt(3); k++) {
            int x = fx + along.getOffsetX() * k, z = fz + along.getOffsetZ() * k;
            int g = ground(world, x, base, z);
            if (g == Integer.MIN_VALUE) break;
            p.set(x, g + 1, z);
            if (soft(world.getBlockState(p))) setBlockState(world, p, k == 0 ? chiseled : bricks);
        }

        // Rubble scattered around.
        for (int i = 0; i < 7; i++) {
            float a = random.nextFloat() * MathHelper.TAU, dist = 4.5f + random.nextFloat() * 3f;
            int x = o.getX() + Math.round(MathHelper.cos(a) * dist), z = o.getZ() + Math.round(MathHelper.sin(a) * dist);
            int g = ground(world, x, base, z);
            if (g == Integer.MIN_VALUE) continue;
            BlockState rubble = random.nextBoolean() ? stone : bricks;
            if (random.nextBoolean()) {
                p.set(x, g, z);
                setBlockState(world, p, rubble);                    // set into the ground
            } else {
                p.set(x, g + 1, z);
                if (soft(world.getBlockState(p))) setBlockState(world, p, rubble);
            }
        }

        // Heart: wisp spawner on the chiseled plinth, two chests facing it.
        p.set(o.getX(), base + 1, o.getZ());
        setBlockState(world, p, Blocks.SPAWNER.getDefaultState());
        BlockEntity be = world.getBlockEntity(p);
        if (be instanceof MobSpawnerBlockEntity spawner) spawner.setEntityType(ModEntities.VOID_WISP, random);
        for (int k = 2; k <= 3; k++) {
            p.set(o.getX(), base + k, o.getZ());
            if (!world.isAir(p) && soft(world.getBlockState(p))) setBlockState(world, p, Blocks.AIR.getDefaultState());
        }
        placeChest(world, random, new BlockPos(o.getX() + 2, base + 1, o.getZ()), Direction.WEST);
        placeChest(world, random, new BlockPos(o.getX() - 2, base + 1, o.getZ()), Direction.EAST);

        // Glowbells growing in the cracks.
        for (int i = 0; i < 8; i++) {
            p.set(o.getX() + random.nextInt(11) - 5, base + 1, o.getZ() + random.nextInt(11) - 5);
            if (world.isAir(p) && ModBlocks.GLOWBELL.getDefaultState().canPlaceAt(world, p)) {
                setBlockState(world, p, ModBlocks.GLOWBELL.getDefaultState());
            }
        }
        return true;
    }

    private void placeChest(StructureWorldAccess world, Random random, BlockPos pos, Direction facing) {
        setBlockState(world, pos, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, facing));
        LootableInventory.setLootTable(world, random, pos, LOOT);
    }
}
