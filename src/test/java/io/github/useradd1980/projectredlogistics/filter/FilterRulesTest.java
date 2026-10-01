package io.github.useradd1980.projectredlogistics.filter;

import io.github.useradd1980.projectredlogistics.block.entity.FilterBlockEntity;
import io.github.useradd1980.projectredlogistics.routing.LogisticsRoutingData;
import mrtjp.projectred.api.pneumatics.PneumaticPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FilterRulesTest {

    @Test
    void incomingMatchIgnoresConfiguredStackQuantity() {
        ItemStack template = new ItemStack(Items.COBBLESTONE, 64);
        ItemStack incoming = new ItemStack(Items.COBBLESTONE, 1);

        assertTrue(FilterRules.matches(template, incoming));
        assertFalse(FilterRules.matches(
                template,
                new ItemStack(Items.DIRT, 64)));
    }

    @Test
    void selectedColourReplacesExistingPayloadColour() {
        FakePayload payload = new FakePayload(
                new ItemStack(Items.COBBLESTONE));

        LogisticsRoutingData.setPayloadColour(payload, 11);
        FilterRules.applyOutputColour(payload, 5);

        assertEquals(
                5,
                LogisticsRoutingData.getPayloadColour(payload).orElseThrow());
    }

    @Test
    void uncolouredFilterClearsExistingPayloadColour() {
        FakePayload payload = new FakePayload(
                new ItemStack(Items.COBBLESTONE));

        LogisticsRoutingData.setPayloadColour(payload, 5);
        FilterRules.applyOutputColour(
                payload,
                FilterBlockEntity.NO_COLOUR);

        assertTrue(LogisticsRoutingData.getPayloadColour(payload).isEmpty());
    }

    private static final class FakePayload implements PneumaticPayload {

        private final ItemStack stack;
        private final Map<ResourceLocation, CompoundTag> data =
                new HashMap<>();

        private FakePayload(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public ItemStack getItemStack() {
            return stack;
        }

        @Override
        public boolean hasData(ResourceLocation key) {
            return data.containsKey(key);
        }

        @Override
        public CompoundTag getData(ResourceLocation key) {
            CompoundTag value = data.get(key);
            return value == null ? null : value.copy();
        }

        @Override
        public void setData(ResourceLocation key, CompoundTag value) {
            data.put(key, value.copy());
        }

        @Override
        public void removeData(ResourceLocation key) {
            data.remove(key);
        }
    }
}
