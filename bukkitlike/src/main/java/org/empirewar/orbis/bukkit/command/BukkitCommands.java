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
package org.empirewar.orbis.bukkit.command;

import io.leangen.geantyref.TypeToken;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.empirewar.orbis.command.CommonCommands;
import org.empirewar.orbis.command.Permissions;
import org.empirewar.orbis.command.parser.FlagValueParser;
import org.empirewar.orbis.command.parser.RegionFlagParser;
import org.empirewar.orbis.command.parser.RegionisedWorldParser;
import org.empirewar.orbis.command.parser.registry.RegistryValueParser;
import org.empirewar.orbis.migrations.worldguard.WorldGuardMigrator;
import org.empirewar.orbis.player.OrbisSession;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.bukkit.PluginHolder;
import org.incendo.cloud.bukkit.internal.BukkitBrigadierMapper;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.paper.PaperCommandManager;

import java.util.logging.Logger;

public class BukkitCommands<
        M extends
                CommandManager<OrbisSession> & BrigadierManagerHolder<OrbisSession, ?>
                        & PluginHolder> {

    private final M manager;

    public BukkitCommands(M manager) {
        this.manager = manager;

        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            if (manager instanceof BukkitCommandManager<?> bukkit) {
                bukkit.registerBrigadier();
            }
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            if (manager instanceof LegacyPaperCommandManager<?> legacy) {
                legacy.registerAsynchronousCompletions();
            }
        }

        this.mapDumbBrigadierStuff();
        this.registerCommands();
    }

    private void registerCommands() {
        new CommonCommands(manager);

        manager.command(manager.commandBuilder("orbis")
                .permission(Permissions.MANAGE)
                .literal("migrate")
                .literal("worldguard")
                .handler(context -> {
                    final OrbisSession sender = context.sender();
                    final Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
                    if (worldGuard == null || !worldGuard.isEnabled()) {
                        sender.sendMessage(Component.text(
                                "WorldGuard is not installed. WorldGuard must be installed for migration to work.",
                                NamedTextColor.RED));
                        return;
                    }
                    new WorldGuardMigrator(sender);
                }));
    }

    // How sad that Cloud changed from being clientside, tons of issues stem from this.
    private void mapDumbBrigadierStuff() {
        if (!manager.hasBrigadierManager()) return;

        final Logger logger;
        if (manager instanceof PaperCommandManager<?>) {
            logger = Logger.getLogger("Orbis");
        } else {
            logger = manager.owningPlugin().getLogger();
        }

        final BukkitBrigadierMapper<OrbisSession> brigadierMapper =
                new BukkitBrigadierMapper<>(logger, manager.brigadierManager());
        brigadierMapper.mapSimpleNMS(
                new TypeToken<RegionFlagParser<OrbisSession>>() {}, "resource_location", true);

        brigadierMapper.mapSimpleNMS(
                new TypeToken<RegionisedWorldParser<OrbisSession>>() {}, "resource_location", true);

        brigadierMapper.mapSimpleNMS(
                new TypeToken<FlagValueParser<OrbisSession>>() {}, "message", true);

        brigadierMapper.mapSimpleNMS(
                new TypeToken<RegistryValueParser<OrbisSession, ?, ?>>() {},
                "resource_location",
                true);
    }
}
