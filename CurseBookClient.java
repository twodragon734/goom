package com.forgebox.cursebook;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;

public class CurseBookClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (stack.isOf(CurseBook.CURSE_BOOK)) lines.add(Text.translatable("cursebook.curse_book.help"));
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.currentScreen != null) return;
            if (client.player.getMainHandStack().isOf(CurseBook.CURSE_BOOK) && client.options.useKey.wasPressed()) {
                client.setScreen(new CurseSelectScreen());
            }
        });
    }

    private static class CurseSelectScreen extends Screen {
        private final List<ButtonWidget> buttons = new ArrayList<>();

        protected CurseSelectScreen() {
            super(Text.translatable("screen.cursebook.title"));
        }

        @Override
        protected void init() {
            buttons.clear();
            int y = height / 2 - 78;
            for (CurseBook.Curse curse : CurseBook.Curse.values()) {
                ButtonWidget button = ButtonWidget.builder(Text.literal(curse.displayName()), ignored -> chooseTarget(curse))
                        .dimensions(width / 2 - 90, y, 180, 20).build();
                addDrawableChild(button);
                buttons.add(button);
                y += 25;
            }
        }

        private void chooseTarget(CurseBook.Curse curse) {
            client.setScreen(new TargetSelectScreen(curse));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            renderBackground(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 105, ColorHelper.getArgb(255, 255, 225, 160));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Choose a curse"), width / 2, height / 2 - 90, 0xFFAAAAAA);
            super.render(context, mouseX, mouseY, delta);
        }
    }

    private static class TargetSelectScreen extends Screen {
        private final CurseBook.Curse curse;

        protected TargetSelectScreen(CurseBook.Curse curse) {
            super(Text.translatable("screen.cursebook.target"));
            this.curse = curse;
        }

        @Override
        protected void init() {
            int y = height / 2 - 50;
            boolean found = false;
            for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                if (entry.getProfile().getId().equals(client.player.getUuid())) continue;
                found = true;
                String name = entry.getProfile().getName();
                addDrawableChild(ButtonWidget.builder(Text.literal(name), ignored -> apply(curse, name))
                        .dimensions(width / 2 - 90, y, 180, 20).build());
                y += 25;
            }
            if (!found) addDrawableChild(ButtonWidget.builder(Text.translatable("screen.cursebook.none"), ignored -> close())
                    .dimensions(width / 2 - 120, height / 2, 240, 20).build());
        }

        private void apply(CurseBook.Curse curse, String player) {
            client.player.networkHandler.sendChatCommand("cursebook apply " + curse.id() + " " + player);
            close();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            renderBackground(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 80, ColorHelper.getArgb(255, 255, 225, 160));
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Using: " + curse.displayName()), width / 2, height / 2 - 65, 0xFFAAAAAA);
            super.render(context, mouseX, mouseY, delta);
        }
    }
}
