/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2025 Empire War
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
package org.empirewar.orbis;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.registry.lifecycle.RegistryLifecycles;
import org.empirewar.orbis.selection.SelectionManager;
import org.empirewar.orbis.serialization.StaticGsonProvider;
import org.empirewar.orbis.world.RegionisedWorld;
import org.empirewar.orbis.world.RegionisedWorldSet;
import org.jetbrains.annotations.Nullable;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Represents a platform for running the Orbis plugin.
 */
public abstract class OrbisPlatform implements Orbis {

    private static final String GLOBAL_REGION_ID = "orbis:global";

    private final SelectionManager selectionManager = new SelectionManager();
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

    public void loadWorld(Key world) {
        this.loadWorld(world, null);
    }

    public void loadWorld(Key world, @Nullable UUID worldId) {
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

            final RegionisedWorldSet set = new RegionisedWorldSet(world);
            final String worldKeyName = set.worldId().orElseThrow().asString();

            final List<Region> regions = new ArrayList<>(regionNames.size());

            // Add the global set that encompasses the world
            // (either already exists from a loaded file, or is created here)
            Region globalSetRegion = OrbisRegistries.REGIONS
                    .get(worldKeyName)
                    .orElseGet(() ->
                            OrbisRegistries.REGIONS.register(worldKeyName, new GlobalRegion(set)));

            for (String regionName : regionNames) {
                if (regionName.equals(worldKeyName)) {
                    logger().error("Illegal region name in world set");
                    continue;
                }

                final Region region = OrbisRegistries.REGIONS.get(regionName).orElse(null);
                if (region == null) {
                    logger().warn(
                                    "Region by name '{}' could not be found, ignoring...",
                                    regionName);
                    continue;
                }

                regions.add(region);
            }

            // Add the global region for *this* world
            set.add(globalSetRegion);
            // Also add the global region that is a part of all worlds
            set.add(OrbisRegistries.REGIONS.get(GLOBAL_REGION_ID).orElseThrow());
            // Now add all the regions that are part of this world
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
        // Flags should no longer be changed
        // (might be called multiple times in test environments)
        if (!isTestEnvironment()
                || OrbisRegistries.FLAGS.getLifecycle() != RegistryLifecycles.frozen()) {
            OrbisRegistries.FLAGS.setLifecycle(RegistryLifecycles.frozen());
        }

        File regionsFolder = dataFolder().resolve("regions").toFile();
        if (!regionsFolder.exists()) regionsFolder.mkdirs();
        for (File regionFile : regionsFolder.listFiles()) {
            if (!regionFile.getName().endsWith(".json")) continue;
            try (FileReader reader = new FileReader(regionFile)) {
                final Region region = StaticGsonProvider.GSON.fromJson(reader, Region.class);
                if (region == null) {
                    logger().error(
                                    "Error loading region from '{}' - is the file corrupted?",
                                    regionFile);
                    continue;
                }
                OrbisRegistries.REGIONS.register(region.name(), region);
            }
        }

        // Global region encompassing all worlds is either already loaded from a file, or is created
        // here
        final boolean hasGlobalRegion =
                OrbisRegistries.REGIONS.get(GLOBAL_REGION_ID).isPresent();
        if (!hasGlobalRegion) {
            // Add the global region that encompasses all worlds
            final GlobalRegion globalRegion = new GlobalRegion(GLOBAL_REGION_ID);
            globalRegion.priority(0);
            OrbisRegistries.REGIONS.register(GLOBAL_REGION_ID, globalRegion);
        }

        // Regions can still be changed at runtime
        // (might be called multiple times in test environments)
        if (!isTestEnvironment()
                || OrbisRegistries.REGIONS.getLifecycle() != RegistryLifecycles.active()) {
            OrbisRegistries.REGIONS.setLifecycle(RegistryLifecycles.active());
        }
    }

    public void saveRegions() throws IOException {
        File regionsFolder = dataFolder().resolve("regions").toFile();
        if (!regionsFolder.exists()) regionsFolder.mkdirs();

        for (Region region : OrbisRegistries.REGIONS.getAll()) {
            File regionFile = new File(
                    regionsFolder + File.separator + region.name().replace(":", "-") + ".json");
            try (FileWriter writer = new FileWriter(regionFile)) {
                StaticGsonProvider.GSON.toJson(region, writer);
            }
        }

        for (RegionisedWorld world : getRegionisedWorlds()) {
            this.saveWorldSet(world);
        }
    }

    public void saveWorld(Key world) {
        this.saveWorld(world, null);
    }

    public void saveWorld(Key world, @Nullable UUID worldId) {
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
                worldsConfig().node("worlds", world.worldId().orElseThrow().asString(), "regions");
        final List<String> regionsInWorld = new ArrayList<>();
        for (Region region : world.regions()) {
            if (region.name().equals(world.worldId().orElseThrow().asString())) continue;
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
        anySucceeded = OrbisRegistries.REGIONS.unregister(region.key()).isPresent() || anySucceeded;

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

    public boolean isTestEnvironment() {
        return false;
    }
}
