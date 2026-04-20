package net.votmdevs.voicesofthemines.block;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.KerfurSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class VotvDoorBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean isOpen = false;
    private int closeTimer = 0;

    public VotvDoorBlockEntity(BlockPos pos, BlockState state) {
        super(KerfurMod.VOTV_DOOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VotvDoorBlockEntity be) {
        if (level.isClientSide()) return;

        if (be.isOpen && be.closeTimer > 0) {
            be.closeTimer--;
            if (be.closeTimer == 0) {
                be.closeDoor();
            }
        }
    }

    public void openDoor() {
        if (!this.isOpen) {
            this.isOpen = true;
            this.closeTimer = 60; // 3 секунды до закрытия

            this.level.playSound(null, this.worldPosition, KerfurSounds.VOTV_DOOR_SOUND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            this.triggerAnim("door_controller", "open");

            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(VotvDoorBlock.OPEN, true), 3);

            this.setChanged();
        }
    }

    public void closeDoor() {
        if (this.isOpen) {
            this.isOpen = false;
            this.closeTimer = 0;

            this.level.playSound(null, this.worldPosition, KerfurSounds.VOTV_DOOR_SOUND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            this.triggerAnim("door_controller", "close");

            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(VotvDoorBlock.OPEN, false), 3);

            this.setChanged();
        }
    }

    public void toggleDoor() {
        if (this.isOpen) closeDoor();
        else openDoor();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        AnimationController<VotvDoorBlockEntity> controller = new AnimationController<>(this, "door_controller", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle_door"));
        });

        controller.triggerableAnim("open", RawAnimation.begin().thenPlay("open_door").thenLoop("idle_door"));
        controller.triggerableAnim("close", RawAnimation.begin().thenPlay("close_door").thenLoop("idle_door"));

        registrar.add(controller);
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsOpen", this.isOpen);
        tag.putInt("CloseTimer", this.closeTimer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.isOpen = tag.getBoolean("IsOpen");
        this.closeTimer = tag.getInt("CloseTimer");
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag() { return this.saveWithoutMetadata(); }
}