package com.megapointer.laser.client;

import com.megapointer.laser.LaserModes;
import com.megapointer.laser.LaserCinematics;
import com.megapointer.laser.LaserPointerItem;
import com.megapointer.laser.MegaLaser90;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class LaserModeScreen extends Screen{
    private int page;
    public LaserModeScreen(){
        super(Text.literal("MEGA LASER 90"));
        ItemStack held=heldPointer();
        page=held.isEmpty()?0:LaserModes.theme(LaserPointerItem.selected(held));
    }
    private static ItemStack heldPointer(){
        var player=MinecraftClient.getInstance().player;
        if(player==null)return ItemStack.EMPTY;
        if(player.getMainHandStack().isOf(MegaLaser90.LASER_POINTER))return player.getMainHandStack();
        if(player.getOffHandStack().isOf(MegaLaser90.LASER_POINTER))return player.getOffHandStack();
        return ItemStack.EMPTY;
    }
    @Override protected void init(){
        int left=width/2-207,top=height/2-108;
        for(int i=0;i<10;i++){
            final int mode=page*10+i;
            String text="#"+(mode+1)+" "+LaserModes.SHAPES[i]+" ("+
                String.format("%,d",LaserModes.limit(mode))+" blocks)"+
                (LaserCinematics.enabled(mode)?" ★":"");
            addDrawableChild(ButtonWidget.builder(Text.literal(text),b->{
                if(client!=null&&client.player!=null)
                    client.player.networkHandler.sendChatCommand("laserpointer mode "+(mode+1));
                close();
            }).dimensions(left+(i%2)*210,top+34+(i/2)*27,204,23).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("PREVIOUS"),b->{
            page=(page+8)%9;clearAndInit();
        }).dimensions(left,top+177,110,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("NEXT"),b->{
            page=(page+1)%9;clearAndInit();
        }).dimensions(left+304,top+177,110,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("DONE"),b->close())
            .dimensions(width/2-40,top+177,80,20).build());
    }
    @Override public void render(DrawContext ctx,int x,int y,float delta){
        renderBackground(ctx,x,y,delta);
        int left=width/2-207,top=height/2-108;
        ctx.fill(left-9,top-10,left+423,top+222,0xD00A1022);
        ctx.drawCenteredTextWithShadow(textRenderer,"MEGA LASER 90 — "+
            LaserModes.THEMES[page].toUpperCase(),width/2,top,0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
            "Theme "+(page+1)+"/9  •  10 attacks per theme  •  M to open",
            width/2,top+17,0x83E7FF);
        ctx.drawCenteredTextWithShadow(textRenderer,
            page==8?"★ The five strongest attacks feature five-second cutscenes":
                "Select a blast. Back up your world first.",
            width/2,top+207,0xEFC3FF);
        super.render(ctx,x,y,delta);
    }
    @Override public boolean shouldPause(){return false;}
}
