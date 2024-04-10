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
package org.empirewar.orbis.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegionisedWorldTest {

    @Test
    @Order(1)
    void testAddingGettingAndRemovingRegion() {
        RegionisedWorldSet set = new RegionisedWorldSet();
        Region region = new Region("test");
        assertTrue(set.add(region));
        assertTrue(set.getByName("test").isPresent());
        assertTrue(set.remove(region));
    }

    @Test
    @Order(2)
    void testWorldPositionQuery() {
        RegionisedWorldSet set = new RegionisedWorldSet();
        Region region = new Region("test");
        region.area().addPoint(new Vector3i());
        region.area().addPoint(new Vector3i(5, 5, 5));
        set.add(region);
        assertTrue(set.query(RegionQuery.Position.builder()
                        .position(new Vector3d(4, 4, 4))
                        .build())
                .result()
                .contains(region));
    }

    @Test
    @Order(3)
    void testWorldPositionPriorityQuery() {
        RegionisedWorldSet set = new RegionisedWorldSet();
        Region region = new Region("test");
        Region region2 = new Region("test2");
        Region region3 = new Region("test3");

        region.area().addPoint(new Vector3i());
        region.area().addPoint(new Vector3i(5, 5, 5));

        region2.area().addPoint(new Vector3i());
        region2.area().addPoint(new Vector3i(5, 5, 5));
        region2.priority(100);

        region3.area().addPoint(new Vector3i());
        region3.area().addPoint(new Vector3i(5, 5, 5));
        region3.priority(50);

        set.add(region);
        set.add(region3);
        set.add(region2);

        final Set<Region> result = set.query(RegionQuery.Position.builder()
                        .position(new Vector3d(4, 4, 4))
                        .build())
                .result();
        assertEquals(region2, result.stream().findFirst().orElseThrow());
        assertEquals(region3, result.stream().toList().get(1));
        assertEquals(region, result.stream().toList().get(2));
    }

    @Test
    @Order(4)
    void testChainedWorldPositionQuery() {
        RegionisedWorldSet set = new RegionisedWorldSet();
        Region region = new Region("test");
        region.area().addPoint(new Vector3i());
        region.area().addPoint(new Vector3i(5, 5, 5));
        region.addFlag(DefaultFlags.CAN_BREAK);
        region.setFlag(DefaultFlags.CAN_BREAK, false);
        set.add(region);

        final boolean canAct = set.query(RegionQuery.Position.builder().position(4, 4, 4))
                .query(RegionQuery.Flag.builder(DefaultFlags.CAN_BREAK))
                .result()
                .orElse(true);
        assertFalse(canAct);
    }

    @Test
    @Order(5)
    void testComplexChainedWorldPositionQuery() {
        RegionisedWorldSet set = new RegionisedWorldSet();
        Region region = new Region("test");
        Region region2 = new Region("test2");
        region.area().addPoint(new Vector3i());
        region.area().addPoint(new Vector3i(5, 5, 5));
        region.addFlag(DefaultFlags.CAN_BREAK);
        region.setFlag(DefaultFlags.CAN_BREAK, false);

        region2.area().addPoint(new Vector3i());
        region2.area().addPoint(new Vector3i(5, 5, 5));
        region2.addFlag(DefaultFlags.CAN_BREAK);
        region2.setFlag(DefaultFlags.CAN_BREAK, true);
        region2.priority(2);

        set.add(region);
        set.add(region2);

        final boolean canAct = set.query(RegionQuery.Position.builder()
                        .position(new Vector3d(4, 4, 4))
                        .build())
                .query(RegionQuery.Flag.<Boolean>builder()
                        .flag(DefaultFlags.CAN_BREAK)
                        .build())
                .result()
                .orElse(true);
        assertTrue(canAct);

        // Now flip so region 1 has priority
        region2.priority(0);

        final boolean canAct2 = set.query(RegionQuery.Position.builder()
                        .position(new Vector3d(4, 4, 4))
                        .build())
                .query(RegionQuery.Flag.<Boolean>builder()
                        .flag(DefaultFlags.CAN_BREAK)
                        .build())
                .result()
                .orElse(true);
        assertFalse(canAct2);
    }
}
