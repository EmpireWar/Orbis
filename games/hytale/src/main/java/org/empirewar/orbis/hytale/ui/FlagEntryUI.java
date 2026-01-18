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
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import org.empirewar.orbis.flag.MutableRegionFlag;
import org.empirewar.orbis.flag.RegistryRegionFlag;

public final class FlagEntryUI {

    public static void render(
            UICommandBuilder ui,
            UIEventBuilder ev,
            int index,
            String regionName,
            RegistryRegionFlag<?> registryFlag,
            MutableRegionFlag<?> flag) {
        ui.append("#FlagCards", "Entries/Orbis_FlagEntry.ui");

        String base = "#FlagCards[" + index + "]";

        ui.set(base + " #FlagName.Text", registryFlag.key().asString());
        ui.set(base + " #FlagValue.Text", String.valueOf(flag.getValue()));

        registryFlag
                .description()
                .ifPresentOrElse(
                        d -> {
                            ui.set(base + " #FlagDescription.Text", d);
                            ui.set(base + " #FlagDescription.Visible", true);
                        },
                        () -> ui.set(base + " #FlagDescription.Visible", false));

        boolean isBoolean = flag.getValue() instanceof Boolean;
        String toggleEvent = isBoolean ? "ToggleFlag" : "ModifyFlag";
        ui.set(base + " #ToggleFlag.Text", isBoolean ? "Toggle" : "Modify");

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                base + " #ToggleFlag",
                EventData.of(UIActions.BUTTON, toggleEvent)
                        .append(UIActions.REGION, regionName)
                        .append(UIActions.FLAG, registryFlag.key().asString()));

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                base + " #RemoveFlag",
                EventData.of(UIActions.BUTTON, "RemoveFlag")
                        .append(UIActions.REGION, regionName)
                        .append(UIActions.FLAG, registryFlag.key().asString()));
    }

    private FlagEntryUI() {}
}
