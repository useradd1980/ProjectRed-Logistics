package io.github.useradd1980.projectredlogistics.block;

import io.github.useradd1980.projectredlogistics.block.entity.ItemDetectorBlockEntity;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import mrtjp.projectred.core.block.ProjectRedBlock;
import mrtjp.projectred.expansion.block.BaseDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ItemDetectorBlock extends BaseDeviceBlock {

    public ItemDetectorBlock() {
        super(ProjectRedBlock.STONE_MACHINE_PROPERTIES);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(ProjectRedBlock.SIDE,
                        context.getNearestLookingDirection().ordinal())
                .setValue(ProjectRedBlock.ACTIVE, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemDetectorBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<?> getBlockEntityType() {
        return LogisticsContent.ITEM_DETECTOR_BLOCK_ENTITY.get();
    }
}
