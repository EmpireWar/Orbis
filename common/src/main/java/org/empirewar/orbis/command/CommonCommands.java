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
package org.empirewar.orbis.command;

import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.command.caption.OrbisCaptionProvider;
import org.empirewar.orbis.command.parser.FlagValueParser;
import org.empirewar.orbis.command.parser.RegionFlagParser;
import org.empirewar.orbis.command.parser.RegionParser;
import org.empirewar.orbis.command.parser.RegionisedWorldParser;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.flag.value.FlagValue;
import org.empirewar.orbis.player.ConsoleOrbisSession;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;

public final class CommonCommands {

    public CommonCommands(Orbis orbis, CommandManager<ConsoleOrbisSession> manager) {
        // Register our custom caption registry so we can define exception messages for parsers
        manager.captionRegistry().registerProvider(new OrbisCaptionProvider<>());

        manager.parserRegistry()
                .registerParserSupplier(
                        TypeToken.get(Region.class), parserParameters -> new RegionParser<>());

        manager.parserRegistry()
                .registerParserSupplier(
                        TypeToken.get(RegionisedWorld.class),
                        parserParameters -> new RegionisedWorldParser<>());

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
    }
}
