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
package org.empirewar.orbis.hytale.command;

import static net.kyori.adventure.text.Component.text;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.hytale.util.TextUtil;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.util.OrbisText;
import org.empirewar.orbis.world.RegionisedWorld;
import org.jspecify.annotations.NonNull;

public class OrbisCommand extends AbstractCommandCollection {

    public OrbisCommand() {
        super("orbis", "The base Orbis command.");
        this.addSubCommand(new OrbisWhereCommand());
    }

    private static class OrbisWhereCommand extends AbstractPlayerCommand {

        public OrbisWhereCommand() {
            super("where", "Tells you what regions you are in.");
        }

        @Override
        protected void execute(
                @NonNull CommandContext commandContext,
                @NonNull Store<EntityStore> store,
                @NonNull Ref<EntityStore> ref,
                @NonNull PlayerRef playerRef,
                @NonNull World world) {
            final Orbis orbis = OrbisAPI.get();
            final Key playerWorld = orbis.getPlayerWorld(playerRef.getUuid());
            final RegionisedWorld regionisedWorld = orbis.getRegionisedWorld(playerWorld);
            playerRef.sendMessage(TextUtil.componentToHytaleMessage(OrbisText.PREFIX.append(text(
                    "You are in world "
                            + regionisedWorld.worldId().orElseThrow().asString() + ".",
                    OrbisText.SECONDARY_ORANGE))));

            TransformComponent transform =
                    ref.getStore().getComponent(ref, TransformComponent.getComponentType());
            Vector3d position = transform.getPosition();

            //            BuilderToolsPlugin.get().getBuilderState(null, null).getSelection().
            for (Region region : regionisedWorld
                    .query(RegionQuery.Position.at(position.x, position.y, position.z)
                            .build())
                    .result()) {
                playerRef.sendMessage(TextUtil.componentToHytaleMessage(OrbisText.PREFIX.append(
                        text("You are in region " + region.name() + ".", OrbisText.EREBOR_GREEN))));
            }
        }
    }
}
