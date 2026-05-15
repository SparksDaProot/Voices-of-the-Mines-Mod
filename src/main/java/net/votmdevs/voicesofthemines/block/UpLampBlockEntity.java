package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class UpLampBlockEntity extends BlockEntity implements GeoBlockEntity, IPowerableDevice {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean isPowered = false;

    public UpLampBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.UP_LAMP_BE.get(), pos, state);
    }

    @Override
    public boolean isPowered() { return isPowered; }

    @Override
    public void setPowered(boolean powered) {
        this.isPowered = powered;
        if (!powered && getBlockState().getValue(UpLampBlock.LIT)) {
            if (level != null) level.setBlock(worldPosition, getBlockState().setValue(UpLampBlock.LIT, false), 3);
        }
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> {
            boolean isLit = getBlockState().getValue(UpLampBlock.LIT);

            // event check
            boolean censorActive = false;
            if (this.level instanceof net.minecraft.server.level.ServerLevel sl) {
                censorActive = net.votmdevs.voicesofthemines.world.SignalManager.get(sl).isCensorEventActive;
            } else if (this.level != null && this.level.isClientSide()) {
                censorActive = net.votmdevs.voicesofthemines.client.ClientInputHandler.IS_CENSOR_EVENT_ACTIVE;
            }

            if (isLit && isPowered && !censorActive) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("light"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }
        }));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.isPowered = tag.getBoolean("IsPowered");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsPowered", this.isPowered);
    }

    @Override
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}