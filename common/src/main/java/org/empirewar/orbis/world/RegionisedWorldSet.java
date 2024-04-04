package org.empirewar.orbis.world;

import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public non-sealed class RegionisedWorldSet implements RegionisedWorld {

    private final Set<Region> regions;

    public RegionisedWorldSet() {
        this.regions = new HashSet<>();
    }

    @Override
    public RegionQuery.Result<Set<Region>, RegionQuery.Position> query(RegionQuery.Position position) {
        final Set<Region> result = regions.stream().filter(r -> r.area().contains(position.position())).collect(Collectors.toSet());
        return position.resultBuilder()
                .query(position)
                .result(result)
                .build();
    }

    @Override
    public Set<Region> regions() {
        return regions;
    }

    @Override
    public boolean add(Region region) {
        return regions.add(region);
    }

    @Override
    public boolean remove(Region region) {
        return regions.remove(region);
    }
}
