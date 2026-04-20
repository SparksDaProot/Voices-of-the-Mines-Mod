package net.votmdevs.voicesofthemines;

import net.votmdevs.voicesofthemines.entity.CockroachEntity;
import net.votmdevs.voicesofthemines.entity.FleshEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = KerfurMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KerfurEventHandler {

    @SubscribeEvent
    public static void onZombieDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Zombie zombie) {
            Level level = zombie.level();
            if (!level.isClientSide()) {
                if (level.random.nextFloat() < 0.5F) {
                    FleshEntity flesh = KerfurMod.FLESH.get().create(level);
                    if (flesh != null) {
                        flesh.moveTo(zombie.getX(), zombie.getY(), zombie.getZ(), zombie.getYRot(), 0.0F);
                        flesh.setDeltaMovement((level.random.nextFloat() - 0.5) * 0.2, 0.2, (level.random.nextFloat() - 0.5) * 0.2);
                        level.addFreshEntity(flesh);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            ServerLevel level = (ServerLevel) event.level;
            if (level.dimension() == Level.OVERWORLD) {
                net.votmdevs.voicesofthemines.world.SignalManager.get(level).tick();
            }
            if (level.getGameTime() % 20 == 0) {
                for (Player player : level.players()) {
                    List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(24.0D));
                    for (ItemEntity item : items) {
                        if (item.getItem().getItem().isEdible() && item.getAge() >= 200 && !item.getPersistentData().getBoolean("RoachesSpawned")) {
                            item.getPersistentData().putBoolean("RoachesSpawned", true);
                            int count = 3 + level.random.nextInt(3);
                            for (int i = 0; i < count; i++) {
                                CockroachEntity roach = KerfurMod.COCKROACH.get().create(level);
                                if (roach != null) {
                                    roach.moveTo(item.getX() + (level.random.nextDouble() - 0.5), item.getY(), item.getZ() + (level.random.nextDouble() - 0.5), level.random.nextFloat() * 360F, 0);
                                    level.addFreshEntity(roach);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // Knockdown
    @SubscribeEvent
    public static void onPlayerDamage(net.minecraftforge.event.entity.living.LivingDamageEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {

            if (event.getAmount() >= 6.0F) {
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                        new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.KnockdownPacket(),
                        player.connection.connection,
                        net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
            }
        }
    }


    // Radiation/Suit
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            Player player = event.player;
            if (player.tickCount % 20 == 0) {

                boolean hasCapsule = false;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    if (player.getInventory().getItem(i).getItem() == KerfurMod.RADIOACTIVE_CAPSULE.get()) {
                        hasCapsule = true;
                        break;
                    }
                }

                if (hasCapsule) {
                    boolean hasHelmet = player.getItemBySlot(EquipmentSlot.HEAD).getItem() == KerfurMod.HAZARD_HELMET.get();
                    boolean hasChest = player.getItemBySlot(EquipmentSlot.CHEST).getItem() == KerfurMod.HAZARD_CHESTPLATE.get();
                    boolean hasLegs = player.getItemBySlot(EquipmentSlot.LEGS).getItem() == KerfurMod.HAZARD_LEGGINGS.get();
                    boolean hasBoots = player.getItemBySlot(EquipmentSlot.FEET).getItem() == KerfurMod.HAZARD_BOOTS.get();

                    if (!(hasHelmet && hasChest && hasLegs && hasBoots)) {
                        player.addEffect(new MobEffectInstance(KerfurMod.RADIATION.get(), 100, 0, false, true, true));
                    }
                }
            }
        }
    }
}