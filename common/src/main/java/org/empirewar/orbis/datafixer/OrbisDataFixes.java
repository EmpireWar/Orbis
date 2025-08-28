/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2025 Empire War
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
package org.empirewar.orbis.datafixer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.empirewar.orbis.OrbisAPI;

public final class OrbisDataFixes {

    private OrbisDataFixes() {}

    private static final String VERSION_KEY = "data_version";

    /**
     * Updates the fixer version of an element.
     * @param element the element to set the version on
     * @return the modified element
     */
    public static @Nullable JsonElement updateFixerVersion(@Nullable JsonElement element) {
        if (element == null) return null;
        element.getAsJsonObject().addProperty(VERSION_KEY, Schemas.CURRENT_VERSION);
        return element;
    }

    /**
     * Gets the current fixer version of an element.
     * @param element the element to get the version from
     * @return the version of the element, or the lowest version (1) if it is not set
     */
    public static int getFixerVersion(@NonNull JsonElement element) {
        final JsonObject object = element.getAsJsonObject();
        if (object.has(VERSION_KEY)) {
            return object.get(VERSION_KEY).getAsInt();
        }
        return 1;
    }

    public static JsonElement fix(DSL.TypeReference typeReference, @NonNull JsonObject object) {
        // Is there a version update for this object?
        if (!Schemas.FIXER
                .getSchema(DataFixUtils.makeKey(Schemas.CURRENT_VERSION))
                .types()
                .contains(typeReference.typeName())) {
            OrbisAPI.get().logger().debug("Not updating {}", typeReference.typeName());
            return object;
        }

        final int currentVer = OrbisDataFixes.getFixerVersion(object);
        if (Schemas.CURRENT_VERSION == currentVer) {
            return object;
        }

        OrbisAPI.get()
                .logger()
                .debug(
                        "Updating {} from version {} to version {}",
                        typeReference.typeName(),
                        currentVer,
                        Schemas.CURRENT_VERSION);

        Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, object);
        return Schemas.FIXER
                .update(typeReference, dynamic, currentVer, Schemas.CURRENT_VERSION)
                .getValue();
    }
}
