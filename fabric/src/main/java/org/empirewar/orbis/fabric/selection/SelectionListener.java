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
package org.empirewar.orbis.fabric.selection;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.command.Permissions;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.util.OrbisText;
import org.joml.Vector3i;

import java.time.Duration;

public final class SelectionListener {

    private final Orbis api;

    public SelectionListener(Orbis api) {
        this.api = api;

        // TODO how do we listen to left click air on fabric?
        AttackBlockCallback.EVENT.register(this::onLeftClick);
        UseItemCallback.EVENT.register(this::onRightClick);
    }

    private InteractionResult onRightClick(
            Player player, Level level, InteractionHand interactionHand) {
        if (!me.lucko.fabric.api.permissions.v0.Permissions.check(player, Permissions.MANAGE, 3))
            return InteractionResult.PASS;

        final ItemStack item = player.getItemInHand(interactionHand);
        if (item.isEmpty()
                || !item.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                        .contains("orbis_is_wand")) {
            return InteractionResult.PASS;
        }

        final Selection selection = api.selectionManager().get(player.getUUID()).orElse(null);
        if (selection == null) {
            ((Audience) player)
                    .sendMessage(OrbisText.PREFIX.append(Component.text(
                            "You don't have an active selection.", OrbisText.SECONDARY_RED)));
            return InteractionResult.PASS;
        }

        final Vector3i last =
                selection.getPoints().stream().reduce((first, second) -> second).orElse(null);
        if (last == null) return InteractionResult.PASS;
        selection.removePoint(last);
        ((Audience) player)
                .sendMessage(OrbisText.PREFIX.append(
                        Component.text("Removed the last added point.", OrbisText.SECONDARY_RED)));
        return InteractionResult.SUCCESS;
    }

    private InteractionResult onLeftClick(
            Player player,
            Level level,
            InteractionHand interactionHand,
            BlockPos blockPos,
            Direction direction) {
        if (!me.lucko.fabric.api.permissions.v0.Permissions.check(player, Permissions.MANAGE, 3))
            return InteractionResult.PASS;

        final ItemStack item = player.getItemInHand(interactionHand);
        if (item.isEmpty()
                || !item.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                        .contains("orbis_is_wand")) return InteractionResult.PASS;

        final Selection selection = api.selectionManager().get(player.getUUID()).orElseGet(() -> {
            Selection newSelection = new Selection(AreaType.CUBOID);
            api.selectionManager().add(player.getUUID(), newSelection);
            ((Audience) player)
                    .sendMessage(OrbisText.PREFIX.append(Component.text(
                            "Started a new cuboid selection.", OrbisText.EREBOR_GREEN)));
            return newSelection;
        });

        Vector3i point;
        final BlockState block = level.getBlockState(blockPos);
        if (block.isAir()) {
            final BlockPos playerPos = player.blockPosition();
            point = new Vector3i(playerPos.getX(), playerPos.getY(), playerPos.getZ());
        } else {
            point = new Vector3i(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }

        selection.addPoint(point);
        final TextComponent teleportPart = Component.text(
                        "[" + point.x + ", " + point.y + ", " + point.z + "]",
                        OrbisText.EREBOR_GREEN)
                .hoverEvent(HoverEvent.showText(
                        Component.text("Click to teleport.", OrbisText.EREBOR_GREEN)))
                .clickEvent(ClickEvent.callback(
                        audience -> player.teleportTo(point.x, point.y, point.z),
                        ClickCallback.Options.builder()
                                .lifetime(Duration.ofMinutes(3))
                                .build()));
        ((Audience) player)
                .sendMessage(OrbisText.PREFIX.append(
                        Component.text("Added point ", OrbisText.EREBOR_GREEN)
                                .append(teleportPart)
                                .append(Component.text(" to selection.", OrbisText.EREBOR_GREEN))));

        return InteractionResult.FAIL;
    }
}
