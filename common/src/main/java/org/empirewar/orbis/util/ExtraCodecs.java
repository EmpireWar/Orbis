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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

import org.joml.Vector2i;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.UUID;

public interface ExtraCodecs {

    Codec<Vector3ic> VEC_3I = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("x").forGetter(Vector3ic::x),
                    Codec.INT.fieldOf("y").forGetter(Vector3ic::y),
                    Codec.INT.fieldOf("z").forGetter(Vector3ic::z))
            .apply(instance, Vector3i::new));

    Codec<Vector2i> VEC_2I = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("x").forGetter(Vector2i::x),
                    Codec.INT.fieldOf("y").forGetter(Vector2i::y))
            .apply(instance, Vector2i::new));

    Codec<Key> KEY =
            Codec.STRING.comapFlatMap(ExtraCodecs::validateKey, Key::asString).stable();

    private static DataResult<Key> validateKey(final String id) {
        return DataResult.success(Key.key(id));
    }

    Codec<UUID> STRING_UUID = Codec.STRING.comapFlatMap(
            string -> {
                try {
                    return DataResult.success(UUID.fromString(string), Lifecycle.stable());
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Invalid UUID " + string + ": " + e.getMessage());
                }
            },
            UUID::toString);

    Codec<Component> COMPONENT = Codec.STRING.comapFlatMap(
            json -> DataResult.success(JSONComponentSerializer.json().deserialize(json)),
            component -> JSONComponentSerializer.json().serialize(component));
}
