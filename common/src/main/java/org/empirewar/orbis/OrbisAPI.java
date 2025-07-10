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
package org.empirewar.orbis;

import org.empirewar.orbis.registry.OrbisRegistries;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Provides access to the {@link Orbis} instance.
 */
public abstract class OrbisAPI implements Orbis {

    private static Orbis instance;

    public static Orbis get() {
        return instance;
    }

    public static void set(Orbis instance) {
        if (OrbisAPI.instance != null) {
            throw new IllegalStateException("Instance already set!");
        }
        OrbisRegistries.initialize();
        OrbisAPI.instance = instance;
    }

    // --- TESTING SUPPORT ---
    /**
     * Resets the OrbisAPI singleton. For test use only!
     */
    @VisibleForTesting
    public static void reset() {
        instance = null;
    }
}
