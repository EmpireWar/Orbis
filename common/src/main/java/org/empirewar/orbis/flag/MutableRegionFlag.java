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
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.registry.OrbisRegistries;

import java.util.function.Supplier;

/**
 * Represents a flag that has a changeable {@link T} value.
 * <p>
 * This flag is added to a {@link org.empirewar.orbis.region.Region}.
 * <p>
 * A {@link RegistryRegionFlag} may be converted to a Mutable instance by calling {@link RegistryRegionFlag#asMutable()}.
 * @param <T> the type this flag has
 * @see RegionFlag
 */
public sealed class MutableRegionFlag<T> extends RegionFlag<T> permits GroupedMutableRegionFlag {

    // This feels a bit abusive of dispatch but it works perfect
    // Map from mutable -> constant region flag in registry
    // (Honestly I'm not entirely sure what dispatch does but basic idea is we are mapping the codec
    // from registry)
    public static final MapCodec<MutableRegionFlag<?>> CODEC = OrbisRegistries.FLAGS
            .getCodec()
            .dispatchMap(mu -> OrbisRegistries.FLAGS.get(mu.key()).orElseThrow(), r -> r.asMutable()
                    .getCodec(r));

    public static final Codec<MutableRegionFlag<?>> TYPE_CODEC = OrbisRegistries.FLAG_TYPE
            .getCodec()
            .dispatch("region_flag_type", MutableRegionFlag::getType, RegionFlagType::codec);

    private T value;

    MutableRegionFlag(Key key, Supplier<T> defaultValue, Codec<T> codec) {
        super(key, codec);
        this.value = defaultValue.get();
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public RegionFlagType<?> getType() {
        return RegionFlagType.MUTABLE;
    }

    @Override
    public MapCodec<? extends MutableRegionFlag<T>> getCodec(RegistryRegionFlag<?> registry) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
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
