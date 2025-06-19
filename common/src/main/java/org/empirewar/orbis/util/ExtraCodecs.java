/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 Empire War
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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.UUID;

public interface ExtraCodecs {

    Codec<Vector3i> VEC_3I = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("x").forGetter(Vector3i::x),
                    Codec.INT.fieldOf("y").forGetter(Vector3i::y),
                    Codec.INT.fieldOf("z").forGetter(Vector3i::z))
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
