/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 EmpireWar
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
package org.empirewar.orbis.flag;

import com.mojang.serialization.Codec;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

import org.empirewar.orbis.registry.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents the flag of a {@link org.empirewar.orbis.region.Region}.
 * <p>
 * All flags are {@link Keyed} and thus have a {@link Key} to identify them.
 * <p>
 * To add a region flag, you should use the {@link #builder()} and register it to {@link Registries#FLAGS}.
 * <p>
 * Note that all flags must provide a {@link Codec} of {@link T}.
 * This allows for deserialization and serialization and is also used in command parsing.
 * <p>
 * You may need to add Mojang's <a href="https://github.com/Mojang/DataFixerUpper">DataFixerUpper</a> to your dependencies.
 * <p>
 * This base class is immutable and is stored in the {@link Registries#FLAGS} registry.
 * A flag may be converted to a mutable representation by using {@link #asMutable()} or {@link #asGrouped()} as required.
 * The provided {@link Supplier} of {@link T} will be used to copy a value into the mutable representation.
 * @param <T> the type this flag has
 */
public sealed class RegionFlag<T> implements Keyed permits MutableRegionFlag {

    protected final Key key;
    protected final Supplier<T> defaultValueSupplier;
    protected final T defaultValue;
    protected final Codec<T> codec;

    protected RegionFlag(Key key, Supplier<T> defaultValue, Codec<T> codec) {
        this.key = key;
        this.defaultValueSupplier = defaultValue;
        this.defaultValue = defaultValue.get();
        this.codec = codec;
    }

    public Codec<T> typeCodec() {
        return codec;
    }

    public T getDefaultValue() {
        return defaultValue;
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
     * Gets the codec for this flag.
     * <p>
     * The parameter provided is the instance of this flag type in the {@link Registries#FLAGS} registry.
     * This allows for transformation within the codec to occur.
     * @param registry the registry value of this flag
     * @return the codec
     */
    public Codec<? extends RegionFlag<T>> getCodec(RegionFlag<?> registry) {
        return Codec.unit(() -> new RegionFlag<>(key, defaultValueSupplier, codec));
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

    public static <T> Builder<T> builder() {
        return new RegionFlagBuilder<>();
    }

    public sealed interface Builder<T> permits RegionFlagBuilder {

        Builder<T> key(Key key);

        Builder<T> defaultValue(Supplier<T> value);

        Builder<T> codec(Codec<T> codec);

        RegionFlag<T> build();
    }
}
