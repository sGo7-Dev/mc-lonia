package game.word;

import java.util.HashMap;
import java.util.Map;

public class TypBloks {
    public static final String TESTBLOK = "testblok";

    private static final Map<String, TypBlok> blokos = new HashMap<>();

    static {
        TypBlok testblok = new TypBlok(TESTBLOK);
        testblok.addTextura("superior", "/assets/textures/testgrafics_texture.png");
        testblok.addTextura("lateral", "/assets/textures/testgrafics_texture.png");
        blokos.put(TESTBLOK, testblok);
    }

    public static TypBlok get(String nombre) {
        return blokos.get(nombre);
    }

    public static TypBlok getTestBlok() {
        return blokos.get(TESTBLOK);
    }

    public static boolean existe(String nombre) {
        return blokos.containsKey(nombre);
    }

    public static class TypBlok {
        private final String nombre;
        private final Map<String, String> texturas;
        private boolean enTodoElChunk;

        public TypBlok(String nombre) {
            this.nombre = nombre;
            this.texturas = new HashMap<>();
            this.enTodoElChunk = false;
        }

        public TypBlok addTextura(String cara, String rutaPNG) {
            texturas.put(cara, rutaPNG);
            return this;
        }

        public TypBlok enTodoElChunk(boolean valor) {
            this.enTodoElChunk = valor;
            return this;
        }

        public String getNombre() {
            return nombre;
        }

        public String getTextura(String cara) {
            return texturas.get(cara);
        }

        public Map<String, String> getTexturas() {
            return texturas;
        }

        public boolean isEnTodoElChunk() {
            return enTodoElChunk;
        }

        public boolean tieneTextura(String cara) {
            return texturas.containsKey(cara);
        }
    }
}
