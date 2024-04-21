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
package org.empirewar.orbis.flag;

import com.mojang.serialization.Codec;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.registry.Registries;
import org.empirewar.orbis.util.ExtraCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The default flags that Orbis provides.
 */
public final class DefaultFlags {

    // spotless:off
    public static final RegionFlag<Boolean> CAN_BREAK = register("can_break", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> CAN_PLACE = register("can_place", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> CAN_PVP = register("can_pvp", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> CAN_TAKE_MOB_DAMAGE_SOURCES = register("can_take_mob_damage_sources", () -> true, Codec.BOOL);
    public static final RegionFlag<List<Key>> DAMAGEABLE_ENTITIES = register("damageable_entities", ArrayList::new, ExtraCodecs.KEY.listOf());
    public static final RegionFlag<Boolean> CAN_DESTROY_ITEM_FRAME = register("can_destroy_item_frame", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> CAN_DESTROY_PAINTING = register("can_destroy_painting", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> CAN_DESTROY_VEHICLE = register("can_destroy_vehicle", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> FALL_DAMAGE = register("fall_damage", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> CAN_DROP_ITEM = register("can_drop_item", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> CAN_PICKUP_ITEM = register("can_pickup_item", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> BLOCK_INVENTORY_ACCESS = register("block_inventory_access", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> TRIGGER_REDSTONE = register("trigger_redstone", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> CORAL_DECAY = register("coral_decay", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> LEAF_DECAY = register("leaf_decay", () -> true, Codec.BOOL);
    public static final RegionFlag<List<Key>> GROWABLE_BLOCKS = register("growable_blocks", ArrayList::new, ExtraCodecs.KEY.listOf());
    public static final RegionFlag<Boolean> BLOCK_TRAMPLE = register("block_trample", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> ITEM_FRAME_ROTATE = register("item_frame_rotate", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> ITEM_FRAME_ITEM_PLACE = register("item_frame_item_place", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> CAN_ENTER = register("can_enter", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> DRAIN_HUNGER = register("drain_hunger", () -> true, Codec.BOOL);
    public static final RegionFlag<Boolean> FIRE_SPREAD = register("fire_spread", () -> true, Codec.BOOL);
    public static final RegionFlag<Long> TIME = register("time", () -> 12000L, Codec.LONG);
//    public static final RegionFlag<Component> DENY_MESSAGE = register("deny_message", () -> Component.text("You can't do that here.", TextColor.color(244, 61, 61)), ExtraCodecs.COMPONENT);
    // spotless:on

    private static <T> RegionFlag<T> register(
            String name, Supplier<T> defaultValue, Codec<T> codec) {
        final Key key = Key.key("orbis", name);
        final RegionFlag<T> entry = RegionFlag.<T>builder()
                .key(key)
                .codec(codec)
                .defaultValue(defaultValue)
                .build();
        Registries.FLAGS.register(key, entry);
        return entry;
    }
}
