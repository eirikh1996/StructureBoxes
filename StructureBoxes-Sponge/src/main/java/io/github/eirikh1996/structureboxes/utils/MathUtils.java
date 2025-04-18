/*
    This file is part of Structure Boxes.

    Structure Boxes is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Structure Boxes is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Structure Boxes.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldedit.sponge.SpongeWorld;
import com.sk89q.worldedit.sponge.adapter.SpongeImplAdapter;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldManager;
import com.sk89q.worldedit.util.Location;

public class MathUtils {

    private static final SpongeImplAdapter ADAPTER = StructureBoxes.getInstance().getWorldEditPlugin().getAdapter();

    public static ServerLocation weToSpongeLoc(Location sbLoc){
        return ServerLocation.of((ServerWorld) ((SpongeWorld) sbLoc.getExtent()).getWorld(), sbLoc.getX(), sbLoc.getY(), sbLoc.getZ());
    }

    public static Location spongeToWELoc(ServerLocation spongeLoc){
        return new Location(ADAPTER.getWorld(), spongeLoc.getBlockX(), spongeLoc.getBlockY(), spongeLoc.getBlockZ());
    }
}
