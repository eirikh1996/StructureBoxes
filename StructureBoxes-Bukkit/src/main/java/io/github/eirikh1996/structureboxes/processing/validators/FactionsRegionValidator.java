package io.github.eirikh1996.structureboxes.processing.validators;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import io.github.eirikh1996.structureboxes.processing.BukkitRegionPredicate;
import io.github.eirikh1996.structureboxes.processing.Result;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FactionsRegionValidator implements BukkitRegionPredicate {
    @Override
    public @NotNull Result validate(@NotNull Player player, @NotNull Location location) {
        PS ps = PS.valueOf(BukkitAdapter.adapt(location));
        MPlayer mp = MPlayer.get(player);
        Faction f = BoardColl.get().getFactionAt(ps);
        return Result.of(!mp.isOverriding() && (f.isPermitted(MPerm.getPermBuild().getId(), f.getRelationTo(mp).getId()) || BoardColl.get().getTerritoryAccessAt(ps).isGranted(mp)));
    }

    @Override
    public @NotNull Result regionPresent(@NotNull Location location) {
        return Result.of(BoardColl.get().getFactionAt(PS.valueOf(BukkitAdapter.adapt(location))) != null);
    }
}
