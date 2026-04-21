package net.votmdevs.voicesofthemines.network;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
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
            new ResourceLocation(VoicesOfTheMines.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, FixServerPacket.class, FixServerPacket::encode, FixServerPacket::decode, FixServerPacket::handle);
        INSTANCE.registerMessage(id++, ListCustomItemPacket.class, ListCustomItemPacket::encode, ListCustomItemPacket::decode, ListCustomItemPacket::handle);
        INSTANCE.registerMessage(id++, SendEmailPacket.class, SendEmailPacket::encode, SendEmailPacket::decode, SendEmailPacket::handle);
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
        public KnockdownPacket() {}
        public static void encode(KnockdownPacket msg, FriendlyByteBuf buffer) {}
        public static KnockdownPacket decode(FriendlyByteBuf buffer) { return new KnockdownPacket(); }
        public static void handle(KnockdownPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> net.votmdevs.voicesofthemines.client.ClientInputHandler.triggerKnockdown());
            ctx.get().setPacketHandled(true);
        }
    }

    public static class NotificationPacket {
        private final String message;
        public NotificationPacket(String message) { this.message = message; }
        public static void encode(NotificationPacket msg, FriendlyByteBuf buffer) { buffer.writeUtf(msg.message); }
        public static NotificationPacket decode(FriendlyByteBuf buffer) { return new NotificationPacket(buffer.readUtf()); }
        public static void handle(NotificationPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> GmodNotificationManager.addNotification(msg.message));
            ctx.get().setPacketHandled(true);
        }
    }

    public static class GrabPacket {
        private final int entityId;
        private final boolean isGrabbing;

        public GrabPacket(int entityId, boolean isGrabbing) { this.entityId = entityId; this.isGrabbing = isGrabbing; }

        public static void encode(GrabPacket msg, FriendlyByteBuf buffer) { buffer.writeInt(msg.entityId); buffer.writeBoolean(msg.isGrabbing); }

        public static GrabPacket decode(FriendlyByteBuf buffer) { return new GrabPacket(buffer.readInt(), buffer.readBoolean()); }

        public static void handle(GrabPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    if (msg.isGrabbing) {
                        Entity e = player.serverLevel().getEntity(msg.entityId);
                        if (e instanceof FleshEntity flesh && flesh.getFleshLevel() < 5) {
                            if (!flesh.isHeld()) { flesh.setHeldBy(player.getUUID()); flesh.playSound(VotmSounds.FLESH_GRAB.get(), 1.0F, 1.0F); }
                        } else if (e instanceof GarbageEntity garbage && garbage.getGarbageLevel() < 5) {
                            if (!garbage.isHeld()) { garbage.setHeldBy(player.getUUID()); garbage.playSound(VotmSounds.GARBAGE_GRAB.get(), 1.0F, 1.0F); }
                        } else if (e instanceof net.votmdevs.voicesofthemines.entity.MaxwellEntity maxwell) {
                            if (!maxwell.isHeld()) maxwell.setHeldBy(player.getUUID());
                        } else if (e instanceof net.votmdevs.voicesofthemines.entity.DriveEntity drive) {
                            if (!drive.isHeld()) drive.setHeldBy(player.getUUID());
                        } else if (e instanceof net.votmdevs.voicesofthemines.entity.FuelCanEntity fuelCan) {
                            if (!fuelCan.isHeld()) fuelCan.setHeldBy(player.getUUID());
                        } else if (e instanceof net.votmdevs.voicesofthemines.entity.AtvEntity atv) {
                            if (!atv.isHeld()) atv.setHeldBy(player.getUUID());
                        }
                    } else {
                        for (Entity e : player.level().getEntitiesOfClass(FleshEntity.class, player.getBoundingBox().inflate(10.0D))) {
                            if (e instanceof FleshEntity flesh && player.getUUID().equals(flesh.getHeldBy().orElse(null))) flesh.setHeldBy(null);
                        }
                        for (Entity e : player.level().getEntitiesOfClass(GarbageEntity.class, player.getBoundingBox().inflate(10.0D))) {
                            if (e instanceof GarbageEntity garbage && player.getUUID().equals(garbage.getHeldBy().orElse(null))) garbage.setHeldBy(null);
                        }
                        for (Entity e : player.level().getEntitiesOfClass(FuelCanEntity.class, player.getBoundingBox().inflate(10.0D))) {
                            if (e instanceof FuelCanEntity fuelCan && player.getUUID().equals(fuelCan.getHeldBy().orElse(null))) fuelCan.setHeldBy(null);
                        }
                        for (Entity e : player.level().getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.DriveEntity.class, player.getBoundingBox().inflate(10.0D))) {
                            if (e instanceof net.votmdevs.voicesofthemines.entity.DriveEntity drive && player.getUUID().equals(drive.getHeldBy().orElse(null))) drive.setHeldBy(null);
                        }
                        for (Entity e : player.level().getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.MaxwellEntity.class, player.getBoundingBox().inflate(10.0D))) {
                            if (e instanceof net.votmdevs.voicesofthemines.entity.MaxwellEntity maxwell && player.getUUID().equals(maxwell.getHeldBy().orElse(null))) maxwell.setHeldBy(null);
                        }
                        for (Entity e : player.level().getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.AtvEntity.class, player.getBoundingBox().inflate(10.0D))) {
                            if (e instanceof net.votmdevs.voicesofthemines.entity.AtvEntity atv && player.getUUID().equals(atv.getHeldBy().orElse(null))) atv.setHeldBy(null);
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class ExtractGarbagePacket {
        private final int entityId;
        public ExtractGarbagePacket(int entityId) { this.entityId = entityId; }
        public static void encode(ExtractGarbagePacket msg, FriendlyByteBuf buffer) { buffer.writeInt(msg.entityId); }
        public static ExtractGarbagePacket decode(FriendlyByteBuf buffer) { return new ExtractGarbagePacket(buffer.readInt()); }

        public static void handle(ExtractGarbagePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    Entity e = player.serverLevel().getEntity(msg.entityId);
                    if (e instanceof GarbageEntity garbage && garbage.getGarbageLevel() > 1 && !garbage.isHeld()) {
                        garbage.setGarbageLevel(garbage.getGarbageLevel() - 1);
                        GarbageEntity singlePiece = VoicesOfTheMines.GARBAGE.get().create(player.level());
                        if (singlePiece != null) {
                            singlePiece.moveTo(player.getX(), player.getY() + 1.5D, player.getZ(), 0, 0);
                            player.level().addFreshEntity(singlePiece);
                            singlePiece.playSound(VotmSounds.GARBAGE_GRAB.get(), 1.0F, 1.2F);
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
        public UpdatePosterUrlPacket(BlockPos pos, String url) { this.pos = pos; this.url = url; }
        public static void encode(UpdatePosterUrlPacket msg, FriendlyByteBuf buffer) { buffer.writeBlockPos(msg.pos); buffer.writeUtf(msg.url); }
        public static UpdatePosterUrlPacket decode(FriendlyByteBuf buffer) { return new UpdatePosterUrlPacket(buffer.readBlockPos(), buffer.readUtf()); }

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

    public static class HookPullPacket {
        private final double scrollDelta;
        public HookPullPacket(double scrollDelta) { this.scrollDelta = scrollDelta; }
        public static void encode(HookPullPacket msg, FriendlyByteBuf buffer) { buffer.writeDouble(msg.scrollDelta); }
        public static HookPullPacket decode(FriendlyByteBuf buffer) { return new HookPullPacket(buffer.readDouble()); }

        public static void handle(HookPullPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    for (Entity e : player.level().getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.HookEntity.class, player.getBoundingBox().inflate(30.0D))) {
                        if (e instanceof net.votmdevs.voicesofthemines.entity.HookEntity hook && player.getUUID().equals(hook.getOwnerUUID())) {
                            if (hook.isStuck()) {
                                int stuckId = hook.getStuckEntityId();
                                if (stuckId != -1) {
                                    Entity stuckTarget = player.level().getEntity(stuckId);
                                    if (stuckTarget != null) {
                                        net.minecraft.world.phys.Vec3 pullVec = player.position().subtract(stuckTarget.position()).normalize();
                                        double distance = player.distanceTo(stuckTarget);
                                        double speed = Math.min(0.2D + (distance * 0.02D), 2.5D) * Math.signum(msg.scrollDelta);
                                        stuckTarget.setDeltaMovement(stuckTarget.getDeltaMovement().add(pullVec.scale(speed)));
                                        stuckTarget.hurtMarked = true; stuckTarget.fallDistance = 0.0F;
                                    }
                                } else {
                                    net.minecraft.world.phys.Vec3 pullVec = hook.position().subtract(player.position()).normalize();
                                    double distance = player.distanceTo(hook);
                                    double speed = Math.min(0.2D + (distance * 0.02D), 2.5D) * Math.signum(msg.scrollDelta);
                                    player.setDeltaMovement(player.getDeltaMovement().add(pullVec.scale(speed)));
                                    player.hurtMarked = true; player.fallDistance = 0.0F;
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
        public HookDetachPacket() {}
        public static void encode(HookDetachPacket msg, FriendlyByteBuf buffer) {}
        public static HookDetachPacket decode(FriendlyByteBuf buffer) { return new HookDetachPacket(); }
        public static void handle(HookDetachPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    for (Entity e : player.level().getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.HookEntity.class, player.getBoundingBox().inflate(20.0D))) {
                        if (e instanceof net.votmdevs.voicesofthemines.entity.HookEntity hook && player.getUUID().equals(hook.getOwnerUUID())) {
                            hook.discard();
                            break;
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class PackGarbagePacket {
        private final int entityId;
        public PackGarbagePacket(int entityId) { this.entityId = entityId; }
        public static void encode(PackGarbagePacket msg, FriendlyByteBuf buffer) { buffer.writeInt(msg.entityId); }
        public static PackGarbagePacket decode(FriendlyByteBuf buffer) { return new PackGarbagePacket(buffer.readInt()); }

        public static void handle(PackGarbagePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    Entity e = player.serverLevel().getEntity(msg.entityId);
                    if (e instanceof GarbageEntity garbage) {
                        net.minecraft.world.item.ItemStack handItem = player.getMainHandItem();
                        if (handItem.getItem() == VoicesOfTheMines.TRASH_ROLL.get()) {
                            if (garbage.getGarbageLevel() > 1) {
                                garbage.setGarbageLevel(garbage.getGarbageLevel() - 1);
                            } else {
                                garbage.discard();
                            }
                            handItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));
                            net.minecraft.world.entity.item.ItemEntity bagEntity = new net.minecraft.world.entity.item.ItemEntity(
                                    player.level(), garbage.getX(), garbage.getY() + 0.5, garbage.getZ(),
                                    new net.minecraft.world.item.ItemStack(VoicesOfTheMines.TRASH_BAG.get())
                            );
                            player.level().addFreshEntity(bagEntity);
                            player.level().playSound(null, garbage.blockPosition(), VotmSounds.PACK_GARBAGE.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class AtvBrakePacket {
        private final boolean isBraking;
        public AtvBrakePacket(boolean isBraking) { this.isBraking = isBraking; }
        public static void encode(AtvBrakePacket msg, FriendlyByteBuf buffer) { buffer.writeBoolean(msg.isBraking); }
        public static AtvBrakePacket decode(FriendlyByteBuf buffer) { return new AtvBrakePacket(buffer.readBoolean()); }
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

    public static class AtvCrashPacket {
        public AtvCrashPacket() {}
        public static void encode(AtvCrashPacket msg, FriendlyByteBuf buffer) {}
        public static AtvCrashPacket decode(FriendlyByteBuf buffer) { return new AtvCrashPacket(); }
        public static void handle(AtvCrashPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.getVehicle() instanceof net.votmdevs.voicesofthemines.entity.AtvEntity atv) {
                    atv.level().playSound(null, atv.blockPosition(), VotmSounds.ATV_CRASH.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.stopRiding();
                    net.minecraft.world.phys.Vec3 look = atv.getLookAngle();
                    player.setDeltaMovement(-look.x * 0.8, 0.5, -look.z * 0.8);
                    player.hurtMarked = true; player.hurt(player.damageSources().generic(), 8.0f);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class SyncSignalsPacket {
        private final java.util.List<net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal> signals;
        public SyncSignalsPacket(java.util.List<net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal> signals) { this.signals = signals; }
        public static void encode(SyncSignalsPacket msg, FriendlyByteBuf buffer) {
            buffer.writeInt(msg.signals.size());
            for (net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal s : msg.signals) {
                buffer.writeUtf(s.id); buffer.writeFloat(s.x); buffer.writeFloat(s.y); buffer.writeUtf(s.type);
                buffer.writeBoolean(s.isDownloaded); buffer.writeBoolean(s.isCalibrated); buffer.writeBoolean(s.isChecked);
                buffer.writeFloat(s.targetLine); buffer.writeFloat(s.targetWave);
            }
        }
        public static SyncSignalsPacket decode(FriendlyByteBuf buffer) {
            java.util.List<net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal> list = new java.util.ArrayList<>();
            int size = buffer.readInt();
            for (int i = 0; i < size; i++) {
                list.add(new net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal(buffer.readUtf(), buffer.readFloat(), buffer.readFloat(), buffer.readUtf(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readFloat(), buffer.readFloat()));
            }
            return new SyncSignalsPacket(list);
        }
        public static void handle(SyncSignalsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> net.votmdevs.voicesofthemines.client.gui.TerminalFindScreen.CLIENT_SIGNALS = msg.signals);
            ctx.get().setPacketHandled(true);
        }
    }

    public static class CatchSignalPacket {
        private final String signalId;
        public CatchSignalPacket(String signalId) { this.signalId = signalId; }
        public static void encode(CatchSignalPacket msg, FriendlyByteBuf buffer) { buffer.writeUtf(msg.signalId); }
        public static CatchSignalPacket decode(FriendlyByteBuf buffer) { return new CatchSignalPacket(buffer.readUtf()); }
        public static void handle(CatchSignalPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) net.votmdevs.voicesofthemines.world.SignalManager.get(player.serverLevel()).catchSignal(msg.signalId);
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class SyncProcessingStatePacket {
        private final boolean isProcessing;
        public SyncProcessingStatePacket(boolean isProcessing) { this.isProcessing = isProcessing; }
        public static void encode(SyncProcessingStatePacket msg, FriendlyByteBuf buffer) { buffer.writeBoolean(msg.isProcessing); }
        public static SyncProcessingStatePacket decode(FriendlyByteBuf buffer) { return new SyncProcessingStatePacket(buffer.readBoolean()); }
        public static void handle(SyncProcessingStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> net.votmdevs.voicesofthemines.client.gui.TerminalFindScreen.IS_PROCESSING_ACTIVE = msg.isProcessing);
            ctx.get().setPacketHandled(true);
        }
    }

    public static class SyncCalibrateTargetPacket {
        private final boolean hasSignal;
        private final float targetLine, targetWave;
        private final String signalType;
        public SyncCalibrateTargetPacket(boolean hasSignal, float targetLine, float targetWave, String signalType) {
            this.hasSignal = hasSignal; this.targetLine = targetLine; this.targetWave = targetWave; this.signalType = signalType;
        }
        public static void encode(SyncCalibrateTargetPacket msg, FriendlyByteBuf buffer) {
            buffer.writeBoolean(msg.hasSignal); buffer.writeFloat(msg.targetLine); buffer.writeFloat(msg.targetWave); buffer.writeUtf(msg.signalType != null ? msg.signalType : "");
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
        private final int signalLevel;
        public SyncCheckTargetPacket(boolean hasSignal, String signalType, int signalLevel) { this.hasSignal = hasSignal; this.signalType = signalType; this.signalLevel = signalLevel; }
        public static void encode(SyncCheckTargetPacket msg, FriendlyByteBuf buffer) { buffer.writeBoolean(msg.hasSignal); buffer.writeUtf(msg.signalType != null ? msg.signalType : ""); buffer.writeInt(msg.signalLevel); }
        public static SyncCheckTargetPacket decode(FriendlyByteBuf buffer) { return new SyncCheckTargetPacket(buffer.readBoolean(), buffer.readUtf(), buffer.readInt()); }
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
        public FinishCalibrationPacket() {}
        public static void encode(FinishCalibrationPacket msg, FriendlyByteBuf buffer) {}
        public static FinishCalibrationPacket decode(FriendlyByteBuf buffer) { return new FinishCalibrationPacket(); }
        public static void handle(FinishCalibrationPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(player.serverLevel());
                    net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal sig = manager.getProcessingSignal();
                    if (sig != null) manager.finishCalibration(sig.id);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class FinishCheckPacket {
        private final BlockPos pos;
        public FinishCheckPacket(BlockPos pos) { this.pos = pos; }
        public static void encode(FinishCheckPacket msg, FriendlyByteBuf buffer) { buffer.writeBlockPos(msg.pos); }
        public static FinishCheckPacket decode(FriendlyByteBuf buffer) { return new FinishCheckPacket(buffer.readBlockPos()); }
        public static void handle(FinishCheckPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    BlockEntity be = player.level().getBlockEntity(msg.pos);
                    if (be instanceof net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity terminal && terminal.hasDrive()) {
                        String sigId = terminal.getDriveSignalId(), sigType = terminal.getDriveSignalType();
                        int sigLevel = terminal.getDriveSignalLevel();

                        net.votmdevs.voicesofthemines.entity.DriveEntity drive = VoicesOfTheMines.DRIVE.get().create(player.serverLevel());
                        if (drive != null) {
                            drive.moveTo(msg.pos.getX() + 0.5, msg.pos.getY() + 1.2, msg.pos.getZ() + 0.5, 0, 0);
                            drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_ID, sigId != null ? sigId : "");
                            drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_TYPE, sigType != null ? sigType : "");
                            drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_LEVEL, sigLevel);

                            net.minecraft.world.phys.Vec3 throwVec = player.getEyePosition().subtract(new net.minecraft.world.phys.Vec3(msg.pos.getX() + 0.5, msg.pos.getY() + 1.2, msg.pos.getZ() + 0.5)).normalize().scale(0.4D);
                            drive.setDeltaMovement(throwVec.x, 0.3D, throwVec.z);
                            player.level().addFreshEntity(drive);
                            player.level().playSound(null, msg.pos, VotmSounds.BUTTON_CLICK.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                        terminal.setDrive(false, "", "", 0);
                        if (sigId != null && !sigId.isEmpty()) net.votmdevs.voicesofthemines.world.SignalManager.get(player.serverLevel()).finishCheck(sigId);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class InsertDrivePacket {
        private final BlockPos pos;
        private final int driveEntityId;
        public InsertDrivePacket(BlockPos pos, int driveEntityId) { this.pos = pos; this.driveEntityId = driveEntityId; }
        public static void encode(InsertDrivePacket msg, FriendlyByteBuf buffer) { buffer.writeBlockPos(msg.pos); buffer.writeInt(msg.driveEntityId); }
        public static InsertDrivePacket decode(FriendlyByteBuf buffer) { return new InsertDrivePacket(buffer.readBlockPos(), buffer.readInt()); }
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
                            if (block == VoicesOfTheMines.TERMINAL_PROCESSING.get() && isEmpty) {
                                player.level().playSound(null, msg.pos, VotmSounds.BUG_ALERT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.5F);
                                return;
                            }
                            String sigType = drive.getEntityData().get(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_TYPE);
                            int sigLevel = drive.getEntityData().get(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_LEVEL);

                            terminal.setDrive(true, sigId != null ? sigId : "", sigType != null ? sigType : "", sigLevel);
                            player.level().playSound(null, msg.pos, VotmSounds.DRIVE_IN.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                            drive.discard();
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class SyncProcessingTargetPacket {
        private final boolean hasSignal;
        private final String signalType;
        private final int signalLevel;
        public SyncProcessingTargetPacket(boolean hasSignal, String signalType, int signalLevel) { this.hasSignal = hasSignal; this.signalType = signalType; this.signalLevel = signalLevel; }
        public static void encode(SyncProcessingTargetPacket msg, FriendlyByteBuf buffer) { buffer.writeBoolean(msg.hasSignal); buffer.writeUtf(msg.signalType != null ? msg.signalType : ""); buffer.writeInt(msg.signalLevel); }
        public static SyncProcessingTargetPacket decode(FriendlyByteBuf buffer) { return new SyncProcessingTargetPacket(buffer.readBoolean(), buffer.readUtf(), buffer.readInt()); }
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
                    if (be instanceof net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity terminal && terminal.hasDrive()) {
                        String sigId = terminal.getDriveSignalId(), sigType = terminal.getDriveSignalType();
                        int sigLevel = terminal.getDriveSignalLevel();

                        net.votmdevs.voicesofthemines.entity.DriveEntity drive = VoicesOfTheMines.DRIVE.get().create(player.serverLevel());
                        if (drive != null) {
                            drive.moveTo(msg.pos.getX() + 0.5, msg.pos.getY() + 1.2, msg.pos.getZ() + 0.5, 0, 0);
                            drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_ID, sigId != null ? sigId : "");
                            drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_TYPE, sigType != null ? sigType : "");
                            drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_LEVEL, sigLevel + 1);

                            net.minecraft.world.phys.Vec3 throwVec = player.getEyePosition().subtract(new net.minecraft.world.phys.Vec3(msg.pos.getX() + 0.5, msg.pos.getY() + 1.2, msg.pos.getZ() + 0.5)).normalize().scale(0.4D);
                            drive.setDeltaMovement(throwVec.x, 0.3D, throwVec.z);
                            player.level().addFreshEntity(drive);
                            player.level().playSound(null, msg.pos, VotmSounds.BUTTON_CLICK.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                        terminal.setDrive(false, "", "", 0);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class SyncComputerDataPacket {
        private final int points, cursorLvl, pingLvl, procSpeedLvl, procLvlLvl;
        private final java.util.List<net.votmdevs.voicesofthemines.world.PlayerData.Email> emails;
        private final java.util.List<net.votmdevs.voicesofthemines.world.PlayerData.CustomLot> market;

        public SyncComputerDataPacket(int points, int c, int p, int ps, int pl, java.util.List<net.votmdevs.voicesofthemines.world.PlayerData.Email> emails, java.util.List<net.votmdevs.voicesofthemines.world.PlayerData.CustomLot> market) {
            this.points = points; this.cursorLvl = c; this.pingLvl = p; this.procSpeedLvl = ps; this.procLvlLvl = pl; this.emails = emails; this.market = market;
        }

        public static void encode(SyncComputerDataPacket msg, FriendlyByteBuf buffer) {
            buffer.writeInt(msg.points); buffer.writeInt(msg.cursorLvl); buffer.writeInt(msg.pingLvl); buffer.writeInt(msg.procSpeedLvl); buffer.writeInt(msg.procLvlLvl);
            buffer.writeInt(msg.emails.size());
            for (net.votmdevs.voicesofthemines.world.PlayerData.Email e : msg.emails) {
                buffer.writeUtf(e.sender); buffer.writeUtf(e.title); buffer.writeUtf(e.text); buffer.writeBoolean(e.isRead);
            }
            buffer.writeInt(msg.market.size());
            for (net.votmdevs.voicesofthemines.world.PlayerData.CustomLot lot : msg.market) {
                buffer.writeUtf(lot.lotId); buffer.writeItem(lot.stack); buffer.writeInt(lot.price);
            }
        }

        public static SyncComputerDataPacket decode(FriendlyByteBuf buffer) {
            int pts = buffer.readInt(), c = buffer.readInt(), p = buffer.readInt(), ps = buffer.readInt(), pl = buffer.readInt();
            int eSize = buffer.readInt();
            java.util.List<net.votmdevs.voicesofthemines.world.PlayerData.Email> emails = new java.util.ArrayList<>();
            for (int i = 0; i < eSize; i++) emails.add(new net.votmdevs.voicesofthemines.world.PlayerData.Email(buffer.readUtf(), buffer.readUtf(), buffer.readUtf(), buffer.readBoolean()));

            int mSize = buffer.readInt();
            java.util.List<net.votmdevs.voicesofthemines.world.PlayerData.CustomLot> market = new java.util.ArrayList<>();
            for (int i = 0; i < mSize; i++) market.add(new net.votmdevs.voicesofthemines.world.PlayerData.CustomLot(buffer.readUtf(), null, buffer.readItem(), buffer.readInt()));

            return new SyncComputerDataPacket(pts, c, p, ps, pl, emails, market);
        }

        public static void handle(SyncComputerDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                net.votmdevs.voicesofthemines.client.gui.ComputerScreen.POINTS = msg.points;
                net.votmdevs.voicesofthemines.client.gui.ComputerScreen.UPG_CURSOR = msg.cursorLvl;
                net.votmdevs.voicesofthemines.client.gui.ComputerScreen.UPG_PING = msg.pingLvl;
                net.votmdevs.voicesofthemines.client.gui.ComputerScreen.UPG_PROC_SPEED = msg.procSpeedLvl;
                net.votmdevs.voicesofthemines.client.gui.ComputerScreen.UPG_PROC_LVL = msg.procLvlLvl;
                net.votmdevs.voicesofthemines.client.gui.ComputerScreen.EMAILS = msg.emails;
                net.votmdevs.voicesofthemines.client.gui.ComputerScreen.CUSTOM_MARKET = msg.market;
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

                    if (manager.getGlobalPlayerData().buyUpgrade(player.getUUID(), msg.upgradeType)) {
                        manager.setDirty();
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);

                        net.votmdevs.voicesofthemines.world.PlayerData pd = manager.getGlobalPlayerData();
                        KerfurPacketHandler.INSTANCE.sendTo(new SyncComputerDataPacket(pd.getPoints(player.getUUID()), pd.getCursorSpeedLvl(), pd.getPingCooldownLvl(), pd.getProcessingSpeedLvl(), pd.getProcessingLevelLvl(), pd.getEmails(player.getUUID()), pd.customMarket), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
                    } else {
                        player.level().playSound(null, player.blockPosition(), VotmSounds.BUG_ALERT.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class BuyStorePacket {
        private final int totalCost;
        private final java.util.List<String> standardItems;
        private final java.util.List<String> customLotIds;

        public BuyStorePacket(int totalCost, java.util.List<String> standardItems, java.util.List<String> customLotIds) {
            this.totalCost = totalCost; this.standardItems = standardItems; this.customLotIds = customLotIds;
        }

        public static void encode(BuyStorePacket msg, FriendlyByteBuf buffer) {
            buffer.writeInt(msg.totalCost);
            buffer.writeInt(msg.standardItems.size()); for (String s : msg.standardItems) buffer.writeUtf(s);
            buffer.writeInt(msg.customLotIds.size()); for (String s : msg.customLotIds) buffer.writeUtf(s);
        }

        public static BuyStorePacket decode(FriendlyByteBuf buffer) {
            int cost = buffer.readInt();
            int sSize = buffer.readInt(); java.util.List<String> sItems = new java.util.ArrayList<>(); for (int i = 0; i < sSize; i++) sItems.add(buffer.readUtf());
            int cSize = buffer.readInt(); java.util.List<String> cItems = new java.util.ArrayList<>(); for (int i = 0; i < cSize; i++) cItems.add(buffer.readUtf());
            return new BuyStorePacket(cost, sItems, cItems);
        }

        public static void handle(BuyStorePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(player.serverLevel());
                    net.votmdevs.voicesofthemines.world.PlayerData pd = manager.getGlobalPlayerData();

                    if (pd.spendPoints(player.getUUID(), msg.totalCost)) {
                        for (String itemId : msg.standardItems) {
                            net.minecraft.world.item.Item mcItem = getItemById(itemId);
                            if (mcItem != null) pd.addDelivery(player.getUUID(), new net.minecraft.world.item.ItemStack(mcItem));
                        }

                        for (String lotId : msg.customLotIds) {
                            net.votmdevs.voicesofthemines.world.PlayerData.CustomLot boughtLot = null;
                            for (int i = 0; i < pd.customMarket.size(); i++) {
                                if (pd.customMarket.get(i).lotId.equals(lotId)) { boughtLot = pd.customMarket.remove(i); break; }
                            }
                            if (boughtLot != null) {
                                pd.addDelivery(player.getUUID(), boughtLot.stack);
                                pd.addPoints(boughtLot.sellerId, boughtLot.price); // Переводим деньги продавцу
                                pd.addEmail(boughtLot.sellerId, "Market", "Item Sold", "Your item '" + boughtLot.stack.getHoverName().getString() + "' was bought! " + boughtLot.price + " points added to balance.");
                            }
                        }

                        manager.setDirty();
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.NOTE_BLOCK_CHIME.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
                        KerfurPacketHandler.INSTANCE.sendTo(new SyncComputerDataPacket(pd.getPoints(player.getUUID()), pd.getCursorSpeedLvl(), pd.getPingCooldownLvl(), pd.getProcessingSpeedLvl(), pd.getProcessingLevelLvl(), pd.getEmails(player.getUUID()), pd.customMarket), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
                    } else {
                        player.level().playSound(null, player.blockPosition(), VotmSounds.BUG_ALERT.get(), net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }

        private static net.minecraft.world.item.Item getItemById(String id) {
            switch(id) {
                case "hazard_suit": return VoicesOfTheMines.HAZARD_HELMET.get();
                case "hook": return VoicesOfTheMines.HOOK_ITEM.get();
                case "trash_roll": return VoicesOfTheMines.TRASH_ROLL.get();
                case "glasses": return VoicesOfTheMines.ACCESSORY_GLASSES.get();
                case "jacket": return VoicesOfTheMines.ACCESSORY_JACKET.get();
                case "keypad": return VoicesOfTheMines.KEYPAD_ITEM.get();
                case "poster": return VoicesOfTheMines.POSTER_ITEM.get();
                case "taco": return VoicesOfTheMines.TACO.get();
                case "toblerone": return VoicesOfTheMines.TOBLERONE.get();
                case "cheese": return VoicesOfTheMines.CHEESE.get();
                case "burger": return VoicesOfTheMines.BURGER.get();
                case "painter_black": return VoicesOfTheMines.PAINTER_BLACK.get();
                case "painter_blue": return VoicesOfTheMines.PAINTER_BLUE.get();
                case "painter_red": return VoicesOfTheMines.PAINTER_RED.get();
                case "painter_green": return VoicesOfTheMines.PAINTER_GREEN.get();
                case "painter_pink": return VoicesOfTheMines.PAINTER_PINK.get();
                case "painter_white": return VoicesOfTheMines.PAINTER_WHITE.get();
                case "painter_yellow": return VoicesOfTheMines.PAINTER_YELLOW.get();
                default: return null;
            }
        }
    }

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
                    java.util.List<net.votmdevs.voicesofthemines.world.PlayerData.Email> emails = manager.getGlobalPlayerData().getEmails(player.getUUID());
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
                    java.util.List<net.votmdevs.voicesofthemines.world.PlayerData.Email> emails = manager.getGlobalPlayerData().getEmails(player.getUUID());
                    if (msg.index >= 0 && msg.index < emails.size()) {
                        emails.remove(msg.index);
                        manager.setDirty();
                        net.votmdevs.voicesofthemines.world.PlayerData pd = manager.getGlobalPlayerData();
                        KerfurPacketHandler.INSTANCE.sendTo(new SyncComputerDataPacket(pd.getPoints(player.getUUID()), pd.getCursorSpeedLvl(), pd.getPingCooldownLvl(), pd.getProcessingSpeedLvl(), pd.getProcessingLevelLvl(), pd.getEmails(player.getUUID()), pd.customMarket), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

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
                        if (mc.level.getBlockState(pos).getBlock() == VoicesOfTheMines.TABLE.get()) {
                            tablePos = pos;
                            break;
                        }
                    }

                    if (tablePos != null) {
                        mc.level.playLocalSound(tablePos.getX() + 0.5, tablePos.getY() + 0.5, tablePos.getZ() + 0.5,
                                VotmSounds.EMAIL_ALERT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F, false);
                    } else {
                        mc.player.playSound(VotmSounds.EMAIL_ALERT.get(), 1.0F, 1.0F);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class ListCustomItemPacket {
        private final int price;
        public ListCustomItemPacket(int price) { this.price = price; }
        public static void encode(ListCustomItemPacket msg, FriendlyByteBuf buffer) { buffer.writeInt(msg.price); }
        public static ListCustomItemPacket decode(FriendlyByteBuf buffer) { return new ListCustomItemPacket(buffer.readInt()); }

        public static void handle(ListCustomItemPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    net.minecraft.world.item.ItemStack stack = player.getMainHandItem();
                    if (!stack.isEmpty() && msg.price > 0) {
                        net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(player.serverLevel());
                        manager.getGlobalPlayerData().customMarket.add(new net.votmdevs.voicesofthemines.world.PlayerData.CustomLot(java.util.UUID.randomUUID().toString(), player.getUUID(), stack.copy(), msg.price));
                        player.getMainHandItem().shrink(stack.getCount()); // Забираем предмет из рук!
                        manager.setDirty();
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class SendEmailPacket {
        private final String to, from, topic, text;
        public SendEmailPacket(String to, String from, String topic, String text) { this.to = to; this.from = from; this.topic = topic; this.text = text; }
        public static void encode(SendEmailPacket msg, FriendlyByteBuf buffer) { buffer.writeUtf(msg.to); buffer.writeUtf(msg.from); buffer.writeUtf(msg.topic); buffer.writeUtf(msg.text); }
        public static SendEmailPacket decode(FriendlyByteBuf buffer) { return new SendEmailPacket(buffer.readUtf(), buffer.readUtf(), buffer.readUtf(), buffer.readUtf()); }

        public static void handle(SendEmailPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(player.serverLevel());
                    java.util.UUID targetUUID = manager.getGlobalPlayerData().getUUIDByName(msg.to);
                    if (targetUUID != null) {
                        manager.getGlobalPlayerData().addEmail(targetUUID, msg.from, msg.topic, msg.text);
                        manager.setDirty();

                        ServerPlayer targetPlayer = player.server.getPlayerList().getPlayer(targetUUID);
                        if (targetPlayer != null) KerfurPacketHandler.INSTANCE.sendTo(new EmailNotificationPacket(), targetPlayer.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
                        KerfurPacketHandler.INSTANCE.sendTo(
                                new NotificationPacket("Email sent successfully!"),
                                player.connection.connection,
                                net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                        );
                    } else {
                        manager.getGlobalPlayerData().addEmail(player.getUUID(), "System", "Error", "User '" + msg.to + "' not found.");
                        manager.setDirty();
                        KerfurPacketHandler.INSTANCE.sendTo(
                                new NotificationPacket("ERR: User not found!"),
                                player.connection.connection,
                                net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                        );
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
    public static class FixServerPacket {
        private final BlockPos pos;
        public FixServerPacket(BlockPos pos) { this.pos = pos; }
        public static void encode(FixServerPacket msg, FriendlyByteBuf buffer) { buffer.writeBlockPos(msg.pos); }
        public static FixServerPacket decode(FriendlyByteBuf buffer) { return new FixServerPacket(buffer.readBlockPos()); }

        public static void handle(FixServerPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    net.minecraft.world.level.block.state.BlockState state = player.level().getBlockState(msg.pos);
                    if (state.getBlock() == net.votmdevs.voicesofthemines.VoicesOfTheMines.SERVER_BLOCK.get()) {
                        player.level().setBlock(msg.pos, state.setValue(net.votmdevs.voicesofthemines.block.ServerBlock.BROKEN, false), 3);
                        player.level().playSound(null, msg.pos, net.minecraft.sounds.SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}