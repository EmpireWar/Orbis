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

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.registry.lifecycle.RegistryLifecycle;
import org.empirewar.orbis.registry.lifecycle.RegistryLifecycles;

import java.util.*;

public abstract non-sealed class SimpleOrbisRegistry<T, K> implements OrbisRegistry<T, K> {

    protected final Key registryKey;
    protected final Map<K, T> entries = new HashMap<>();
    protected RegistryLifecycle lifecycle = RegistryLifecycles.loading();

    public SimpleOrbisRegistry(Key registryKey) {
        this.registryKey = registryKey;
    }

    @Override
    public Key key() {
        return registryKey;
    }

    @Override
    public T register(K key, T entry) {
        if (lifecycle == RegistryLifecycles.frozen())
            throw new IllegalStateException("Registry is frozen");
        entries.put(key, entry);
        return entry;
    }

    @Override
    public Optional<T> unregister(K key) {
        if (lifecycle == RegistryLifecycles.frozen())
            throw new IllegalStateException("Registry is frozen");
        T entry = entries.remove(key);
        return Optional.ofNullable(entry);
    }

    @Override
    public Optional<T> get(K key) {
        return Optional.ofNullable(entries.get(key));
    }

    @Override
    public Optional<K> getKey(T entry) {
        for (Map.Entry<K, T> e : entries.entrySet()) {
            if (Objects.equals(e.getValue(), entry)) return Optional.of(e.getKey());
        }
        return Optional.empty();
    }

    @Override
    public Set<T> getAll() {
        return new HashSet<>(entries.values());
    }

    @Override
    public Set<K> getKeys() {
        return new HashSet<>(entries.keySet());
    }

    @Override
    public RegistryLifecycle getLifecycle() {
        return lifecycle;
    }

    @Override
    public void setLifecycle(RegistryLifecycle lifecycle) {
        if (lifecycle.equals(this.lifecycle)) {
            throw new IllegalArgumentException("Lifecycle has not changed");
        }

        // Registries initialise before the API is set, only log if the API is set (i.e. a runtime
        // lifecycle change)
        if (OrbisAPI.get() != null) {
            OrbisAPI.get()
                    .logger()
                    .info(
                            "Registry '{}' lifecycle changed from {} to {}",
                            key().asString(),
                            this.lifecycle.name(),
                            lifecycle.name());
        }

        this.lifecycle = lifecycle;
    }

    @Override
    public String toString() {
        return "Registry[" + this.registryKey + "]";
    }

    @Override
    public Iterator<T> iterator() {
        return entries.values().iterator();
    }
}
