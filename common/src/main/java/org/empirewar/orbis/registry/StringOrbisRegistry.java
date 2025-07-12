/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2025 Empire War
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
package org.empirewar.orbis.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;

import net.kyori.adventure.key.Key;

public class StringOrbisRegistry<T> extends SimpleOrbisRegistry<T, String> {

    public StringOrbisRegistry(Key key) {
        super(key);
    }

    @Override
    public Codec<T> getCodec() {
        return Codec.STRING.flatXmap(
                id -> this.get(id)
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(
                                () -> "Unknown registry key in " + this.key() + ": " + id,
                                Lifecycle.stable())),
                value -> this.getKey(value)
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(
                                () -> "Unknown registry element in " + this.key() + ":" + value,
                                Lifecycle.stable())));
    }
}
