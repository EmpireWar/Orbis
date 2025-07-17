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
package org.empirewar.orbis.util;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.PrimitiveCodec;

import java.util.Optional;

public record EnumCodec<E extends Enum<E>>(Class<E> clazz) implements PrimitiveCodec<E> {

    @Override
    public <T> DataResult<E> read(DynamicOps<T> dynamicOps, T t) {
        DataResult<String> res = dynamicOps.getStringValue(t);
        final Optional<String> result = res.result();
        if (res.error().isPresent() || result.isEmpty())
            return DataResult.error(
                    () -> "Unable to parse EnumCodec for \"" + clazz.getSimpleName() + "\": "
                            + (res.error().isPresent()
                                    ? res.error().get().message()
                                    : "(no error)"),
                    Lifecycle.stable());
        Optional<E> value = Optional.empty();
        for (E enumConstant : clazz.getEnumConstants()) {
            if (enumConstant.name().equals(result.get())) {
                value = Optional.of(enumConstant);
                break;
            }
        }
        return value.map(DataResult::success)
                .orElseGet(() -> DataResult.error(
                        () -> "Unable to parse EnumCodec for \"" + clazz.getSimpleName()
                                + "\": Unknown enum value \"" + result.get() + "\"",
                        Lifecycle.stable()));
    }

    @Override
    public <T> T write(DynamicOps<T> dynamicOps, E e) {
        return dynamicOps.createString(e.name());
    }

    static <E extends Enum<E>> DataResult<E> validate(String name, E[] enumValues) {
        for (E e : enumValues) {
            if (e.name().equals(name)) {
                return DataResult.success(e);
            }
        }
        return DataResult.error(
                () -> String.format("\"%s\" has no enum value \"%s\"!", "", name),
                Lifecycle.stable());
    }
}
