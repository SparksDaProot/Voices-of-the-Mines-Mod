package net.votmdevs.voicesofthemines.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.client.sound.AlarmSoundInstance;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class AlarmBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public int detectMode = 0; // 0 = Redstone, 1 = Entity (Monsters), 2 = Player
    public int iconTimer = 0;  // timer
    public boolean isActive = false; // on/off

    private AlarmSoundInstance currentSound;

    public AlarmBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.ALARM_BE.get(), pos, state);
    }

    public void cycleMode() {
        this.detectMode = (this.detectMode + 1) % 3;
        this.iconTimer = 80;
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AlarmBlockEntity be) {
        if (level.isClientSide) {
            if (be.iconTimer > 0) be.iconTimer--;

            if (be.isActive) {
                if (be.currentSound == null || be.currentSound.isStopped()) {
                    be.currentSound = new AlarmSoundInstance(pos);
                    Minecraft.getInstance().getSoundManager().play(be.currentSound);
                }
            } else {
                if (be.currentSound != null && !be.currentSound.isStopped()) {
                    be.currentSound.fadeOutAndStop();
                    be.currentSound = null;
                }
            }
            return;
        }

        if (be.iconTimer > 0) {
            be.iconTimer--;
            if (be.iconTimer == 0) level.sendBlockUpdated(pos, state, state, 3);
        }

        boolean hasRedstone = level.hasNeighborSignal(pos);
        boolean shouldBeActive = false;

        if (hasRedstone) {
            if (be.detectMode == 0) {
                // Redstone detect
                shouldBeActive = true;
            } else if (be.detectMode == 1) {
                // Entity (Monster) detect
                AABB searchBox = new AABB(pos).inflate(20.0D);
                List<Monster> monsters = level.getEntitiesOfClass(Monster.class, searchBox);
                shouldBeActive = !monsters.isEmpty();
            } else if (be.detectMode == 2) {
                // Player detect
                AABB searchBox = new AABB(pos).inflate(20.0D);
                List<Player> players = level.getEntitiesOfClass(Player.class, searchBox, p -> !p.isCreative() && !p.isSpectator());
                shouldBeActive = !players.isEmpty();
            }
        }

        if (be.isActive != shouldBeActive) {
            be.isActive = shouldBeActive;
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("DetectMode", detectMode);
        tag.putInt("IconTimer", iconTimer);
        tag.putBoolean("IsActive", isActive);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.detectMode = tag.getInt("DetectMode");
        this.iconTimer = tag.getInt("IconTimer");
        this.isActive = tag.getBoolean("IsActive");
    }

    @Override
    public CompoundTag getUpdateTag() { return this.saveWithoutMetadata(); }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> {
            if (this.isActive) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("on"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("off"));
            }
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}