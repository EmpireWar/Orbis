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
package org.empirewar.orbis.migrations.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class WorldGuardMigrator {

    private static final Map<String, RegionFlag<?>> FLAG_MAPPINGS;

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
                DefaultFlags.CAN_DROP_ITEMS,
                "item-pickup",
                DefaultFlags.CAN_PICKUP_ITEMS,
                "chest-access",
                DefaultFlags.BLOCK_INVENTORY_ACCESS,
                "use",
                DefaultFlags.TRIGGER_REDSTONE,
                "coral-fade",
                DefaultFlags.CORAL_DECAY));
        FLAG_MAPPINGS.putAll(Map.of("leaf-decay", DefaultFlags.LEAF_DECAY));
    }

    private static final Map<RegionFlag<?>, FlagTransformer> TRANSFORMERS = Map.of(
            DefaultFlags.DAMAGEABLE_ENTITIES, (audience, region, flag, orbisRegion, orbisFlag) -> {
                StateFlag stateFlag = (StateFlag) flag;
                final StateFlag.State value = region.getFlag(stateFlag);
                // Don't add if allowed - all entities can be attacked
                if (value == StateFlag.State.ALLOW) {
                    return;
                }

                // No entities can be attacked
                orbisRegion.addFlag(orbisFlag);
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
            final RegionisedWorld regionisedWorld =
                    OrbisAPI.get().getRegionisedWorld(world.getUID());
            for (ProtectedRegion region : regionManager.getRegions().values()) {
                // If added by another world
                final Optional<Region> possibleExisting =
                        OrbisAPI.get().getGlobalWorld().getByName(region.getId());
                if (possibleExisting.isPresent()) {
                    regionisedWorld.add(possibleExisting.get());
                    continue;
                }

                audience.sendMessage(Component.text(
                        "Processing region " + region.getId() + "...", NamedTextColor.YELLOW));
                Region orbisRegion = region.getId().equals("__global__")
                        ? regionisedWorld.getByName(world.key().asString()).orElseThrow()
                        : new Region(region.getId());

                if (!orbisRegion.isGlobal()) {
                    // TODO poly support
                    final BlockVector3 min = region.getMinimumPoint();
                    final BlockVector3 max = region.getMaximumPoint();
                    orbisRegion.area().addPoint(new Vector3i(min.getX(), min.getY(), min.getZ()));
                    orbisRegion.area().addPoint(new Vector3i(max.getX(), max.getY(), max.getZ()));
                }

                for (Flag<?> flag : region.getFlags().keySet()) {
                    final String name = flag.getName();
                    final RegionFlag<?> orbisFlag = FLAG_MAPPINGS.get(name);
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
                OrbisAPI.get().getGlobalWorld().add(orbisRegion);
                audience.sendMessage(Component.text(
                        "Added region '" + orbisRegion.name() + "'.", NamedTextColor.GREEN));
            }

            for (ProtectedRegion region : regionManager.getRegions().values()) {
                final ProtectedRegion parent = region.getParent();
                if (parent != null) {
                    final Optional<Region> orbisRegion = regionisedWorld.getByName(region.getId());
                    if (orbisRegion.isEmpty()) continue;
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
        // spotless:on
    }
}
