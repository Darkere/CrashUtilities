package com.darkere.crashutils;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

public class CustomFakePlayer extends FakePlayer {


    public CustomFakePlayer(ServerLevel world, GameProfile name) {
        super(world, name);
    }

    @Override
    public Vec3 position() {
        return position;
    }

}
