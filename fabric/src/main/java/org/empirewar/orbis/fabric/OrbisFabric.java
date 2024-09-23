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
package org.empirewar.orbis.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.selection.SelectionManager;
import org.empirewar.orbis.world.RegionisedWorld;
import org.empirewar.orbis.world.RegionisedWorldSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
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

public class OrbisFabric implements ModInitializer, Orbis {

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("orbis");

    private volatile MinecraftServerAudiences adventure;

    public MinecraftServerAudiences adventure() {
        MinecraftServerAudiences ret = this.adventure;
        if (ret == null) {
            throw new IllegalStateException("Tried to access Adventure without a running server!");
        }
        return ret;
    }

    private final SelectionManager selectionManager = new SelectionManager();
    private final RegionisedWorldSet globalSet = new RegionisedWorldSet();
    private final Map<Key, RegionisedWorldSet> worldSets = new HashMap<>();

    private ItemStack wandItem;
    private Path dataFolder;

    private volatile MinecraftServer server;

    public MinecraftServer server() {
        return server;
    }

    @Override
    public void onInitialize() {
        OrbisAPI.set(this);

        this.dataFolder = FabricLoader.getInstance().getConfigDir().resolve("orbis");

        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world!");

        // Register with the server lifecycle callbacks
        // This will ensure any platform data is cleared between game instances
        // This is important on the integrated server, where multiple server instances
        // can exist for one mod initialization.
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.adventure = MinecraftServerAudiences.of(server);
            this.server = server;

            this.loadConfig();
            this.registerListeners();
            try {
                this.loadRegions();
            } catch (IOException e) {
                logger().error("Error loading regions", e);
            }

            this.wandItem = new ItemStack(Items.BLAZE_ROD);
            wandItem.set(DataComponents.ITEM_NAME, adventure.asNative(Selection.WAND_NAME));
            wandItem.set(
                    DataComponents.LORE,
                    new ItemLore(Selection.WAND_LORE.stream()
                            .map(adventure::asNative)
                            .toList()));
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            server.getAllLevels().forEach(this::loadWorld);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            try {
                saveRegions();
            } catch (IOException e) {
                logger().error("Error saving regions", e);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            this.adventure = null;
            this.server = null;
        });
    }

    public Path getDataFolder() {
        return dataFolder;
    }

    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigurationNode rootNode;

    private void loadConfig() {
        try {
            final Path configPath = dataFolder().resolve("config.yml");
            try {
                final Path jarPath = FabricLoader.getInstance()
                        .getModContainer("orbis")
                        .orElseThrow()
                        .findPath("config.yml")
                        .orElseThrow();
                Files.copy(jarPath, configPath);
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
        ServerWorldEvents.UNLOAD.register((s, world) -> {
            final RegionisedWorldSet set =
                    this.worldSets.remove(world.dimension().key());
            try {
                this.saveWorld(set);
            } catch (IOException e) {
                logger().error(
                                "Error saving world set {}",
                                world.dimension().key().asMinimalString(),
                                e);
            }
        });

        ServerWorldEvents.LOAD.register((s, world) -> this.loadWorld(world));
    }

    private void loadWorld(ServerLevel world) {
        final Key key = world.dimension().key();
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
            LOGGER.info(
                    "Loaded world set {} with {} regions", key.asMinimalString(), regions.size());
        } catch (SerializationException e) {
            logger().error("Error loading world set {}", key.asMinimalString(), e);
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
        return server.getPlayerList()
                .getPlayer(player)
                .serverLevel()
                .dimension()
                .key();
    }

    @Override
    public boolean hasPermission(UUID player, String permission) {
        final ServerPlayer vanilla = server.getPlayerList().getPlayer(player);
        if (vanilla == null) return false;
        return Permissions.check(vanilla, permission);
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
        return LOGGER;
    }
}
