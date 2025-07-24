package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;

import java.util.Optional;

@Deprecated(forRemoval = true)
public class BentoBoxUtils {

    public static boolean canBuild(Player player, org.bukkit.Location location) {
        BentoBox bentoBox = StructureBoxes.getInstance().getBentoBoxPlugin();
        final Optional<Island> island = bentoBox.getIslands().getIslandAt(location);
        return island.isPresent() && island.get().isAllowed(User.getInstance(player), Flags.PLACE_BLOCKS);
    }
    public static boolean withinRegion(Location location) {
        return StructureBoxes.getInstance().getBentoBoxPlugin().getIslands().getIslandAt(location).isPresent();
    }
}
