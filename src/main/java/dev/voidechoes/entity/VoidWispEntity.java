package dev.voidechoes.entity;

import dev.voidechoes.registry.ModParticles;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A drifting shard-spirit of the outer End. Hovers, circles its prey and
 * strikes with weakening touches.
 */
public class VoidWispEntity extends HostileEntity {
    public VoidWispEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.experiencePoints = 8;
        this.setNoGravity(true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 18.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.55)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation nav = new BirdNavigation(this, world);
        nav.setCanPathThroughDoors(false);
        nav.setCanSwim(true);
        nav.setCanEnterOpenDoors(true);
        return nav;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.3, true));
        this.goalSelector.add(5, new FlyGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getWorld().isClient && this.random.nextInt(3) == 0) {
            this.getWorld().addParticle(ModParticles.VOID_MOTE,
                    this.getParticleX(0.6), this.getRandomBodyY() - 0.25, this.getParticleZ(0.6),
                    0, -0.02, 0);
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 0), this);
            this.playSound(SoundEvents.BLOCK_AMETHYST_CLUSTER_HIT, 1.0f, 0.7f);
        }
        return hit;
    }

    /** Always weightless, even when loaded from NBT without a NoGravity tag (spawners, /summon). */
    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK;
    }

    @Override
    public float getSoundPitch() {
        return 0.6f + this.random.nextFloat() * 0.4f;
    }
}
