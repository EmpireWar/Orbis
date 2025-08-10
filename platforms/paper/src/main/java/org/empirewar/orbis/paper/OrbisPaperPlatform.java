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
