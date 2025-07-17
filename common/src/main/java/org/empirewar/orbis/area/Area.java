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

import com.mojang.serialization.Codec;

import org.empirewar.orbis.registry.OrbisRegistries;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Set;

/**
 * Represents some area that forms a shape through a set of points.
 * <p>
 * Areas are non-locational; they do not contain any world-specific data.
 * <p>
 * Instead, a world contains a set of Areas.
 */
public sealed interface Area extends Iterable<Vector3ic> permits EncompassingArea {

    Codec<Area> CODEC =
            OrbisRegistries.AREA_TYPE.getCodec().dispatch(Area::getType, AreaType::codec);

    /**
     * Removes all points from this area.
     */
    void clearPoints();

    /**
     * Attempts to add a point to this area.
     * <p>
     * This method will return false if the area does not support the addition of
     * another point, e.g. a cuboid exceeding 4 points.
     *
     * @param point the point to add
     * @return true if the point is able to fit into this area
     */
    boolean addPoint(Vector3ic point);

    /**
     * Attempts to remove a point from this area.
     * <p>
     * This method will return as specified by {@link java.util.Set#remove(Object)}.
     *
     * @param point the point to remove
     * @return specified by {@link java.util.Set#remove(Object)}
     */
    boolean removePoint(Vector3ic point);

    /**
     * @see #contains(double, double, double)
     */
    boolean contains(Vector3dc point);

    /**
     * Gets whether the specified point is within this area.
     * <p>
     * This method will always return false if the area is incomplete.
     *
     * @param x the x point to check
     * @param y the y point to check
     * @param z the z point to check
     * @return true if the point is within the area specified by {@link #points()}
     */
    boolean contains(double x, double y, double z);

    /**
     * Gets the minimum point of this area.
     * <p>
     * This method does not guarantee returning a {@link Vector3i} from
     * {@link #points()}; it is the minimum bounds point.
     *
     * @return the minimum point
     */
    Vector3ic getMin();

    /**
     * Gets the maximum point of this area.
     * <p>
     * This method does not guarantee returning a {@link Vector3i} from
     * {@link #points()}; it is the maximum bounds point.
     *
     * @return the maximum point
     */
    Vector3ic getMax();

    /**
     * Gets the set of points that make up this area.
     * <p>
     * The returned set cannot be modified.
     *
     * @return set of points of this area
     */
    Set<Vector3ic> points();

    /**
     * Returns a set of points representing the boundary of this area.
     */
    Set<Vector3ic> getBoundaryPoints();

    AreaType<?> getType();
}
