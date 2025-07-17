/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2025 Empire War
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
package org.empirewar.orbis.command;

import static org.junit.jupiter.api.Assertions.*;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.TestOrbisPlatform;
import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.flag.value.FlagValue;
import org.empirewar.orbis.member.PermissionMember;
import org.empirewar.orbis.region.GlobalRegion;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.session.TestOrbisConsoleSession;
import org.empirewar.orbis.session.TestOrbisPlayerSession;
import org.empirewar.orbis.world.RegionisedWorld;
import org.joml.Vector3i;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegionCommandTest {

    private TestOrbisPlatform platform;
    private RegionCommand cmd;

    @BeforeAll
    void setupPlatform() {
        platform = new TestOrbisPlatform();
        cmd = new RegionCommand();
    }

    @AfterAll
    static void cleanup() {
        OrbisAPI.reset();
    }

    @Test
    void testOnAddPosWithNormalRegion() {
        TestOrbisPlayerSession session = new TestOrbisPlayerSession(UUID.randomUUID());
        Region region = new Region("test", new CuboidArea());

        // Should add the point
        cmd.onAddPos(session, region, 1, 2, 3);
        assertTrue(region.area().points().contains(new Vector3i(1, 2, 3)));
    }

    @Test
    void testOnAddPosWithGlobalRegion() {
        TestOrbisConsoleSession session = new TestOrbisConsoleSession();
        GlobalRegion region = new GlobalRegion("global");

        // Should NOT throw from the command itself
        assertDoesNotThrow(() -> cmd.onAddPos(session, region, 1, 2, 3));
        // But accessing area() should throw
        assertThrows(IllegalStateException.class, region::area);
    }

    @Test
    void testOnCreateNormalRegion() {
        TestOrbisPlayerSession session = new TestOrbisPlayerSession(UUID.randomUUID());
        String regionName = "region1";
        cmd.onCreate(session, false, true, regionName, null);
        Region region = OrbisRegistries.REGIONS.get(regionName).orElse(null);
        assertNotNull(region);
        assertFalse(region.isGlobal());
    }

    @Test
    void testOnCreateGlobalRegion() {
        TestOrbisConsoleSession session = new TestOrbisConsoleSession();
        String regionName = "global1";
        cmd.onCreate(session, true, true, regionName, null);
        Region region = OrbisRegistries.REGIONS.get(regionName).orElse(null);
        assertNotNull(region);
        assertTrue(region.isGlobal());

        TestOrbisPlayerSession playerSession = new TestOrbisPlayerSession(UUID.randomUUID());
        String playerRegionName = "playerglobalregion";
        cmd.onCreate(playerSession, true, true, playerRegionName, null);
        Region playerRegion = OrbisRegistries.REGIONS.get(playerRegionName).orElse(null);
        assertNotNull(playerRegion);
        assertTrue(playerRegion.isGlobal());

        // Global region should not exist in the player's regionised world
        final RegionisedWorld playerRegionisedWorld =
                platform.getRegionisedWorld(platform.getPlayerWorld(playerSession.getUuid()));
        assertFalse(playerRegionisedWorld.getByName(playerRegionName).isPresent());
    }

    @Test
    void testOnAddParent() {
        TestOrbisPlayerSession session = new TestOrbisPlayerSession(UUID.randomUUID());
        Region child = new Region("child", new CuboidArea());
        Region parent = new Region("parent", new CuboidArea());
        assertDoesNotThrow(() -> cmd.onAddParent(session, child, parent));
        assertTrue(child.parents().contains(parent));
    }

    @Test
    void testOnRemove() {
        TestOrbisPlayerSession session = new TestOrbisPlayerSession(UUID.randomUUID());
        final RegionisedWorld playerRegionisedWorld =
                platform.getRegionisedWorld(platform.getPlayerWorld(session.getUuid()));

        String regionName = "toremove";
        cmd.onCreate(session, false, true, regionName, null);
        Region region = OrbisRegistries.REGIONS.get(regionName).orElse(null);
        assertNotNull(region);
        assertNotNull(playerRegionisedWorld.getByName(regionName).orElse(null));
        cmd.onRemove(session, region);
        assertFalse(OrbisRegistries.REGIONS.get(regionName).isPresent());
        assertFalse(playerRegionisedWorld.getByName(regionName).isPresent());
    }

    @Test
    void testOnAddPermissionAndRemovePermission() {
        TestOrbisPlayerSession session = new TestOrbisPlayerSession(UUID.randomUUID());
        Region region = new Region("permregion", new CuboidArea());
        String perm = "foo.bar";
        cmd.onAddPermission(session, region, perm);
        assertTrue(region.members().stream()
                .anyMatch(m -> m instanceof PermissionMember
                        && ((PermissionMember) m).permission().equals(perm)));
        cmd.onRemovePermission(session, region, perm);
        assertTrue(region.members().stream()
                .noneMatch(m -> m instanceof PermissionMember
                        && ((PermissionMember) m).permission().equals(perm)));
    }

    @Test
    void testOnFlagAddAndSet() {
        TestOrbisPlayerSession session = new TestOrbisPlayerSession(UUID.randomUUID());
        Region region = new Region("flagregion", new CuboidArea());
        RegistryRegionFlag<Boolean> flag = DefaultFlags.CAN_BREAK;
        cmd.onFlagAdd(session, null, region, flag, null);
        assertTrue(region.hasFlag(flag));
        cmd.onFlagSet(session, null, region, flag, new FlagValue<>(true));
        assertTrue(region.hasFlag(flag));
    }

    @Test
    void testOnListPoints() {
        TestOrbisConsoleSession session = new TestOrbisConsoleSession();
        Region region = new Region("pointregion", new CuboidArea());
        region.area().addPoint(new Vector3i(1, 2, 3));
        assertDoesNotThrow(() -> cmd.onListPoints(session, region));
    }
}
