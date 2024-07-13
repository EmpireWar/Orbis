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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.empirewar.orbis.util.ExtraCodecs;
import org.joml.Vector3dc;
import org.joml.Vector3i;

import java.util.List;
import java.util.Optional;

public final class PolygonArea extends EncompassingArea {

    public static MapCodec<PolygonArea> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(ExtraCodecs.VEC_3I
                            .listOf()
                            .fieldOf("points")
                            .forGetter(c -> c.points.stream().toList()))
                    .apply(instance, PolygonArea::new));

    public PolygonArea() {
        super();
    }

    private PolygonArea(List<Vector3i> points) {
        super(points);
    }

    @Override
    public boolean contains(Vector3dc point) {
        return contains(point.x(), point.y(), point.z());
    }

    @Override
    public boolean contains(double x, double y, double z) {
        boolean inside = false;

        // TODO how to fix algorithm so that a position on a block that a polygon line passes over
        // is considered valid?

        final int vertexCount = points.size();
        final List<Vector3i> list = points.stream().toList();
        // Ray-casting algorithm
        for (int i = 0, j = vertexCount - 1; i < vertexCount; j = i++) {
            Vector3i vertex1 = list.get(i);
            Vector3i vertex2 = list.get(j);

            // spotless:off
            if ((vertex1.z > z) != (vertex2.z > z) &&
                    (x < (vertex2.x - vertex1.x) * (z - vertex1.z) / (vertex2.z - vertex1.z) + vertex1.x)) {
                inside = !inside;
            }
            // spotless:on
        }

        return inside;
    }

    @Override
    public AreaType<?> getType() {
        return AreaType.POLYGON;
    }

    @Override
    public Optional<Integer> getExpectedPoints() {
        return Optional.empty();
    }
}
