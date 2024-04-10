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

import org.empirewar.orbis.serialization.context.passport.Passport;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class ContextQueue {

    private final Map<Class<?>, Passport<?>> blindPatienceMappings = new HashMap<>();

    ContextQueue() {}

    public void clear() {
        if (!blindPatienceMappings.isEmpty()) {
            System.out.println(
                    "There were existing blind patience mappings: " + blindPatienceMappings);
        }
        blindPatienceMappings.clear();
    }

    public <T> void beg(Class<T> passport, Consumer<T> consumer) {
        final Passport<T> passportSupplier =
                (Passport<T>) blindPatienceMappings.getOrDefault(passport, new Passport<>());
        passportSupplier.add(consumer);
        blindPatienceMappings.put(passport, passportSupplier);
    }

    public <T> void rewardPatience(T object) {
        final Passport<?> passport = blindPatienceMappings.remove(object.getClass());
        if (passport == null) return;
        passport.getSuppliers().forEach(consumer -> ((Consumer<T>) consumer).accept(object));
    }
}
