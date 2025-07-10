/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2025 Empire War
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
package org.empirewar.orbis.command;

import static org.junit.jupiter.api.Assertions.*;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.TestOrbisPlatform;
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.session.TestOrbisPlayerSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SelectionCommandTest {
    private TestOrbisPlatform platform;
    private SelectionCommand cmd;

    @BeforeAll
    void setupPlatform() {
        platform = new TestOrbisPlatform();
        cmd = new SelectionCommand();
    }

    @AfterAll
    static void cleanup() {
        OrbisAPI.reset();
    }

    @Test
    void testOnSelectTypeCreatesSelection() {
        TestOrbisPlayerSession session = new TestOrbisPlayerSession(UUID.randomUUID());
        AreaType<?> type = AreaType.CUBOID;
        cmd.onSelectType(session, type);
        Selection sel = OrbisAPI.get().selectionManager().get(session.getUuid()).orElse(null);
        assertNotNull(sel);
        assertEquals(type, sel.getSelectionType());
    }

    @Test
    void testOnSelectTypeUpdatesSelectionType() {
        TestOrbisPlayerSession session = new TestOrbisPlayerSession(UUID.randomUUID());
        cmd.onSelectType(session, AreaType.CUBOID);
        cmd.onSelectType(session, AreaType.SPHERE);
        Selection sel = OrbisAPI.get().selectionManager().get(session.getUuid()).orElse(null);
        assertNotNull(sel);
        assertEquals(AreaType.SPHERE, sel.getSelectionType());
    }

    @Test
    void testOnClearRemovesSelection() {
        TestOrbisPlayerSession session = new TestOrbisPlayerSession(UUID.randomUUID());
        cmd.onSelectType(session, AreaType.CUBOID);
        assertTrue(OrbisAPI.get().selectionManager().get(session.getUuid()).isPresent());
        cmd.onClear(session);
        assertFalse(OrbisAPI.get().selectionManager().get(session.getUuid()).isPresent());
    }

    @Test
    void testOnClearWithNoSelectionDoesNotThrow() {
        TestOrbisPlayerSession session = new TestOrbisPlayerSession(UUID.randomUUID());
        assertDoesNotThrow(() -> cmd.onClear(session));
        assertFalse(OrbisAPI.get().selectionManager().get(session.getUuid()).isPresent());
    }
}
