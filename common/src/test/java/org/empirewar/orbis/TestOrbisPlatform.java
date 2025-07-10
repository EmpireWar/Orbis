/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2025 Empire War
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
package org.empirewar.orbis;

import net.kyori.adventure.key.Key;

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
}
