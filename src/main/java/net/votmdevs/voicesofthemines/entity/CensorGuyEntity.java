package net.votmdevs.voicesofthemines.entity;

import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.votmdevs.voicesofthemines.VotmSounds;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import net.votmdevs.voicesofthemines.world.SignalManager;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CensorGuyEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int noLookTimer = 0;
    private int aliveTimer = 0;
    private boolean isAttacking = false;
    private Player targetPlayer;

    public CensorGuyEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 100.0D)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.0D)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public boolean isInvulnerable() { return true; }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        aliveTimer++;

        if (targetPlayer == null) {
            targetPlayer = this.level().getNearestPlayer(this, 100);
            if (targetPlayer == null) {
                this.discard();
                return;
            }
        }

        this.lookControl.setLookAt(targetPlayer);

        if (isAttacking) {
            KerfurPacketHandler.INSTANCE.sendTo(new KerfurPacketHandler.CensorShakePacket(true), ((ServerPlayer)targetPlayer).connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);

            Vec3 move = targetPlayer.getEyePosition().subtract(this.position()).normalize().scale(1.2);
            this.setPos(this.getX() + move.x, this.getY() + move.y, this.getZ() + move.z);

            if (this.distanceTo(targetPlayer) < 2.0f) {
                KerfurPacketHandler.INSTANCE.sendTo(new KerfurPacketHandler.CensorJumpscarePacket(), ((ServerPlayer)targetPlayer).connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
                KerfurPacketHandler.INSTANCE.sendTo(new KerfurPacketHandler.CensorShakePacket(false), ((ServerPlayer)targetPlayer).connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
                this.discard();
            }
        } else {
            if (aliveTimer < 10) return;

            Vec3 viewVector = targetPlayer.getViewVector(1.0F).normalize();
            Vec3 entityCenter = this.position().add(0, this.getBbHeight() / 2.0, 0);
            Vec3 toEntity = entityCenter.subtract(targetPlayer.getEyePosition()).normalize();

            double dot = viewVector.dot(toEntity);

            boolean isLookingAt = dot > 0.95;
            boolean hasLineOfSight = targetPlayer.hasLineOfSight(this);

            if (isLookingAt && hasLineOfSight) {
                SignalManager sm = SignalManager.get((ServerLevel)this.level());
                sm.isCensorEventActive = true;
                sm.censorEventTimer = 12000;
                sm.setDirty();

                for(ServerPlayer p : ((ServerLevel)this.level()).players()) {
                    p.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, 1.0f));
                    p.playNotifySound(VotmSounds.BASETURNOFF.get(), SoundSource.MASTER, 1.0f, 1.0f);
                    p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 12000, 0, false, false, false));
                }

                KerfurPacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), new KerfurPacketHandler.SyncCensorStatePacket(true));
                this.discard();
            } else {
                noLookTimer++;
                if (noLookTimer > 200) {
                    isAttacking = true;
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}