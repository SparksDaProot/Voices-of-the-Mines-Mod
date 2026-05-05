package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SafeBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ItemStack storedItem = ItemStack.EMPTY;
    public String passcode = "";

    public int doorState = 0;
    public int animationTimer = 0;

    public SafeBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.SAFE_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SafeBlockEntity entity) {
        if (entity.doorState > 0) {
            entity.animationTimer++;

            if (entity.doorState == 1 && entity.animationTimer >= 30) {
                entity.doorState = 2;
                entity.animationTimer = 0;

                if (!level.isClientSide && !entity.storedItem.isEmpty()) {
                    net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                            level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, entity.storedItem.copy());
                    level.addFreshEntity(itemEntity);
                    entity.storedItem = ItemStack.EMPTY; // Очищаем сейф
                }
                entity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            else if (entity.doorState == 2 && entity.animationTimer >= 20) {
                entity.doorState = 3;
                entity.animationTimer = 0;
                entity.triggerAnim("door", "close");
                entity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            else if (entity.doorState == 3 && entity.animationTimer >= 30) {
                entity.doorState = 0;
                entity.animationTimer = 0;
                entity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }

    public void openSafe() {
        if (this.doorState == 0) {
            this.doorState = 1;
            this.animationTimer = 0;
            this.triggerAnim("door", "open");
            this.setChanged();
            if (this.level != null) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "door", 0, event -> {
            if (doorState == 0) return event.setAndContinue(RawAnimation.begin().thenLoop("close_idle"));
            if (doorState == 2) return event.setAndContinue(RawAnimation.begin().thenLoop("open_idle"));
            return PlayState.CONTINUE; // Для open и close используем триггеры
        }).triggerableAnim("open", RawAnimation.begin().thenPlay("open"))
                .triggerableAnim("close", RawAnimation.begin().thenPlay("close")));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.passcode = tag.getString("Passcode");
        this.doorState = tag.getInt("DoorState");
        this.animationTimer = tag.getInt("AnimTimer");
        if (tag.contains("StoredItem")) {
            this.storedItem = ItemStack.of(tag.getCompound("StoredItem"));
        } else {
            this.storedItem = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Passcode", this.passcode);
        tag.putInt("DoorState", this.doorState);
        tag.putInt("AnimTimer", this.animationTimer);
        if (!this.storedItem.isEmpty()) {
            tag.put("StoredItem", this.storedItem.save(new CompoundTag()));
        }
    }

    @Override
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}