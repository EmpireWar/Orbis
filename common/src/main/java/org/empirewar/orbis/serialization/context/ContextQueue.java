/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 EmpireWar
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
package org.empirewar.orbis.serialization.context;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.serialization.context.passport.Passport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * This class is a bit of a mess but I can't think of a better way and I also don't want to copy over everything
 * from our private projects.
 */
public final class ContextQueue {

    private final Map<Class<?>, Passport<?>> blindPatienceMappings = new HashMap<>();
    private final Map<Class<?>, List<?>> resolved = new HashMap<>();

    ContextQueue() {}

    public void clear() {
        if (!blindPatienceMappings.isEmpty()) {
            OrbisAPI.get()
                    .logger()
                    .error(
                            "There were existing blind patience mappings: {}",
                            blindPatienceMappings);
        }
        blindPatienceMappings.clear();
        resolved.clear();
    }

    public <T> void beg(Class<T> passport, Function<T, Boolean> consumer) {
        if (resolved.containsKey(passport)) {
            final List<?> list = resolved.get(passport);
            for (Object o : list) {
                if (consumer.apply((T) o)) {
                    return;
                }
            }
        }

        final Passport<T> passportSupplier =
                (Passport<T>) blindPatienceMappings.getOrDefault(passport, new Passport<>());
        passportSupplier.add(consumer);
        blindPatienceMappings.put(passport, passportSupplier);
    }

    public <T> void rewardPatience(Class<? extends T> type, T object) {
        final List<T> list = (List<T>) resolved.getOrDefault(type, new ArrayList<>());
        list.add(object);
        resolved.put(type, list);

        final Passport<?> passport = blindPatienceMappings.get(type);
        if (passport == null) return;

        final Iterator<? extends Function<?, Boolean>> iterator =
                passport.getSuppliers().iterator();
        while (iterator.hasNext()) {
            final Function<T, Boolean> next = (Function<T, Boolean>) iterator.next();
            if (next.apply(object)) {
                iterator.remove();
            }
        }

        if (passport.getSuppliers().isEmpty()) {
            blindPatienceMappings.remove(type);
        }
    }
}
