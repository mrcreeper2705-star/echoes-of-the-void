package dev.voidechoes.item;

import dev.voidechoes.registry.ModParticles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** Shared instant-teleport logic for the Voidstep Charm and the Voidcrystal Blade. */
public final class VoidBlink {
    private VoidBlink() {}

    /**
     * Finds a safe spot along the player's line of sight.
     * @param needsBlock when true the player must be aiming at a block; otherwise the blink may end mid-air.
     */
    @Nullable
    public static Vec3d findDestination(World world, PlayerEntity user, double range, boolean needsBlock) {
        Vec3d eye = user.getEyePos();
        Vec3d look = user.getRotationVec(1.0f);
        HitResult hit = world.raycast(new RaycastContext(eye, eye.add(look.multiply(range)),
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
        if (needsBlock && hit.getType() != HitResult.Type.BLOCK) return null;
        if (hit instanceof BlockHitResult bhr && hit.getType() == HitResult.Type.BLOCK) {
            Vec3d onTop = Vec3d.ofBottomCenter(bhr.getBlockPos().up());
            if (fits(world, user, onTop)) return onTop;
        }
        double dist = hit.getType() == HitResult.Type.MISS ? range : hit.getPos().distanceTo(eye) - 0.6;
        double eyeH = user.getStandingEyeHeight();
        for (double d = dist; d > 1.5; d -= 0.5) {
            Vec3d feet = eye.add(look.multiply(d)).subtract(0, eyeH, 0);
            if (fits(world, user, feet)) return feet;
        }
        return null;
    }

    private static boolean fits(World world, PlayerEntity user, Vec3d feet) {
        return world.isSpaceEmpty(user, user.getDimensions(user.getPose()).getBoxAt(feet));
    }

    /** Moves the player with particles and sounds at both ends and a trail between them. */
    public static void blink(ServerWorld world, PlayerEntity user, Vec3d target) {
        Vec3d from = user.getPos();
        burst(world, from, user.getHeight());
        user.requestTeleport(target.x, target.y, target.z);
        user.onLanding();
        burst(world, target, user.getHeight());
        Vec3d step = target.subtract(from);
        int n = Math.max(6, (int) (step.length() * 1.5));
        for (int i = 0; i <= n; i++) {
            Vec3d p = from.add(step.multiply(i / (double) n));
            world.spawnParticles(ModParticles.VOID_MOTE, p.x, p.y + 1.0, p.z, 2, 0.15, 0.3, 0.15, 0.0);
        }
        world.playSound(null, from.x, from.y, from.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.8f, 1.3f);
        world.playSound(null, target.x, target.y, target.z, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS, 1.0f, 1.4f);
    }

    public static void fail(ServerWorld world, PlayerEntity user) {
        world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.PLAYERS, 0.5f, 1.6f);
    }

    private static void burst(ServerWorld world, Vec3d pos, float height) {
        world.spawnParticles(ParticleTypes.REVERSE_PORTAL, pos.x, pos.y + height / 2, pos.z, 30, 0.3, height / 3, 0.3, 0.05);
        world.spawnParticles(ModParticles.CRYSTAL_GLINT, pos.x, pos.y + height / 2, pos.z, 10, 0.4, height / 3, 0.4, 0.02);
    }
}
