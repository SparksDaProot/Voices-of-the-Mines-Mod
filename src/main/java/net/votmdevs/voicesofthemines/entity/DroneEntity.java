package net.votmdevs.voicesofthemines.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DroneEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 0=Approach, 1=Descend, 2=Wait, 3=Ascend, 4=Leave
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<BlockPos> TARGET = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BLOCK_POS);

    public int waitTimer = 0;
    public final SimpleContainer inventory = new SimpleContainer(27);

    public DroneEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, 0);
        this.entityData.define(TARGET, BlockPos.ZERO);
    }

    public void setTargetPosition(BlockPos pos) { this.entityData.set(TARGET, pos); }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;

        BlockPos targetPos = this.entityData.get(TARGET);
        if (targetPos.equals(BlockPos.ZERO)) return;

        int state = this.entityData.get(STATE);
        Vec3 currentPos = this.position();
        Vec3 targetVec;

        this.setNoGravity(true);

        switch (state) {
            case 0: // APPROACH
                targetVec = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 30, targetPos.getZ() + 0.5);
                flyTowards(targetVec, 1.0);
                if (currentPos.distanceToSqr(targetVec) < 4.0) this.entityData.set(STATE, 1);
                break;

            case 1: // DESCEND
                targetVec = new Vec3(targetPos.getX() + 0.5, currentPos.y - 10, targetPos.getZ() + 0.5);
                flyTowards(targetVec, 0.4);

                boolean obstacleFound = false;
                for (int i = 0; i <= 2; i++) {
                    if (!this.level().getBlockState(this.blockPosition().below(i)).canBeReplaced()) {
                        obstacleFound = true;
                        break;
                    }
                }

                if (obstacleFound || this.onGround() || currentPos.y <= targetPos.getY() + 1.5) {
                    this.entityData.set(STATE, 2);
                }
                break;

            case 2: // WAIT
                this.setDeltaMovement(0, 0, 0);
                waitTimer++;
                if (waitTimer > 1000) this.entityData.set(STATE, 3);
                break;

            case 3: // ASCEND
                targetVec = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 30, targetPos.getZ() + 0.5);
                flyTowards(targetVec, 0.5);
                if (currentPos.distanceToSqr(targetVec) < 4.0) this.entityData.set(STATE, 4);
                break;

            case 4: // LEAVE
                targetVec = new Vec3(targetPos.getX() - 300, targetPos.getY() + 30, targetPos.getZ() + 0.5);
                flyTowards(targetVec, 1.2);
                if (currentPos.distanceToSqr(new Vec3(targetPos.getX(), currentPos.y, targetPos.getZ())) > 15000.0) {

                    // === ЛОГИКА ПРОДАЖИ ===
                    int earnedPoints = 0;
                    for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                        net.minecraft.world.item.ItemStack stack = this.inventory.getItem(i);
                        if (!stack.isEmpty()) {
                            int price = getSellPrice(stack.getItem());
                            earnedPoints += price * stack.getCount();
                        }
                    }

                    if (earnedPoints > 0) {
                        net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get((net.minecraft.server.level.ServerLevel)this.level());
                        manager.getGlobalPlayerData().addPoints(earnedPoints);
                        manager.setDirty();
                    }

                    this.discard(); // Улетаем и пропадаем
                }
                break;
        }
    }

    private void flyTowards(Vec3 target, double speedMultiplier) {
        Vec3 dir = target.subtract(this.position()).normalize().scale(speedMultiplier);
        this.setDeltaMovement(dir);

        if (dir.lengthSqr() > 0.01) {
            float targetYaw = (float) (Math.atan2(dir.z, dir.x) * (180F / Math.PI)) - 90.0F;
            this.setYRot(targetYaw);
            this.yBodyRot = targetYaw;
            this.yHeadRot = targetYaw;
        }
    }

    @Override
    public net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if (!this.level().isClientSide() && hand == net.minecraft.world.InteractionHand.MAIN_HAND) {
            if (this.entityData.get(STATE) == 2) {
                net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player,
                        new net.minecraft.world.SimpleMenuProvider(
                                (id, playerInv, p) -> new net.votmdevs.voicesofthemines.inventory.DroneMenu(id, playerInv, this.inventory),
                                net.minecraft.network.chat.Component.literal("Drone Storage")
                        ), buf -> buf.writeInt(this.getId()));
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Inventory", this.inventory.createTag());
        tag.putInt("WaitTimer", this.waitTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Inventory")) {
            this.inventory.fromTag(tag.getList("Inventory", 10));
        }
        if (tag.contains("WaitTimer")) {
            this.waitTimer = tag.getInt("WaitTimer");
        }
    }

    // sell prices
    private int getSellPrice(net.minecraft.world.item.Item item) {
        if (item == net.votmdevs.voicesofthemines.KerfurMod.HAZARD_HELMET.get() ||
                item == net.votmdevs.voicesofthemines.KerfurMod.HAZARD_CHESTPLATE.get() ||
                item == net.votmdevs.voicesofthemines.KerfurMod.HAZARD_LEGGINGS.get() ||
                item == net.votmdevs.voicesofthemines.KerfurMod.HAZARD_BOOTS.get()) return 150;

        if (item == net.votmdevs.voicesofthemines.KerfurMod.HOOK_ITEM.get()) return 25;
        if (item == net.votmdevs.voicesofthemines.KerfurMod.TRASH_ROLL.get()) return 5;
        if (item == net.votmdevs.voicesofthemines.KerfurMod.ACCESSORY_GLASSES.get()) return 1;
        if (item == net.votmdevs.voicesofthemines.KerfurMod.ACCESSORY_JACKET.get()) return 1;
        if (item == net.votmdevs.voicesofthemines.KerfurMod.KEYPAD_ITEM.get()) return 10;
        if (item == net.votmdevs.voicesofthemines.KerfurMod.POSTER_ITEM.get()) return 5;
        if (item == net.votmdevs.voicesofthemines.KerfurMod.TACO.get()) return 1;
        if (item == net.votmdevs.voicesofthemines.KerfurMod.TOBLERONE.get()) return 1;
        if (item == net.votmdevs.voicesofthemines.KerfurMod.CHEESE.get()) return 1;
        if (item == net.votmdevs.voicesofthemines.KerfurMod.BURGER.get()) return 1;

        if (item == net.votmdevs.voicesofthemines.KerfurMod.PAINTER_BLACK.get() ||
                item == net.votmdevs.voicesofthemines.KerfurMod.PAINTER_BLUE.get() ||
                item == net.votmdevs.voicesofthemines.KerfurMod.PAINTER_GREEN.get() ||
                item == net.votmdevs.voicesofthemines.KerfurMod.PAINTER_PINK.get() ||
                item == net.votmdevs.voicesofthemines.KerfurMod.PAINTER_RED.get() ||
                item == net.votmdevs.voicesofthemines.KerfurMod.PAINTER_WHITE.get() ||
                item == net.votmdevs.voicesofthemines.KerfurMod.PAINTER_YELLOW.get()) return 20;

        return 0; // garbage 0 points :C
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}