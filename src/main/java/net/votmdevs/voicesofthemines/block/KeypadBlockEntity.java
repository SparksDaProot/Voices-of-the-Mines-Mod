package net.votmdevs.voicesofthemines.block;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
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
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KeypadBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private String currentCode = "";
    private String savedCode = "";

    private int status = 0;
    private int timer = 0;

    public KeypadBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.KEYPAD_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, KeypadBlockEntity be) {
        if (level.isClientSide()) return;

        if (be.timer > 0) {
            be.timer--;

            if (be.status == 3) {
                if (be.timer == 54) { // Через 0.3 сек (60 - 6 тиков)
                    level.playSound(null, pos, VotmSounds.KEYPAD_ACCESS.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                } else if (be.timer == 38) { // Через 0.8 сек после access (54 - 16 тиков)
                    level.playSound(null, pos, VotmSounds.KEYPAD_DENIED.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }

            if (be.timer == 0) {
                be.status = 1;
                be.currentCode = "";
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }

    public void pressButton(int number) {
        if (this.status == 2 || this.status == 3) return;

        if (this.level.isClientSide()) {
            this.triggerAnim("buttons", "button_" + number);
            this.level.playLocalSound(this.worldPosition, VotmSounds.KEYPAD_PRESS.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
        } else {
            if (currentCode.length() >= 4) currentCode = "";
            currentCode += number;

            if (currentCode.length() == 4) {
                if (status == 0) {
                    savedCode = currentCode;
                    currentCode = "";
                    status = 1;
                } else if (status == 1) {
                    if (currentCode.equals(savedCode)) {
                        status = 2;
                        timer = 40;

                        for (BlockPos checkPos : BlockPos.betweenClosed(this.worldPosition.offset(-2, -2, -2), this.worldPosition.offset(2, 2, 2))) {
                            BlockEntity neighbor = this.level.getBlockEntity(checkPos);
                            if (neighbor instanceof VotvDoorBlockEntity door) {
                                door.openDoor();
                            }
                        }

                    } else {
                        status = 3;
                        timer = 60;
                        this.level.playSound(null, this.worldPosition, VotmSounds.KEYPAD_CANCELED.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }

            this.setChanged();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public String getCurrentCode() { return currentCode; }
    public int getStatus() { return status; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        AnimationController<KeypadBlockEntity> controller = new AnimationController<>(this, "buttons", 0, event -> PlayState.STOP);
        for (int i = 1; i <= 9; i++) {
            controller.triggerableAnim("button_" + i, RawAnimation.begin().thenPlay("button_" + i));
        }
        registrar.add(controller);
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("CurrentCode", this.currentCode);
        tag.putString("SavedCode", this.savedCode);
        tag.putInt("Status", this.status);
        tag.putInt("Timer", this.timer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.currentCode = tag.getString("CurrentCode");
        this.savedCode = tag.getString("SavedCode");
        this.status = tag.getInt("Status");
        this.timer = tag.getInt("Timer");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
}