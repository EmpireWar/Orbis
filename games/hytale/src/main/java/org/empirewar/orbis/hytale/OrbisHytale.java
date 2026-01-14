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
package org.empirewar.orbis.hytale;

import com.hypixel.hytale.server.core.event.events.PrepareUniverseEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;

import org.jspecify.annotations.NonNull;

import java.io.IOException;

public class OrbisHytale extends JavaPlugin {

    private final OrbisHytalePlatform platform;

    public OrbisHytale(@NonNull JavaPluginInit init) {
        super(init);
        this.platform = new OrbisHytalePlatform(this);

        //        this.getEventRegistry().register(AddWorldEvent.class, event -> {
        //            platform.logger().info("Added new world");
        //        });
    }

    @Override
    protected void setup() {
        this.getEventRegistry().register(PrepareUniverseEvent.class, event -> {
            platform.logger().info("Preparing universe");
        });

        this.getEventRegistry().register(AllWorldsLoadedEvent.class, event -> {
            platform.logger().info("All worlds loaded");
        });

        try {
            platform.loadRegions();
        } catch (IOException e) {
            platform.logger().error("Error loading regions", e);
        }
    }

    @Override
    protected void start() {}
}
