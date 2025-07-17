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
package org.empirewar.orbis.region;

import com.mojang.serialization.MapCodec;

import org.empirewar.orbis.registry.KeyOrbisRegistry;
import org.empirewar.orbis.registry.OrbisRegistries;

public interface RegionType<R extends Region> {

    RegionType<Region> NORMAL = register("normal", Region.CODEC);
    RegionType<GlobalRegion> GLOBAL = register("global", GlobalRegion.CODEC);

    MapCodec<R> codec();

    private static <R extends Region> RegionType<R> register(String id, MapCodec<R> codec) {
        return KeyOrbisRegistry.register(OrbisRegistries.REGION_TYPE, id, () -> codec);
    }
}
