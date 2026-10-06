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
package org.empirewar.orbis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;

import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.minecraft.flags.MinecraftFlags;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.serialization.StaticGsonProvider;
import org.joml.Vector3i;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * A region file that cannot be read must not stop the others from loading. Regions missing from the
 * registry get pruned out of {@code worlds.yml} on the next save, so an aborted load loop turns a
 * single corrupt file into silent, permanent data loss across every region after it.
 */
public class OrbisPlatformLoadRegionsTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void cleanup() {
        OrbisAPI.reset();
    }

    // OrbisRegistries.REGIONS is static and shared across test classes, so names are unique.
    private void writeRegion(String name) throws IOException {
        final CuboidArea area = new CuboidArea();
        area.addPoint(new Vector3i(0, 0, 0));
        area.addPoint(new Vector3i(4, 4, 4));
        final Region region = new Region(name, area);
        region.addFlag(MinecraftFlags.CAN_BREAK);

        final Path regions = Files.createDirectories(tempDir.resolve("regions"));
        Files.writeString(
                regions.resolve(name + ".json"),
                StaticGsonProvider.GSON.toJson(region),
                StandardCharsets.UTF_8);
    }

    private void writeRaw(String fileName, String contents) throws IOException {
        final Path regions = Files.createDirectories(tempDir.resolve("regions"));
        Files.writeString(regions.resolve(fileName), contents, StandardCharsets.UTF_8);
    }

    @Test
    void corruptRegionFileDoesNotAbortTheLoadLoop() throws IOException {
        writeRegion("load-aaa-good");
        // Exactly the artefact the old RegionAdapter produced when encoding failed.
        writeRaw("load-bbb-broken.json", "null");
        writeRegion("load-ccc-good");

        new TestOrbisPlatform(tempDir);

        assertTrue(OrbisRegistries.REGIONS.get("load-aaa-good").isPresent());
        assertTrue(
                OrbisRegistries.REGIONS.get("load-ccc-good").isPresent(),
                "a region after the corrupt file should still load");
        assertFalse(OrbisRegistries.REGIONS.get("load-bbb-broken").isPresent());
    }

    @Test
    void unparseableJsonDoesNotAbortTheLoadLoop() throws IOException {
        writeRegion("load2-aaa-good");
        writeRaw("load2-bbb-broken.json", "{");
        writeRegion("load2-ccc-good");

        new TestOrbisPlatform(tempDir);

        assertTrue(OrbisRegistries.REGIONS.get("load2-aaa-good").isPresent());
        assertTrue(
                OrbisRegistries.REGIONS.get("load2-ccc-good").isPresent(),
                "a region after the unparseable file should still load");
    }

    @Test
    void saveRegionsLeavesNoTempFiles() throws IOException {
        writeRegion("load3-saved");

        final TestOrbisPlatform platform = new TestOrbisPlatform(tempDir);
        platform.saveRegions();

        try (Stream<Path> files = Files.list(tempDir.resolve("regions"))) {
            final List<Path> written = files.toList();
            for (Path file : written) {
                final String fileName = file.getFileName().toString();
                assertFalse(fileName.endsWith(".tmp"), "left a temp file behind: " + fileName);
                assertNotNull(
                        JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)));
            }
            assertTrue(written.stream()
                    .anyMatch(file -> file.getFileName().toString().equals("load3-saved.json")));
        }
    }
}
