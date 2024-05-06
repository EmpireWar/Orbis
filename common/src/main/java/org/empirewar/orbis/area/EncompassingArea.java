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
package org.empirewar.orbis.area;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract sealed class EncompassingArea implements Area permits CuboidArea, PolygonArea {

    protected final Set<Vector3i> points;
    protected final Vector3i min = new Vector3i();
    protected final Vector3i max = new Vector3i();

    EncompassingArea() {
        final int expected = getExpectedPoints().orElse(0);
        this.points = new HashSet<>(expected);
        calculateEncompassingArea();
    }

    EncompassingArea(List<Vector3i> points) {
        this.points = new HashSet<>(points);
        calculateEncompassingArea();
    }

    /**
     * Calculates the minimum and maximum points of this area.
     */
    protected void calculateEncompassingArea() {
        // Avoid throwing IncompleteAreaException to keep this simple
        final Vector3ic first = points.stream().findFirst().orElse(new Vector3i());
        int minX = first.x();
        int minY = first.y();
        int minZ = first.z();
        int maxX = minX;
        int maxY = minY;
        int maxZ = minZ;

        for (Vector3ic point : points) {
            minX = Math.min(point.x(), minX);
            minY = Math.min(point.y(), minY);
            minZ = Math.min(point.z(), minZ);

            maxX = Math.max(point.x(), maxX);
            maxY = Math.max(point.y(), maxY);
            maxZ = Math.max(point.z(), maxZ);
        }

        min.x = minX;
        min.y = minY;
        min.z = minZ;

        max.x = maxX;
        max.y = maxY;
        max.z = maxZ;
    }

    @Override
    public boolean addPoint(Vector3i point) {
        final Optional<Integer> expectedPoints = getExpectedPoints();
        if (expectedPoints.isPresent()) {
            if (points.size() + 1 > expectedPoints.get()) {
                return false;
            }
        }

        if (points.add(point)) {
            calculateEncompassingArea();
            return true;
        }
        return false;
    }

    @Override
    public boolean removePoint(Vector3i point) {
        if (points.remove(point)) {
            calculateEncompassingArea();
            return true;
        }
        return false;
    }

    @Override
    public Vector3ic getMin() {
        return min;
    }

    @Override
    public Vector3ic getMax() {
        return max;
    }

    @Override
    public Set<Vector3ic> points() {
        return Set.copyOf(points);
    }

    /**
     * Gets the expected number of points for this area.
     * <p>
     * This may represent a "maximum" however for some shapes such as polygons this
     * is not applicable. In these cases, this method should instead return
     * {@link Optional#empty()}.
     *
     * @return an {@link Optional} with the number of expected points, else
     *         {@link Optional#empty()} if any number of points is applicable.
     */
    public abstract Optional<Integer> getExpectedPoints();

    @NotNull @Override
    public Iterator<Vector3ic> iterator() {
        Set<Vector3ic> blocks = new HashSet<>();
        for (int x = min.x; x <= max.x; x++) {
            for (int y = min.y; y <= max.y; y++) {
                for (int z = min.z; z <= max.z; z++) {
                    // Contains considers the implementation (e.g. polygon)
                    if (contains(x, y, z)) {
                        blocks.add(new Vector3i(x, y, z));
                    }
                }
            }
        }
        return blocks.iterator();
    }
}
