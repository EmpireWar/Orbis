package org.empirewar.orbis.world;

import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;

import java.util.Set;

/**
 * Represents a world that contains a set of {@link Region}s.
 * <p>
 * Several methods are provided through applicable {@link RegionQuery} subclasses to allow specific queries for regions.
 */
public sealed interface RegionisedWorld extends RegionQuery.Position.Queryable permits RegionisedWorldSet {

    /**
     * Gets the set of regions within this regionised world.
     * @return all regions within this world
     */
    Set<Region> regions();

    /**
     * Attempts to add a region to this world.
     * @param region the region to add
     * @return false if the region is already attached to this world
     */
    boolean add(Region region);

    /**
     * Attempts to remove a region from this world.
     * @param region
     * @return
     */
    boolean remove(Region region);
}
