package game.word;

import org.joml.Vector3f;

public class CicloDiaNoche {
    private float tiempoDia; // 0.0 = medianoche, 0.25 = amanecer, 0.5 = mediodia, 0.75 = atardecer
    private float velocidad;
    private boolean activado;

    private Vector3f colorCieloDia;
    private Vector3f colorCieloNoche;
    private Vector3f colorCieloActual;

    private Vector3f luzPosDia;
    private Vector3f luzPosNoche;
    private Vector3f luzPosActual;

    private float intensidadLuzDia;
    private float intensidadLuzNoche;
    private float intensidadLuzActual;

    public CicloDiaNoche() {
        this.tiempoDia = 0.5f; // empezar en mediodia
        this.velocidad = 0.001f;
        this.activado = true;

        this.colorCieloDia = new Vector3f(0.24f, 0.56f, 0.74f);   // azul cielo
        this.colorCieloNoche = new Vector3f(0.05f, 0.05f, 0.15f); // azul oscuro noche
        this.colorCieloActual = new Vector3f(colorCieloDia);

        this.luzPosDia = new Vector3f(10.0f, 20.0f, 10.0f);
        this.luzPosNoche = new Vector3f(-10.0f, -5.0f, 10.0f);
        this.luzPosActual = new Vector3f(luzPosDia);

        this.intensidadLuzDia = 1.0f;
        this.intensidadLuzNoche = 0.2f;
        this.intensidadLuzActual = intensidadLuzDia;
    }

    public void update(float deltaTime) {
        if (!activado) {
            return;
        }

        tiempoDia += velocidad * deltaTime * 60.0f;
        if (tiempoDia > 1.0f) {
            tiempoDia -= 1.0f;
        }

        // Calcular factores de mezcla usando seno para transicion suave
        float angulo = tiempoDia * (float) Math.PI * 2.0f;
        float diaFactor = (float) (Math.sin(angulo - Math.PI / 2.0) + 1.0) / 2.0f; // 0 a 1
        float nocheFactor = 1.0f - diaFactor;

        // Mezclar colores de cielo
        colorCieloActual.set(
            colorCieloDia.x * diaFactor + colorCieloNoche.x * nocheFactor,
            colorCieloDia.y * diaFactor + colorCieloNoche.y * nocheFactor,
            colorCieloDia.z * diaFactor + colorCieloNoche.z * nocheFactor
        );

        // Mezclar posicion de luz
        luzPosActual.set(
            luzPosDia.x * diaFactor + luzPosNoche.x * nocheFactor,
            luzPosDia.y * diaFactor + luzPosNoche.y * nocheFactor,
            luzPosDia.z * diaFactor + luzPosNoche.z * nocheFactor
        );

        // Mezclar intensidad
        intensidadLuzActual = intensidadLuzDia * diaFactor + intensidadLuzNoche * nocheFactor;
    }

    public Vector3f getColorCielo() {
        return colorCieloActual;
    }

    public Vector3f getLuzPosicion() {
        return luzPosActual;
    }

    public float getIntensidadLuz() {
        return intensidadLuzActual;
    }

    public float getTiempoDia() {
        return tiempoDia;
    }

    public boolean esDia() {
        return tiempoDia > 0.25f && tiempoDia < 0.75f;
    }

    public void setVelocidad(float velocidad) {
        this.velocidad = velocidad;
    }

    public void setActivado(boolean activado) {
        this.activado = activado;
    }

    public boolean isActivado() {
        return activado;
    }
}
