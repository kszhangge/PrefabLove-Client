package dev.diona.southside.module.modules.combat;

import cc.polyfrost.oneconfig.config.options.*;
import dev.diona.southside.event.events.StrafeEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.module.modules.client.Target;
import dev.diona.southside.util.player.Rotation;
import dev.diona.southside.util.player.RotationUtil;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class AimAssist extends Module {
    public final Slider rangeValue = new Slider("Range", 4, 1, 8, 0.1);
    public final Slider fovValue = new Slider("FOV", 90, 0, 180, 1);
    public final Slider speedValue = new Slider("Speed", 2, 1, 180, 1);
    public final Slider noise1Value = new Slider("Noise Amplitude", 0.5, 0, 2, 0.1);
    public final Slider noise2Value = new Slider("Noise Frequency", 1.0, 0.1, 5.0, 0.1);
    public final Slider noise3Value = new Slider("Noise Randomness", 0.3, 0, 1, 0.1);
    public final Slider noise4Value = new Slider("Noise Smoothing", 0.7, 0.1, 2.0, 0.1);
    public final Slider noise5Value = new Slider("Noise Offset", 0.2, 0, 1, 0.05);
    public final Slider smoothnessValue = new Slider("Smoothness", 0.3, 0.1, 1.0, 0.1);
    public final Checkbox safetyCheck = new Checkbox("Safety Check", true);
    public final Checkbox onlyWhenAttacking = new Checkbox("Only When Attacking", true);
    public final Checkbox checkVisible = new Checkbox("Check Visibility", true);
    public final Checkbox avoidSnap = new Checkbox("Avoid Snap Detection", true);
    public final Slider maxAngleChange = new Slider("Max Angle Change", 30, 5, 180, 1);
    public final Checkbox useNeuralNetwork = new Checkbox("Use Neural Network", false);
    public final Slider learningRate = new Slider("Learning Rate", 0.1, 0.01, 1.0, 0.01);
    public final Slider adaptationSpeed = new Slider("Adaptation Speed", 0.5, 0.1, 2.0, 0.1);

    private final Random random = new Random();
    private long lastUpdateTime = 0;
    private double timeCounter = 0;
    private Entity lastTarget = null;
    private int sameTargetCounter = 0;
    private final SimpleAimNeuralNetwork neuralNetwork = new SimpleAimNeuralNetwork();
    private final Map<Integer, TargetPattern> targetPatterns = new HashMap<>();
    private long lastAimTime = 0;
    private float[] lastAimAngles = new float[2];
    private int suspiciousBehaviorCount = 0;
    private static final int MAX_SUSPICIOUS_COUNT = 3;
    private static final long MIN_AIM_INTERVAL = 50;
    private static final int MAX_HISTORY_SIZE = 20;
    private static final float MIN_PITCH = -90.0f;
    private static final float MAX_PITCH = 90.0f;
    private double noiseOffsetX = 0;
    private double noiseOffsetY = 0;

    public AimAssist(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
        noiseOffsetX = random.nextDouble() * 1000;
        noiseOffsetY = random.nextDouble() * 1000;
    }

    @EventListener
    public void onStrafe(StrafeEvent event) {
        if (!isSafeToAim()) return;
        
        Entity target = findBestTarget();
        if (target == null) {
            resetTracking();
            return;
        }

        if (useNeuralNetwork.isEnabled()) {
            updateNeuralNetwork(target);
        }

        Rotation targetRotation = calculateTargetRotation(target);
        if (targetRotation == null) return;

        Rotation safeRotation = applySafetyChecks(targetRotation);
        if (safeRotation == null) return;

        if (applyValidRotation(safeRotation)) {
            updateSafetyTracking(safeRotation);
        }
    }

    private boolean isSafeToAim() {
        if (mc == null || mc.gameSettings == null || mc.player == null || mc.world == null) return false;
        if (onlyWhenAttacking.isEnabled() && !mc.gameSettings.keyBindAttack.isKeyDown()) return false;
        if (mc.currentScreen != null) return false;
        if (mc.player.isDead || mc.player.capabilities.isCreativeMode) return false;
        return System.currentTimeMillis() - lastAimTime >= MIN_AIM_INTERVAL;
    }

    private Entity findBestTarget() {
        double range = rangeValue.getValue().doubleValue();
        double maxFov = fovValue.getValue().doubleValue();
        Entity bestTarget = null;
        double bestScore = Double.MAX_VALUE;

        for (Entity entity : mc.world.loadedEntityList) {
            if (!isValidTarget(entity)) continue;
            
            double score = calculateTargetScore(entity, range, maxFov);
            if (score < bestScore) {
                bestScore = score;
                bestTarget = entity;
            }
        }

        updateTargetTracking(bestTarget);
        return bestTarget;
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == mc.player || !(entity instanceof EntityLivingBase)) return false;
        if (!Target.isTarget(entity) || !entity.isEntityAlive()) return false;
        
        double distance = mc.player.getDistance(entity);
        double range = rangeValue.getValue().doubleValue();
        if (distance > range) return false;
        
        if (checkVisible.isEnabled() && !mc.player.canEntityBeSeen(entity)) return false;
        
        return RotationUtil.getRotationDifference(entity) <= fovValue.getValue().doubleValue();
    }

    private double calculateTargetScore(Entity entity, double range, double maxFov) {
        double distance = mc.player.getDistance(entity);
        double rotationDiff = RotationUtil.getRotationDifference(entity);
        double score = (distance / range) * 0.6 + (rotationDiff / maxFov) * 0.4;
        
        if (useNeuralNetwork.isEnabled()) {
            TargetPattern pattern = targetPatterns.get(entity.getEntityId());
            if (pattern != null) {
                score *= pattern.getPredictionConfidence();
            }
        }
        return score;
    }

    private Rotation calculateTargetRotation(Entity target) {
        Rotation baseRotation = RotationUtil.toRotation(RotationUtil.getCenter(target.getEntityBoundingBox()), 1.0F);
        
        if (useNeuralNetwork.isEnabled()) {
            baseRotation = applyNeuralPrediction(baseRotation, target);
        }
        
        Rotation humanizedRotation = applyHumanizationNoise(baseRotation);
        return RotationUtil.limitAngleChange(
            RotationUtil.getPlayerRotation(), 
            humanizedRotation, 
            (float) speedValue.getValue().doubleValue()
        );
    }

    private Rotation applyNeuralPrediction(Rotation rotation, Entity target) {
        TargetPattern pattern = targetPatterns.get(target.getEntityId());
        if (pattern == null) return rotation;
        
        float[] prediction = pattern.predictNextPosition();
        float confidence = pattern.getPredictionConfidence();
        float predictedYaw = rotation.yaw + prediction[0] * confidence;
        float predictedPitch = MathHelper.clamp(rotation.pitch + prediction[1] * confidence, MIN_PITCH, MAX_PITCH);
        
        return new Rotation(predictedYaw, predictedPitch);
    }

    private Rotation applyHumanizationNoise(Rotation rotation) {
        long currentTime = System.currentTimeMillis();
        if (lastUpdateTime == 0) lastUpdateTime = currentTime;
        
        double deltaTime = (currentTime - lastUpdateTime) / 1000.0;
        lastUpdateTime = currentTime;
        timeCounter += deltaTime;

        double amplitude = noise1Value.getValue().doubleValue();
        double frequency = noise2Value.getValue().doubleValue();
        double randomness = noise3Value.getValue().doubleValue();
        double smoothing = noise4Value.getValue().doubleValue();
        double offset = noise5Value.getValue().doubleValue();
        double smoothness = MathHelper.clamp(smoothnessValue.getValue().doubleValue(), 0.1, 1.0);

        double timeWithOffset = timeCounter + offset;
        double yawNoise = generateAdvancedNoise(timeWithOffset * frequency + noiseOffsetX, noiseOffsetY, randomness, smoothing) * amplitude * smoothness;
        double pitchNoise = generateAdvancedNoise(noiseOffsetX, timeWithOffset * frequency + noiseOffsetY, randomness, smoothing) * amplitude * 0.5 * smoothness;

        yawNoise = MathHelper.clamp(yawNoise, -8.0, 8.0);
        pitchNoise = MathHelper.clamp(pitchNoise, -4.0, 4.0);

        float newYaw = (float) (rotation.yaw + yawNoise);
        float newPitch = (float) (rotation.pitch + pitchNoise);
        
        return normalizeRotation(new Rotation(newYaw, newPitch));
    }

    private double generateAdvancedNoise(double x, double y, double randomness, double smoothing) {
        double baseNoise = generateImprovedNoise(x, y);
        double randomComponent = (random.nextDouble() - 0.5) * 2.0 * randomness;
        double combined = (baseNoise * (1.0 - randomness)) + (randomComponent * randomness);
        
        double smoothed = smoothNoise(combined, smoothing);
        return MathHelper.clamp(smoothed, -1.0, 1.0);
    }

    private double smoothNoise(double value, double smoothing) {
        if (smoothing <= 0.1) return value;
        double factor = 1.0 / smoothing;
        return Math.tanh(value * factor) / Math.tanh(factor);
    }

    private boolean applyValidRotation(Rotation rotation) {
        if (!isValidRotation(rotation)) {
            return false;
        }

        Rotation currentRotation = RotationUtil.getPlayerRotation();
        Rotation smoothedRotation = smoothRotationTransition(currentRotation, rotation);
        
        if (!isValidRotation(smoothedRotation)) {
            smoothedRotation = normalizeRotation(smoothedRotation);
        }
        
        if (isValidRotation(smoothedRotation)) {
            smoothedRotation.apply();
            return true;
        }
        return false;
    }

    private boolean isValidRotation(Rotation rotation) {
        if (rotation == null) return false;
        
        float yaw = rotation.yaw;
        float pitch = rotation.pitch;
        
        if (Float.isNaN(yaw) || Float.isInfinite(yaw) || Float.isNaN(pitch) || Float.isInfinite(pitch)) {
            return false;
        }
        
        if (pitch < MIN_PITCH || pitch > MAX_PITCH) {
            return false;
        }
        
        return true;
    }

    private Rotation normalizeRotation(Rotation rotation) {
        float yaw = MathHelper.wrapDegrees(rotation.yaw);
        float pitch = MathHelper.clamp(rotation.pitch, MIN_PITCH, MAX_PITCH);
        return new Rotation(yaw, pitch);
    }

    private Rotation smoothRotationTransition(Rotation from, Rotation to) {
        float smoothness = (float) smoothnessValue.getValue().doubleValue();
        smoothness = MathHelper.clamp(smoothness, 0.1f, 1.0f);
        
        float deltaYaw = MathHelper.wrapDegrees(to.yaw - from.yaw);
        float deltaPitch = to.pitch - from.pitch;
        
        float smoothedYaw = from.yaw + deltaYaw * smoothness;
        float smoothedPitch = from.pitch + deltaPitch * smoothness;
        
        smoothedYaw = MathHelper.wrapDegrees(smoothedYaw);
        smoothedPitch = MathHelper.clamp(smoothedPitch, MIN_PITCH, MAX_PITCH);
        
        return new Rotation(smoothedYaw, smoothedPitch);
    }

    private Rotation applySafetyChecks(Rotation rotation) {
        if (!safetyCheck.isEnabled()) return rotation;
        
        Rotation normalizedRotation = normalizeRotation(rotation);
        if (!isValidRotation(normalizedRotation)) {
            return null;
        }
        
        float deltaYaw = MathHelper.wrapDegrees(normalizedRotation.yaw - lastAimAngles[0]);
        float deltaPitch = normalizedRotation.pitch - lastAimAngles[1];
        float maxChange = (float) maxAngleChange.getValue().doubleValue();

        if (Math.abs(deltaYaw) > maxChange || Math.abs(deltaPitch) > maxChange) {
            if (avoidSnap.isEnabled()) {
                return handleSuspiciousMovement(deltaYaw, deltaPitch, maxChange);
            }
        } else {
            suspiciousBehaviorCount = Math.max(0, suspiciousBehaviorCount - 1);
        }

        return normalizedRotation;
    }

    private Rotation handleSuspiciousMovement(float deltaYaw, float deltaPitch, float maxChange) {
        suspiciousBehaviorCount++;
        if (suspiciousBehaviorCount > MAX_SUSPICIOUS_COUNT) return null;
        
        float limitedYaw = lastAimAngles[0] + Math.signum(deltaYaw) * maxChange * 0.5f;
        float limitedPitch = lastAimAngles[1] + Math.signum(deltaPitch) * maxChange * 0.5f;
        
        return normalizeRotation(new Rotation(limitedYaw, limitedPitch));
    }

    private void updateSafetyTracking(Rotation rotation) {
        lastAimTime = System.currentTimeMillis();
        Rotation normalized = normalizeRotation(rotation);
        lastAimAngles[0] = normalized.yaw;
        lastAimAngles[1] = normalized.pitch;
    }

    private void updateTargetTracking(Entity currentTarget) {
        if (currentTarget == lastTarget) {
            sameTargetCounter++;
        } else {
            sameTargetCounter = 0;
            lastTarget = currentTarget;
        }

        if (currentTarget != null && useNeuralNetwork.isEnabled()) {
            int entityId = currentTarget.getEntityId();
            targetPatterns.computeIfAbsent(entityId, k -> new TargetPattern()).updatePosition(currentTarget);
        }
    }

    private void updateNeuralNetwork(Entity target) {
        TargetPattern pattern = targetPatterns.get(target.getEntityId());
        if (pattern != null && neuralNetwork != null) {
            neuralNetwork.learnFromPattern(pattern, learningRate.getValue().floatValue());
        }
    }

    private void resetTracking() {
        sameTargetCounter = 0;
        lastTarget = null;
    }

    private double generateImprovedNoise(double x, double y) {
        double noise = 0.0;
        double amplitude = 1.0;
        double frequency = 1.0;
        double maxAmplitude = 0.0;

        for (int i = 0; i < 3; i++) {
            noise += generatePerlinNoise(x * frequency, y * frequency) * amplitude;
            maxAmplitude += amplitude;
            amplitude *= 0.5;
            frequency *= 2.0;
        }

        return noise / maxAmplitude;
    }

    private double generatePerlinNoise(double x, double y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        double u = fade(x);
        double v = fade(y);
        int A = p[X] + Y;
        int B = p[X + 1] + Y;
        
        return lerp(v, lerp(u, grad(p[p[A]], x, y, 0), grad(p[p[B]], x - 1, y, 0)),
                lerp(u, grad(p[p[A + 1]], x, y - 1, 0), grad(p[p[B + 1]], x - 1, y - 1, 0)));
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private static final int[] p = new int[512];
    private static final int[] permutation = {
        151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30,
        69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94,
        252, 219, 203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171,
        168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60,
        211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1,
        216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86,
        164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118,
        126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170,
        213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9, 129, 22, 39,
        253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228, 251, 34,
        242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107, 49,
        192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254,
        138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
    };

    static {
        System.arraycopy(permutation, 0, p, 0, 256);
        System.arraycopy(permutation, 0, p, 256, 256);
    }

    private class SimpleAimNeuralNetwork {
        private final float[] weights = new float[6];

        public SimpleAimNeuralNetwork() {
            for (int i = 0; i < weights.length; i++) {
                weights[i] = (random.nextFloat() - 0.5f) * 2f;
            }
        }

        public void learnFromPattern(TargetPattern pattern, float learningRate) {
            if (pattern.getHistorySize() < 2) return;
            
            List<float[]> history = pattern.getPositionHistory();
            float[] lastPos = history.get(history.size() - 2);
            float[] currentPos = history.get(history.size() - 1);
            float[] predicted = predict(lastPos[0], lastPos[1], lastPos[2]);
            float[] actual = {
                currentPos[0] - lastPos[0], 
                currentPos[1] - lastPos[1], 
                currentPos[2] - lastPos[2]
            };

            for (int i = 0; i < 3; i++) {
                float error = actual[i] - predicted[i];
                weights[i * 2] += learningRate * error * lastPos[0];
                weights[i * 2 + 1] += learningRate * error * lastPos[1];
            }
        }

        public float[] predict(float x, float y, float z) {
            return new float[]{
                weights[0] * x + weights[1] * y,
                weights[2] * x + weights[3] * y,
                weights[4] * x + weights[5] * y
            };
        }
    }

    private class TargetPattern {
        private final List<float[]> positionHistory = new ArrayList<>();
        private long lastUpdateTime = 0;

        public void updatePosition(Entity target) {
            float[] currentPos = {
                (float) target.posX,
                (float) target.posY,
                (float) target.posZ
            };
            positionHistory.add(currentPos);
            if (positionHistory.size() > MAX_HISTORY_SIZE) {
                positionHistory.remove(0);
            }
            lastUpdateTime = System.currentTimeMillis();
        }

        public float[] predictNextPosition() {
            if (positionHistory.size() < 2) return new float[]{0, 0, 0};
            
            float[] lastPos = positionHistory.get(positionHistory.size() - 1);
            float[] prediction = neuralNetwork.predict(lastPos[0], lastPos[1], lastPos[2]);
            return new float[]{prediction[0], prediction[1], 0};
        }

        public float getPredictionConfidence() {
            if (positionHistory.size() < 5) return 0.1f;
            
            float consistency = calculateMovementConsistency();
            return Math.min(consistency * adaptationSpeed.getValue().floatValue(), 1.0f);
        }

        private float calculateMovementConsistency() {
            if (positionHistory.size() < 3) return 0f;
            
            float totalChange = 0f;
            int samples = 0;
            
            for (int i = 1; i < positionHistory.size(); i++) {
                float[] prev = positionHistory.get(i - 1);
                float[] curr = positionHistory.get(i);
                totalChange += Math.abs(curr[0] - prev[0]) + Math.abs(curr[1] - prev[1]);
                samples += 2;
            }
            
            float avgChange = totalChange / samples;
            return 1.0f / (1.0f + avgChange);
        }

        public List<float[]> getPositionHistory() {
            return new ArrayList<>(positionHistory);
        }

        public int getHistorySize() {
            return positionHistory.size();
        }
    }
}
