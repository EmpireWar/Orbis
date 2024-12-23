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
package org.empirewar.orbis.bukkit.command;

import static net.kyori.adventure.text.Component.text;

import static org.empirewar.orbis.command.parser.RegionParser.regionParser;
import static org.incendo.cloud.bukkit.parser.OfflinePlayerParser.offlinePlayerParser;

import io.leangen.geantyref.TypeToken;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.empirewar.orbis.bukkit.OrbisBukkit;
import org.empirewar.orbis.bukkit.session.PlayerSession;
import org.empirewar.orbis.command.CommonCommands;
import org.empirewar.orbis.command.Permissions;
import org.empirewar.orbis.command.parser.AreaTypeParser;
import org.empirewar.orbis.command.parser.FlagValueParser;
import org.empirewar.orbis.command.parser.RegionFlagParser;
import org.empirewar.orbis.command.parser.RegionParser;
import org.empirewar.orbis.command.parser.RegionisedWorldParser;
import org.empirewar.orbis.member.Member;
import org.empirewar.orbis.member.PlayerMember;
import org.empirewar.orbis.migrations.worldguard.WorldGuardMigrator;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.util.OrbisText;
import org.empirewar.orbis.world.RegionisedWorld;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.bukkit.internal.BukkitBrigadierMapper;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

public class BukkitCommands {

    public BukkitCommands(OrbisBukkit plugin, LegacyPaperCommandManager<OrbisSession> manager) {
        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }

        CommonCommands commonCommands = new CommonCommands(plugin, manager);

        this.mapDumbBrigadierStuff(manager);

        manager.command(manager.commandBuilder("orbis")
                .permission(Permissions.MANAGE)
                .literal("migrate")
                .literal("worldguard")
                .handler(context -> {
                    final OrbisSession sender = context.sender();
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

        manager.command(manager.commandBuilder("orbis")
                .senderType(PlayerSession.class)
                .permission(Permissions.MANAGE)
                .literal("where")
                .handler(context -> {
                    final PlayerSession sender = context.sender();
                    final Audience audience = sender.audience();
                    final Player player = sender.getPlayer();
                    final Key playerWorld = plugin.adventureKey(player.getWorld());
                    final RegionisedWorld world = plugin.getRegionisedWorld(playerWorld);
                    audience.sendMessage(OrbisText.PREFIX.append(text(
                            "You are in world " + world.worldName().orElseThrow() + ".",
                            OrbisText.SECONDARY_ORANGE)));
                    final Location loc = player.getLocation();
                    for (Region region : world.query(RegionQuery.Position.builder()
                                    .position(loc.getX(), loc.getY(), loc.getZ())
                                    .build())
                            .result()) {
                        audience.sendMessage(OrbisText.PREFIX.append(text(
                                "You are in region " + region.name() + ".",
                                OrbisText.EREBOR_GREEN)));
                    }
                }));

        manager.command(manager.commandBuilder("orbis")
                .senderType(PlayerSession.class)
                .permission(Permissions.MANAGE)
                .literal("wand")
                .handler(context -> {
                    final PlayerSession sender = context.sender();
                    final Player player = sender.getPlayer();
                    player.getInventory().addItem(plugin.wandItem());
                }));

        manager.command(manager.commandBuilder("region", "rg")
                .permission(Permissions.MANAGE)
                .literal("member")
                .literal("player")
                .literal("add")
                .required("region", regionParser())
                .required("player", offlinePlayerParser())
                .handler(context -> {
                    final Region region = context.get("region");
                    final OfflinePlayer player = context.get("player");
                    region.addMember(new PlayerMember(player.getUniqueId()));
                    final OrbisSession sender = context.sender();
                    sender.audience()
                            .sendMessage(OrbisText.PREFIX.append(Component.text(
                                    "Added " + player.getName() + " as a member to region "
                                            + region.name() + ".",
                                    OrbisText.EREBOR_GREEN)));
                }));

        manager.command(manager.commandBuilder("region", "rg")
                .permission(Permissions.MANAGE)
                .literal("member")
                .literal("player")
                .literal("remove")
                .required("region", regionParser())
                .required("player", offlinePlayerParser())
                .handler(context -> {
                    final Region region = context.get("region");
                    final OfflinePlayer player = context.get("player");
                    final OrbisSession sender = context.sender();
                    for (Member member : region.members()) {
                        if (member instanceof PlayerMember playerMember
                                && playerMember.playerId().equals(player.getUniqueId())) {
                            region.removeMember(member);
                            sender.audience()
                                    .sendMessage(OrbisText.PREFIX.append(Component.text(
                                            "Removed member '" + player.getName() + "'.",
                                            OrbisText.EREBOR_GREEN)));
                            return;
                        }
                    }
                    sender.audience()
                            .sendMessage(OrbisText.PREFIX.append(Component.text(
                                    "Couldn't find a member with that name.",
                                    OrbisText.SECONDARY_RED)));
                }));
    }

    // How sad that Cloud changed from being clientside, tons of issues stem from this.
    private void mapDumbBrigadierStuff(LegacyPaperCommandManager<OrbisSession> manager) {
        // Not on Spigot.
        if (!manager.hasBrigadierManager()) return;

        final BukkitBrigadierMapper<OrbisSession> brigadierMapper = new BukkitBrigadierMapper<>(
                manager.owningPlugin().getLogger(), manager.brigadierManager());
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
                new TypeToken<AreaTypeParser<OrbisSession>>() {}, "resource_location", true);
    }
}
