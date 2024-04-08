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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class RegionisedWorldSet implements RegionisedWorld {

    private final Set<Region> regions;

    public RegionisedWorldSet() {
        this.regions = new HashSet<>();
    }

    @Override
    public RegionQuery.Result<Set<Region>, RegionQuery.Position> query(
            RegionQuery.Position position) {
        final Set<Region> result = regions.stream()
                .filter(r -> r.area().contains(position.position()))
                .collect(Collectors.toSet());
        return position.resultBuilder().query(position).result(result).build();
    }

    @Override
    public Set<Region> regions() {
        return regions;
    }

    @Override
    public Optional<Region> getByName(String regionName) {
        return regions.stream()
                .filter(region -> region.getName().equals(regionName))
                .findAny();
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
