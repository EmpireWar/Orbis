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

import static net.kyori.adventure.text.Component.text;

import net.kyori.adventure.text.format.NamedTextColor;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.flag.value.FlagValue;
import org.empirewar.orbis.member.FlagMemberGroup;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Flag;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.minecraft.extras.suggestion.ComponentTooltipSuggestion;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Permission("orbis.manage")
public final class RegionCommand {

    @Command("region|rg create <name>")
    public void onCreate(
            OrbisSession session,
            @Flag("global") boolean global,
            @Argument("name") String regionName) {
        if (OrbisAPI.get().getGlobalWorld().getByName(regionName).isPresent()) {
            session.audience()
                    .sendMessage(text("Region by that name already exists.", NamedTextColor.RED));
            return;
        }

        final Region region = global ? new GlobalRegion(regionName) : new Region(regionName);
        OrbisAPI.get().getGlobalWorld().add(region);
        session.audience()
                .sendMessage(text(
                        "Created " + (global ? "global " : "") + "region with name '" + regionName
                                + "'!",
                        NamedTextColor.GREEN));
    }

    @Command("region|rg flag add <region> <flag> [value]")
    public <T> void onFlagAdd(
            OrbisSession session,
            @Flag(value = "groups", suggestions = "groups") @Nullable String[] groups,
            @Argument("region") Region region,
            @Argument("flag") RegionFlag<?> flag,
            @Argument("value") @Nullable FlagValue<?> value) {
        if (groups == null) {
            region.addFlag(flag);
        } else {
            try {
                region.addGroupedFlag(
                        flag,
                        Arrays.stream(groups)
                                .map(FlagMemberGroup::valueOf)
                                .collect(Collectors.toSet()));
            } catch (IllegalArgumentException e) {
                session.audience()
                        .sendMessage(text("Invalid group specified.", NamedTextColor.RED));
            }
        }

        if (value != null) {
            RegionFlag<T> cast = (RegionFlag<T>) flag;
            region.setFlag(cast, (T) value.instance());
        }
        session.audience()
                .sendMessage(text(
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
                .sendMessage(text(
                        "Removed flag '" + flag.key().asString() + "' from region " + region.name()
                                + "!",
                        NamedTextColor.RED));
    }

    @Command("region|rg flag set <region> <flag> <value>")
    public <T> void onFlagSet(
            OrbisSession session,
            @Flag(value = "groups", suggestions = "groups") @Nullable String[] groups,
            @Argument("region") Region region,
            @Argument("flag") RegionFlag<?> flag,
            @Argument("value") @Greedy FlagValue<?> value) {
        if (!region.hasFlag(flag)) {
            onFlagAdd(session, groups, region, flag, value);
            return;
        } else if (groups != null) {
            onFlagRemove(session, region, flag);
            onFlagAdd(session, groups, region, flag, value);
            return;
        }

        // Is there a better way? I'm not sure...
        RegionFlag<T> cast = (RegionFlag<T>) flag;
        region.setFlag(cast, (T) value.instance());
        session.audience().sendMessage(text("success"));
    }

    @Command("region|rg setpriority <region> <priority>")
    public void onSetPriority(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("priority") int priority) {
        region.priority(priority);
        session.audience()
                .sendMessage(text(
                        "Set priority of region '" + region.name() + "' to " + priority + ".",
                        NamedTextColor.GREEN));
    }

    @Command("region|rg parent add <region> <parent>")
    public void onAddParent(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("parent") Region parent) {
        region.addParent(parent);
        session.audience()
                .sendMessage(text(
                        "Added parent '" + parent.name() + "' to '" + region.name() + "'.",
                        NamedTextColor.GREEN));
    }

    @Command("region|rg parent remove <region> <parent>")
    public void onRemoveParent(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("parent") Region parent) {
        region.removeParent(parent);
        session.audience()
                .sendMessage(text(
                        "Removed parent '" + parent.name() + "' from '" + region.name() + "'.",
                        NamedTextColor.RED));
    }

    @Command("region|rg world add <region> <world>")
    public void addWorld(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("world") RegionisedWorld world) {
        if (region.isGlobal()) {
            session.audience()
                    .sendMessage(
                            text("Can't assign world to a global region.", NamedTextColor.RED));
            return;
        }

        if (world.add(region)) {
            session.audience()
                    .sendMessage(text(
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
        if (region.isGlobal()) {
            session.audience()
                    .sendMessage(
                            text("Can't assign world to a global region.", NamedTextColor.RED));
            return;
        }

        if (world.remove(region)) {
            session.audience()
                    .sendMessage(text(
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
        session.audience().sendMessage(text("Added position!"));
    }

    @Suggestions("groups")
    public List<ComponentTooltipSuggestion> groupSuggestions(
            CommandContext<OrbisSession> context, CommandInput input) {
        return Arrays.stream(FlagMemberGroup.values())
                .map(fmg ->
                        ComponentTooltipSuggestion.suggestion(fmg.name(), text(fmg.description())))
                .toList();
    }
}
