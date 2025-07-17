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
