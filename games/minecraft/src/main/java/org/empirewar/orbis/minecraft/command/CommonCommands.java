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
package org.empirewar.orbis.minecraft.command;

import static net.kyori.adventure.text.Component.text;

import com.github.benmanes.caffeine.cache.Caffeine;

import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.flag.value.FlagValue;
import org.empirewar.orbis.member.MemberType;
import org.empirewar.orbis.minecraft.command.caption.OrbisCaptionProvider;
import org.empirewar.orbis.minecraft.command.parser.FlagValueParser;
import org.empirewar.orbis.minecraft.command.parser.RegionFlagParser;
import org.empirewar.orbis.minecraft.command.parser.RegionisedWorldParser;
import org.empirewar.orbis.command.parser.registry.RegistryMapper;
import org.empirewar.orbis.minecraft.command.parser.registry.RegistryValueParser;
import org.empirewar.orbis.minecraft.player.OrbisSession;
import org.empirewar.orbis.minecraft.player.PlayerOrbisSession;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.selection.Selection;
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
                            sender.sendMessage(OrbisText.PREFIX.append(text(
                                    "You don't have any pending confirmations.",
                                    OrbisText.SECONDARY_RED)));
                        })
                        .confirmationRequiredNotifier((sender, context) -> {
                            sender.sendMessage(OrbisText.PREFIX.append(text(
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

        // Wand command
        manager.command(manager.commandBuilder("orbis")
                .senderType(PlayerOrbisSession.class)
                .permission(org.empirewar.orbis.util.Permissions.MANAGE)
                .literal("wand")
                .handler(context -> {
                    final PlayerOrbisSession sender = context.sender();
                    sender.giveWandItem();
                    Selection.WAND_LORE.forEach(sender::sendMessage);
                }));

        // Where command
        manager.command(manager.commandBuilder("orbis")
                .permission(org.empirewar.orbis.util.Permissions.MANAGE)
                .senderType(PlayerOrbisSession.class)
                .literal("where")
                .handler(context -> {
                    final Orbis orbis = OrbisAPI.get();
                    final PlayerOrbisSession sender = context.sender();
                    final Key playerWorld = orbis.getPlayerWorld(sender.getUuid());
                    final RegionisedWorld world = orbis.getRegionisedWorld(playerWorld);
                    sender.sendMessage(OrbisText.PREFIX.append(text(
                            "You are in world " + world.worldId().orElseThrow().asString() + ".",
                            OrbisText.SECONDARY_ORANGE)));
                    for (Region region : world.query(RegionQuery.Position.at(sender.getPosition())
                                    .build())
                            .result()) {
                        sender.sendMessage(OrbisText.PREFIX.append(text(
                                "You are in region " + region.name() + ".",
                                OrbisText.EREBOR_GREEN)));
                    }
                }));

        // Register the confirmation processor. This will enable confirmations for commands that
        // require it
        manager.registerCommandPostProcessor(confirmationManager.createPostprocessor());

        // Register our custom caption registry so we can define exception messages for parsers
        manager.captionRegistry().registerProvider(new OrbisCaptionProvider<>());

        manager.parserRegistry()
                .registerParserSupplier(
                        TypeToken.get(RegionisedWorld.class),
                        parserParameters -> new RegionisedWorldParser<>());

        final TypeToken<?> typeToken = TypeToken.get(TypeFactory.parameterizedClass(
                RegistryRegionFlag.class, TypeFactory.unboundWildcard()));
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
                .registerParserSupplier(
                        areaTypeToken,
                        parserParameters -> new RegistryValueParser<>(
                                OrbisRegistries.AREA_TYPE, RegistryMapper.KEY));

        final TypeToken<?> memberTypeToken = TypeToken.get(
                TypeFactory.parameterizedClass(MemberType.class, TypeFactory.unboundWildcard()));
        manager.parserRegistry()
                .registerParserSupplier(
                        memberTypeToken,
                        parserParameters -> new RegistryValueParser<>(
                                OrbisRegistries.MEMBER_TYPE, RegistryMapper.KEY));

        manager.parserRegistry()
                .registerParserSupplier(
                        TypeToken.get(Region.class),
                        parserParameters -> new RegistryValueParser<>(
                                OrbisRegistries.REGIONS, RegistryMapper.IDENTITY));

        AnnotationParser<OrbisSession> annotationParser =
                new AnnotationParser<>(manager, OrbisSession.class);

        // Override the default exception handlers
        MinecraftExceptionHandler.create(OrbisSession::getAudience)
                .defaultInvalidSyntaxHandler()
                .defaultInvalidSenderHandler()
                .defaultNoPermissionHandler()
                .defaultArgumentParsingHandler()
                .defaultCommandExecutionHandler()
                .registerTo(manager);

        annotationParser.parse(new HelpCommands(manager));
        annotationParser.parse(new RegionCommand());
        annotationParser.parse(new SelectionCommand());
    }
}
