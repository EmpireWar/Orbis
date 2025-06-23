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
package org.empirewar.orbis.flag;

import com.mojang.serialization.MapCodec;

import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.registry.OrbisRegistry;

public interface RegionFlagType<F extends MutableRegionFlag<?>> {

    // spotless:off
    RegionFlagType<MutableRegionFlag<?>> MUTABLE = register("mutable", MutableRegionFlag.CODEC);
    RegionFlagType<GroupedMutableRegionFlag<?>> GROUPED_MUTABLE = register("grouped_mutable", GroupedMutableRegionFlag.CODEC);
    // spotless:on

    MapCodec<F> codec();

    private static <F extends MutableRegionFlag<?>> RegionFlagType<F> register(
            String id, MapCodec<F> codec) {
        return OrbisRegistry.register(OrbisRegistries.FLAG_TYPE, id, () -> codec);
    }
}
