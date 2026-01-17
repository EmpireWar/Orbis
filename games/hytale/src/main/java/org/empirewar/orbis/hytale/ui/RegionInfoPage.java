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

import com.google.common.collect.Iterables;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.member.Member;
import org.empirewar.orbis.member.PermissionMember;
import org.empirewar.orbis.member.PlayerMember;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.joml.Vector3ic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.OptionalInt;

/**
 * Interactive UI page that displays detailed information about a region.
 */
public class RegionInfoPage extends InteractiveCustomUIPage<RegionInfoPage.RegionInfoData> {

    private final String regionName;

    public RegionInfoPage(PlayerRef playerRef, CustomPageLifetime lifetime, Region region) {
        super(playerRef, lifetime, RegionInfoData.CODEC);
        this.regionName = region.key();
    }

    @Override
    public void handleDataEvent(
            @NonNull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            RegionInfoPage.@NonNull RegionInfoData data) {
        if (data.button == null) {
            return;
        }

        switch (data.button) {
            case "Close" -> this.close();

            case "AddParent" -> {
                // your logic
            }

            case "RemoveParent" -> {
                // your logic
            }

            case "Flags" -> {
                final Player player = store.getComponent(ref, Player.getComponentType());
                final Region region = resolveRegion();
                player.getPageManager()
                        .openCustomPage(
                                ref,
                                store,
                                new RegionFlagsPage(
                                        playerRef, CustomPageLifetime.CanDismiss, region));
            }
        }

        this.sendUpdate();
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder ui,
            UIEventBuilder ev,
            Store<EntityStore> store) {
        ui.append("Pages/Orbis_RegionInfo.ui");
        ui.set("#TitleLabel.Text", regionName);

        // Close + FlagMgr buttons
        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseButton",
                EventData.of("Button", "Close"));
        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#FlagManagerButton",
                EventData.of("Button", "Flags").append("Region", regionName));

        Region region = resolveRegion();
        if (region == null) {
            renderMissingUI(ui);
            return;
        }

        renderPriority(ui, ev, region);
        renderParents(ui, ev, region);
        renderArea(ui, ev, region);
        renderMembers(ui, ev, region);
    }

    private void renderPriority(UICommandBuilder ui, UIEventBuilder ev, Region region) {
        ui.clear("#PriorityActions");

        ui.appendInline("#PriorityActions", """
            Button #SetPriority {
              Background: #2D5D91;
              Anchor: (Width: 120, Height: 30);
              Style: ButtonStyle();
              Label {Text:"Set priority"; Style:(FontSize:14,RenderBold:true,TextColor:#FFFFFF);}
            }
        """);

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#PriorityActions #SetPriority",
                EventData.of("Button", "SetPriority").append("Region", regionName));
    }

    private void renderParents(UICommandBuilder ui, UIEventBuilder ev, Region region) {
        ui.clear("#ParentsHeader");
        ui.clear("#ParentsList");

        // Add parent button
        ui.appendInline("#ParentsHeader", """
            Button #AddParent {
              Background: #2D5D91; Anchor:(Width:160,Height:30); Style:ButtonStyle();
              Label {Text:"Add parent"; Style:(FontSize:14,RenderBold:true,TextColor:#FFFFFF);}
            }
        """);
        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ParentsHeader #AddParent",
                EventData.of("Button", "AddParent").append("Region", regionName));

        var parents = region.parents();
        if (parents.isEmpty()) {
            ui.appendInline(
                    "#ParentsList",
                    "Label {Text:\"No parents assigned.\"; Style:(FontSize:15,TextColor:#8A90B2);} ");
            return;
        }

        int idx = 0;
        for (Region parent : parents) {
            String itemUI = String.format("""
                Group {
                  LayoutMode: Left;
                  Label { FlexWeight:1; Text:"%s"; Style:(FontSize:16,TextColor:#FFFFFF); }
                  Button #Remove {
                    Background:#AA3E3E; Anchor:(Width:110,Height:28); Style:ButtonStyle();
                    Label {Text:"Remove"; Style:(FontSize:14,RenderBold:true,TextColor:#FFFFFF);}
                  }
                }
            """, parent.name());

            ui.appendInline("#ParentsList", itemUI);
            ev.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    "#ParentsList[" + idx + "] #Remove",
                    EventData.of("Button", "RemoveParent")
                            .append("Region", regionName)
                            .append("Parent", parent.name()));
            idx++;
        }
    }

    private void renderArea(UICommandBuilder ui, UIEventBuilder ev, Region region) {
        ui.clear("#AreaDetails");
        ui.clear("#AreaActions");

        if (region.isGlobal()) {
            ui.appendInline(
                    "#AreaDetails",
                    "Label {Text:\"Global - no area.\"; Style:(FontSize:16,TextColor:#8A90B2);} ");
            return;
        }

        Area area = region.area();
        Vector3ic min = area.getMin(), max = area.getMax();
        long volume = Iterables.size(area);
        int points = area.points().size();

        ui.appendInline(
                "#AreaDetails",
                String.format(
                        """
                            Label {Text:"Type: %s"; Style:(FontSize:16,TextColor:#FFFFFF); }
                            Label {Text:"Bounds: (%d,%d,%d) - (%d,%d,%d)"; Style:(FontSize:16,TextColor:#FFFFFF); }
                            Label {Text:"Volume: %,d blocks"; Style:(FontSize:16,TextColor:#FFFFFF); }
                            Label {Text:"Points: %d"; Style:(FontSize:16,TextColor:#FFFFFF); }
                        """,
                        OrbisRegistries.AREA_TYPE
                                .getKey(area.getType())
                                .orElseThrow()
                                .asString(),
                        min.x(),
                        min.y(),
                        min.z(),
                        max.x(),
                        max.y(),
                        max.z(),
                        volume,
                        points));
    }

    private void renderMembers(UICommandBuilder ui, UIEventBuilder ev, Region region) {
        ui.clear("#MembersHeader");
        ui.clear("#MembersList");

        // Add member button
        ui.appendInline("#MembersHeader", """
            Button #AddMember {
              Background: #2D5D91;
              Anchor: (Width: 160, Height: 30);
              Style: ButtonStyle();
              Label {
                Text: "Add member";
                Style: (FontSize: 14, RenderBold: true, TextColor: #FFFFFF);
              }
            }
        """);

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#MembersHeader #AddMember",
                EventData.of("Button", "AddMember").append("Region", regionName));

        var members = region.members();
        if (members.isEmpty()) {
            ui.appendInline(
                    "#MembersList",
                    "Label {Text:\"No members.\"; Style:(FontSize:15,TextColor:#8A90B2);}");
            return;
        }

        int index = 0;
        for (Member member : members) {
            String inline = String.format(
                    """
                        Group {
                          LayoutMode: Left;
                          Padding: (Top: 4, Bottom: 4);

                          Label {
                            FlexWeight: 1;
                            Text: "%s";
                            Style: (FontSize: 16, TextColor: #FFFFFF);
                          }

                          Button #RemoveMember {
                            Background: #AA3E3E;
                            Anchor: (Width: 110, Height: 28);
                            Style: ButtonStyle();
                            Label {
                              Text: "Remove";
                              Style: (FontSize: 14, RenderBold: true, TextColor: #FFFFFF);
                            }
                          }
                        }
                    """,
                    OrbisRegistries.MEMBER_TYPE
                            .getKey(member.getType())
                            .orElseThrow()
                            .asString());

            ui.appendInline("#MembersList", inline);

            String value = "";
            if (member instanceof PermissionMember permissionMember) {
                value = permissionMember.permission();
            } else if (member instanceof PlayerMember playerMember) {
                value = playerMember.playerId().toString();
            }

            ev.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    "#MembersList[" + index + "] #RemoveMember",
                    EventData.of("Button", "RemoveMember")
                            .append("Region", regionName)
                            .append("Member", value));

            index++;
        }
    }

    private void renderMissingUI(UICommandBuilder ui) {
        ui.clear("#PriorityActions");
        ui.clear("#ParentsHeader");
        ui.clear("#ParentsList");
        ui.clear("#AreaDetails");
        ui.clear("#AreaActions");
        ui.clear("#MembersHeader");
        ui.clear("#MembersList");

        ui.appendInline(
                "#PriorityActions",
                "Label {Text:\"Region not found.\"; Style:(FontSize:16,TextColor:#FF6B6B);}");
    }

    private Region resolveRegion() {
        return OrbisRegistries.REGIONS.get(regionName).orElse(null);
    }

    public static final class RegionInfoData {

        // ---- Common keys ----
        public static final String KEY_BUTTON = "Button";
        public static final String KEY_REGION = "Region";

        // ---- Parent management ----
        public static final String KEY_PARENT = "Parent";

        // ---- Member management ----
        public static final String KEY_MEMBER = "Member";

        // ---- Teleport / area ----
        public static final String KEY_X = "X";
        public static final String KEY_Y = "Y";
        public static final String KEY_Z = "Z";

        // ---- Codec ----
        public static final BuilderCodec<RegionInfoData> CODEC = BuilderCodec.builder(
                        RegionInfoData.class, RegionInfoData::new)
                .addField(
                        new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
                        (d, v) -> d.button = v,
                        d -> d.button)
                .addField(
                        new KeyedCodec<>(KEY_REGION, Codec.STRING),
                        (d, v) -> d.region = v,
                        d -> d.region)
                .addField(
                        new KeyedCodec<>(KEY_PARENT, Codec.STRING),
                        (d, v) -> d.parent = v,
                        d -> d.parent)
                .addField(
                        new KeyedCodec<>(KEY_MEMBER, Codec.STRING),
                        (d, v) -> d.member = v,
                        d -> d.member)
                .addField(new KeyedCodec<>(KEY_X, Codec.STRING), (d, v) -> d.x = v, d -> d.x)
                .addField(new KeyedCodec<>(KEY_Y, Codec.STRING), (d, v) -> d.y = v, d -> d.y)
                .addField(new KeyedCodec<>(KEY_Z, Codec.STRING), (d, v) -> d.z = v, d -> d.z)
                .build();

        // ---- Decoded values ----
        private @Nullable String button;
        private @Nullable String region;
        private @Nullable String parent;
        private @Nullable String member;
        private @Nullable String x;
        private @Nullable String y;
        private @Nullable String z;

        // ---- Convenience helpers ----

        public @Nullable String button() {
            return button;
        }

        public @Nullable String region() {
            return region;
        }

        public @Nullable String parent() {
            return parent;
        }

        public @Nullable String member() {
            return member;
        }

        public OptionalInt x() {
            return parseInt(x);
        }

        public OptionalInt y() {
            return parseInt(y);
        }

        public OptionalInt z() {
            return parseInt(z);
        }

        private static OptionalInt parseInt(@Nullable String value) {
            try {
                return value == null
                        ? OptionalInt.empty()
                        : OptionalInt.of(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                return OptionalInt.empty();
            }
        }
    }
}
