/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2026 Empire War
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
package org.empirewar.orbis.hytale.ui;

/** Keys and values used in {@link com.hypixel.hytale.server.core.ui.builder.EventData} payloads. */
public final class UIActions {

    // Payload keys.
    public static final String BUTTON = "Button";
    public static final String REGION = "Region";
    public static final String FLAG = "Flag";
    public static final String PARENT = "Parent";
    public static final String MEMBER = "Member";
    public static final String PRIORITY = "Priority";
    public static final String VALUE = "Value";

    // Button values.
    public static final String OPEN_FLAGS = "Flags";
    public static final String ADD_FLAG = "AddFlag";
    public static final String TOGGLE_FLAG = "ToggleFlag";
    public static final String MODIFY_FLAG = "ModifyFlag";
    public static final String REMOVE_FLAG = "RemoveFlag";
    public static final String ADD_PARENT = "AddParent";
    public static final String REMOVE_PARENT = "RemoveParent";
    public static final String ADD_MEMBER = "AddMember";
    public static final String REMOVE_MEMBER = "RemoveMember";
    public static final String SET_PRIORITY = "SetPriority";

    /**
     * Prefixes a payload key to bind it to an element's live value rather than a fixed string, so
     * the value is read at click time.
     *
     * @param key the payload key
     * @return the live-bound form of the key
     */
    public static String live(String key) {
        return "@" + key;
    }

    private UIActions() {}
}
