package net.votmdevs.voicesofthemines.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class BlackSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected BlackSmokeParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = spriteSet;
        this.friction = 0.96F; // Трение воздуха
        this.speedUpWhenYMotionIsBlocked = true;

        this.xd = this.xd * 0.01F + xSpeed;
        this.yd = this.yd * 0.01F + ySpeed + 0.05F;
        this.zd = this.zd * 0.01F + zSpeed;

        this.quadSize *= 2.0F;

        this.lifetime = (int)(20.0D / (Math.random() * 0.8D + 0.2D)) + 20;

        this.rCol = 0.15f;
        this.gCol = 0.15f;
        this.bCol = 0.15f;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);

        this.alpha = 1.0F - ((float)this.age / (float)this.lifetime);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new BlackSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}