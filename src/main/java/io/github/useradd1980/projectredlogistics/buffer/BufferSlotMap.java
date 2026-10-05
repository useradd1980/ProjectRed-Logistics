package io.github.useradd1980.projectredlogistics.buffer;

public final class BufferSlotMap {

    public static final int INVENTORY_SIZE = 20;
    public static final int COLUMN_SIZE = 4;

    private BufferSlotMap() { }

    public static int slotCount(int frontSide, int accessSide) {
        validateSide(frontSide);
        validateSide(accessSide);
        return frontSide == accessSide ? INVENTORY_SIZE : COLUMN_SIZE;
    }

    public static int mapSlot(int frontSide, int accessSide, int localSlot) {
        validateSide(frontSide);
        validateSide(accessSide);

        int count = slotCount(frontSide, accessSide);
        if (localSlot < 0 || localSlot >= count) {
            throw new IndexOutOfBoundsException(
                    "Buffer local slot " + localSlot + " outside 0.." + (count - 1));
        }

        if (frontSide == accessSide) {
            return localSlot;
        }

        int column = Math.floorMod(5 + accessSide - frontSide, 6);
        return column * COLUMN_SIZE + localSlot;
    }

    private static void validateSide(int side) {
        if (side < 0 || side > 5) {
            throw new IllegalArgumentException(
                    "Minecraft side ordinal must be 0..5: " + side);
        }
    }
}
