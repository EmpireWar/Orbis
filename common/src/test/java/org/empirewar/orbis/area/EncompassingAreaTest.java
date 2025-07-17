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
package org.empirewar.orbis.area;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertFalse(area.contains(0, 0, 0));
        area.addPoint(new Vector3i(0, 0, 0));
        area.addPoint(new Vector3i(10, 10, 10));
        assertTrue(area.contains(0, 0, 0));
        assertTrue(area.contains(1, 1, 1));
        assertTrue(area.contains(10, 10, 10));
        assertFalse(area.contains(-10, -10, -10));
        area.removePoint(new Vector3i(0, 0, 0));
        area.removePoint(new Vector3i(10, 10, 10));
        // Based on a real-world example
        area.addPoint(new Vector3i(-467, 147, 49));
        area.addPoint(new Vector3i(-652, 4, -87));
        assertTrue(area.contains(-580, 81, -26));
    }
}
