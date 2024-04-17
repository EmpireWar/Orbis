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

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;

import net.kyori.adventure.key.Key;

import java.util.Objects;
import java.util.function.Supplier;

final class RegionFlagBuilder<T> implements RegionFlag.Builder<T> {

    private Key key;
    private Supplier<T> defaultValue;
    private Codec<T> codec;

    RegionFlagBuilder() {}

    @Override
    public RegionFlag<T> build() {
        Preconditions.checkState(this.key != null, "Key cannot be empty");
        Preconditions.checkState(this.defaultValue != null, "Value cannot be empty");
        Preconditions.checkState(this.codec != null, "Value Codec be empty");
        return new RegionFlag<>(key, defaultValue, codec);
    }

    @Override
    public RegionFlag.Builder<T> key(Key key) {
        Objects.requireNonNull(key, "Key cannot be null");
        this.key = key;
        return this;
    }

    @Override
    public RegionFlag.Builder<T> defaultValue(Supplier<T> value) {
        Objects.requireNonNull(value, "Value cannot be null");
        this.defaultValue = value;
        return this;
    }

    @Override
    public RegionFlag.Builder<T> codec(Codec<T> codec) {
        Objects.requireNonNull(codec, "Codec cannot be null");
        this.codec = codec;
        return this;
    }
}
