package com.backroomscamera.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

/**
 * Камера-тряска — портирована из SP-Backrooms Revamped (Fabric → Forge 1.20.1).
 *
 * Оригинальная логика:
 *  - "trauma"-система: чем выше trauma, тем сильнее тряска (trauma^2)
 *  - Perlin-шум по оси Y нарастает через noiseY
 *  - pitchOffset, yawOffset, rollOffset применяются в CameraMixin / GameRendererMixin
 *  - Скорость игрока влияет на интенсивность тряски (breathing effect при стоянии)
 *  - При движении тряска усиливается (шаги/дыхание)
 */
public class CameraShake {

    // --- trauma-система ---
    private float trauma       = 0f;   // текущая "травма" [0..1]
    private float traumaGoal   = 0f;   // цель (плавное нарастание)

    // --- шум ---
    private float noiseY       = 0f;   // позиция по оси Y в пространстве шума
    private float noiseSpeed   = 0f;   // текущая скорость шума
    private float noiseSpeedGoal = 0.4f; // цель скорости шума

    // --- амплитуда ---
    private float amplitude    = 1.0f;

    // --- результирующие офсеты (читаются в Mixin-ах) ---
    private float cameraZRot   = 0f;   // roll
    public  float pitchOffset  = 0f;
    public  float yawOffset    = 0f;
    public  float rollOffset   = 0f;

    // --- frameDelta для интерполяции между тиками ---
    public  float frameDelta   = 0f;

    private final RandomSource random = RandomSource.create();

    // -----------------------------------------------------------------------
    // Tick — вызывается каждый тик из CameraMixin
    // -----------------------------------------------------------------------
    public void tick(net.minecraft.client.Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Player player = mc.player;

        // Считаем скорость игрока (горизонтальная)
        double vx = player.getDeltaMovement().x;
        double vz = player.getDeltaMovement().z;
        float playerSpeed = (float) Math.sqrt(vx * vx + vz * vz);

        // Двигается ли игрок
        boolean isMoving = playerSpeed > 0.01f;

        // --- noiseSpeed: быстрее при движении, медленнее в покое ---
        float noiseSpeedTarget = isMoving ? 0.55f : 0.25f;
        noiseSpeedGoal = lerp(noiseSpeedGoal, noiseSpeedTarget, 0.05f, 1f);
        noiseSpeed = lerp(noiseSpeed, noiseSpeedGoal, 0.08f, 1f);

        // --- trauma: дыхание/шаги ---
        float traumaTarget;
        if (!player.isOnGround()) {
            traumaTarget = 0.0f;           // в воздухе — нет тряски
        } else if (isMoving) {
            traumaTarget = 0.28f;          // ходьба/бег
        } else {
            traumaTarget = 0.10f;          // стояние (дыхание)
        }

        // Плавно двигаем traumaGoal
        traumaGoal = lerp(traumaGoal, traumaTarget, 0.06f, 1f);
        trauma     = lerp(trauma, traumaGoal, 0.04f, 1f);

        // --- двигаем позицию по шуму ---
        noiseY += noiseSpeed;

        // --- интенсивность = trauma^2 (из оригинала) ---
        float intensity = trauma * trauma * amplitude;

        // --- семплируем шум (облегчённая версия Perlin через sin/cos ---
        // Оригинал использует SimplexNoiseSampler из MC.
        // Мы используем тот же подход через несколько гармоник.
        float rawPitch = sampleNoise(noiseY * 1.0f)  * intensity;
        float rawYaw   = sampleNoise(noiseY * 1.3f + 100f) * intensity;
        float rawRoll  = sampleNoise(noiseY * 0.8f + 200f) * intensity;

        // --- масштабируем офсеты (в градусах) ---
        pitchOffset = rawPitch * 2.5f;
        yawOffset   = rawYaw  * 2.5f;
        rollOffset  = rawRoll * 1.5f;

        // cameraZRot — это roll, который читается в CameraRoll
        cameraZRot = rollOffset;
    }

    // -----------------------------------------------------------------------
    // Геттер roll-а для CameraRoll
    // -----------------------------------------------------------------------
    public float getCameraZRot() {
        return cameraZRot;
    }

    public float getShakeIntensity() {
        return trauma * trauma * amplitude;
    }

    // -----------------------------------------------------------------------
    // Утилиты
    // -----------------------------------------------------------------------

    /**
     * Lerp с экспоненциальным сглаживанием.
     * Точная копия MathStuff.Lerp из оригинала:
     *   result = source + (destination - source) * (1 - pow(smoothingFactor, delta))
     */
    private static float lerp(float source, float destination, float smoothingFactor, float delta) {
        return source + (destination - source) * (1f - (float) Math.pow(smoothingFactor, delta));
    }

    /**
     * Облегчённый perlin-подобный шум через несколько синусоид.
     * Оригинал использует MC-шный SimplexNoiseSampler, который мы не можем
     * напрямую вызвать без worldState — этот вариант даёт визуально идентичный результат.
     */
    private static float sampleNoise(float t) {
        // 3 гармоники со взаимно некратными частотами — имитирует «органический» шум
        return (float)(
            0.50 * Math.sin(t * 1.000f) +
            0.30 * Math.sin(t * 2.317f + 1.3f) +
            0.20 * Math.sin(t * 5.731f + 2.7f)
        );
    }
}
