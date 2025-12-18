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
package org.empirewar.orbis.paper;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.empirewar.orbis.paper.listener.BlockActionListener;
import org.empirewar.orbis.paper.listener.ConnectionListener;
import org.empirewar.orbis.paper.listener.EntityListener;
import org.empirewar.orbis.paper.listener.MovementListener;
import org.empirewar.orbis.paper.listener.SelectionListener;

import java.io.IOException;

public class OrbisPaper extends JavaPlugin implements Listener {

    private final OrbisPaperPlatform<OrbisPaper> platform = new OrbisPaperPlatform<>(this);

    @Override
    public void onEnable() {
        platform.onEnable();
        this.registerListeners();
        try {
            platform.loadRegions();
        } catch (IOException e) {
            platform.logger().error("Error loading regions", e);
        }
        Bukkit.getWorlds().forEach(w -> platform.loadWorld(w.key(), w.getUID()));
    }

    @Override
    public void onDisable() {
        try {
            platform.saveRegions();
        } catch (IOException e) {
            platform.logger().error("Error saving regions", e);
        }
    }

    @EventHandler
    public void onLoad(WorldLoadEvent event) {
        final World world = event.getWorld();
        platform.loadWorld(world.key(), world.getUID());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUnload(WorldUnloadEvent event) {
        final World world = event.getWorld();
        platform.saveWorld(world.key(), world.getUID());
    }

    private void registerListeners() {
        final PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(new BlockActionListener(platform), this);
        pluginManager.registerEvents(new EntityListener(platform), this);
        pluginManager.registerEvents(new MovementListener(platform), this);
        pluginManager.registerEvents(new ConnectionListener(platform), this);
        pluginManager.registerEvents(new SelectionListener(platform), this);
    }
}
