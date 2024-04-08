/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 EmpireWar
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
package org.empirewar.orbis.world;

import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;

import java.util.Optional;
import java.util.Set;

/**
 * Represents a world that contains a set of {@link Region}s.
 * <p>
 * Several methods are provided through applicable {@link RegionQuery}
 * subclasses to allow specific queries for regions.
 */
public sealed interface RegionisedWorld extends RegionQuery.Position.Queryable permits RegionisedWorldSet {

    /**
     * Gets the set of regions within this regionised world.
     *
     * @return all regions within this world
     */
    Set<Region> regions();

    /**
     * Gets a region by the specified name, if it is present.
     *
     * @param regionName
     *            the region name
     * @return an optional with the region if present, else {@link Optional#empty()}
     */
    Optional<Region> getByName(String regionName);

    /**
     * Attempts to add a region to this world.
     *
     * @param region
     *            the region to add
     * @return false if the region is already attached to this world
     */
    boolean add(Region region);

    /**
     * Attempts to remove a region from this world.
     *
     * @param region
     *            the region to remove
     * @return true if the region was present and was successfully removed
     */
    boolean remove(Region region);
}
