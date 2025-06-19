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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.empirewar.orbis.util.ExtraCodecs;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public sealed class PolygonArea extends EncompassingArea permits PolyhedralArea {

    public static MapCodec<PolygonArea> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(ExtraCodecs.VEC_3I
                            .listOf()
                            .fieldOf("points")
                            .forGetter(c -> new LinkedList<>(c.points)))
                    .apply(instance, PolygonArea::new));

    public PolygonArea() {
        super();
    }

    protected PolygonArea(List<Vector3i> points) {
        super(points);
    }

    @Override
    public boolean contains(Vector3dc point) {
        return contains(point.x(), point.y(), point.z());
    }

    @Override
    public boolean contains(double x, double y, double z) {
        // Quick and dirty check.
        if (x < min.x() || x > max.x() || z < min.z() || z > max.z()) {
            return false;
        }

        // https://www.eecs.umich.edu/courses/eecs380/HANDOUTS/PROJ2/InsidePoly.html
        double angle = 0;

        final Vector3dc blockPosition = new Vector3d(x, y, z);
        final LinkedList<Vector3ic> pointsList = new LinkedList<>(points);
        for (int i = 0; i < pointsList.size(); i++) {
            final Vector3ic point = pointsList.get(i);
            final Vector3ic nextPoint = pointsList.get((i + 1) % pointsList.size());
            double xOffset = point.x() - blockPosition.x();
            double zOffset = point.z() - blockPosition.z();
            double nextXOffset = nextPoint.x() - blockPosition.x();
            double nextZOffset = nextPoint.z() - blockPosition.z();
            angle += angle(xOffset, zOffset, nextXOffset, nextZOffset);
        }

        // If the angle is zero, it is on the edge
        return Math.abs(angle) >= Math.PI || angle == 0;
    }

    /**
     * Return the angle between two vectors on a plane.
     * The angle is from vector 1 to vector 2, positive anticlockwise.
     * The result is between -pi and pi.
     */
    private double angle(double x1, double y1, double x2, double y2) {
        double dTheta, theta1, theta2;

        theta1 = Math.atan2(y1, x1);
        theta2 = Math.atan2(y2, x2);
        dTheta = theta2 - theta1;
        while (dTheta > Math.PI) dTheta -= Math.PI * 2;
        while (dTheta < -Math.PI) dTheta += Math.PI * 2;

        return dTheta;
    }

    @Override
    public AreaType<?> getType() {
        return AreaType.POLYGON;
    }

    @Override
    public Optional<Integer> getMaximumPoints() {
        return Optional.empty();
    }

    @Override
    public int getMinimumPoints() {
        return 3;
    }
}
