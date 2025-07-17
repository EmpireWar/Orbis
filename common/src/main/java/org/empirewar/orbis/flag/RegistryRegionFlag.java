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
package org.empirewar.orbis.flag;

import com.mojang.serialization.Codec;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.registry.OrbisRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * This base class is immutable and is stored in the {@link OrbisRegistries#FLAGS} registry.
 * A flag may be converted to a mutable representation by using {@link #asMutable()} or {@link #asGrouped()} as required.
 * The provided {@link Supplier} of {@link T} will be used to copy a value into the mutable representation.
 * @param <T> the type this flag has
 * @see RegionFlag
 */
public final class RegistryRegionFlag<T> extends RegionFlag<T> {

    private final Supplier<T> defaultValueSupplier;
    private final Class<T> defaultValueType;
    private final @Nullable String description;

    RegistryRegionFlag(
            Key key, @Nullable String description, Supplier<T> defaultValue, Codec<T> codec) {
        super(key, codec);
        this.defaultValueSupplier = defaultValue;
        this.defaultValueType = (Class<T>) defaultValue.get().getClass();
        this.description = description;
    }

    /**
     * Gets the class of the default value of this flag.
     * @return class of the default value
     */
    public Class<T> defaultValueType() {
        return defaultValueType;
    }

    /**
     * Gets a mutable representation of this flag.
     * @return mutable representation
     */
    public MutableRegionFlag<T> asMutable() {
        return new MutableRegionFlag<>(key, defaultValueSupplier, codec);
    }

    /**
     * Gets a grouped mutable representation of this flag.
     * @return grouped mutable representation
     */
    public GroupedMutableRegionFlag<T> asGrouped() {
        return new GroupedMutableRegionFlag<>(key, defaultValueSupplier, codec);
    }

    /**
     * Gets the description of this flag.
     * <p>
     * This description is displayed as a tooltip when hovering over the flag name in chat or command suggestions.
     * @return the description
     */
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public static <T> Builder<T> builder() {
        return new RegionFlagBuilder<>();
    }

    public sealed interface Builder<T> permits RegionFlagBuilder {

        Builder<T> key(Key key);

        Builder<T> description(@Nullable String description);

        Builder<T> defaultValue(Supplier<T> value);

        Builder<T> codec(Codec<T> codec);

        RegistryRegionFlag<T> build();
    }
}
