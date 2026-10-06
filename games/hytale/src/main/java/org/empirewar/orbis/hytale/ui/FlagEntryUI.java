/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2026 Empire War
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
package org.empirewar.orbis.hytale.ui;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import org.empirewar.orbis.flag.MutableRegionFlag;
import org.empirewar.orbis.flag.RegistryRegionFlag;

/** Renders a single flag card into {@code #FlagCards}. */
public final class FlagEntryUI {

    private static final String GOLD = "#93844c";

    /**
     * Appends and populates one flag card.
     *
     * <p>Boolean flags get an in-place checkbox; anything else falls back to a modify button, which
     * is not yet wired up. The flag description is attached as a tooltip rather than an inline
     * label so cards stay compact.
     *
     * @param ui the command builder
     * @param ev the event builder
     * @param index the card's index within {@code #FlagCards}
     * @param regionName the region being edited
     * @param registryFlag the flag definition
     * @param flag the flag's value on this region
     */
    public static void render(
            UICommandBuilder ui,
            UIEventBuilder ev,
            int index,
            String regionName,
            RegistryRegionFlag<?> registryFlag,
            MutableRegionFlag<?> flag) {
        ui.append("#FlagCards", "Entries/Orbis_FlagEntry.ui");

        final String base = "#FlagCards[" + index + "]";
        final String key = registryFlag.key().asString();

        ui.set(base + " #FlagName.Text", key);
        ui.set(base + " #FlagValue.Text", String.valueOf(flag.getValue()));

        ui.set(base + ".TooltipTextSpans", tooltip(registryFlag));

        final EventData payload =
                EventData.of(UIActions.REGION, regionName).append(UIActions.FLAG, key);

        if (flag.getValue() instanceof Boolean value) {
            ui.set(base + " #FlagToggle.Visible", true);
            ui.set(base + " #FlagToggle.Value", value);
            ui.set(base + " #ModifyFlag.Visible", false);

            // ValueChanged rather than Activating: the checkbox reports its new state itself.
            ev.addEventBinding(
                    CustomUIEventBindingType.ValueChanged,
                    base + " #FlagToggle",
                    payload.append(UIActions.BUTTON, UIActions.TOGGLE_FLAG),
                    false);
        } else {
            ui.set(base + " #FlagToggle.Visible", false);
            ui.set(base + " #ModifyFlag.Visible", true);

            ev.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    base + " #ModifyFlag",
                    payload.append(UIActions.BUTTON, UIActions.MODIFY_FLAG));
        }

        final EventData remove = EventData.of(UIActions.BUTTON, UIActions.REMOVE_FLAG)
                .append(UIActions.REGION, regionName)
                .append(UIActions.FLAG, key);

        ev.addEventBinding(CustomUIEventBindingType.Activating, base + " #RemoveFlag", remove);

        // Right-click the card as a shortcut for the remove button.
        ev.addEventBinding(CustomUIEventBindingType.RightClicking, base, remove);
    }

    private static Message tooltip(RegistryRegionFlag<?> registryFlag) {
        final Message name =
                Message.raw(registryFlag.key().asString()).bold(true).color(GOLD);

        return registryFlag
                .description()
                .map(description ->
                        Message.join(name, Message.raw("\n\n"), Message.raw(description)))
                .orElse(name);
    }

    private FlagEntryUI() {}
}
