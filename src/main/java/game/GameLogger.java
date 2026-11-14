package game;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameLogger {
    private static final String LOG_FILE = "log.txt";
    private static PrintWriter writer;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static void init() {
        try {
            writer = new PrintWriter(new FileWriter(LOG_FILE, true), true);
            log("=== Inicio de sesion MC-Lonia ===");
        } catch (IOException e) {
            System.err.println("No se pudo iniciar el logger: " + e.getMessage());
        }
    }

    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String line = "[" + timestamp + "] " + message;
        if (writer != null) {
            writer.println(line);
            writer.flush();
        }
        System.out.println(line);
    }

    public static void logError(String message, Throwable t) {
        String timestamp = LocalDateTime.now().format(formatter);
        String line = "[" + timestamp + "] ERROR: " + message;
        if (writer != null) {
            writer.println(line);
            if (t != null) {
                t.printStackTrace(writer);
            }
            writer.flush();
        }
        System.err.println(line);
        if (t != null) {
            t.printStackTrace();
        }
    }

    public static void close() {
        if (writer != null) {
            log("=== Cierre de sesion MC-Lonia ===");
            writer.close();
            writer = null;
        }
    }
}
