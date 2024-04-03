/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024  EmpireWar
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
package org.empirewar.orbis.area;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.joml.Vector3d;
import org.joml.Vector3i;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EncompassingAreaTest {

    @Test
    @Order(1)
    void testAdd() {
        CuboidArea area = new CuboidArea();
        assertTrue(area.addPoint(new Vector3i(0, 0, 0)));
        assertTrue(area.addPoint(new Vector3i(10, 10, 10)));
        assertEquals(2, area.points().size());
    }

    @Test
    @Order(2)
    void testRemove() {
        CuboidArea area = new CuboidArea();
        area.addPoint(new Vector3i(0, 0, 0));
        assertTrue(area.removePoint(new Vector3i(0, 0, 0)));
        assertTrue(area.points().isEmpty());
    }

    @Test
    @Order(3)
    void testMinMax() {
        CuboidArea area = new CuboidArea();
        area.addPoint(new Vector3i(0, 0, 0));
        area.addPoint(new Vector3i(10, 10, 10));
        assertEquals(new Vector3i(0, 0, 0), area.getMin());
        assertEquals(new Vector3i(10, 10, 10), area.getMax());
    }

    @Test
    @Order(4)
    void testExceedsPointLimits() {
        CuboidArea area = new CuboidArea();
        area.addPoint(new Vector3i(0, 0, 0));
        area.addPoint(new Vector3i(10, 10, 10));
        assertFalse(area.addPoint(new Vector3i(222, 222, 222)));
    }

    @Test
    @Order(5)
    void testContains() {
        CuboidArea area = new CuboidArea();
        assertFalse(area.contains(new Vector3d(0, 0, 0)));
        area.addPoint(new Vector3i(0, 0, 0));
        area.addPoint(new Vector3i(10, 10, 10));
        assertTrue(area.contains(new Vector3d(0, 0, 0)));
        assertTrue(area.contains(new Vector3d(1, 1, 1)));
        assertTrue(area.contains(new Vector3d(10, 10, 10)));
        assertFalse(area.contains(new Vector3d(-10, -10, -10)));
    }
}
