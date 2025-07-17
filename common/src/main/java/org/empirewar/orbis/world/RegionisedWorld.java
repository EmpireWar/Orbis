/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2024 Empire War
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.empirewar.orbis.world;

import net.kyori.adventure.key.Key;

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
// spotless:off
public sealed interface RegionisedWorld extends RegionQuery.Position.Queryable permits RegionisedWorldSet {
// spotless:on

    /**
     * Gets the key of the world this represents.
     * <p>
     * This method may return an empty optional if this is a "global holder" of all regions.
     * @return key of world this represents
     */
    Optional<Key> worldId();

    /**
     * Gets the set of regions within this regionised world.
     *
     * @return all regions within this world
     */
    Set<Region> regions();

    /**
     * Gets a region by the specified name, if it is present.
     *
     * @param regionName the region name
     * @return an optional with the region if present, else {@link Optional#empty()}
     */
    Optional<Region> getByName(String regionName);

    /**
     * Attempts to add a region to this world.
     *
     * @param region the region to add
     * @return false if the region is already attached to this world
     */
    boolean add(Region region);

    /**
     * Attempts to remove a region from this world.
     *
     * @param region the region to remove
     * @return true if the region was present and was successfully removed
     */
    boolean remove(Region region);
}
