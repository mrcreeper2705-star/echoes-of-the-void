package dev.voidechoes.client;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;

/** Soft, self-lit motes that drift upward, sway, fade in and out and cycle their sprite. */
public class VoidMoteParticle extends SpriteBillboardParticle {
    private final SpriteProvider sprites;
    private final float baseScale;
    private final float phase;
    private final boolean glint;

    protected VoidMoteParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz,
                               SpriteProvider sprites, boolean glint) {
        super(world, x, y, z);
        this.sprites = sprites;
        this.glint = glint;
        this.velocityX = vx + (this.random.nextFloat() - 0.5f) * 0.01f;
        this.velocityY = vy + (glint ? 0.0 : 0.008 + this.random.nextFloat() * 0.012);
        this.velocityZ = vz + (this.random.nextFloat() - 0.5f) * 0.01f;
        this.velocityMultiplier = glint ? 0.85f : 0.96f;
        this.gravityStrength = glint ? 0.0f : -0.003f;
        this.collidesWithWorld = false;
        this.maxAge = glint ? 10 + this.random.nextInt(8) : 40 + this.random.nextInt(50);
        this.baseScale = glint ? 0.12f + this.random.nextFloat() * 0.06f : 0.05f + this.random.nextFloat() * 0.09f;
        this.scale = baseScale;
        this.phase = this.random.nextFloat() * MathHelper.TAU;
        this.alpha = 0.0f;
        if (glint) {
            this.setColor(0.75f + random.nextFloat() * 0.25f, 0.95f, 1.0f);
        } else {
            // Blend between violet and cyan.
            float t = this.random.nextFloat();
            this.setColor(MathHelper.lerp(t, 0.78f, 0.35f), MathHelper.lerp(t, 0.42f, 0.95f), 1.0f);
        }
        this.setSpriteForAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.dead) return;
        this.setSpriteForAge(this.sprites);
        float life = (float) this.age / this.maxAge;
        this.alpha = life < 0.2f ? life / 0.2f : 1.0f - (life - 0.2f) / 0.8f;
        if (!glint) {
            this.velocityX += MathHelper.sin(this.age * 0.12f + phase) * 0.0012;
            this.velocityZ += MathHelper.cos(this.age * 0.10f + phase) * 0.0012;
            this.scale = baseScale * (0.85f + 0.15f * MathHelper.sin(this.age * 0.3f + phase));
        } else {
            this.scale = baseScale * (1.0f - life * 0.5f);
            this.angle = this.prevAngle + 0.15f;
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getBrightness(float tint) {
        return 0xF000F0;
    }

    public record MoteFactory(SpriteProvider sprites) implements ParticleFactory<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new VoidMoteParticle(world, x, y, z, vx, vy, vz, sprites, false);
        }
    }

    public record GlintFactory(SpriteProvider sprites) implements ParticleFactory<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new VoidMoteParticle(world, x, y, z, vx, vy, vz, sprites, true);
        }
    }
}
