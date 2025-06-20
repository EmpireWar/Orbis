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
package org.empirewar.orbis.paper;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.empirewar.orbis.bukkit.OrbisBukkitPlatform;
import org.empirewar.orbis.selection.Selection;
import org.slf4j.Logger;

import java.util.UUID;

public final class OrbisPaperPlatform extends OrbisBukkitPlatform<OrbisPaper> {

    public static final ItemStack WAND_ITEM;

    static {
        WAND_ITEM = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = WAND_ITEM.getItemMeta();
        meta.displayName(Selection.WAND_NAME);
        meta.lore(Selection.WAND_LORE);
        meta.getPersistentDataContainer().set(WAND_KEY, PersistentDataType.BOOLEAN, true);
        WAND_ITEM.setItemMeta(meta);
    }

    OrbisPaperPlatform(OrbisPaper plugin) {
        super(plugin);
    }

    @Override
    public ItemStack wandItem() {
        return WAND_ITEM;
    }

    @Override
    public Audience senderAsAudience(CommandSender player) {
        return player;
    }

    @Override
    public Key adventureKey(Keyed keyed) {
        return keyed.key();
    }

    @Override
    public Key getPlayerWorld(UUID player) {
        return Bukkit.getPlayer(player).getWorld().key();
    }

    @Override
    public Logger logger() {
        return plugin.getSLF4JLogger();
    }
}
