package processing.mode.cpp;

import processing.app.ui.Editor;
import processing.app.ui.EditorToolbar;

/**
 * CppToolbar.java
 *
 * Barra de herramientas del Mode C++: reutiliza el layout por defecto
 * de EditorToolbar (botones Run/Stop + selector de Mode) y solo conecta
 * esos botones a CppEditor.handleRun()/handleStop(). La personalización
 * visual (iconos propios, etc.) es Fase 4 (UX).
 */
public class CppToolbar extends EditorToolbar {

    public CppToolbar(Editor editor) {
        super(editor);
    }

    @Override
    public void handleRun(int modifiers) {
        ((CppEditor) editor).handleRun();
    }

    @Override
    public void handleStop() {
        ((CppEditor) editor).handleStop();
    }
}
