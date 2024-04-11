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
package org.empirewar.orbis.paper.command;

import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.empirewar.orbis.command.RegionCommand;
import org.empirewar.orbis.command.caption.OrbisCaptionProvider;
import org.empirewar.orbis.command.parser.FlagValueParser;
import org.empirewar.orbis.command.parser.RegionFlagParser;
import org.empirewar.orbis.command.parser.RegionParser;
import org.empirewar.orbis.command.parser.RegionisedWorldParser;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.flag.value.FlagValue;
import org.empirewar.orbis.migrations.worldguard.WorldGuardMigrator;
import org.empirewar.orbis.paper.OrbisPaper;
import org.empirewar.orbis.player.ConsoleOrbisSession;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.bukkit.internal.BukkitBrigadierMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.paper.PaperCommandManager;

public class Commands {

    public Commands(OrbisPaper plugin) {
        PaperCommandManager<ConsoleOrbisSession> manager = new PaperCommandManager<>(
                plugin, /* 1 */
                ExecutionCoordinator.asyncCoordinator(), /* 2 */
                SenderMapper.create(ConsoleOrbisSession::new, session ->
                        (CommandSender) session.audience()) /* 3 */);

        // Register our custom caption registry so we can define exception messages for parsers
        manager.captionRegistry().registerProvider(new OrbisCaptionProvider<>());

        manager.parserRegistry()
                .registerParserSupplier(
                        TypeToken.get(Region.class), parserParameters -> new RegionParser<>());

        manager.parserRegistry()
                .registerParserSupplier(
                        TypeToken.get(RegionisedWorld.class),
                        parserParameters -> new RegionisedWorldParser<>());

        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }

        final BukkitBrigadierMapper<ConsoleOrbisSession> brigadierMapper =
                new BukkitBrigadierMapper<>(manager, manager.brigadierManager());
        brigadierMapper.mapSimpleNMS(
                new TypeToken<RegionFlagParser<ConsoleOrbisSession>>() {},
                "resource_location",
                true);

        brigadierMapper.mapSimpleNMS(
                new TypeToken<RegionisedWorldParser<ConsoleOrbisSession>>() {},
                "resource_location",
                true);

        brigadierMapper.mapSimpleNMS(
                new TypeToken<RegionParser<ConsoleOrbisSession>>() {}, "resource_location", true);

        final TypeToken<?> typeToken = TypeToken.get(
                TypeFactory.parameterizedClass(RegionFlag.class, TypeFactory.unboundWildcard()));
        manager.parserRegistry()
                .registerParserSupplier(typeToken, parserParameters -> new RegionFlagParser<>());

        final TypeToken<?> flagValueToken = TypeToken.get(
                TypeFactory.parameterizedClass(FlagValue.class, TypeFactory.unboundWildcard()));
        manager.parserRegistry()
                .registerParserSupplier(
                        flagValueToken, parserParameters -> new FlagValueParser<>(manager));

        AnnotationParser<ConsoleOrbisSession> annotationParser =
                new AnnotationParser<>(manager, ConsoleOrbisSession.class);

        // Override the default exception handlers
        MinecraftExceptionHandler.<ConsoleOrbisSession>create(OrbisSession::audience)
                .defaultInvalidSyntaxHandler()
                .defaultInvalidSenderHandler()
                .defaultNoPermissionHandler()
                .defaultArgumentParsingHandler()
                .defaultCommandExecutionHandler()
                .registerTo(manager);

        annotationParser.parse(new RegionCommand());

        manager.command(manager.commandBuilder("orbis")
                .literal("migrate")
                .literal("worldguard")
                .handler(context -> {
                    final ConsoleOrbisSession sender = context.sender();
                    final Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
                    if (worldGuard == null || !worldGuard.isEnabled()) {
                        sender.audience()
                                .sendMessage(Component.text(
                                        "WorldGuard is not installed. WorldGuard must be installed for migration to work.",
                                        NamedTextColor.RED));
                        return;
                    }
                    new WorldGuardMigrator(sender.audience());
                }));
    }
}
