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

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.registry.OrbisRegistries;

public interface RegionFlagType<F extends MutableRegionFlag<?>> {

    Key key();

    MapCodec<F> codec();

    static <F extends MutableRegionFlag<?>> RegionFlagType<F> register(
            String id, MapCodec<F> codec) {
        Key key = Key.key("orbis", id);
        RegionFlagType<F> type = new RegionFlagType<F>() {
            @Override
            public Key key() {
                return key;
            }

            @Override
            public MapCodec<F> codec() {
                return codec;
            }
        };
        OrbisRegistries.FLAG_TYPE.register(key, type);
        return type;
    }

    // spotless:off
    RegionFlagType<MutableRegionFlag<?>> MUTABLE = register("mutable", MutableRegionFlag.CODEC);
    RegionFlagType<GroupedMutableRegionFlag<?>> GROUPED_MUTABLE = register("grouped_mutable", GroupedMutableRegionFlag.CODEC);
    // spotless:on
}
