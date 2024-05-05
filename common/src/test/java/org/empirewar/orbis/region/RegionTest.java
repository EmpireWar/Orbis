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
package org.empirewar.orbis.region;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.empirewar.orbis.area.CuboidArea;
import org.junit.jupiter.api.Test;

public class RegionTest {

    @Test
    void testParentLoops() {
        // test -> test2 -> test3
        Region region = new Region("test", new CuboidArea());
        Region region2 = new Region("test2", new CuboidArea());
        Region region3 = new Region("test3", new CuboidArea());
        assertDoesNotThrow(() -> region2.addParent(region));
        assertDoesNotThrow(() -> region3.addParent(region2));
        assertThrows(IllegalArgumentException.class, () -> region2.addParent(region2));
        assertThrows(IllegalArgumentException.class, () -> region.addParent(region2));
        assertThrows(IllegalArgumentException.class, () -> region3.addParent(region));
    }

    @Test
    void testInvalidGlobalMethods() {
        GlobalRegion region = new GlobalRegion("test");
        Region other = new Region("other", new CuboidArea());
        assertThrows(IllegalStateException.class, region::parents);
        assertThrows(IllegalStateException.class, () -> region.addParent(other));
        assertThrows(IllegalStateException.class, () -> region.removeParent(other));
        assertThrows(IllegalStateException.class, region::area);
    }
}
