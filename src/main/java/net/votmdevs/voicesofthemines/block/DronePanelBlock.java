package net.votmdevs.voicesofthemines.block;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.entity.DroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DronePanelBlock extends BaseEntityBlock {
    public DronePanelBlock(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DronePanelBlockEntity panel) {
                if (player.isShiftKeyDown()) {
                    if (panel.isOpen) {

                        List<DroneEntity> activeDrones = level.getEntitiesOfClass(DroneEntity.class, player.getBoundingBox().inflate(2000.0D));
                        if (!activeDrones.isEmpty()) {
                            sendNotification(player, "ERR: Drone is already deployed!");
                            return InteractionResult.SUCCESS;
                        }

                        BlockPos target = findTarget(level, pos);
                        if (target != null) {
                            DroneEntity drone = KerfurMod.DRONE.get().create(level);
                            if (drone != null) {
                                drone.setPos(target.getX() - 100, target.getY() + 30, target.getZ());
                                drone.setTargetPosition(target);

                                net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get((net.minecraft.server.level.ServerLevel)level);
                                net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> queue = manager.getGlobalPlayerData().getDeliveryQueue();

                                int slot = 0;
                                java.util.Iterator<net.minecraft.world.item.ItemStack> it = queue.iterator();
                                while(it.hasNext() && slot < 27) {
                                    drone.inventory.setItem(slot++, it.next());
                                    it.remove();
                                }
                                manager.setDirty();

                                level.addFreshEntity(drone);
                                sendNotification(player, "Drone dispatched from West.");
                            }
                        } else {
                            sendNotification(player, "ERR: No Drone Target block found in radius!");
                        }
                    }
                } else {
                    panel.toggleOpen();
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private BlockPos findTarget(Level level, BlockPos startPos) {
        for (int x = -10; x <= 10; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos p = startPos.offset(x, y, z);
                    if (level.getBlockState(p).getBlock() == KerfurMod.DRONE_TARGET.get()) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    private void sendNotification(Player player, String message) {
        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                    new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.NotificationPacket(message),
                    sp.connection.connection,
                    net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
            );
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DronePanelBlockEntity(pos, state); }
}