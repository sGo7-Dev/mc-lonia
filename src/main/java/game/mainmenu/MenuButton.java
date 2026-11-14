package game.mainmenu;

import org.joml.Vector2f;

public class MenuButton {
    private final String text;
    private final String actionId;
    private final Vector2f position;
    private final Vector2f size;
    private boolean hovered;
    private boolean pressed;

    public MenuButton(String text, String actionId, float x, float y, float width, float height) {
        this.text = text;
        this.actionId = actionId;
        this.position = new Vector2f(x, y);
        this.size = new Vector2f(width, height);
        this.hovered = false;
        this.pressed = false;
    }

    public String getText() {
        return text;
    }

    public String getActionId() {
        return actionId;
    }

    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getSize() {
        return size;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getWidth() {
        return size.x;
    }

    public float getHeight() {
        return size.y;
    }

    public boolean isHovered() {
        return hovered;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public boolean contains(float mouseX, float mouseY) {
        return mouseX >= position.x && mouseX <= position.x + size.x
            && mouseY >= position.y && mouseY <= position.y + size.y;
    }

    public void press() {
        this.pressed = true;
    }

    public void reset() {
        this.pressed = false;
    }
}
