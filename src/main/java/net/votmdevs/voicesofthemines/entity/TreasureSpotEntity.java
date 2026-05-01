package net.votmdevs.voicesofthemines.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class TreasureSpotEntity extends Mob {
    public static final EntityDataAccessor<String> LOOT_ID = SynchedEntityData.defineId(TreasureSpotEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Integer> LOOT_COUNT = SynchedEntityData.defineId(TreasureSpotEntity.class, EntityDataSerializers.INT);

    public TreasureSpotEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LOOT_ID, "minecraft:dirt");
        this.entityData.define(LOOT_COUNT, 1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("LootId", this.entityData.get(LOOT_ID));
        tag.putInt("LootCount", this.entityData.get(LOOT_COUNT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("LootId")) this.entityData.set(LOOT_ID, tag.getString("LootId"));
        if (tag.contains("LootCount")) this.entityData.set(LOOT_COUNT, tag.getInt("LootCount"));
    }

    public void digUp() {
        if (!this.level().isClientSide) {
            String itemId = this.entityData.get(LOOT_ID);
            int count = this.entityData.get(LOOT_COUNT);

            net.minecraft.resources.ResourceLocation resourceLocation = new net.minecraft.resources.ResourceLocation(itemId.split(":")[0], itemId.split(":")[1]);
            net.minecraft.world.item.Item lootItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(resourceLocation);

            if (lootItem != null && lootItem != net.minecraft.world.item.Items.AIR) {
                ItemStack stack = new ItemStack(lootItem, count);
                ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY() + 0.5, this.getZ(), stack);
                this.level().addFreshEntity(itemEntity);
            }

            ((ServerLevel) this.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()), this.getX(), this.getY() + 0.5, this.getZ(), 30, 0.4, 0.4, 0.4, 0.15);
            this.level().playSound(null, this.blockPosition(), SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.0f, 0.7f);

            this.discard();
        }
    }

    @Override public boolean isPickable() { return false; }
    @Override public boolean isPushable() { return false; }
}