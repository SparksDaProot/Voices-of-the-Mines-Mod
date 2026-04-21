package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import org.jetbrains.annotations.Nullable;

public class ServerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<ServerType> TYPE = EnumProperty.create("type", ServerType.class);
    public static final BooleanProperty BROKEN = BooleanProperty.create("broken");

    public ServerBlock(Properties properties) {
        super(properties.randomTicks());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, ServerType.BASE)
                .setValue(BROKEN, false));
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && !state.getValue(BROKEN)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ServerBlockEntity serverBe) {
                serverBe.hitCount++;

                if (serverBe.hitCount >= 3) {
                    serverBe.hitCount = 0;
                    if (serverBe.diskUsesLeft > 0) {
                        serverBe.diskUsesLeft--;
                        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.SHIELD_BLOCK, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                    } else {
                        level.setBlock(pos, state.setValue(BROKEN, true), 3);
                        level.playSound(null, pos, VotmSounds.SERVERDOWN.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                } else {
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.IRON_GOLEM_DAMAGE, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.0F);
                }
                serverBe.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
        super.attack(state, level, pos, player);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(BROKEN);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(BROKEN)) {
            if (random.nextInt(100) < 12) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ServerBlockEntity serverBe) {
                    if (serverBe.diskUsesLeft > 0) {
                        serverBe.diskUsesLeft--;
                        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.SHIELD_BLOCK, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                        serverBe.setChanged();
                    } else {
                        level.setBlock(pos, state.setValue(BROKEN, true), 3);
                        level.playSound(null, pos, VotmSounds.SERVERDOWN.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(BROKEN)) {
            if (random.nextInt(20) == 0) {
                level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        VotmSounds.SERVOMOTOR_LOOP.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.3F, 1.0F, false);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        boolean isBroken = state.getValue(BROKEN);
        ItemStack itemInHand = player.getItemInHand(hand);

        if (itemInHand.getItem() == VoicesOfTheMines.DISK_BLUE.get()) {
            if (!isBroken) {
                if (!level.isClientSide()) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof ServerBlockEntity serverBe) {
                        if (serverBe.diskUsesLeft < 4) {
                            serverBe.diskUsesLeft++;
                            itemInHand.shrink(1);
                            serverBe.triggerAnim("controller", "insert");
                            level.playSound(null, pos, VotmSounds.DRIVE_IN.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                            serverBe.setChanged();
                            level.sendBlockUpdated(pos, state, state, 3);
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        else if (itemInHand.isEmpty() && isBroken) {
            if (level.isClientSide()) {
                net.minecraft.client.Minecraft.getInstance().setScreen(new net.votmdevs.voicesofthemines.client.gui.ServerMinigameScreen(pos));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, BROKEN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ServerBlockEntity(pos, state); }
}