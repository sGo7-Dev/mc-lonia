package game;

import org.lwjgl.Version;

public class Main {
    public static void main(String[] args) {
        GameLogger.init();
        GameLogger.log("Iniciando MC-Lonia...");
        GameLogger.log("Version de LWJGL: " + Version.getVersion());

        Game game = null;
        try {
            game = new Game(896, 504, "MCLonia");
            game.run();
        } catch (Exception e) {
            GameLogger.logError("Error fatal durante la ejecucion", e);
        } finally {
            if (game != null) {
                game.cleanup();
            }
            GameLogger.close();
        }
    }
}
