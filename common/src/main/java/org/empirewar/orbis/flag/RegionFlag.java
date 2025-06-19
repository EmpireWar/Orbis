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
 * To add a region flag, you should use the {@link RegistryRegionFlag#builder()} and register it to {@link Registries#FLAGS}.
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
     * The parameter provided is the instance of this flag type in the {@link Registries#FLAGS} registry.
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
