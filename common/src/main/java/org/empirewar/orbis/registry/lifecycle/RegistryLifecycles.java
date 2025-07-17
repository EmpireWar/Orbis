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
