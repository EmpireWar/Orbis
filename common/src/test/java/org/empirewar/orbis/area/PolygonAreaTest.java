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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.joml.Vector3i;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Arrays;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PolygonAreaTest {

    @Test
    @Order(1)
    void testContains() {
        PolygonArea area = new PolygonArea();
        // Make a triangle
        assertTrue(area.addPoint(new Vector3i(0, 0, 0)));
        assertTrue(area.addPoint(new Vector3i(3, 0, 0)));
        assertTrue(area.addPoint(new Vector3i(3, 0, 4)));

        // Double position
        assertTrue(area.contains(2, 0, 1.6));
        // Point on the line
        assertTrue(area.contains(3, 0, 4));
        assertFalse(area.contains(4, 0, 4));
        assertFalse(area.contains(6, 0, 4));

        area.clearPoints();

        List<Vector3i> squarePolygon = Arrays.asList(
                new Vector3i(1, 0, 1), // Bottom-left corner
                new Vector3i(5, 0, 1), // Bottom-right corner
                new Vector3i(5, 0, 5), // Top-right corner
                new Vector3i(1, 0, 5) // Top-left corner
                );
        squarePolygon.forEach(area::addPoint);
        assertTrue(area.contains(3, 0, 3));
        assertFalse(area.contains(6, 0, 3));

        area.clearPoints();

        List<Vector3i> pentagonPolygon = Arrays.asList(
                new Vector3i(3, 0, 1), // Bottom vertex
                new Vector3i(6, 0, 3), // Bottom-right vertex
                new Vector3i(5, 0, 6), // Top-right vertex
                new Vector3i(1, 0, 6), // Top-left vertex
                new Vector3i(0, 0, 3) // Bottom-left vertex
                );
        pentagonPolygon.forEach(area::addPoint);
        assertTrue(area.contains(3, 0, 4));
        assertFalse(area.contains(7, 0, 3));
    }
}
