package processing.mode.cpp;

import java.io.File;

import processing.app.Base;
import processing.app.Mode;
import processing.app.ui.Editor;
import processing.app.ui.EditorException;
import processing.app.ui.EditorState;

/**
 * CppModeDev.java
 *
 * Punto de entrada del Mode C++ para Processing4. Registra el editor
 * (CppEditor) y expone las rutas al runtime C++ (headers + fuentes)
 * que CppBuild necesita para compilar cada sketch.
 *
 * Se llama CppModeDev (no CppMode) a propósito: Processing exige que el
 * .jar, la clase Java dentro de él y la carpeta de instalación en
 * sketchbook/modes/ compartan el mismo nombre (ver
 * ModeContribution.findClassInZipFile() en el PDE), y "CppMode" ya lo
 * usa processing-cpp/processing.cpp — un Mode C++ para Processing4
 * distinto y más maduro (ver CLAUDE.md). Este nombre evita chocar con
 * esa instalación si conviven en el mismo sketchbook.
 */
public class CppModeDev extends Mode {

    public CppModeDev(Base base, File folder) {
        super(base, folder);
    }

    @Override
    public String getTitle() {
        return "C++ (dev)";
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
     * CLAUDE.md). Se busca en este orden:
     *
     *   1. PROCESSING_CPP_MODE_RUNTIME (override explícito, útil en dev/tests).
     *   2. <carpeta del Mode>/runtime — el caso normal para un Mode
     *      instalado de verdad: scripts/package-mode.sh copia runtime/
     *      dentro de la distribución, así que viaja junto al .jar sin
     *      depender de dónde esté el resto del repo.
     *   3. <carpeta del Mode>/../runtime — layout de desarrollo en este
     *      repo, donde mode/ y runtime/ son carpetas hermanas y el PDE
     *      apunta directamente a mode/ sin pasar por el empaquetado.
     */
    public File getRuntimeRoot() {
        String override = System.getenv("PROCESSING_CPP_MODE_RUNTIME");
        if (override != null) {
            return new File(override);
        }
        File bundled = new File(getFolder(), "runtime");
        if (bundled.exists()) {
            return bundled;
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
