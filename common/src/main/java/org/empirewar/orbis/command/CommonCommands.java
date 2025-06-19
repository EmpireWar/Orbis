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

import static net.kyori.adventure.text.Component.text;

import com.github.benmanes.caffeine.cache.Caffeine;

import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;

import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.command.caption.OrbisCaptionProvider;
import org.empirewar.orbis.command.parser.AreaTypeParser;
import org.empirewar.orbis.command.parser.FlagValueParser;
import org.empirewar.orbis.command.parser.RegionFlagParser;
import org.empirewar.orbis.command.parser.RegionParser;
import org.empirewar.orbis.command.parser.RegionisedWorldParser;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.flag.value.FlagValue;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.util.OrbisText;
import org.empirewar.orbis.world.RegionisedWorld;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.processors.cache.CaffeineCache;
import org.incendo.cloud.processors.confirmation.ConfirmationConfiguration;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;

import java.time.Duration;

public final class CommonCommands {

    public CommonCommands(CommandManager<OrbisSession> manager) {
        /*
         * Create the confirmation manager. This allows us to require certain commands to be
         * confirmed before they can be executed
         */
        ConfirmationConfiguration<OrbisSession> confirmationConfig =
                ConfirmationConfiguration.<OrbisSession>builder()
                        .cache(CaffeineCache.of(Caffeine.newBuilder()
                                .expireAfterWrite(Duration.ofSeconds(30))
                                .build()))
                        .noPendingCommandNotifier(sender -> {
                            sender.audience()
                                    .sendMessage(OrbisText.PREFIX.append(text(
                                            "You don't have any pending confirmations.",
                                            OrbisText.SECONDARY_RED)));
                        })
                        .confirmationRequiredNotifier((sender, context) -> {
                            sender.audience()
                                    .sendMessage(OrbisText.PREFIX.append(text(
                                            "Confirmation required. Confirm using /confirm.",
                                            OrbisText.SECONDARY_ORANGE)));
                        })
                        .build();

        final ConfirmationManager<OrbisSession> confirmationManager =
                ConfirmationManager.confirmationManager(confirmationConfig);

        // Register the confirmation command.
        manager.command(manager.commandBuilder("orbis")
                .literal("confirm")
                .handler(confirmationManager.createExecutionHandler()));

        // Register the confirmation processor. This will enable confirmations for commands that
        // require it
        manager.registerCommandPostProcessor(confirmationManager.createPostprocessor());

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

        final TypeToken<?> areaTypeToken = TypeToken.get(
                TypeFactory.parameterizedClass(AreaType.class, TypeFactory.unboundWildcard()));
        manager.parserRegistry()
                .registerParserSupplier(areaTypeToken, parserParameters -> new AreaTypeParser<>());

        AnnotationParser<OrbisSession> annotationParser =
                new AnnotationParser<>(manager, OrbisSession.class);

        // Override the default exception handlers
        MinecraftExceptionHandler.create(OrbisSession::audience)
                .defaultInvalidSyntaxHandler()
                .defaultInvalidSenderHandler()
                .defaultNoPermissionHandler()
                .defaultArgumentParsingHandler()
                .defaultCommandExecutionHandler()
                .registerTo(manager);

        //        annotationParser.parse(new HelpCommands(orbis, manager));
        annotationParser.parse(new RegionCommand());
        annotationParser.parse(new SelectionCommand());
    }
}
