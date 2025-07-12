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

import org.empirewar.orbis.registry.lifecycle.RegistryLifecycle;

import java.util.function.Consumer;

public non-sealed interface ResolvableRegistry<T extends RegistryResolvable<K>, K>
        extends OrbisRegistry<T, K> {

    /**
     * Resolves a single entry in this registry.
     *
     * <p>If the entry is already present in the registry, it will be passed to the
     * consumer immediately. If the entry is not present, and the registry is not
     * frozen, the consumer will be registered with the registry's resolver list,
     * and will be passed the entry when it is registered.
     *
     * <p>If the registry has already passed the specified lifecycle, this method will throw an
     * {@link IllegalStateException}.
     *
     * @param key         the key of the entry to resolve
     * @param entry       the consumer to accept the entry
     * @param waitFor     the lifecycle to wait in for which the entry is valid
     */
    void resolve(K key, Consumer<T> entry, RegistryLifecycle waitFor);

    /**
     * A convenience overload of {@link #resolve(Object, Consumer, RegistryLifecycle)}
     * which uses the registry's current lifecycle.
     *
     * @param key   the key of the entry to resolve
     * @param entry the consumer to accept the entry
     */
    default void resolve(K key, Consumer<T> entry) {
        resolve(key, entry, getLifecycle());
    }
}
