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
package org.empirewar.orbis.sponge.command;

import org.empirewar.orbis.command.CommonCommands;
import org.empirewar.orbis.player.OrbisSession;
import org.empirewar.orbis.sponge.OrbisSponge;
import org.empirewar.orbis.sponge.session.SpongeConsoleSession;
import org.empirewar.orbis.sponge.session.SpongePlayerSession;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.sponge.SpongeCommandManager;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryHolder;

public final class SpongeCommands {

    public SpongeCommands(OrbisSponge plugin, RegistryHolder registryHolder) {
        SpongeCommandManager<OrbisSession> manager = new SpongeCommandManager<>(
                plugin.pluginContainer(),
                ExecutionCoordinator.simpleCoordinator(),
                registryHolder,
                SenderMapper.create(
                        cause -> {
                            if (cause.audience() instanceof ServerPlayer player) {
                                return new SpongePlayerSession(player, cause);
                            }
                            return new SpongeConsoleSession(cause);
                        },
                        session -> {
                            if (session instanceof SpongePlayerSession player) {
                                return player.getCause();
                            }
                            return ((SpongeConsoleSession) session).cause();
                        }));

        new CommonCommands(manager);
    }
}
