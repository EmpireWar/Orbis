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
package org.empirewar.orbis.flag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class RegionFlagTest {

    @Test
    void testAddingFlagAndQuery() {
        Region region = new Region("test", new CuboidArea());
        region.addFlag(DefaultFlags.CAN_BREAK);

        region.setFlag(DefaultFlags.CAN_BREAK, false);
        final Optional<Boolean> result = region.query(RegionQuery.Flag.<Boolean>builder()
                        .flag(DefaultFlags.CAN_BREAK)
                        .build())
                .result();
        assertTrue(result.isPresent());
        assertFalse(result.get());

        region.setFlag(DefaultFlags.CAN_BREAK, true);
        final Optional<Boolean> newResult = region.query(RegionQuery.Flag.<Boolean>builder()
                        .flag(DefaultFlags.CAN_BREAK)
                        .build())
                .result();
        assertTrue(newResult.isPresent());
        assertTrue(newResult.get());
    }

    @Test
    void testRegionParentFlags() {
        Region region = new Region("test", new CuboidArea());
        GlobalRegion region2 = new GlobalRegion("test2");
        region2.addFlag(DefaultFlags.CAN_BREAK);
        region2.setFlag(DefaultFlags.CAN_BREAK, false);
        region.addParent(region2);

        final Optional<Boolean> result =
                region.query(RegionQuery.Flag.builder(DefaultFlags.CAN_BREAK)).result();
        assertTrue(result.isPresent());
        assertFalse(result.get());

        Region region3 = new Region("test3", new CuboidArea());
        region3.priority(100);
        region3.addFlag(DefaultFlags.CAN_BREAK);
        region.addParent(region3);

        final Optional<Boolean> priorityResult =
                region.query(RegionQuery.Flag.builder(DefaultFlags.CAN_BREAK)).result();
        assertTrue(priorityResult.isPresent());
        assertTrue(priorityResult.get());
    }
}
