package io.github.useradd1980.projectredlogistics.init;

import codechicken.lib.inventory.container.CCLMenuType;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.SimpleMultipartType;
import io.github.useradd1980.projectredlogistics.ProjectRedLogistics;
import io.github.useradd1980.projectredlogistics.block.BufferBlock;
import io.github.useradd1980.projectredlogistics.block.FilterBlock;
import io.github.useradd1980.projectredlogistics.block.ManagerBlock;
import io.github.useradd1980.projectredlogistics.block.SortingMachineBlock;
import io.github.useradd1980.projectredlogistics.block.entity.BufferBlockEntity;
import io.github.useradd1980.projectredlogistics.block.entity.FilterBlockEntity;
import io.github.useradd1980.projectredlogistics.block.entity.ManagerBlockEntity;
import io.github.useradd1980.projectredlogistics.block.entity.SortingMachineBlockEntity;
import io.github.useradd1980.projectredlogistics.menu.BufferMenu;
import io.github.useradd1980.projectredlogistics.menu.FilterMenu;
import io.github.useradd1980.projectredlogistics.menu.ManagerMenu;
import io.github.useradd1980.projectredlogistics.menu.SortingMachineMenu;
import io.github.useradd1980.projectredlogistics.tube.RestrictionTubeItem;
import io.github.useradd1980.projectredlogistics.tube.RestrictionTubePart;
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

    public static final String ID_BUFFER = "buffer";
    public static final String ID_FILTER = "filter";
    public static final String ID_SORTING_MACHINE = "sorting_machine";
    public static final String ID_MANAGER = "manager";
    public static final String ID_RESTRICTION_TUBE = "restriction_tube";

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, ProjectRedLogistics.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, ProjectRedLogistics.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ProjectRedLogistics.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, ProjectRedLogistics.MOD_ID);
    public static final DeferredRegister<MultipartType<?>> PART_TYPES =
            DeferredRegister.create(MultipartType.MULTIPART_TYPES, ProjectRedLogistics.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ProjectRedLogistics.MOD_ID);

    public static final Supplier<Block> BUFFER_BLOCK =
            BLOCKS.register(ID_BUFFER, BufferBlock::new);

    public static final Supplier<Item> BUFFER_ITEM =
            ITEMS.register(ID_BUFFER,
                    () -> new BlockItem(BUFFER_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Block> FILTER_BLOCK =
            BLOCKS.register(ID_FILTER, FilterBlock::new);

    public static final Supplier<Item> FILTER_ITEM =
            ITEMS.register(ID_FILTER,
                    () -> new BlockItem(FILTER_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Block> SORTING_MACHINE_BLOCK =
            BLOCKS.register(ID_SORTING_MACHINE, SortingMachineBlock::new);

    public static final Supplier<Item> SORTING_MACHINE_ITEM =
            ITEMS.register(ID_SORTING_MACHINE,
                    () -> new BlockItem(SORTING_MACHINE_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Block> MANAGER_BLOCK =
            BLOCKS.register(ID_MANAGER, ManagerBlock::new);

    public static final Supplier<Item> MANAGER_ITEM =
            ITEMS.register(ID_MANAGER,
                    () -> new BlockItem(MANAGER_BLOCK.get(), new Item.Properties()));

    public static final Supplier<MultipartType<RestrictionTubePart>> RESTRICTION_TUBE_PART =
            PART_TYPES.register(
                    ID_RESTRICTION_TUBE,
                    () -> new SimpleMultipartType<>(isClient -> new RestrictionTubePart()));

    public static final Supplier<Item> RESTRICTION_TUBE_ITEM =
            ITEMS.register(ID_RESTRICTION_TUBE, RestrictionTubeItem::new);

    public static final Supplier<BlockEntityType<BufferBlockEntity>> BUFFER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    ID_BUFFER,
                    () -> BlockEntityType.Builder
                            .of(BufferBlockEntity::new, BUFFER_BLOCK.get())
                            .build(null));

    public static final Supplier<BlockEntityType<FilterBlockEntity>> FILTER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    ID_FILTER,
                    () -> BlockEntityType.Builder
                            .of(FilterBlockEntity::new, FILTER_BLOCK.get())
                            .build(null));

    public static final Supplier<BlockEntityType<SortingMachineBlockEntity>> SORTING_MACHINE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    ID_SORTING_MACHINE,
                    () -> BlockEntityType.Builder
                            .of(SortingMachineBlockEntity::new, SORTING_MACHINE_BLOCK.get())
                            .build(null));

    public static final Supplier<BlockEntityType<ManagerBlockEntity>> MANAGER_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    ID_MANAGER,
                    () -> BlockEntityType.Builder
                            .of(ManagerBlockEntity::new, MANAGER_BLOCK.get())
                            .build(null));

    public static final Supplier<MenuType<BufferMenu>> BUFFER_MENU =
            MENU_TYPES.register(ID_BUFFER, () -> CCLMenuType.create(BufferMenu.FACTORY));

    public static final Supplier<MenuType<FilterMenu>> FILTER_MENU =
            MENU_TYPES.register(ID_FILTER, () -> CCLMenuType.create(FilterMenu.FACTORY));

    public static final Supplier<MenuType<SortingMachineMenu>> SORTING_MACHINE_MENU =
            MENU_TYPES.register(
                    ID_SORTING_MACHINE,
                    () -> CCLMenuType.create(SortingMachineMenu.FACTORY));

    public static final Supplier<MenuType<ManagerMenu>> MANAGER_MENU =
            MENU_TYPES.register(
                    ID_MANAGER,
                    () -> CCLMenuType.create(ManagerMenu.FACTORY));

    public static final Supplier<CreativeModeTab> LOGISTICS_TAB =
            CREATIVE_TABS.register(
                    "logistics",
                    () -> CreativeModeTab.builder()
                            .icon(() -> new ItemStack(FILTER_ITEM.get()))
                            .title(Component.translatable(
                                    "itemGroup." + ProjectRedLogistics.MOD_ID))
                            .displayItems((parameters, output) -> {
                                output.accept(BUFFER_ITEM.get());
                                output.accept(FILTER_ITEM.get());
                                output.accept(SORTING_MACHINE_ITEM.get());
                                output.accept(MANAGER_ITEM.get());
                                output.accept(RESTRICTION_TUBE_ITEM.get());
                            })
                            .build());

    private LogisticsContent() { }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        PART_TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BUFFER_BLOCK_ENTITY.get(),
                (tile, side) -> tile.getItemHandler(side));

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                FILTER_BLOCK_ENTITY.get(),
                (tile, side) -> tile.getFilterItemHandler());
    }
}
