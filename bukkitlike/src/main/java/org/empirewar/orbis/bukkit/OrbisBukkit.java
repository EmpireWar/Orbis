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
package org.empirewar.orbis.bukkit;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.empirewar.orbis.Orbis;

public interface OrbisBukkit extends Orbis {

    NamespacedKey WAND_KEY = NamespacedKey.fromString("orbis:wand");

    static boolean isWand(ItemStack stack) {
        return stack.getItemMeta() != null
                && stack.getItemMeta().getPersistentDataContainer().has(WAND_KEY);
    }

    ItemStack wandItem();

    Audience senderAsAudience(CommandSender player);

    Key adventureKey(Keyed keyed);
}
