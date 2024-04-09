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
import net.kyori.adventure.text.format.NamedTextColor;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.flag.value.FlagValue;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

@Permission("orbis.manage")
public final class RegionCommand {

    @Command("region|rg create <name>")
    public void onCreate(OrbisSession session, @Argument("name") String regionName) {
        final Region region = new Region(regionName);
        OrbisAPI.get().getGlobalWorld().add(region);
        session.audience()
                .sendMessage(Component.text(
                        "Created region with name '" + regionName + "'!", NamedTextColor.GREEN));
    }

    @Command("region|rg flag add <region> <flag> [value]")
    public <T> void onFlagAdd(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("flag") RegionFlag<?> flag,
            @Argument("value") @Nullable FlagValue<?> value) {
        region.addFlag(flag);
        if (value != null) {
            RegionFlag<T> cast = (RegionFlag<T>) flag;
            region.setFlag(cast, (T) value.instance());
        }
        session.audience()
                .sendMessage(Component.text(
                        "Added flag '" + flag.key().asString() + "' to region " + region.name()
                                + "!",
                        NamedTextColor.GREEN));
    }

    @Command("region|rg flag remove <region> <flag>")
    public void onFlagRemove(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("flag") RegionFlag<?> flag) {
        region.removeFlag(flag);
        session.audience()
                .sendMessage(Component.text(
                        "Removed flag '" + flag.key().asString() + "' to region " + region.name()
                                + "!",
                        NamedTextColor.RED));
    }

    @Command("region|rg flag set <region> <flag> <value>")
    public <T> void onFlagSet(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("flag") RegionFlag<?> flag,
            @Argument("value") FlagValue<?> value) {
        // Is there a better way? I'm not sure...
        RegionFlag<T> cast = (RegionFlag<T>) flag;
        region.setFlag(cast, (T) value.instance());
        session.audience().sendMessage(Component.text("success"));
    }

    @Command("region|rg world add <region> <world>")
    public void addWorld(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("world") RegionisedWorld world) {
        if (world.add(region)) {
            session.audience()
                    .sendMessage(Component.text(
                            "Added region '" + region.name() + "' to world '"
                                    + world.worldName().orElseThrow() + "'.",
                            NamedTextColor.GREEN));
        }
    }

    @Command("region|rg world remove <region> <world>")
    public void removeWorld(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("world") RegionisedWorld world) {
        if (world.remove(region)) {
            session.audience()
                    .sendMessage(Component.text(
                            "Removed region '" + region.name() + "' to world '"
                                    + world.worldName().orElseThrow() + "'.",
                            NamedTextColor.RED));
        }
    }

    @Command("region|rg addpos <region> <x> <y> <z>")
    public void onAddPos(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("x") int x,
            @Argument("y") int y,
            @Argument("z") int z) {
        region.area().addPoint(new Vector3i(x, y, z));
        session.audience().sendMessage(Component.text("Added position!"));
    }
}
