package io.github.eirikh1996.structureboxes.processing.validators;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.processing.BukkitRegionPredicate;
import io.github.eirikh1996.structureboxes.processing.Result;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorldGuardRegionValidator implements BukkitRegionPredicate {
    @Override
    public @NotNull Result validate(@NotNull Player player, @NotNull Location location) {
        if (!(location.getExtent() instanceof World)) {
            throw new IllegalArgumentException("Extent of supplied location must be of type World");
        }
        LocalPlayer lp = StructureBoxes.getInstance().getWorldGuardPlugin().wrapPlayer(player);
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get((World) location.getExtent());
        ApplicableRegionSet regions = manager.getApplicableRegions(location.toVector().toBlockPoint());
        final Result res = Result.of(regions.isMemberOfAll(lp) || regions.isOwnerOfAll(lp));
        if (res.isFailure()) {
            player.sendMessage(Component.text());
        }
        return res;
    }

    @Override
    public @NotNull Result regionPresent(@NotNull Location location) {
        return Result.of(WorldGuard.getInstance().getPlatform().getRegionContainer().get((World) location.getExtent()).getApplicableRegions(location.toVector().toBlockPoint()).size() > 0);
    }
}
