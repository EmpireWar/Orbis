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
import org.empirewar.orbis.registry.OrbisRegistries;

import java.lang.reflect.Type;
import java.util.Optional;

public final class RegionAdapter implements JsonSerializer<Region>, JsonDeserializer<Region> {

    @Override
    public JsonElement serialize(Region region, Type typeOfSrc, JsonSerializationContext context) {
        final Codec<Region> dispatch =
                OrbisRegistries.REGION_TYPE.getCodec().dispatch(Region::getType, RegionType::codec);
        final Optional<JsonElement> result = dispatch.encodeStart(JsonOps.INSTANCE, region)
                .resultOrPartial(
                        msg -> OrbisAPI.get().logger().error("Error saving region: {}", msg));
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
                OrbisRegistries.REGION_TYPE.getCodec().dispatch(Region::getType, RegionType::codec);
        final Optional<Region> result = dispatch.parse(JsonOps.INSTANCE, fixed)
                .resultOrPartial(
                        msg -> OrbisAPI.get().logger().error("Error loading region: {}", msg));
        return result.orElse(null);
    }
}
