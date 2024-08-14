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
package org.empirewar.orbis.sponge;

import com.google.inject.Inject;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.selection.SelectionManager;
import org.empirewar.orbis.sponge.command.SpongeCommands;
import org.empirewar.orbis.sponge.key.SpongeDataKeys;
import org.empirewar.orbis.sponge.listener.BlockActionListener;
import org.empirewar.orbis.sponge.listener.ConnectionListener;
import org.empirewar.orbis.sponge.listener.InteractEntityListener;
import org.empirewar.orbis.sponge.listener.MovementListener;
import org.empirewar.orbis.sponge.selection.SelectionListener;
import org.empirewar.orbis.world.RegionisedWorld;
import org.empirewar.orbis.world.RegionisedWorldSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Plugin("orbis")
public class OrbisSponge implements Orbis {

    private final SelectionManager selectionManager = new SelectionManager();
    private final RegionisedWorldSet globalSet = new RegionisedWorldSet();
    private final Map<Key, RegionisedWorldSet> worldSets = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger("orbis");
    private final PluginContainer pluginContainer;

    public PluginContainer pluginContainer() {
        return pluginContainer;
    }

    public static OrbisSponge get() {
        return (OrbisSponge) OrbisAPI.get();
    }

    private final @ConfigDir(sharedRoot = false) Path dataFolder;

    public Path getDataFolder() {
        return dataFolder;
    }

    @Inject
    public OrbisSponge(
            PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path dataFolder) {
        this.pluginContainer = pluginContainer;
        this.dataFolder = dataFolder;
        OrbisAPI.set(this);
        new SpongeCommands(this);
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        this.loadConfig();
        this.registerListeners();
        try {
            this.loadRegions();
        } catch (IOException e) {
            logger().error("Error loading regions", e);
        }
    }

    @Listener
    public void onServerStarted(final StartedEngineEvent<Server> event) {
        SpongeDataKeys.initialized();
        Sponge.server().worldManager().worlds().forEach(this::loadWorld);
    }

    @Listener
    public void onServerStopping(final StoppingEngineEvent<Server> event) {
        try {
            saveRegions();
        } catch (IOException e) {
            logger().error("Error saving regions", e);
        }
    }

    @Listener
    private void onRegisterData(final RegisterDataEvent event) {
        SpongeDataKeys.register(event);
    }

    @Listener
    public void onWorldLoad(LoadWorldEvent event) {
        this.loadWorld(event.world());
    }

    @Listener(order = Order.LATE)
    public void onWorldUnload(UnloadWorldEvent event) {
        final ServerWorld world = event.world();
        final RegionisedWorldSet set = this.worldSets.remove(world.key());
        try {
            this.saveWorld(set);
        } catch (IOException e) {
            logger().error(
                            "Error saving world set {} ({})",
                            world.uniqueId(),
                            world.key().asMinimalString(),
                            e);
        }
    }

    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigurationNode rootNode;

    private void loadConfig() {
        try {
            final File dataFolderFile = dataFolder().toFile();
            if (!dataFolderFile.exists()) {
                dataFolderFile.mkdirs();
            }

            final Path configPath = dataFolder().resolve("config.yml");
            try {
                Files.copy(
                        pluginContainer
                                .openResource(URI.create("/assets/orbis/config.yml"))
                                .orElseThrow(),
                        configPath);
            } catch (FileAlreadyExistsException ignored) {
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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
        final EventManager eventManager = Sponge.eventManager();
        eventManager.registerListeners(pluginContainer, new BlockActionListener(this));
        eventManager.registerListeners(pluginContainer, new InteractEntityListener(this));
        eventManager.registerListeners(pluginContainer, new MovementListener(this));
        eventManager.registerListeners(pluginContainer, new ConnectionListener(this));
        eventManager.registerListeners(pluginContainer, new SelectionListener(this));
    }

    private void loadWorld(ServerWorld world) {
        final ResourceKey key = world.key();
        try {
            final List<String> regionNames = config().node("worlds", key.asString(), "regions")
                    .getList(String.class, new ArrayList<>());

            final RegionisedWorldSet set = new RegionisedWorldSet(key, key.asString());

            List<Region> regions = new ArrayList<>();
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
            worldSets.put(key, set);
            logger.info(
                    "Loaded world set {} ({}) with {} regions",
                    world.uniqueId(),
                    key.asMinimalString(),
                    regions.size());
        } catch (SerializationException e) {
            logger().error(
                            "Error loading world set {} ({})",
                            world.uniqueId(),
                            key.asMinimalString(),
                            e);
        }
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

    @Override
    public Key getPlayerWorld(UUID player) {
        return Sponge.server().player(player).orElseThrow().world().key();
    }

    @Override
    public boolean hasPermission(UUID player, String permission) {
        final ServerPlayer sponge = Sponge.server().player(player).orElse(null);
        if (sponge == null) return false;
        return sponge.hasPermission(permission);
    }

    @Override
    public Path dataFolder() {
        return dataFolder;
    }

    @Override
    public ConfigurationNode config() {
        return rootNode;
    }

    @Override
    public Logger logger() {
        return logger;
    }
}
