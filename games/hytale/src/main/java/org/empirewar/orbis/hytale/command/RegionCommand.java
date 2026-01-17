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

import static net.kyori.adventure.text.Component.text;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import net.kyori.adventure.text.Component;

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
import org.empirewar.orbis.hytale.command.arguments.OrbisArgTypes;
import org.empirewar.orbis.hytale.ui.RegionInfoPage;
import org.empirewar.orbis.hytale.util.TextUtil;
import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.util.OrbisText;
import org.empirewar.orbis.util.OrbisTranslations;
import org.empirewar.orbis.util.Permissions;
import org.empirewar.orbis.world.RegionisedWorld;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RegionCommand extends AbstractCommandCollection {

    public RegionCommand() {
        super("region", "The base Orbis region command.");
        this.addAliases("rg");
        this.requirePermission(Permissions.MANAGE);
        this.addSubCommand(new RegionCreateCommand());
        this.addSubCommand(new RegionVisualiseCommand());
        this.addSubCommand(new RegionInfoCommand());
        this.addSubCommand(new RegionWorldCommand());
    }

    private static class RegionCreateCommand extends CommandBase {

        private final RequiredArg<String> regionNameArg;
        private final OptionalArg<AreaType<?>> areaTypeArg;
        private final FlagArg globalArg, ignoreSelection;

        protected RegionCreateCommand() {
            super(
                    "create",
                    "Creates a new region. "
                            + "If your selection using the wand is empty, we will instead use your selector builder tool selection to create a cuboid region.");
            this.regionNameArg =
                    this.withRequiredArg("name", "The name of the region.", ArgTypes.STRING);
            this.areaTypeArg = this.withOptionalArg(
                    "area_type", "The area type of the region.", OrbisArgTypes.AREA_TYPE);
            this.globalArg = this.withFlagArg("global", "Create the region as a global region.");
            this.ignoreSelection =
                    this.withFlagArg("ignore-selection", "Ignore your current selection.");
        }

        @Override
        protected void executeSync(@NonNull CommandContext context) {
            final String regionName = this.regionNameArg.get(context);
            AreaType<?> areaType = this.areaTypeArg.get(context);
            final boolean global = this.globalArg.get(context);
            final boolean ignoreSelection = this.ignoreSelection.get(context);

            final Orbis orbis = OrbisAPI.get();
            if (OrbisRegistries.REGIONS.get(regionName).isPresent()) {
                TextUtil.send(
                        context,
                        OrbisText.PREFIX.append(OrbisTranslations.REGION_ALREADY_EXISTS.arguments(
                                Component.text(regionName))));
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
            } else if (context.sender() instanceof Player player) {

                final Selection selection = orbis.selectionManager()
                        .get(player.getUuid())
                        .orElseGet(() -> {
                            final BlockSelection blockSelection = BuilderToolsPlugin.get()
                                    .getBuilderState(player, player.getPlayerRef())
                                    .getSelection();
                            if (blockSelection != null && blockSelection.hasSelectionBounds()) {
                                Selection sel = new Selection(AreaType.CUBOID);
                                final com.hypixel.hytale.math.vector.Vector3i min =
                                        blockSelection.getSelectionMin();
                                final com.hypixel.hytale.math.vector.Vector3i max =
                                        blockSelection.getSelectionMax();
                                sel.addPoint(new Vector3i(min.x, min.y, min.z));
                                sel.addPoint(new Vector3i(max.x, max.y, max.z));
                                return sel;
                            }

                            return null;
                        });

                if (selection == null) {
                    TextUtil.send(
                            context,
                            OrbisText.PREFIX.append(OrbisTranslations.REGION_SELECTION_REQUIRED));
                    return;
                }

                AreaType<?> defaultedType =
                        areaType == null ? selection.getSelectionType() : areaType;
                if (selection.getSelectionType() != defaultedType) {
                    TextUtil.send(
                            context,
                            OrbisText.PREFIX.append(
                                    OrbisTranslations.REGION_SELECTION_TYPE_MISMATCH.arguments(
                                            Component.text(OrbisRegistries.AREA_TYPE
                                                    .getKey(defaultedType)
                                                    .orElseThrow()
                                                    .asString()),
                                            Component.text(OrbisRegistries.AREA_TYPE
                                                    .getKey(selection.getSelectionType())
                                                    .orElseThrow()
                                                    .asString()))));
                    return;
                }

                try {
                    area = selection.build();
                } catch (IncompleteAreaException e) {
                    TextUtil.send(
                            context,
                            OrbisText.PREFIX.append(
                                    OrbisTranslations.REGION_INCOMPLETE_SELECTION.arguments(
                                            text(e.getMessage()))));
                    return;
                }

                selection.getPoints().forEach(area::addPoint);
                TextUtil.send(
                        context,
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

            final Region region =
                    global ? new GlobalRegion(regionName) : new Region(regionName, area);
            OrbisRegistries.REGIONS.register(regionName, region);
            if (context.sender() instanceof Player player && !region.isGlobal()) {
                orbis.getRegionisedWorld(orbis.getPlayerWorld(player.getUuid())).add(region);
            }
            TextUtil.send(
                    context,
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_CREATED.arguments(
                            Component.text(regionName), Component.text(global ? "global " : ""))));
        }
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

    private static class RegionInfoCommand extends AbstractPlayerCommand {

        private final RequiredArg<Region> regionArg;

        public RegionInfoCommand() {
            super("info", "Displays detailed information about a region.");
            this.regionArg = this.withRequiredArg("region", "The region", OrbisArgTypes.REGION);
        }

        @Override
        protected void execute(
                @NonNull CommandContext context,
                @NonNull Store<EntityStore> store,
                @NonNull Ref<EntityStore> ref,
                @NonNull PlayerRef playerRef,
                @NonNull World world) {
            final Region region = regionArg.get(context);
            final Player player = store.getComponent(ref, Player.getComponentType());

            if (player == null) {
                TextUtil.send(
                        context,
                        OrbisText.PREFIX.append(Component.text(
                                "Unable to open the region interface right now.",
                                OrbisText.SECONDARY_RED)));
                return;
            }

            CompletableFuture.runAsync(
                    () -> player.getPageManager()
                            .openCustomPage(
                                    ref,
                                    store,
                                    new RegionInfoPage(
                                            playerRef, CustomPageLifetime.CanDismiss, region)),
                    world);
        }
    }

    private static class RegionWorldAddCommand extends CommandBase {

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
        protected void executeSync(@NonNull CommandContext context) {
            final Region region = regionArg.get(context);
            final RegionisedWorld world = worldArg.get(context);
            if (region.isGlobal()) {
                TextUtil.send(
                        context, OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_GLOBAL));
                return;
            }

            if (world.add(region)) {
                TextUtil.send(
                        context,
                        OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_ADDED.arguments(
                                Component.text(region.name()),
                                Component.text(world.worldId().orElseThrow().asString()))));
                return;
            }

            TextUtil.send(
                    context,
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_ADD_FAILED.arguments(
                            Component.text(region.name()),
                            Component.text(world.worldId().orElseThrow().asString()))));
        }
    }

    private static class RegionWorldRemoveCommand extends CommandBase {

        private final RequiredArg<Region> regionArg;
        private final RequiredArg<RegionisedWorld> worldArg;

        public RegionWorldRemoveCommand() {
            super("remove", "Removes a region from a world set.");
            this.regionArg = this.withRequiredArg("region", "The region", OrbisArgTypes.REGION);
            this.worldArg =
                    this.withRequiredArg("world", "The world", OrbisArgTypes.REGIONISED_WORLD);
        }

        @Override
        protected void executeSync(@NonNull CommandContext context) {
            final Region region = regionArg.get(context);
            final RegionisedWorld world = worldArg.get(context);
            if (region.isGlobal()) {
                TextUtil.send(
                        context, OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_GLOBAL));
                return;
            }

            if (world.remove(region)) {
                TextUtil.send(
                        context,
                        OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_REMOVED.arguments(
                                Component.text(region.name()),
                                Component.text(world.worldId().orElseThrow().asString()))));
                return;
            }

            TextUtil.send(
                    context,
                    OrbisText.PREFIX.append(OrbisTranslations.REGION_WORLD_REMOVE_FAILED.arguments(
                            Component.text(region.name()),
                            Component.text(world.worldId().orElseThrow().asString()))));
        }
    }
}
