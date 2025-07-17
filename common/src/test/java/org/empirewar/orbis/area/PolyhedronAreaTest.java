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
public class PolyhedronAreaTest {

    @Test
    @Order(1)
    void testContains() {
        PolyhedralArea area = new PolyhedralArea();

        // Test with a cube (6 faces, 8 vertices)
        List<Vector3i> cubeVertices = Arrays.asList(
                // Bottom face (y=0)
                new Vector3i(0, 0, 0),
                new Vector3i(4, 0, 0),
                new Vector3i(4, 0, 4),
                new Vector3i(0, 0, 4),
                // Top face (y=4)
                new Vector3i(0, 4, 0),
                new Vector3i(4, 4, 0),
                new Vector3i(4, 4, 4),
                new Vector3i(0, 4, 4));

        // Add all vertices to the polyhedron
        cubeVertices.forEach(area::addPoint);

        // Test points inside the cube
        assertTrue(area.contains(2, 2, 2)); // Center
        assertTrue(area.contains(1, 1, 1)); // Near bottom front left corner
        assertTrue(area.contains(3, 3, 3)); // Near top back right corner

        // Test points on the surface
        assertTrue(area.contains(2, 0, 2)); // On bottom face
        assertTrue(area.contains(2, 4, 2)); // On top face
        assertTrue(area.contains(0, 2, 2)); // On left face
        assertTrue(area.contains(4, 2, 2)); // On right face
        assertTrue(area.contains(2, 2, 0)); // On front face
        assertTrue(area.contains(2, 2, 4)); // On back face

        // Test points outside the cube
        assertFalse(area.contains(-1, 2, 2)); // Left of cube
        assertFalse(area.contains(5, 2, 2)); // Right of cube
        assertFalse(area.contains(2, -1, 2)); // Below cube
        assertFalse(area.contains(2, 5, 2)); // Above cube
        assertFalse(area.contains(2, 2, -1)); // In front of cube
        assertFalse(area.contains(2, 2, 5)); // Behind cube

        // Test points exactly on vertices
        assertTrue(area.contains(0, 0, 0)); // Bottom front left vertex
        assertTrue(area.contains(4, 4, 4)); // Top back right vertex

        // Clear for next test
        area.clearPoints();

        // Test with a pyramid (5 faces, 5 vertices)
        List<Vector3i> pyramidVertices = Arrays.asList(
                // Base (square)
                new Vector3i(0, 0, 0),
                new Vector3i(4, 0, 0),
                new Vector3i(4, 0, 4),
                new Vector3i(0, 0, 4),
                // Apex
                new Vector3i(2, 4, 2));

        pyramidVertices.forEach(area::addPoint);

        // Test points inside the pyramid
        assertTrue(area.contains(2, 1, 2)); // Inside the pyramid
        assertTrue(area.contains(1, 1, 1)); // Inside the pyramid near base
        assertTrue(area.contains(3, 1, 3)); // Inside the pyramid near base

        // Test points on the surface
        assertTrue(area.contains(2, 0, 2)); // Center of base
        assertTrue(area.contains(2, 4, 2)); // Apex
        assertTrue(area.contains(1, 2, 1)); // On a triangular face

        // Test points outside the pyramid
        assertFalse(area.contains(2, -1, 2)); // Below base
        assertFalse(area.contains(2, 5, 2)); // Above apex
        assertFalse(area.contains(5, 0, 2)); // Outside x bounds
        assertFalse(area.contains(2, 2, 5)); // Outside z bounds
    }
}
