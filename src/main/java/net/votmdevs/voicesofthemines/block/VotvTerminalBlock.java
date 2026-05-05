package net.votmdevs.voicesofthemines.block;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.votmdevs.voicesofthemines.VotmSounds;
import org.jetbrains.annotations.Nullable;

public class VotvTerminalBlock extends BaseEntityBlock {
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private final VoxelShape shapeNorth;
    private final VoxelShape shapeEast;
    private final VoxelShape shapeSouth;
    private final VoxelShape shapeWest;

    public VotvTerminalBlock(Properties properties, VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        this.shapeNorth = north;
        this.shapeEast = east;
        this.shapeSouth = south;
        this.shapeWest = west;
    }

    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);

        // tr
        if (stack.getItem() == net.minecraft.world.item.Items.REDSTONE) {
            if (!level.isClientSide()) {
                net.minecraft.nbt.CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("SelectedMainTransformer")) {
                    BlockPos mainPos = BlockPos.of(tag.getLong("SelectedMainTransformer"));
                    BlockEntity mainBe = level.getBlockEntity(mainPos);
                    if (mainBe instanceof TransformerBlockEntity mainTransformer && mainTransformer.isMain) {
                        if (!mainTransformer.connectedDevices.contains(pos)) {
                            mainTransformer.connectedDevices.add(pos);
                            mainTransformer.setChanged();
                            if (be instanceof IPowerableDevice device) {
                                device.setPowered(mainTransformer.isActive);
                            }
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§bTerminal linked to network!"), true);
                            level.playSound(null, pos, VotmSounds.CONNECT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                            ((net.minecraft.server.level.ServerLevel) level).sendParticles(net.minecraft.core.particles.DustParticleOptions.REDSTONE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.0);
                        } else {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cDevice already linked!"), true);
                        }
                    }
                }
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }

        // power check
        if (be instanceof IPowerableDevice device && !device.isPowered()) {
            if (!level.isClientSide()) {
                level.playSound(null, pos, VotmSounds.DENY.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        // stop selling
        if (this == VoicesOfTheMines.TABLE.get() && !stack.isEmpty()) {
            if (stack.getCount() > 50) {
                if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                            new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.NotificationPacket("ERR: Max stack size for sale is 50!"),
                            sp.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                    );
                    level.playSound(null, pos, VotmSounds.BUG_ALERT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.5F);
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
            if (level.isClientSide()) {
                net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.SellItemScreen(stack));
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }

        // terminals
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(serverPlayer.serverLevel());
            manager.getGlobalPlayerData().initPlayerIfNeeded(serverPlayer.getUUID(), serverPlayer.getScoreboardName());

            boolean isBaseBroken = false;
            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-50, -20, -50), pos.offset(50, 20, 50))) {
                BlockState st = level.getBlockState(p);
                if (st.getBlock() == VoicesOfTheMines.SERVER_BLOCK.get() &&
                        st.getValue(net.votmdevs.voicesofthemines.block.ServerBlock.TYPE) == net.votmdevs.voicesofthemines.block.ServerType.BASE &&
                        st.getValue(net.votmdevs.voicesofthemines.block.ServerBlock.BROKEN)) {
                    isBaseBroken = true;
                    break;
                }
            }
            if (isBaseBroken) {
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                        new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.NotificationPacket("ERR: BASE SERVER OFFLINE. CONNECTION LOST."),
                        serverPlayer.connection.connection,
                        net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
                level.playSound(null, pos, VotmSounds.BUG_ALERT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.5F);
                return net.minecraft.world.InteractionResult.SUCCESS;
            }

            if (this == VoicesOfTheMines.TERMINAL_FIND.get()) {
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                        new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncSignalsPacket(manager.getUncaughtSignals()),
                        serverPlayer.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                        new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncProcessingStatePacket(manager.hasProcessingSignal()),
                        serverPlayer.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
            }
            else if (this == VoicesOfTheMines.TERMINAL_CALIBRATE.get()) {
                net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal sig = manager.getProcessingSignal();
                boolean hasSig = (sig != null);
                float tLine = hasSig ? sig.targetLine : 0f;
                float tWave = hasSig ? sig.targetWave : 0f;
                String sType = hasSig ? sig.type : "";

                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                        new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncCalibrateTargetPacket(hasSig, tLine, tWave, sType),
                        serverPlayer.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
            }
            else if (this == VoicesOfTheMines.TABLE.get()) {
                net.votmdevs.voicesofthemines.world.PlayerData pd = manager.getGlobalPlayerData();
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                        new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncComputerDataPacket(
                                pd.getPoints(serverPlayer.getUUID()), pd.getCursorSpeedLvl(), pd.getPingCooldownLvl(), pd.getProcessingSpeedLvl(), pd.getProcessingLevelLvl(), pd.getEmails(serverPlayer.getUUID()), pd.customMarket
                        ), serverPlayer.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
            }
            else if (this == VoicesOfTheMines.TERMINAL_PROCESSING.get()) {
                if (be instanceof VotvTerminalBlockEntity terminal && terminal.hasDrive()) {
                    net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                            new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncProcessingTargetPacket(true, terminal.getDriveSignalType(), terminal.getDriveSignalLevel()),
                            serverPlayer.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                    );
                }
            }
            else if (this == VoicesOfTheMines.TERMINAL_CHECK.get()) {
                if (be instanceof VotvTerminalBlockEntity terminal && terminal.hasDrive()) {
                    String sType = terminal.getDriveSignalType();
                    boolean hasSig = true;
                    if (terminal.getDriveSignalId() == null || terminal.getDriveSignalId().isEmpty()) {
                        net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal sig = manager.getCalibratedSignal();
                        if (sig != null) {
                            sType = sig.type;
                            terminal.setDrive(true, sig.id, sig.type);
                        } else hasSig = false;
                    }
                    net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                            new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncCheckTargetPacket(hasSig, sType, terminal.getDriveSignalLevel()),
                            serverPlayer.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                    );
                }
            }
        } else {
            // client
            if (this == VoicesOfTheMines.TERMINAL_FIND.get()) net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.TerminalFindScreen(pos));
            else if (this == VoicesOfTheMines.TERMINAL_CALIBRATE.get()) net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.TerminalCalibrateScreen(pos));
            else if (this == VoicesOfTheMines.TABLE.get()) net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.ComputerScreen(pos));
            else if (this == VoicesOfTheMines.TERMINAL_PROCESSING.get()) {
                if (be instanceof VotvTerminalBlockEntity terminal && terminal.hasDrive()) net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.TerminalProcessingScreen(pos));
            }
            else if (this == VoicesOfTheMines.TERMINAL_CHECK.get()) {
                if (be instanceof VotvTerminalBlockEntity terminal && terminal.hasDrive()) net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.TerminalCheckScreen(pos));
            }
        }

        if (this == VoicesOfTheMines.TERMINAL_FIND.get() || this == VoicesOfTheMines.TERMINAL_CALIBRATE.get()) {
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            float damage = net.votmdevs.voicesofthemines.config.VotmConfig.getTerminalPunchDamage();
            if (damage > 0) {
                if (this == VoicesOfTheMines.TABLE.get()) {
                    player.hurt(level.damageSources().generic(), damage);
                    level.playSound(null, pos, VotmSounds.ROAR_PC.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.2F);
                }
                if (this == VoicesOfTheMines.TERMINAL_CHECK.get() || this == VoicesOfTheMines.TERMINAL_FIND.get() || this == VoicesOfTheMines.TERMINAL_CALIBRATE.get() || this == VoicesOfTheMines.TERMINAL_PROCESSING.get()) {
                    player.hurt(level.damageSources().generic(), damage);
                    level.playSound(null, pos, VotmSounds.ROAR.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.2F);
                }
            }
        }
        super.attack(state, level, pos, player);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && isWideTerminal(this)) {
            Direction facing = state.getValue(FACING);
            if (level.getBlockState(pos.relative(facing)).canBeReplaced()) level.setBlock(pos.relative(facing), VoicesOfTheMines.PHANTOM_BLOCK.get().defaultBlockState(), 3);
            if (level.getBlockState(pos.relative(facing,2)).canBeReplaced()) level.setBlock(pos.relative(facing,2), VoicesOfTheMines.PHANTOM_BLOCK.get().defaultBlockState(), 3);
            if (level.getBlockState(pos.relative(facing.getOpposite())).canBeReplaced()) level.setBlock(pos.relative(facing.getOpposite()), VoicesOfTheMines.PHANTOM_BLOCK.get().defaultBlockState(), 3);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isWideTerminal(this)) {
            Direction facing = state.getValue(FACING);
            if (level.getBlockState(pos.relative(facing)).is(VoicesOfTheMines.PHANTOM_BLOCK.get())) level.removeBlock(pos.relative(facing), false);
            if (level.getBlockState(pos.relative(facing,2)).is(VoicesOfTheMines.PHANTOM_BLOCK.get())) level.removeBlock(pos.relative(facing,2), false);
            if (level.getBlockState(pos.relative(facing.getOpposite())).is(VoicesOfTheMines.PHANTOM_BLOCK.get())) level.removeBlock(pos.relative(facing.getOpposite()), false);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private boolean isWideTerminal(Block block) { return block == VoicesOfTheMines.TERMINAL_CALIBRATE.get() || block == VoicesOfTheMines.TERMINAL_PROCESSING.get(); }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> shapeEast;
            case SOUTH -> shapeSouth;
            case WEST -> shapeWest;
            default -> shapeNorth;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (isWideTerminal(this)) {
            Direction facing = context.getHorizontalDirection().getOpposite();
            if (!context.getLevel().getBlockState(context.getClickedPos().relative(facing)).canBeReplaced() || !context.getLevel().getBlockState(context.getClickedPos().relative(facing.getOpposite())).canBeReplaced()) return null;
        }
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new VotvTerminalBlockEntity(pos, state); }
}