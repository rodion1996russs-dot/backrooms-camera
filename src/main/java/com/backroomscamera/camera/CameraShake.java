package com.backroomscamera.camera;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class CameraShake {

    private float trauma = 0f;
    private float traumaGoal = 0f;
    private float noiseY = 0f;
    private float noiseSpeed = 0f;
    private float cameraZRot = 0f;
    private float amplitude = 1.0f;

    // head bob
    private float bobStep = 0f;
    private float bobPitch = 0f;
    private float bobRoll = 0f;

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
        boolean isMoving = playerSpeed > 0.02f;
        boolean isOnGround = player.onGround();

        // --- шум (дыхание) ---
        float noiseSpeedTarget = isMoving ? 0.3f : 0.15f;
        noiseSpeed = lerp(noiseSpeed, noiseSpeedTarget, 0.06f);
        noiseY += noiseSpeed;

        // --- trauma ---
        float traumaTarget;
        if (!isOnGround) {
            traumaTarget = 0.0f;
        } else if (isMoving) {
            traumaTarget = 0.12f;
        } else {
            traumaTarget = 0.20f; // дыхание стоя
        }
        traumaGoal = lerp(traumaGoal, traumaTarget, 0.04f);
        trauma = lerp(trauma, traumaGoal, 0.03f);

        float intensity = trauma * trauma * amplitude;

        float rawPitch = sampleNoise(noiseY) * intensity;
        float rawYaw   = sampleNoise(noiseY * 1.3f + 100f) * intensity;
        float rawRoll  = sampleNoise(noiseY * 0.8f + 200f) * intensity;

        // --- head bob при ходьбе/беге ---
        if (isMoving && isOnGround) {
            float stepSpeed = playerSpeed * 12f;
            bobStep += stepSpeed;
            // pitch: вверх-вниз
            float targetBobPitch = (float) Math.sin(bobStep * 0.35f) * playerSpeed * 5.0f;
            // roll: качание влево-вправо (в такт шагам, половина частоты)
            float targetBobRoll = (float) Math.sin(bobStep * 0.175f) * playerSpeed * 12.0f;

            bobPitch = lerp(bobPitch, targetBobPitch, 0.18f);
            bobRoll  = lerp(bobRoll,  targetBobRoll,  0.18f);
        } else {
            // плавно гасим bob
            bobPitch = lerp(bobPitch, 0f, 0.08f);
            bobRoll  = lerp(bobRoll,  0f, 0.08f);
        }

        pitchOffset = rawPitch * 2.5f + bobPitch;
        yawOffset   = rawYaw  * 2.0f;
        rollOffset  = rawRoll * 1.5f + bobRoll;

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
