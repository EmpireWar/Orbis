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
package org.empirewar.orbis.sponge.command;

import static org.empirewar.orbis.command.parser.RegionParser.regionParser;
import static org.incendo.cloud.sponge.parser.UserParser.userParser;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.empirewar.orbis.command.CommonCommands;
import org.empirewar.orbis.member.Member;
import org.empirewar.orbis.member.PlayerMember;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.sponge.OrbisSponge;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.setting.ManagerSetting;
import org.incendo.cloud.sponge.SpongeCommandManager;
import org.spongepowered.api.entity.living.player.User;

public final class SpongeCommands {

    public SpongeCommands(OrbisSponge plugin) {
        SpongeCommandManager<OrbisSession> manager = new SpongeCommandManager<>(
                plugin.pluginContainer(),
                ExecutionCoordinator.asyncCoordinator(),
                SenderMapper.create(
                        ConsoleOrbisSessionExtension::new,
                        s -> ((ConsoleOrbisSessionExtension) s).cause()));

        manager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);
        manager.settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true);

        CommonCommands commonCommands = new CommonCommands(plugin, manager);

        manager.command(manager.commandBuilder("region", "rg")
                .literal("member")
                .literal("add")
                .required("region", regionParser())
                .required("player", userParser())
                .handler(context -> {
                    final Region region = context.get("region");
                    final User player = context.get("player");
                    region.addMember(new PlayerMember(player.uniqueId()));
                    final OrbisSession sender = context.sender();
                    sender.audience()
                            .sendMessage(Component.text(
                                    "Added " + player.name() + " as a member to region "
                                            + region.name() + ".",
                                    NamedTextColor.GREEN));
                }));

        manager.command(manager.commandBuilder("region", "rg")
                .literal("member")
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
                            sender.audience()
                                    .sendMessage(Component.text(
                                            "Removed member '" + player.name() + "'.",
                                            NamedTextColor.GREEN));
                            return;
                        }
                    }
                    sender.audience()
                            .sendMessage(Component.text(
                                    "Couldn't find a member with that name.", NamedTextColor.RED));
                }));
    }
}
