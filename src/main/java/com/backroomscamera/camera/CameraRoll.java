package com.backroomscamera.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class CameraRoll {

    private float prevYaw = 0f;
    private float rotAmount = 0f;
    private float spinRoll = 0f;
    private float strafeRoll = 0f;

    private static final float LOOK_ROLL_MULTIPLIER = 0.08f;
    private static final float STRAFE_ROLL_MULTIPLIER = 10.0f;

    public float doCameraRoll(Player player, float tickDelta) {
        float currentYaw = player.yRotO + (player.getYRot() - player.yRotO) * tickDelta;

        float deltaYaw = currentYaw - prevYaw;
        while (deltaYaw > 180f) deltaYaw -= 360f;
        while (deltaYaw < -180f) deltaYaw += 360f;

        prevYaw = currentYaw;

        rotAmount += deltaYaw * LOOK_ROLL_MULTIPLIER;
        spinRoll = lerp(spinRoll, rotAmount, 0.20f);
        rotAmount = lerp(rotAmount, 0f, 0.10f);

        float strafeComponent = get2DRelativeRotation(player, tickDelta);
        strafeRoll = lerp(strafeRoll, strafeComponent * STRAFE_ROLL_MULTIPLIER, 0.15f);

        CameraShake shake = ClientCameraState.getCameraShake();
        float shakeRoll = (shake != null) ? shake.getCameraZRot() : 0f;

        return spinRoll + strafeRoll + shakeRoll;
    }

    private float get2DRelativeRotation(Player player, float tickDelta) {
        Vec3 vel = player.getDeltaMovement();
        if (vel.lengthSqr() < 0.0001) return 0f;

        float yawRad = (float) Math.toRadians(
            player.yRotO + (player.getYRot() - player.yRotO) * tickDelta
        );

        float rightX = (float) Math.cos(yawRad);
        float rightZ = (float) Math.sin(yawRad);

        return (float)(vel.x * rightX + vel.z * rightZ);
    }

    private static float lerp(float src, float dst, float smooth) {
        return src + (dst - src) * smooth;
    }
}
