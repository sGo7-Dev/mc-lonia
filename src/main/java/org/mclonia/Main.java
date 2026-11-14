package org.mclonia;

import org.lwjgl.Version;
import org.mclonia.graphics.Window;

public class Main {
    static void main() {
        Window window = new Window(896, 504, "MCLonia");
        window.renderLoop();
        window.cleanup();
    }
}
