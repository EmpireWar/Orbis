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
package org.empirewar.orbis.neoforge;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.command.Permissions;
import org.empirewar.orbis.modded.command.ModdedCommands;
import org.empirewar.orbis.neoforge.listener.BlockActionListener;
import org.empirewar.orbis.neoforge.listener.ConnectionListener;
import org.empirewar.orbis.neoforge.listener.InteractEntityListener;
import org.empirewar.orbis.neoforge.selection.SelectionListener;
import org.empirewar.orbis.neoforge.session.NeoForgeConsoleSession;
import org.empirewar.orbis.neoforge.session.NeoForgePlayerSession;
import org.empirewar.orbis.selection.Selection;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.neoforge.NeoForgeServerCommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

@Mod("orbis")
public class OrbisNeoForge extends OrbisPlatform {
    public static final Logger LOGGER = LoggerFactory.getLogger("orbis");

    private volatile MinecraftServerAudiences adventure;
    private volatile MinecraftServer server;

    private ItemStack wandItem;
    private final Path dataFolder;

    public OrbisNeoForge() {
        // You can do simple construction logic here if needed.
        // Heavy logic should go into lifecycle event handlers.
        this.dataFolder = Path.of("config", "orbis");
        load();

        new ModdedCommands<>(new NeoForgeServerCommandManager<>(
                ExecutionCoordinator.simpleCoordinator(),
                SenderMapper.create(
                        sender -> {
                            if (sender.getPlayer() instanceof ServerPlayer player) {
                                return new NeoForgePlayerSession(player, sender);
                            }
                            return new NeoForgeConsoleSession(sender);
                        },
                        session -> {
                            if (session instanceof NeoForgePlayerSession playerSession) {
                                return playerSession.getCause();
                            }

                            return ((NeoForgeConsoleSession) session).cause();
                        })));

        NeoForge.EVENT_BUS.register(this);
    }

    public MinecraftServerAudiences adventure() {
        var ret = this.adventure;
        if (ret == null) {
            throw new IllegalStateException("Tried to access Adventure without a running server!");
        }
        return ret;
    }

    public ItemStack getWandItem() {
        return wandItem.copy();
    }

    public MinecraftServer server() {
        return server;
    }

    public static final PermissionNode<Boolean> ORBIS_MANAGE = new PermissionNode<>(
            ResourceLocation.fromNamespaceAndPath("orbis", Permissions.MANAGE),
            PermissionTypes.BOOLEAN,
            (player, playerUUID, context) -> true);

    @SubscribeEvent
    public void onPermissionNodesRegister(PermissionGatherEvent.Nodes event) {
        event.addNodes(ORBIS_MANAGE);
    }

    @SubscribeEvent
    public void onServerStarting(ServerAboutToStartEvent event) {
        this.adventure = MinecraftServerAudiences.of(event.getServer());
        this.server = event.getServer();

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
                new ItemLore(
                        Selection.WAND_LORE.stream().map(adventure::asNative).toList()));
        CustomData.update(DataComponents.CUSTOM_DATA, wandItem, compoundTag -> {
            final CompoundTag tag = new CompoundTag();
            tag.putBoolean("orbis_is_wand", true);
            compoundTag.merge(tag);
        });
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        try {
            saveRegions();
        } catch (IOException e) {
            logger().error("Error saving regions", e);
        }
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        this.adventure = null;
        this.server = null;
    }

    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            loadWorld(((Keyed) serverLevel.dimension()).key(), UUID.randomUUID());
        }
    }

    @SubscribeEvent
    public void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            saveWorld(((Keyed) serverLevel.dimension()).key(), UUID.randomUUID());
        }
    }

    private void registerListeners() {
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
        // TODO: How do we check permissions?
        return vanilla.hasPermissions(3);
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
