package processing.mode.cpp;

import java.io.File;

import processing.app.Base;
import processing.app.Mode;
import processing.app.ui.Editor;
import processing.app.ui.EditorException;
import processing.app.ui.EditorState;

/**
 * CppMode.java
 *
 * Punto de entrada del Mode C++ para Processing4. Registra el editor
 * (CppEditor) y expone las rutas al runtime C++ (headers + fuentes)
 * que CppBuild necesita para compilar cada sketch.
 */
public class CppMode extends Mode {

    public CppMode(Base base, File folder) {
        super(base, folder);
    }

    @Override
    public String getTitle() {
        return "C++";
    }

    @Override
    public Editor createEditor(Base base, String path, EditorState state) throws EditorException {
        return new CppEditor(base, path, state, this);
    }

    @Override
    public String getDefaultExtension() {
        return "cpp";
    }

    @Override
    public String[] getExtensions() {
        return new String[] { "cpp", "h", "hpp" };
    }

    @Override
    public String[] getIgnorable() {
        return new String[] { "build" };
    }

    /**
     * Raíz del runtime C++ (headers en include/, fuentes en src/, ver
     * CLAUDE.md). En este repo mode/ y runtime/ son carpetas hermanas,
     * layout válido mientras se desarrolla apuntando el PDE directamente
     * a mode/ como carpeta del Mode.
     *
     * TODO: cuando el Mode se empaquete de verdad para distribuirse,
     * el runtime (headers + toolchain) tendrá que vendorizarse dentro
     * de la distribución del Mode en vez de asumir este layout de
     * desarrollo; PROCESSING_CPP_MODE_RUNTIME permite apuntar a otra
     * ruta mientras tanto (por ejemplo, en tests).
     */
    public File getRuntimeRoot() {
        String override = System.getenv("PROCESSING_CPP_MODE_RUNTIME");
        if (override != null) {
            return new File(override);
        }
        return new File(getFolder(), "../runtime").getAbsoluteFile();
    }

    public File getRuntimeIncludeDir() {
        return new File(getRuntimeRoot(), "include");
    }

    public File getRuntimeSrcDir() {
        return new File(getRuntimeRoot(), "src");
    }

    public File getTemplateFile() {
        return new File(getRuntimeSrcDir(), "main_template.cpp.in");
    }
}
