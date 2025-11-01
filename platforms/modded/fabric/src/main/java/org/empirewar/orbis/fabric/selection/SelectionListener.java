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
package org.empirewar.orbis.fabric.selection;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
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
import org.empirewar.orbis.util.OrbisTranslations;
import org.joml.Vector3i;
import org.joml.Vector3ic;

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
                        .copyTag()
                        .contains("orbis_is_wand")) {
            return InteractionResult.PASS;
        }

        final Selection selection = api.selectionManager().get(player.getUUID()).orElse(null);
        if (selection == null) {
            ((Audience) player)
                    .sendMessage(OrbisText.PREFIX.append(OrbisTranslations.SELECTION_NOT_ACTIVE));
            return InteractionResult.PASS;
        }

        final Vector3ic last =
                selection.getPoints().stream().reduce((first, second) -> second).orElse(null);
        if (last == null) return InteractionResult.PASS;
        selection.removePoint(last);
        ((Audience) player)
                .sendMessage(OrbisText.PREFIX.append(OrbisTranslations.SELECTION_POINT_REMOVED));
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
                        .copyTag()
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

        ((Audience) player)
                .sendMessage(OrbisText.PREFIX
                        .append(OrbisTranslations.SELECTION_POINT_ADDED.arguments(
                                Component.text(point.x),
                                Component.text(point.y),
                                Component.text(point.z)))
                        .hoverEvent(
                                HoverEvent.showText(OrbisTranslations.GENERIC_CLICK_TO_TELEPORT))
                        .clickEvent(ClickEvent.callback(
                                audience -> player.teleportTo(point.x, point.y, point.z),
                                ClickCallback.Options.builder()
                                        .lifetime(Duration.ofMinutes(3))
                                        .build())));

        return InteractionResult.FAIL;
    }
}
