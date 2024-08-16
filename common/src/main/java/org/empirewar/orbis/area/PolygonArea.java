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
        // I TRIED EVERY ALGORITHM IN EXISTENCE AND NONE WOULD PASS THE POLYGON AREA TEST
        // I GAVE UP AND COPIED WORLDGUARD

        // Quick and dirty check.
        if (x < min.x() || x > max.x() || z < min.z() || z > max.z()) {
            return false;
        }

        final LinkedList<Vector3i> list = new LinkedList<>(points);

        boolean inside = false;
        int npoints = points.size();
        int xNew, zNew;
        int xOld, zOld;
        int x1, z1;
        int x2, z2;
        long crossproduct;
        int i;

        xOld = list.get(npoints - 1).x();
        zOld = list.get(npoints - 1).z();

        for (i = 0; i < npoints; i++) {
            xNew = list.get(i).x();
            zNew = list.get(i).z();
            // Check for corner
            if (xNew == x && zNew == z) {
                return true;
            }
            if (xNew > xOld) {
                x1 = xOld;
                x2 = xNew;
                z1 = zOld;
                z2 = zNew;
            } else {
                x1 = xNew;
                x2 = xOld;
                z1 = zNew;
                z2 = zOld;
            }
            if (x1 <= x && x <= x2) {
                crossproduct = ((long) z - (long) z1) * (long) (x2 - x1)
                        - ((long) z2 - (long) z1) * (long) (x - x1);
                if (crossproduct == 0) {
                    if ((z1 <= z) == (z <= z2)) return true; // on edge
                } else if (crossproduct < 0 && (x1 != x)) {
                    inside = !inside;
                }
            }
            xOld = xNew;
            zOld = zNew;
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
