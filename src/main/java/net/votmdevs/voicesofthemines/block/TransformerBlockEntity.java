package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import net.votmdevs.voicesofthemines.config.VotmConfig;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class TransformerBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public boolean isMain = false;
    public int energy = 0;
    public boolean isActive = false;
    public boolean needsReboot = false;
    public boolean isNetworkActive = false;
    public boolean isReady = false;

    public int currentTicksToDrain = 0;

    public List<BlockPos> secondaries = new ArrayList<>();
    public List<BlockPos> connectedDevices = new ArrayList<>();
    public BlockPos mainTransformerPos = null;

    private int tickCounter = 0;

    public TransformerBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.TRANSFORMER_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TransformerBlockEntity entity) {
        if (level.isClientSide) return;

        boolean isOn = entity.isMain ? entity.isActive : (entity.mainTransformerPos != null && entity.isNetworkActive && !entity.needsReboot);
        if (!isOn) return;

        int deviceCount = 0;
        if (entity.isMain) {
            deviceCount = entity.connectedDevices.size();
        } else if (entity.mainTransformerPos != null) {
            BlockEntity mBe = level.getBlockEntity(entity.mainTransformerPos);
            if (mBe instanceof TransformerBlockEntity mainTr) {
                deviceCount = mainTr.connectedDevices.size();
            }
        }

        entity.tickCounter++;

        if (entity.currentTicksToDrain <= 0) {
            double deviceFactor = VotmConfig.TRANSFORMER_DEVICE_FACTOR.get();
            double baseDrain = VotmConfig.TRANSFORMER_BASE_DRAIN.get()
                    / (1.0 + Math.log1p(deviceCount) * deviceFactor);
            float randomFactor = 0.7f + (level.random.nextFloat() * 0.6f);
            entity.currentTicksToDrain = Math.max(
                    VotmConfig.TRANSFORMER_MIN_TICKS.get(),
                    (int)(baseDrain * randomFactor)
            );
        }

        if (entity.tickCounter >= entity.currentTicksToDrain) {
            entity.tickCounter = 0;
            entity.currentTicksToDrain = 0;
            entity.energy--;

            if (entity.energy <= 0) {
                entity.energy = -1;

                level.playSound(null, pos, VotmSounds.TURNOFF.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);

                if (entity.isMain) {
                    entity.shutdownNetwork();
                } else if (entity.mainTransformerPos != null) {
                    BlockEntity mBe = level.getBlockEntity(entity.mainTransformerPos);
                    if (mBe instanceof TransformerBlockEntity mainTr) {
                        mainTr.shutdownNetwork();
                    }
                }
            }
            entity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public void checkNetworkStart() {
        if (isMain && secondaries.size() >= 2) {
            isActive = true;
            needsReboot = false;
            energy = VotmConfig.TRANSFORMER_BASE_ENERGY.get();
            isReady = false;
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            setSecondariesActive(true);
            updateDevicesPower(true);
        }
    }

    public void shutdownNetwork() {
        isActive = false;
        needsReboot = true;
        isReady = false;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.playSound(null, worldPosition, VotmSounds.BASETURNOFF.get(), net.minecraft.sounds.SoundSource.MASTER, 1.0F, 1.0F);
        }

        for (BlockPos secPos : secondaries) {
            BlockEntity secBe = level.getBlockEntity(secPos);
            if (secBe instanceof TransformerBlockEntity sec) {
                sec.needsReboot = true;
                sec.isNetworkActive = false;
                sec.isReady = false;
                sec.setChanged();
                level.sendBlockUpdated(secPos, sec.getBlockState(), sec.getBlockState(), 3);
            }
        }
        updateDevicesPower(false);
    }

    public void tryRestartNetwork() {
        if (!isMain) return;
        if (secondaries.size() < 2) return;

        if (!this.isReady) return;

        for (BlockPos secPos : secondaries) {
            BlockEntity secBe = level.getBlockEntity(secPos);
            if (secBe instanceof TransformerBlockEntity sec) {
                if (!sec.isReady) return;
            } else {
                return;
            }
        }

        isActive = true;
        needsReboot = false;
        isReady = false;
        energy = VotmConfig.TRANSFORMER_BASE_ENERGY.get();
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        for (BlockPos secPos : secondaries) {
            BlockEntity secBe = level.getBlockEntity(secPos);
            if (secBe instanceof TransformerBlockEntity sec) {
                sec.isNetworkActive = true;
                sec.needsReboot = false;
                sec.isReady = false;
                sec.energy = VotmConfig.TRANSFORMER_BASE_ENERGY.get();
                sec.setChanged();
                level.sendBlockUpdated(secPos, sec.getBlockState(), sec.getBlockState(), 3);
            }
        }
        updateDevicesPower(true);
    }

    private void setSecondariesActive(boolean active) {
        if (level == null) return;
        for (BlockPos secPos : secondaries) {
            BlockEntity secBe = level.getBlockEntity(secPos);
            if (secBe instanceof TransformerBlockEntity sec) {
                sec.isNetworkActive = active;
                sec.energy = VotmConfig.TRANSFORMER_BASE_ENERGY.get();
                if (active) {
                    sec.needsReboot = false;
                    sec.isReady = false;
                }
                sec.setChanged();
                level.sendBlockUpdated(secPos, sec.getBlockState(), sec.getBlockState(), 3);
            }
        }
    }

    private void updateDevicesPower(boolean powered) {
        if (level == null) return;
        for (BlockPos devicePos : connectedDevices) {
            BlockEntity be = level.getBlockEntity(devicePos);
            if (be instanceof IPowerableDevice device) {
                device.setPowered(powered);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> {
            boolean isOn = isMain ? isActive : (mainTransformerPos != null && !needsReboot && isNetworkActive);
            if (isOn) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("on"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("off"));
            }
        }));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        isMain = tag.getBoolean("IsMain");
        energy = tag.getInt("Energy");
        isActive = tag.getBoolean("IsActive");
        needsReboot = tag.getBoolean("NeedsReboot");
        isNetworkActive = tag.getBoolean("IsNetworkActive");
        isReady = tag.getBoolean("IsReady");

        if (tag.contains("MainPos")) {
            mainTransformerPos = NbtUtils.readBlockPos(tag.getCompound("MainPos"));
        } else {
            mainTransformerPos = null;
        }

        secondaries.clear();
        ListTag secList = tag.getList("Secondaries", 10);
        for (int i = 0; i < secList.size(); i++) {
            secondaries.add(NbtUtils.readBlockPos(secList.getCompound(i)));
        }

        connectedDevices.clear();
        ListTag devList = tag.getList("Devices", 10);
        for (int i = 0; i < devList.size(); i++) {
            connectedDevices.add(NbtUtils.readBlockPos(devList.getCompound(i)));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsMain", isMain);
        tag.putInt("Energy", energy);
        tag.putBoolean("IsActive", isActive);
        tag.putBoolean("NeedsReboot", needsReboot);
        tag.putBoolean("IsNetworkActive", isNetworkActive);
        tag.putBoolean("IsReady", isReady);

        if (mainTransformerPos != null) {
            tag.put("MainPos", NbtUtils.writeBlockPos(mainTransformerPos));
        }

        ListTag secList = new ListTag();
        for (BlockPos pos : secondaries) secList.add(NbtUtils.writeBlockPos(pos));
        tag.put("Secondaries", secList);

        ListTag devList = new ListTag();
        for (BlockPos pos : connectedDevices) devList.add(NbtUtils.writeBlockPos(pos));
        tag.put("Devices", devList);
    }

    @Override
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}