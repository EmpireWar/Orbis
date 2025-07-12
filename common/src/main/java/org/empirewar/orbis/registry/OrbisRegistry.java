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
package org.empirewar.orbis.registry;

import com.mojang.serialization.Codec;

import net.kyori.adventure.key.Keyed;

import org.empirewar.orbis.registry.lifecycle.RegistryLifecycle;

import java.util.Optional;
import java.util.Set;

public sealed interface OrbisRegistry<T, K> extends Iterable<T>, Keyed
        permits ResolvableRegistry, SimpleOrbisRegistry {

    T register(K key, T entry);

    Optional<T> unregister(K key);

    Optional<T> get(K key);

    Optional<K> getKey(T entry);

    Codec<T> getCodec();

    Set<T> getAll();

    Set<K> getKeys();

    RegistryLifecycle getLifecycle();

    void setLifecycle(RegistryLifecycle lifecycle);

    static <V, K, T extends V> T register(OrbisRegistry<V, K> registry, K key, T entry) {
        registry.register(key, entry);
        return entry;
    }
}
