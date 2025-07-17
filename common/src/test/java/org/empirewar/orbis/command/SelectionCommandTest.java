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
