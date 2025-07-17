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
        WAND_ITEM.offer(Keys.CUSTOM_NAME, Selection.WAND_NAME);
        WAND_ITEM.offerAll(Keys.LORE, Selection.WAND_LORE);
    }
}
