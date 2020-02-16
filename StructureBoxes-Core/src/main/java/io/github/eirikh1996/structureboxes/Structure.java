package io.github.eirikh1996.structureboxes;

import io.github.eirikh1996.structureboxes.utils.Location;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class Structure {
    private final UUID id;
    private final long placementTime;
    private final String schematicName;
    private final Map<Location, Object> originalBlocks;
    private final UUID owner;
    private final AtomicBoolean processing;

    public Structure(String schematicName, Map<Location, Object> originalBlocks, UUID owner) {
        this.schematicName = schematicName;
        this.originalBlocks = originalBlocks;
        this.owner = owner;
        placementTime = System.currentTimeMillis();
        id = UUID.randomUUID();
        processing = new AtomicBoolean(true);
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public long getPlacementTime() {
        return placementTime;
    }

    public Set<Location> getStructure() {
        return originalBlocks.keySet();
    }

    public Map<Location, Object> getOriginalBlocks() {
        return originalBlocks;
    }

    public String getSchematicName() {
        return schematicName;
    }

    public boolean isProcessing() {
        return processing.get();
    }

    public void setProcessing(boolean processing) {
        this.processing.set(processing);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Structure)) return false;
        Structure structure1 = (Structure) o;
        return getPlacementTime() == structure1.getPlacementTime() &&
                getId().equals(structure1.getId()) &&
                getStructure().equals(structure1.getStructure()) &&
                getOwner().equals(structure1.getOwner());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
