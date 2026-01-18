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
package org.empirewar.orbis.hytale.logging;

import com.hypixel.hytale.logger.HytaleLogger;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.helpers.MessageFormatter;

public final class HytaleSlf4jLogger extends AbstractLogger {

    private final String name;
    private final HytaleLogger hytale;

    public HytaleSlf4jLogger(String name, HytaleLogger hytale) {
        this.name = name;
        this.hytale = hytale;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return true;
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return "Orbis";
    }

    @Override
    protected void handleNormalizedLoggingCall(
            Level level, Marker marker, String message, Object[] arguments, Throwable throwable) {
        String formatted = MessageFormatter.arrayFormat(message, arguments).getMessage();

        switch (level) {
            case ERROR ->
                hytale.at(java.util.logging.Level.SEVERE).withCause(throwable).log(formatted);
            case WARN -> hytale.at(java.util.logging.Level.WARNING).log(formatted);
            case INFO -> hytale.at(java.util.logging.Level.INFO).log(formatted);
            case DEBUG -> hytale.at(java.util.logging.Level.FINE).log(formatted);
            case TRACE -> hytale.at(java.util.logging.Level.FINER).log(formatted);
        }
    }
}
