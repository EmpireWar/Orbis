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
package org.empirewar.orbis.paper;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.paper.command.Commands;
import org.empirewar.orbis.paper.listener.BlockActionListener;
import org.empirewar.orbis.paper.listener.InteractEntityListener;
import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;
import org.empirewar.orbis.world.RegionisedWorldSet;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrbisPaper extends JavaPlugin implements Orbis, Listener {

    private final RegionisedWorldSet globalSet = new RegionisedWorldSet();
    private final Map<UUID, RegionisedWorldSet> worldSets = new HashMap<>();

    @Override
    public void onLoad() {
        OrbisAPI.set(this);
    }

    @Override
    public void onEnable() {
        this.registerListeners();
        new Commands(this);
        this.loadConfig();
        try {
            this.loadRegions();
        } catch (IOException e) {
            logger().error("Error loading regions", e);
        }
        Bukkit.getWorlds().forEach(this::loadWorld);
    }

    @Override
    public void onDisable() {
        try {
            saveRegions();
        } catch (IOException e) {
            logger().error("Error saving regions", e);
        }
    }

    @EventHandler
    public void onLoad(WorldLoadEvent event) {
        this.loadWorld(event.getWorld());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUnload(WorldUnloadEvent event) {
        this.worldSets.remove(event.getWorld().getUID());
    }

    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigurationNode rootNode;

    private void loadConfig() {
        try {
            final Path configPath = dataFolder().resolve("config.yml");
            saveResource("config.yml", false);

            loader = YamlConfigurationLoader.builder().path(configPath).build();
            rootNode = loader.load();
        } catch (IOException e) {
            logger().error("Error loading config", e);
        }
    }

    @Override
    public void saveConfig() {
        try {
            loader.save(config());
        } catch (ConfigurateException e) {
            logger().error("Error saving config", e);
        }
    }

    private void registerListeners() {
        final PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(new BlockActionListener(this), this);
        pluginManager.registerEvents(new InteractEntityListener(this), this);
    }

    private void loadWorld(World world) {
        try {
            final List<String> regionNames = config().node(
                            "worlds", world.key().asString(), "regions")
                    .getList(String.class, new ArrayList<>());

            final RegionisedWorldSet set =
                    new RegionisedWorldSet(world.getUID(), world.getKey().asString());

            List<Region> regions = new ArrayList<>();
            Region globalRegion = OrbisAPI.get()
                    .getGlobalWorld()
                    .getByName(set.worldName().orElseThrow())
                    .filter(r -> {
                        // Can somebody really be this stupid?
                        if (!r.isGlobal()) {
                            logger().error(
                                            "Region name conflict! Region with name {} exists and matches a world name, but it is not global! Destroying invalid region!",
                                            set.worldName().orElseThrow());
                            OrbisAPI.get().getGlobalWorld().remove(r);
                            OrbisAPI.get().getRegionisedWorlds().forEach(rw -> rw.remove(r));
                            return false;
                        }
                        return true;
                    })
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
            worldSets.put(world.getUID(), set);
        } catch (SerializationException e) {
            logger().error("Error loading world set {}", world.getUID(), e);
        }
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
    public RegionisedWorld getRegionisedWorld(UUID worldId) {
        return worldSets.get(worldId);
    }

    @Override
    public Path dataFolder() {
        return getDataFolder().toPath();
    }

    @Override
    public ConfigurationNode config() {
        return rootNode;
    }

    @Override
    public Logger logger() {
        return getSLF4JLogger();
    }
}
