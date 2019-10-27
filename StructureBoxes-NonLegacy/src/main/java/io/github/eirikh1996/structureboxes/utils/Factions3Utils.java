package io.github.eirikh1996.structureboxes.utils;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Factions3Utils {
    public static boolean allowBuild(Player player, Location location){
        MPlayer mp = MPlayer.get(player);
        PS ps = PS.valueOf(location);
        Faction faction = BoardColl.get().getFactionAt(ps);
        return faction.isPlayerPermitted(mp, MPerm.getPermBuild());
    }
}
