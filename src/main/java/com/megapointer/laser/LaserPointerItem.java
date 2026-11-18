package com.megapointer.laser;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import java.util.List;

public final class LaserPointerItem extends Item {
    public LaserPointerItem(Settings settings) { super(settings); }
    public static int selected(ItemStack stack) {
        NbtComponent data=stack.getOrDefault(DataComponentTypes.CUSTOM_DATA,NbtComponent.DEFAULT);
        return LaserModes.clamp(data.copyNbt().getInt("laser_mode"));
    }
    public static void setSelected(ItemStack stack,int index) {
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA,stack,
            nbt->nbt.putInt("laser_mode",LaserModes.clamp(index)));
    }
    @Override public TypedActionResult<ItemStack> use(World world,PlayerEntity user,Hand hand) {
        ItemStack stack=user.getStackInHand(hand);
        if(user.isSneaking()||world.isClient()) return TypedActionResult.success(stack);
        if(!(user instanceof ServerPlayerEntity player)||!(world instanceof ServerWorld serverWorld))
            return TypedActionResult.pass(stack);
        if(!player.isCreative()&&!player.hasPermissionLevel(2)){
            player.sendMessage(Text.literal("§cCreative mode or operator permission required."),true);
            return TypedActionResult.fail(stack);
        }
        if(player.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
        BlockHitResult hit=(BlockHitResult)player.raycast(256.0,1.0f,false);
        if(hit.getType()!=HitResult.Type.BLOCK){
            player.sendMessage(Text.literal("§eAim at a block within 256 blocks."),true);
            return TypedActionResult.fail(stack);
        }
        int mode=selected(stack);
        if(LaserJobs.start(serverWorld,player,hit.getBlockPos(),mode)){
            player.getItemCooldownManager().set(this,30);
            if(LaserCinematics.enabled(mode)){
                ServerPlayNetworking.send(player,new LaserCutscenePayload(
                    mode,hit.getBlockPos(),LaserCinematics.INTRO_TICKS));
            }else LaserVisuals.fireBeam(serverWorld,player.getEyePos(),hit.getPos(),mode);
            player.sendMessage(Text.literal("§bFIRING "+LaserModes.name(mode)),true);
        }else player.sendMessage(Text.literal("§cAnother blast is already running."),true);
        return TypedActionResult.success(stack);
    }
    @Override public void appendTooltip(ItemStack stack,TooltipContext context,List<Text> tooltip,TooltipType type){
        int mode=selected(stack);
        tooltip.add(Text.literal("§bMode "+(mode+1)+"/90: "+LaserModes.name(mode)));
        tooltip.add(Text.literal("§7Cap: "+String.format("%,d",LaserModes.limit(mode))+" blocks"));
        if(LaserCinematics.enabled(mode)) tooltip.add(Text.literal("§d5-second cinematic: "+LaserCinematics.title(mode)));
        tooltip.add(Text.literal("§7Right click: fire; M or Shift+right click: select"));
        tooltip.add(Text.literal("§cBack up your world before using."));
        super.appendTooltip(stack,context,tooltip,type);
    }
}
