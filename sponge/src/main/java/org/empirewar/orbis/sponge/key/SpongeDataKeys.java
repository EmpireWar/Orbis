/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 EmpireWar
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
package org.empirewar.orbis.sponge.key;

import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.sponge.OrbisSponge;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

// https://github.com/SpongePowered/Sponge/blob/api-11/testplugins/src/main/java/org/spongepowered/test/customdata/CustomDataTest.java
public final class SpongeDataKeys {

    public static final Key<Value<Boolean>> IS_WAND =
            Key.from(OrbisSponge.get().pluginContainer(), "wand", Boolean.class);

    public static ItemStack WAND_ITEM;

    public static void register(RegisterDataEvent event) {
        event.register(DataRegistration.of(IS_WAND, ItemStack.class));
    }

    public static void initialized() {
        WAND_ITEM = ItemStack.builder().itemType(ItemTypes.BLAZE_ROD).build();
        WAND_ITEM.offer(IS_WAND, true);
        WAND_ITEM.offer(Keys.DISPLAY_NAME, Selection.WAND_NAME);
        WAND_ITEM.offerAll(Keys.LORE, Selection.WAND_LORE);
    }
}
