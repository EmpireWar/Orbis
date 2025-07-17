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
package org.empirewar.orbis.area;

import com.mojang.serialization.MapCodec;

import org.empirewar.orbis.registry.KeyOrbisRegistry;
import org.empirewar.orbis.registry.OrbisRegistries;

public interface AreaType<A extends Area> {

    AreaType<CuboidArea> CUBOID = register("cuboid", CuboidArea.CODEC);
    AreaType<PolygonArea> POLYGON = register("polygon", PolygonArea.CODEC);
    AreaType<PolyhedralArea> POLYHEDRAL = register("polyhedral", PolyhedralArea.CODEC);
    AreaType<SphericalArea> SPHERE = register("sphere", SphericalArea.CODEC);

    MapCodec<A> codec();

    private static <A extends Area> AreaType<A> register(String id, MapCodec<A> codec) {
        return KeyOrbisRegistry.register(OrbisRegistries.AREA_TYPE, id, () -> codec);
    }
}
