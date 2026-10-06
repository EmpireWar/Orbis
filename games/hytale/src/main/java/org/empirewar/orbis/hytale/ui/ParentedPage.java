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

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.jetbrains.annotations.Nullable;

/**
 * A page that can navigate back to the page that opened it.
 *
 * <p>Binds {@code #BackButton} to reopen {@code parent}. A page opened at the top level passes a
 * {@code null} parent, in which case the button is hidden and dismissing closes the UI outright.
 *
 * @param <T> the event data type
 */
public abstract class ParentedPage<T extends ParentedPage.ParentedData>
        extends InteractiveCustomUIPage<T> {

    private final @Nullable InteractiveCustomUIPage<?> parent;

    protected ParentedPage(
            PlayerRef playerRef,
            CustomPageLifetime lifetime,
            BuilderCodec<T> eventDataCodec,
            @Nullable InteractiveCustomUIPage<?> parent) {
        super(playerRef, lifetime, eventDataCodec);
        this.parent = parent;
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder ui,
            UIEventBuilder ev,
            Store<EntityStore> store) {
        if (parent == null) {
            ui.set("#BackButton.Visible", false);
            return;
        }

        ui.set("#BackButton.Visible", true);
        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#BackButton",
                EventData.of(ParentedData.BACK, "Back"));
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, T data) {
        if (data.back == null) return;
        openParent(ref, store);
    }

    /**
     * Returns whether the given event was the back button, so subclasses can skip their own
     * handling once navigation has happened.
     *
     * @param data the event data
     * @return true if the back button fired
     */
    protected boolean isBack(T data) {
        return data.back != null;
    }

    private void openParent(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (parent == null) return;

        final Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        player.getPageManager().openCustomPage(ref, store, parent);
    }

    /**
     * Sends a message to the player viewing this page.
     *
     * @param message the message
     */
    protected void sendMessage(Message message) {
        playerRef.sendMessage(message);
    }

    /** Event data carrying the back button binding. */
    public abstract static class ParentedData {

        public static final String BACK = "Back";

        /**
         * Adds the back field to a codec builder. Every subclass page's codec must call this, or
         * its back button will not fire.
         *
         * @param builder the builder
         * @param <D> the data type
         * @return the same builder
         */
        public static <D extends ParentedData> BuilderCodec.Builder<D> addBackField(
                BuilderCodec.Builder<D> builder) {
            return builder.addField(
                    new KeyedCodec<>(BACK, Codec.STRING), (d, v) -> d.back = v, d -> d.back);
        }

        @Nullable String back;
    }
}
