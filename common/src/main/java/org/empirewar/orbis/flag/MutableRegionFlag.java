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
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.util.ExtraCodecs;

public final class MutableRegionFlag<T> extends RegionFlag<T> {

    private T value;

    MutableRegionFlag(Key key, T defaultValue, Codec<T> codec) {
        super(key, defaultValue, codec);
        this.value = defaultValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public MapCodec<MutableRegionFlag<T>> getCodec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                        ExtraCodecs.KEY.fieldOf("key").forGetter(MutableRegionFlag::key),
                        codec.fieldOf("value").forGetter(MutableRegionFlag::getValue))
                .apply(instance, (key, value) -> new MutableRegionFlag<>(key, value, codec)));
    }
}
