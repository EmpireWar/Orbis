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
package org.empirewar.orbis.fabric.command;

import static net.kyori.adventure.text.Component.text;

import static org.empirewar.orbis.command.parser.RegionParser.regionParser;
import static org.incendo.cloud.minecraft.modded.parser.VanillaArgumentParsers.singlePlayerSelectorParser;

import io.leangen.geantyref.TypeToken;

import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.server.level.ServerPlayer;

import org.empirewar.orbis.command.CommonCommands;
import org.empirewar.orbis.command.Permissions;
import org.empirewar.orbis.command.parser.AreaTypeParser;
import org.empirewar.orbis.command.parser.FlagValueParser;
import org.empirewar.orbis.command.parser.RegionFlagParser;
import org.empirewar.orbis.command.parser.RegionParser;
import org.empirewar.orbis.command.parser.RegionisedWorldParser;
import org.empirewar.orbis.fabric.OrbisFabric;
import org.empirewar.orbis.fabric.session.FabricConsoleSession;
import org.empirewar.orbis.fabric.session.FabricPlayerSession;
import org.empirewar.orbis.member.Member;
import org.empirewar.orbis.member.PlayerMember;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.util.OrbisText;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;
import org.incendo.cloud.minecraft.extras.suggestion.ComponentTooltipSuggestion;
import org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector;
import org.incendo.cloud.setting.ManagerSetting;

public final class FabricCommands {

    private final FabricServerCommandManager<OrbisSession> manager;

    public FabricCommands(OrbisFabric mod) {
        this.manager = new FabricServerCommandManager<>(
                ExecutionCoordinator.simpleCoordinator(),
                SenderMapper.create(
                        sender -> {
                            if (sender.getPlayer() instanceof ServerPlayer player) {
                                return new FabricPlayerSession(player, sender);
                            }
                            return new FabricConsoleSession(sender);
                        },
                        session -> {
                            if (session instanceof FabricPlayerSession playerSession) {
                                return playerSession.getCause();
                            }

                            return ((FabricConsoleSession) session).cause();
                        }));

        manager.appendSuggestionMapper(suggestion -> {
            if (!(suggestion instanceof ComponentTooltipSuggestion componentTooltipSuggestion))
                return suggestion;

            return TooltipSuggestion.suggestion(
                    suggestion.suggestion(),
                    NonWrappingComponentSerializer.INSTANCE.serialize(
                            componentTooltipSuggestion.tooltip()));
        });

        manager.settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true);

        this.mapBrigadierArguments();
        this.registerCommands(mod);
    }

    private void mapBrigadierArguments() {
        manager.brigadierManager()
                .registerMapping(
                        new TypeToken<RegionFlagParser<OrbisSession>>() {}, configurer -> configurer
                                .to(parser -> ResourceLocationArgument.id())
                                .cloudSuggestions());

        manager.brigadierManager()
                .registerMapping(
                        new TypeToken<RegionisedWorldParser<OrbisSession>>() {},
                        configurer -> configurer
                                .to(parser -> ResourceLocationArgument.id())
                                .cloudSuggestions());

        manager.brigadierManager()
                .registerMapping(
                        new TypeToken<RegionParser<OrbisSession>>() {}, configurer -> configurer
                                .to(parser -> ResourceLocationArgument.id())
                                .cloudSuggestions());

        manager.brigadierManager()
                .registerMapping(
                        new TypeToken<FlagValueParser<OrbisSession>>() {}, configurer -> configurer
                                .to(parser -> MessageArgument.message())
                                .cloudSuggestions());

        manager.brigadierManager()
                .registerMapping(
                        new TypeToken<RegionParser<OrbisSession>>() {}, configurer -> configurer
                                .to(parser -> ResourceLocationArgument.id())
                                .cloudSuggestions());

        manager.brigadierManager()
                .registerMapping(
                        new TypeToken<AreaTypeParser<OrbisSession>>() {}, configurer -> configurer
                                .to(parser -> ResourceLocationArgument.id())
                                .cloudSuggestions());
    }

    private void registerCommands(OrbisFabric mod) {
        new CommonCommands(manager);

        manager.command(manager.commandBuilder("region", "rg")
                .permission(Permissions.MANAGE)
                .literal("member")
                .literal("add")
                .literal("player")
                .required("region", regionParser())
                .required("player", singlePlayerSelectorParser())
                .handler(context -> {
                    final Region region = context.get("region");
                    final SinglePlayerSelector playerSelector = context.get("player");
                    final ServerPlayer player = playerSelector.single();
                    region.addMember(new PlayerMember(player.getUUID()));
                    final OrbisSession sender = context.sender();
                    sender.sendMessage(OrbisText.PREFIX.append(Component.text("Added ")
                            .append((ComponentLike) player.getName())
                            .append(text(
                                    " as a member to region " + region.name() + ".",
                                    OrbisText.EREBOR_GREEN))));
                }));

        manager.command(manager.commandBuilder("region", "rg")
                .permission(Permissions.MANAGE)
                .literal("member")
                .literal("remove")
                .literal("player")
                .required("region", regionParser())
                .required("player", singlePlayerSelectorParser())
                .handler(context -> {
                    final Region region = context.get("region");
                    final SinglePlayerSelector playerSelector = context.get("player");
                    final ServerPlayer player = playerSelector.single();
                    final OrbisSession sender = context.sender();
                    for (Member member : region.members()) {
                        if (member instanceof PlayerMember playerMember
                                && playerMember.playerId().equals(player.getUUID())) {
                            region.removeMember(member);
                            sender.sendMessage(
                                    OrbisText.PREFIX.append(Component.text("Removed member '")
                                            .append((ComponentLike) player.getName())
                                            .append(text("'.", OrbisText.EREBOR_GREEN))));
                            return;
                        }
                    }
                    sender.sendMessage(OrbisText.PREFIX.append(Component.text(
                            "Couldn't find a member with that name.", OrbisText.SECONDARY_RED)));
                }));

        //        manager.command(manager.commandBuilder("orbis")
        //                .senderType(FabricPlayerOrbisSession.class)
        //                .literal("where")
        //                .handler(context -> {
        //                    final FabricPlayerOrbisSession sender = context.sender();
        //                    if (sender.audience() instanceof ServerPlayer player) {
        //                        final Key playerWorld = player.level().dimension().key();
        //                        final RegionisedWorld world = mod.getRegionisedWorld(playerWorld);
        //                        player.sendMessage(OrbisText.PREFIX.append(text(
        //                                "You are in world " + world.worldName().orElseThrow() +
        // ".",
        //                                OrbisText.SECONDARY_ORANGE)));
        //                        for (Region region : world.query(RegionQuery.Position.builder()
        //                                        .position(
        //                                                player.position().x(),
        //                                                player.position().y(),
        //                                                player.position().z())
        //                                        .build())
        //                                .result()) {
        //                            player.sendMessage(OrbisText.PREFIX.append(text(
        //                                    "You are in region " + region.name() + ".",
        //                                    OrbisText.EREBOR_GREEN)));
        //                        }
        //                    }
        //                }));

        //        manager.command(manager.commandBuilder("orbis")
        //                .senderType(PlayerSession.class)
        //                .literal("wand")
        //                .handler(context -> {
        //                    final ServerPlayer player = context.sender().getCause().getPlayer();
        //                    mod.server().executeBlocking(player.getInventory().add());
        //                }));
        //
        //        manager.command(manager.commandBuilder("region", "rg")
        //                .literal("member")
        //                .literal("player")
        //                .literal("add")
        //                .required("region", regionParser())
        //                .required("player", userParser())
        //                .handler(context -> {
        //                    final Region region = context.get("region");
        //                    final User player = context.get("player");
        //                    region.addMember(new PlayerMember(player.uniqueId()));
        //                    final OrbisSession sender = context.sender();
        //                    sender.audience()
        //                            .sendMessage(OrbisText.PREFIX.append(Component.text(
        //                                    "Added " + player.name() + " as a member to region "
        //                                            + region.name() + ".",
        //                                    OrbisText.EREBOR_GREEN)));
        //                }));
        //
        //        manager.command(manager.commandBuilder("region", "rg")
        //                .literal("member")
        //                .literal("player")
        //                .literal("remove")
        //                .required("region", regionParser())
        //                .required("player", userParser())
        //                .handler(context -> {
        //                    final Region region = context.get("region");
        //                    final User player = context.get("player");
        //                    final OrbisSession sender = context.sender();
        //                    for (Member member : region.members()) {
        //                        if (member instanceof PlayerMember playerMember
        //                                && playerMember.playerId().equals(player.uniqueId())) {
        //                            region.removeMember(member);
        //                            sender.audience()
        //                                    .sendMessage(OrbisText.PREFIX.append(Component.text(
        //                                            "Removed member '" + player.name() + "'.",
        //                                            OrbisText.EREBOR_GREEN)));
        //                            return;
        //                        }
        //                    }
        //                    sender.audience()
        //                            .sendMessage(OrbisText.PREFIX.append(Component.text(
        //                                    "Couldn't find a member with that name.",
        //                                    OrbisText.SECONDARY_RED)));
        //                }));
    }
}
