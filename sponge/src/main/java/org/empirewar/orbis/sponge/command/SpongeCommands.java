/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 Empire War
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
