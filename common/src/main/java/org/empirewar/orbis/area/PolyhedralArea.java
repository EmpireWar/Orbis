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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.empirewar.orbis.util.ExtraCodecs;
import org.joml.Vector3i;

import java.util.*;

/**
 * Polyhedral areas, with complex shapes
 */
public final class PolyhedralArea extends EncompassingArea {

    public static Codec<PolyhedralArea> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(ExtraCodecs.VEC_3I
                            .listOf()
                            .fieldOf("points")
                            .forGetter(c -> c.points().stream().toList()))
                    .apply(instance, PolyhedralArea::new));

    public PolyhedralArea() {
        super();
    }

    private PolyhedralArea(List<Vector3i> points) {
        super(points);
    }

    /**
     * Vertices that are contained in the convex hull.
     */
    private final Set<Vector3i> vertices = new LinkedHashSet<>();

    /**
     * Vertices that are coplanar to the first 3 vertices
     */
    private final Set<Vector3i> vertexBacklog = new LinkedHashSet<>();

    @Override
    public Optional<Integer> getExpectedPoints() {
        return Optional.empty();
    }

    /**
     * Obtain all the vertices for a polyhedral region
     *
     * @return vertices of the polyhedron
     */
    public Collection<Vector3i> getVertices() {
        if (vertexBacklog.isEmpty()) return vertices;

        final List<Vector3i> allVertices = new ArrayList<>(vertices);
        allVertices.addAll(vertexBacklog);

        return allVertices;
    }

    @Override
    public AreaType<?> getType() {
        return AreaType.POLYHEDRON;
    }
}
