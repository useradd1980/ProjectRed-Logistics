package io.github.useradd1980.projectredlogistics.block;

import io.github.useradd1980.projectredlogistics.block.entity.FilterBlockEntity;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import mrtjp.projectred.core.block.ProjectRedBlock;
import mrtjp.projectred.expansion.block.BaseDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FilterBlock extends BaseDeviceBlock {

    public FilterBlock() {
        super(ProjectRedBlock.STONE_MACHINE_PROPERTIES);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FilterBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<?> getBlockEntityType() {
        return LogisticsContent.FILTER_BLOCK_ENTITY.get();
    }
}
