/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2026 Empire War
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
package org.empirewar.orbis.hytale;

import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.hytale.logging.HytaleSlf4jLogger;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

public class OrbisHytalePlatform extends OrbisPlatform {

    private final OrbisHytale plugin;
    private final Logger logger;

    OrbisHytalePlatform(OrbisHytale plugin) {
        this.plugin = plugin;
        this.logger = new HytaleSlf4jLogger("Orbis", plugin.getLogger());
        load();
    }

    @Override
    protected InputStream getResourceAsStream(String path) {
        // Java doesn't like leading slashes
        if (path.charAt(0) == '/') {
            path = path.substring(1);
        }

        return plugin.getClassLoader().getResourceAsStream(path);
    }

    @Override
    public Key getPlayerWorld(UUID player) {
        final Universe universe = Universe.get();
        final PlayerRef playerRef = universe.getPlayer(player);
        final World world = universe.getWorld(playerRef.getWorldUuid());
        final String name = world.getName();
        return Key.key("hytale", name);
    }

    @Override
    public boolean hasPermission(UUID player, String permission) {
        return PermissionsModule.get().hasPermission(player, permission);
    }

    @Override
    public Path dataFolder() {
        return plugin.getDataDirectory();
    }

    @Override
    public Logger logger() {
        return logger;
    }
}
