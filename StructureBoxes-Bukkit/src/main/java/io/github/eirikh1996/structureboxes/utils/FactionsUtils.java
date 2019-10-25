package io.github.eirikh1996.structureboxes.utils;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FactionsUtils {
    public static boolean allowBuild(Player player, Location location){
        PS ps = PS.valueOf(location);
        MPlayer mp = MPlayer.get(player);
        Faction f = BoardColl.get().getFactionAt(ps);
        return f.isPermitted(MPerm.getPermBuild(), f.getRelationTo(mp)) || mp.isOverriding();
    }
}
