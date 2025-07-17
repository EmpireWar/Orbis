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
package org.empirewar.orbis.command;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

import com.google.common.collect.Iterables;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.area.PolygonArea;
import org.empirewar.orbis.area.PolyhedralArea;
import org.empirewar.orbis.area.SphericalArea;
import org.empirewar.orbis.exception.IncompleteAreaException;
import org.empirewar.orbis.flag.GroupedMutableRegionFlag;
import org.empirewar.orbis.flag.MutableRegionFlag;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.flag.value.FlagValue;
import org.empirewar.orbis.member.FlagMemberGroup;
import org.empirewar.orbis.member.Member;
import org.empirewar.orbis.member.PermissionMember;
import org.empirewar.orbis.member.PlayerMember;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.player.PlayerOrbisSession;
import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.util.OrbisText;
import org.empirewar.orbis.util.OrbisTranslations;
import org.empirewar.orbis.world.RegionisedWorld;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Flag;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.minecraft.extras.suggestion.ComponentTooltipSuggestion;
import org.incendo.cloud.processors.confirmation.annotation.Confirmation;
import org.incendo.cloud.suggestion.Suggestion;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.*;
import java.util.stream.Collectors;

@Permission(Permissions.MANAGE)
public final class RegionCommand {

    @Command("region|rg create|define <name> [area_type]")
    public void onCreate(
            OrbisSession session,
            @Flag("global") boolean global,
            @Flag("ignore-selection") boolean ignoreSelection,
            @Argument("name") String regionName,
            @Argument("area_type") @Nullable AreaType<?> areaType) {
        final Orbis orbis = OrbisAPI.get();
        if (OrbisRegistries.REGIONS.get(regionName).isPresent()) {
            session.sendMessage(OrbisText.PREFIX.append(
                    OrbisTranslations.REGION_ALREADY_EXISTS.arguments(Component.text(regionName))));
            return;
        }

        if (ignoreSelection) {
            areaType = null;
        }

        Area area;
        if (global) {
            area = null;
        } else if (ignoreSelection) {
            area = new CuboidArea();
        } else if (session instanceof PlayerOrbisSession player) {
            final Selection selection =
                    orbis.selectionManager().get(player.getUuid()).orElse(null);
            if (selection == null) {
                session.sendMessage(
                        OrbisText.PREFIX.append(OrbisTranslations.REGION_SELECTION_REQUIRED));
                return;
            }

            AreaType<?> defaultedType = areaType == null ? AreaType.CUBOID : areaType;
            if (selection.getSelectionType() != defaultedType) {
                session.sendMessage(OrbisText.PREFIX.append(
                        OrbisTranslations.REGION_SELECTION_TYPE_MISMATCH.arguments(
                                Component.text(defaultedType.toString()),
                                Component.text(selection.getSelectionType().toString()))));
                return;
            }

            try {
                area = selection.build();
            } catch (IncompleteAreaException e) {
                session.sendMessage(
                        OrbisText.PREFIX.append(OrbisTranslations.REGION_INCOMPLETE_SELECTION));
                return;
            }

            selection.getPoints().forEach(area::addPoint);
            session.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_USED_SELECTION_NOTE));
        } else if (areaType == null || areaType == AreaType.CUBOID) {
            area = new CuboidArea();
        } else if (areaType == AreaType.POLYGON) {
            area = new PolygonArea();
        } else if (areaType == AreaType.POLYHEDRAL) {
            area = new PolyhedralArea();
        } else if (areaType == AreaType.SPHERE) {
            area = new SphericalArea();
        } else {
            area = new CuboidArea();
        }

        final Region region = global ? new GlobalRegion(regionName) : new Region(regionName, area);
        OrbisRegistries.REGIONS.register(regionName, region);
        if (session instanceof PlayerOrbisSession player && !region.isGlobal()) {
            orbis.getRegionisedWorld(orbis.getPlayerWorld(player.getUuid())).add(region);
        }
        session.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.REGION_CREATED.arguments(
                Component.text(regionName), Component.text(global ? "global " : ""))));
    }

    @Command("region|rg setarea <region>")
    @CommandDescription("Set the area a region spans.")
    public void onSetArea(PlayerOrbisSession session, @Argument("region") Region region) {
        if (region.isGlobal()) {
            session.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_GLOBAL_AREA_NOT_SUPPORTED));
            return;
        }

        final Selection selection =
                OrbisAPI.get().selectionManager().get(session.getUuid()).orElse(null);
        if (selection == null) {
            session.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_NO_ACTIVE_SELECTION));
            return;
        }

        if (selection.getSelectionType() != region.area().getType()) {
            session.sendMessage(OrbisText.PREFIX.append(
                    OrbisTranslations.REGION_SET_AREA_TYPE_MISMATCH.arguments(
                            Component.text(region.area().getType().toString()),
                            Component.text(selection.getSelectionType().toString()))));
            return;
        }

        try {
            final Area area = selection.build();
            region.area().clearPoints();
            for (Vector3ic point : area.points()) {
                region.area().addPoint(new Vector3i(point.x(), point.y(), point.z()));
            }
            session.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_SET_AREA_SUCCESS.arguments(
                            Component.text(region.name()))));
        } catch (IncompleteAreaException e) {
            session.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_INCOMPLETE_SELECTION));
        }
    }

    @Command("region|rg remove|delete <region>")
    @CommandDescription("Completely remove a region and remove it from any worlds.")
    @Confirmation
    public void onRemove(OrbisSession session, @Argument("region") Region region) {
        final boolean anySucceeded = OrbisAPI.get().removeRegion(region);
        if (anySucceeded) {
            session.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_REMOVE_SUCCESS.arguments(
                            Component.text(region.name()))));
            return;
        }

        session.sendMessage(OrbisText.PREFIX.append(
                OrbisTranslations.REGION_REMOVE_FAILED.arguments(Component.text(region.name()))));
    }

    @Command("region|rg flag add <region> <flag> [value]")
    @CommandDescription("Add a flag to a region, optionally with a specific value.")
    public <T> void onFlagAdd(
            OrbisSession session,
            @Flag(value = "groups", suggestions = "groups") @Nullable String[] groups,
            @Argument("region") Region region,
            @Argument("flag") RegistryRegionFlag<?> flag,
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
                session.sendMessage(
                        OrbisText.PREFIX.append(OrbisTranslations.REGION_INVALID_GROUP));
                return;
            }
        }

        if (value != null) {
            RegistryRegionFlag<T> cast = (RegistryRegionFlag<T>) flag;
            region.setFlag(cast, (T) value.instance());
        }
        session.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.REGION_FLAG_ADDED.arguments(
                Component.text(flag.key().asString()), Component.text(region.name()))));
    }

    @Command("region|rg flag list <region>")
    @CommandDescription("List all flags set on a region.")
    public void onFlagList(OrbisSession session, @Argument("region") Region region) {
        final String regionName = region.name();

        // Header with region name
        session.sendMessage(OrbisText.PREFIX.append(text("[", NamedTextColor.GRAY)
                .append(text(regionName, OrbisText.SECONDARY_ORANGE))
                .append(text("]", NamedTextColor.GRAY))));

        session.sendMessage(empty());

        // Display all flags with no limit
        displayFlags(session, region, -1);
    }

    @Command("region|rg flag remove <region> <flag>")
    @CommandDescription("Remove a flag from a region.")
    public void onFlagRemove(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("flag") RegistryRegionFlag<?> flag) {
        region.removeFlag(flag);
        session.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.REGION_FLAG_REMOVED.arguments(
                Component.text(flag.key().asString()), Component.text(region.name()))));
    }

    @Command("region|rg flag set <region> <flag> <value>")
    @CommandDescription(
            "Set the value of a flag on a region. This will add the flag if it does not already exist.")
    public <T> void onFlagSet(
            OrbisSession session,
            @Flag(value = "groups", suggestions = "groups") @Nullable String[] groups,
            @Argument("region") Region region,
            @Argument("flag") RegistryRegionFlag<?> flag,
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
        RegistryRegionFlag<T> cast = (RegistryRegionFlag<T>) flag;
        region.setFlag(cast, (T) value.instance());
        session.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.REGION_FLAG_SET.arguments(
                Component.text(flag.key().asString()), Component.text(region.name()))));
    }

    @Command("region|rg setpriority <region> <priority>")
    @CommandDescription(
            "Set the priority of a region. Flags in regions with higher priority take precedence.")
    public void onSetPriority(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("priority") int priority) {
        if (region.isGlobal()) {
            session.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.REGION_PRIORITY_GLOBAL));
            return;
        }

        region.priority(priority);
        session.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.REGION_PRIORITY_SET.arguments(
                Component.text(region.name()), Component.text(String.valueOf(priority)))));
    }

    @Command("region|rg parent add <region> <parent>")
    @CommandDescription(
            "Add a parent to a region. The region will inherit flags and members from the parent region. Flags or members on the child region still take precedence.")
    public void onAddParent(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("parent") Region parent) {
        if (region.isGlobal()) {
            session.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.REGION_PARENT_GLOBAL));
            return;
        }

        if (region.equals(parent)) {
            session.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.REGION_PARENT_SELF));
            return;
        }

        if (region.parents().contains(parent)) {
            session.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_PARENT_ALREADY.arguments(
                            Component.text(parent.name()), Component.text(region.name()))));
            return;
        }

        try {
            region.addParent(parent);
            session.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_PARENT_ADDED.arguments(
                            Component.text(parent.name()), Component.text(region.name()))));
        } catch (IllegalArgumentException e) {
            session.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.REGION_PARENT_CIRCULAR));
        }
    }

    @Command("region|rg parent remove <region> <parent>")
    @CommandDescription("Removes a parent from a region.")
    public void onRemoveParent(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("parent") Region parent) {
        region.removeParent(parent);
        session.sendMessage(
                OrbisText.PREFIX.append(OrbisTranslations.REGION_PARENT_REMOVED.arguments(
                        Component.text(parent.name()), Component.text(region.name()))));
    }

    @Command("region|rg world add <region> <world>")
    @CommandDescription(
            "Adds a region to a world set. The region will affect the world it is added into.")
    public void addWorld(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("world") RegionisedWorld world) {
        if (region.isGlobal()) {
            session.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_GLOBAL));
            return;
        }

        if (world.add(region)) {
            session.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_ADDED.arguments(
                            Component.text(region.name()),
                            Component.text(world.worldId().orElseThrow().asString()))));
            return;
        }

        session.sendMessage(
                OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_ADD_FAILED.arguments(
                        Component.text(region.name()),
                        Component.text(world.worldId().orElseThrow().asString()))));
    }

    @Command("region|rg world remove <region> <world>")
    @CommandDescription("Removes a region from a world set.")
    public void removeWorld(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("world") RegionisedWorld world) {
        if (region.isGlobal()) {
            session.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_GLOBAL));
            return;
        }

        if (world.remove(region)) {
            session.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_REMOVED.arguments(
                            Component.text(region.name()),
                            Component.text(world.worldId().orElseThrow().asString()))));
            return;
        }

        session.sendMessage(
                OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_REMOVE_FAILED.arguments(
                        Component.text(region.name()),
                        Component.text(world.worldId().orElseThrow().asString()))));
    }

    @Command("region|rg points add <region> <x> <y> <z>")
    @CommandDescription("Adds a point to a region's area.")
    public void onAddPos(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("x") int x,
            @Argument("y") int y,
            @Argument("z") int z) {
        if (!region.isGlobal() && region.area().addPoint(new Vector3i(x, y, z))) {
            session.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_POINT_ADDED.arguments(
                            Component.text(x),
                            Component.text(y),
                            Component.text(z),
                            Component.text(region.name()))));
            return;
        }

        session.sendMessage(
                OrbisText.PREFIX.append(OrbisTranslations.REGION_POINT_ADD_FAILED.arguments(
                        Component.text(region.name()))));
    }

    @Command("region|rg points list <region>")
    @CommandDescription("Lists all points in a region's area.")
    public void onListPoints(OrbisSession session, @Argument("region") Region region) {
        session.sendMessage(OrbisText.PREFIX.append(text("Points in region ")
                .append(text(region.name(), OrbisText.SECONDARY_ORANGE))
                .append(text(":"))));

        if (region.isGlobal()) {
            session.sendMessage(text("  Global region - no area defined", NamedTextColor.GRAY));
            return;
        }

        Set<Vector3ic> points = region.area().points();
        if (points.isEmpty()) {
            session.sendMessage(
                    text("  No points defined for this region.", OrbisText.SECONDARY_RED));
            return;
        }

        int index = 1;
        for (Vector3ic point : points) {
            String pointStr =
                    String.format("%d. [%d, %d, %d]", index++, point.x(), point.y(), point.z());
            String teleportCmd = String.format("/tp %d %d %d", point.x(), point.y(), point.z());

            Component pointComponent = text().content("  " + pointStr)
                    .hoverEvent(HoverEvent.showText(text("Click to teleport to this point")))
                    .clickEvent(ClickEvent.runCommand(teleportCmd))
                    .append(space())
                    .append(text("["))
                    .append(text("TP", OrbisText.EREBOR_GREEN))
                    .append(text("]"))
                    .build();

            session.sendMessage(pointComponent);
        }

        session.sendMessage(empty());
    }

    @Command("region|rg member add <region> player <uuid>")
    @CommandDescription(
            "Adds a player member to a region. This will match a player with the specified UUID.")
    public void onAddPlayer(
            OrbisSession session, @Argument("region") Region region, @Argument("uuid") UUID uuid) {
        region.addMember(new PlayerMember(uuid));
        session.sendMessage(OrbisText.PREFIX.append(Component.text(
                "Added '" + uuid + "' as a member to region " + region.name() + ".",
                OrbisText.EREBOR_GREEN)));
    }

    @Command("region|rg member remove <region> player <uuid>")
    @CommandDescription("Removes a player member from a region.")
    public void onRemovePlayer(
            OrbisSession session, @Argument("region") Region region, @Argument("uuid") UUID uuid) {
        for (Member member : region.members()) {
            if (member instanceof PlayerMember playerMember
                    && playerMember.playerId().equals(uuid)) {
                region.removeMember(member);
                session.sendMessage(OrbisText.PREFIX.append(
                        Component.text("Removed member '" + uuid + "'.", OrbisText.EREBOR_GREEN)));
                return;
            }
        }
        session.sendMessage(OrbisText.PREFIX.append(
                Component.text("Couldn't find a member with that UUID.", OrbisText.SECONDARY_RED)));
    }

    @Command("region|rg member add <region> permission <permission>")
    @CommandDescription(
            "Adds a permission member to a region. This will match players with the specified permission.")
    public void onAddPermission(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("permission") String permission) {
        region.addMember(new PermissionMember(permission));
        session.sendMessage(OrbisText.PREFIX.append(Component.text(
                "Added permission " + permission + " as a member to region " + region.name() + ".",
                OrbisText.EREBOR_GREEN)));
    }

    @Command("region|rg member remove <region> permission <permission>")
    @CommandDescription("Removes a permission member from a region.")
    public void onRemovePermission(
            OrbisSession session,
            @Argument("region") Region region,
            @Argument("permission") String permission) {
        for (Member member : region.members()) {
            if (member instanceof PermissionMember permissionMember
                    && permissionMember.permission().equals(permission)) {
                region.removeMember(member);
                session.getAudience()
                        .sendMessage(OrbisText.PREFIX.append(Component.text(
                                "Removed permission member '" + permission + "'.",
                                OrbisText.EREBOR_GREEN)));
                return;
            }
        }

        session.sendMessage(OrbisText.PREFIX.append(Component.text(
                "Couldn't find a member with that permission.", OrbisText.SECONDARY_RED)));
    }

    @Command("region|rg info <region>")
    public void onInfo(OrbisSession session, @Argument("region") Region region) {
        final String regionName = region.name();

        // Header with region name
        session.sendMessage(OrbisText.PREFIX.append(text("[", NamedTextColor.GRAY)
                .append(text(regionName, OrbisText.SECONDARY_ORANGE))
                .append(text("]", NamedTextColor.GRAY))));

        // Priority section - clickable to set priority
        session.sendMessage(createClickableLine(
                "Priority",
                String.valueOf(region.priority()),
                "/rg setpriority " + regionName + " ",
                "Click to set priority"));

        // Parents section with clickable elements
        Component parentsLine = text("Parents: ", OrbisText.EREBOR_GREEN);

        if (!region.isGlobal()) {
            // Add parent button
            parentsLine = parentsLine
                    .append(text("[", NamedTextColor.GRAY))
                    .append(text("+", NamedTextColor.GREEN)
                            .hoverEvent(HoverEvent.showText(
                                    text("Click to add a parent region", OrbisText.EREBOR_GREEN)))
                            .clickEvent(ClickEvent.suggestCommand(
                                    "/rg parent add " + regionName + " ")))
                    .append(text("]", NamedTextColor.GRAY));

            session.sendMessage(parentsLine);

            // List existing parents with remove buttons
            if (region.parents().isEmpty()) {
                session.sendMessage(text("  » None", NamedTextColor.GRAY));
            } else {
                for (Region parent : region.parents()) {
                    Component parentLine = text(
                                    "  ▷ ", NamedTextColor.GRAY) // Right-pointing triangle
                            .append(text(parent.name(), NamedTextColor.WHITE))
                            .append(space())
                            .append(text("[", NamedTextColor.GRAY)
                                    .append(text("-", NamedTextColor.RED)
                                            .hoverEvent(HoverEvent.showText(
                                                    text("Remove parent", OrbisText.SECONDARY_RED)))
                                            .clickEvent(
                                                    ClickEvent.suggestCommand("/rg parent remove "
                                                            + regionName + " " + parent.name()))))
                            .append(text("]", NamedTextColor.GRAY));
                    session.sendMessage(parentLine);
                }
            }
        } else {
            session.sendMessage(parentsLine.append(
                    text("Global regions cannot have parents", NamedTextColor.GRAY)));
        }

        // Area information section
        session.sendMessage(empty());
        session.sendMessage(text("Area Information", OrbisText.EREBOR_GREEN));

        if (region.isGlobal()) {
            session.sendMessage(text("  Global region - no area defined", NamedTextColor.GRAY));
        } else {
            final Area area = region.area();
            // Area type
            final String areaName = OrbisRegistries.AREA_TYPE
                    .getKey(area.getType())
                    .orElseThrow()
                    .asString();
            session.sendMessage(text("  Type: ", NamedTextColor.GRAY)
                    .append(text(areaName, NamedTextColor.WHITE))
                    .hoverEvent(HoverEvent.showText(text("Area type", OrbisText.EREBOR_GREEN))));

            // Bounding box
            Vector3ic min = area.getMin();
            Vector3ic max = area.getMax();
            session.sendMessage(text("  Bounds: ", NamedTextColor.GRAY)
                    .append(text(
                            String.format(
                                    "(%d, %d, %d) to (%d, %d, %d)",
                                    min.x(), min.y(), min.z(), max.x(), max.y(), max.z()),
                            NamedTextColor.WHITE))
                    .hoverEvent(HoverEvent.showText(text(
                            "The minimum and maximum points of the area",
                            OrbisText.EREBOR_GREEN))));

            // Volume
            long volume = Iterables.size(area);
            session.sendMessage(text("  Volume: ", NamedTextColor.GRAY)
                    .append(text(String.format("%,d", volume), NamedTextColor.WHITE))
                    .append(text(" blocks", NamedTextColor.GRAY)));

            // Number of points
            int pointCount = area.points().size();
            session.sendMessage(text("  Points: ", NamedTextColor.GRAY)
                    .append(text(String.format("%,d", pointCount), NamedTextColor.WHITE)));

            // Add clickable command to set area
            if (session instanceof PlayerOrbisSession) {
                session.sendMessage(empty());
                session.sendMessage(text("  [▶] ", NamedTextColor.GRAY)
                        .append(text("Set area", NamedTextColor.YELLOW)
                                .hoverEvent(HoverEvent.showText(text(
                                        "Click to set a new area for this region",
                                        OrbisText.EREBOR_GREEN)))
                                .clickEvent(ClickEvent.suggestCommand("/rg setarea " + regionName)))
                        .append(text(" (select an area first with ", NamedTextColor.GRAY))
                        .append(text("/sel", NamedTextColor.YELLOW)
                                .hoverEvent(HoverEvent.showText(text(
                                        "Click to learn about selection commands",
                                        OrbisText.EREBOR_GREEN)))
                                .clickEvent(ClickEvent.suggestCommand("/sel help")))
                        .append(text(")", NamedTextColor.GRAY)));

                // Add teleport to center option
                int centerX = (min.x() + max.x()) / 2;
                int centerZ = (min.z() + max.z()) / 2;
                int centerY = (min.y() + max.y()) / 2;

                session.sendMessage(text("  [▶] ", NamedTextColor.GRAY)
                        .append(text("Teleport to center", NamedTextColor.YELLOW)
                                .hoverEvent(HoverEvent.showText(text(
                                        "Click to teleport to the center of this region",
                                        OrbisText.EREBOR_GREEN)))
                                .clickEvent(ClickEvent.runCommand(
                                        "/tp @s " + centerX + " " + centerY + " " + centerZ)))
                        .append(text(" (", NamedTextColor.GRAY)
                                .append(text(
                                        centerX + ", " + centerY + ", " + centerZ,
                                        NamedTextColor.WHITE))
                                .append(text(")", NamedTextColor.GRAY))));
            }
        }

        // Flags section
        displayFlags(session, region, 5);

        // Members section with clickable elements
        Component membersLine = text("Members: ", OrbisText.EREBOR_GREEN)
                .append(text("[", NamedTextColor.GRAY)
                        .append(text("+", NamedTextColor.GREEN)
                                .hoverEvent(HoverEvent.showText(
                                        text("Click to add a member", OrbisText.EREBOR_GREEN)))
                                .clickEvent(ClickEvent.suggestCommand(
                                        "/rg member add " + regionName + " ")))
                        .append(text("]", NamedTextColor.GRAY)));

        session.sendMessage(membersLine);

        // List existing members with remove buttons
        for (Member member : region.members()) {
            final Key typeName =
                    OrbisRegistries.MEMBER_TYPE.getKey(member.getType()).orElseThrow();
            String value = "";
            if (member instanceof PermissionMember permissionMember) {
                value = permissionMember.permission();
            } else if (member instanceof PlayerMember playerMember) {
                value = playerMember.playerId().toString();
            }

            Component memberLine = text("  " + typeName.asString() + ": ", NamedTextColor.GRAY)
                    .append(text(value, NamedTextColor.WHITE))
                    .append(text(" ", NamedTextColor.GRAY))
                    .append(text("[-]", NamedTextColor.RED)
                            .hoverEvent(HoverEvent.showText(
                                    text("Click to remove this member", OrbisText.SECONDARY_RED)))
                            .clickEvent(ClickEvent.suggestCommand("/rg member remove " + regionName
                                    + " " + typeName.value() + " " + value)));
            session.sendMessage(memberLine);
        }
    }

    @Command("region|rg list")
    @CommandDescription("List all regions, optionally filtered by worlds.")
    public void onList(
            OrbisSession session,
            @Flag(value = "worlds", suggestions = "worlds") @Nullable String[] worlds) {
        final Orbis orbis = OrbisAPI.get();
        Set<Region> regions = new HashSet<>();
        if (worlds != null && worlds.length > 0) {
            for (String worldKey : worlds) {
                final RegionisedWorld world = orbis.getRegionisedWorld(Key.key(worldKey));
                regions.addAll(world.regions());
            }
        } else {
            // Add regions from all worlds
            regions.addAll(OrbisRegistries.REGIONS.getAll());
        }

        if (regions.isEmpty()) {
            session.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.REGION_LIST_EMPTY));
            return;
        }

        session.sendMessage(OrbisText.PREFIX.append(text("Regions:", OrbisText.EREBOR_GREEN)));
        for (Region region : regions) {
            session.sendMessage(text("- ", NamedTextColor.GRAY)
                    .append(text(region.name(), OrbisText.SECONDARY_ORANGE)));
        }
    }

    @Command("region|rg visualise|visualize")
    @CommandDescription("Toggle region boundary visualisation.")
    public void onVisualise(PlayerOrbisSession session) {
        OrbisPlatform platform = (OrbisPlatform) OrbisAPI.get();
        UUID uuid = session.getUuid();
        boolean nowVisualising = !platform.isVisualising(uuid);
        platform.setVisualising(uuid, nowVisualising);
        if (nowVisualising) {
            session.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.ENABLE_REGION_VISUALISATION));
        } else {
            session.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.DISABLE_REGION_VISUALISATION));
        }
    }

    @Suggestions("groups")
    public List<ComponentTooltipSuggestion> groupSuggestions(
            CommandContext<OrbisSession> context, CommandInput input) {
        return Arrays.stream(FlagMemberGroup.values())
                .map(fmg ->
                        ComponentTooltipSuggestion.suggestion(fmg.name(), text(fmg.description())))
                .toList();
    }

    @Suggestions("worlds")
    public List<Suggestion> worldSuggestions(
            CommandContext<OrbisSession> context, CommandInput input) {
        return OrbisAPI.get().getRegionisedWorlds().stream()
                .map(rw -> Suggestion.suggestion(rw.worldId().orElseThrow().asString()))
                .toList();
    }

    /**
     * Creates a clickable line with hover text and command suggestion.
     *
     * @param label The label to display
     * @param value The value to display
     * @param command The command to suggest when clicked
     * @param hoverText The text to show on hover
     * @return A clickable component
     */
    private Component createClickableLine(
            String label, String value, String command, String hoverText) {
        return text(label + ": ", OrbisText.EREBOR_GREEN)
                .append(text(value, NamedTextColor.WHITE)
                        .hoverEvent(HoverEvent.showText(text(hoverText, OrbisText.EREBOR_GREEN)))
                        .clickEvent(ClickEvent.suggestCommand(command)));
    }

    /**
     * Displays the flags for a region with an optional limit.
     *
     * @param session The session to send messages to
     * @param region  The region to display flags for
     * @param limit   Maximum number of flags to display (use -1 for no limit)
     */
    private void displayFlags(OrbisSession session, Region region, int limit) {
        final String regionName = region.name();

        // Flags section with clickable elements
        Component flagsLine = text("Flags: ", OrbisText.EREBOR_GREEN)
                .append(text("[", NamedTextColor.GRAY)
                        .append(text("List", NamedTextColor.YELLOW)
                                .hoverEvent(HoverEvent.showText(
                                        text("Click to list all flags", OrbisText.EREBOR_GREEN)))
                                .clickEvent(ClickEvent.runCommand("/rg flag list " + regionName)))
                        .append(text("] ", NamedTextColor.GRAY))
                        .append(text("[", NamedTextColor.GRAY)
                                .append(text("+", NamedTextColor.GREEN)
                                        .hoverEvent(HoverEvent.showText(text(
                                                "Click to add a flag", OrbisText.EREBOR_GREEN)))
                                        .clickEvent(ClickEvent.suggestCommand(
                                                "/rg flag add " + regionName + " ")))
                                .append(text("] ", NamedTextColor.GRAY))));

        session.sendMessage(flagsLine);

        // Get all flags
        Map<RegistryRegionFlag<?>, Set<String>> flags = new HashMap<>();
        for (RegistryRegionFlag<?> flag : OrbisRegistries.FLAGS) {
            region.getFlag(flag).ifPresent(mu -> {
                if (mu instanceof GroupedMutableRegionFlag<?> grouped) {
                    flags.put(
                            flag,
                            grouped.groups().stream()
                                    .map(FlagMemberGroup::name)
                                    .collect(Collectors.toSet()));
                } else {
                    flags.put(flag, Collections.emptySet());
                }
            });
        }

        if (flags.isEmpty()) {
            if (limit != 0) { // Only show "No flags set" if we're not just checking
                session.sendMessage(text("  No flags set.", NamedTextColor.GRAY));
            }
            return;
        }

        // Convert to list for easier handling
        List<Map.Entry<RegistryRegionFlag<?>, Set<String>>> flagEntries =
                new ArrayList<>(flags.entrySet());
        boolean hasMore = limit > 0 && flagEntries.size() > limit;
        int displayCount = hasMore ? limit - 1 : flagEntries.size();

        // Display each flag with its value and groups
        for (int i = 0; i < displayCount; i++) {
            Map.Entry<RegistryRegionFlag<?>, Set<String>> entry = flagEntries.get(i);
            final RegistryRegionFlag<?> registryFlag = entry.getKey();
            final MutableRegionFlag<?> flag = region.getFlag(registryFlag).orElseThrow();
            Set<String> groups = entry.getValue();

            Component flagName = text(flag.key().asString(), NamedTextColor.WHITE);
            final Optional<String> description = registryFlag.description();
            if (description.isPresent()) {
                flagName = flagName.hoverEvent(
                        HoverEvent.showText(text(description.get(), NamedTextColor.WHITE)));
            }

            Component flagLine = text("  ", NamedTextColor.GRAY)
                    .append(flagName)
                    .append(text(": ", NamedTextColor.GRAY))
                    .append(text(flag.getValue().toString(), NamedTextColor.YELLOW));

            // Add groups if present
            if (!groups.isEmpty()) {
                flagLine = flagLine.append(text(" (", NamedTextColor.GRAY))
                        .append(text(String.join(", ", groups), NamedTextColor.AQUA))
                        .append(text(")", NamedTextColor.GRAY));
            }

            // Add modify button
            flagLine = flagLine.append(space())
                    .append(text("[", NamedTextColor.GRAY))
                    .append(text("✎", NamedTextColor.YELLOW)
                            .hoverEvent(HoverEvent.showText(
                                    text("Click to modify this flag", OrbisText.SECONDARY_ORANGE)))
                            .clickEvent(ClickEvent.suggestCommand("/rg flag set " + regionName + " "
                                    + flag.key().asString() + " ")))
                    .append(text("]", NamedTextColor.GRAY));

            // Add remove button
            flagLine = flagLine.append(space())
                    .append(text("[", NamedTextColor.GRAY))
                    .append(text("-", NamedTextColor.RED)
                            .hoverEvent(HoverEvent.showText(
                                    text("Click to remove this flag", OrbisText.SECONDARY_RED)))
                            .clickEvent(ClickEvent.suggestCommand("/rg flag remove " + regionName
                                    + " " + flag.key().asString())))
                    .append(text("]", NamedTextColor.GRAY));

            session.sendMessage(flagLine);
        }

        // Show "and X more..." if there are more flags to show
        if (hasMore) {
            int remaining = flagEntries.size() - displayCount;
            session.sendMessage(text(
                            "  and " + remaining + " more...",
                            NamedTextColor.GRAY,
                            TextDecoration.ITALIC)
                    .hoverEvent(HoverEvent.showText(
                            text("Click to view all flags", OrbisText.EREBOR_GREEN)))
                    .clickEvent(ClickEvent.runCommand("/rg flag list " + regionName)));
        }
    }
}
