package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WetPuddleBlock extends BaseEntityBlock {
    public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 0, 4);
    // Хитбокс без коллизии (только для выделения и срабатывания entityInside)
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public WetPuddleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, 1));
    }

    // ВОТ ОНА - МАГИЯ СКОЛЬЖЕНИЯ БЕЗ КОЛЛИЗИИ!
    @SuppressWarnings("deprecation")
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
        super.entityInside(state, level, pos, entity);

        // Если это игрок или моб, и он стоит на земле
        if (entity.onGround() && entity instanceof net.minecraft.world.entity.LivingEntity) {
            net.minecraft.world.phys.Vec3 motion = entity.getDeltaMovement();

            // Если он двигается (но не летит со скоростью света)
            if (motion.horizontalDistanceSqr() > 0.0001 && motion.horizontalDistanceSqr() < 0.25) {
                // Коэффициент 1.6D идеально противодействует трению обычных блоков,
                // превращая пол под лужей в каток (даже чуть более скользкий, чем лед!)
                entity.setDeltaMovement(motion.x * 1.6D, motion.y, motion.z * 1.6D);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(DISTANCE); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new WetPuddleBlockEntity(pos, state); }
}