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

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;

import net.kyori.adventure.key.Key;

import java.util.Objects;
import java.util.function.Supplier;

final class RegionFlagBuilder<T> implements RegistryRegionFlag.Builder<T> {

    private Key key;
    private String description;
    private Supplier<T> defaultValue;
    private Codec<T> codec;

    RegionFlagBuilder() {}

    @Override
    public RegistryRegionFlag<T> build() {
        Preconditions.checkState(this.key != null, "Key cannot be empty");
        Preconditions.checkState(this.defaultValue != null, "Value cannot be empty");
        Preconditions.checkState(this.codec != null, "Value Codec be empty");
        return new RegistryRegionFlag<>(key, description, defaultValue, codec);
    }

    @Override
    public RegistryRegionFlag.Builder<T> key(Key key) {
        Objects.requireNonNull(key, "Key cannot be null");
        this.key = key;
        return this;
    }

    @Override
    public RegistryRegionFlag.Builder<T> description(String description) {
        Objects.requireNonNull(description, "Description cannot be null");
        this.description = description;
        return this;
    }

    @Override
    public RegistryRegionFlag.Builder<T> defaultValue(Supplier<T> value) {
        Objects.requireNonNull(value, "Value cannot be null");
        this.defaultValue = value;
        return this;
    }

    @Override
    public RegistryRegionFlag.Builder<T> codec(Codec<T> codec) {
        Objects.requireNonNull(codec, "Codec cannot be null");
        this.codec = codec;
        return this;
    }
}
