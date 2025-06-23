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

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SimpleOrbisRegistry<T> implements OrbisRegistry<T> {

    private final Key key;

    private final Map<Key, T> idToEntry = new HashMap<>();
    private final Map<T, Key> valueToEntry = new IdentityHashMap<>();

    SimpleOrbisRegistry(Key key) {
        this.key = key;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public T register(Key key, T entry) {
        idToEntry.put(key, entry);
        valueToEntry.put(entry, key);
        return entry;
    }

    @Override
    public Optional<T> get(Key key) {
        return Optional.ofNullable(idToEntry.get(key));
    }

    @Override
    public Optional<Key> getKey(T entry) {
        return Optional.ofNullable(this.valueToEntry.get(entry));
    }

    @Override
    public Set<T> getAll() {
        return valueToEntry.keySet();
    }

    @Override
    public Set<Key> getKeys() {
        return idToEntry.keySet();
    }

    @Override
    public String toString() {
        return "Registry[" + this.key + "]";
    }

    @NotNull @Override
    public Iterator<T> iterator() {
        return idToEntry.values().iterator();
    }
}
