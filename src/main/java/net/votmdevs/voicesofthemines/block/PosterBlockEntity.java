package net.votmdevs.voicesofthemines.block;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PosterBlockEntity extends BlockEntity {
    private String customImageUrl = "";

    public PosterBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.POSTER_BLOCK_ENTITY.get(), pos, state);
    }

    public String getCustomImageUrl() {
        return customImageUrl;
    }

    public void setCustomImageUrl(String customImageUrl) {
        this.customImageUrl = customImageUrl;
        setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("CustomImageUrl", this.customImageUrl);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.customImageUrl = tag.getString("CustomImageUrl");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            String oldUrl = this.customImageUrl;
            this.load(tag);
            // Если ссылка поменялась — заставляем Майнкрафт перерисовать текстуру
            if (!this.customImageUrl.equals(oldUrl) && this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }
}