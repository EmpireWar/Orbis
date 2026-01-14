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
package org.empirewar.orbis.minecraft.command;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.minecraft.player.PlayerOrbisSession;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.util.OrbisText;
import org.empirewar.orbis.util.OrbisTranslations;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

@Permission(Permissions.MANAGE)
public final class SelectionCommand {

    @Command("orbis select|selection|sel <type>")
    public void onSelectType(PlayerOrbisSession session, @Argument("type") AreaType<?> type) {
        final Orbis orbis = OrbisAPI.get();
        orbis.selectionManager()
                .get(session.getUuid())
                .ifPresentOrElse(
                        selection -> selection.setSelectionType(type),
                        () -> orbis.selectionManager().add(session.getUuid(), new Selection(type)));
        final Key typeKey = OrbisRegistries.AREA_TYPE.getKey(type).orElseThrow();
        session.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.SELECTION_SET_TYPE.arguments(
                Component.text(typeKey.asString()))));
    }

    @Command("orbis select|selection|sel clear")
    public void onClear(PlayerOrbisSession session) {
        final Orbis orbis = OrbisAPI.get();
        orbis.selectionManager()
                .get(session.getUuid())
                .ifPresentOrElse(
                        selection -> {
                            selection.clear();
                            session.sendMessage(
                                    OrbisText.PREFIX.append(OrbisTranslations.SELECTION_CLEARED));
                            orbis.selectionManager().remove(session.getUuid());
                        },
                        () -> {
                            session.sendMessage(OrbisText.PREFIX.append(
                                    OrbisTranslations.SELECTION_NOT_ACTIVE));
                        });
    }
}
