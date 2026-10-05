package io.github.useradd1980.projectredlogistics.block;

import io.github.useradd1980.projectredlogistics.block.entity.BufferBlockEntity;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import mrtjp.projectred.core.block.ProjectRedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

public class BufferBlock extends ProjectRedBlock {

    public BufferBlock() {
        super(ProjectRedBlock.STONE_MACHINE_PROPERTIES);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(
                ProjectRedBlock.SIDE,
                context.getNearestLookingDirection().ordinal());
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder) {

        super.createBlockStateDefinition(builder);
        builder.add(ProjectRedBlock.SIDE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BufferBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<?> getBlockEntityType() {
        return LogisticsContent.BUFFER_BLOCK_ENTITY.get();
    }
}
