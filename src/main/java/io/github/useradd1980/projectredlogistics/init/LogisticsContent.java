package io.github.useradd1980.projectredlogistics.init;

import io.github.useradd1980.projectredlogistics.ProjectRedLogistics;
import io.github.useradd1980.projectredlogistics.block.FilterBlock;
import io.github.useradd1980.projectredlogistics.block.entity.FilterBlockEntity;
import io.github.useradd1980.projectredlogistics.menu.FilterMenu;
import codechicken.lib.inventory.container.CCLMenuType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class LogisticsContent {

    public static final String ID_FILTER = "filter";

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, ProjectRedLogistics.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, ProjectRedLogistics.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ProjectRedLogistics.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, ProjectRedLogistics.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ProjectRedLogistics.MOD_ID);

    public static final Supplier<Block> FILTER_BLOCK =
            BLOCKS.register(ID_FILTER, FilterBlock::new);

    public static final Supplier<Item> FILTER_ITEM =
            ITEMS.register(ID_FILTER,
                    () -> new BlockItem(FILTER_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<FilterBlockEntity>> FILTER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    ID_FILTER,
                    () -> BlockEntityType.Builder
                            .of(FilterBlockEntity::new, FILTER_BLOCK.get())
                            .build(null));

    public static final Supplier<MenuType<FilterMenu>> FILTER_MENU =
            MENU_TYPES.register(ID_FILTER, () -> CCLMenuType.create(FilterMenu.FACTORY));

    public static final Supplier<CreativeModeTab> LOGISTICS_TAB =
            CREATIVE_TABS.register(
                    "logistics",
                    () -> CreativeModeTab.builder()
                            .icon(() -> new ItemStack(FILTER_ITEM.get()))
                            .title(Component.translatable(
                                    "itemGroup." + ProjectRedLogistics.MOD_ID))
                            .displayItems((parameters, output) ->
                                    output.accept(FILTER_ITEM.get()))
                            .build());

    private LogisticsContent() { }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                FILTER_BLOCK_ENTITY.get(),
                (tile, side) -> tile.getFilterItemHandler());
    }
}
