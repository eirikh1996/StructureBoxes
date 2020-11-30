package io.github.eirikh1996.structureboxes;

import io.github.eirikh1996.structureboxes.utils.IncrementalPlacementTask;
import io.github.eirikh1996.structureboxes.utils.Location;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Structure implements Iterable<Location> {
    private final UUID id;
    private int minX, minY, minZ, maxX, maxY, maxZ, expiry = -1;
    private long placementTime;
    private final String schematicName;
    private final Map<Location, Object> originalBlocks;
    private final UUID owner;
    private final AtomicBoolean processing;
    private LinkedList<Location> locationsToRemove = new LinkedList<>();
    private IncrementalPlacementTask incrementalPlacementTask;

    public Structure(String schematicName, Map<Location, Object> originalBlocks, UUID owner) {
        this.schematicName = schematicName;
        this.originalBlocks = originalBlocks;
        this.owner = owner;
        placementTime = -1;
        id = UUID.randomUUID();
        for (Location loc : originalBlocks.keySet()) {
            if (minX > loc.getX()) {
                minX = loc.getX();
            }
            if (minY > loc.getY()) {
                minY = loc.getY();
            }
            if (minZ > loc.getZ()) {
                minZ = loc.getZ();
            }
            if (maxX < loc.getX()) {
                maxX = loc.getX();
            }
            if (maxY < loc.getY()) {
                maxY = loc.getY();
            }
            if (maxZ < loc.getZ()) {
                maxZ = loc.getZ();
            }
        }
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

    public void setPlacementTime(long placementTime) {
        this.placementTime = placementTime;
    }

    public Set<Location> getStructure() {
        return originalBlocks.keySet();
    }

    public boolean contains(Location location) {
        return originalBlocks.containsKey(location);
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

    public void setProcessingLater(final boolean processing, long delay) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setProcessing(processing);
            }
        }, delay);
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

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @NotNull
    @Override
    public Iterator<Location> iterator() {
        return originalBlocks.keySet().iterator();
    }

    public LinkedList<Location> getLocationsToRemove() {
        return locationsToRemove;
    }

    public void setLocationsToRemove(LinkedList<Location> locationsToRemove) {
        this.locationsToRemove = locationsToRemove;
    }

    public IncrementalPlacementTask getIncrementalPlacementTask() {
        return incrementalPlacementTask;
    }

    public void setIncrementalPlacementTask(IncrementalPlacementTask incrementalPlacementTask) {
        this.incrementalPlacementTask = incrementalPlacementTask;
    }

    public int getExpiry() {
        return expiry;
    }

    public void setExpiry(int expiry) {
        this.expiry = expiry;
    }
}
