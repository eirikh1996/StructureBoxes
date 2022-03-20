package io.github.eirikh1996.structureboxes.utils;

public final class StructureBlock {
    private final String type;
    private final byte data;

    public StructureBlock(String type, byte data) {
        this.type = type;
        this.data = data;
    }

    public StructureBlock(String type) {
        this.type = type;
        data = 0;
    }

    public String getType() {
        return type;
    }

    public byte getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StructureBlock)) return false;
        StructureBlock that = (StructureBlock) o;
        return getData() == that.getData() &&
                getType().equals(that.getType());
    }

    @Override
    public int hashCode() {
        return getType().hashCode() + getData();
    }
}
