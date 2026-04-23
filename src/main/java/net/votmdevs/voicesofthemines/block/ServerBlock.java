package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
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

    private static final VoxelShape SHAPE = Block.box(
            0.0D, 0.0D, 0.0D,
            16.0D, 32.0D, 16.0D
    );

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
            if (random.nextInt(1000) < 12) {
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
            if (random.nextInt(160) == 0) {
                level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        VotmSounds.SERVOMOTOR_LOOP.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.3F, 1.0F, false);
            }

            if (random.nextInt(400) == 0) {
                int soundChoice = random.nextInt(8) + 1;
                net.minecraft.sounds.SoundEvent randomServerSound = switch (soundChoice) {
                    case 1 -> VotmSounds.SERVER1.get();
                    case 2 -> VotmSounds.SERVER2.get();
                    case 3 -> VotmSounds.SERVER3.get();
                    case 4 -> VotmSounds.SERVER4.get();
                    case 5 -> VotmSounds.SERVER5.get();
                    case 6 -> VotmSounds.SERVER6.get();
                    case 7 -> VotmSounds.SERVER7.get();
                    default -> VotmSounds.SERVER8.get();
                };

                level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        randomServerSound, net.minecraft.sounds.SoundSource.BLOCKS, 0.4F, 1.0F, false);
            }
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide()) {
            String typeName = state.getValue(TYPE).getSerializedName();
            typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
            net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get((ServerLevel) level);
            manager.placedServers.put(typeName, pos);
            manager.setDirty();
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            String typeName = state.getValue(TYPE).getSerializedName();
            typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
            net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get((ServerLevel) level);

            if (pos.equals(manager.placedServers.get(typeName))) {
                manager.placedServers.remove(typeName);
                manager.setDirty();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide() && state.getBlock() == oldState.getBlock() && state.getValue(TYPE) != oldState.getValue(TYPE)) {
            net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get((ServerLevel) level);

            String oldName = oldState.getValue(TYPE).getSerializedName();
            oldName = oldName.substring(0, 1).toUpperCase() + oldName.substring(1);

            if (pos.equals(manager.placedServers.get(oldName))) {
                manager.placedServers.remove(oldName);
            }

            String newName = state.getValue(TYPE).getSerializedName();
            newName = newName.substring(0, 1).toUpperCase() + newName.substring(1);
            manager.placedServers.put(newName, pos);

            manager.setDirty();
        }
        super.onPlace(state, level, pos, oldState, isMoving);
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

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}