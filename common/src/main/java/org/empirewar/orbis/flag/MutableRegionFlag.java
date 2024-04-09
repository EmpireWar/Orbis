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
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.registry.Registries;

/**
 * Represents a flag that has a changeable {@link T} value.
 * <p>
 * This flag is added to a {@link org.empirewar.orbis.region.Region}.
 * <p>
 * A {@link RegionFlag} may be converted to a Mutable instance by calling {@link RegionFlag#asMutable()}.
 * @param <T> the type this flag has
 */
public final class MutableRegionFlag<T> extends RegionFlag<T> {

    // This feels a bit abusive of dispatch but it works perfect
    // Map from mutable -> constant region flag in registry
    // (Honestly I'm not entirely sure what dispatch does but basic idea is we are mapping the codec
    // from registry)
    public static final Codec<MutableRegionFlag<?>> CODEC = Registries.FLAGS
            .getCodec()
            .dispatch(mu -> Registries.FLAGS.get(mu.key()).orElseThrow(), r -> r.asMutable()
                    .getCodec(r));

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
    public Codec<MutableRegionFlag<T>> getCodec(RegionFlag<?> registry) {
        return RecordCodecBuilder.create(instance -> instance.group(
                        codec.fieldOf("value").forGetter(MutableRegionFlag::getValue))
                .apply(instance, (value) -> {
                    // spotless:off
                    final MutableRegionFlag<T> mutable = (MutableRegionFlag<T>) registry.asMutable();
                    // spotless:on
                    mutable.setValue(value);
                    return mutable;
                }));
    }
}
