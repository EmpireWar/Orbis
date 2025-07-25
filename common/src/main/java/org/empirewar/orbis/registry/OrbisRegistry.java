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
