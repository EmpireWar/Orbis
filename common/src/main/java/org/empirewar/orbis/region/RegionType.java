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
package org.empirewar.orbis.region;

import com.mojang.serialization.MapCodec;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.registry.OrbisRegistries;

public interface RegionType<R extends Region> {

    RegionType<Region> NORMAL = register("normal", Region.CODEC);
    RegionType<GlobalRegion> GLOBAL = register("global", GlobalRegion.CODEC);

    Key key();

    MapCodec<R> codec();

    static <R extends Region> RegionType<R> register(String id, MapCodec<R> codec) {
        Key key = Key.key("orbis", id);
        RegionType<R> type = new RegionType<R>() {
            @Override
            public Key key() {
                return key;
            }

            @Override
            public MapCodec<R> codec() {
                return codec;
            }
        };
        OrbisRegistries.REGION_TYPE.register(key, type);
        return type;
    }
}
