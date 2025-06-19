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
package org.empirewar.orbis.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.region.RegionType;
import org.empirewar.orbis.registry.Registries;

import java.lang.reflect.Type;
import java.util.Optional;

public final class RegionAdapter implements JsonSerializer<Region>, JsonDeserializer<Region> {

    @Override
    public JsonElement serialize(Region region, Type typeOfSrc, JsonSerializationContext context) {
        final Codec<Region> dispatch =
                Registries.REGION_TYPE.getCodec().dispatch(Region::getType, RegionType::codec);
        final Optional<JsonElement> result = dispatch.encodeStart(JsonOps.INSTANCE, region)
                .resultOrPartial(OrbisAPI.get().logger()::error);
        return result.orElse(null);
    }

    @Override
    public Region deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject object = json.getAsJsonObject();
        Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, object);
        JsonElement fixed;
        // TODO datafixer
        fixed = dynamic.getValue();

        final Codec<Region> dispatch =
                Registries.REGION_TYPE.getCodec().dispatch(Region::getType, RegionType::codec);
        final Optional<Region> result = dispatch.parse(JsonOps.INSTANCE, fixed)
                .resultOrPartial(OrbisAPI.get().logger()::error);
        return result.orElse(null);
    }
}
