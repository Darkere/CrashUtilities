package com.darkere.crashutils;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

public class CustomFakePlayer extends FakePlayer {


    public CustomFakePlayer(ServerWorld world, GameProfile name) {
        super(world, name);
    }

    @Override
    public Vector3d position() {
        return position;
    }

}
