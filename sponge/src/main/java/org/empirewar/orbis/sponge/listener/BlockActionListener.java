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
package org.empirewar.orbis.sponge.listener;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.BlockTypeTags;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;

public final class BlockActionListener {

    private final Orbis orbis;

    public BlockActionListener(Orbis orbis) {
        this.orbis = orbis;
    }

    @Listener(order = Order.EARLY)
    public void onChange(ChangeBlockEvent.All event, @First ServerPlayer serverPlayer) {
        event.transactions(Operations.BREAK.get()).forEach(transaction -> {
            if (shouldPreventBlockAction(
                    transaction.original(), serverPlayer, DefaultFlags.CAN_BREAK)) {
                event.invalidateAll();
            }
        });

        event.transactions(Operations.PLACE.get()).forEach(transaction -> {
            if (shouldPreventBlockAction(
                    transaction.original(), serverPlayer, DefaultFlags.CAN_PLACE)) {
                event.invalidateAll();
            }
        });

        event.transactions(Operations.DECAY.get()).forEach(transaction -> {
            if (shouldPreventBlockAction(transaction.original(), DefaultFlags.LEAF_DECAY)) {
                transaction.invalidate();
            }
        });

        event.transactions(Operations.GROWTH.get()).forEach(transaction -> {
            final BlockSnapshot block = transaction.finalReplacement();
            final RegionisedWorld world = orbis.getRegionisedWorld(event.world().key());
            final List<Key> growable = world.query(RegionQuery.Position.builder()
                            .position(
                                    block.position().x(),
                                    block.position().y(),
                                    block.position().z()))
                    .query(RegionQuery.Flag.builder(DefaultFlags.GROWABLE_BLOCKS))
                    .result()
                    .orElse(null);
            if (growable == null) return;

            if (!growable.contains(block.state().type().key(RegistryTypes.BLOCK_TYPE))) {
                transaction.invalidate();
            }
        });

        event.transactions(Operations.MODIFY.get()).forEach(transaction -> {
            final BlockSnapshot original = transaction.original();
            final BlockSnapshot replacement = transaction.finalReplacement();
            if (original.state().type().isAnyOf(BlockTypes.FARMLAND.get())
                    && replacement.state().type().isAnyOf(BlockTypes.DIRT.get())) {
                if (shouldPreventBlockAction(original, serverPlayer, DefaultFlags.BLOCK_TRAMPLE)) {
                    transaction.invalidate();
                }
            }

            if (!original.state().type().is(BlockTypeTags.CORALS)) return;
            if (replacement
                    .state()
                    .type()
                    .key(RegistryTypes.BLOCK_TYPE)
                    .asString()
                    .contains("DEAD")) {
                if (shouldPreventBlockAction(original, DefaultFlags.CORAL_DECAY)) {
                    transaction.invalidate();
                }
            }
        });
    }

    @Listener(order = Order.EARLY)
    public void onFireSpread(ChangeBlockEvent.Pre event) {
        if (!event.context().containsKey(EventContextKeys.FIRE_SPREAD)) return;
        for (ServerLocation location : event.locations()) {
            if (shouldPreventBlockAction(location.createSnapshot(), DefaultFlags.FIRE_SPREAD)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @Listener(order = Order.EARLY)
    public void onAttemptAccess(InteractContainerEvent.Open event) {
        // TODO
        //        event.context().get(EventContextKeys.BLOCK_EVENT_PROCESS)
        //        event.setCancelled(
        //
        // shouldPreventBlockAction(event.container().currentMenu().orElseThrow().inventory().,
        // DefaultFlags.BLOCK_INVENTORY_ACCESS));
    }

    @Listener(order = Order.EARLY)
    public void onRedstoneUse(InteractBlockEvent.Secondary event, @Root ServerPlayer player) {
        final BlockSnapshot block = event.block();
        if (block.get(Keys.POWER).isPresent()
                || block.get(Keys.IS_POWERED).isPresent()
                || block.get(Keys.REDSTONE_DELAY).isPresent()) {
            if (shouldPreventBlockAction(block, player, DefaultFlags.TRIGGER_REDSTONE)) {
                event.setUseBlockResult(Tristate.FALSE);
                event.setCancelled(true);
            }
        }
    }

    private boolean shouldPreventBlockAction(
            @Nullable BlockSnapshot block, RegionFlag<Boolean> flag) {
        return shouldPreventBlockAction(block, null, flag);
    }

    // spotless:off
    private boolean shouldPreventBlockAction(@Nullable BlockSnapshot block, @Nullable ServerPlayer player, RegionFlag<Boolean> flag) {
        // spotless:on
        if (block == null) return false;
        final Vector3i blockPos = block.position();
        final Vector3d pos = new Vector3d(blockPos.x(), blockPos.y(), blockPos.z());
        final RegionisedWorld world =
                orbis.getRegionisedWorld(block.location().orElseThrow().world().key());
        final RegionQuery.Flag.Builder<Boolean> builder = RegionQuery.Flag.builder(flag);
        if (player != null) builder.player(player.uniqueId());
        final boolean canAct = world.query(RegionQuery.Position.builder().position(pos))
                .query(builder)
                .result()
                .orElse(true);
        return !canAct;
    }
}
