package net.votmdevs.voicesofthemines.block;

import net.votmdevs.voicesofthemines.KerfurMod;
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

    // server
    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(serverPlayer.serverLevel());

            if (this == KerfurMod.TERMINAL_FIND.get()) {
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                        new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncSignalsPacket(manager.getUncaughtSignals()),
                        serverPlayer.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                        new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncProcessingStatePacket(manager.hasProcessingSignal()),
                        serverPlayer.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
            }
            else if (this == KerfurMod.TERMINAL_CALIBRATE.get()) {
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

            else if (this == KerfurMod.TABLE.get()) {
                net.votmdevs.voicesofthemines.world.PlayerData pd = manager.getGlobalPlayerData();

                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                        new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncComputerDataPacket(pd.getPoints(), pd.getCursorSpeedLvl(), pd.getPingCooldownLvl(), pd.getProcessingSpeedLvl(), pd.getProcessingLevelLvl()),
                        serverPlayer.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
            }

            else if (this == KerfurMod.TERMINAL_PROCESSING.get()) {
                net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity terminal = (net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity) level.getBlockEntity(pos);
                if (terminal != null && terminal.hasDrive()) {
                    String sigType = terminal.getDriveSignalType();
                    int sigLevel = terminal.getDriveSignalLevel();
                    net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                            new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncProcessingTargetPacket(true, sigType, sigLevel),
                            serverPlayer.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                    );
                }
            }

            else if (this == KerfurMod.TERMINAL_CHECK.get()) {
                net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity terminal = (net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity) level.getBlockEntity(pos);

                String sType = "";
                boolean hasSig = false;

                if (terminal != null && terminal.hasDrive()) {
                    String driveSigId = terminal.getDriveSignalId();
                    String driveSigType = terminal.getDriveSignalType();

                    if (driveSigId != null && !driveSigId.isEmpty() && driveSigType != null && !driveSigType.isEmpty()) {
                        hasSig = true;
                        sType = driveSigType;
                    } else {
                        net.votmdevs.voicesofthemines.world.SignalManager.VotvSignal sig = manager.getCalibratedSignal();
                        if (sig != null) {
                            hasSig = true;
                            sType = sig.type;
                            terminal.setDrive(true, sig.id, sig.type);
                        }
                    }
                    int sigLevel = terminal.getDriveSignalLevel();
                    net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                            new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncCheckTargetPacket(hasSig, sType, sigLevel),
                            serverPlayer.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                    );
                }
            }
        } else {
            // client
            if (this == KerfurMod.TERMINAL_FIND.get()) {
                net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.TerminalFindScreen(pos));
            } else if (this == KerfurMod.TERMINAL_CALIBRATE.get()) {
                net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.TerminalCalibrateScreen(pos));
            }
            else if (this == KerfurMod.TABLE.get()) {
                net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.ComputerScreen(pos));
            }
            else if (this == KerfurMod.TERMINAL_PROCESSING.get()) {
                net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity terminal = (net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity) level.getBlockEntity(pos);
                if (terminal != null && terminal.hasDrive()) {
                    net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.TerminalProcessingScreen(pos));
                }
            }
            else if (this == KerfurMod.TERMINAL_CHECK.get()) {
                net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity terminal = (net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity) level.getBlockEntity(pos);
                if (terminal != null && terminal.hasDrive()) {
                    net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.TerminalCheckScreen(pos));
                }
            }
        }

        if (this == KerfurMod.TERMINAL_FIND.get() || this == KerfurMod.TERMINAL_CALIBRATE.get()) {
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }
        return net.minecraft.world.InteractionResult.PASS;
    }


    // fake blocks for hitboxes
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && isWideTerminal(this)) {
            Direction facing = state.getValue(FACING);

            Direction side1 = facing;
            Direction side2 = facing.getOpposite();

            BlockPos pos1 = pos.relative(side1);
            BlockPos pos1_1 = pos.relative(side1,2);
            BlockPos pos2 = pos.relative(side2);

            if (level.getBlockState(pos1).canBeReplaced()) {
                level.setBlock(pos1, KerfurMod.PHANTOM_BLOCK.get().defaultBlockState(), 3);
            }
            if (level.getBlockState(pos1_1).canBeReplaced()) {
                level.setBlock(pos1_1, KerfurMod.PHANTOM_BLOCK.get().defaultBlockState(), 3);
            }
            if (level.getBlockState(pos2).canBeReplaced()) {
                level.setBlock(pos2, KerfurMod.PHANTOM_BLOCK.get().defaultBlockState(), 3);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isWideTerminal(this)) {
            Direction facing = state.getValue(FACING);

            Direction side1 = facing;
            Direction side2 = facing.getOpposite();

            BlockPos pos1 = pos.relative(side1);
            BlockPos pos1_1 = pos.relative(side1,2);
            BlockPos pos2 = pos.relative(side2);

            if (level.getBlockState(pos1).is(KerfurMod.PHANTOM_BLOCK.get())) {
                level.removeBlock(pos1, false);
            }
            if (level.getBlockState(pos1_1).is(KerfurMod.PHANTOM_BLOCK.get())) {
                level.removeBlock(pos1_1, false);
            }
            if (level.getBlockState(pos2).is(KerfurMod.PHANTOM_BLOCK.get())) {
                level.removeBlock(pos2, false);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private boolean isWideTerminal(Block block) {
        return block == KerfurMod.TERMINAL_CALIBRATE.get() ||
                block == KerfurMod.TERMINAL_PROCESSING.get();
    }


    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case EAST: return shapeEast;
            case SOUTH: return shapeSouth;
            case WEST: return shapeWest;
            case NORTH:
            default: return shapeNorth;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (isWideTerminal(this)) {
            Direction facing = context.getHorizontalDirection().getOpposite();

            Direction side1 = facing;
            Direction side2 = facing.getOpposite();

            BlockPos pos = context.getClickedPos();
            Level level = context.getLevel();

            if (!level.getBlockState(pos.relative(side1)).canBeReplaced() || !level.getBlockState(pos.relative(side2)).canBeReplaced()) {
                return null;
            }
        }
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VotvTerminalBlockEntity(pos, state);
    }
}