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

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class RegionisedWorldSet implements RegionisedWorld {

    private final String worldName;
    private final Key worldId;
    private final Set<Region> regions;

    public RegionisedWorldSet() {
        this(null, null);
    }

    public RegionisedWorldSet(@Nullable Key worldId, @Nullable String worldName) {
        this.worldName = worldName;
        this.worldId = worldId;
        this.regions = new HashSet<>();
    }

    @Override
    public Optional<String> worldName() {
        return Optional.ofNullable(worldName);
    }

    @Override
    public Optional<Key> worldId() {
        return Optional.ofNullable(worldId);
    }

    @Override
    public RegionQuery.FilterableRegionResult<RegionQuery.Position> query(
            RegionQuery.Position position) {
        final Set<Region> result = regions.stream()
                .filter(r -> r.isGlobal() || r.area().contains(position.position()))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return position.resultBuilder().query(position).result(result).build();
    }

    @Override
    public Set<Region> regions() {
        return regions;
    }

    @Override
    public Optional<Region> getByName(String regionName) {
        return regions.stream()
                .filter(region -> region.name().equals(regionName))
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
