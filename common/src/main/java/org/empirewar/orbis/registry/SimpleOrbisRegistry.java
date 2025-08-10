/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2024 Empire War
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
        if (entries.containsKey(key)) throw new IllegalArgumentException("Entry already exists");
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if (entry == null) throw new IllegalArgumentException("Entry cannot be null");
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
