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
package org.empirewar.orbis.sponge.command;

import static org.empirewar.orbis.command.parser.RegionParser.regionParser;
import static org.incendo.cloud.sponge.parser.UserParser.userParser;

import net.kyori.adventure.text.Component;

import org.empirewar.orbis.command.CommonCommands;
import org.empirewar.orbis.command.Permissions;
import org.empirewar.orbis.member.Member;
import org.empirewar.orbis.member.PlayerMember;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.sponge.OrbisSponge;
import org.empirewar.orbis.sponge.session.SpongeConsoleSession;
import org.empirewar.orbis.sponge.session.SpongePlayerSession;
import org.empirewar.orbis.util.OrbisText;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.sponge.SpongeCommandManager;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryHolder;

public final class SpongeCommands {

    public SpongeCommands(OrbisSponge plugin, RegistryHolder registryHolder) {
        SpongeCommandManager<OrbisSession> manager = new SpongeCommandManager<>(
                plugin.pluginContainer(),
                ExecutionCoordinator.simpleCoordinator(),
                registryHolder,
                SenderMapper.create(
                        cause -> {
                            if (cause.audience() instanceof ServerPlayer player) {
                                return new SpongePlayerSession(player, cause);
                            }
                            return new SpongeConsoleSession(cause);
                        },
                        session -> {
                            if (session instanceof SpongePlayerSession player) {
                                return player.getCause();
                            }
                            return ((SpongeConsoleSession) session).cause();
                        }));

        new CommonCommands(manager);

        manager.command(manager.commandBuilder("region", "rg")
                .permission(Permissions.MANAGE)
                .literal("member")
                .literal("player")
                .literal("add")
                .required("region", regionParser())
                .required("player", userParser())
                .handler(context -> {
                    final Region region = context.get("region");
                    final User player = context.get("player");
                    region.addMember(new PlayerMember(player.uniqueId()));
                    final OrbisSession sender = context.sender();
                    sender.sendMessage(OrbisText.PREFIX.append(Component.text(
                            "Added " + player.name() + " as a member to region " + region.name()
                                    + ".",
                            OrbisText.EREBOR_GREEN)));
                }));

        manager.command(manager.commandBuilder("region", "rg")
                .permission(Permissions.MANAGE)
                .literal("member")
                .literal("player")
                .literal("remove")
                .required("region", regionParser())
                .required("player", userParser())
                .handler(context -> {
                    final Region region = context.get("region");
                    final User player = context.get("player");
                    final OrbisSession sender = context.sender();
                    for (Member member : region.members()) {
                        if (member instanceof PlayerMember playerMember
                                && playerMember.playerId().equals(player.uniqueId())) {
                            region.removeMember(member);
                            sender.sendMessage(OrbisText.PREFIX.append(Component.text(
                                    "Removed member '" + player.name() + "'.",
                                    OrbisText.EREBOR_GREEN)));
                            return;
                        }
                    }
                    sender.sendMessage(OrbisText.PREFIX.append(Component.text(
                            "Couldn't find a member with that name.", OrbisText.SECONDARY_RED)));
                }));
    }
}
