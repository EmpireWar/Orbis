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
package org.empirewar.orbis.flag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

import org.empirewar.orbis.registry.OrbisRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents the flag of a {@link org.empirewar.orbis.region.Region}.
 * <p>
 * All flags are {@link Keyed} and thus have a {@link Key} to identify them.
 * <p>
 * To add a region flag, you should use the {@link RegistryRegionFlag#builder()} and register it to {@link OrbisRegistries#FLAGS}.
 * <p>
 * Note that all flags must provide a {@link Codec} of {@link T}.
 * This allows for deserialization and serialization and is also used in command parsing.
 * <p>
 * You may need to add Mojang's <a href="https://github.com/Mojang/DataFixerUpper">DataFixerUpper</a> to your dependencies.
 * @param <T> the type this flag has
 */
public sealed class RegionFlag<T> implements Keyed permits MutableRegionFlag, RegistryRegionFlag {

    protected final Key key;
    protected final Codec<T> codec;

    protected RegionFlag(Key key, Codec<T> codec) {
        this.key = key;
        this.codec = codec;
    }

    public Codec<T> typeCodec() {
        return codec;
    }

    /**
     * Gets the codec for this flag.
     * <p>
     * The parameter provided is the instance of this flag type in the {@link OrbisRegistries#FLAGS} registry.
     * This allows for transformation within the codec to occur.
     * @param registry the registry value of this flag
     * @return the codec
     */
    public MapCodec<? extends RegionFlag<T>> getCodec(RegistryRegionFlag<?> registry) {
        return MapCodec.unit(() -> new RegionFlag<>(key, codec));
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegionFlag<?> that)) return false;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }
}
