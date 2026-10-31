package com.forgebox.cursebook;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CurseBook implements ModInitializer {
    public static final String MOD_ID = "cursebook";
    public static final Item CURSE_BOOK = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "curse_book"),
            new CurseBookItem(new Item.Settings().maxCount(1))
    );

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(CURSE_BOOK));
        registerCommands();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                CommandManager.literal("cursebook")
                        .then(CommandManager.literal("apply")
                                .then(CommandManager.argument("curse", StringArgumentType.word())
                                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                                .executes(context -> applyCurse(
                                                        context.getSource().getPlayer(),
                                                        StringArgumentType.getString(context, "curse"),
                                                        EntityArgumentType.getPlayer(context, "target"))))))
                        .then(CommandManager.literal("cleanse")
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .executes(context -> cleanse(EntityArgumentType.getPlayer(context, "target")))))
        ));
    }

    private static int applyCurse(ServerPlayerEntity caster, String curse, ServerPlayerEntity target) {
        Curse curseType = Curse.fromId(curse);
        if (curseType == null) {
            caster.sendMessage(Text.literal("Unknown curse. Try weakness, darkness, mining, hunger, or slowness."), false);
            return 0;
        }
        target.addStatusEffect(new StatusEffectInstance(curseType.effect(), curseType.duration(), curseType.amplifier(), false, true, true));
        target.sendMessage(Text.literal("You have been afflicted with " + curseType.displayName() + "!"), false);
        if (caster != target) caster.sendMessage(Text.literal("Afflicted " + target.getName().getString() + " with " + curseType.displayName() + "."), false);
        return 1;
    }

    private static int cleanse(ServerPlayerEntity target) {
        for (Curse curse : Curse.values()) target.removeStatusEffect(curse.effect());
        target.sendMessage(Text.literal("The curse book has cleansed you."), false);
        return 1;
    }

    public enum Curse {
        WEAKNESS("weakness", "Weakness", StatusEffects.WEAKNESS, 20 * 60 * 5, 1),
        DARKNESS("darkness", "Darkness", StatusEffects.DARKNESS, 20 * 30, 0),
        MINING("mining", "Mining Fatigue", StatusEffects.MINING_FATIGUE, 20 * 60 * 3, 1),
        HUNGER("hunger", "Hunger", StatusEffects.HUNGER, 20 * 60 * 3, 1),
        SLOWNESS("slowness", "Slowness", StatusEffects.SLOWNESS, 20 * 60 * 2, 1);

        private final String id;
        private final String displayName;
        private final net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect;
        private final int duration;
        private final int amplifier;

        Curse(String id, String displayName, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect, int duration, int amplifier) {
            this.id = id;
            this.displayName = displayName;
            this.effect = effect;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        public static Curse fromId(String id) {
            for (Curse curse : values()) if (curse.id.equals(id)) return curse;
            return null;
        }

        public String id() { return id; }
        public String displayName() { return displayName; }
        public net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect() { return effect; }
        public int duration() { return duration; }
        public int amplifier() { return amplifier; }
    }
}
