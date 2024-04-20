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

import static org.empirewar.orbis.command.parser.RegionParser.regionParser;
import static org.incendo.cloud.bukkit.parser.OfflinePlayerParser.offlinePlayerParser;

import io.leangen.geantyref.TypeToken;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.empirewar.orbis.command.CommonCommands;
import org.empirewar.orbis.command.parser.FlagValueParser;
import org.empirewar.orbis.command.parser.RegionFlagParser;
import org.empirewar.orbis.command.parser.RegionParser;
import org.empirewar.orbis.command.parser.RegionisedWorldParser;
import org.empirewar.orbis.member.Member;
import org.empirewar.orbis.member.PlayerMember;
import org.empirewar.orbis.migrations.worldguard.WorldGuardMigrator;
import org.empirewar.orbis.paper.OrbisPaper;
import org.empirewar.orbis.player.ConsoleOrbisSession;
import org.empirewar.orbis.region.Region;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.bukkit.internal.BukkitBrigadierMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;

public final class PaperCommands {

    public PaperCommands(OrbisPaper plugin) {
        PaperCommandManager<ConsoleOrbisSession> manager = new PaperCommandManager<>(
                plugin, /* 1 */
                ExecutionCoordinator.asyncCoordinator(), /* 2 */
                SenderMapper.create(ConsoleOrbisSession::new, session ->
                        (CommandSender) session.audience()) /* 3 */);

        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }

        CommonCommands commonCommands = new CommonCommands(plugin, manager);

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

        brigadierMapper.mapSimpleNMS(
                new TypeToken<FlagValueParser<ConsoleOrbisSession>>() {}, "message", true);

        brigadierMapper.mapSimpleNMS(
                new TypeToken<RegionParser<ConsoleOrbisSession>>() {}, "resource_location", true);

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

        manager.command(manager.commandBuilder("region", "rg")
                .literal("member")
                .literal("add")
                .required("region", regionParser())
                .required("player", offlinePlayerParser())
                .handler(context -> {
                    final Region region = context.get("region");
                    final OfflinePlayer player = context.get("player");
                    region.addMember(new PlayerMember(player.getUniqueId()));
                    final ConsoleOrbisSession sender = context.sender();
                    sender.audience()
                            .sendMessage(Component.text(
                                    "Added " + player.getName() + " as a member to region "
                                            + region.name() + ".",
                                    NamedTextColor.GREEN));
                }));

        manager.command(manager.commandBuilder("region", "rg")
                .literal("member")
                .literal("remove")
                .required("region", regionParser())
                .required("player", offlinePlayerParser())
                .handler(context -> {
                    final Region region = context.get("region");
                    final OfflinePlayer player = context.get("player");
                    final ConsoleOrbisSession sender = context.sender();
                    for (Member member : region.members()) {
                        if (member instanceof PlayerMember playerMember
                                && playerMember.playerId().equals(player.getUniqueId())) {
                            region.removeMember(member);
                            sender.audience()
                                    .sendMessage(Component.text(
                                            "Removed member '" + player.getName() + "'.",
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
