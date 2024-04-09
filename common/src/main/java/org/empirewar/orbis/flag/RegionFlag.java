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
import com.mojang.serialization.MapCodec;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

import org.empirewar.orbis.registry.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents the flag of a {@link org.empirewar.orbis.region.Region}.
 * <p>
 * All flags are {@link Keyed} and thus have a {@link Key} to identify them.
 * <p>
 * To add a region flag, you should use the {@link #builder()} and register it to {@link Registries#FLAGS}.
 * @param <T> the type this flag has
 */
public sealed class RegionFlag<T> implements Keyed permits MutableRegionFlag {

    protected final Key key;
    protected final T defaultValue;
    protected final Codec<T> codec;

    protected RegionFlag(Key key, T defaultValue, Codec<T> codec) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.codec = codec;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public MutableRegionFlag<T> asMutable() {
        return new MutableRegionFlag<>(key, defaultValue, codec);
    }

    public MapCodec<? extends RegionFlag<T>> getCodec() {
        return MapCodec.unit(() -> new RegionFlag<>(key, defaultValue, codec));
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

        Builder<T> defaultValue(T value);

        Builder<T> codec(Codec<T> codec);

        RegionFlag<T> build();
    }
}
