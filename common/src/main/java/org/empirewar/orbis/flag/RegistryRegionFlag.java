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
