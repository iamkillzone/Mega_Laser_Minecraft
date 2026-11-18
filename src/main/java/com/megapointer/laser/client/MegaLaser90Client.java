package com.megapointer.laser.client;

import com.megapointer.laser.MegaLaser90;
import com.megapointer.laser.LaserCinematics;
import com.megapointer.laser.LaserCutscenePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;
import org.lwjgl.glfw.GLFW;

public final class MegaLaser90Client implements ClientModInitializer{
    @Override public void onInitializeClient(){
        ClientPlayNetworking.registerGlobalReceiver(LaserCutscenePayload.ID,(payload,context)->
            context.client().execute(()->{
                MinecraftClient client=context.client();
                if(LaserCinematics.enabled(payload.mode())&&client.player!=null
                    &&client.world!=null&&client.currentScreen==null)
                    client.setScreen(new LaserCinematicScreen(payload));
            }));
        KeyBinding key=KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.megalaser90.selector",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_M,"key.categories.megalaser90"));
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            while(key.wasPressed()){
                if(client.player!=null&&client.currentScreen==null&&
                    (client.player.getMainHandStack().isOf(MegaLaser90.LASER_POINTER)
                    ||client.player.getOffHandStack().isOf(MegaLaser90.LASER_POINTER)))
                    client.setScreen(new LaserModeScreen());
            }
        });
        UseItemCallback.EVENT.register((player,world,hand)->{
            ItemStack stack=player.getStackInHand(hand);
            if(world.isClient()&&player.isSneaking()&&stack.isOf(MegaLaser90.LASER_POINTER)){
                MinecraftClient.getInstance().setScreen(new LaserModeScreen());
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        });
    }
}
