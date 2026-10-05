package io.github.useradd1980.projectredlogistics.buffer;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BufferSlotMapTest {

    @Test
    void frontFaceExposesAllTwentySlots() {
        for (int front = 0; front < 6; front++) {
            assertEquals(20, BufferSlotMap.slotCount(front, front));

            for (int slot = 0; slot < 20; slot++) {
                assertEquals(slot, BufferSlotMap.mapSlot(front, front, slot));
            }
        }
    }

    @Test
    void fiveOtherFacesPartitionInventoryIntoFourSlotColumns() {
        for (int front = 0; front < 6; front++) {
            Set<Integer> mapped = new HashSet<>();

            for (int side = 0; side < 6; side++) {
                if (side == front) continue;

                assertEquals(4, BufferSlotMap.slotCount(front, side));
                int first = BufferSlotMap.mapSlot(front, side, 0);

                for (int local = 0; local < 4; local++) {
                    int actual = BufferSlotMap.mapSlot(front, side, local);
                    assertEquals(first + local, actual);
                    assertTrue(mapped.add(actual));
                }
            }

            assertEquals(20, mapped.size());

            for (int slot = 0; slot < 20; slot++) {
                assertTrue(mapped.contains(slot));
            }
        }
    }

    @Test
    void invalidSidesAndLocalSlotsAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> BufferSlotMap.slotCount(-1, 0));
        assertThrows(
                IllegalArgumentException.class,
                () -> BufferSlotMap.slotCount(0, 6));
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> BufferSlotMap.mapSlot(0, 1, 4));
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> BufferSlotMap.mapSlot(0, 0, 20));
    }
}
