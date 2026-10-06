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
package org.empirewar.orbis.serialization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.TestOrbisPlatform;
import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.minecraft.flags.MinecraftFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.joml.Vector3i;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Covers the round-trip guarantee that a region flag this build cannot understand - typically one
 * owned by a plugin that failed to load - is preserved verbatim rather than wiped on the next save.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegionSerialisationTest {

    private static final String UNKNOWN_KEY = "otherplugin:custom";

    @SuppressWarnings("unused")
    private TestOrbisPlatform platform;

    @BeforeAll
    void setupPlatform() {
        platform = new TestOrbisPlatform();
    }

    @AfterAll
    static void cleanup() {
        OrbisAPI.reset();
    }

    // OrbisRegistries.REGIONS is static and shared across test classes, so names are prefixed.
    private static Region newRegion(String name) {
        final CuboidArea area = new CuboidArea();
        area.addPoint(new Vector3i(0, 0, 0));
        area.addPoint(new Vector3i(4, 4, 4));
        return new Region("serialise-" + name, area);
    }

    private static JsonObject flagEntry(String type, String regionFlagType, JsonElement value) {
        final JsonObject entry = new JsonObject();
        entry.add("value", value);
        entry.addProperty("type", type);
        entry.addProperty("region_flag_type", regionFlagType);
        return entry;
    }

    private static JsonObject unknownEntry() {
        return flagEntry(UNKNOWN_KEY, "orbis:mutable", new JsonPrimitive(42));
    }

    private static JsonObject toJson(Region region) {
        return StaticGsonProvider.GSON.toJsonTree(region).getAsJsonObject();
    }

    private static Region fromJson(JsonObject json) {
        return StaticGsonProvider.GSON.fromJson(json, Region.class);
    }

    /** Compares as a set - {@code Region.flags} iterates a HashMap, so array order is arbitrary. */
    private static Set<JsonElement> flagSet(JsonObject region) {
        final Set<JsonElement> set = new HashSet<>();
        region.getAsJsonArray("flags").forEach(set::add);
        return set;
    }

    private static JsonObject withUnknownFlag(Region region) {
        final JsonObject json = toJson(region);
        json.getAsJsonArray("flags").add(unknownEntry());
        return json;
    }

    @Test
    void unknownFlagSurvivesLoadSaveLoad() {
        final Region region = newRegion("survives");
        region.addFlag(MinecraftFlags.CAN_BREAK);
        region.setFlag(MinecraftFlags.CAN_BREAK, false);

        final Region loaded = fromJson(withUnknownFlag(region));
        assertNotNull(loaded, "region with an unrecognised flag should still load");

        // The known flag is untouched and still queryable.
        assertTrue(loaded.hasFlag(MinecraftFlags.CAN_BREAK));
        final Optional<Boolean> queried = loaded.query(RegionQuery.Flag.<Boolean>builder()
                        .flag(MinecraftFlags.CAN_BREAK)
                        .build())
                .result();
        assertTrue(queried.isPresent());
        assertFalse(queried.get());

        assertEquals(Set.of(UNKNOWN_KEY), loaded.unknownFlagKeys());

        // The unrecognised entry is written back out byte-for-byte.
        final JsonObject resaved = toJson(loaded);
        assertTrue(
                flagSet(resaved).contains(unknownEntry()),
                "unrecognised flag entry should be re-emitted unchanged");

        // A further cycle must be stable - no accumulation, no mutation.
        final JsonObject thirdPass = toJson(fromJson(resaved));
        assertEquals(flagSet(resaved), flagSet(thirdPass));
    }

    @Test
    void retainedEntryNotLostWhenOtherFlagsAreEdited() {
        final Region region = newRegion("edits");
        region.addFlag(MinecraftFlags.CAN_BREAK);

        final Region loaded = fromJson(withUnknownFlag(region));
        assertNotNull(loaded);

        loaded.setFlag(MinecraftFlags.CAN_BREAK, true);
        loaded.addFlag(MinecraftFlags.CAN_PLACE);
        loaded.removeFlag(MinecraftFlags.CAN_BREAK);

        final JsonObject resaved = toJson(loaded);
        assertTrue(flagSet(resaved).contains(unknownEntry()));

        final Region reloaded = fromJson(resaved);
        assertNotNull(reloaded);
        assertTrue(reloaded.hasFlag(MinecraftFlags.CAN_PLACE));
        assertFalse(reloaded.hasFlag(MinecraftFlags.CAN_BREAK));
        assertEquals(Set.of(UNKNOWN_KEY), reloaded.unknownFlagKeys());
    }

    @Test
    void mixedKnownAndMultipleUnknownFlags() {
        final Region region = newRegion("mixed");
        region.addFlag(MinecraftFlags.CAN_BREAK);
        region.addFlag(MinecraftFlags.CAN_PLACE);

        final JsonObject json = toJson(region);
        final JsonObject first =
                flagEntry("pluginone:alpha", "orbis:mutable", new JsonPrimitive(1));
        final JsonObject second =
                flagEntry("plugintwo:beta", "orbis:mutable", new JsonPrimitive("hello"));
        json.getAsJsonArray("flags").add(first);
        json.getAsJsonArray("flags").add(second);

        final Region loaded = fromJson(json);
        assertNotNull(loaded);
        assertEquals(Set.of("pluginone:alpha", "plugintwo:beta"), loaded.unknownFlagKeys());

        final Set<JsonElement> resaved = flagSet(toJson(loaded));
        assertEquals(4, resaved.size());
        assertTrue(resaved.contains(first));
        assertTrue(resaved.contains(second));
    }

    @Test
    void groupedUnknownFlagIsRetainedWithGroups() {
        final Region region = newRegion("grouped");
        region.addFlag(MinecraftFlags.CAN_BREAK);

        // A grouped entry carries an extra "groups" array. Retention happens above the
        // RegionFlagType dispatch layer, so the whole entry survives including that array.
        final JsonObject grouped =
                flagEntry("otherplugin:grouped", "orbis:grouped_mutable", new JsonPrimitive(true));
        final JsonArray groups = new JsonArray();
        groups.add("MEMBER");
        grouped.add("groups", groups);

        final JsonObject json = toJson(region);
        json.getAsJsonArray("flags").add(grouped);

        final Region loaded = fromJson(json);
        assertNotNull(loaded);
        assertEquals(Set.of("otherplugin:grouped"), loaded.unknownFlagKeys());
        assertTrue(flagSet(toJson(loaded)).contains(grouped));
    }

    @Test
    void unknownRegionFlagTypeIsRetained() {
        final Region region = newRegion("unknown-type");
        region.addFlag(MinecraftFlags.CAN_BREAK);

        // FLAG_TYPE is frozen too, so an unrecognised region_flag_type fails at the outer dispatch.
        final JsonObject weird =
                flagEntry("otherplugin:x", "otherplugin:weird", new JsonPrimitive(true));
        final JsonObject json = toJson(region);
        json.getAsJsonArray("flags").add(weird);

        final Region loaded = fromJson(json);
        assertNotNull(loaded);
        assertEquals(Set.of("otherplugin:x"), loaded.unknownFlagKeys());
        assertTrue(flagSet(toJson(loaded)).contains(weird));
    }

    @Test
    void malformedFlagKeyIsRetainedNotThrown() {
        final Region region = newRegion("malformed");
        region.addFlag(MinecraftFlags.CAN_BREAK);

        // Key.key throws rather than erroring; without the guards this takes out the whole region.
        final JsonObject malformed =
                flagEntry("NOT A KEY!!", "orbis:mutable", new JsonPrimitive(true));
        final JsonObject json = toJson(region);
        json.getAsJsonArray("flags").add(malformed);

        final Region loaded = assertDoesNotThrow(() -> fromJson(json));
        assertNotNull(loaded);
        assertEquals(Set.of("NOT A KEY!!"), loaded.unknownFlagKeys());
        assertTrue(flagSet(toJson(loaded)).contains(malformed));
    }

    @Test
    void globalRegionRetainsUnknownFlags() {
        final GlobalRegion region = new GlobalRegion("serialise-global");
        region.addFlag(MinecraftFlags.CAN_BREAK);

        final JsonObject json = toJson(region);
        assertEquals("orbis:global", json.get("type").getAsString());
        json.getAsJsonArray("flags").add(unknownEntry());

        final Region loaded = fromJson(json);
        assertNotNull(loaded);
        assertTrue(loaded.isGlobal());
        assertEquals(Set.of(UNKNOWN_KEY), loaded.unknownFlagKeys());
        assertTrue(flagSet(toJson(loaded)).contains(unknownEntry()));
    }

    @Test
    void emptyFlagsListStillRoundTrips() {
        final Region region = newRegion("empty");

        final JsonObject json = toJson(region);
        assertEquals(0, json.getAsJsonArray("flags").size());

        final Region loaded = fromJson(json);
        assertNotNull(loaded);
        assertTrue(loaded.unknownFlagKeys().isEmpty());
        assertEquals(0, toJson(loaded).getAsJsonArray("flags").size());
    }
}
