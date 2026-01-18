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

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.minecraft.flags.MinecraftFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

public class TestOrbisPlatform extends OrbisPlatform {

    private static final Key OVERWORLD = Key.key("orbis", "world");
    private static final UUID OVERWORLD_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    public TestOrbisPlatform() {
        // Load flags
        final RegistryRegionFlag<Boolean> canBreak = MinecraftFlags.CAN_BREAK;
        load();
        try {
            loadRegions();
        } catch (IOException e) {
            logger().error("Error loading regions", e);
        }
        loadWorld(OVERWORLD, OVERWORLD_ID);
    }

    @Override
    protected InputStream getResourceAsStream(String name) {
        // Java doesn't like leading slashes
        if (name.charAt(0) == '/') {
            name = name.substring(1);
        }

        // For testing, load from the classpath
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    @Override
    public Key getPlayerWorld(UUID player) {
        return OVERWORLD;
    }

    @Override
    public boolean hasPermission(UUID player, String permission) {
        return true;
    }

    @Override
    public Path dataFolder() {
        // Use a test-specific directory in the system temp directory
        Path testDir = Path.of(System.getProperty("java.io.tmpdir"), "orbis-test-data");
        try {
            // Create the directory if it doesn't exist
            java.nio.file.Files.createDirectories(testDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test data directory", e);
        }
        return testDir;
    }

    private final Logger logger = LoggerFactory.getLogger("orbis");

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public boolean isTestEnvironment() {
        return true;
    }
}
