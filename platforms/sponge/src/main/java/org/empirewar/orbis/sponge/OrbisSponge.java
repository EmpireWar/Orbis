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
package org.empirewar.orbis.sponge;

import com.google.inject.Inject;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.minecraft.OrbisMinecraftPlatform;
import org.empirewar.orbis.sponge.command.SpongeCommands;
import org.empirewar.orbis.sponge.key.SpongeDataKeys;
import org.empirewar.orbis.sponge.listener.*;
import org.empirewar.orbis.sponge.selection.SelectionListener;
import org.empirewar.orbis.sponge.task.SpongeRegionVisualiserTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.UUID;

@Plugin("orbis")
public class OrbisSponge extends OrbisMinecraftPlatform {

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
        load();
    }

    @Listener
    public void onRegisterCommands(RegisterCommandEvent<Command.Parameterized> event) {
        new SpongeCommands(this, event.registryHolder());
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
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
        Sponge.server().worldManager().worlds().forEach(w -> this.loadWorld(w.key(), w.uniqueId()));
        Sponge.server()
                .scheduler()
                .submit(Task.builder()
                        .execute(new SpongeRegionVisualiserTask(this))
                        .interval(Ticks.of(20))
                        .plugin(pluginContainer)
                        .build());
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
        final ServerWorld world = event.world();
        this.loadWorld(world.key(), world.uniqueId());
    }

    @Listener(order = Order.LATE)
    public void onWorldUnload(UnloadWorldEvent event) {
        final ServerWorld world = event.world();
        this.saveWorld(world.key(), world.uniqueId());
    }

    private void registerListeners() {
        final EventManager eventManager = Sponge.eventManager();
        eventManager.registerListeners(
                pluginContainer, new BlockActionListener(this), MethodHandles.lookup());
        eventManager.registerListeners(
                pluginContainer, new InteractEntityListener(this), MethodHandles.lookup());
        eventManager.registerListeners(
                pluginContainer, new MovementListener(this), MethodHandles.lookup());
        eventManager.registerListeners(
                pluginContainer, new ConnectionListener(this), MethodHandles.lookup());
        eventManager.registerListeners(
                pluginContainer, new SelectionListener(this), MethodHandles.lookup());
        eventManager.registerListeners(
                pluginContainer, new DamageEntityListener(this), MethodHandles.lookup());
        eventManager.registerListeners(
                pluginContainer, new RegionMessagesListener(this), MethodHandles.lookup());
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
    protected InputStream getResourceAsStream(String path) {
        return pluginContainer.openResource(path).orElseThrow();
    }

    @Override
    public Logger logger() {
        return logger;
    }
}
