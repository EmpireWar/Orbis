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
package org.empirewar.orbis.command;

import static net.kyori.adventure.text.Component.text;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.player.PlayerOrbisSession;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.util.OrbisText;
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
        session.sendMessage(OrbisText.PREFIX.append(text(
                "Set your selection to an " + typeKey.asString() + ".", OrbisText.EREBOR_GREEN)));
    }

    @Command("orbis select|selection|sel clear")
    public void onClear(PlayerOrbisSession session) {
        final Orbis orbis = OrbisAPI.get();
        orbis.selectionManager()
                .get(session.getUuid())
                .ifPresentOrElse(
                        selection -> {
                            selection.clear();
                            session.sendMessage(OrbisText.PREFIX.append(text(
                                    "Cleared your current selection.", OrbisText.EREBOR_GREEN)));
                            orbis.selectionManager().remove(session.getUuid());
                        },
                        () -> {
                            session.sendMessage(OrbisText.PREFIX.append(text(
                                    "You don't have an active selection.",
                                    OrbisText.SECONDARY_RED)));
                        });
    }
}
