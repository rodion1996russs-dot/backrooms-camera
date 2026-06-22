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
