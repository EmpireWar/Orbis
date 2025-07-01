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
package org.empirewar.orbis.bukkit;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.bukkit.task.BukkitRegionVisualiserTask;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

public abstract class OrbisBukkitPlatform<P extends JavaPlugin> extends OrbisPlatform
        implements Orbis {

    protected static final NamespacedKey WAND_KEY = NamespacedKey.fromString("orbis:wand");

    protected final P plugin;

    public OrbisBukkitPlatform(P plugin) {
        this.plugin = plugin;
        load();
    }

    public void onEnable() {
        // Start region visualizer
        Bukkit.getScheduler()
                .scheduleSyncRepeatingTask(plugin, new BukkitRegionVisualiserTask(this), 20L, 20L);
    }

    public boolean isWand(ItemStack stack) {
        return stack.getItemMeta() != null
                && stack.getItemMeta().getPersistentDataContainer().has(WAND_KEY);
    }

    public abstract ItemStack wandItem();

    public abstract Audience senderAsAudience(CommandSender player);

    public abstract Key adventureKey(Keyed keyed);

    @Override
    public boolean hasPermission(UUID player, String permission) {
        final Player bukkit = Bukkit.getPlayer(player);
        if (bukkit == null) return false;
        return bukkit.hasPermission(permission);
    }

    @Override
    protected InputStream getResourceAsStream(String path) {
        // Bukkit doesn't like leading slashes
        if (path.charAt(0) == '/') {
            path = path.substring(1);
        }
        return plugin.getResource(path);
    }

    @Override
    public Path dataFolder() {
        return plugin.getDataFolder().toPath();
    }
}
