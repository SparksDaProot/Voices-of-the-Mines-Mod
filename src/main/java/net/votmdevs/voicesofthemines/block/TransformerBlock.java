package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import org.jetbrains.annotations.Nullable;

public class TransformerBlock extends BaseEntityBlock {
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public TransformerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    private void sendNotif(Player player, String msg) {
        if (player instanceof ServerPlayer sp) {
            KerfurPacketHandler.INSTANCE.sendTo(new KerfurPacketHandler.NotificationPacket(msg), sp.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TransformerBlockEntity transformer)) return InteractionResult.PASS;

        // link
        if (stack.getItem() == Items.REDSTONE) {
            if (!level.isClientSide) {
                CompoundTag tag = stack.getOrCreateTag();

                if (tag.contains("SelectedMainTransformer")) {
                    BlockPos mainPos = BlockPos.of(tag.getLong("SelectedMainTransformer"));

                    if (mainPos.equals(pos)) {
                        tag.remove("SelectedMainTransformer");
                        sendNotif(player, "Main Transformer deselected.");
                        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.2f);
                        return InteractionResult.SUCCESS;
                    }

                    BlockEntity mainBe = level.getBlockEntity(mainPos);
                    if (mainBe instanceof TransformerBlockEntity mainTransformer && mainTransformer.isMain) {

                        if (mainTransformer.secondaries.contains(pos) && transformer.mainTransformerPos != null && transformer.mainTransformerPos.equals(mainPos)) {
                            sendNotif(player, "Already linked to this network.");
                            return InteractionResult.SUCCESS;
                        }

                        if (!mainTransformer.secondaries.contains(pos)) {
                            mainTransformer.secondaries.add(pos);
                        }

                        transformer.mainTransformerPos = mainPos;
                        transformer.isNetworkActive = mainTransformer.isActive;
                        transformer.needsReboot = mainTransformer.needsReboot;
                        transformer.isReady = false;
                        transformer.energy = 100;

                        transformer.setChanged();
                        mainTransformer.setChanged();

                        level.sendBlockUpdated(pos, transformer.getBlockState(), transformer.getBlockState(), 3);
                        level.sendBlockUpdated(mainPos, mainTransformer.getBlockState(), mainTransformer.getBlockState(), 3);

                        sendNotif(player, "Secondary Transformer linked! Total: " + mainTransformer.secondaries.size());
                        level.playSound(null, pos, VotmSounds.CONNECT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                        ((ServerLevel) level).sendParticles(DustParticleOptions.REDSTONE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.0);

                        mainTransformer.checkNetworkStart();
                    } else {
                        tag.remove("SelectedMainTransformer");
                        sendNotif(player, "Previous Main Transformer lost. Select a new one.");
                    }
                } else {
                    tag.remove("SelectedLamp");
                    transformer.isMain = true;
                    transformer.energy = 100;
                    transformer.setChanged();
                    level.sendBlockUpdated(pos, transformer.getBlockState(), transformer.getBlockState(), 3);

                    tag.putLong("SelectedMainTransformer", pos.asLong());
                    sendNotif(player, "Main Transformer selected. Click other transformers or devices to link.");
                    level.playSound(null, pos, VotmSounds.CONNECT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.8f);
                    ((ServerLevel) level).sendParticles(DustParticleOptions.REDSTONE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.0);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // repair
        if (!level.isClientSide) {
            TransformerBlockEntity mainTr = transformer.isMain ? transformer : null;
            if (!transformer.isMain && transformer.mainTransformerPos != null) {
                BlockEntity mBe = level.getBlockEntity(transformer.mainTransformerPos);
                if (mBe instanceof TransformerBlockEntity m) mainTr = m;
            }

            if (mainTr != null) {
                if (mainTr.needsReboot) {
                    if (!transformer.isReady) {
                        transformer.isReady = true;
                        transformer.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);
                        sendNotif(player, "Transformer serviced. Service OTHERS to restore power!");
                        level.playSound(null, pos, VotmSounds.TURNON.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                        mainTr.tryRestartNetwork();
                    } else {
                        sendNotif(player, "This transformer is already serviced. Check the others.");
                        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.2F);
                    }
                }
                else if (transformer.energy < 100) {
                    transformer.energy = 100;
                    transformer.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                    sendNotif(player, "Transformer fully recharged.");
                    level.playSound(null, pos, VotmSounds.TURNON.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                } else {
                    sendNotif(player, "Transformer is at 100% and operating normally.");
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.2F);
                }
            } else {
                sendNotif(player, "Transformer is unlinked or Main is missing.");
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.2F);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TransformerBlockEntity transformer && !level.isClientSide()) {

                if (transformer.isMain) {
                    transformer.shutdownNetwork();
                    for (BlockPos secPos : transformer.secondaries) {
                        BlockEntity secBe = level.getBlockEntity(secPos);
                        if (secBe instanceof TransformerBlockEntity sec) {
                            sec.mainTransformerPos = null;
                            sec.needsReboot = false;
                            sec.isNetworkActive = false;
                            sec.isReady = false;
                            sec.setChanged();
                            level.sendBlockUpdated(secPos, sec.getBlockState(), sec.getBlockState(), 3);
                        }
                    }
                } else if (transformer.mainTransformerPos != null) {
                    BlockEntity mainBe = level.getBlockEntity(transformer.mainTransformerPos);
                    if (mainBe instanceof TransformerBlockEntity mainTransformer) {
                        mainTransformer.secondaries.remove(pos);

                        if (mainTransformer.secondaries.size() < 2) {
                            mainTransformer.shutdownNetwork();
                        }

                        mainTransformer.setChanged();
                        level.sendBlockUpdated(transformer.mainTransformerPos, mainTransformer.getBlockState(), mainTransformer.getBlockState(), 3);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) { return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TransformerBlockEntity(pos, state); }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, VoicesOfTheMines.TRANSFORMER_BE.get(), TransformerBlockEntity::tick);
    }
}