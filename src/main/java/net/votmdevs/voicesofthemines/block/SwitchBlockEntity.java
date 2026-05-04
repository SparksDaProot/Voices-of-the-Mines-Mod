package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

public class SwitchBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public final List<BlockPos> linkedLamps = new ArrayList<>();

    public SwitchBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.SWITCH_BE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> {
            boolean isPowered = getBlockState().getValue(SwitchBlock.POWERED);
            if (isPowered) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("on"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlay("off"));
            }
        }));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (BlockPos lampPos : linkedLamps) {
            list.add(net.minecraft.nbt.NbtUtils.writeBlockPos(lampPos));
        }
        tag.put("LinkedLamps", list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        linkedLamps.clear();
        if (tag.contains("LinkedLamps")) {
            ListTag list = tag.getList("LinkedLamps", 10);
            for (int i = 0; i < list.size(); i++) {
                linkedLamps.add(net.minecraft.nbt.NbtUtils.readBlockPos(list.getCompound(i)));
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}