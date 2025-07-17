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
package org.empirewar.orbis.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;

import net.kyori.adventure.key.Key;

public class StringOrbisRegistry<T> extends SimpleOrbisRegistry<T, String> {

    public StringOrbisRegistry(Key key) {
        super(key);
    }

    @Override
    public Codec<T> getCodec() {
        return Codec.STRING.flatXmap(
                id -> this.get(id)
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(
                                () -> "Unknown registry key in " + this.key() + ": " + id,
                                Lifecycle.stable())),
                value -> this.getKey(value)
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(
                                () -> "Unknown registry element in " + this.key() + ":" + value,
                                Lifecycle.stable())));
    }
}
