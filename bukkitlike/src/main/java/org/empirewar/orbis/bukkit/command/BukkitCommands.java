/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 Empire War
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
import org.empirewar.orbis.command.parser.RegionParser;
import org.empirewar.orbis.command.parser.RegionisedWorldParser;
import org.empirewar.orbis.command.parser.RegistryValueParser;
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
        // Not on Spigot.
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
                new TypeToken<RegionParser<OrbisSession>>() {}, "resource_location", true);

        brigadierMapper.mapSimpleNMS(
                new TypeToken<FlagValueParser<OrbisSession>>() {}, "message", true);

        brigadierMapper.mapSimpleNMS(
                new TypeToken<RegionParser<OrbisSession>>() {}, "resource_location", true);

        brigadierMapper.mapSimpleNMS(
                new TypeToken<RegistryValueParser<OrbisSession, ?>>() {},
                "resource_location",
                true);
    }
}
