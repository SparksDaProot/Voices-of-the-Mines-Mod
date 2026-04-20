package net.votmdevs.voicesofthemines.network;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.KerfurSounds;
import net.votmdevs.voicesofthemines.client.gui.GmodNotificationManager;
import net.votmdevs.voicesofthemines.entity.FleshEntity;
import net.votmdevs.voicesofthemines.entity.FuelCanEntity;
import net.votmdevs.voicesofthemines.entity.GarbageEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class KerfurPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(KerfurMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, EmailNotificationPacket.class, EmailNotificationPacket::encode, EmailNotificationPacket::decode, EmailNotificationPacket::handle);
        INSTANCE.registerMessage(id++, ReadEmailPacket.class, ReadEmailPacket::encode, ReadEmailPacket::decode, ReadEmailPacket::handle);
        INSTANCE.registerMessage(id++, DeleteEmailPacket.class, DeleteEmailPacket::encode, DeleteEmailPacket::decode, DeleteEmailPacket::handle);
        INSTANCE.registerMessage(id++, SyncCalibrateTargetPacket.class, SyncCalibrateTargetPacket::encode, SyncCalibrateTargetPacket::decode, SyncCalibrateTargetPacket::handle);
        INSTANCE.registerMessage(id++, SyncProcessingStatePacket.class, SyncProcessingStatePacket::encode, SyncProcessingStatePacket::decode, SyncProcessingStatePacket::handle);
        INSTANCE.registerMessage(id++, SyncSignalsPacket.class, SyncSignalsPacket::encode, SyncSignalsPacket::decode, SyncSignalsPacket::handle);
        INSTANCE.registerMessage(id++, CatchSignalPacket.class, CatchSignalPacket::encode, CatchSignalPacket::decode, CatchSignalPacket::handle);
        INSTANCE.registerMessage(id++, NotificationPacket.class, NotificationPacket::encode, NotificationPacket::decode, NotificationPacket::handle);
        INSTANCE.registerMessage(id++, GrabPacket.class, GrabPacket::encode, GrabPacket::decode, GrabPacket::handle);
        INSTANCE.registerMessage(id++, ExtractGarbagePacket.class, ExtractGarbagePacket::encode, ExtractGarbagePacket::decode, ExtractGarbagePacket::handle);
        INSTANCE.registerMessage(id++, UpdatePosterUrlPacket.class, UpdatePosterUrlPacket::encode, UpdatePosterUrlPacket::decode, UpdatePosterUrlPacket::handle);
        INSTANCE.registerMessage(id++, FinishCheckPacket.class, FinishCheckPacket::encode, FinishCheckPacket::decode, FinishCheckPacket::handle);
        INSTANCE.registerMessage(id++, FinishCalibrationPacket.class, FinishCalibrationPacket::encode, FinishCalibrationPacket::decode, FinishCalibrationPacket::handle);
        INSTANCE.registerMessage(id++, SyncComputerDataPacket.class, SyncComputerDataPacket::encode, SyncComputerDataPacket::decode, SyncComputerDataPacket::handle);
        INSTANCE.registerMessage(id++, BuyUpgradePacket.class, BuyUpgradePacket::encode, BuyUpgradePacket::decode, BuyUpgradePacket::handle);
        INSTANCE.registerMessage(id++, SyncCheckTargetPacket.class, SyncCheckTargetPacket::encode, SyncCheckTargetPacket::decode, SyncCheckTargetPacket::handle);
        INSTANCE.registerMessage(id++, AtvCrashPacket.class, AtvCrashPacket::encode, AtvCrashPacket::decode, AtvCrashPacket::handle);
        INSTANCE.registerMessage(id++, AtvBrakePacket.class, AtvBrakePacket::encode, AtvBrakePacket::decode, AtvBrakePacket::handle);
        INSTANCE.registerMessage(id++, PackGarbagePacket.class, PackGarbagePacket::encode, PackGarbagePacket::decode, PackGarbagePacket::handle);
        INSTANCE.registerMessage(id++, HookPullPacket.class, HookPullPacket::encode, HookPullPacket::decode, HookPullPacket::handle);
        INSTANCE.registerMessage(id++, InsertDrivePacket.class, InsertDrivePacket::encode, InsertDrivePacket::decode, InsertDrivePacket::handle);
        INSTANCE.registerMessage(id++, HookDetachPacket.class, HookDetachPacket::encode, HookDetachPacket::decode, HookDetachPacket::handle);
        INSTANCE.registerMessage(id++, KnockdownPacket.class, KnockdownPacket::encode, KnockdownPacket::decode, KnockdownPacket::handle);
        INSTANCE.registerMessage(id++, BuyStorePacket.class, BuyStorePacket::encode, BuyStorePacket::decode, BuyStorePacket::handle);
        INSTANCE.registerMessage(id++, SyncProcessingTargetPacket.class, SyncProcessingTargetPacket::encode, SyncProcessingTargetPacket::decode, SyncProcessingTargetPacket::handle);
        INSTANCE.registerMessage(id++, FinishProcessingPacket.class, FinishProcessingPacket::encode, FinishProcessingPacket::decode, FinishProcessingPacket::handle);
    }

    public static class KnockdownPacket {
        public KnockdownPacket() {
        }

        public static void encode(KnockdownPacket msg, FriendlyByteBuf buffer) {
        }

        public static KnockdownPacket decode(FriendlyByteBuf buffer) {
            return new KnockdownPacket();
        }

        public static void handle(KnockdownPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                // Запускаем анимацию падения на клиенте!
                net.votmdevs.voicesofthemines.client.ClientInputHandler.triggerKnockdown();
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class NotificationPacket {
        private final String message;

        public NotificationPacket(String message) {
            this.message = message;
        }

        public static void encode(NotificationPacket msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.message);
        }

        public static NotificationPacket decode(FriendlyByteBuf buffer) {
            return new NotificationPacket(buffer.readUtf());
        }

        public static void handle(NotificationPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> GmodNotificationManager.addNotification(msg.message));
            ctx.get().setPacketHandled(true);
        }
    }

    // Grab
    public static class GrabPacket {
        private final int entityId;
        private final boolean isGrabbing;

        public GrabPacket(int entityId, boolean isGrabbing) {
            this.entityId = entityId;
            this.isGrabbing = isGrabbing;
        }

        public static void encode(GrabPacket msg, FriendlyByteBuf buffer) {
            buffer.writeInt(msg.entityId);
            buffer.writeBoolean(msg.isGrabbing);
        }

        public static GrabPacket decode(FriendlyByteBuf buffer) {
            return new GrabPacket(buffer.readInt(), buffer.readBoolean());
        }

        public static void handle(GrabPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    if (msg.isGrabbing) {
                        Entity e = player.serverLevel().getEntity(msg.entityId);
                        if (e instanceof FleshEntity flesh && flesh.getFleshLevel() < 5) {
                            if (!flesh.isHeld()) {
                                flesh.setHeldBy(player.getUUID());
                                flesh.playSound(net.votmdevs.voicesofthemines.KerfurSounds.FLESH_GRAB.get(), 1.0F, 1.0F);
                            }
                        } else if (e instanceof GarbageEntity garbage && garbage.getGarbageLevel() < 5) {
                            if (!garbage.isHeld()) {
                                garbage.setHeldBy(player.getUUID());
                                garbage.playSound(net.votmdevs.voicesofthemines.KerfurSounds.GARBAGE_GRAB.get(), 1.0F, 1.0F);
                            }
                        } else if (e instanceof net.votmdevs.voicesofthemines.entity.MaxwellEntity maxwell) {
                            if (!maxwell.isHeld()) {
                                maxwell.setHeldBy(player.getUUID());
                            }
                        } else if (e instanceof net.votmdevs.voicesofthemines.entity.DriveEntity drive) {
                            if (!drive.isHeld()) {
                                drive.setHeldBy(player.getUUID());
                            }
                        } else if (e instanceof net.votmdevs.voicesofthemines.entity.FuelCanEntity fuelCan) {
                            if (!fuelCan.isHeld()) {
                                fuelCan.setHeldBy(player.getUUID());
                            }
                        } else if (e instanceof net.votmdevs.voicesofthemines.entity.AtvEntity atv) {
                            if (!atv.isHeld()) atv.setHeldBy(player.getUUID());
                        }
                    } else {
                        for (Entity e : player.level().getEntitiesOfClass(FleshEntity.class, player.getBoundingBox().inflate(10.0D))) {
                            if (e instanceof FleshEntity flesh && player.getUUID().equals(flesh.getHeldBy().orElse(null)))
                                flesh.setHeldBy(null);
                        }
                        for (Entity e : player.level().getEntitiesOfClass(GarbageEntity.class, player.getBoundingBox().inflate(10.0D))) {
                            if (e instanceof GarbageEntity garbage && player.getUUID().equals(garbage.getHeldBy().orElse(null)))
                                garbage.setHeldBy(null);
                        }
                        for (Entity e : player.level().getEntitiesOfClass(FuelCanEntity.class, player.getBoundingBox().inflate(10.0D))) {
                            if (e instanceof FuelCanEntity fuelCan && player.getUUID().equals(fuelCan.getHeldBy().orElse(null)))
                                fuelCan.setHeldBy(null);
                        }
                        for (Entity e : player.level().getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.DriveEntity.class, player.getBoundingBox().inflate(10.0D))) {
                            if (e instanceof net.votmdevs.voicesofthemines.entity.DriveEntity drive && player.getUUID().equals(drive.getHeldBy().orElse(null)))
                                drive.setHeldBy(null);
                        }
                        for (Entity e : player.level().getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.MaxwellEntity.class, player.getBoundingBox().inflate(10.0D))) {
                            if (e instanceof net.votmdevs.voicesofthemines.entity.MaxwellEntity maxwell && player.getUUID().equals(maxwell.getHeldBy().orElse(null)))
                                maxwell.setHeldBy(null);
                        }
                        for (Entity e : player.level().getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.AtvEntity.class, player.getBoundingBox().inflate(10.0D))) {
                            if (e instanceof net.votmdevs.voicesofthemines.entity.AtvEntity atv && player.getUUID().equals(atv.getHeldBy().orElse(null)))
                                atv.setHeldBy(null);
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class ExtractGarbagePacket {
        private final int entityId;

        public ExtractGarbagePacket(int entityId) {
            this.entityId = entityId;
        }

        public static void encode(ExtractGarbagePacket msg, FriendlyByteBuf buffer) {
            buffer.writeInt(msg.entityId);
        }

        public static ExtractGarbagePacket decode(FriendlyByteBuf buffer) {
            return new ExtractGarbagePacket(buffer.readInt());
        }

        public static void handle(ExtractGarbagePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    Entity e = player.serverLevel().getEntity(msg.entityId);
                    if (e instanceof GarbageEntity garbage && garbage.getGarbageLevel() > 1 && !garbage.isHeld()) {
                        garbage.setGarbageLevel(garbage.getGarbageLevel() - 1);

                        GarbageEntity singlePiece = KerfurMod.GARBAGE.get().create(player.level());
                        if (singlePiece != null) {
                            singlePiece.moveTo(player.getX(), player.getY() + 1.5D, player.getZ(), 0, 0);
                            player.level().addFreshEntity(singlePiece);
                            singlePiece.playSound(KerfurSounds.GARBAGE_GRAB.get(), 1.0F, 1.2F);
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class UpdatePosterUrlPacket {
        private final BlockPos pos;
        private final String url;

        public UpdatePosterUrlPacket(BlockPos pos, String url) {
            this.pos = pos;
            this.url = url;
        }

        public static void encode(UpdatePosterUrlPacket msg, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(msg.pos);
            buffer.writeUtf(msg.url);
        }

        public static UpdatePosterUrlPacket decode(FriendlyByteBuf buffer) {
            return new UpdatePosterUrlPacket(buffer.readBlockPos(), buffer.readUtf());
        }

        public static void handle(UpdatePosterUrlPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    BlockEntity be = player.level().getBlockEntity(msg.pos);
                    if (be instanceof net.votmdevs.voicesofthemines.block.PosterBlockEntity posterEntity) {
                        posterEntity.setCustomImageUrl(msg.url);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    // Hook
    public static class HookPullPacket {
        private final double scrollDelta;

        public HookPullPacket(double scrollDelta) {
            this.scrollDelta = scrollDelta;
        }

        public static void encode(HookPullPacket msg, FriendlyByteBuf buffer) {
            buffer.writeDouble(msg.scrollDelta);
        }

        public static HookPullPacket decode(FriendlyByteBuf buffer) {
            return new HookPullPacket(buffer.readDouble());
        }

        public static void handle(HookPullPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    for (Entity e : player.level().getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.HookEntity.class, player.getBoundingBox().inflate(30.0D))) {
                        if (e instanceof net.votmdevs.voicesofthemines.entity.HookEntity hook && player.getUUID().equals(hook.getOwnerUUID())) {
                            if (hook.isStuck()) {

                                int stuckId = hook.getStuckEntityId();

                                if (stuckId != -1) {
                                    // === ТЯНЕМ СУЩНОСТЬ К ИГРОКУ ===
                                    Entity stuckTarget = player.level().getEntity(stuckId);
                                    if (stuckTarget != null) {
                                        net.minecraft.world.phys.Vec3 pullVec = player.position().subtract(stuckTarget.position()).normalize();
                                        double distance = player.distanceTo(stuckTarget);
                                        double speed = Math.min(0.2D + (distance * 0.02D), 2.5D) * Math.signum(msg.scrollDelta);

                                        stuckTarget.setDeltaMovement(stuckTarget.getDeltaMovement().add(pullVec.scale(speed)));
                                        stuckTarget.hurtMarked = true;
                                        stuckTarget.fallDistance = 0.0F; // Спасаем моба от урона при падении
                                    }
                                } else {
                                    // === ТЯНЕМ ИГРОКА К БЛОКУ ===
                                    net.minecraft.world.phys.Vec3 pullVec = hook.position().subtract(player.position()).normalize();
                                    double distance = player.distanceTo(hook);
                                    double speed = Math.min(0.2D + (distance * 0.02D), 2.5D) * Math.signum(msg.scrollDelta);

                                    player.setDeltaMovement(player.getDeltaMovement().add(pullVec.scale(speed)));
                                    player.hurtMarked = true;
                                    player.fallDistance = 0.0F; // Спасаем себя от урона
                                }
                            }
                            break;
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class HookDetachPacket {
        public HookDetachPacket() {
        }

        public static void encode(HookDetachPacket msg, FriendlyByteBuf buffer) {
        }

        public static HookDetachPacket decode(FriendlyByteBuf buffer) {
            return new HookDetachPacket();
        }

        public static void handle(HookDetachPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    for (Entity e : player.level().getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.HookEntity.class, player.getBoundingBox().inflate(20.0D))) {
                        if (e instanceof net.votmdevs.voicesofthemines.entity.HookEntity hook && player.getUUID().equals(hook.getOwnerUUID())) {
                            hook.discard(); // Удаляем крюк
                            break;
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    // Trash
    public static class PackGarbagePacket {
        private final int entityId;

        public PackGarbagePacket(int entityId) {
            this.entityId = entityId;
        }

        public static void encode(PackGarbagePacket msg, FriendlyByteBuf buffer) {
            buffer.writeInt(msg.entityId);
        }

        public static PackGarbagePacket decode(FriendlyByteBuf buffer) {
            return new PackGarbagePacket(buffer.readInt());
        }

        public static void handle(PackGarbagePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    Entity e = player.serverLevel().getEntity(msg.entityId);
                    if (e instanceof GarbageEntity garbage) {
                        net.minecraft.world.item.ItemStack handItem = player.getMainHandItem();

                        if (handItem.getItem() == KerfurMod.TRASH_ROLL.get()) {

                            if (garbage.getGarbageLevel() > 1) {
                                garbage.setGarbageLevel(garbage.getGarbageLevel() - 1);
                            } else {
                                garbage.discard();
                            }

                            handItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));

                            net.minecraft.world.entity.item.ItemEntity bagEntity = new net.minecraft.world.entity.item.ItemEntity(
                                    player.level(), garbage.getX(), garbage.getY() + 0.5, garbage.getZ(),
                                    new net.minecraft.world.item.ItemStack(KerfurMod.TRASH_BAG.get())
                            );
                            player.level().addFreshEntity(bagEntity);
                            player.level().playSound(null, garbage.blockPosition(), net.votmdevs.voicesofthemines.KerfurSounds.PACK_GARBAGE.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    // Break ATV
    public static class AtvBrakePacket {
        private final boolean isBraking;

        public AtvBrakePacket(boolean isBraking) {
            this.isBraking = isBraking;
        }

        public static void encode(AtvBrakePacket msg, FriendlyByteBuf buffer) {
            buffer.writeBoolean(msg.isBraking);
        }

        public static AtvBrakePacket decode(FriendlyByteBuf buffer) {
            return new AtvBrakePacket(buffer.readBoolean());
        }

        public static void handle(AtvBrakePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.getVehicle() instanceof net.votmdevs.voicesofthemines.entity.AtvEntity atv) {
                    atv.setBraking(msg.isBraking);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    // ;P
    public static class AtvCrashPacket {
        public AtvCrashPacket() {
        }

        public static void encode(AtvCrashPacket msg, FriendlyByteBuf buffer) {
        }

        public static AtvCrashPacket decode(FriendlyByteBuf buffer) {
            return new AtvCrashPacket();
        }

        public static void handle(AtvCrashPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.getVehicle() instanceof net.votmdevs.voicesofthemines.entity.AtvEntity atv) {
                    atv.level().playSound(null, atv.blockPosition(), net.votmdevs.voicesofthemines.KerfurSounds.ATV_CRASH.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.stopRiding();
                    net.minecraft.world.phys.Vec3 look = atv.getLookAngle();
                    player.setDeltaMovement(-look.x * 0.8, 0.5, -look.z * 0.8);
                    player.hurtMarked = true;
                    player.hurt(player.damageSources().generic(), 8.0f);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    // SIGNALS!!!
    public static class SyncSignalsPacket {
        private final java.util.List<net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal> signals;

        public SyncSignalsPacket(java.util.List<net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal> signals) {
            this.signals = signals;
        }

        public static void encode(SyncSignalsPacket msg, FriendlyByteBuf buffer) {
            buffer.writeInt(msg.signals.size());
            for (net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal s : msg.signals) {
                buffer.writeUtf(s.id);
                buffer.writeFloat(s.x);
                buffer.writeFloat(s.y);
                buffer.writeUtf(s.type);
                buffer.writeBoolean(s.isDownloaded);
                buffer.writeBoolean(s.isCalibrated);
                buffer.writeBoolean(s.isChecked);
                buffer.writeFloat(s.targetLine);
                buffer.writeFloat(s.targetWave);
            }
        }

        public static SyncSignalsPacket decode(FriendlyByteBuf buffer) {
            java.util.List<net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal> list = new java.util.ArrayList<>();
            int size = buffer.readInt();
            for (int i = 0; i < size; i++) {
                list.add(new net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal(
                        buffer.readUtf(), buffer.readFloat(), buffer.readFloat(), buffer.readUtf(),
                        buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(),
                        buffer.readFloat(), buffer.readFloat()
                ));
            }
            return new SyncSignalsPacket(list);
        }

        public static void handle(SyncSignalsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                net.votmdevs.voicesofthemines.client.gui.TerminalFindScreen.CLIENT_SIGNALS = msg.signals;
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class CatchSignalPacket {
        private final String signalId;

        public CatchSignalPacket(String signalId) {
            this.signalId = signalId;
        }

        public static void encode(CatchSignalPacket msg, FriendlyByteBuf buffer) {
            buffer.writeUtf(msg.signalId);
        }

        public static CatchSignalPacket decode(FriendlyByteBuf buffer) {
            return new CatchSignalPacket(buffer.readUtf());
        }

        public static void handle(CatchSignalPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    net.votmdevs.voicesofthemines.world.SignalManager.get(player.serverLevel()).catchSignal(msg.signalId);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class SyncProcessingStatePacket {
        private final boolean isProcessing;

        public SyncProcessingStatePacket(boolean isProcessing) {
            this.isProcessing = isProcessing;
        }

        public static void encode(SyncProcessingStatePacket msg, FriendlyByteBuf buffer) {
            buffer.writeBoolean(msg.isProcessing);
        }

        public static SyncProcessingStatePacket decode(FriendlyByteBuf buffer) {
            return new SyncProcessingStatePacket(buffer.readBoolean());
        }

        public static void handle(SyncProcessingStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                net.votmdevs.voicesofthemines.client.gui.TerminalFindScreen.IS_PROCESSING_ACTIVE = msg.isProcessing;
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class SyncCalibrateTargetPacket {
        private final boolean hasSignal;
        private final float targetLine;
        private final float targetWave;
        private final String signalType;

        public SyncCalibrateTargetPacket(boolean hasSignal, float targetLine, float targetWave, String signalType) {
            this.hasSignal = hasSignal;
            this.targetLine = targetLine;
            this.targetWave = targetWave;
            this.signalType = signalType;
        }

        public static void encode(SyncCalibrateTargetPacket msg, FriendlyByteBuf buffer) {
            buffer.writeBoolean(msg.hasSignal);
            buffer.writeFloat(msg.targetLine);
            buffer.writeFloat(msg.targetWave);
            buffer.writeUtf(msg.signalType != null ? msg.signalType : "");
        }

        public static SyncCalibrateTargetPacket decode(FriendlyByteBuf buffer) {
            return new SyncCalibrateTargetPacket(buffer.readBoolean(), buffer.readFloat(), buffer.readFloat(), buffer.readUtf());
        }

        public static void handle(SyncCalibrateTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                if (net.votmdevs.voicesofthemines.client.gui.TerminalCalibrateScreen.CURRENT_TARGET_LINE != msg.targetLine ||
                        net.votmdevs.voicesofthemines.client.gui.TerminalCalibrateScreen.CURRENT_TARGET_WAVE != msg.targetWave) {

                    net.votmdevs.voicesofthemines.client.gui.TerminalCalibrateScreen.SESSION_DATA.clear();
                }

                net.votmdevs.voicesofthemines.client.gui.TerminalCalibrateScreen.HAS_ACTIVE_SIGNAL = msg.hasSignal;
                if (msg.hasSignal) {
                    net.votmdevs.voicesofthemines.client.gui.TerminalCalibrateScreen.CURRENT_TARGET_LINE = msg.targetLine;
                    net.votmdevs.voicesofthemines.client.gui.TerminalCalibrateScreen.CURRENT_TARGET_WAVE = msg.targetWave;
                    net.votmdevs.voicesofthemines.client.gui.TerminalCalibrateScreen.CURRENT_SIGNAL_TYPE = msg.signalType;
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class SyncCheckTargetPacket {
        private final boolean hasSignal;
        private final String signalType;
        private final int signalLevel; // НОВОЕ

        public SyncCheckTargetPacket(boolean hasSignal, String signalType, int signalLevel) {
            this.hasSignal = hasSignal;
            this.signalType = signalType;
            this.signalLevel = signalLevel;
        }
        public static void encode(SyncCheckTargetPacket msg, FriendlyByteBuf buffer) {
            buffer.writeBoolean(msg.hasSignal);
            buffer.writeUtf(msg.signalType != null ? msg.signalType : "");
            buffer.writeInt(msg.signalLevel);
        }
        public static SyncCheckTargetPacket decode(FriendlyByteBuf buffer) {
            return new SyncCheckTargetPacket(buffer.readBoolean(), buffer.readUtf(), buffer.readInt());
        }
        public static void handle(SyncCheckTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                net.votmdevs.voicesofthemines.client.gui.TerminalCheckScreen.HAS_ACTIVE_SIGNAL = msg.hasSignal;
                if (msg.hasSignal) {
                    net.votmdevs.voicesofthemines.client.gui.TerminalCheckScreen.CURRENT_SIGNAL_TYPE = msg.signalType;
                    net.votmdevs.voicesofthemines.client.gui.TerminalCheckScreen.CURRENT_SIGNAL_LEVEL = msg.signalLevel;
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class FinishCalibrationPacket {
        public FinishCalibrationPacket() {
        }

        public static void encode(FinishCalibrationPacket msg, FriendlyByteBuf buffer) {
        }

        public static FinishCalibrationPacket decode(FriendlyByteBuf buffer) {
            return new FinishCalibrationPacket();
        }

        public static void handle(FinishCalibrationPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(player.serverLevel());
                    net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal sig = manager.getProcessingSignal();
                    if (sig != null) {
                        manager.finishCalibration(sig.id);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class FinishCheckPacket {
        private final BlockPos pos;

        public FinishCheckPacket(BlockPos pos) {
            this.pos = pos;
        }

        public static void encode(FinishCheckPacket msg, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(msg.pos);
        }

        public static FinishCheckPacket decode(FriendlyByteBuf buffer) {
            return new FinishCheckPacket(buffer.readBlockPos());
        }

        public static void handle(FinishCheckPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    BlockEntity be = player.level().getBlockEntity(msg.pos);
                    if (be instanceof net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity terminal) {

                        if (terminal.hasDrive()) {
                            String sigId = terminal.getDriveSignalId();
                            String sigType = terminal.getDriveSignalType();
                            int sigLevel = terminal.getDriveSignalLevel();

                            net.votmdevs.voicesofthemines.entity.DriveEntity drive = KerfurMod.DRIVE.get().create(player.serverLevel());
                            if (drive != null) {
                                drive.moveTo(msg.pos.getX() + 0.5, msg.pos.getY() + 1.2, msg.pos.getZ() + 0.5, 0, 0);
                                drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_ID, sigId != null ? sigId : "");
                                drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_TYPE, sigType != null ? sigType : "");
                                drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_LEVEL, sigLevel);

                                net.minecraft.world.phys.Vec3 playerPos = player.getEyePosition();
                                net.minecraft.world.phys.Vec3 blockPos = new net.minecraft.world.phys.Vec3(msg.pos.getX() + 0.5, msg.pos.getY() + 1.2, msg.pos.getZ() + 0.5);
                                net.minecraft.world.phys.Vec3 throwVec = playerPos.subtract(blockPos).normalize().scale(0.4D);
                                drive.setDeltaMovement(throwVec.x, 0.3D, throwVec.z);

                                player.level().addFreshEntity(drive);
                                player.level().playSound(null, msg.pos, net.votmdevs.voicesofthemines.KerfurSounds.BUTTON_CLICK.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                            }

                            terminal.setDrive(false, "", "", 0);

                            if (sigId != null && !sigId.isEmpty()) {
                                net.votmdevs.voicesofthemines.world.SignalManager.get(player.serverLevel()).finishCheck(sigId);
                            }
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class InsertDrivePacket {
        private final BlockPos pos;
        private final int driveEntityId;

        public InsertDrivePacket(BlockPos pos, int driveEntityId) {
            this.pos = pos;
            this.driveEntityId = driveEntityId;
        }

        public static void encode(InsertDrivePacket msg, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(msg.pos);
            buffer.writeInt(msg.driveEntityId);
        }

        public static InsertDrivePacket decode(FriendlyByteBuf buffer) {
            return new InsertDrivePacket(buffer.readBlockPos(), buffer.readInt());
        }

        public static void handle(InsertDrivePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    Entity e = player.serverLevel().getEntity(msg.driveEntityId);
                    BlockEntity be = player.serverLevel().getBlockEntity(msg.pos);

                    if (e instanceof net.votmdevs.voicesofthemines.entity.DriveEntity drive && be instanceof net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity terminal) {
                        if (!terminal.hasDrive()) {
                            String sigId = drive.getEntityData().get(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_ID);
                            boolean isEmpty = (sigId == null || sigId.isEmpty());
                            net.minecraft.world.level.block.Block block = player.serverLevel().getBlockState(msg.pos).getBlock();
                            if (block == KerfurMod.TERMINAL_PROCESSING.get() && isEmpty) {
                                player.level().playSound(null, msg.pos, KerfurSounds.BUG_ALERT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.5F);
                                return; // Прерываем выполнение! Диск не вставится.
                            }
                            String sigType = drive.getEntityData().get(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_TYPE);
                            int sigLevel = drive.getEntityData().get(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_LEVEL);

                            terminal.setDrive(true, sigId != null ? sigId : "", sigType != null ? sigType : "", sigLevel);

                            player.level().playSound(null, msg.pos, KerfurSounds.DRIVE_IN.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                            drive.discard();
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
    //GUI Processing
    public static class SyncProcessingTargetPacket {
        private final boolean hasSignal;
        private final String signalType;
        private final int signalLevel;

        public SyncProcessingTargetPacket(boolean hasSignal, String signalType, int signalLevel) {
            this.hasSignal = hasSignal;
            this.signalType = signalType;
            this.signalLevel = signalLevel;
        }
        public static void encode(SyncProcessingTargetPacket msg, FriendlyByteBuf buffer) {
            buffer.writeBoolean(msg.hasSignal);
            buffer.writeUtf(msg.signalType != null ? msg.signalType : "");
            buffer.writeInt(msg.signalLevel);
        }
        public static SyncProcessingTargetPacket decode(FriendlyByteBuf buffer) {
            return new SyncProcessingTargetPacket(buffer.readBoolean(), buffer.readUtf(), buffer.readInt());
        }
        public static void handle(SyncProcessingTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                net.votmdevs.voicesofthemines.client.gui.TerminalProcessingScreen.HAS_ACTIVE_SIGNAL = msg.hasSignal;
                if (msg.hasSignal) {
                    net.votmdevs.voicesofthemines.client.gui.TerminalProcessingScreen.CURRENT_SIGNAL_TYPE = msg.signalType;
                    net.votmdevs.voicesofthemines.client.gui.TerminalProcessingScreen.CURRENT_SIGNAL_LEVEL = msg.signalLevel;
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class FinishProcessingPacket {
        private final BlockPos pos;
        public FinishProcessingPacket(BlockPos pos) { this.pos = pos; }
        public static void encode(FinishProcessingPacket msg, FriendlyByteBuf buffer) { buffer.writeBlockPos(msg.pos); }
        public static FinishProcessingPacket decode(FriendlyByteBuf buffer) { return new FinishProcessingPacket(buffer.readBlockPos()); }

        public static void handle(FinishProcessingPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    BlockEntity be = player.level().getBlockEntity(msg.pos);
                    if (be instanceof net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity terminal) {
                        if (terminal.hasDrive()) {
                            String sigId = terminal.getDriveSignalId();
                            String sigType = terminal.getDriveSignalType();
                            int sigLevel = terminal.getDriveSignalLevel();

                            net.votmdevs.voicesofthemines.entity.DriveEntity drive = KerfurMod.DRIVE.get().create(player.serverLevel());
                            if (drive != null) {
                                drive.moveTo(msg.pos.getX() + 0.5, msg.pos.getY() + 1.2, msg.pos.getZ() + 0.5, 0, 0);
                                drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_ID, sigId != null ? sigId : "");
                                drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_TYPE, sigType != null ? sigType : "");
                                drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_LEVEL, sigLevel + 1); // УЛУЧШАЕМ!

                                net.minecraft.world.phys.Vec3 throwVec = player.getEyePosition().subtract(new net.minecraft.world.phys.Vec3(msg.pos.getX() + 0.5, msg.pos.getY() + 1.2, msg.pos.getZ() + 0.5)).normalize().scale(0.4D);
                                drive.setDeltaMovement(throwVec.x, 0.3D, throwVec.z);
                                player.level().addFreshEntity(drive);
                                player.level().playSound(null, msg.pos, net.votmdevs.voicesofthemines.KerfurSounds.BUTTON_CLICK.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            terminal.setDrive(false, "", "", 0);
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
    public static class SyncComputerDataPacket {
        private final int points;
        private final int cursorLvl, pingLvl, procSpeedLvl, procLvlLvl;
        private final java.util.List<net.votmdevs.voicesofthemines.world.PlayerData.Email> emails; // Новое!

        public SyncComputerDataPacket(int points, int cursorLvl, int pingLvl, int procSpeedLvl, int procLvlLvl, java.util.List<net.votmdevs.voicesofthemines.world.PlayerData.Email> emails) {
            this.points = points; this.cursorLvl = cursorLvl; this.pingLvl = pingLvl;
            this.procSpeedLvl = procSpeedLvl; this.procLvlLvl = procLvlLvl; this.emails = emails;
        }

        public static void encode(SyncComputerDataPacket msg, FriendlyByteBuf buffer) {
            buffer.writeInt(msg.points);
            buffer.writeInt(msg.cursorLvl); buffer.writeInt(msg.pingLvl);
            buffer.writeInt(msg.procSpeedLvl); buffer.writeInt(msg.procLvlLvl);
            buffer.writeInt(msg.emails.size());
            for (net.votmdevs.voicesofthemines.world.PlayerData.Email e : msg.emails) {
                buffer.writeUtf(e.sender); buffer.writeUtf(e.title); buffer.writeUtf(e.text); buffer.writeBoolean(e.isRead);
            }
        }

        public static SyncComputerDataPacket decode(FriendlyByteBuf buffer) {
            int pts = buffer.readInt(); int c = buffer.readInt(); int p = buffer.readInt(); int ps = buffer.readInt(); int pl = buffer.readInt();
            int size = buffer.readInt();
            java.util.List<net.votmdevs.voicesofthemines.world.PlayerData.Email> emails = new java.util.ArrayList<>();
            for (int i = 0; i < size; i++) emails.add(new net.votmdevs.voicesofthemines.world.PlayerData.Email(buffer.readUtf(), buffer.readUtf(), buffer.readUtf(), buffer.readBoolean()));
            return new SyncComputerDataPacket(pts, c, p, ps, pl, emails);
        }

        public static void handle(SyncComputerDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                net.votmdevs.voicesofthemines.client.gui.ComputerScreen.POINTS = msg.points;
                net.votmdevs.voicesofthemines.client.gui.ComputerScreen.UPG_CURSOR = msg.cursorLvl;
                net.votmdevs.voicesofthemines.client.gui.ComputerScreen.UPG_PING = msg.pingLvl;
                net.votmdevs.voicesofthemines.client.gui.ComputerScreen.UPG_PROC_SPEED = msg.procSpeedLvl;
                net.votmdevs.voicesofthemines.client.gui.ComputerScreen.UPG_PROC_LVL = msg.procLvlLvl;
                net.votmdevs.voicesofthemines.client.gui.ComputerScreen.EMAILS = msg.emails; // Принимаем письма на клиент!
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class BuyUpgradePacket {
        private final String upgradeType;
        public BuyUpgradePacket(String upgradeType) { this.upgradeType = upgradeType; }
        public static void encode(BuyUpgradePacket msg, FriendlyByteBuf buffer) { buffer.writeUtf(msg.upgradeType); }
        public static BuyUpgradePacket decode(FriendlyByteBuf buffer) { return new BuyUpgradePacket(buffer.readUtf()); }

        public static void handle(BuyUpgradePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(player.serverLevel());

                    if (manager.getGlobalPlayerData().buyUpgrade(msg.upgradeType)) {
                        manager.setDirty();
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);

                        net.votmdevs.voicesofthemines.world.PlayerData pd = manager.getGlobalPlayerData();
                        KerfurPacketHandler.INSTANCE.sendTo(new SyncComputerDataPacket(pd.getPoints(), pd.getCursorSpeedLvl(), pd.getPingCooldownLvl(), pd.getProcessingSpeedLvl(), pd.getProcessingLevelLvl(),pd.getEmails()), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
                    } else {
                        player.level().playSound(null, player.blockPosition(), KerfurSounds.BUG_ALERT.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class BuyStorePacket {
        private final int totalCost;
        private final java.util.List<String> items;

        public BuyStorePacket(int totalCost, java.util.List<String> items) {
            this.totalCost = totalCost;
            this.items = items;
        }

        public static void encode(BuyStorePacket msg, FriendlyByteBuf buffer) {
            buffer.writeInt(msg.totalCost);
            buffer.writeInt(msg.items.size());
            for (String s : msg.items) buffer.writeUtf(s);
        }

        public static BuyStorePacket decode(FriendlyByteBuf buffer) {
            int cost = buffer.readInt();
            int size = buffer.readInt();
            java.util.List<String> items = new java.util.ArrayList<>();
            for (int i = 0; i < size; i++) items.add(buffer.readUtf());
            return new BuyStorePacket(cost, items);
        }

        public static void handle(BuyStorePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(player.serverLevel());


                    if (manager.getGlobalPlayerData().spendPoints(msg.totalCost)) {

                        for (String itemId : msg.items) {
                            net.minecraft.world.item.Item mcItem = getItemById(itemId);
                            if (mcItem != null) {
                                manager.getGlobalPlayerData().addDelivery(new net.minecraft.world.item.ItemStack(mcItem));
                            }
                        }

                        manager.setDirty();
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.NOTE_BLOCK_CHIME.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);

                        net.votmdevs.voicesofthemines.world.PlayerData pd = manager.getGlobalPlayerData();
                        KerfurPacketHandler.INSTANCE.sendTo(new SyncComputerDataPacket(pd.getPoints(), pd.getCursorSpeedLvl(), pd.getPingCooldownLvl(), pd.getProcessingSpeedLvl(), pd.getProcessingLevelLvl(),pd.getEmails()), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
                    } else {
                        player.level().playSound(null, player.blockPosition(), KerfurSounds.BUG_ALERT.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }

        private static net.minecraft.world.item.Item getItemById(String id) {
            switch(id) {
                case "hazard_suit": return net.votmdevs.voicesofthemines.KerfurMod.HAZARD_HELMET.get();
                case "hook": return net.votmdevs.voicesofthemines.KerfurMod.HOOK_ITEM.get();
                case "trash_roll": return net.votmdevs.voicesofthemines.KerfurMod.TRASH_ROLL.get();
                case "glasses": return net.votmdevs.voicesofthemines.KerfurMod.ACCESSORY_GLASSES.get();
                case "jacket": return net.votmdevs.voicesofthemines.KerfurMod.ACCESSORY_JACKET.get();
                case "keypad": return net.votmdevs.voicesofthemines.KerfurMod.KEYPAD_ITEM.get();
                case "poster": return net.votmdevs.voicesofthemines.KerfurMod.POSTER_ITEM.get();
                case "taco": return net.votmdevs.voicesofthemines.KerfurMod.TACO.get();
                case "toblerone": return net.votmdevs.voicesofthemines.KerfurMod.TOBLERONE.get();
                case "cheese": return net.votmdevs.voicesofthemines.KerfurMod.CHEESE.get();
                case "burger": return net.votmdevs.voicesofthemines.KerfurMod.BURGER.get();
                case "painter_black": return net.votmdevs.voicesofthemines.KerfurMod.PAINTER_BLACK.get();
                case "painter_blue": return net.votmdevs.voicesofthemines.KerfurMod.PAINTER_BLUE.get();
                case "painter_red": return net.votmdevs.voicesofthemines.KerfurMod.PAINTER_RED.get();
                case "painter_green": return net.votmdevs.voicesofthemines.KerfurMod.PAINTER_GREEN.get();
                case "painter_pink": return net.votmdevs.voicesofthemines.KerfurMod.PAINTER_PINK.get();
                case "painter_white": return net.votmdevs.voicesofthemines.KerfurMod.PAINTER_WHITE.get();
                case "painter_yellow": return net.votmdevs.voicesofthemines.KerfurMod.PAINTER_YELLOW.get();
                default: return null;
            }
        }
    }
    // email
    public static class ReadEmailPacket {
        private final int index;
        public ReadEmailPacket(int index) { this.index = index; }
        public static void encode(ReadEmailPacket msg, FriendlyByteBuf buffer) { buffer.writeInt(msg.index); }
        public static ReadEmailPacket decode(FriendlyByteBuf buffer) { return new ReadEmailPacket(buffer.readInt()); }
        public static void handle(ReadEmailPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(player.serverLevel());
                    java.util.List<net.votmdevs.voicesofthemines.world.PlayerData.Email> emails = manager.getGlobalPlayerData().getEmails();
                    if (msg.index >= 0 && msg.index < emails.size()) {
                        emails.get(msg.index).isRead = true;
                        manager.setDirty();
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class DeleteEmailPacket {
        private final int index;
        public DeleteEmailPacket(int index) { this.index = index; }
        public static void encode(DeleteEmailPacket msg, FriendlyByteBuf buffer) { buffer.writeInt(msg.index); }
        public static DeleteEmailPacket decode(FriendlyByteBuf buffer) { return new DeleteEmailPacket(buffer.readInt()); }
        public static void handle(DeleteEmailPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(player.serverLevel());
                    java.util.List<net.votmdevs.voicesofthemines.world.PlayerData.Email> emails = manager.getGlobalPlayerData().getEmails();
                    if (msg.index >= 0 && msg.index < emails.size()) {
                        emails.remove(msg.index);
                        manager.setDirty();
                        net.votmdevs.voicesofthemines.world.PlayerData pd = manager.getGlobalPlayerData();
                        KerfurPacketHandler.INSTANCE.sendTo(new SyncComputerDataPacket(pd.getPoints(), pd.getCursorSpeedLvl(), pd.getPingCooldownLvl(), pd.getProcessingSpeedLvl(), pd.getProcessingLevelLvl(), pd.getEmails()), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
    // EMAIL NOTIF
    public static class EmailNotificationPacket {
        public EmailNotificationPacket() {}

        public static void encode(EmailNotificationPacket msg, FriendlyByteBuf buffer) {}

        public static EmailNotificationPacket decode(FriendlyByteBuf buffer) { return new EmailNotificationPacket(); }

        public static void handle(EmailNotificationPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.player != null && mc.level != null) {
                    BlockPos playerPos = mc.player.blockPosition();
                    BlockPos tablePos = null;

                    for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-32, -16, -32), playerPos.offset(32, 16, 32))) {
                        if (mc.level.getBlockState(pos).getBlock() == net.votmdevs.voicesofthemines.KerfurMod.TABLE.get()) {
                            tablePos = pos;
                            break;
                        }
                    }

                    if (tablePos != null) {
                        mc.level.playLocalSound(tablePos.getX() + 0.5, tablePos.getY() + 0.5, tablePos.getZ() + 0.5,
                                net.votmdevs.voicesofthemines.KerfurSounds.EMAIL_ALERT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F, false);
                    } else {
                        mc.player.playSound(net.votmdevs.voicesofthemines.KerfurSounds.EMAIL_ALERT.get(), 1.0F, 1.0F);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}