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
package org.empirewar.orbis.member;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.minecraft.flags.MinecraftFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

public class TestFlagRegionMember {

    @Test
    void testRegionWithMemberAndGroupedFlag() {
        Region region = new Region("Selma", new CuboidArea());
        region.addGroupedFlag(MinecraftFlags.ITEM_FRAME_ITEM_PLACE, Set.of(FlagMemberGroup.MEMBER));
        final UUID fakePlayer = UUID.randomUUID();
        final PlayerMember fakeMember = new PlayerMember(fakePlayer);

        // Not specifying a player should give us a valid result
        assertTrue(region.query(RegionQuery.Flag.builder(MinecraftFlags.ITEM_FRAME_ITEM_PLACE))
                .result()
                .isPresent());

        // Specify the player to check the group
        assertTrue(region.query(RegionQuery.Flag.builder(MinecraftFlags.ITEM_FRAME_ITEM_PLACE)
                        .player(fakePlayer))
                .result()
                .isEmpty());

        region.addMember(fakeMember);
        assertTrue(region.query(RegionQuery.Flag.builder(MinecraftFlags.ITEM_FRAME_ITEM_PLACE)
                        .player(fakePlayer))
                .result()
                .isPresent());
    }
}
