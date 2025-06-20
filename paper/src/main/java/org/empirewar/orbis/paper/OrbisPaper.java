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
import org.empirewar.orbis.bukkit.listener.*;
import org.empirewar.orbis.bukkit.selection.SelectionListener;
import org.empirewar.orbis.paper.listener.InteractEntityExtensionListener;

import java.io.IOException;

public class OrbisPaper extends JavaPlugin implements Listener {

    private final OrbisPaperPlatform platform = new OrbisPaperPlatform(this);

    @Override
    public void onEnable() {
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
        pluginManager.registerEvents(new InteractEntityExtensionListener(platform), this);
        pluginManager.registerEvents(new MovementListener(platform), this);
        pluginManager.registerEvents(new RegionEnterLeaveListener(platform), this);
        pluginManager.registerEvents(new ConnectionListener(platform), this);
        pluginManager.registerEvents(new SelectionListener(platform), this);
        pluginManager.registerEvents(new EntityDamageListener(platform), this);
    }
}
