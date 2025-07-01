/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2025 Empire War
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

import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.selection.SelectionManager;
import org.empirewar.orbis.serialization.StaticGsonProvider;
import org.empirewar.orbis.serialization.context.CodecContext;
import org.empirewar.orbis.world.RegionisedWorld;
import org.empirewar.orbis.world.RegionisedWorldSet;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Represents a platform for running the Orbis plugin.
 */
public abstract class OrbisPlatform implements Orbis {

    private final SelectionManager selectionManager = new SelectionManager();
    private final RegionisedWorldSet globalSet = new RegionisedWorldSet();
    private final Map<Key, RegionisedWorldSet> worldSets = new ConcurrentHashMap<>();

    // Players currently visualizing regions
    private final Set<UUID> visualisingPlayers = ConcurrentHashMap.newKeySet();

    public OrbisPlatform() {
        OrbisAPI.set(this);
    }

    protected void load() {
        try {
            this.loadConfigs();
        } catch (IOException e) {
            logger().error("Error loading configs", e);
        }
    }

    protected abstract InputStream getResourceAsStream(String path);

    private ConfigurationLoader<CommentedConfigurationNode> loaderRoot, loaderWorlds;
    private ConfigurationNode rootNode, worldsNode;

    private void loadConfigs() throws IOException {
        final File dataFolderFile = dataFolder().toFile();
        if (!dataFolderFile.exists()) {
            dataFolderFile.mkdirs();
        }

        final Path configPath = dataFolder().resolve("config.yml");

        final Path worldsPath = dataFolder().resolve("worlds.yml");
        if (!Files.exists(worldsPath)) {
            // Support migration from config file -> worlds file (early indev - can be removed at
            // some point)
            if (Files.exists(configPath)) {
                Files.copy(configPath, worldsPath);
            } else {
                Files.createFile(worldsPath);
            }
        }

        loaderWorlds = YamlConfigurationLoader.builder().path(worldsPath).build();
        worldsNode = loaderWorlds.load();

        try {
            Files.copy(getResourceAsStream("/assets/orbis/config.yml"), configPath);
        } catch (FileAlreadyExistsException ignored) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        loaderRoot = YamlConfigurationLoader.builder().path(configPath).build();
        rootNode = loaderRoot.load();
    }

    @Override
    public ConfigurationNode config() {
        return rootNode;
    }

    @Override
    public ConfigurationNode worldsConfig() {
        return worldsNode;
    }

    public void loadWorld(Key world, UUID worldId) {
        try {
            final List<String> regionNames = new ArrayList<>();
            final ConfigurationNode worldsNode = worldsConfig().node("worlds");
            for (Object configWorldName : worldsNode.childrenMap().keySet()) {
                String stringConfigWorldName = (String) configWorldName;
                if (world.asString().matches(stringConfigWorldName)) {
                    regionNames.addAll(worldsNode
                            .node(stringConfigWorldName, "regions")
                            .getList(String.class, new ArrayList<>()));
                }
            }

            final RegionisedWorldSet set = new RegionisedWorldSet(world, world.asString());

            List<Region> regions = new ArrayList<>(regionNames.size());
            Region globalRegion = OrbisAPI.get()
                    .getGlobalWorld()
                    .getByName(set.worldName().orElseThrow())
                    .orElseGet(() -> new GlobalRegion(set));
            globalRegion.priority(0);
            OrbisAPI.get().getGlobalWorld().add(globalRegion);
            for (String regionName : regionNames) {
                if (regionName.equals(set.worldName().orElseThrow())) {
                    logger().error("Illegal region name in world set");
                    continue;
                }

                final Region region =
                        OrbisAPI.get().getGlobalWorld().getByName(regionName).orElse(null);
                if (region == null) {
                    logger().warn(
                                    "Region by name '{}' could not be found, ignoring...",
                                    regionName);
                    continue;
                }

                regions.add(region);
            }

            set.add(globalRegion);
            regions.forEach(set::add);
            worldSets.put(world, set);
            logger().info(
                            "Loaded world set {} ({}) with {} regions",
                            worldId,
                            world.asMinimalString(),
                            regions.size());
        } catch (SerializationException e) {
            logger().error("Error loading world set {} ({})", worldId, world.asMinimalString(), e);
        }
    }

    public void loadRegions() throws IOException {
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

    public void saveRegions() throws IOException {
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
            this.saveWorldSet(world);
        }
    }

    public void saveWorld(Key world, UUID worldId) {
        final RegionisedWorldSet removed = worldSets.remove(world);
        if (removed == null) {
            this.logger()
                    .warn(
                            "Unable to save world set {} ({}) because it could not be found",
                            worldId,
                            world.asMinimalString());
            return;
        }

        try {
            this.saveWorldSet(removed);
        } catch (IOException e) {
            this.logger()
                    .error(
                            "Error saving world set {} ({})",
                            worldId,
                            world.key().asMinimalString(),
                            e);
        }
    }

    private void saveWorldSet(RegionisedWorld world) throws IOException {
        world.worldId().ifPresent(id -> logger().info("Saving world {}", id));
        final ConfigurationNode node =
                worldsConfig().node("worlds", world.worldName().orElseThrow(), "regions");
        final List<String> regionsInWorld = new ArrayList<>();
        for (Region region : world.regions()) {
            if (region.name().equals(world.worldName().orElseThrow())) continue;
            regionsInWorld.add(region.name());
        }
        node.setList(String.class, regionsInWorld);

        try {
            loaderWorlds.save(worldsConfig());
        } catch (ConfigurateException e) {
            logger().error("Error saving worlds", e);
        }
    }

    @Override
    public boolean removeRegion(Region region) {
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

    @Override
    public SelectionManager selectionManager() {
        return selectionManager;
    }

    @Override
    public Set<RegionisedWorld> getRegionisedWorlds() {
        return worldSets.values().stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public RegionisedWorld getGlobalWorld() {
        return globalSet;
    }

    @Override
    public RegionisedWorld getRegionisedWorld(Key worldId) {
        return worldSets.get(worldId);
    }

    /**
     * Returns true if the player is currently visualising regions.
     */
    public boolean isVisualising(UUID uuid) {
        return visualisingPlayers.contains(uuid);
    }

    /**
     * Sets whether the player is visualising regions.
     */
    public void setVisualising(UUID uuid, boolean visualising) {
        if (visualising) visualisingPlayers.add(uuid);
        else visualisingPlayers.remove(uuid);
    }

    /**
     * Returns the set of all visualising player UUIDs.
     */
    public Set<UUID> getVisualisingPlayers() {
        return visualisingPlayers;
    }
}
