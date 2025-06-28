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
package org.empirewar.orbis.spigot;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.empirewar.orbis.bukkit.command.BukkitCommands;
import org.empirewar.orbis.bukkit.listener.BlockActionListener;
import org.empirewar.orbis.bukkit.listener.ConnectionListener;
import org.empirewar.orbis.bukkit.listener.MovementListener;
import org.empirewar.orbis.bukkit.listener.RegionEnterLeaveListener;
import org.empirewar.orbis.bukkit.selection.SelectionListener;
import org.empirewar.orbis.bukkit.session.BukkitPlayerSession;
import org.empirewar.orbis.player.ConsoleOrbisSession;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.spigot.listener.InteractEntityExtensionListener;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.io.IOException;

public class OrbisSpigot extends JavaPlugin implements Listener {

    private final OrbisSpigotPlatform platform = new OrbisSpigotPlatform(this);

    private BukkitAudiences adventure;

    public @NonNull BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException(
                    "Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        this.registerListeners();

        LegacyPaperCommandManager<OrbisSession> manager = new LegacyPaperCommandManager<>(
                this, /* 1 */
                ExecutionCoordinator.asyncCoordinator(), /* 2 */
                SenderMapper.create(
                        sender -> {
                            if (sender instanceof Player player) {
                                return new BukkitPlayerSession(player);
                            }
                            return new ConsoleOrbisSession(adventure.sender(sender));
                        },
                        session -> {
                            if (session instanceof BukkitPlayerSession playerSession) {
                                return playerSession.getPlayer();
                            }
                            return Bukkit.getConsoleSender();
                        }) /* 3 */);
        new BukkitCommands<>(manager);

        try {
            platform.loadRegions();
        } catch (IOException e) {
            platform.logger().error("Error loading regions", e);
        }
        Bukkit.getWorlds().forEach(w -> platform.loadWorld(platform.worldToKey(w), w.getUID()));
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        try {
            platform.saveRegions();
        } catch (IOException e) {
            platform.logger().error("Error saving regions", e);
        }
    }

    @EventHandler
    public void onLoad(WorldLoadEvent event) {
        final World world = event.getWorld();
        platform.loadWorld(platform.worldToKey(world), world.getUID());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUnload(WorldUnloadEvent event) {
        final World world = event.getWorld();
        final Key key = platform.worldToKey(world);
        platform.saveWorld(key, world.getUID());
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
    }
}
