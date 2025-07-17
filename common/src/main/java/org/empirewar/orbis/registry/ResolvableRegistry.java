/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2025 Empire War
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
