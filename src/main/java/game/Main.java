package game;

import org.lwjgl.Version;

public class Main {
    public static void main(String[] args) {
        GameLogger.init();
        GameLogger.log("Iniciando MC-Lonia...");
        GameLogger.log("Version de LWJGL: " + Version.getVersion());

        int width = 1280;
        int height = 720;

        Game game = new Game(width, height, "MC-Lonia");
        try {
            game.run();
        } catch (Exception e) {
            GameLogger.logError("Error fatal durante la ejecucion", e);
        } finally {
            game.cleanup();
            GameLogger.close();
        }
    }
}
