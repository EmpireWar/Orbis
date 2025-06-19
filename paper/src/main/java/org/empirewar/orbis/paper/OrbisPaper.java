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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.bukkit.OrbisBukkit;
import org.empirewar.orbis.bukkit.listener.*;
import org.empirewar.orbis.bukkit.selection.SelectionListener;
import org.empirewar.orbis.paper.listener.InteractEntityExtensionListener;
import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.selection.SelectionManager;
import org.empirewar.orbis.world.RegionisedWorld;
import org.empirewar.orbis.world.RegionisedWorldSet;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrbisPaper extends JavaPlugin implements OrbisBukkit, Listener {

    public static final ItemStack WAND_ITEM;

    static {
        WAND_ITEM = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = WAND_ITEM.getItemMeta();
        meta.displayName(Selection.WAND_NAME);
        meta.lore(Selection.WAND_LORE);
        meta.getPersistentDataContainer().set(WAND_KEY, PersistentDataType.BOOLEAN, true);
        WAND_ITEM.setItemMeta(meta);
    }

    @Override
    public ItemStack wandItem() {
        return WAND_ITEM;
    }

    @Override
    public Audience senderAsAudience(CommandSender player) {
        return player;
    }

    @Override
    public Key adventureKey(Keyed keyed) {
        return keyed.key();
    }

    private final SelectionManager selectionManager = new SelectionManager();
    private final RegionisedWorldSet globalSet = new RegionisedWorldSet();
    private final Map<Key, RegionisedWorldSet> worldSets = new HashMap<>();

    @Override
    public void onLoad() {
        OrbisAPI.set(this);
    }

    @Override
    public void onEnable() {
        this.registerListeners();
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
        final World world = event.getWorld();
        final RegionisedWorldSet set = this.worldSets.remove(world.key());
        try {
            this.saveWorld(set);
        } catch (IOException e) {
            logger().error(
                            "Error saving world set {} ({})",
                            world.getUID(),
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
            saveResource("assets/orbis/config.yml", false);

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
        pluginManager.registerEvents(new InteractEntityExtensionListener(this), this);
        pluginManager.registerEvents(new MovementListener(this), this);
        pluginManager.registerEvents(new RegionEnterLeaveListener(this), this);
        pluginManager.registerEvents(new ConnectionListener(this), this);
        pluginManager.registerEvents(new SelectionListener(this), this);
        pluginManager.registerEvents(new EntityDamageListener(this), this);
    }

    private void loadWorld(World world) {
        final Key key = world.key();
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
            logger().info(
                            "Loaded world set {} ({}) with {} regions",
                            world.getUID(),
                            key.asMinimalString(),
                            regions.size());
        } catch (SerializationException e) {
            logger().error(
                            "Error loading world set {} ({})",
                            world.getUID(),
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
        return Bukkit.getPlayer(player).getWorld().key();
    }

    @Override
    public boolean hasPermission(UUID player, String permission) {
        final Player bukkit = Bukkit.getPlayer(player);
        if (bukkit == null) return false;
        return bukkit.hasPermission(permission);
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
