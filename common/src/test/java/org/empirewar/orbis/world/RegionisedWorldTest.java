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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

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
}
