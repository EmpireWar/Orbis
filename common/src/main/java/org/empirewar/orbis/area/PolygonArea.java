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
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
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
        Path2D path = new Path2D.Double();

        final List<Vector3i> list = points.stream().toList();

        // Move to the first point in the polygon
        path.moveTo(list.getFirst().x, list.getFirst().z);

        // Connect the points in the polygon
        for (int i = 1; i < points.size(); i++) {
            path.lineTo(list.get(i).x, list.get(i).z);
        }

        // Close the path
        path.closePath();

        // Create a Point2D object for the test point
        Point2D testPoint = new Point2D.Double(x, z);

        // Check if the test point is inside the polygon
        return path.contains(testPoint);
    }

    // Helper method to check if a point lies exactly on a segment between two vertices with double
    // precision
    private static boolean onSegment(Vector3i p1, Vector3i p2, Vector3d p) {
        return p.x <= Math.max(p1.x, p2.x)
                && p.x >= Math.min(p1.x, p2.x)
                && p.z <= Math.max(p1.z, p2.z)
                && p.z >= Math.min(p1.z, p2.z)
                && Math.abs((p2.z - p1.z) * (p.x - p1.x) - (p2.x - p1.x) * (p.z - p1.z)) < 1e-9;
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
