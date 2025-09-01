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
package org.empirewar.orbis.migrations.rpgregions;

import net.islandearth.rpgregions.api.IRPGRegionsAPI;
import net.islandearth.rpgregions.api.RPGRegionsAPI;
import net.islandearth.rpgregions.api.integrations.IntegrationManager;
import net.islandearth.rpgregions.api.integrations.rpgregions.RPGRegionsIntegration;
import net.islandearth.rpgregions.api.integrations.rpgregions.region.PolyRegion;
import net.islandearth.rpgregions.api.integrations.rpgregions.region.RPGRegionsRegion;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.area.PolygonArea;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.world.RegionisedWorld;
import org.joml.Vector3i;

public final class RPGRegionsMigrator {

    public RPGRegionsMigrator(Audience actor) {
        final Audience audience = Audience.audience(actor, Bukkit.getConsoleSender());

        final IRPGRegionsAPI api = RPGRegionsAPI.getAPI();
        final IntegrationManager integrationManager = api.getManagers().getIntegrationManager();
        if (!(integrationManager instanceof RPGRegionsIntegration integration)) {
            audience.sendMessage(Component.text(
                    "The RPGRegions integration is not the default one.", NamedTextColor.RED));
            return;
        }

        int worldIndex = 1;
        for (World world : Bukkit.getWorlds()) {
            audience.sendMessage(Component.text(
                    "Processing worlds (" + worldIndex + "/"
                            + Bukkit.getWorlds().size() + "): " + world.getName(),
                    NamedTextColor.GREEN));

            final RegionisedWorld regionisedWorld = OrbisAPI.get().getRegionisedWorld(world.key());

            for (RPGRegionsRegion region : integration.getRegions()) {
                if (!region.getWorld().equals(world.getUID())) continue;

                audience.sendMessage(Component.text(
                        "Processing region " + region.getName() + "...", NamedTextColor.YELLOW));

                Area area = region instanceof PolyRegion ? new PolygonArea() : new CuboidArea();
                for (Location point : region.getPoints()) {
                    area.addPoint(new Vector3i(point.blockX(), point.blockY(), point.blockZ()));
                }

                Region orbisRegion = new Region(region.getName(), area);
                orbisRegion.priority(region.getPriority());

                regionisedWorld.add(orbisRegion);
                OrbisRegistries.REGIONS.register(orbisRegion.key(), orbisRegion);
                audience.sendMessage(Component.text(
                        "Added region '" + orbisRegion.name() + "'.", NamedTextColor.GREEN));
            }

            worldIndex++;
        }

        audience.sendMessage(Component.text("Migration complete.", NamedTextColor.GREEN));

        // spotless:off
        audience.sendMessage(Component.text("What's next?", NamedTextColor.YELLOW, TextDecoration.BOLD));
        audience.sendMessage(Component.text(" 1) Stop your server.", NamedTextColor.GRAY));
        audience.sendMessage(Component.text(" 2) Change the RPGRegions integration to Orbis.", NamedTextColor.GRAY));
        audience.sendMessage(Component.text(" 3) Start your server again. Welcome to Orbis!", NamedTextColor.GRAY));
        // spotless:on
    }
}
