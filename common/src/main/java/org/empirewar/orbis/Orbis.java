/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 Empire War
 * Copyright (C) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.empirewar.orbis;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.selection.SelectionManager;
import org.empirewar.orbis.world.RegionisedWorld;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

/**
 * The core Orbis instance for a platform that manages worlds and players.
 */
public interface Orbis {

    SelectionManager selectionManager();

    /**
     * Gets all regionised worlds.
     * <p>
     * This does not contain the {@link #getGlobalWorld()}.
     * @return {@link Set} of regionised worlds
     */
    Set<RegionisedWorld> getRegionisedWorlds();

    /**
     * Gets the "global world" which holds all regions.
     * @return global world holding all regions
     */
    RegionisedWorld getGlobalWorld();

    RegionisedWorld getRegionisedWorld(Key worldId);

    boolean removeRegion(Region region);

    Key getPlayerWorld(UUID player);

    boolean hasPermission(UUID player, String permission);

    Path dataFolder();

    ConfigurationNode config();

    ConfigurationNode worldsConfig();

    Logger logger();
}
