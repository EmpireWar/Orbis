/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2026 Empire War
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
package org.empirewar.orbis.hytale.flags;

import com.mojang.serialization.Codec;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;

import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.registry.OrbisRegistries;

import java.util.function.Supplier;

/**
 * The default flags that Orbis provides for Hytale.
 */
public final class HytaleFlags {

    // spotless:off
    public static final RegistryRegionFlag<Boolean> CAN_BREAK = register("can_break",
            "Whether players can break blocks", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> CAN_PLACE = register("can_place",
            "Whether players can place blocks", () -> true, Codec.BOOL);
    // spotless:on

    private static <T> RegistryRegionFlag<T> register(
            @KeyPattern.Value String name, Supplier<T> defaultValue, Codec<T> codec) {
        return register(name, null, defaultValue, codec);
    }

    private static <T> RegistryRegionFlag<T> register(
            @KeyPattern.Value String name,
            String description,
            Supplier<T> defaultValue,
            Codec<T> codec) {
        final Key key = Key.key("orbis", name);
        final RegistryRegionFlag.Builder<T> entry =
                RegistryRegionFlag.<T>builder().key(key).codec(codec).defaultValue(defaultValue);
        if (description != null) {
            entry.description(description);
        }

        final RegistryRegionFlag<T> built = entry.build();
        OrbisRegistries.FLAGS.register(key, built);
        return built;
    }
}
