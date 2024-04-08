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
package org.empirewar.orbis.command;

import net.kyori.adventure.text.Component;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.region.Region;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.joml.Vector3i;

import java.util.UUID;

public final class RegionCommand {

    @Command("rg create <name>")
    public void onCreate(OrbisSession session, @Argument("name") String name) {
        final Region region = new Region(name);
        OrbisAPI.get().getRegionisedWorld(UUID.randomUUID()).add(region);
        region.addFlag(DefaultFlags.CAN_BREAK);
        session.getAudience().sendMessage(Component.text("Created a region!"));
    }

    @Command("rg addpos <region> <x> <y> <z>")
    public void onAddPos(
            OrbisSession session,
            @Argument("region") String regionName,
            @Argument("x") int x,
            @Argument("y") int y,
            @Argument("z") int z) {
        final Region region = OrbisAPI.get()
                .getRegionisedWorld(UUID.randomUUID())
                .getByName(regionName)
                .orElseThrow();
        region.area().addPoint(new Vector3i(x, y, z));
        session.getAudience().sendMessage(Component.text("Added position!"));
    }
}
