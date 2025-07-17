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

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.registry.lifecycle.RegistryLifecycle;

import java.util.*;
import java.util.function.Consumer;

public class ResolvableStringOrbisRegistry<T extends RegistryResolvable<String>>
        extends StringOrbisRegistry<T> implements ResolvableRegistry<T, String> {

    private final Map<String, List<PendingResolution<T>>> pending = new HashMap<>();

    public ResolvableStringOrbisRegistry(Key key) {
        super(key);
    }

    @Override
    public T register(String key, T entry) {
        super.register(key, entry);
        List<PendingResolution<T>> consumers = pending.remove(key);
        if (consumers != null) {
            OrbisAPI.get()
                    .logger()
                    .info(
                            "Resolving {} to {} consumers with {} lifecycle",
                            key,
                            consumers.size(),
                            lifecycle.name());
            for (PendingResolution<T> pr : consumers) {
                pr.consumer.accept(entry);
            }
        }
        return entry;
    }

    @Override
    public Optional<String> getKey(T entry) {
        return Optional.ofNullable(entry.key());
    }

    @Override
    public void resolve(String key, Consumer<T> consumer, RegistryLifecycle waitFor) {
        if (waitFor.hasExpired(lifecycle)) {
            throw new IllegalStateException("Cannot resolve with " + waitFor.name()
                    + " lifecycle after " + lifecycle.name() + " phase");
        }

        T value = get(key).orElse(null);
        if (value != null) {
            consumer.accept(value);
        } else {
            pending.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(new PendingResolution<>(consumer, waitFor));
        }
    }

    @Override
    public void setLifecycle(RegistryLifecycle lifecycle) {
        super.setLifecycle(lifecycle);
        // Handle pending expirations
        Iterator<Map.Entry<String, List<PendingResolution<T>>>> it =
                pending.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<PendingResolution<T>>> entry = it.next();
            List<PendingResolution<T>> list = entry.getValue();
            list.removeIf(pr -> {
                if (pr.lifecycle.hasExpired(lifecycle)) {
                    OrbisAPI.get()
                            .logger()
                            .error(
                                    "Could not resolve '{}' before {} in registry '{}'",
                                    entry.getKey(),
                                    pr.lifecycle,
                                    key().asString());
                    return true;
                }
                return false;
            });
            if (list.isEmpty()) it.remove();
        }
    }

    private record PendingResolution<T>(Consumer<T> consumer, RegistryLifecycle lifecycle) {}
}
