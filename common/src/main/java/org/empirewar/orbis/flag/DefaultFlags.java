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

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.registry.Registries;

public final class DefaultFlags {

    // spotless:off
    public static final RegionFlag<Boolean> CAN_BREAK = register("can_break", false, Codec.BOOL);
    public static final RegionFlag<Boolean> CAN_PLACE = register("can_place", true, Codec.BOOL);
    // spotless:on

    private static <T> RegionFlag<T> register(String name, T defaultValue, Codec<T> codec) {
        final Key key = Key.key("orbis", name);
        final RegionFlag<T> entry = RegionFlag.<T>builder()
                .key(key)
                .codec(codec)
                .defaultValue(defaultValue)
                .build();
        Registries.FLAGS.register(key, entry);
        return entry;
    }
}
