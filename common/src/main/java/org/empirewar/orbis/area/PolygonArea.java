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
import org.joml.Vector3ic;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public final class PolygonArea extends EncompassingArea {

    public static MapCodec<PolygonArea> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(ExtraCodecs.VEC_3I
                            .listOf()
                            .fieldOf("points")
                            .forGetter(c -> new LinkedList<>(c.points)))
                    .apply(instance, PolygonArea::new));

    private static final double EPSILON = 1e-7;

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
        // Quick and dirty check.
        if (x < min.x() || x > max.x() || z < min.z() || z > max.z()) {
            return false;
        }

        // https://www.eecs.umich.edu/courses/eecs380/HANDOUTS/PROJ2/InsidePoly.html
        double angleSum = 0;

        final Vector3dc blockPosition = new Vector3d(x, y, z);
        final LinkedList<Vector3ic> pointsList = new LinkedList<>(points);
        for (int i = 0; i < pointsList.size(); i++) {
            Vector3dc pointToPos = new Vector3d(pointsList.get(i)).sub(blockPosition);
            Vector3dc nextPointToPos =
                    new Vector3d(pointsList.get((i + 1) % pointsList.size())).sub(blockPosition);
            double m1 = pointToPos.length();
            double m2 = nextPointToPos.length();
            if (m1 * m2 <= EPSILON) {
                // We are on a node, consider this inside
                angleSum = 2 * Math.PI;
                break;
            }

            double cosTheta = (pointToPos.x() * nextPointToPos.x()
                            + pointToPos.y() * nextPointToPos.y()
                            + pointToPos.z() * nextPointToPos.z())
                    / (m1 * m2);
            angleSum += Math.acos(cosTheta);
        }

        return angleSum == 2 * Math.PI;
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
