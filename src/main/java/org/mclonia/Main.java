package org.mclonia;

import org.LWJGL.Version;
import org.mclonia.game.Game;

public class Main {
    static void main() {
        System.out.println("Versión de LWJGL: " + Version.getVersion());
        Game game = new Game(896, 504, "MCLonia");
        game.run();
    }
}