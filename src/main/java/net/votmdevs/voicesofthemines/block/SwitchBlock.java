package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.votmdevs.voicesofthemines.VotmSounds;
import org.jetbrains.annotations.Nullable;

public class SwitchBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    // HITBOX
    protected static final VoxelShape UP_AABB = Block.box(6.0D, 14.0D, 6.0D, 10.0D, 16.0D, 10.0D); // Был 0-2, стал 14-16
    protected static final VoxelShape DOWN_AABB = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 2.0D, 10.0D); // Был 14-16, стал 0-2
    protected static final VoxelShape NORTH_AABB = Block.box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 2.0D); // Сдвинут назад по оси Z
    protected static final VoxelShape SOUTH_AABB = Block.box(6.0D, 6.0D, 14.0D, 10.0D, 10.0D, 16.0D); // Сдвинут вперед по оси Z
    protected static final VoxelShape WEST_AABB = Block.box(0.0D, 6.0D, 6.0D, 2.0D, 10.0D, 10.0D); // Сдвинут назад по оси X
    protected static final VoxelShape EAST_AABB = Block.box(14.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D); // Сдвинут вперед по оси X

    public SwitchBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        switch (dir) {
            case UP: return UP_AABB;
            case DOWN: return DOWN_AABB;
            case SOUTH: return SOUTH_AABB;
            case WEST: return WEST_AABB;
            case EAST: return EAST_AABB;
            case NORTH:
            default: return NORTH_AABB;
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SwitchBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        // linking
        if (stack.getItem() == Items.REDSTONE) {
            if (!level.isClientSide) {
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("SelectedLamp")) {
                    BlockPos lampPos = BlockPos.of(tag.getLong("SelectedLamp"));

                    if (lampPos.distSqr(pos) <= 400) {
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof SwitchBlockEntity switchEnt) {
                            if (switchEnt.linkedLamps.size() < 4) {
                                if (!switchEnt.linkedLamps.contains(lampPos)) {
                                    switchEnt.linkedLamps.add(lampPos);
                                    switchEnt.setChanged();

                                    player.displayClientMessage(Component.literal("§aLamp linked! (" + switchEnt.linkedLamps.size() + "/4)"), true);

                                    level.playSound(null, pos, VotmSounds.CONNECT.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

                                    tag.remove("SelectedLamp");
                                    ((ServerLevel) level).sendParticles(DustParticleOptions.REDSTONE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, 0.2, 0.2, 0.2, 0.0);
                                } else {
                                    player.displayClientMessage(Component.literal("§cLamp is already linked to this switch."), true);
                                }
                            } else {
                                player.displayClientMessage(Component.literal("§cThis switch is full (Max 4 lamps)."), true);
                            }
                        }
                    } else {
                        player.displayClientMessage(Component.literal("§cLamp is too far! (Max 20 blocks)."), true);
                    }
                } else {
                    player.displayClientMessage(Component.literal("§cSelect a lamp with redstone dust first!"), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // off / on
        if (!level.isClientSide) {
            boolean isPowered = state.getValue(POWERED);
            boolean newState = !isPowered;

            level.setBlock(pos, state.setValue(POWERED, newState), 3);

            level.playSound(null, pos, VotmSounds.LIGHTSWITCH.get(), SoundSource.BLOCKS, 1.0f, newState ? 1.1f : 0.9f);

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SwitchBlockEntity switchEnt) {
                for (BlockPos lampPos : switchEnt.linkedLamps) {
                    BlockState lampState = level.getBlockState(lampPos);
                    if (lampState.getBlock() instanceof UpLampBlock) {
                        level.setBlock(lampPos, lampState.setValue(UpLampBlock.LIT, newState), 3);
                    }
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}