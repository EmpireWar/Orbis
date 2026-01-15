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
package org.empirewar.orbis.hytale.command;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

import com.google.common.collect.Iterables;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.flag.GroupedMutableRegionFlag;
import org.empirewar.orbis.flag.MutableRegionFlag;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.hytale.command.arguments.OrbisArgTypes;
import org.empirewar.orbis.hytale.util.TextUtil;
import org.empirewar.orbis.member.FlagMemberGroup;
import org.empirewar.orbis.member.Member;
import org.empirewar.orbis.member.PermissionMember;
import org.empirewar.orbis.member.PlayerMember;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.util.OrbisText;
import org.empirewar.orbis.util.OrbisTranslations;
import org.empirewar.orbis.util.Permissions;
import org.empirewar.orbis.world.RegionisedWorld;
import org.joml.Vector3ic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RegionCommand extends AbstractCommandCollection {

    public RegionCommand() {
        super("region", "The base Orbis region command.");
        this.addAliases("rg");
        this.requirePermission(Permissions.MANAGE);
        this.addSubCommand(new RegionVisualiseCommand());
        this.addSubCommand(new RegionInfoCommand());
        this.addSubCommand(new RegionWorldCommand());
    }

    private static class RegionVisualiseCommand extends AbstractPlayerCommand {

        protected RegionVisualiseCommand() {
            super("visualise", "Toggle region boundary visualisation.");
        }

        @Override
        protected void execute(
                @NonNull CommandContext commandContext,
                @NonNull Store<EntityStore> store,
                @NonNull Ref<EntityStore> ref,
                @NonNull PlayerRef playerRef,
                @NonNull World world) {
            OrbisPlatform platform = (OrbisPlatform) OrbisAPI.get();
            UUID uuid = playerRef.getUuid();
            boolean nowVisualising = !platform.isVisualising(uuid);
            platform.setVisualising(uuid, nowVisualising);
            if (nowVisualising) {
                TextUtil.send(
                        commandContext,
                        OrbisText.PREFIX.append(OrbisTranslations.ENABLE_REGION_VISUALISATION));
            } else {
                TextUtil.send(
                        commandContext,
                        OrbisText.PREFIX.append(OrbisTranslations.DISABLE_REGION_VISUALISATION));
            }
            commandContext.sendMessage(Message.raw(
                            "Sorry, this feature currently isn't implemented in the Hytale game module!")
                    .color(Color.RED));
        }
    }

    private static class RegionWorldCommand extends AbstractCommandCollection {

        public RegionWorldCommand() {
            super("world", "World-related region management commands.");
            this.addSubCommand(new RegionWorldAddCommand());
            this.addSubCommand(new RegionWorldRemoveCommand());
        }
    }

    private static class RegionInfoCommand extends AbstractCommand {

        private final RequiredArg<Region> regionArg;

        public RegionInfoCommand() {
            super("info", "Displays detailed information about a region.");
            this.regionArg = this.withRequiredArg("region", "The region", OrbisArgTypes.REGION);
        }

        @Override
        protected @Nullable CompletableFuture<Void> execute(@NonNull CommandContext context) {
            final Region region = regionArg.get(context);
            final String regionName = region.name();

            TextUtil.send(
                    context,
                    OrbisText.PREFIX.append(text("[", NamedTextColor.GRAY)
                            .append(text(regionName, OrbisText.SECONDARY_ORANGE))
                            .append(text("]", NamedTextColor.GRAY))));

            TextUtil.send(
                    context,
                    createClickableLine(
                            "Priority",
                            String.valueOf(region.priority()),
                            "/rg setpriority " + regionName + " ",
                            "Click to set priority"));

            Component parentsLine = text("Parents: ", OrbisText.EREBOR_GREEN);

            if (!region.isGlobal()) {
                parentsLine = parentsLine
                        .append(text("[", NamedTextColor.GRAY))
                        .append(text("+", NamedTextColor.GREEN)
                                .hoverEvent(HoverEvent.showText(text(
                                        "Click to add a parent region", OrbisText.EREBOR_GREEN)))
                                .clickEvent(ClickEvent.suggestCommand(
                                        "/rg parent add " + regionName + " ")))
                        .append(text("]", NamedTextColor.GRAY));

                TextUtil.send(context, parentsLine);

                if (region.parents().isEmpty()) {
                    TextUtil.send(context, text("  » None", NamedTextColor.GRAY));
                } else {
                    for (Region parent : region.parents()) {
                        Component parentLine = text("  ▷ ", NamedTextColor.GRAY)
                                .append(text(parent.name(), NamedTextColor.WHITE))
                                .append(space())
                                .append(text("[", NamedTextColor.GRAY)
                                        .append(text("-", NamedTextColor.RED)
                                                .hoverEvent(HoverEvent.showText(text(
                                                        "Remove parent", OrbisText.SECONDARY_RED)))
                                                .clickEvent(ClickEvent.suggestCommand(
                                                        "/rg parent remove " + regionName + " "
                                                                + parent.name()))))
                                .append(text("]", NamedTextColor.GRAY));
                        TextUtil.send(context, parentLine);
                    }
                }
            } else {
                TextUtil.send(
                        context,
                        parentsLine.append(
                                text("Global regions cannot have parents", NamedTextColor.GRAY)));
            }

            TextUtil.send(context, empty());
            TextUtil.send(context, text("Area Information", OrbisText.EREBOR_GREEN));

            if (region.isGlobal()) {
                TextUtil.send(
                        context, text("  Global region - no area defined", NamedTextColor.GRAY));
            } else {
                final Area area = region.area();
                final String areaName = OrbisRegistries.AREA_TYPE
                        .getKey(area.getType())
                        .orElseThrow()
                        .asString();
                TextUtil.send(
                        context,
                        text("  Type: ", NamedTextColor.GRAY)
                                .append(text(areaName, NamedTextColor.WHITE))
                                .hoverEvent(HoverEvent.showText(
                                        text("Area type", OrbisText.EREBOR_GREEN))));

                Vector3ic min = area.getMin();
                Vector3ic max = area.getMax();
                TextUtil.send(
                        context,
                        text("  Bounds: ", NamedTextColor.GRAY)
                                .append(text(
                                        String.format(
                                                "(%d, %d, %d) to (%d, %d, %d)",
                                                min.x(), min.y(), min.z(), max.x(), max.y(),
                                                max.z()),
                                        NamedTextColor.WHITE))
                                .hoverEvent(HoverEvent.showText(text(
                                        "The minimum and maximum points of the area",
                                        OrbisText.EREBOR_GREEN))));

                long volume = Iterables.size(area);
                TextUtil.send(
                        context,
                        text("  Volume: ", NamedTextColor.GRAY)
                                .append(text(String.format("%,d", volume), NamedTextColor.WHITE))
                                .append(text(" blocks", NamedTextColor.GRAY)));

                int pointCount = area.points().size();
                TextUtil.send(
                        context,
                        text("  Points: ", NamedTextColor.GRAY)
                                .append(text(
                                        String.format("%,d", pointCount), NamedTextColor.WHITE)));

                TextUtil.send(context, empty());
                TextUtil.send(
                        context,
                        text("  [▶] ", NamedTextColor.GRAY)
                                .append(text("Set area", NamedTextColor.YELLOW)
                                        .hoverEvent(HoverEvent.showText(text(
                                                "Click to set a new area for this region",
                                                OrbisText.EREBOR_GREEN)))
                                        .clickEvent(ClickEvent.suggestCommand(
                                                "/rg setarea " + regionName)))
                                .append(text(" (select an area first with ", NamedTextColor.GRAY))
                                .append(text("/sel", NamedTextColor.YELLOW))
                                .append(text(")", NamedTextColor.GRAY)));

                TextUtil.send(
                        context,
                        text("  [▶] ", NamedTextColor.GRAY)
                                .append(text("Select area", NamedTextColor.YELLOW)
                                        .hoverEvent(HoverEvent.showText(text(
                                                "Click to select this region's area",
                                                OrbisText.EREBOR_GREEN)))
                                        .clickEvent(ClickEvent.suggestCommand(
                                                "/rg area select " + regionName))));

                int centerX = (min.x() + max.x()) / 2;
                int centerZ = (min.z() + max.z()) / 2;
                int centerY = (min.y() + max.y()) / 2;

                TextUtil.send(
                        context,
                        text("  [▶] ", NamedTextColor.GRAY)
                                .append(text("Teleport to center", NamedTextColor.YELLOW)
                                        .hoverEvent(HoverEvent.showText(text(
                                                "Click to teleport to the center of this region",
                                                OrbisText.EREBOR_GREEN)))
                                        .clickEvent(ClickEvent.runCommand("/tp @s " + centerX + " "
                                                + centerY + " " + centerZ)))
                                .append(text(" (", NamedTextColor.GRAY)
                                        .append(text(
                                                centerX + ", " + centerY + ", " + centerZ,
                                                NamedTextColor.WHITE))
                                        .append(text(")", NamedTextColor.GRAY))));
            }

            displayFlags(context, region, 5);

            Component membersLine = text("Members: ", OrbisText.EREBOR_GREEN)
                    .append(text("[", NamedTextColor.GRAY)
                            .append(text("+", NamedTextColor.GREEN)
                                    .hoverEvent(HoverEvent.showText(
                                            text("Click to add a member", OrbisText.EREBOR_GREEN)))
                                    .clickEvent(ClickEvent.suggestCommand(
                                            "/rg member add " + regionName + " ")))
                            .append(text("]", NamedTextColor.GRAY)));

            TextUtil.send(context, membersLine);

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
                                .hoverEvent(HoverEvent.showText(text(
                                        "Click to remove this member", OrbisText.SECONDARY_RED)))
                                .clickEvent(ClickEvent.suggestCommand("/rg member remove "
                                        + regionName + " " + typeName.value() + " " + value)));
                TextUtil.send(context, memberLine);
            }

            return CompletableFuture.completedFuture(null);
        }
    }

    private static class RegionWorldAddCommand extends AbstractCommand {

        private final RequiredArg<Region> regionArg;
        private final RequiredArg<RegionisedWorld> worldArg;

        public RegionWorldAddCommand() {
            super(
                    "add",
                    "Adds a region to a world set. The region will affect the world it is added into.");
            this.regionArg = this.withRequiredArg("region", "The region", OrbisArgTypes.REGION);
            this.worldArg =
                    this.withRequiredArg("world", "The world", OrbisArgTypes.REGIONISED_WORLD);
        }

        @Override
        protected @Nullable CompletableFuture<Void> execute(@NonNull CommandContext context) {
            final Region region = regionArg.get(context);
            final RegionisedWorld world = worldArg.get(context);
            if (region.isGlobal()) {
                TextUtil.send(
                        context, OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_GLOBAL));
                return CompletableFuture.completedFuture(null);
            }

            if (world.add(region)) {
                TextUtil.send(
                        context,
                        OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_ADDED.arguments(
                                Component.text(region.name()),
                                Component.text(world.worldId().orElseThrow().asString()))));
                return CompletableFuture.completedFuture(null);
            }

            TextUtil.send(
                    context,
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_ADD_FAILED.arguments(
                            Component.text(region.name()),
                            Component.text(world.worldId().orElseThrow().asString()))));
            return CompletableFuture.completedFuture(null);
        }
    }

    private static class RegionWorldRemoveCommand extends AbstractCommand {

        private final RequiredArg<Region> regionArg;
        private final RequiredArg<RegionisedWorld> worldArg;

        public RegionWorldRemoveCommand() {
            super("remove", "Removes a region from a world set.");
            this.regionArg = this.withRequiredArg("region", "The region", OrbisArgTypes.REGION);
            this.worldArg =
                    this.withRequiredArg("world", "The world", OrbisArgTypes.REGIONISED_WORLD);
        }

        @Override
        protected @Nullable CompletableFuture<Void> execute(@NonNull CommandContext context) {
            final Region region = regionArg.get(context);
            final RegionisedWorld world = worldArg.get(context);
            if (region.isGlobal()) {
                TextUtil.send(
                        context, OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_GLOBAL));
                return CompletableFuture.completedFuture(null);
            }

            if (world.remove(region)) {
                TextUtil.send(
                        context,
                        OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_REMOVED.arguments(
                                Component.text(region.name()),
                                Component.text(world.worldId().orElseThrow().asString()))));
                return CompletableFuture.completedFuture(null);
            }

            TextUtil.send(
                    context,
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_REMOVE_FAILED.arguments(
                            Component.text(region.name()),
                            Component.text(world.worldId().orElseThrow().asString()))));
            return CompletableFuture.completedFuture(null);
        }
    }

    private static Component createClickableLine(
            String label, String value, String command, String hoverText) {
        return text(label + ": ", OrbisText.EREBOR_GREEN)
                .append(text(value, NamedTextColor.WHITE)
                        .hoverEvent(HoverEvent.showText(text(hoverText, OrbisText.EREBOR_GREEN)))
                        .clickEvent(ClickEvent.suggestCommand(command)));
    }

    private static void displayFlags(CommandContext context, Region region, int limit) {
        final String regionName = region.name();

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

        TextUtil.send(context, flagsLine);

        Map<RegistryRegionFlag<?>, Set<String>> flags = new HashMap<>();
        for (RegistryRegionFlag<?> flag : OrbisRegistries.FLAGS) {
            region.getFlag(flag).ifPresent(mu -> {
                if (mu instanceof GroupedMutableRegionFlag<?> grouped) {
                    flags.put(
                            flag,
                            grouped.groups().stream()
                                    .map(FlagMemberGroup::name)
                                    .collect(HashSet::new, Set::add, Set::addAll));
                } else {
                    flags.put(flag, Collections.emptySet());
                }
            });
        }

        if (flags.isEmpty()) {
            if (limit != 0) {
                TextUtil.send(context, text("  No flags set.", NamedTextColor.GRAY));
            }
            return;
        }

        List<Map.Entry<RegistryRegionFlag<?>, Set<String>>> flagEntries =
                new ArrayList<>(flags.entrySet());
        boolean hasMore = limit > 0 && flagEntries.size() > limit;
        int displayCount = hasMore ? limit - 1 : flagEntries.size();

        for (int i = 0; i < displayCount; i++) {
            Map.Entry<RegistryRegionFlag<?>, Set<String>> entry = flagEntries.get(i);
            final RegistryRegionFlag<?> registryFlag = entry.getKey();
            final MutableRegionFlag<?> flag = region.getFlag(registryFlag).orElseThrow();
            Set<String> groups = entry.getValue();

            Component flagName = text(flag.key().asString(), NamedTextColor.WHITE);
            registryFlag
                    .description()
                    .ifPresent(description -> flagName.hoverEvent(
                            HoverEvent.showText(text(description, NamedTextColor.WHITE))));

            Component flagLine = text("  ", NamedTextColor.GRAY)
                    .append(flagName)
                    .append(text(": ", NamedTextColor.GRAY))
                    .append(text(flag.getValue().toString(), NamedTextColor.YELLOW));

            if (!groups.isEmpty()) {
                flagLine = flagLine.append(text(" (", NamedTextColor.GRAY))
                        .append(text(String.join(", ", groups), NamedTextColor.AQUA))
                        .append(text(")", NamedTextColor.GRAY));
            }

            flagLine = flagLine.append(space())
                    .append(text("[", NamedTextColor.GRAY))
                    .append(text("✎", NamedTextColor.YELLOW)
                            .hoverEvent(HoverEvent.showText(
                                    text("Click to modify this flag", OrbisText.SECONDARY_ORANGE)))
                            .clickEvent(ClickEvent.suggestCommand("/rg flag set " + regionName + " "
                                    + flag.key().asString() + " ")))
                    .append(text("]", NamedTextColor.GRAY));

            flagLine = flagLine.append(space())
                    .append(text("[", NamedTextColor.GRAY))
                    .append(text("-", NamedTextColor.RED)
                            .hoverEvent(HoverEvent.showText(
                                    text("Click to remove this flag", OrbisText.SECONDARY_RED)))
                            .clickEvent(ClickEvent.suggestCommand("/rg flag remove " + regionName
                                    + " " + flag.key().asString())))
                    .append(text("]", NamedTextColor.GRAY));

            TextUtil.send(context, flagLine);
        }

        if (hasMore) {
            int remaining = flagEntries.size() - displayCount;
            TextUtil.send(
                    context,
                    text(
                                    "  and " + remaining + " more...",
                                    NamedTextColor.GRAY,
                                    TextDecoration.ITALIC)
                            .hoverEvent(HoverEvent.showText(
                                    text("Click to view all flags", OrbisText.EREBOR_GREEN)))
                            .clickEvent(ClickEvent.runCommand("/rg flag list " + regionName)));
        }
    }
}
