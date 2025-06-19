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
    public static final RegistryRegionFlag<Boolean> CAN_BREAK = register("can_break",
            "Whether players can break blocks", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> CAN_PLACE = register("can_place",
            "Whether players can place blocks", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> CAN_PVP = register("can_pvp",
            "Whether players can attack each other", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> CAN_TAKE_MOB_DAMAGE_SOURCES = register("can_take_mob_damage_sources",
            "Whether players can take damage from mobs", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<List<Key>> DAMAGEABLE_ENTITIES = register("damageable_entities",
            "An array of entity keys that can be attacked", ArrayList::new, ExtraCodecs.KEY.listOf());
    public static final RegistryRegionFlag<Boolean> CAN_DESTROY_ITEM_FRAME = register("can_destroy_item_frame", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> CAN_DESTROY_PAINTING = register("can_destroy_painting", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> CAN_DESTROY_VEHICLE = register("can_destroy_vehicle", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> FALL_DAMAGE = register("fall_damage",
            "Whether players take fall damage", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> CAN_DROP_ITEM = register("can_drop_item",
            "Whether players can drop items", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> CAN_PICKUP_ITEM = register("can_pickup_item",
            "Whether players can pick up items", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> BLOCK_INVENTORY_ACCESS = register("block_inventory_access",
            "Whether players can access block inventories (such as barrels)", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> TRIGGER_REDSTONE = register("trigger_redstone",
            "Whether players can trigger redstone", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> CORAL_DECAY = register("coral_decay",
            "Whether coral should bleach naturally", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> LEAF_DECAY = register("leaf_decay",
            "Whether leaves should decay naturally", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<List<Key>> GROWABLE_BLOCKS = register("growable_blocks",
            "An array of block keys that can grow with age (e.g. wheat, sugarcane, but not grass)", ArrayList::new, ExtraCodecs.KEY.listOf());
    public static final RegistryRegionFlag<Boolean> BLOCK_TRAMPLE = register("block_trample",
            "Whether blocks such as farmland can be trampled (turned to dirt)", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> ITEM_FRAME_ROTATE = register("item_frame_rotate", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> ITEM_FRAME_ITEM_PLACE = register("item_frame_item_place", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> CAN_ENTER = register("can_enter",
            "Whether players can enter this region. Useful when combined with flag groups.", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> DRAIN_HUNGER = register("drain_hunger",
            "Whether players should lose hunger (cancels plugin sources)", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Boolean> FIRE_SPREAD = register("fire_spread",
            "Whether fire should spread naturally", () -> true, Codec.BOOL);
    public static final RegistryRegionFlag<Long> TIME = register("time",
            "An integer/long specifying the time of day in ticks", () -> 12000L, Codec.LONG);
    public static final RegistryRegionFlag<Boolean> INVULNERABILITY = register("invulnerability",
            "Whether players should take damage from any source", () -> false, Codec.BOOL);
//    public static final RegionFlag<Component> DENY_MESSAGE = register("deny_message", () -> Component.text("You can't do that here.", TextColor.color(244, 61, 61)), ExtraCodecs.COMPONENT);
    // spotless:on

    private static <T> RegistryRegionFlag<T> register(
            String name, Supplier<T> defaultValue, Codec<T> codec) {
        return register(name, null, defaultValue, codec);
    }

    private static <T> RegistryRegionFlag<T> register(
            String name, String description, Supplier<T> defaultValue, Codec<T> codec) {
        final Key key = Key.key("orbis", name);
        final RegistryRegionFlag.Builder<T> entry =
                RegistryRegionFlag.<T>builder().key(key).codec(codec).defaultValue(defaultValue);
        if (description != null) {
            entry.description(description);
        }

        final RegistryRegionFlag<T> built = entry.build();
        Registries.FLAGS.register(key, built);
        return built;
    }
}
