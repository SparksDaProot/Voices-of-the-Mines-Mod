package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

import java.util.ArrayList;
import java.util.List;

public class SwitchBlockEntity extends BlockEntity implements GeoBlockEntity, IPowerableDevice {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public final List<BlockPos> linkedLamps = new ArrayList<>();

    // power
    private boolean isPowered = false;

    public SwitchBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.SWITCH_BE.get(), pos, state);
    }

    @Override
    public boolean isPowered() { return isPowered; }

    @Override
    public void setPowered(boolean powered) {
        this.isPowered = powered;
        if (!powered && getBlockState().getValue(SwitchBlock.POWERED)) {
            if (level != null) level.setBlock(worldPosition, getBlockState().setValue(SwitchBlock.POWERED, false), 3);
        }
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> {
            boolean isSwitchPowered = getBlockState().getValue(SwitchBlock.POWERED);
            if (isSwitchPowered) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("on"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlay("off"));
            }
        }));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsPowered", this.isPowered);
        ListTag list = new ListTag();
        for (BlockPos lampPos : linkedLamps) {
            list.add(net.minecraft.nbt.NbtUtils.writeBlockPos(lampPos));
        }
        tag.put("LinkedLamps", list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.isPowered = tag.getBoolean("IsPowered");
        linkedLamps.clear();
        if (tag.contains("LinkedLamps")) {
            ListTag list = tag.getList("LinkedLamps", 10);
            for (int i = 0; i < list.size(); i++) {
                linkedLamps.add(net.minecraft.nbt.NbtUtils.readBlockPos(list.getCompound(i)));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}