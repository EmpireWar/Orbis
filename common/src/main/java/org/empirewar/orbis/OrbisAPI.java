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
