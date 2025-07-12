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
package org.empirewar.orbis.registry.lifecycle;

/**
 * Utility and factory class for registry lifecycles.
 */
public final class RegistryLifecycles {
    private RegistryLifecycles() {}

    public static RegistryLifecycle loading() {
        return LOADING;
    }

    public static RegistryLifecycle active() {
        return ACTIVE;
    }

    public static RegistryLifecycle frozen() {
        return FROZEN;
    }

    private static final RegistryLifecycle LOADING = new RegistryLifecycle() {

        @Override
        public boolean hasExpired(RegistryLifecycle current) {
            return current == ACTIVE || current == FROZEN;
        }

        @Override
        public String name() {
            return "loading";
        }
    };

    private static final RegistryLifecycle ACTIVE = new RegistryLifecycle() {

        @Override
        public boolean hasExpired(RegistryLifecycle current) {
            return current == FROZEN;
        }

        @Override
        public String name() {
            return "active";
        }
    };

    private static final RegistryLifecycle FROZEN = new RegistryLifecycle() {

        @Override
        public boolean hasExpired(RegistryLifecycle current) {
            return false;
        }

        @Override
        public String name() {
            return "frozen";
        }
    };
}
