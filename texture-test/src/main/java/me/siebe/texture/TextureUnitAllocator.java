package me.siebe.texture;

public class TextureUnitAllocator {
    private int nextUnit = 0;
    private final int maxUnits;

    public TextureUnitAllocator(int maxUnits) {
        this.maxUnits = maxUnits;
    }

    public void reset() {
        nextUnit = 0;
    }

    public int allocate() {
        if (nextUnit >= maxUnits) {
            throw new RuntimeException("Cannot allocate more than " + maxUnits + " units");
        }
        return nextUnit++;
    }
}
