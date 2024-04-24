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

import java.util.List;
import java.util.Optional;

/**
 * Polygonal areas that span across the entire world height
 */
public final class PolygonalArea extends EncompassingArea {

    public static Codec<PolygonalArea> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExtraCodecs.VEC_3I.listOf().fieldOf("points").forGetter(c -> c.points().stream()
                            .toList()))
            .apply(instance, PolygonalArea::new));

    public PolygonalArea() {
        super();
    }

    private PolygonalArea(List<Vector3i> points) {
        super(points);
    }

    // todo when a polygonal area is created, make it span across the entire world height
    @Override
    public Optional<Integer> getExpectedPoints() {
        return Optional.empty();
    }

    @Override
    public AreaType<?> getType() {
        return AreaType.POLYGON;
    }
}
