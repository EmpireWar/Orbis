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
