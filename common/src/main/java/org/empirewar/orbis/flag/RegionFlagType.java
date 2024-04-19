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
package org.empirewar.orbis.flag;

import com.mojang.serialization.Codec;

import org.empirewar.orbis.registry.Registries;
import org.empirewar.orbis.registry.Registry;

public interface RegionFlagType<F extends MutableRegionFlag<?>> {

    // spotless:off
    RegionFlagType<MutableRegionFlag<?>> MUTABLE = register("mutable", MutableRegionFlag.CODEC);
    RegionFlagType<GroupedMutableRegionFlag<?>> GROUPED_MUTABLE = register("grouped_mutable", GroupedMutableRegionFlag.CODEC);
    // spotless:on

    Codec<F> codec();

    private static <F extends MutableRegionFlag<?>> RegionFlagType<F> register(
            String id, Codec<F> codec) {
        return Registry.register(Registries.FLAG_TYPE, id, () -> codec);
    }
}
