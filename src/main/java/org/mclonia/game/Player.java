package org.mclonia.game;

import org.joml.Vector3f;

import static org.LWJGL.glfw.GLFW.*;

/**
 * Sistema del jugador y cámara en primera persona.
 */
public class Player {
    private final World world;

    // Posición
    private Vector3f position;
    private Vector3f velocity;

    // Cámara
    private float yaw;
    private float pitch;
    private float sensitivity;
    private float moveSpeed;

    // Configuración
    private static final float MAX_PITCH = 89.0f;
    private static final float MIN_PITCH = -89.0f;
    private static final float GRAVITY = 0.02f;
    private static final float JUMP_HEIGHT = 0.15f;

    public Player(World world) {
        this.world = world;
        this.position = new Vector3f(0, 20, 0);
        this.velocity = new Vector3f(0, 0, 0);
        this.yaw = -90.0f;
        this.pitch = 0.0f;
        this.sensitivity = 0.15f;
        this.moveSpeed = 0.08f;
    }

    public void update(float deltaTime, long window) {
        // Aplicar gravedad
        velocity.y -= GRAVITY;

        // Movimiento WASD
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            moveForward(deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            moveBackward(deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            moveLeft(deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            moveRight(deltaTime);
        }
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && onGround()) {
            velocity.y = JUMP_HEIGHT;
        }

        // Aplicar velocidad
        position.add(velocity.x * deltaTime * 60, velocity.y * deltaTime * 60, velocity.z * deltaTime * 60);

        // Colisión simple con el suelo
        if (position.y < 16) {
            position.y = 16;
            velocity.y = 0;
        }
    }

    private boolean onGround() {
        int x = (int) Math.floor(position.x);
        int y = (int) Math.floor(position.y);
        int z = (int) Math.floor(position.z);
        Block below = world.getBlock(x, y - 1, z);
        return !below.isAir();
    }

    private void moveForward(float deltaTime) {
        float rad = (float) Math.toRadians(yaw);
        velocity.z = (float) (Math.cos(rad) * moveSpeed);
        velocity.x = (float) (Math.sin(rad) * moveSpeed);
    }

    private void moveBackward(float deltaTime) {
        float rad = (float) Math.toRadians(yaw);
        velocity.z = -(float) (Math.cos(rad) * moveSpeed);
        velocity.x = -(float) (Math.sin(rad) * moveSpeed);
    }

    private void moveLeft(float deltaTime) {
        float rad = (float) Math.toRadians(yaw - 90);
        velocity.z = (float) (Math.cos(rad) * moveSpeed);
        velocity.x = (float) (Math.sin(rad) * moveSpeed);
    }

    private void moveRight(float deltaTime) {
        float rad = (float) Math.toRadians(yaw + 90);
        velocity.z = (float) (Math.cos(rad) * moveSpeed);
        velocity.x = (float) (Math.sin(rad) * moveSpeed);
    }

    public void setMouseLook(float mouseX, float mouseY) {
        yaw -= mouseX * sensitivity;
        pitch -= mouseY * sensitivity;

        // Limitar rotación vertical
        if (pitch > MAX_PITCH) pitch = MAX_PITCH;
        if (pitch < MIN_PITCH) pitch = MIN_PITCH;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }
}