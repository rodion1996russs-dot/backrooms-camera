package com.backroomscamera.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.player.Player;

public class CameraShake {

    private float trauma = 0f;
    private float traumaGoal = 0f;
    private float noiseY = 0f;
    private float noiseSpeed = 0f;
    private float noiseSpeedGoal = 0.4f;
    private float amplitude = 1.0f;
    private float cameraZRot = 0f;

    public float pitchOffset = 0f;
    public float yawOffset = 0f;
    public float rollOffset = 0f;
    public float frameDelta = 0f;

    public void tick(Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Player player = mc.player;

        double vx = player.getDeltaMovement().x;
        double vz = player.getDeltaMovement().z;
        float playerSpeed = (float) Math.sqrt(vx * vx + vz * vz);
        boolean isMoving = playerSpeed > 0.01f;

        float noiseSpeedTarget = isMoving ? 0.55f : 0.25f;
        noiseSpeedGoal = lerp(noiseSpeedGoal, noiseSpeedTarget, 0.05f);
        noiseSpeed = lerp(noiseSpeed, noiseSpeedGoal, 0.08f);

        float traumaTarget;
        if (!player.onGround()) {
            traumaTarget = 0.0f;
        } else if (isMoving) {
            traumaTarget = 0.28f;
        } else {
            traumaTarget = 0.10f;
        }

        traumaGoal = lerp(traumaGoal, traumaTarget, 0.06f);
        trauma = lerp(trauma, traumaGoal, 0.04f);

        noiseY += noiseSpeed;

        float intensity = trauma * trauma * amplitude;

        float rawPitch = sampleNoise(noiseY * 1.0f) * intensity;
        float rawYaw = sampleNoise(noiseY * 1.3f + 100f) * intensity;
        float rawRoll = sampleNoise(noiseY * 0.8f + 200f) * intensity;

        pitchOffset = rawPitch * 2.5f;
        yawOffset = rawYaw * 2.5f;
        rollOffset = rawRoll * 1.5f;

        cameraZRot = rollOffset;
    }

    public float getCameraZRot() {
        return cameraZRot;
    }

    public float getShakeIntensity() {
        return trauma * trauma * amplitude;
    }

    private static float lerp(float src, float dst, float smooth) {
        return src + (dst - src) * smooth;
    }

    private static float sampleNoise(float t) {
        return (float)(
            0.50 * Math.sin(t * 1.000f) +
            0.30 * Math.sin(t * 2.317f + 1.3f) +
            0.20 * Math.sin(t * 5.731f + 2.7f)
        );
    }
}
