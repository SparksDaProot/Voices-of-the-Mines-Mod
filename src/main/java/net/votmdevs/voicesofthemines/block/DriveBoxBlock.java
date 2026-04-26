package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.DriveEntity;
import org.jetbrains.annotations.Nullable;

public class DriveBoxBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    protected static final net.minecraft.world.phys.shapes.VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 9.0D, 14.0D);

    public DriveBoxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DriveBoxBlockEntity box) {
            ItemStack heldItem = player.getItemInHand(hand);

            // interact
            if (player.isShiftKeyDown() && heldItem.isEmpty()) {
                if (box.isOpen) {
                    for (int i = 5; i >= 0; i--) {
                        ItemStack diskStack = box.inventory.getStackInSlot(i);
                        if (!diskStack.isEmpty()) {
                            DriveEntity drive = VoicesOfTheMines.DRIVE.get().create(level);
                            if (drive != null) {
                                drive.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, 0);

                                CompoundTag diskTag = diskStack.getTag();
                                if (diskTag != null) {
                                    drive.getEntityData().set(DriveEntity.SIGNAL_ID, diskTag.getString("SignalId"));
                                    drive.getEntityData().set(DriveEntity.SIGNAL_TYPE, diskTag.getString("SignalType"));
                                    drive.getEntityData().set(DriveEntity.SIGNAL_LEVEL, diskTag.getInt("SignalLevel"));
                                }

                                // drop drive
                                net.minecraft.world.phys.Vec3 throwVec = player.getEyePosition().subtract(drive.position()).normalize().scale(0.5D);
                                drive.setDeltaMovement(throwVec.x, 0.3D, throwVec.z);
                                level.addFreshEntity(drive);

                                box.inventory.extractItem(i, 1, false);
                                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.0F);
                            }
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }

            // Open/Close
            if (!player.isShiftKeyDown()) {
                box.triggerOpenClose(!box.isOpen);
                level.playSound(null, pos, box.isOpen ? net.votmdevs.voicesofthemines.VotmSounds.DRIVEBOX_OPEN.get() : net.votmdevs.voicesofthemines.VotmSounds.DRIVEBOX_CLOSE.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 0.5F);
            }
        }
        return InteractionResult.SUCCESS;
    }

    // save NBT
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DriveBoxBlockEntity box) {
                ItemStack stack = new ItemStack(VoicesOfTheMines.DRIVE_BOX_ITEM.get());
                CompoundTag nbt = new CompoundTag();
                nbt.put("Inventory", box.inventory.serializeNBT());
                stack.setTag(nbt);

                ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    // NBT
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Inventory")) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DriveBoxBlockEntity box) {
                box.inventory.deserializeNBT(stack.getTag().getCompound("Inventory"));
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    // remove vanilla drop cuz it works bad with NBT inventory
    @Override
    public java.util.List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        return java.util.Collections.emptyList();
    }

    // creative copy with all signals
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DriveBoxBlockEntity box) {
            CompoundTag nbt = new CompoundTag();
            nbt.put("Inventory", box.inventory.serializeNBT());
            stack.setTag(nbt);
        }
        return stack;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DriveBoxBlockEntity(pos, state); }
}