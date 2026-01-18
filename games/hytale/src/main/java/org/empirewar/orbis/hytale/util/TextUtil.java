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
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class TextUtil {

    public static void send(IMessageReceiver receiver, Component component) {
        receiver.sendMessage(componentToHytaleMessage(component));
    }

    public static void send(CommandContext context, Component component) {
        context.sendMessage(componentToHytaleMessage(component));
    }

    public static Message componentToHytaleMessage(Component component) {
        if (component == null) {
            return Message.empty();
        }

        ConversionResult result = toMessage(component);
        Message message = result.message();
        applyStyle(message, component);

        if (result.includeChildren()) {
            for (Component child : component.children()) {
                message.insert(componentToHytaleMessage(child));
            }
        }

        return message;
    }

    private static ConversionResult toMessage(Component component) {
        if (component instanceof TextComponent text) {
            return new ConversionResult(Message.raw(text.content()), true);
        }

        if (component instanceof TranslatableComponent translatable) {
            String key = translatable.key();
            Message message = Message.translation(key);
            for (Component argument : translatable.args()) {
                message.insert(componentToHytaleMessage(argument));
            }
            return new ConversionResult(message, false);
        }

        String plain = PlainTextComponentSerializer.plainText().serialize(component);
        return new ConversionResult(Message.raw(plain), false);
    }

    private static void applyStyle(Message message, Component component) {
        TextColor color = component.color();
        if (color != null) {
            message.color(color.asHexString());
        }

        if (component.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE) {
            message.bold(true);
        }

        if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.TRUE) {
            message.italic(true);
        }

        if (component.decoration(TextDecoration.UNDERLINED) == TextDecoration.State.TRUE) {
            message.insert(Message.raw(""));
        }

        if (component.decoration(TextDecoration.STRIKETHROUGH) == TextDecoration.State.TRUE) {
            message.insert(Message.raw(""));
        }

        if (component.decoration(TextDecoration.OBFUSCATED) == TextDecoration.State.TRUE) {
            message.insert(Message.raw(""));
        }

        ClickEvent clickEvent = component.clickEvent();
        if (clickEvent != null && clickEvent.action() == ClickEvent.Action.OPEN_URL) {
            message.link(clickEvent.value());
        }
    }

    private record ConversionResult(Message message, boolean includeChildren) {}
}
