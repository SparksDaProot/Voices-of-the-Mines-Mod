package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import net.votmdevs.voicesofthemines.world.PlayerData;
import net.votmdevs.voicesofthemines.world.SignalManager;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class VendingBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // STORAGE: [BANANA, CHEESE, TACO, TOBLERONE, BURGER]
    private final int[] stock = {5, 5, 5, 5, 5};

    private int stuckHitsRequired = 0;
    private int pendingItemIndex = -1;

    public VendingBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.VENDING_BE.get(), pos, state);
    }

    public void tryBuy(ServerPlayer player) {
        if (stuckHitsRequired > 0) return;

        List<Integer> availableItems = new ArrayList<>();
        for (int i = 0; i < stock.length; i++) {
            if (stock[i] > 0) availableItems.add(i);
        }

        if (availableItems.isEmpty()) {
            this.level.playSound(null, this.worldPosition, VotmSounds.BUG_ALERT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.5F);
            return;
        }

        SignalManager manager = SignalManager.get(player.serverLevel());
        PlayerData pd = manager.getGlobalPlayerData();

        if (pd.spendPoints(player.getUUID(), 2)) {
            manager.setDirty();
            KerfurPacketHandler.INSTANCE.sendTo(new KerfurPacketHandler.SyncComputerDataPacket(pd.getPoints(player.getUUID()), pd.getCursorSpeedLvl(), pd.getPingCooldownLvl(), pd.getProcessingSpeedLvl(), pd.getProcessingLevelLvl(), pd.getEmails(player.getUUID()), pd.customMarket), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);

            int chosenItemIndex = availableItems.get(this.level.random.nextInt(availableItems.size()));
            stock[chosenItemIndex]--;
            this.setChanged();

            if (this.level.random.nextFloat() < 0.10f) {
                stuckHitsRequired = 3;
                pendingItemIndex = chosenItemIndex;
                this.level.playSound(null, this.worldPosition, net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_CLOSE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.6F);
            } else {
                dispenseItem(chosenItemIndex);
                sendAnimPacket("give");
            }
        } else {
            this.level.playSound(null, this.worldPosition, VotmSounds.BUG_ALERT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (isStockEmpty()) {
            summonRestocker();
        }
    }

    private void summonRestocker() {
        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.core.Direction dir = this.getBlockState().getValue(net.votmdevs.voicesofthemines.block.VendingBlock.FACING);

            BlockPos spawnPos = this.worldPosition.relative(dir);

            net.votmdevs.voicesofthemines.entity.RestockerEntity restocker = VoicesOfTheMines.RESTOCKER.get().create(serverLevel);
            if (restocker != null) {
                float yRot = dir.getOpposite().toYRot();

                restocker.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, yRot, 0);

                restocker.setYBodyRot(yRot);
                restocker.setYHeadRot(yRot);
                restocker.yRotO = yRot;
                restocker.yBodyRotO = yRot;
                restocker.yHeadRotO = yRot;

                restocker.setTargetVending(this.worldPosition);

                serverLevel.addFreshEntity(restocker);
            }
        }
    }

    public void tryPunch(ServerPlayer player) {
        if (stuckHitsRequired > 0) {
            stuckHitsRequired--;
            if (stuckHitsRequired == 0) {
                dispenseItem(pendingItemIndex);
                sendAnimPacket("give");
                pendingItemIndex = -1;
            } else {
                sendAnimPacket("stuck");
                this.level.playSound(null, this.worldPosition, net.minecraft.sounds.SoundEvents.METAL_HIT, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        if (isStockEmpty()) {
            summonRestocker();
        }
    }

    private void dispenseItem(int itemIndex) {
        Item itemToDrop = switch (itemIndex) {
            case 0 -> VoicesOfTheMines.BANANA.get();
            case 1 -> VoicesOfTheMines.CHEESE.get();
            case 2 -> VoicesOfTheMines.TACO.get();
            case 3 -> VoicesOfTheMines.TOBLERONE.get();
            default -> VoicesOfTheMines.BURGER.get();
        };

        Direction dir = this.getBlockState().getValue(VendingBlock.FACING);
        double dX = this.worldPosition.getX() + 0.5D + dir.getStepX() * 0.6D;
        double dY = this.worldPosition.getY() + 0.2D;
        double dZ = this.worldPosition.getZ() + 0.5D + dir.getStepZ() * 0.6D;

        ItemEntity itemEntity = new ItemEntity(this.level, dX, dY, dZ, new ItemStack(itemToDrop));
        itemEntity.setDeltaMovement(dir.getStepX() * 0.2D, 0.1D, dir.getStepZ() * 0.2D);
        this.level.addFreshEntity(itemEntity);

        this.level.playSound(null, this.worldPosition, net.minecraft.sounds.SoundEvents.DISPENSER_DISPENSE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.2F);

        if (this.level instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, dX, dY + 0.2, dZ, 5, 0.1, 0.1, 0.1, 0.05);
        }
    }

    private void sendAnimPacket(String animName) {
        KerfurPacketHandler.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunkAt(this.worldPosition)),
                new KerfurPacketHandler.VendingAnimPacket(this.worldPosition, animName)
        );
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> event.setAndContinue(RawAnimation.begin().thenLoop("idle")))
                .triggerableAnim("give", RawAnimation.begin().thenPlay("give").thenLoop("idle"))
                .triggerableAnim("stuck", RawAnimation.begin().thenPlay("stuck").thenLoop("idle"))
        );
    }

    public boolean isStockEmpty() {
        for (int s : stock) {
            if (s > 0) return false;
        }
        return true;
    }
    //restock
    public void refillStock() {
        for (int i = 0; i < stock.length; i++) {
            stock[i] = 5;
        }
        this.setChanged();
        this.level.playSound(null, this.worldPosition, net.minecraft.sounds.SoundEvents.CHEST_LOCKED, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putIntArray("Stock", stock);
        tag.putInt("StuckHits", stuckHitsRequired);
        tag.putInt("PendingItem", pendingItemIndex);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Stock")) {
            int[] savedStock = tag.getIntArray("Stock");
            if (savedStock.length == 5) System.arraycopy(savedStock, 0, stock, 0, 5);
        }
        stuckHitsRequired = tag.getInt("StuckHits");
        pendingItemIndex = tag.getInt("PendingItem");
    }
}