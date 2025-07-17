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
package org.empirewar.orbis.area;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract sealed class EncompassingArea implements Area
        permits CuboidArea, PolygonArea, SphericalArea {

    protected final Set<Vector3ic> points;
    protected final Set<Vector3ic> boundaryPoints;
    protected final Vector3i min = new Vector3i();
    protected final Vector3i max = new Vector3i();

    private final List<Runnable> updateListeners = new ArrayList<>(1);

    EncompassingArea() {
        final int expected = getMaximumPoints().orElse(0);
        this.points = new LinkedHashSet<>(expected);
        this.boundaryPoints = new LinkedHashSet<>(expected * 4);
        calculateEncompassingArea();
    }

    EncompassingArea(List<Vector3ic> points) {
        this.points = new LinkedHashSet<>(points);
        this.boundaryPoints = new LinkedHashSet<>(points.size() * 4);
        calculateEncompassingArea();
    }

    public void addUpdateListener(Runnable listener) {
        this.updateListeners.add(listener);
    }

    public void removeUpdateListener(Runnable listener) {
        this.updateListeners.remove(listener);
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

        this.boundaryPoints.clear();
        // Only generate boundary points if we have enough points
        if (points.size() >= getMinimumPoints()) {
            this.boundaryPoints.addAll(generateBoundaryPoints());
        }

        // Copy to prevent concurrent modification
        List.copyOf(updateListeners).forEach(Runnable::run);
    }

    @Override
    public void clearPoints() {
        points.clear();
        calculateEncompassingArea();
    }

    @Override
    public boolean addPoint(Vector3ic point) {
        final Optional<Integer> expectedPoints = getMaximumPoints();
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
    public boolean removePoint(Vector3ic point) {
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

    @Override
    public Set<Vector3ic> getBoundaryPoints() {
        return boundaryPoints;
    }

    protected abstract Set<Vector3ic> generateBoundaryPoints();

    /**
     * Gets the maximum number of points for this area.
     * <p>
     * For some shapes such as polygons this is not applicable. In these cases, this method should instead return
     * {@link Optional#empty()}.
     *
     * @return an {@link Optional} with the number of maximum points, else
     *         {@link Optional#empty()} if any number of points is applicable.
     */
    public abstract Optional<Integer> getMaximumPoints();

    /**
     * Gets the minimum number of points for this area.
     *
     * @return the minimum number of points required to build this area
     */
    public abstract int getMinimumPoints();

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
