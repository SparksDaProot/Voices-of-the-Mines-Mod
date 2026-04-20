package net.votmdevs.voicesofthemines.block;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class VotvTerminalBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // memory
    private boolean hasDrive = false;
    private String driveSignalId = "";
    private String driveSignalType = "";
    private int driveSignalLevel = 0;

    public VotvTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.TERMINAL_BE.get(), pos, state);
    }

    public boolean hasDrive() { return hasDrive; }
    public String getDriveSignalId() { return driveSignalId; }
    public String getDriveSignalType() { return driveSignalType; }
    public int getDriveSignalLevel() { return driveSignalLevel; }

    // level
    public void setDrive(boolean hasDrive, String signalId, String signalType, int signalLevel) {
        this.hasDrive = hasDrive;
        this.driveSignalId = signalId;
        this.driveSignalType = signalType;
        this.driveSignalLevel = signalLevel;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void setDrive(boolean hasDrive, String signalId, String signalType) {
        setDrive(hasDrive, signalId, signalType, 0);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.hasDrive = tag.getBoolean("HasDrive");
        this.driveSignalId = tag.getString("DriveSignalId");
        this.driveSignalType = tag.getString("DriveSignalType");
        this.driveSignalLevel = tag.getInt("DriveSignalLevel");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("HasDrive", this.hasDrive);
        tag.putString("DriveSignalId", this.driveSignalId);
        tag.putString("DriveSignalType", this.driveSignalType);
        tag.putInt("DriveSignalLevel", this.driveSignalLevel);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}