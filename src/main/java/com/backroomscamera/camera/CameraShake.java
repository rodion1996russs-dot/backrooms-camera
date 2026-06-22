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
    private float bobProgress = 0f;
    private float bobVelocity = 0f;

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

        // --- скорость шума ---
        float noiseSpeedTarget = isMoving ? 0.45f : 0.18f;
        noiseSpeed = lerp(noiseSpeed, noiseSpeedTarget, 0.07f);
        noiseY += noiseSpeed;

        // --- trauma (дыхание/тряска) ---
        float traumaTarget;
        if (!isOnGround) {
            traumaTarget = 0.0f;
        } else if (isMoving) {
            traumaTarget = 0.18f;
        } else {
            traumaTarget = 0.22f; // дыхание стоя сильнее
        }

        traumaGoal = lerp(traumaGoal, traumaTarget, 0.05f);
        trauma = lerp(trauma, traumaGoal, 0.03f);

        float intensity = trauma * trauma * amplitude;

        float rawPitch = sampleNoise(noiseY * 1.0f) * intensity;
        float rawYaw   = sampleNoise(noiseY * 1.3f + 100f) * intensity;
        float rawRoll  = sampleNoise(noiseY * 0.8f + 200f) * intensity;

        // --- head bob при ходьбе ---
        float bobTarget = 0f;
        if (isMoving && isOnGround) {
            float speed = Math.min(playerSpeed * 8f, 1.0f);
            bobProgress += speed;
            bobTarget = (float) Math.sin(bobProgress * 0.4f) * speed * 0.6f;
        } else {
            bobProgress *= 0.85f; // плавно гасим
        }
        bobVelocity = lerp(bobVelocity, bobTarget, 0.15f);

        pitchOffset = rawPitch * 3.0f + bobVelocity;
        yawOffset   = rawYaw  * 2.0f;
        rollOffset  = rawRoll * 1.5f + bobVelocity * 0.3f;

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
