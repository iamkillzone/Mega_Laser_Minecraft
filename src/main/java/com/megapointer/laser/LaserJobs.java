package com.megapointer.laser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class LaserJobs {
    private static final List<Job> JOBS=new ArrayList<>();
    private LaserJobs(){}
    public static void clear(){JOBS.clear();}
    public static boolean start(ServerWorld world,ServerPlayerEntity player,BlockPos origin,int mode){
        if(JOBS.size()>=4||JOBS.stream().anyMatch(j->j.owner.equals(player.getUuid()))) return false;
        JOBS.add(new Job(world,player.getUuid(),origin.toImmutable(),LaserModes.clamp(mode),player.getEyePos()));
        return true;
    }
    public static void tick(MinecraftServer server){
        Iterator<Job> it=JOBS.iterator();
        while(it.hasNext()){
            Job job=it.next();
            ServerPlayerEntity owner=server.getPlayerManager().getPlayer(job.owner);
            if(owner==null||owner.getServerWorld()!=job.world){it.remove();continue;}
            job.tick(owner,Math.max(1,384/JOBS.size()));
            if(job.done){
                owner.sendMessage(Text.literal("§a"+LaserModes.name(job.mode)+" completed: "+
                    String.format("%,d",job.removed)+" blocks destroyed."),false);
                it.remove();
            }
        }
    }
    private static final class Job{
        final ServerWorld world;final UUID owner;final BlockPos origin;
        final int mode,radius,target;final Vec3d eye;
        int x,y,z,removed,elapsed; boolean done;
        Job(ServerWorld w,UUID o,BlockPos p,int m,Vec3d eye){
            world=w;owner=o;origin=p;mode=m;this.eye=eye;
            target=LaserModes.limit(m);radius=LaserModes.radius(m);
            x=y=z=-radius;
        }
        void advance(){
            if(++x>radius){x=-radius;if(++z>radius){z=-radius;if(++y>radius)done=true;}}
        }
        void tick(ServerPlayerEntity player,int allowance){
            elapsed++;
            if(LaserCinematics.enabled(mode)&&elapsed<=LaserCinematics.INTRO_TICKS){
                LaserVisuals.cinematicCharge(world,origin,mode,radius,elapsed);
                if(elapsed==LaserCinematics.INTRO_TICKS){
                    LaserVisuals.fireBeam(world,eye,Vec3d.ofCenter(origin),mode);
                    LaserVisuals.cinematicImpact(world,origin,mode,radius);
                }
                return;
            }
            LaserVisuals.animate(world,origin,mode,radius,elapsed,removed,target);
            int checked=0,changed=0;
            while(!done&&removed<target&&checked<6500&&changed<allowance){
                int bx=origin.getX()+x,by=origin.getY()+y,bz=origin.getZ()+z;
                double nx=(double)x/radius,ny=(double)y/radius,nz=(double)z/radius;
                advance(); checked++;
                if(!LaserModes.inside(LaserModes.shape(mode),nx,ny,nz))continue;
                if(by<world.getBottomY()||by>=world.getTopY())continue;
                if(!world.isChunkLoaded(bx>>4,bz>>4))continue;
                BlockPos pos=new BlockPos(bx,by,bz);
                if(!world.canPlayerModifyAt(player,pos))continue;
                BlockState state=world.getBlockState(pos);
                if(state.isAir()||state.getHardness(world,pos)<0)continue;
                if(world.setBlockState(pos,Blocks.AIR.getDefaultState(),Block.NOTIFY_LISTENERS)){
                    removed++;changed++;
                    if(removed%20==0)LaserVisuals.blockBurst(world,pos,mode,elapsed);
                }
            }
            if(removed>=target)done=true;
        }
    }
}
