/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024  EmpireWar
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

import org.joml.Vector3d;

import java.util.Set;

/**
 * Represents some area that forms a shape through a set of points.
 * <p>
 * Areas are non-locational; they do not contain any world-specific data.
 * <p>
 * Instead, a world contains a set of Areas.
 */
public interface Area {

    /**
     * Gets the set of points that make up this area.
     * <p>
     * The returned set cannot be modified.
     * @return set of points of this area
     */
    Set<Vector3d> points();
}
