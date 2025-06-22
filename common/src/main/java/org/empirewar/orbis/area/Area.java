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
package org.empirewar.orbis.area;

import com.mojang.serialization.Codec;

import org.empirewar.orbis.registry.Registries;
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

    Codec<Area> CODEC = Registries.AREA_TYPE.getCodec().dispatch(Area::getType, AreaType::codec);

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
    boolean addPoint(Vector3i point);

    /**
     * Attempts to remove a point from this area.
     * <p>
     * This method will return as specified by {@link java.util.Set#remove(Object)}.
     *
     * @param point the point to remove
     * @return specified by {@link java.util.Set#remove(Object)}
     */
    boolean removePoint(Vector3i point);

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

    AreaType<?> getType();
}
