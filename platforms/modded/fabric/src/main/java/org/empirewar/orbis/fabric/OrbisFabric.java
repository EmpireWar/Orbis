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
package org.empirewar.orbis.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.fabric.command.FabricCommands;
import org.empirewar.orbis.fabric.listener.BlockActionListener;
import org.empirewar.orbis.fabric.listener.ConnectionListener;
import org.empirewar.orbis.fabric.listener.InteractEntityListener;
import org.empirewar.orbis.fabric.selection.SelectionListener;
import org.empirewar.orbis.selection.Selection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

public class OrbisFabric extends OrbisPlatform implements ModInitializer {

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

    private ItemStack wandItem;
    private Path dataFolder;

    public ItemStack getWandItem() {
        return wandItem.copy();
    }

    private volatile MinecraftServer server;

    public MinecraftServer server() {
        return server;
    }

    @Override
    public void onInitialize() {
        this.dataFolder = FabricLoader.getInstance().getConfigDir().resolve("orbis");

        load();

        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Registering commands in the event doesn't seem to work with Cloud
        //        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess,
        // environment) -> {
        new FabricCommands(this);
        //        });

        //        OrbisComponents.initialise();

        // Register with the server lifecycle callbacks
        // This will ensure any platform data is cleared between game instances
        // This is important on the integrated server, where multiple server instances
        // can exist for one mod initialization.
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.adventure = MinecraftServerAudiences.of(server);
            this.server = server;

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
            CustomData.update(DataComponents.CUSTOM_DATA, wandItem, compoundTag -> {
                final CompoundTag tag = new CompoundTag();
                tag.putBoolean("orbis_is_wand", true);
                compoundTag.merge(tag);
            });
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

    private void registerListeners() {
        ServerWorldEvents.UNLOAD.register(
                (s, world) -> this.saveWorld(((Keyed) world.dimension()).key(), UUID.randomUUID()));
        ServerWorldEvents.LOAD.register(
                (s, world) -> this.loadWorld(((Keyed) world.dimension()).key(), UUID.randomUUID()));
        new SelectionListener(this);
        new ConnectionListener(this);
        new InteractEntityListener(this);
        new BlockActionListener(this);
    }

    @Override
    public Key getPlayerWorld(UUID player) {
        return ((Keyed) server.getPlayerList().getPlayer(player).serverLevel().dimension()).key();
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
    protected InputStream getResourceAsStream(String path) {
        return getClass().getResourceAsStream(path);
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }
}
