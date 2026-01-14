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
package org.empirewar.orbis.minecraft;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;

import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.minecraft.command.caption.OrbisCaptionProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class OrbisMinecraftPlatform extends OrbisPlatform {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MiniMessage miniMessage() {
        return miniMessage;
    }

    @Override
    protected void load() {
        super.load();

        MiniMessageTranslationStore store =
                MiniMessageTranslationStore.create(Key.key("orbis", "translations"));
        store.defaultLocale(Locale.UK);

        final File translationsDirectory = dataFolder().resolve("translations").toFile();
        translationsDirectory.mkdirs();

        Locale.availableLocales().forEach(locale -> {
            String resourcePath =
                    String.format("assets/orbis/translations_%s.properties", locale.toString());
            if (OrbisPlatform.class.getClassLoader().getResource(resourcePath) != null) {
                try {
                    // Copy bundled translation file to the translations directory
                    final Path target = translationsDirectory
                            .toPath()
                            .resolve(String.format("%s.properties", locale));
                    try (InputStream is = getResourceAsStream("/" + resourcePath)) {
                        try {
                            Files.copy(is, target);
                        } catch (FileAlreadyExistsException ignored) {
                            compareTranslations(locale, resourcePath, target);
                        }
                    }

                    // Register the translation bundle from the copied/existing target
                    store.registerAll(locale, target, true);
                    logger().info("Loaded translations for {}", locale);
                } catch (Exception e) {
                    logger().warn("Failed to copy translations for {}", locale, e);
                }
            }
        });

        GlobalTranslator.translator().addSource(store);
        OrbisCaptionProvider.registerTranslations();
    }

    private void compareTranslations(Locale locale, String resourcePath, Path target) {
        // Compare keys between existing file and bundled resource, warn on diffs
        try (InputStream existingIn = Files.newInputStream(target);
                InputStream jarIn = getResourceAsStream("/" + resourcePath)) {
            if (jarIn != null) {
                final Properties existingProps = new Properties();
                final Properties jarProps = new Properties();
                existingProps.load(existingIn);
                jarProps.load(jarIn);

                final Set<String> existingKeys = existingProps.stringPropertyNames();
                final Set<String> jarKeys = jarProps.stringPropertyNames();

                final Set<String> missingKeys = jarKeys.stream()
                        .filter(k -> !existingKeys.contains(k))
                        .collect(Collectors.toSet());
                final Set<String> extraKeys = existingKeys.stream()
                        .filter(k -> !jarKeys.contains(k))
                        .collect(Collectors.toSet());

                if (!missingKeys.isEmpty()) {
                    logger().warn("Translations for {} are missing keys: {}", locale, missingKeys);
                }

                if (!extraKeys.isEmpty()) {
                    logger().warn(
                                    "Translations for {} contain unknown/removed keys: {}",
                                    locale,
                                    extraKeys);
                }

                if (!missingKeys.isEmpty() || !extraKeys.isEmpty()) {
                    logger().warn("Consider regenerating the translation file for {}", locale);
                }
            }
        } catch (IOException e) {
            logger().warn("Failed to compare translation keys for {}", locale, e);
        }
    }
}
