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
import software.bernie.geckolib.util.GeckoLibUtil;

public class RadioBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public boolean isPlaying = false;
    public float volume = 0.5f;
    public String currentTrack = "";

    public RadioBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.RADIO_BE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsPlaying", isPlaying);
        tag.putFloat("Volume", volume);
        tag.putString("Track", currentTrack);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.isPlaying = tag.getBoolean("IsPlaying");
        this.volume = tag.getFloat("Volume");
        this.currentTrack = tag.getString("Track");
    }

    @Override
    public CompoundTag getUpdateTag() { return this.saveWithoutMetadata(); }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {}

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}