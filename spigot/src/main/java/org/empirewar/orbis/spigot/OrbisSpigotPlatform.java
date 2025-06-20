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
package org.empirewar.orbis.spigot;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.empirewar.orbis.bukkit.OrbisBukkitPlatform;
import org.empirewar.orbis.selection.Selection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class OrbisSpigotPlatform extends OrbisBukkitPlatform<OrbisSpigot> {

    public static final ItemStack WAND_ITEM;

    static {
        WAND_ITEM = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = WAND_ITEM.getItemMeta();
        final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
        meta.setDisplayName(legacy.serialize(Selection.WAND_NAME));
        meta.setLore(Selection.WAND_LORE.stream().map(legacy::serialize).toList());
        meta.getPersistentDataContainer().set(WAND_KEY, PersistentDataType.BOOLEAN, true);
        WAND_ITEM.setItemMeta(meta);
    }

    OrbisSpigotPlatform(OrbisSpigot plugin) {
        super(plugin);
    }

    public Key worldToKey(World world) {
        return Key.key(world.getKey().toString());
    }

    @Override
    public ItemStack wandItem() {
        return WAND_ITEM;
    }

    @Override
    public Audience senderAsAudience(CommandSender player) {
        return plugin.adventure().sender(player);
    }

    @Override
    public Key adventureKey(Keyed keyed) {
        return Key.key(keyed.getKey().toString());
    }

    @Override
    public Key getPlayerWorld(UUID player) {
        return worldToKey(Bukkit.getPlayer(player).getWorld());
    }

    private final Logger logger = LoggerFactory.getLogger("orbis");

    @Override
    public Logger logger() {
        return logger;
    }
}
