package org.mclonia.graphics;

import org.joml.Vector2f;

import java.util.function.Consumer;

public class Button {
    private String text;
    private Vector2f pos;
    private Vector2f size;
    private Runnable callback;

    public Button(Vector2f pos, Vector2f size, String text, Runnable callback) {
        this.text = text;
        this.pos = pos;
        this.size = size;
        this.callback = callback;
    }

    public void press() {
        callback.run();
    }
}
