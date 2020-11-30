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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class MathUtils {

    public static Location<World> sbToSpongeLoc(io.github.eirikh1996.structureboxes.utils.Location sbLoc){
        return new Location<>(Sponge.getServer().getWorld(sbLoc.getWorld()).get(), sbLoc.getX(), sbLoc.getY(), sbLoc.getZ());
    }

    public static io.github.eirikh1996.structureboxes.utils.Location spongeToSBLoc(Location<World> spongeLoc){
        return new io.github.eirikh1996.structureboxes.utils.Location(spongeLoc.getExtent().getName(), spongeLoc.getBlockX(), spongeLoc.getBlockY(), spongeLoc.getBlockZ());
    }
}
