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
