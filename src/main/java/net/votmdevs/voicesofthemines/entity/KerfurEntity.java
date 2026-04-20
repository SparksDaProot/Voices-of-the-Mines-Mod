package net.votmdevs.voicesofthemines.entity;

import net.votmdevs.voicesofthemines.VotmSounds;
import net.votmdevs.voicesofthemines.inventory.KerfurMenu;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Arrays;
import java.util.List;

public class KerfurEntity extends TamableAnimal implements GeoEntity, MenuProvider {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> DEACTIVATED = SynchedEntityData.defineId(KerfurEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PETTING = SynchedEntityData.defineId(KerfurEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WAKING_UP = SynchedEntityData.defineId(KerfurEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> KERFUR_COLOR = SynchedEntityData.defineId(KerfurEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> FLASHLIGHT_ON = SynchedEntityData.defineId(KerfurEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WAITING = SynchedEntityData.defineId(KerfurEntity.class, EntityDataSerializers.BOOLEAN);

    private static final List<Item> LIGHT_SOURCES = Arrays.asList(
            Items.TORCH, Items.SOUL_TORCH, Items.LANTERN,
            Items.SOUL_LANTERN, Items.GLOWSTONE, Items.SEA_LANTERN
    );

    private int torchCooldown = 0;
    private int pettingTicks = 0;
    private int wakeUpTicks = 0;
    private int pickupTimer = 0;
    private int meowTicks = 0;
    private int playerNearTicks = 0;

    private boolean wasPetting = false;
    private boolean wasHurt = false;
    private boolean wasPettingForMovement = false;

    private boolean notifiedFull = false;
    private boolean notifiedRecharge = false;

    private long lastPetTime = 0;
    private int petCombo = 0;

    public final SimpleContainer inventory = new SimpleContainer(27);

    public KerfurEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DEACTIVATED, false);
        this.entityData.define(PETTING, false);
        this.entityData.define(WAKING_UP, false);
        this.entityData.define(KERFUR_COLOR, "blue");
        this.entityData.define(FLASHLIGHT_ON, false);
        this.entityData.define(WAITING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.2D, 5.0F, 2.0F, false) {
            @Override public boolean canUse() { return super.canUse() && !isWaitingMode(); }
        });
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override public boolean canUse() { return super.canUse() && !isWaitingMode(); }
            @Override public boolean canContinueToUse() { return super.canContinueToUse() && !isWaitingMode(); }
        });
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this) {
            @Override public boolean canUse() { return super.canUse() && !isWaitingMode(); }
        });
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(VotmSounds.WALK.get(), 0.15F, 1.0F);
    }

    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        this.playSound(VotmSounds.JUMP.get(), 1.0F, 1.0F);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return (!isDeactivated() && !isWakingUp()) ? VotmSounds.IDLE.get() : null;
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
            float healthPercentage = this.getHealth() / this.getMaxHealth();

            if (healthPercentage < 0.70F && !isDeactivated() && !isWakingUp()) {
                this.setDeactivated(true);
                this.navigation.stop();
                this.goalSelector.removeAllGoals(goal -> true);
                this.playSound(VotmSounds.SHUTDOWN.get(), 1.0F, 1.0F);

                if (!notifiedRecharge && this.getOwner() instanceof ServerPlayer sp) {
                    sendNotification(sp, "Kerfur is recharging");
                    notifiedRecharge = true;
                }
            } else if (isDeactivated()) {
                if (this.tickCount % 20 == 0) {
                    this.heal(1.0F);
                }
                if (this.getHealth() >= this.getMaxHealth()) {
                    this.setDeactivated(false);
                    this.setWakingUp(true);
                    notifiedRecharge = false;
                }
            } else if (isWakingUp()) {
                wakeUpTicks++;
                if (wakeUpTicks > 10) {
                    this.setWakingUp(false);
                    wakeUpTicks = 0;
                    this.registerGoals();
                }
            }

            if (isPetting()) {
                pettingTicks++;
                this.navigation.stop();
                if (pettingTicks > 10) {
                    this.setPetting(false);
                    pettingTicks = 0;
                }
            }

            boolean inventoryFull = !hasEmptySlot();

            if (!isDeactivated() && !isWakingUp()) {
                if (inventoryFull && !isWaitingMode() && this.getAttribute(Attributes.MOVEMENT_SPEED).getValue() <= 0.4D) {
                    setWaitingMode(true);
                    this.navigation.stop();

                    if (!notifiedFull && this.getOwner() instanceof ServerPlayer sp) {
                        sendNotification(sp, "Kerfur is full!");
                        notifiedFull = true;
                    }
                } else if (!inventoryFull && isWaitingMode() && this.getOwner() != null && this.distanceTo(this.getOwner()) <= 20.0D) {
                    setWaitingMode(false);
                    notifiedFull = false;
                }
            }

            if (!isDeactivated()) {
                int lightLevel = this.level().getMaxLocalRawBrightness(this.blockPosition());
                boolean shouldBeOn = lightLevel <= 0;

                if (shouldBeOn != isFlashlightOn()) {
                    setFlashlightOn(shouldBeOn);
                    this.playSound(VotmSounds.FLASHLIGHT.get(), 1.0F, 1.0F);
                }

                if (torchCooldown > 0) torchCooldown--;

                if (lightLevel < 3 && torchCooldown == 0 && !isWaitingMode()) {
                    int slotToUse = -1;
                    for (int i = 0; i < inventory.getContainerSize(); i++) {
                        if (LIGHT_SOURCES.contains(inventory.getItem(i).getItem())) {
                            slotToUse = i;
                            break;
                        }
                    }

                    if (slotToUse != -1) {
                        BlockPos posToPlace = this.blockPosition();
                        if (this.level().getBlockState(posToPlace).isAir() &&
                                Block.canSupportRigidBlock(this.level(), posToPlace.below())) {

                            ItemStack lightItem = inventory.getItem(slotToUse);
                            Block blockToPlace = Block.byItem(lightItem.getItem());

                            this.level().setBlock(posToPlace, blockToPlace.defaultBlockState(), 3);
                            this.playSound(SoundEvents.WOOD_PLACE, 1.0F, 1.0F);

                            lightItem.shrink(1);
                            inventory.setItem(slotToUse, lightItem);

                            torchCooldown = 60;
                        }
                    }
                }
            }

            if (!isDeactivated() && !isWakingUp() && !isWaitingMode() && !inventoryFull) {
                List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(6.0D));
                if (!items.isEmpty()) {
                    ItemEntity target = items.get(0);
                    this.getNavigation().moveTo(target, 1.2D);

                    if (this.distanceTo(target) < 2.5D) {
                        pickupTimer++;
                        if (pickupTimer > 30) {
                            ItemStack stack = target.getItem().copy();
                            ItemStack leftover = this.inventory.addItem(stack);

                            if (leftover.isEmpty()) {
                                target.discard();
                            } else {
                                target.setItem(leftover);
                            }

                            this.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                            pickupTimer = 0;
                        }
                    } else {
                        if (pickupTimer > 0) pickupTimer--;
                    }
                } else {
                    pickupTimer = 0;
                }
            }

            if (isWaitingMode() && this.getOwner() != null) {
                double dist = this.distanceTo(this.getOwner());
                if (dist > 20.0D) {
                    setWaitingMode(false);
                    this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D * 1.5D);
                    this.getNavigation().moveTo(this.getOwner(), 1.5D);
                }
            }

            if (this.getOwner() != null && this.distanceTo(this.getOwner()) < 3.0D && this.getAttribute(Attributes.MOVEMENT_SPEED).getValue() > 0.4D) {
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D);
                this.jumpFromGround();
                meowTicks = 40;

                if (inventoryFull) {
                    setWaitingMode(true);
                    this.navigation.stop();
                }
            }

            if (meowTicks > 0) {
                if (meowTicks % 15 == 0) this.playSound(VotmSounds.MEOW.get(), 1.0F, 1.0F);
                meowTicks--;
            }

            //  Abandonedd
            if (this.getKerfurColor().equals("abandoned")) {
                // 1. Случайные криповые звуки
                if (this.random.nextInt(400) == 0) {
                    this.playSound(VotmSounds.MURDER_RANDOM.get(), 1.0F, 1.0F);
                }

                boolean isPlayerNear = false;
                List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(4.0D));

                if (!nearbyPlayers.isEmpty()) {
                    isPlayerNear = true;
                    playerNearTicks++;
                } else {
                    playerNearTicks = 0;
                }

                if (isPlayerNear && playerNearTicks > 100) {
                    if (this.tickCount % 20 == 0) {
                        for (Player target : nearbyPlayers) {
                            if (this.random.nextFloat() < 0.60F) {
                                this.level().playSound(null, target.blockPosition(), VotmSounds.PLAYER_COUGH.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.2F);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {

            if (isDeactivated()) {
                if (!this.level().isClientSide()) {
                    this.playSound(VotmSounds.WARNING.get(), 1.0F, 1.0F);

                    if (player instanceof ServerPlayer sp) {
                        sendNotification(sp, "Wait! Kerfur is recharging!");
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            if (player.isShiftKeyDown() && !isWakingUp()) {
                if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    NetworkHooks.openScreen(serverPlayer, this, buffer -> {
                        buffer.writeUtf(this.getKerfurColor());
                        buffer.writeBoolean(false);
                    });
                    this.playSound(VotmSounds.OPEN_STORAGE.get(), 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            if (!this.isTame() && !this.level().isClientSide()) {
                this.tame(player);
            }

            long currentTime = this.level().getGameTime();
            boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 0.005D;

            if (!isMoving && !isWakingUp()) {

                if (this.level().isClientSide()) {
                    this.wasPetting = false;
                } else {
                    this.setPetting(true);
                    this.pettingTicks = 0;

                    if (currentTime - lastPetTime < 15) {
                        petCombo++;
                    } else {
                        petCombo = 1;
                    }
                    lastPetTime = currentTime;

                    if (petCombo >= 15) {
                        this.level().explode(this, this.getX(), this.getY(), this.getZ(), 4.0F, true, Level.ExplosionInteraction.MOB);

                        if (player instanceof ServerPlayer sp) {
                            sendNotification(sp, "What have you done!");
                        }
                        this.discard();
                    } else {
                        float pitch = 1.0F + (petCombo * 0.1F);
                        if (this.getKerfurColor().equals("abandoned")) {
                            this.playSound(VotmSounds.MURDER_MEOW.get(), 1.0F, pitch);
                        } else {
                            this.playSound(VotmSounds.MEOW.get(), 1.0F, pitch);
                        }
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            lastPetTime = currentTime;
        }
        return super.mobInteract(player, hand);
    }

    private void sendNotification(ServerPlayer player, String message) {
        KerfurPacketHandler.INSTANCE.sendTo(
                new KerfurPacketHandler.NotificationPacket(message),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        registrar.add(new AnimationController<>(this, "state", 5, this::statePredicate));
        registrar.add(new AnimationController<>(this, "action", 0, this::actionPredicate));
    }

    private PlayState movementPredicate(AnimationState<KerfurEntity> event) {
        if (isDeactivated() || isWakingUp()) return PlayState.STOP;

        if (!isPetting() && this.wasPettingForMovement) {
            event.getController().forceAnimationReset();
        }
        this.wasPettingForMovement = isPetting();

        if (event.isMoving()) {
            event.getController().setAnimation(RawAnimation.begin().thenLoop("move"));
        } else {
            event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        }
        return PlayState.CONTINUE;
    }

    private PlayState statePredicate(AnimationState<KerfurEntity> event) {
        if (isDeactivated()) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("off").thenLoop("off_idle"));
            return PlayState.CONTINUE;
        } else if (isWakingUp()) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("on"));
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private PlayState actionPredicate(AnimationState<KerfurEntity> event) {
        boolean currentlyPetting = isPetting();
        boolean currentlyHurt = this.hurtTime > 0 && !isDeactivated() && !isWakingUp();

        if (currentlyPetting && !wasPetting) {
            event.getController().forceAnimationReset();
        }
        this.wasPetting = currentlyPetting;

        if (currentlyHurt && !wasHurt) {
            event.getController().forceAnimationReset();
        }
        this.wasHurt = currentlyHurt;

        if (currentlyPetting) {
            event.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("pet"));
            return PlayState.CONTINUE;
        }

        if (currentlyHurt) {
            event.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("hurt"));
            return PlayState.CONTINUE;
        }

        return PlayState.STOP;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new KerfurMenu(containerId, playerInventory, this.inventory, this.getKerfurColor(), false);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("KerfurColor", this.getKerfurColor());
        compound.putBoolean("Waiting", this.isWaitingMode());
        compound.put("Inventory", this.inventory.createTag());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("KerfurColor")) {
            this.setKerfurColor(compound.getString("KerfurColor"));
        }
        if (compound.contains("Waiting")) {
            this.setWaitingMode(compound.getBoolean("Waiting"));
        }
        if (compound.contains("Inventory")) {
            this.inventory.fromTag(compound.getList("Inventory", 10));
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) { return null; }

    public String getKerfurColor() { return this.entityData.get(KERFUR_COLOR); }
    public void setKerfurColor(String color) { this.entityData.set(KERFUR_COLOR, color); }

    public boolean isDeactivated() { return this.entityData.get(DEACTIVATED); }
    public void setDeactivated(boolean value) { this.entityData.set(DEACTIVATED, value); }

    public boolean isPetting() { return this.entityData.get(PETTING); }
    public void setPetting(boolean value) { this.entityData.set(PETTING, value); }

    public boolean isWakingUp() { return this.entityData.get(WAKING_UP); }
    public void setWakingUp(boolean value) { this.entityData.set(WAKING_UP, value); }

    public boolean isFlashlightOn() { return this.entityData.get(FLASHLIGHT_ON); }
    public void setFlashlightOn(boolean val) { this.entityData.set(FLASHLIGHT_ON, val); }

    public boolean isWaitingMode() { return this.entityData.get(WAITING); }
    public void setWaitingMode(boolean val) { this.entityData.set(WAITING, val); }
}