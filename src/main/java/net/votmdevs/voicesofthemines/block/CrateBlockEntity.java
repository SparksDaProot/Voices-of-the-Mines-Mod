package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrateBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final List<ItemStack> inventory = new ArrayList<>();
    private boolean isInitialized = false;

    public CrateBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.CRATE_BE.get(), pos, state);
    }

    // loot
    private void initLoot() {
        if (isInitialized) return;
        inventory.add(new ItemStack(Items.COAL, 4));
        inventory.add(new ItemStack(Items.OAK_PLANKS, 12));
        inventory.add(new ItemStack(Items.OAK_LOG, 3));
        inventory.add(new ItemStack(VoicesOfTheMines.CASSETTE.get(), 1));
        inventory.add(new ItemStack(VoicesOfTheMines.PLUSHIE_BENJIKUS_COMMON_ITEM.get(), 1));
        inventory.add(new ItemStack(VoicesOfTheMines.DISK_BLUE.get(), 1));
        inventory.add(new ItemStack(VoicesOfTheMines.MARACAS.get(), 1));
        inventory.add(new ItemStack(VoicesOfTheMines.BANANA.get(), 1));
        inventory.add(new ItemStack(VoicesOfTheMines.BURGER.get(), 1));
        inventory.add(new ItemStack(VoicesOfTheMines.TACO.get(), 1));
        isInitialized = true;
        setChanged();
    }

    public void addItem(ItemStack stack) {
        if (!isInitialized) initLoot();
        inventory.add(stack);
        setChanged();
    }

    public void dropRandomLoot(Level level, BlockPos pos) {
        if (!isInitialized) initLoot();
        if (inventory.isEmpty()) return;

        Collections.shuffle(inventory); // random

        // 2 loots
        int drops = Math.min(2, inventory.size());
        for (int i = 0; i < drops; i++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inventory.get(i));
        }
        inventory.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Initialized", isInitialized);
        ListTag list = new ListTag();
        for (ItemStack stack : inventory) {
            list.add(stack.save(new CompoundTag()));
        }
        tag.put("CrateLoot", list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        isInitialized = tag.getBoolean("Initialized");
        inventory.clear();
        if (tag.contains("CrateLoot")) {
            ListTag list = tag.getList("CrateLoot", 10);
            for (int i = 0; i < list.size(); i++) {
                inventory.add(ItemStack.of(list.getCompound(i)));
            }
        }
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}