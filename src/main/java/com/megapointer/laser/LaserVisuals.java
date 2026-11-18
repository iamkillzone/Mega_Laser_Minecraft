package com.megapointer.laser;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public final class LaserVisuals{
    private LaserVisuals(){}
    private static DustParticleEffect particle(int mode){
        int color=LaserModes.THEME_RGB[LaserModes.theme(mode)];
        return new DustParticleEffect(new Vector3f(
            ((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f),1.7f);
    }
    public static void fireBeam(ServerWorld world,Vec3d start,Vec3d end,int mode){
        Vec3d line=end.subtract(start);
        double length=line.length();
        int steps=(int)Math.min(120,Math.max(4,length*2));
        for(int i=0;i<=steps;i++){
            Vec3d p=start.add(line.multiply(i/(double)steps));
            world.spawnParticles(particle(mode),p.x,p.y,p.z,2,.08,.08,.08,0);
        }
    }
    public static void animate(ServerWorld world,BlockPos pos,int mode,int radius,int tick,int removed,int target){
        if(tick%5!=0)return;
        Vec3d center=Vec3d.ofCenter(pos);
        double phase=tick*.2;
        for(int i=0;i<24;i++){
            double angle=phase+i*Math.PI/12;
            double size=Math.max(3,Math.min(radius,6+tick*.1));
            double x=center.x+size*Math.cos(angle);
            double z=center.z+size*Math.sin(angle);
            world.spawnParticles(particle(mode),x,center.y+Math.sin(angle*2)*3,z,1,.2,.2,.2,.01);
        }
    }
    public static void cinematicCharge(ServerWorld world,BlockPos pos,int mode,int radius,int tick){
        Vec3d c=Vec3d.ofCenter(pos);
        double angle=tick*.15;
        for(int i=0;i<16;i++){
            double a=angle+Math.PI*2*i/16;
            double r=Math.max(2,12-tick*.09);
            double h=(mode==85?tick*.15:Math.sin(a*2)*6);
            world.spawnParticles(particle(mode),c.x+Math.cos(a)*r,c.y+h,c.z+Math.sin(a)*r,2,.1,.1,.1,.03);
        }
        if(tick%20==0)world.spawnParticles(ParticleTypes.END_ROD,c.x,c.y,c.z,24,2,2,2,.15);
    }
    public static void cinematicImpact(ServerWorld world,BlockPos pos,int mode,int radius){
        Vec3d c=Vec3d.ofCenter(pos);
        world.spawnParticles(ParticleTypes.FLASH,c.x,c.y,c.z,1,0,0,0,0);
        world.spawnParticles(ParticleTypes.END_ROD,c.x,c.y,c.z,120,Math.min(14,radius),8,Math.min(14,radius),.15);
    }
    public static void blockBurst(ServerWorld world,BlockPos pos,int mode,int tick){
        if(tick%2!=0)return;
        world.spawnParticles(particle(mode),pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,2,.5,.5,.5,.02);
    }
}
