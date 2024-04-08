/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 EmpireWar
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
package org.empirewar.orbis.paper.command;

import org.bukkit.command.CommandSender;
import org.empirewar.orbis.command.RegionCommand;
import org.empirewar.orbis.paper.OrbisPaper;
import org.empirewar.orbis.player.ConsoleOrbisSession;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;

public class Commands {

    public Commands(OrbisPaper plugin) {
        PaperCommandManager<ConsoleOrbisSession> commandManager = new PaperCommandManager<>(plugin, /* 1 */
                ExecutionCoordinator.asyncCoordinator(), /* 2 */
                SenderMapper.create(ConsoleOrbisSession::new,
                        session -> (CommandSender) session.getAudience()) /* 3 */);

        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            commandManager.registerBrigadier();
        } else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            commandManager.registerAsynchronousCompletions();
        }

        AnnotationParser<ConsoleOrbisSession> annotationParser = new AnnotationParser<>(commandManager,
                ConsoleOrbisSession.class);
        annotationParser.parse(new RegionCommand());
    }
}
