package net.votmdevs.voicesofthemines.entity;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import net.votmdevs.voicesofthemines.inventory.KerfurMenu;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class OmegaKerfurEntity extends TamableAnimal implements GeoEntity, MenuProvider {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    // cosmetic
    private static final EntityDataAccessor<String> KERFUR_ACCESSORY = SynchedEntityData.defineId(OmegaKerfurEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DEACTIVATED = SynchedEntityData.defineId(OmegaKerfurEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PETTING = SynchedEntityData.defineId(OmegaKerfurEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WAKING_UP = SynchedEntityData.defineId(OmegaKerfurEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FLASHLIGHT_ON = SynchedEntityData.defineId(OmegaKerfurEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WAITING = SynchedEntityData.defineId(OmegaKerfurEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DANCING = SynchedEntityData.defineId(OmegaKerfurEntity.class, EntityDataSerializers.BOOLEAN);
    // colors
    private static final EntityDataAccessor<String> KERFUR_COLOR = SynchedEntityData.defineId(OmegaKerfurEntity.class, EntityDataSerializers.STRING);

    public final SimpleContainer inventory = new SimpleContainer(54);
    private int pettingTicks = 0, wakeUpTicks = 0, pickupTimer = 0, petCombo = 0;
    private int attackTicks = 0;
    private long lastPetTime = 0;

    private boolean notifiedFull = false;
    private boolean notifiedRecharge = false;

    public OmegaKerfurEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.setMaxUpStep(1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DEACTIVATED, false);
        this.entityData.define(PETTING, false);
        this.entityData.define(WAKING_UP, false);
        this.entityData.define(FLASHLIGHT_ON, false);
        this.entityData.define(WAITING, false);
        this.entityData.define(DANCING, false);
        this.entityData.define(KERFUR_COLOR, "blue");
        this.entityData.define(KERFUR_ACCESSORY, "none");// Синий по умолчанию
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0D, 5.0F, 2.0F, false) {
            @Override public boolean canUse() { return super.canUse() && !isDeactivated() && !isWaitingMode() && !isDancing() && !isPetting() && attackTicks == 0; }
        });
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override public boolean canUse() { return super.canUse() && !isDeactivated() && !isWaitingMode() && !isDancing() && !isPetting() && attackTicks == 0; }
        });
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        if (blockIn.is(BlockTags.LOGS) || blockIn.is(BlockTags.PLANKS) || blockIn.is(BlockTags.WOODEN_DOORS) || blockIn.is(BlockTags.WOODEN_STAIRS) || blockIn.is(BlockTags.WOODEN_SLABS)) {
            this.playSound(VotmSounds.OMEGA_STEP_WOOD.get(), 0.3F, 1.0F);
        } else if (blockIn.is(BlockTags.DIRT) || blockIn.is(BlockTags.SAND) || blockIn.is(Blocks.GRAVEL) || blockIn.is(Blocks.CLAY) || blockIn.is(Blocks.GRASS_BLOCK)) {
            this.playSound(VotmSounds.OMEGA_STEP_DIRT.get(), 0.3F, 1.0F);
        } else {
            this.playSound(VotmSounds.OMEGA_STEP_DEFAULT.get(), 0.3F, 1.0F);
        }
    }

    private void sendNotification(ServerPlayer player, String message) {
        KerfurPacketHandler.INSTANCE.sendTo(
                new KerfurPacketHandler.NotificationPacket(message),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }

    private boolean hasEmptySlot() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (this.inventory.getItem(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            checkNearbyJukebox();

            if (attackTicks > 0) {
                attackTicks--;
                this.getNavigation().stop();
            }

            if (isPetting()) {
                pettingTicks++;
                this.getNavigation().stop();
                if (pettingTicks > 10) {
                    this.setPetting(false);
                    pettingTicks = 0;
                }
            }

            if (this.isTame() && this.getOwner() != null && !isDeactivated() && !isDancing() && !isPetting() && attackTicks == 0) {
                double dist = this.distanceTo(this.getOwner());
                double currentSpeed = this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();

                if (dist > 7.0D && currentSpeed <= 0.35D) {
                    this.playSound(VotmSounds.OMEGA_SPRINT.get(), 1.0F, 1.0F);
                    this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.45D);
                } else if (dist < 4.0D && currentSpeed > 0.35D) {
                    this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D);
                }
            }

            float healthPercentage = this.getHealth() / this.getMaxHealth();
            boolean inventoryFull = !hasEmptySlot();

            if (healthPercentage < 0.70F && !isDeactivated() && !isWakingUp()) {
                this.setDeactivated(true);
                this.getNavigation().stop();
                this.playSound(VotmSounds.SHUTDOWN.get(), 1.0F, 1.0F);

                if (!notifiedRecharge && this.getOwner() instanceof ServerPlayer sp) {
                    sendNotification(sp, "Wait! Kerfur-O is recharging!");
                    notifiedRecharge = true;
                }
            } else if (isDeactivated()) {
                if (this.tickCount % 20 == 0) this.heal(1.0F);
                if (this.getHealth() >= this.getMaxHealth()) {
                    this.setDeactivated(false);
                    this.setWakingUp(true);
                    this.wakeUpTicks = 0;
                    notifiedRecharge = false;
                }
            } else if (isWakingUp()) {
                wakeUpTicks++;
                if (wakeUpTicks > 40) {
                    this.setWakingUp(false);
                    wakeUpTicks = 0;
                }
            }

            if (!isDeactivated() && !isWakingUp()) {
                if (inventoryFull && !isWaitingMode()) {
                    setWaitingMode(true);
                    this.getNavigation().stop();

                    if (!notifiedFull && this.getOwner() instanceof ServerPlayer sp) {
                        sendNotification(sp, "Kerfur-O is full ( ͡° ͜ʖ ͡°)");
                        notifiedFull = true;
                    }
                } else if (!inventoryFull && isWaitingMode() && this.getOwner() != null && this.distanceTo(this.getOwner()) <= 20.0D) {
                    setWaitingMode(false);
                    notifiedFull = false;
                }
            }

            if (!isDeactivated() && !isWaitingMode() && !isDancing() && !isWakingUp() && !inventoryFull && !isPetting() && attackTicks == 0) {
                handlePickup();
            }
        }
    }

    private void checkNearbyJukebox() {
        BlockPos pos = this.blockPosition();
        boolean musicFound = false;
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-5, -1, -5), pos.offset(5, 1, 5))) {
            BlockState state = this.level().getBlockState(nearby);
            if (state.is(Blocks.JUKEBOX) && state.hasProperty(JukeboxBlock.HAS_RECORD) && state.getValue(JukeboxBlock.HAS_RECORD)) {
                musicFound = true;
                break;
            }
        }
        this.entityData.set(DANCING, musicFound);
    }

    private void handlePickup() {
        List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(6.0D));
        if (!items.isEmpty()) {
            ItemEntity target = items.get(0);
            this.getNavigation().moveTo(target, 1.2D);
            if (this.distanceTo(target) < 2.5D && ++pickupTimer > 30) {
                if (this.inventory.addItem(target.getItem().copy()).isEmpty()) target.discard();
                this.playSound(SoundEvents.ITEM_PICKUP, 0.2F, 1.0F);
                pickupTimer = 0;
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack itemInHand = player.getItemInHand(hand);

        if (itemInHand.getItem() == VoicesOfTheMines.ACCESSORY_MAID.get() ||
                itemInHand.getItem() == VoicesOfTheMines.ACCESSORY_RIBBON.get() ||
                itemInHand.getItem() == VoicesOfTheMines.ACCESSORY_GLASSES.get() ||
                itemInHand.getItem() == VoicesOfTheMines.ACCESSORY_JACKET.get()) {

            if (!this.level().isClientSide()) {
                String currentAcc = this.getKerfurAccessory();
                if (!currentAcc.equals("none")) {
                    Item dropItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(VoicesOfTheMines.MODID, currentAcc));
                    if (dropItem != null) {
                        this.spawnAtLocation(dropItem);
                    }
                }

                String newAccName = ForgeRegistries.ITEMS.getKey(itemInHand.getItem()).getPath();
                this.setKerfurAccessory(newAccName); // Ставим аксессуар (maid, ribbon, glasses, jacket)

                if (!player.isCreative()) {
                    itemInHand.shrink(1);
                }

                this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }


        if (attackTicks > 0) return InteractionResult.sidedSuccess(this.level().isClientSide());

        if (isDeactivated() || isWakingUp()) {
            if (!this.level().isClientSide()) {
                this.playSound(VotmSounds.WARNING.get(), 1.0F, 1.0F);
                if (player instanceof ServerPlayer sp) {
                    sendNotification(sp, "Wait! Kerfur-O is recharging!");
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (player.isShiftKeyDown()) {
            if (!this.level().isClientSide() && player instanceof ServerPlayer sp) {
                NetworkHooks.openScreen(sp, this, buf -> {
                    buf.writeUtf(this.getKerfurColor());
                    buf.writeBoolean(true);
                });
                this.playSound(VotmSounds.OPEN_STORAGE.get(), 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (!this.isTame() && !this.level().isClientSide()) {
            this.tame(player);
        }

        if (!this.level().isClientSide()) {
            long time = this.level().getGameTime();

            setPetting(true);
            pettingTicks = 0;
            this.getNavigation().stop();

            if (time - lastPetTime < 15) petCombo++;
            else petCombo = 1;
            lastPetTime = time;

            if (petCombo >= 15) {
                this.triggerAnim("action", "attack");
                this.playSound(VotmSounds.OMEGA_BONK.get(), 1.0F, 1.0F);

                boolean hurt = player.hurt(this.damageSources().mobAttack(this), 6.0F);
                if (!hurt) {
                    player.hurt(this.damageSources().generic(), 6.0F);
                }

                player.knockback(0.6D, this.getX() - player.getX(), this.getZ() - player.getZ());

                petCombo = 0;
                attackTicks = 20;
                this.setPetting(false);
            } else {
                this.triggerAnim("action", "petmeow");
                this.playSound(VotmSounds.MEOW.get(), 1.0F, 1.0F + (petCombo * 0.1F));
            }
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 5, event -> {
            if (isDeactivated() || isWakingUp()) return PlayState.STOP;
            if (isDancing()) return event.setAndContinue(RawAnimation.begin().thenLoop("dance"));

            if (event.isMoving()) {
                boolean isRunning = this.getAttributeValue(Attributes.MOVEMENT_SPEED) > 0.40D;
                return event.setAndContinue(RawAnimation.begin().thenLoop(isRunning ? "run" : "walk"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));

        registrar.add(new AnimationController<>(this, "state", 5, event -> {
            if (isDeactivated()) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("off").thenLoop("off_idle"));
            } else if (isWakingUp()) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("on"));
            }
            return PlayState.STOP;
        }));

        registrar.add(new AnimationController<>(this, "action", 0, event -> PlayState.STOP)
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("attack"))
                .triggerableAnim("petmeow", RawAnimation.begin().thenPlay("petmeow")));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    public String getKerfurColor() { return this.entityData.get(KERFUR_COLOR); }
    public void setKerfurColor(String color) { this.entityData.set(KERFUR_COLOR, color); }

    public String getKerfurAccessory() { return this.entityData.get(KERFUR_ACCESSORY); }
    public void setKerfurAccessory(String accessory) { this.entityData.set(KERFUR_ACCESSORY, accessory); }

    public boolean isDeactivated() { return this.entityData.get(DEACTIVATED); }
    public void setDeactivated(boolean v) { this.entityData.set(DEACTIVATED, v); }
    public boolean isPetting() { return this.entityData.get(PETTING); }
    public void setPetting(boolean v) { this.entityData.set(PETTING, v); }
    public boolean isWakingUp() { return this.entityData.get(WAKING_UP); }
    public void setWakingUp(boolean v) { this.entityData.set(WAKING_UP, v); }
    public boolean isWaitingMode() { return this.entityData.get(WAITING); }
    public void setWaitingMode(boolean v) { this.entityData.set(WAITING, v); }
    public boolean isDancing() { return this.entityData.get(DANCING); }
    public void setDancing(boolean v) { this.entityData.set(DANCING, v); }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
        return new KerfurMenu(id, inv, this.inventory, this.getKerfurColor(), true);
    }

    @Override public Component getDisplayName() { return Component.literal(""); }
    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob m) { return null; }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("KerfurColor", this.getKerfurColor());
        compound.putString("KerfurAccessory", this.getKerfurAccessory());
        compound.putBoolean("Waiting", this.isWaitingMode());
        compound.put("Inventory", this.inventory.createTag());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("KerfurColor")) {
            this.setKerfurColor(compound.getString("KerfurColor")); // ЗАГРУЖАЕМ ЦВЕТ
        }
        if (compound.contains("KerfurAccessory")) {
            this.setKerfurAccessory(compound.getString("KerfurAccessory")); // ЗАГРУЖАЕМ
        }
        if (compound.contains("Waiting")) {
            this.setWaitingMode(compound.getBoolean("Waiting"));
        }
        if (compound.contains("Inventory")) {
            this.inventory.fromTag(compound.getList("Inventory", 10));
        }
    }
}