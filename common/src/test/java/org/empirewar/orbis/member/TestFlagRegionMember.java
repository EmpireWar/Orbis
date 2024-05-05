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
package org.empirewar.orbis.member;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

public class TestFlagRegionMember {

    @Test
    void testRegionWithMemberAndGroupedFlag() {
        Region region = new Region("Selma", new CuboidArea());
        region.addGroupedFlag(DefaultFlags.ITEM_FRAME_ITEM_PLACE, Set.of(FlagMemberGroup.MEMBER));
        final UUID fakePlayer = UUID.randomUUID();
        final PlayerMember fakeMember = new PlayerMember(fakePlayer);

        // Not specifying a player should give us a valid result
        assertTrue(region.query(RegionQuery.Flag.builder(DefaultFlags.ITEM_FRAME_ITEM_PLACE))
                .result()
                .isPresent());

        // Specify the player to check the group
        assertTrue(region.query(RegionQuery.Flag.builder(DefaultFlags.ITEM_FRAME_ITEM_PLACE)
                        .player(fakePlayer))
                .result()
                .isEmpty());

        region.addMember(fakeMember);
        assertTrue(region.query(RegionQuery.Flag.builder(DefaultFlags.ITEM_FRAME_ITEM_PLACE)
                        .player(fakePlayer))
                .result()
                .isPresent());
    }
}
