package com.megapointer.laser.client;

import com.megapointer.laser.LaserCinematics;
import com.megapointer.laser.LaserCutscenePayload;
import com.megapointer.laser.LaserModes;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/** Five distinct five-second cutscenes. ESC/SPACE exits only the camera, not the blast. */
public final class LaserCinematicScreen extends Screen {
    private final int mode, duration;
    private final Vec3d target;
    private ClientWorld originalWorld;
    private Entity originalCamera;
    private ArmorStandEntity camera;
    private int age;
    private boolean restored;

    public LaserCinematicScreen(LaserCutscenePayload payload) {
        super(Text.literal(LaserCinematics.title(payload.mode())));
        mode=LaserModes.clamp(payload.mode());
        duration=Math.max(20,Math.min(100,payload.duration()));
        target=Vec3d.ofCenter(payload.target());
    }

    @Override protected void init(){
        if(client==null||client.player==null||client.world==null||camera!=null)return;
        originalWorld=client.world;
        originalCamera=client.getCameraEntity();
        camera=new ArmorStandEntity(client.world,target.x,target.y,target.z);
        camera.setInvisible(true);
        camera.setNoGravity(true);
        move(0);
        client.setCameraEntity(camera);
    }

    @Override public void tick(){
        if(client==null||client.world==null||client.player==null||
            client.world!=originalWorld||camera==null){close();return;}
        age++;
        if(age>=duration){close();return;}
        move(age/(double)duration);
    }

    private void move(double t){
        if(camera==null)return;
        double a=2*Math.PI*t;
        Vec3d p;
        switch(mode-85){
            case 0 -> p=target.add(16*Math.cos(3*a),25-32*t,16*Math.sin(3*a));
            case 1 -> p=target.add(26*Math.cos(a),10+14*Math.sin(2*a),26*Math.sin(a));
            case 2 -> p=target.add(20*(1-.6*t)*Math.cos(a),42-40*t,20*(1-.6*t)*Math.sin(a));
            case 3 -> p=target.add(18*Math.cos(2*a),10+12*Math.sin(a),18*Math.sin(2*a));
            default -> {
                double jump=Math.floor(t*4);
                double shake=4*t*t;
                p=target.add(28*Math.cos(jump*Math.PI/2)+Math.sin(t*110)*shake,
                    13+Math.cos(jump*2)*11,28*Math.sin(jump*Math.PI/2)+Math.cos(t*135)*shake);
            }
        }
        camera.setPosition(p.x,p.y,p.z);
        Vec3d direction=target.subtract(p);
        double horizontal=Math.sqrt(direction.x*direction.x+direction.z*direction.z);
        float yaw=(float)(Math.toDegrees(Math.atan2(direction.z,direction.x))-90);
        float pitch=(float)(-Math.toDegrees(Math.atan2(direction.y,horizontal)));
        camera.setYaw(yaw);
        camera.setPitch(pitch);
    }

    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta){
        // Transparent center allows the Minecraft world camera to remain visible.
        ctx.fill(0,0,width,Math.min(42,height/6),0xF5000000);
        ctx.fill(0,height-Math.min(49,height/6),width,height,0xF5000000);
        int top=Math.min(42,height/6);
        int color=0xFF000000|LaserModes.THEME_RGB[LaserModes.theme(mode)];
        ctx.drawCenteredTextWithShadow(textRenderer,LaserCinematics.title(mode),
            width/2,12,0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,LaserCinematics.subtitle(mode),
            width/2,height-41,0xFFFFFF);
        int bar=Math.max(20,width/2),left=(width-bar)/2;
        ctx.fill(left,height-24,left+bar,height-17,0xFF252030);
        ctx.fill(left,height-24,left+(int)(bar*age/(double)duration),height-17,color);
        ctx.drawCenteredTextWithShadow(textRenderer,"SPACE / ESC to skip camera",
            width/2,height-12,0xCCCCCC);
    }

    @Override public boolean shouldPause(){return false;}
    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){
        if(keyCode==GLFW.GLFW_KEY_SPACE||keyCode==GLFW.GLFW_KEY_ESCAPE){close();return true;}
        return super.keyPressed(keyCode,scanCode,modifiers);
    }
    @Override public void close(){
        if(!restored){
            restored=true;
            if(client!=null&&client.world==originalWorld&&originalCamera!=null)
                client.setCameraEntity(originalCamera);
        }
        if(client!=null&&client.currentScreen==this)client.setScreen(null);
    }
}
