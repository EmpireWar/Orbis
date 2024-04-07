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

import org.empirewar.orbis.registry.Registries;
import org.empirewar.orbis.registry.Registry;

public interface AreaType<A extends Area> {

    AreaType<CuboidArea> CUBOID = register("cuboid", CuboidArea.CODEC);
    AreaType<PolygonalArea> POLYGON = register("polygon", PolygonalArea.CODEC);
    AreaType<PolygonalArea> POLYHEDRON = register("polyhedron", PolygonalArea.CODEC);

    Codec<A> codec();

    private static <A extends Area> AreaType<A> register(String id, Codec<A> codec) {
        return Registry.register(Registries.AREA_TYPE, id, () -> codec);
    }
}
