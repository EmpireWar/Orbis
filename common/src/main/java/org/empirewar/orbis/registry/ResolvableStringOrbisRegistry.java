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
