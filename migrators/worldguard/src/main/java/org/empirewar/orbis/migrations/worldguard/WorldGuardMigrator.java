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
package org.empirewar.orbis.migrations.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionType;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.area.PolygonArea;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.MutableRegionFlag;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.member.PlayerMember;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.world.RegionisedWorld;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Is this class bad? Yes. Do I care? No, just migrate the data!
public final class WorldGuardMigrator {

    private static final Map<String, RegistryRegionFlag<?>> FLAG_MAPPINGS;

    static {
        FLAG_MAPPINGS = new HashMap<>(Map.of(
                "block-break",
                DefaultFlags.CAN_BREAK,
                "block-place",
                DefaultFlags.CAN_PLACE,
                "pvp",
                DefaultFlags.CAN_PVP,
                "damage-animals",
                DefaultFlags.DAMAGEABLE_ENTITIES,
                "fall-damage",
                DefaultFlags.FALL_DAMAGE,
                "item-drop",
                DefaultFlags.CAN_DROP_ITEM,
                "item-pickup",
                DefaultFlags.CAN_PICKUP_ITEM,
                "chest-access",
                DefaultFlags.BLOCK_INVENTORY_ACCESS,
                "use",
                DefaultFlags.TRIGGER_REDSTONE,
                "coral-fade",
                DefaultFlags.CORAL_DECAY));
        FLAG_MAPPINGS.putAll(Map.of(
                "leaf-decay",
                DefaultFlags.LEAF_DECAY,
                "block-trampling",
                DefaultFlags.BLOCK_TRAMPLE,
                "entity-painting-destroy",
                DefaultFlags.CAN_DESTROY_PAINTING,
                "entity-item-frame-destroy",
                DefaultFlags.CAN_DESTROY_ITEM_FRAME,
                "item-frame-rotation",
                DefaultFlags.ITEM_FRAME_ROTATE,
                "mushroom-growth",
                DefaultFlags.GROWABLE_BLOCKS,
                "vine-growth",
                DefaultFlags.GROWABLE_BLOCKS,
                "rock-growth",
                DefaultFlags.GROWABLE_BLOCKS));
        FLAG_MAPPINGS.putAll(Map.of(
                "crop-growth",
                DefaultFlags.GROWABLE_BLOCKS,
                "vehicle-destroy",
                DefaultFlags.CAN_DESTROY_VEHICLE,
                "entry",
                DefaultFlags.CAN_ENTER,
                "invincible",
                DefaultFlags.CAN_TAKE_MOB_DAMAGE_SOURCES,
                "fire-spread",
                DefaultFlags.FIRE_SPREAD,
                "time-lock",
                DefaultFlags.TIME,
                "greeting",
                DefaultFlags.ENTRY_MESSAGE,
                "farewell",
                DefaultFlags.EXIT_MESSAGE));
    }

    private static final Map<RegionFlag<?>, FlagTransformer> TRANSFORMERS = Map.of(
            DefaultFlags.DAMAGEABLE_ENTITIES,
                    (audience, region, flag, orbisRegion, orbisFlag) -> {
                        StateFlag stateFlag = (StateFlag) flag;
                        final StateFlag.State value = region.getFlag(stateFlag);
                        MutableRegionFlag<List<Key>> existing = (MutableRegionFlag<List<Key>>)
                                orbisRegion.getFlag(orbisFlag).orElse(null);
                        if (existing == null) {
                            existing = (MutableRegionFlag<List<Key>>) orbisFlag.asMutable();
                        }

                        if (value == StateFlag.State.ALLOW) {
                            if (flag.getName().equals("damage-animals")) {
                                final List<Key> valid = Registry.ENTITY_TYPE.stream()
                                        .filter(EntityType::isAlive)
                                        .map(Keyed::key)
                                        .toList();
                                existing.getValue().addAll(valid);
                            }
                        } else {
                            // State is DENY
                            if (!orbisRegion.hasFlag(orbisFlag)) {
                                // No entities can be attacked
                                orbisRegion.addFlag(orbisFlag);
                            }
                            // Else, do nothing. If not in list, not allowed.
                        }
                    },
            DefaultFlags.GROWABLE_BLOCKS,
                    (audience, region, flag, orbisRegion, orbisFlag) -> {
                        if (!orbisRegion.hasFlag(orbisFlag)) {
                            orbisRegion.addFlag(orbisFlag);
                        }

                        final MutableRegionFlag<List<Key>> mu = (MutableRegionFlag<List<Key>>)
                                orbisRegion.getFlag(orbisFlag).orElseThrow();
                        StateFlag stateFlag = (StateFlag) flag;
                        if (region.getFlag(stateFlag) != StateFlag.State.ALLOW) return;

                        final List<Key> value = mu.getValue();
                        switch (flag.getName()) {
                            case "mushroom-growth" -> {
                                value.add(Material.RED_MUSHROOM.key());
                                value.add(Material.BROWN_MUSHROOM.key());
                                value.add(Material.CRIMSON_FUNGUS.key());
                                value.add(Material.WARPED_FUNGUS.key());
                            }
                            case "vine-growth" -> {
                                value.add(Material.VINE.key());
                                value.add(Material.CAVE_VINES.key());
                                value.add(Material.TWISTING_VINES.key());
                                value.add(Material.WEEPING_VINES.key());
                            }
                            case "rock-growth" -> value.add(Material.DRIPSTONE_BLOCK.key());
                            case "crop-growth" -> {
                                for (Material cropsValue : Tag.CROPS.getValues()) {
                                    value.add(cropsValue.key());
                                }
                            }
                        }
                    });

    public WorldGuardMigrator(Audience actor) {
        final WorldGuard instance = WorldGuard.getInstance();

        final Audience audience = Audience.audience(actor, Bukkit.getConsoleSender());

        int errors = 0;

        int worldIndex = 1;
        for (World world : Bukkit.getWorlds()) {
            audience.sendMessage(Component.text(
                    "Processing worlds (" + worldIndex + "/"
                            + Bukkit.getWorlds().size() + "): " + world.getName(),
                    NamedTextColor.GREEN));
            final RegionManager regionManager =
                    instance.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            final RegionisedWorld regionisedWorld = OrbisAPI.get().getRegionisedWorld(world.key());
            for (ProtectedRegion region : regionManager.getRegions().values()) {
                // If added by another world
                final Optional<Region> possibleExisting =
                        OrbisRegistries.REGIONS.get(region.getId());
                if (possibleExisting.isPresent()) {
                    regionisedWorld.add(possibleExisting.get());
                    continue;
                }

                Area area = region.getType() == RegionType.POLYGON
                        ? new PolygonArea()
                        : new CuboidArea();

                audience.sendMessage(Component.text(
                        "Processing region " + region.getId() + "...", NamedTextColor.YELLOW));
                Region orbisRegion = region.getId().equals("__global__")
                        ? regionisedWorld.getByName(world.key().asString()).orElseThrow()
                        : new Region(region.getId(), area);

                if (!orbisRegion.isGlobal()) {
                    if (area instanceof PolygonArea) {
                        for (BlockVector2 point : region.getPoints()) {
                            area.addPoint(new Vector3i(point.getBlockX(), 0, point.getBlockZ()));
                        }
                    } else {
                        final BlockVector3 min = region.getMinimumPoint();
                        final BlockVector3 max = region.getMaximumPoint();
                        orbisRegion
                                .area()
                                .addPoint(new Vector3i(min.getX(), min.getY(), min.getZ()));
                        orbisRegion
                                .area()
                                .addPoint(new Vector3i(max.getX(), max.getY(), max.getZ()));
                    }
                }

                for (Flag<?> flag : region.getFlags().keySet()) {
                    final String name = flag.getName();
                    final RegistryRegionFlag<?> orbisFlag = FLAG_MAPPINGS.get(name);
                    if (orbisFlag == null) {
                        audience.sendMessage(Component.text(
                                "Unable to find flag '" + name + "'!", NamedTextColor.RED));
                        errors++;
                        continue;
                    }

                    final FlagTransformer transformer =
                            TRANSFORMERS.getOrDefault(orbisFlag, FlagTransformer.DEFAULT);
                    transformer.transform(audience, region, flag, orbisRegion, orbisFlag);
                }

                regionisedWorld.add(orbisRegion);
                OrbisRegistries.REGIONS.register(orbisRegion.key(), orbisRegion);
                audience.sendMessage(Component.text(
                        "Added region '" + orbisRegion.name() + "'.", NamedTextColor.GREEN));
            }

            for (ProtectedRegion region : regionManager.getRegions().values()) {
                final Optional<Region> orbisRegion = regionisedWorld.getByName(region.getId());
                if (orbisRegion.isEmpty()) continue;

                final ProtectedRegion parent = region.getParent();
                if (parent != null) {
                    final Optional<Region> orbisParentRegion =
                            regionisedWorld.getByName(parent.getId());
                    if (orbisParentRegion.isEmpty()) {
                        audience.sendMessage(Component.text(
                                "Unable to find parent '" + parent.getId() + "' for region '"
                                        + region.getId() + "'.",
                                NamedTextColor.RED));
                        errors++;
                        continue;
                    }

                    orbisRegion.get().addParent(orbisParentRegion.get());
                    audience.sendMessage(Component.text(
                            "Added parent '" + parent.getId() + "' to '" + region.getId() + "'.",
                            NamedTextColor.GREEN));
                }

                for (UUID uniqueId : region.getMembers().getUniqueIds()) {
                    orbisRegion.get().addMember(new PlayerMember(uniqueId));
                }

                // TODO member groups
            }

            for (Region region : regionisedWorld.regions()) {
                // Apply worldguard default protection rules
                if (region.query(RegionQuery.Flag.builder(DefaultFlags.CAN_BREAK))
                        .result()
                        .isEmpty()) {
                    region.addFlag(DefaultFlags.CAN_BREAK).setValue(false);
                }

                if (region.query(RegionQuery.Flag.builder(DefaultFlags.CAN_PLACE))
                        .result()
                        .isEmpty()) {
                    region.addFlag(DefaultFlags.CAN_PLACE).setValue(false);
                }
            }
            worldIndex++;
        }

        audience.sendMessage(Component.text("Migration complete.", NamedTextColor.GREEN));
        if (errors > 0) {
            audience.sendMessage(
                    Component.text("WARNING: ", NamedTextColor.RED, TextDecoration.BOLD)
                            .append(Component.text(
                                    "Migration completed with " + errors
                                            + " errors. Check logs for more information.",
                                    NamedTextColor.YELLOW)));
        }

        // spotless:off
        audience.sendMessage(Component.text("What's next?", NamedTextColor.YELLOW, TextDecoration.BOLD));
        audience.sendMessage(Component.text(" 1) Stop your server.", NamedTextColor.GRAY));
        audience.sendMessage(Component.text(" 2) Remove the WorldGuard plugin.", NamedTextColor.GRAY));
        audience.sendMessage(Component.text(" 3) Start your server again. Welcome to Orbis!", NamedTextColor.GRAY));
        audience.sendMessage(Component.text("Make sure to check for any migration errors. Most should be harmless, but you may need to adjust some flags.", NamedTextColor.YELLOW));
        // spotless:on
    }
}
