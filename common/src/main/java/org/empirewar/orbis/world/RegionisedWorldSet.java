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
package org.empirewar.orbis.world;

import com.github.davidmoten.rtreemulti.Entry;
import com.github.davidmoten.rtreemulti.RTree;
import com.github.davidmoten.rtreemulti.geometry.Geometry;
import com.github.davidmoten.rtreemulti.geometry.Rectangle;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.area.EncompassingArea;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;
import org.joml.Vector3ic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the regions of a world. Uses a 3D RTree to query regions.
 *
 * @see RegionisedWorld
 */
public final class RegionisedWorldSet implements RegionisedWorld {

    private record GeometryEntry(@Nullable Geometry geometry, Runnable listener) {}

    private final String worldName;
    private final Key worldId;
    private final Set<Region> regions;
    private final Map<Region, GeometryEntry> regionGeometries;

    private RTree<Region, Geometry> regionRTree;

    public RegionisedWorldSet() {
        this(null, null);
    }

    public RegionisedWorldSet(@Nullable Key worldId, @Nullable String worldName) {
        this.worldName = worldName;
        this.worldId = worldId;
        this.regions = ConcurrentHashMap.newKeySet();
        this.regionRTree = RTree.dimensions(3).create();
        this.regionGeometries = new ConcurrentHashMap<>();
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
        Vector3dc pos = position.position();

        // Query the RTree for regions that might contain this point
        Rectangle pointRect = Rectangle.create(
                (float) pos.x(),
                (float) pos.y(),
                (float) pos.z(), // min coords
                (float) pos.x(),
                (float) pos.y(),
                (float) pos.z() // max coords (same as min for point)
                );

        // Get all regions that might contain this point
        LinkedHashSet<Region> result = new LinkedHashSet<>();
        for (Entry<Region, Geometry> entry : regionRTree.search(pointRect)) {
            Region region = entry.value();
            // Double-check the region actually contains the point
            if (region.area().contains(pos)) {
                result.add(region);
            }
        }

        // Add global regions
        for (Region region : regions) {
            if (region.isGlobal()) {
                result.add(region);
            }
        }

        // Sort by priority (reverse natural order)
        List<Region> sorted = new ArrayList<>(result);
        sorted.sort(Comparator.reverseOrder());

        return position.resultBuilder()
                .query(position)
                .result(new LinkedHashSet<>(sorted))
                .build();
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
        if (regions.add(region)) {
            if (!region.isGlobal()) {
                updateBoundingBox(region);
            }
            return true;
        }
        return false;
    }

    private void updateBoundingBox(Region region) {
        if (!regions.contains(region)) {
            return; // Region not in this set
        }

        if (region.isGlobal()) {
            throw new IllegalArgumentException("Cannot update global region");
        }

        final EncompassingArea area = (EncompassingArea) region.area();

        // Remove old entry
        GeometryEntry oldGeometry = regionGeometries.remove(region);
        if (oldGeometry != null) {
            area.removeUpdateListener(oldGeometry.listener());
            if (oldGeometry.geometry() != null) {
                regionRTree = regionRTree.delete(region, oldGeometry.geometry());
            }
        }

        // Add new entry if the area is not empty
        Rectangle newRect = createBoundingBox(area);
        if (newRect != null) {
            regionRTree = regionRTree.add(region, newRect);
        }

        // Always add an update listener
        final Runnable updateListener = () -> updateBoundingBox(region);
        area.addUpdateListener(updateListener);
        regionGeometries.put(region, new GeometryEntry(newRect, updateListener));
    }

    private @Nullable Rectangle createBoundingBox(Area area) {
        // Handle empty areas
        if (area.points().isEmpty()) {
            return null;
        }

        // We will represent all areas as a rectangle of the area it can be in
        // We then use Area#contains to confirm that the point is in the area
        Vector3ic min = area.getMin();
        Vector3ic max = area.getMax();

        return Rectangle.create(
                (float) min.x(),
                (float) min.y(),
                (float) min.z(),
                (float) max.x() + 1,
                (float) max.y() + 1,
                (float) max.z() + 1 // +1 to be inclusive of the max coordinate
                );
    }

    @Override
    public boolean remove(Region region) {
        if (regions.remove(region)) {
            if (!region.isGlobal()) {
                // Remove from RTree
                GeometryEntry geometry = regionGeometries.remove(region);
                if (geometry != null) {
                    final EncompassingArea area = (EncompassingArea) region.area();
                    area.removeUpdateListener(geometry.listener());
                    if (geometry.geometry() != null) {
                        regionRTree = regionRTree.delete(region, geometry.geometry());
                    }
                }
            }
            return true;
        }
        return false;
    }
}
