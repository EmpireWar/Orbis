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
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.hytale.util.Translations;
import org.empirewar.orbis.member.Member;
import org.empirewar.orbis.member.PermissionMember;
import org.empirewar.orbis.member.PlayerMember;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3ic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Shows a region's priority, parents, members and area, and links through to its flags. */
public final class RegionInfoPage extends ParentedPage<RegionInfoPage.RegionInfoData> {

    private static final String MUTED = "#8A90B2";
    private static final String GOLD = "#93844c";

    private final String regionName;

    public RegionInfoPage(PlayerRef player, CustomPageLifetime lifetime, Region region) {
        this(player, lifetime, region, null);
    }

    public RegionInfoPage(
            PlayerRef player,
            CustomPageLifetime lifetime,
            Region region,
            @Nullable InteractiveCustomUIPage<?> parent) {
        super(player, lifetime, RegionInfoData.CODEC, parent);
        this.regionName = region.name();
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder ui,
            UIEventBuilder ev,
            Store<EntityStore> store) {
        ui.append("Pages/Orbis_RegionInfo.ui");
        super.build(ref, ui, ev, store);

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ManageFlags",
                EventData.of(UIActions.BUTTON, UIActions.OPEN_FLAGS)
                        .append(UIActions.REGION, regionName));

        final Region region = resolveRegion();
        if (region == null) {
            renderMissing(ui);
            return;
        }

        ui.set("#RegionNameLabel.Text", region.name());

        renderPriority(ui, ev, region);
        renderParents(ui, ev, region);
        renderMembers(ui, ev, region);
        renderArea(ui, region);
    }

    @Override
    public void handleDataEvent(
            Ref<EntityStore> ref, Store<EntityStore> store, RegionInfoData data) {
        super.handleDataEvent(ref, store, data);
        if (isBack(data) || data.button == null) return;

        switch (data.button) {
            case UIActions.OPEN_FLAGS -> {
                openFlags(store, ref);
                return;
            }
            case UIActions.ADD_PARENT -> addParent(data);
            case UIActions.REMOVE_PARENT -> removeParent(data);
            case UIActions.ADD_MEMBER, UIActions.REMOVE_MEMBER -> {
                // TODO: member editing needs a uuid -> name lookup before it is usable.
                sendMessage(Translations.message("orbis.error.notEditable"));
                return;
            }
            case UIActions.SET_PRIORITY -> setPriority(data);
            default -> {
                return;
            }
        }

        rebuild();
    }

    private void openFlags(Store<EntityStore> store, Ref<EntityStore> ref) {
        final Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        final Region region = resolveRegion();
        if (region == null) return;

        player.getPageManager()
                .openCustomPage(
                        ref,
                        store,
                        new RegionFlagsPage(
                                playerRef, CustomPageLifetime.CanDismiss, region, this));
    }

    private @Nullable Region resolveRegion() {
        return OrbisRegistries.REGIONS.get(regionName).orElse(null);
    }

    private void renderMissing(UICommandBuilder ui) {
        ui.clear("#ParentCards");
        ui.clear("#MemberCards");
        ui.clear("#AreaDetails");

        ui.set("#RegionNameLabel.Text", translate("orbis.region.info.notFound"));

        ui.appendInline(
                "#AreaDetails",
                "Label { Text: %ui.orbis.region.info.notFound;"
                        + " Style: (FontSize: 18, TextColor: #FF6B6B); }");
    }

    private void renderPriority(UICommandBuilder ui, UIEventBuilder ev, Region region) {
        ui.set("#PriorityValue.Value", region.priority());

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AdjustPriority",
                EventData.of(UIActions.BUTTON, UIActions.SET_PRIORITY)
                        .append(UIActions.REGION, regionName)
                        .append(UIActions.live(UIActions.PRIORITY), "#PriorityValue.Value"));
    }

    private void renderParents(UICommandBuilder ui, UIEventBuilder ev, Region region) {
        ui.clear("#ParentCards");

        if (region.isGlobal()) {
            appendMuted(ui, "#ParentCards", "orbis.region.info.globalNoParents");
            return;
        }

        ui.append("#ParentCards", "Entries/Orbis_ParentAddRow.ui");

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ParentCards[0] #AddParent",
                EventData.of(UIActions.BUTTON, UIActions.ADD_PARENT)
                        .append(UIActions.REGION, regionName)
                        .append(
                                UIActions.live(UIActions.PARENT),
                                "#ParentCards[0] #ParentSelector.Value"));

        final List<WrappedDropdownEntryInfo> parentChoices = buildParentChoices(region);

        final boolean any = !parentChoices.isEmpty();
        ui.set("#ParentCards[0] #ParentSelector.Visible", any);
        ui.set("#ParentCards[0] #AddParent.Visible", any);

        if (!any) {
            ui.set("#ParentCards[0] #ParentSelector.Value", "");
        } else {
            ui.set(
                    "#ParentCards[0] #ParentSelector.Entries",
                    parentChoices.stream()
                            .map(WrappedDropdownEntryInfo::toEntry)
                            .toList());
            ui.set(
                    "#ParentCards[0] #ParentSelector.Value",
                    parentChoices.getFirst().value());
        }

        if (region.parents().isEmpty()) {
            appendMuted(ui, "#ParentCards", "orbis.region.info.noParents");
            return;
        }

        int index = 1;
        for (Region parent : region.parents()) {
            ui.append("#ParentCards", "Entries/Orbis_ParentEntry.ui");
            final String entryBase = "#ParentCards[" + index + "]";
            ui.set(entryBase + " #ParentName.Text", parent.name());

            final EventData remove = EventData.of(UIActions.BUTTON, UIActions.REMOVE_PARENT)
                    .append(UIActions.REGION, regionName)
                    .append(UIActions.PARENT, parent.name());

            ev.addEventBinding(
                    CustomUIEventBindingType.Activating, entryBase + " #RemoveParent", remove);
            ev.addEventBinding(CustomUIEventBindingType.RightClicking, entryBase, remove);

            index++;
        }
    }

    private void renderMembers(UICommandBuilder ui, UIEventBuilder ev, Region region) {
        ui.clear("#MemberCards");

        ui.append("#MemberCards", "Entries/Orbis_MemberAddRow.ui");

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#MemberCards[0] #AddMember",
                EventData.of(UIActions.BUTTON, UIActions.ADD_MEMBER)
                        .append(UIActions.REGION, regionName));

        if (region.members().isEmpty()) {
            appendMuted(ui, "#MemberCards", "orbis.region.info.noMembers");
            return;
        }

        int index = 1;
        for (Member member : region.members()) {
            final String label = memberDisplay(member);

            ui.append("#MemberCards", "Entries/Orbis_MemberEntry.ui");
            final String entryBase = "#MemberCards[" + index + "]";
            ui.set(entryBase + " #MemberName.Text", label);
            ui.set(entryBase + " #MemberType.Text", memberType(member));
            ui.set(entryBase + ".TooltipTextSpans", Message.raw(label));

            final EventData remove = EventData.of(UIActions.BUTTON, UIActions.REMOVE_MEMBER)
                    .append(UIActions.REGION, regionName)
                    .append(UIActions.MEMBER, label);

            ev.addEventBinding(
                    CustomUIEventBindingType.Activating, entryBase + " #RemoveMember", remove);

            index++;
        }
    }

    private List<WrappedDropdownEntryInfo> buildParentChoices(Region region) {
        final List<WrappedDropdownEntryInfo> entries = new ArrayList<>();

        for (Region candidate : OrbisRegistries.REGIONS.getAll()) {
            if (candidate.equals(region)) continue;
            if (region.parents().contains(candidate)) continue;
            if (!candidate.isGlobal() && candidate.parents().contains(region)) continue;
            final boolean loopViaExisting = region.parents().stream()
                    .anyMatch(existing ->
                            !existing.isGlobal() && existing.parents().contains(candidate));
            if (loopViaExisting) continue;

            entries.add(new WrappedDropdownEntryInfo(candidate.name(), candidate.name()));
        }

        entries.sort(Comparator.comparing(entry -> entry.label().toLowerCase()));
        return entries;
    }

    private record WrappedDropdownEntryInfo(String label, String value) {

        public DropdownEntryInfo toEntry() {
            return new DropdownEntryInfo(LocalizableString.fromString(label), value);
        }
    }

    private void addParent(RegionInfoData data) {
        if (data.parent == null || data.parent.isBlank()) return;

        final Region region = resolveRegion();
        if (region == null) {
            sendMessage(Translations.message("orbis.error.regionGone"));
            return;
        }

        final Region parent = OrbisRegistries.REGIONS.get(data.parent).orElse(null);
        if (parent == null) {
            sendMessage(Translations.message("orbis.error.parentGone"));
            return;
        }

        try {
            region.addParent(parent);
        } catch (IllegalArgumentException e) {
            // Selection became invalid between render and submission.
            sendMessage(Translations.message("orbis.error.parentInvalid"));
        }
    }

    private void removeParent(RegionInfoData data) {
        if (data.parent == null || data.parent.isBlank()) return;

        final Region region = resolveRegion();
        if (region == null) {
            sendMessage(Translations.message("orbis.error.regionGone"));
            return;
        }

        final Region parent = OrbisRegistries.REGIONS.get(data.parent).orElse(null);
        if (parent == null) {
            sendMessage(Translations.message("orbis.error.parentGone"));
            return;
        }

        region.removeParent(parent);
    }

    private void setPriority(RegionInfoData data) {
        if (data.priority < 0) {
            sendMessage(Translations.message("orbis.error.priorityInvalid"));
            return;
        }

        final Region region = resolveRegion();
        if (region == null) {
            sendMessage(Translations.message("orbis.error.regionGone"));
            return;
        }

        region.priority(data.priority);
    }

    private String memberDisplay(Member member) {
        if (member instanceof PlayerMember p) {
            return p.playerId().toString();
        }
        if (member instanceof PermissionMember p) {
            return p.permission();
        }
        return member.toString();
    }

    private String memberType(Member member) {
        if (member instanceof PlayerMember) {
            return "Player";
        }
        if (member instanceof PermissionMember) {
            return "Permission";
        }
        return member.getClass().getSimpleName();
    }

    private void renderArea(UICommandBuilder ui, Region region) {
        ui.clear("#AreaDetails");

        if (region.isGlobal()) {
            appendMuted(ui, "#AreaDetails", "orbis.region.info.globalArea");
            return;
        }

        final Area area = region.area();
        final Vector3ic min = area.getMin();
        final Vector3ic max = area.getMax();

        appendDetail(
                ui,
                translate("orbis.region.info.bounds"),
                "(%d, %d, %d) - (%d, %d, %d)"
                        .formatted(min.x(), min.y(), min.z(), max.x(), max.y(), max.z()));
        appendDetail(
                ui, translate("orbis.region.info.volume"), "%,d".formatted(Iterables.size(area)));
        appendDetail(
                ui,
                translate("orbis.region.info.points"),
                String.valueOf(area.points().size()));
    }

    private void appendDetail(UICommandBuilder ui, String label, String value) {
        ui.appendInline("#AreaDetails", """
                Group {
                  LayoutMode: Left;
                  Anchor: (Bottom: 2);

                  Label { Text: "%s"; Style: (FontSize: 15, RenderBold: true, TextColor: %s); }
                  Label { Text: " %s"; Style: (FontSize: 15); }
                }
                """.formatted(escape(label), GOLD, escape(value)));
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void appendMuted(UICommandBuilder ui, String selector, String key) {
        ui.appendInline(
                selector,
                "Label { Text: %ui." + key + "; Style: (FontSize: 15, TextColor: " + MUTED
                        + "); }");
    }

    private String translate(String key) {
        return Translations.ui(key, playerRef);
    }

    /** Event payload for this page. */
    public static final class RegionInfoData extends ParentedPage.ParentedData {

        public static final BuilderCodec<RegionInfoData> CODEC = ParentedData.addBackField(
                        BuilderCodec.builder(RegionInfoData.class, RegionInfoData::new))
                .addField(
                        new KeyedCodec<>(UIActions.BUTTON, Codec.STRING),
                        (d, v) -> d.button = v,
                        d -> d.button)
                .addField(
                        new KeyedCodec<>(UIActions.PARENT, Codec.STRING),
                        (d, v) -> d.parent = v,
                        d -> d.parent)
                .addField(
                        new KeyedCodec<>(UIActions.live(UIActions.PARENT), Codec.STRING),
                        (d, v) -> d.parent = v,
                        d -> d.parent)
                .addField(
                        new KeyedCodec<>(UIActions.PRIORITY, Codec.INTEGER),
                        (d, v) -> d.priority = v,
                        d -> d.priority)
                .addField(
                        new KeyedCodec<>(UIActions.live(UIActions.PRIORITY), Codec.INTEGER),
                        (d, v) -> d.priority = v,
                        d -> d.priority)
                .build();

        @Nullable String button;

        @Nullable String parent;

        int priority;
    }
}
