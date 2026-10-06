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
package org.empirewar.orbis.hytale.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;

/**
 * Resolves {@code Server/Languages/<locale>/ui.lang} keys.
 *
 * <p>Most UI text is referenced directly from the {@code .ui} markup with {@code %ui.orbis.*},
 * which the client resolves itself. This helper covers the cases markup cannot express - text
 * pushed into an element at runtime via {@link
 * com.hypixel.hytale.server.core.ui.builder.UICommandBuilder#set}, which takes a plain string.
 */
public final class Translations {

    private static final String UI_PREFIX = "ui.";
    private static final String FALLBACK_LANGUAGE = "en-US";

    /**
     * Resolves a {@code ui.lang} key for the given player, falling back to en-US and finally to
     * the key itself.
     *
     * @param key the key, without the {@code ui.} prefix (e.g. {@code orbis.region.info.title})
     * @param playerRef the player whose language should be used
     * @return the translated text
     */
    public static String ui(String key, PlayerRef playerRef) {
        final String qualified = UI_PREFIX + key;

        final String language = playerRef.getLanguage();
        if (language != null && !language.isBlank()) {
            final String translated = lookup(language, qualified);
            if (translated != null) return translated;
        }

        final String fallback = lookup(FALLBACK_LANGUAGE, qualified);
        return fallback != null ? fallback : qualified;
    }

    /**
     * Creates a client-side translated {@link Message} for a {@code ui.lang} key. Prefer this over
     * {@link #ui(String, PlayerRef)} wherever a {@link Message} is accepted, as it lets the client
     * translate.
     *
     * @param key the key, without the {@code ui.} prefix
     * @return the message
     */
    public static Message message(String key) {
        return Message.translation(UI_PREFIX + key);
    }

    private static String lookup(String language, String qualified) {
        final String translated = I18nModule.get().getMessage(language, qualified);
        if (translated == null || translated.isBlank() || translated.equals(qualified)) {
            return null;
        }
        return translated;
    }

    private Translations() {}
}
