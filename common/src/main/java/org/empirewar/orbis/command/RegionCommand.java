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

import net.kyori.adventure.text.Component;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.area.PolygonArea;
import org.empirewar.orbis.exception.IncompleteAreaException;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.flag.value.FlagValue;
import org.empirewar.orbis.member.FlagMemberGroup;
import org.empirewar.orbis.member.Member;
import org.empirewar.orbis.member.PermissionMember;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.player.PlayerOrbisSession;
import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.util.OrbisText;
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

@Permission(Permissions.MANAGE)
public record RegionCommand(Orbis orbis) {

    @Command("region|rg create <name> [area_type]")
    public void onCreate(
            OrbisSession session,
            @Flag("global") boolean global,
            @Flag("ignore-selection") boolean ignoreSelection,
            @Argument("name") String regionName,
            @Argument("area_type") @Nullable AreaType<?> areaType) {
        if (orbis.getGlobalWorld().getByName(regionName).isPresent()) {
            session.audience()
                    .sendMessage(OrbisText.PREFIX.append(
                            text("Region by that name already exists.", OrbisText.SECONDARY_RED)));
            return;
        }

        if (ignoreSelection) {
            areaType = null;
        }

        Area area;
        if (global) {
            area = null;
        } else if (areaType == null && session instanceof PlayerOrbisSession player) {
            final Selection selection =
                    orbis.selectionManager().get(player.getUuid()).orElse(null);
            if (selection == null) return;
            try {
                area = selection.build();
            } catch (IncompleteAreaException e) {
                session.audience()
                        .sendMessage(OrbisText.PREFIX.append(text(
                                "Incomplete selection! Did you select all points?",
                                OrbisText.SECONDARY_RED)));
                return;
            }

            selection.getPoints().forEach(area::addPoint);
            session.audience()
                    .sendMessage(OrbisText.PREFIX.append(text(
                            "Note: Used your current selection to build the region area. Use the --ignore-selection flag or specify an area type to bypass this.",
                            OrbisText.SECONDARY_ORANGE)));
        } else if (areaType == null || areaType == AreaType.CUBOID) {
            area = new CuboidArea();
        } else {
            area = new PolygonArea();
        }

        final Region region = global ? new GlobalRegion(regionName) : new Region(regionName, area);
        orbis.getGlobalWorld().add(region);
        if (session instanceof PlayerOrbisSession player) {
            orbis.getRegionisedWorld(orbis.getPlayerWorld(player.getUuid())).add(region);
        }
        session.audience()
                .sendMessage(OrbisText.PREFIX.append(text(
                        "Created " + (global ? "global " : "") + "region with name '" + regionName
                                + "'!",
                        OrbisText.EREBOR_GREEN)));
    }

    @Command("region|rg remove|delete <region>")
    public void onRemove(OrbisSession session, @Argument("region") Region region) {
        for (RegionisedWorld world : orbis.getRegionisedWorlds()) {
            world.remove(region);
        }
        orbis.getGlobalWorld().remove(region);
        session.audience()
                .sendMessage(OrbisText.PREFIX.append(text(
                        "Removed the '" + region.name() + "' region.", OrbisText.SECONDARY_RED)));
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
                        .sendMessage(OrbisText.PREFIX.append(
                                text("Invalid group specified.", OrbisText.SECONDARY_RED)));
                return;
            }
        }

        if (value != null) {
            RegionFlag<T> cast = (RegionFlag<T>) flag;
            region.setFlag(cast, (T) value.instance());
        }
        session.audience()
                .sendMessage(OrbisText.PREFIX.append(text(
                        "Added flag '" + flag.key().asString() + "' to region " + region.name()
                                + "!",
                        OrbisText.EREBOR_GREEN)));
    }

    @Command("region|rg flag remove <region> <flag>")
    public void onFlagRemove(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("flag") RegionFlag<?> flag) {
        region.removeFlag(flag);
        session.audience()
                .sendMessage(OrbisText.PREFIX.append(text(
                        "Removed flag '" + flag.key().asString() + "' from region " + region.name()
                                + "!",
                        OrbisText.SECONDARY_RED)));
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
        session.audience()
                .sendMessage(OrbisText.PREFIX.append(
                        text("Set flag '" + flag.key().asString() + "'.", OrbisText.EREBOR_GREEN)));
    }

    @Command("region|rg setpriority <region> <priority>")
    public void onSetPriority(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("priority") int priority) {
        region.priority(priority);
        session.audience()
                .sendMessage(OrbisText.PREFIX.append(text(
                        "Set priority of region '" + region.name() + "' to " + priority + ".",
                        OrbisText.EREBOR_GREEN)));
    }

    @Command("region|rg parent add <region> <parent>")
    public void onAddParent(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("parent") Region parent) {
        region.addParent(parent);
        session.audience()
                .sendMessage(OrbisText.PREFIX.append(text(
                        "Added parent '" + parent.name() + "' to '" + region.name() + "'.",
                        OrbisText.EREBOR_GREEN)));
    }

    @Command("region|rg parent remove <region> <parent>")
    public void onRemoveParent(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("parent") Region parent) {
        region.removeParent(parent);
        session.audience()
                .sendMessage(OrbisText.PREFIX.append(text(
                        "Removed parent '" + parent.name() + "' from '" + region.name() + "'.",
                        OrbisText.SECONDARY_RED)));
    }

    @Command("region|rg world add <region> <world>")
    public void addWorld(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("world") RegionisedWorld world) {
        if (region.isGlobal()) {
            session.audience()
                    .sendMessage(OrbisText.PREFIX.append(text(
                            "Can't assign world to a global region.", OrbisText.SECONDARY_RED)));
            return;
        }

        if (world.add(region)) {
            session.audience()
                    .sendMessage(OrbisText.PREFIX.append(text(
                            "Added region '" + region.name() + "' to world '"
                                    + world.worldName().orElseThrow() + "'.",
                            OrbisText.EREBOR_GREEN)));
        }
    }

    @Command("region|rg world remove <region> <world>")
    public void removeWorld(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("world") RegionisedWorld world) {
        if (region.isGlobal()) {
            session.audience()
                    .sendMessage(OrbisText.PREFIX.append(text(
                            "Can't assign world to a global region.", OrbisText.SECONDARY_RED)));
            return;
        }

        if (world.remove(region)) {
            session.audience()
                    .sendMessage(OrbisText.PREFIX.append(text(
                            "Removed region '" + region.name() + "' from world '"
                                    + world.worldName().orElseThrow() + "'.",
                            OrbisText.SECONDARY_RED)));
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
        session.audience()
                .sendMessage(OrbisText.PREFIX.append(text(
                        "Added point [" + x + ", " + y + ", " + z + "] to '" + region.name() + "'",
                        OrbisText.EREBOR_GREEN)));
    }

    @Command("region|rg member permission add <region> <permission>")
    public void onAddPermission(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("permission") String permission) {
        region.addMember(new PermissionMember(permission));
        session.audience()
                .sendMessage(OrbisText.PREFIX.append(Component.text(
                        "Added permission " + permission + " as a member to region " + region.name()
                                + ".",
                        OrbisText.EREBOR_GREEN)));
    }

    @Command("region|rg member permission remove <region> <permission>")
    public void onRemovePermission(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("permission") String permission) {
        for (Member member : region.members()) {
            if (member instanceof PermissionMember permissionMember
                    && permissionMember.permission().equals(permission)) {
                region.removeMember(member);
                session.audience()
                        .sendMessage(OrbisText.PREFIX.append(Component.text(
                                "Removed permission member '" + permission + "'.",
                                OrbisText.EREBOR_GREEN)));
                return;
            }
        }

        session.audience()
                .sendMessage(OrbisText.PREFIX.append(Component.text(
                        "Couldn't find a member with that name.", OrbisText.SECONDARY_RED)));
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
