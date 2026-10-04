package io.github.useradd1980.projectredlogistics.block;

import io.github.useradd1980.projectredlogistics.block.entity.ManagerBlockEntity;
import io.github.useradd1980.projectredlogistics.init.LogisticsContent;
import mrtjp.projectred.core.block.ProjectRedBlock;
import mrtjp.projectred.expansion.block.BaseDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ManagerBlock extends BaseDeviceBlock {

    public ManagerBlock() {
        super(ProjectRedBlock.STONE_MACHINE_PROPERTIES);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int side = context.getNearestLookingDirection().ordinal();
        return defaultBlockState()
                .setValue(ProjectRedBlock.SIDE, side)
                .setValue(ProjectRedBlock.ACTIVE, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManagerBlockEntity(pos, state);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction direction) {

        if (!(level.getBlockEntity(pos)
                instanceof ManagerBlockEntity manager)
                || !manager.isStockSatisfied()) {
            return 0;
        }

        /*
         * RP2's Manager emitted weak power (level 1) from every face except
         * the rear inventory face. Minecraft's signal query direction points
         * from the receiving block toward this block, so a query matching the
         * Manager's front side corresponds to the Manager's rear face.
         */
        int frontSide = state.getValue(ProjectRedBlock.SIDE);
        return direction.ordinal() == frontSide ? 0 : 1;
    }

    @Override
    protected BlockEntityType<?> getBlockEntityType() {
        return LogisticsContent.MANAGER_BLOCK_ENTITY.get();
    }
}
