package io.github.eirikh1996.structureboxes.processing;

import com.sk89q.worldedit.util.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface BukkitRegionPredicate extends RegionPredicate<Player> {

    @Override
    @NotNull Result validate(@NotNull Player player, @NotNull Location location);
}
