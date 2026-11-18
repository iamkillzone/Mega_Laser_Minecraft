package com.megapointer.laser;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class MegaLaser90 implements ModInitializer {
    public static final String MOD_ID = "megalaser90";
    public static final Item LASER_POINTER = Registry.register(
        Registries.ITEM, Identifier.of(MOD_ID, "laser_pointer"),
        new LaserPointerItem(new Item.Settings().maxCount(1)));

    @Override public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(LaserCutscenePayload.ID, LaserCutscenePayload.CODEC);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(e -> e.add(LASER_POINTER));
        ServerTickEvents.END_SERVER_TICK.register(LaserJobs::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> LaserJobs.clear());
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
            dispatcher.register(CommandManager.literal("laserpointer")
                .then(CommandManager.literal("mode")
                .then(CommandManager.argument("number", IntegerArgumentType.integer(1,90))
                .executes(ctx -> {
                    ServerPlayerEntity player=ctx.getSource().getPlayerOrThrow();
                    ItemStack stack=player.getMainHandStack().isOf(LASER_POINTER)
                        ? player.getMainHandStack() : player.getOffHandStack();
                    if (!stack.isOf(LASER_POINTER)) {
                        ctx.getSource().sendError(Text.literal("Hold the Mega Laser Pointer."));
                        return 0;
                    }
                    int mode=IntegerArgumentType.getInteger(ctx,"number")-1;
                    LaserPointerItem.setSelected(stack,mode);
                    player.sendMessage(Text.literal("§bMode "+(mode+1)+": §f"+LaserModes.name(mode)
                        +" — "+LaserModes.limit(mode)+" blocks max"),false);
                    return 1;
                })))));
    }
}
