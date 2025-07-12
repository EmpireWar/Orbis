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
package org.empirewar.orbis.command.parser.registry;

import net.kyori.adventure.key.Key;

import java.util.function.Function;

public interface RegistryMapper<I, K> {

    RegistryMapper<String, Key> KEY = create(Key::key, Key::asString);
    RegistryMapper<String, String> IDENTITY = create(Function.identity(), Function.identity());

    K map(I base);

    I reverse(K mapped);

    static <I, K> RegistryMapper<I, K> create(
            final Function<I, K> map, final Function<K, I> reverse) {
        return new RegistryMapperImpl<>(map, reverse);
    }
}
