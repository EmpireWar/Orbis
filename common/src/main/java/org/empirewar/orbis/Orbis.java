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
package org.empirewar.orbis;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.selection.SelectionManager;
import org.empirewar.orbis.serialization.StaticGsonProvider;
import org.empirewar.orbis.serialization.context.CodecContext;
import org.empirewar.orbis.world.RegionisedWorld;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The core Orbis instance for a platform that manages worlds and players.
 */
public interface Orbis {

    SelectionManager selectionManager();

    /**
     * Gets all regionised worlds.
     * <p>
     * This does not contain the {@link #getGlobalWorld()}.
     * @return {@link Set} of regionised worlds
     */
    Set<RegionisedWorld> getRegionisedWorlds();

    /**
     * Gets the "global world" which holds all regions.
     * @return global world holding all regions
     */
    RegionisedWorld getGlobalWorld();

    RegionisedWorld getRegionisedWorld(Key worldId);

    Key getPlayerWorld(UUID player);

    boolean hasPermission(UUID player, String permission);

    Path dataFolder();

    ConfigurationNode config();

    void saveConfig();

    Logger logger();

    default void loadRegions() throws IOException {
        File regionsFolder = dataFolder().resolve("regions").toFile();
        if (!regionsFolder.exists()) regionsFolder.mkdirs();
        for (File regionFile : regionsFolder.listFiles()) {
            if (!regionFile.getName().endsWith(".json")) continue;
            try (FileReader reader = new FileReader(regionFile)) {
                final Region region = StaticGsonProvider.GSON.fromJson(reader, Region.class);
                if (region == null) throw new IllegalArgumentException("GSON returned null");
                getGlobalWorld().add(region);
            }
        }
        CodecContext.freeze();
    }

    default void saveRegions() throws IOException {
        File regionsFolder = dataFolder().resolve("regions").toFile();
        if (!regionsFolder.exists()) regionsFolder.mkdirs();

        for (Region region : getGlobalWorld().regions()) {
            File regionFile = new File(
                    regionsFolder + File.separator + region.name().replace(":", "-") + ".json");
            try (FileWriter writer = new FileWriter(regionFile)) {
                StaticGsonProvider.GSON.toJson(region, writer);
            }
        }

        for (RegionisedWorld world : OrbisAPI.get().getRegionisedWorlds()) {
            this.saveWorld(world);
        }
    }

    default void saveWorld(RegionisedWorld world) throws IOException {
        world.worldId().ifPresent(id -> logger().info("Saving world {}", id));
        final ConfigurationNode node =
                config().node("worlds", world.worldName().orElseThrow(), "regions");
        final List<String> regionsInWorld = new ArrayList<>();
        for (Region region : world.regions()) {
            if (region.name().equals(world.worldName().orElseThrow())) continue;
            regionsInWorld.add(region.name());
        }
        node.setList(String.class, regionsInWorld);
        saveConfig();
    }

    default boolean removeRegion(Region region) {
        boolean anySucceeded = false;
        for (RegionisedWorld world : getRegionisedWorlds()) {
            anySucceeded = world.remove(region) || anySucceeded;
        }
        anySucceeded = getGlobalWorld().remove(region) || anySucceeded;

        File regionsFolder = dataFolder().resolve("regions").toFile();
        File regionFile = new File(
                regionsFolder + File.separator + region.name().replace(":", "-") + ".json");
        return anySucceeded && regionFile.delete();
    }
}
