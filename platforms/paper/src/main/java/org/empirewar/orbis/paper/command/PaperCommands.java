/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2025 Empire War
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
package org.empirewar.orbis.paper.command;

import io.leangen.geantyref.TypeToken;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.empirewar.orbis.migrations.rpgregions.RPGRegionsMigrator;
import org.empirewar.orbis.migrations.worldguard.WorldGuardMigrator;
import org.empirewar.orbis.minecraft.command.CommonCommands;
import org.empirewar.orbis.minecraft.command.Permissions;
import org.empirewar.orbis.minecraft.command.parser.FlagValueParser;
import org.empirewar.orbis.minecraft.command.parser.RegionFlagParser;
import org.empirewar.orbis.minecraft.command.parser.RegionisedWorldParser;
import org.empirewar.orbis.minecraft.command.parser.registry.RegistryValueParser;
import org.empirewar.orbis.minecraft.player.OrbisSession;
import org.incendo.cloud.bukkit.internal.BukkitBrigadierMapper;
import org.incendo.cloud.paper.PaperCommandManager;

import java.util.logging.Logger;

public class PaperCommands {

    private final PaperCommandManager<OrbisSession> manager;

    public PaperCommands(PaperCommandManager<OrbisSession> manager) {
        this.manager = manager;

        this.mapBrigadierTypes();
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

        manager.command(manager.commandBuilder("orbis")
                .permission(Permissions.MANAGE)
                .literal("migrate")
                .literal("rpgregions")
                .handler(context -> {
                    final OrbisSession sender = context.sender();
                    final Plugin rpgregions = Bukkit.getPluginManager().getPlugin("RPGRegions");
                    if (rpgregions == null || !rpgregions.isEnabled()) {
                        sender.sendMessage(Component.text(
                                "RPGRegions is not installed. RPGRegions must be installed for migration to work.",
                                NamedTextColor.RED));
                        return;
                    }
                    new RPGRegionsMigrator(sender);
                }));
    }

    private void mapBrigadierTypes() {
        final BukkitBrigadierMapper<OrbisSession> brigadierMapper =
                new BukkitBrigadierMapper<>(Logger.getLogger("Orbis"), manager.brigadierManager());

        brigadierMapper.mapSimpleNMS(
                new TypeToken<RegionFlagParser<OrbisSession>>() {}, "resource_location", true);
        brigadierMapper.mapSimpleNMS(
                new TypeToken<RegionisedWorldParser<OrbisSession>>() {}, "resource_location", true);
        brigadierMapper.mapSimpleNMS(
                new TypeToken<FlagValueParser<OrbisSession>>() {}, "message", true);
        brigadierMapper.mapSimpleNMS(
                new TypeToken<RegistryValueParser<OrbisSession, ?, ?>>() {},
                "resource_location",
                true);
    }
}
