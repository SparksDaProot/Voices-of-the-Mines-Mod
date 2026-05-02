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
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
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
    private java.util.UUID ownerId = null;
    public void setOwnerId(java.util.UUID uuid) { this.ownerId = uuid; }
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
                // === НОВОЕ: Спавн Entity при приземлении ===
                if (waitTimer == 0) {
                    for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                        net.minecraft.world.item.ItemStack stack = this.inventory.getItem(i);
                        if (!stack.isEmpty()) {
                            net.minecraft.world.item.Item item = stack.getItem();
                            boolean isEntityItem = false;
                            EntityType<?> typeToSpawn = null;

                            if (item == VoicesOfTheMines.FUEL_CAN_ITEM.get()) { typeToSpawn = VoicesOfTheMines.FUEL_CAN.get(); isEntityItem = true; }
                            else if (item == VoicesOfTheMines.DRIVE_ITEM.get()) { typeToSpawn = VoicesOfTheMines.DRIVE.get(); isEntityItem = true; }
                            else if (item == VoicesOfTheMines.WASH_SPONGE_ITEM.get()) { typeToSpawn = VoicesOfTheMines.WASH_SPONGE.get(); isEntityItem = true; }

                            if (isEntityItem && typeToSpawn != null) {
                                for(int j = 0; j < stack.getCount(); j++) {
                                    net.minecraft.world.entity.Entity spawned = typeToSpawn.create(this.level());
                                    if (spawned != null) {
                                        // Разбрасываем предметы немного в стороны, чтобы они не застряли друг в друге
                                        double offsetX = (this.random.nextDouble() - 0.5) * 1.5;
                                        double offsetZ = (this.random.nextDouble() - 0.5) * 1.5;
                                        spawned.moveTo(this.getX() + offsetX, this.getY() - 0.5, this.getZ() + offsetZ, this.random.nextFloat() * 360F, 0);
                                        this.level().addFreshEntity(spawned);
                                    }
                                }
                                // Удаляем предмет из инвентаря дрона, так как мы его заспавнили
                                this.inventory.setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
                            }
                        }
                    }
                }

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

                    int earnedPoints = 0;
                    boolean soldAnything = false;
                    StringBuilder receipt = new StringBuilder("The package has been successfully delivered.\n\nSelling list:\n");

                    net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) this.level();
                    net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(serverLevel);

                    net.minecraft.world.item.ItemStack dailyBox = null;
                    net.minecraft.world.item.ItemStack dailyPaper = null;

                    for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                        net.minecraft.world.item.ItemStack stack = this.inventory.getItem(i);
                        if (!stack.isEmpty()) {
                            if (dailyBox == null && stack.getItem() == VoicesOfTheMines.DRIVE_BOX_ITEM.get()) dailyBox = stack;
                            if (dailyPaper == null && stack.getItem() == VoicesOfTheMines.PAPER_SHEET.get()) dailyPaper = stack;
                        }
                    }

                    // daily tasks check
                    String baoMessage = "";
                    boolean taskAttempted = false;

                    if (!manager.isTaskCompletedToday && dailyBox != null) {
                        taskAttempted = true;
                        int taskResult = manager.checkDailyTask(dailyBox, dailyPaper);

                        // drives
                        int boxBasePrice = getSellPrice(dailyBox);
                        earnedPoints += boxBasePrice;
                        soldAnything = true;

                        if (taskResult == 1) {
                            // perfect
                            int signalBonus = manager.currentTask.requiredSignalAmount * 15;
                            int hashBonus = manager.currentTask.requiredHashes.size() * 20;
                            earnedPoints += signalBonus + hashBonus;

                            baoMessage = "Package received - Good job Dr.Steve!\n\nPayment breakdown:\n";
                            baoMessage += "+ " + boxBasePrice + " base value for drives\n";
                            baoMessage += "+ " + signalBonus + " task bonus for signals\n";
                            baoMessage += "+ " + hashBonus + " task bonus for hashes\n";

                        } else if (taskResult == 2) {
                            // more than needed
                            int hashBonus = manager.currentTask.requiredHashes.size() * 20;
                            earnedPoints += hashBonus;

                            baoMessage = "Package received - We got more than we needed - good job, but no bonus for you, because now I have more work to do...\n\nPayment breakdown:\n";
                            baoMessage += "+ " + boxBasePrice + " base value for drives\n";
                            baoMessage += "+ 0 task bonus for signals (Too many disks!)\n";
                            baoMessage += "+ " + hashBonus + " task bonus for hashes\n";

                        } else {
                            // failed
                            baoMessage = "Package received - You have failed the report Dr.Steve. Try harder next time.\n\nPayment breakdown:\n";
                            baoMessage += "+ " + boxBasePrice + " base value for drives\n";
                        }

                        // remove box
                        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                            if (this.inventory.getItem(i) == dailyBox) {
                                this.inventory.setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
                                break;
                            }
                        }
                    }

                    // def selling
                    for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                        net.minecraft.world.item.ItemStack stack = this.inventory.getItem(i);
                        if (!stack.isEmpty() && stack.getItem() != VoicesOfTheMines.PAPER_SHEET.get()) {
                            int price = getSellPrice(stack);
                            if (price > 0) {
                                int itemTotal = price * stack.getCount();
                                earnedPoints += itemTotal;
                                soldAnything = true;
                                receipt.append("- ").append(stack.getCount()).append(" | ").append(stack.getHoverName().getString()).append(" |: for ").append(itemTotal).append(" points\n");
                            }
                        }
                    }

                    // tasks
                    if (taskAttempted) {
                        manager.getGlobalPlayerData().addPoints(this.ownerId, earnedPoints);

                        baoMessage += "\nPoints " + earnedPoints + " in total has been sent to your account balance.";
                        manager.getGlobalPlayerData().addEmail(this.ownerId, "Dr. Bao", "Daily Task Information", baoMessage);
                        manager.setDirty();

                        net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.send(
                                net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                                new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.EmailNotificationPacket()
                        );
                    }
                    else if (soldAnything) {
                        manager.getGlobalPlayerData().addPoints(this.ownerId, earnedPoints);
                        receipt.append("\nPoints ").append(earnedPoints).append(" in total has been sent to your account balance.");
                        manager.getGlobalPlayerData().addEmail(this.ownerId, "Auto", "Package received", receipt.toString());
                        manager.setDirty();

                        net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.send(
                                net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                                new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.EmailNotificationPacket()
                        );
                    }

                    this.discard();
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
        if (this.ownerId != null) tag.putUUID("OwnerId", this.ownerId);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Inventory")) this.inventory.fromTag(tag.getList("Inventory", 10));
        if (tag.contains("WaitTimer")) this.waitTimer = tag.getInt("WaitTimer");
        if (tag.hasUUID("OwnerId")) this.ownerId = tag.getUUID("OwnerId");
    }

    // sell prices
    private int getSellPrice(net.minecraft.world.item.ItemStack stack) {
        net.minecraft.world.item.Item item = stack.getItem();

        // Drive sells
        if (item == VoicesOfTheMines.DRIVE_BOX_ITEM.get()) {
            int totalBoxPrice = 0;
            if (stack.hasTag() && stack.getTag().contains("Inventory")) {
                net.minecraft.nbt.CompoundTag inventoryTag = stack.getTag().getCompound("Inventory");
                net.minecraftforge.items.ItemStackHandler handler = new net.minecraftforge.items.ItemStackHandler(6);
                handler.deserializeNBT(inventoryTag);

                for (int i = 0; i < handler.getSlots(); i++) {
                    net.minecraft.world.item.ItemStack diskStack = handler.getStackInSlot(i);
                    if (!diskStack.isEmpty() && diskStack.hasTag()) {
                        int sigLevel = diskStack.getTag().getInt("SignalLevel");
                        if (sigLevel == 0) totalBoxPrice += 5;
                        else if (sigLevel == 1) totalBoxPrice += 10;
                        else if (sigLevel == 2) totalBoxPrice += 15;
                        else if (sigLevel >= 3) totalBoxPrice += 30;
                    }
                }
            }
            return totalBoxPrice; // summ
        }
        if (item == VoicesOfTheMines.PLUSHIE_LIBE_ITEM.get()) return 30;
        if (item == VoicesOfTheMines.PLUSHIE_SPARKSY_ITEM.get()) return 30;
        if (item == VoicesOfTheMines.PLUSHIE_KEL_ITEM.get()) return 30;
        if (item == VoicesOfTheMines.PLUSHIE_NIKO_ITEM.get()) return 30;
        if (item == VoicesOfTheMines.PLUSHIE_INVINCIBLE_ITEM.get()) return 30;
        if (item == VoicesOfTheMines.PLUSHIE_BENJIKUS_COMMON_ITEM.get()) return 30;
        if (item == VoicesOfTheMines.PLUSHIE_BENJIKUS_ITEM.get()) return 30;
        if (item == VoicesOfTheMines.PLUSHIE_PECORA_ITEM.get()) return 30;
        if (item == VoicesOfTheMines.HAZARD_BOOTS.get()) return 30;
        if (item == VoicesOfTheMines.HAZARD_HELMET.get()) return 30;
        if (item == VoicesOfTheMines.HAZARD_CHESTPLATE.get()) return 30;
        if (item == VoicesOfTheMines.HAZARD_LEGGINGS.get()) return 30;
        if (item == VoicesOfTheMines.HOOK_ITEM.get()) return 25;
        if (item == VoicesOfTheMines.TRASH_ROLL.get()) return 5;
        if (item == VoicesOfTheMines.TRASH_BAG.get()) return 2;
        if (item == VoicesOfTheMines.ACCESSORY_GLASSES.get()) return 1;
        if (item == VoicesOfTheMines.ACCESSORY_JACKET.get()) return 1;
        if (item == VoicesOfTheMines.KEYPAD_ITEM.get()) return 10;
        if (item == VoicesOfTheMines.POSTER_ITEM.get()) return 5;
        if (item == VoicesOfTheMines.MARACAS.get()) return 5;
        if (item == VoicesOfTheMines.TACO.get()) return 2;
        if (item == VoicesOfTheMines.TOBLERONE.get()) return 2;
        if (item == VoicesOfTheMines.CHEESE.get()) return 2;
        if (item == VoicesOfTheMines.BURGER.get()) return 2;
        if (item == VoicesOfTheMines.DISK_BLUE.get()) return 6;
        if (item == VoicesOfTheMines.DRIVE_BOX_ITEM.get()) return 1;
        if (item == VoicesOfTheMines.PAPER_SHEET.get()) return 1;
        if (item == VoicesOfTheMines.CANDLE_HANDLE_ITEM.get()) return 5;
        if (item == VoicesOfTheMines.RECYCLED_PLASTIC.get()) return 1;
        if (item == VoicesOfTheMines.RECYCLED_RUBBER.get()) return 1;
        if (item == VoicesOfTheMines.METAL_SCRAP.get()) return 1;
        if (item == VoicesOfTheMines.ELECTRONIC_WASTE.get()) return 1;
        if (item == VoicesOfTheMines.FUEL_CAN_ITEM.get()) return 13;
        if (item == VoicesOfTheMines.DRIVE_ITEM.get()) return 1;
        if (item == VoicesOfTheMines.WASH_SPONGE_ITEM.get()) return 2;

        if (item == VoicesOfTheMines.PAINTER_BLACK.get() ||
                item == VoicesOfTheMines.PAINTER_BLUE.get() ||
                item == VoicesOfTheMines.PAINTER_GREEN.get() ||
                item == VoicesOfTheMines.PAINTER_PINK.get() ||
                item == VoicesOfTheMines.PAINTER_RED.get() ||
                item == VoicesOfTheMines.PAINTER_WHITE.get() ||
                item == VoicesOfTheMines.PAINTER_YELLOW.get()) return 20;

        return 1; // garbage 1 points :C
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