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

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.hytale.command.OrbisCommand;
import org.empirewar.orbis.hytale.command.RegionCommand;
import org.empirewar.orbis.hytale.listener.BlockBreakListener;
import org.empirewar.orbis.hytale.listener.BlockPlaceListener;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

public class OrbisHytale extends JavaPlugin {

    private final OrbisHytalePlatform platform;

    public OrbisHytale(@NonNull JavaPluginInit init) {
        super(init);
        this.platform = new OrbisHytalePlatform(this);
    }

    @Override
    protected void setup() {
        this.registerListeners();
        this.registerCommands();

        try {
            platform.loadRegions();
        } catch (IOException e) {
            platform.logger().error("Error loading regions", e);
        }
    }

    private void registerListeners() {
        getEntityStoreRegistry().registerSystem(new BlockPlaceListener());
        getEntityStoreRegistry().registerSystem(new BlockBreakListener());

        this.getEventRegistry().registerGlobal(AddWorldEvent.class, event -> {
            final World world = event.getWorld();
            platform.loadWorld(
                    Key.key("hytale", world.getName()), world.getWorldConfig().getUuid());
        });

        this.getEventRegistry().registerGlobal(RemoveWorldEvent.class, event -> {
            final World world = event.getWorld();
            platform.saveWorld(
                    Key.key("hytale", world.getName()), world.getWorldConfig().getUuid());
        });
    }

    private void registerCommands() {
        this.getCommandRegistry().registerCommand(new OrbisCommand());
        this.getCommandRegistry().registerCommand(new RegionCommand());
    }

    @Override
    protected void start() {
        Universe.get().getWorlds().forEach((name, world) -> {
            final Key worldKey = Key.key("hytale", name);
            if (platform.getRegionisedWorld(worldKey) != null) {
                platform.logger()
                        .warn(
                                "Skipping world {} because it was already loaded. Reload?",
                                worldKey.asString());
                return;
            }
            platform.loadWorld(worldKey, world.getWorldConfig().getUuid());
        });
    }

    @Override
    protected void shutdown() {
        try {
            platform.saveRegions();
        } catch (IOException e) {
            platform.logger().error("Error saving regions", e);
        }
    }
}
