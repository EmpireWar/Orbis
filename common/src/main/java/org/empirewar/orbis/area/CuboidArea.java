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
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.List;
import java.util.Optional;

public final class CuboidArea extends EncompassingArea {

    public static Codec<CuboidArea> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExtraCodecs.VEC_3I.listOf().fieldOf("points").forGetter(c -> c.points().stream()
                            .toList()))
            .apply(instance, CuboidArea::new));

    public CuboidArea() {
        super();
    }

    private CuboidArea(List<Vector3i> points) {
        super(points);
    }

    @Override
    public boolean contains(Vector3d point) {
        if (points.size() != getExpectedPoints().orElseThrow()) return false;

        final double x = point.x();
        final double y = point.y();
        final double z = point.z();
        return x >= min.x()
                && x <= max.x()
                && y >= min.y()
                && y <= max.y()
                && z >= min.z()
                && z <= max.z();
    }

    @Override
    public AreaType<?> getType() {
        return AreaType.CUBOID;
    }

    @Override
    public Optional<Integer> getExpectedPoints() {
        return Optional.of(2);
    }
}
