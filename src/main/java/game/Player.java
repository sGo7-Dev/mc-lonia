package game;

import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Player {
    private final World world;

    private Vector3f position;
    private Vector3f velocity;

    private float yaw;
    private float pitch;
    private float sensitivity;
    private float moveSpeed;

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
        // Gravedad con deltaTime para consistencia entre framerates
        velocity.y -= GRAVITY * deltaTime * 60.0f;

        float dx = 0.0f;
        float dz = 0.0f;

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            dz -= 1.0f;
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            dz += 1.0f;
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            dx -= 1.0f;
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            dx += 1.0f;
        }

        // Normalizar para que el movimiento diagonal no sea mas rapido
        if (dx != 0.0f || dz != 0.0f) {
            float len = (float) Math.sqrt(dx * dx + dz * dz);
            dx /= len;
            dz /= len;
        }

        float rad = (float) Math.toRadians(yaw);
        float forwardX = (float) Math.sin(rad);
        float forwardZ = (float) Math.cos(rad);
        float rightX = (float) Math.sin(rad - Math.PI / 2.0);
        float rightZ = (float) Math.cos(rad - Math.PI / 2.0);

        float moveX = forwardX * dz + rightX * dx;
        float moveZ = forwardZ * dz + rightZ * dx;

        velocity.x = moveX * moveSpeed * deltaTime * 60.0f;
        velocity.z = moveZ * moveSpeed * deltaTime * 60.0f;

        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && onGround()) {
            velocity.y = JUMP_HEIGHT;
        }

        position.add(velocity.x, velocity.y, velocity.z);

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

    public void setMouseLook(float mouseX, float mouseY) {
        yaw -= mouseX * sensitivity;
        pitch -= mouseY * sensitivity;

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
