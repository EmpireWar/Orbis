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
package org.empirewar.orbis.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.area.EncompassingArea;
import org.empirewar.orbis.minecraft.flags.MinecraftFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegionisedWorldTest {

    @Test
    @Order(1)
    void testAddingGettingAndRemovingRegion() {
        RegionisedWorldSet set = new RegionisedWorldSet();
        Region region = new Region("test", new CuboidArea());
        assertTrue(set.add(region));
        assertTrue(set.getByName("test").isPresent());
        assertTrue(set.remove(region));
    }

    @Test
    @Order(2)
    void testWorldPositionQuery() {
        RegionisedWorldSet set = new RegionisedWorldSet();
        Region region = new Region("test", new CuboidArea());
        // Based on a real-world example
        region.area().addPoint(new Vector3i(-467, 147, 49));
        region.area().addPoint(new Vector3i(-652, 4, -87));
        set.add(region);
        assertTrue(set.query(RegionQuery.Position.builder()
                        .position(new Vector3d(-580, 81, -26))
                        .build())
                .result()
                .contains(region));
    }

    @Test
    @Order(3)
    void testWorldPositionPriorityQuery() {
        RegionisedWorldSet set = new RegionisedWorldSet();
        Region region = new Region("test", new CuboidArea());
        Region region2 = new Region("test2", new CuboidArea());
        Region region3 = new Region("test3", new CuboidArea());

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
        Region region = new Region("test", new CuboidArea());
        // Based on a real-world example
        region.area().addPoint(new Vector3i(-467, 147, 49));
        region.area().addPoint(new Vector3i(-652, 4, -87));
        region.addFlag(MinecraftFlags.CAN_BREAK);
        region.setFlag(MinecraftFlags.CAN_BREAK, false);
        set.add(region);

        final boolean canAct = set.query(RegionQuery.Position.builder().position(-580, 81, -26))
                .query(RegionQuery.Flag.builder(MinecraftFlags.CAN_BREAK))
                .result()
                .orElse(true);
        assertFalse(canAct);
    }

    @Test
    @Order(5)
    void testComplexChainedWorldPositionQuery() {
        RegionisedWorldSet set = new RegionisedWorldSet();
        Region region = new Region("test", new CuboidArea());
        Region region2 = new Region("test2", new CuboidArea());
        region.area().addPoint(new Vector3i());
        region.area().addPoint(new Vector3i(5, 5, 5));
        region.addFlag(MinecraftFlags.CAN_BREAK);
        region.setFlag(MinecraftFlags.CAN_BREAK, false);

        region2.area().addPoint(new Vector3i());
        region2.area().addPoint(new Vector3i(5, 5, 5));
        region2.addFlag(MinecraftFlags.CAN_BREAK);
        region2.setFlag(MinecraftFlags.CAN_BREAK, true);
        region2.priority(region.priority() + 1);

        set.add(region);
        set.add(region2);

        final boolean canAct = set.query(RegionQuery.Position.builder()
                        .position(new Vector3d(4, 4, 4))
                        .build())
                .query(RegionQuery.Flag.<Boolean>builder()
                        .flag(MinecraftFlags.CAN_BREAK)
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
                        .flag(MinecraftFlags.CAN_BREAK)
                        .build())
                .result()
                .orElse(true);
        assertFalse(canAct2);
    }

    @Test
    @Order(6)
    void testGlobalRegionQuery() {
        RegionisedWorldSet set = new RegionisedWorldSet(Key.key("orbis:world"));
        final Region global = new GlobalRegion(set);
        assertTrue(global.isGlobal());
        // Adding should succeed
        assertTrue(set.add(global));
        // Trying to add again should fail
        assertFalse(set.add(global));

        // Querying the global region of the world at any position should succeed
        assertTrue(set.query(RegionQuery.Position.builder().position(5, 5, 5))
                .result()
                .contains(global));

        // Removing the global region of the world should not be allowed
        assertThrows(IllegalArgumentException.class, () -> set.remove(global));
    }

    @Test
    @Order(7)
    void testRegionAddsListenerToArea() {
        // Setup
        RegionisedWorldSet set = new RegionisedWorldSet();
        CuboidArea area = new CuboidArea();
        Region region = new Region("test", area);

        // Get the updateListeners field using reflection
        List<Runnable> listeners = getUpdateListeners(area);

        // Verify no listeners initially
        assertTrue(listeners.isEmpty(), "Should have no listeners initially");

        // Add region
        assertTrue(set.add(region));

        // Verify one listener was added
        assertEquals(1, listeners.size(), "Should have one listener after adding region");
    }

    @Test
    @Order(8)
    void testAreaUpdateUpdatesRTree() {
        // Setup
        RegionisedWorldSet set = new RegionisedWorldSet();
        CuboidArea area = new CuboidArea();
        Region region = new Region("test", area);

        // Add region
        assertTrue(set.add(region));

        // Query should return empty before adding points
        assertTrue(
                set.query(RegionQuery.Position.at(1, 1, 1)).result().isEmpty(),
                "Should not find region before adding points");

        // Add two points to form a valid cuboid area
        area.addPoint(new Vector3i(0, 0, 0));
        area.addPoint(new Vector3i(2, 2, 2));

        // Query should now find the region within the cuboid
        assertFalse(
                set.query(RegionQuery.Position.at(1, 1, 1)).result().isEmpty(),
                "Should find region inside cuboid after adding points");

        // Verify only one listener exists
        List<Runnable> listeners = getUpdateListeners(area);
        assertEquals(1, listeners.size(), "Should still have only one listener");

        // Also verify point outside the cuboid is not included
        assertTrue(
                set.query(RegionQuery.Position.at(3, 3, 3)).result().isEmpty(),
                "Should not find region outside the cuboid");
    }

    @Test
    @Order(9)
    void testRemoveRegionRemovesListener() {
        // Setup
        RegionisedWorldSet set = new RegionisedWorldSet();
        CuboidArea area = new CuboidArea();
        Region region = new Region("test", area);

        // Add and then remove region
        assertTrue(set.add(region));
        assertTrue(set.remove(region));

        // Verify listener was removed
        List<Runnable> listeners = getUpdateListeners(area);
        assertTrue(listeners.isEmpty(), "Should have no listeners after removing region");
    }

    @Test
    @Order(10)
    void testMultipleAreaUpdates() {
        // Setup
        RegionisedWorldSet set = new RegionisedWorldSet();
        CuboidArea area = new CuboidArea();
        Region region = new Region("test", area);

        // Add region and initial point
        assertTrue(set.add(region));
        area.addPoint(new Vector3i(0, 0, 0));

        // Get initial listener count
        List<Runnable> listeners = getUpdateListeners(area);
        int initialListenerCount = listeners.size();

        // Update area multiple times
        for (int i = 1; i <= 5; i++) {
            area.addPoint(new Vector3i(i, i, i));
            // Verify listener count remains the same
            assertEquals(
                    initialListenerCount,
                    listeners.size(),
                    "Listener count should remain the same after update " + i);
        }
    }

    /**
     * Helper method to get the updateListeners field from EncompassingArea using reflection.
     */
    @SuppressWarnings("unchecked")
    private List<Runnable> getUpdateListeners(EncompassingArea area) {
        try {
            Field field = EncompassingArea.class.getDeclaredField("updateListeners");
            field.setAccessible(true);
            return (List<Runnable>) field.get(area);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get updateListeners", e);
        }
    }
}
