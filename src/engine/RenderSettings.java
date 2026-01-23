package engine;

import java.awt.*;

/** Набор флагов "как рисовать". */
public final class RenderSettings {
    public boolean drawWireframe = true;
    public boolean useTexture = false;
    public boolean useLighting = false;

    public Color baseColor = new Color(180, 180, 220);

    /** может быть null, если текстуру не загрузили */
    public Texture texture = null;
}
