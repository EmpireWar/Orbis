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
