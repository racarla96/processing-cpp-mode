package processing.mode.cpp;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import processing.app.Base;
import processing.app.Formatter;
import processing.app.Language;
import processing.app.Mode;
import processing.app.SketchCode;
import processing.app.ui.Editor;
import processing.app.ui.EditorException;
import processing.app.ui.EditorState;
import processing.app.ui.EditorToolbar;
import processing.app.ui.Toolkit;

/**
 * CppEditor.java
 *
 * Editor del modo C++. Conecta los botones Play/Stop de la barra de
 * herramientas (vía CppToolbar) con CppBuild (compilar) y CppRunner
 * (arrancar/matar el binario resultante). El resto de la UI (menús,
 * consola, resaltado de sintaxis...) lo hereda de Editor sin cambios;
 * pulir esa experiencia (consola de errores clickeable, ocultar la
 * plantilla al usuario...) es Fase 4.
 */
public class CppEditor extends Editor {

    private final CppMode cppMode;
    private final CppRunner runner = new CppRunner();

    protected CppEditor(Base base, String path, EditorState state, Mode mode)
            throws EditorException {
        super(base, path, state, mode);
        this.cppMode = (CppMode) mode;
    }

    @Override
    public EditorToolbar createToolbar() {
        return new CppToolbar(this);
    }

    @Override
    public Formatter createFormatter() {
        // Auto-format real (indentado, etc.) queda para Fase 5; de
        // momento no se toca el texto.
        return text -> text;
    }

    @Override
    public JMenu buildFileMenu() {
        // Sin items de exportación todavía (no hay "exportar aplicación"
        // para C++ como sí lo hay para Java); Editor añade el resto
        // (New/Open/Save/Save As/Quit...) por defecto.
        return buildFileMenu(new JMenuItem[0]);
    }

    @Override
    public JMenu buildSketchMenu() {
        JMenuItem runItem = Toolkit.newJMenuItem(Language.text("menu.sketch.run"), 'R');
        runItem.addActionListener(e -> handleRun());

        JMenuItem stopItem = new JMenuItem(Language.text("menu.sketch.stop"));
        stopItem.addActionListener(e -> handleStop());

        return buildSketchMenu(new JMenuItem[] { runItem, stopItem });
    }

    @Override
    public JMenu buildHelpMenu() {
        // Sin items propios todavía (sin referencia/about específicos de
        // C++ Mode); es un menú vacío pero válido. Fase 4/5.
        return new JMenu(Language.text("menu.help"));
    }

    @Override
    public void handleImportLibrary(String name) {
        statusNotice("Las librerías todavía no están soportadas en C++ Mode.");
    }

    @Override
    public String getCommentPrefix() {
        return "//";
    }

    @Override
    public void internalCloseRunner() {
        handleStop();
    }

    @Override
    public void deactivateRun() {
        getToolbar().deactivateRun();
    }

    /** Compila el sketch (en un hilo aparte) y, si compila, lo ejecuta. */
    public void handleRun() {
        getToolbar().activateRun();
        statusNotice("Compilando…");
        startIndeterminate();

        new Thread(() -> {
            try {
                List<CppSketch.Tab> tabs = new ArrayList<>();
                for (SketchCode code : sketch.getCode()) {
                    if (code.isExtension("cpp")) {
                        tabs.add(new CppSketch.Tab(code.getFileName(), code.getProgram()));
                    }
                }

                File buildDir = new File(sketch.getFolder(), "build");
                CppBuild.Result result = CppBuild.build(
                    sketch.getName(), tabs, buildDir,
                    cppMode.getTemplateFile(), cppMode.getRuntimeIncludeDir(),
                    cppMode.getRuntimeSrcDir());

                EventQueue.invokeLater(() -> {
                    stopIndeterminate();
                    reportProblems(result.problems());
                    if (result.success()) {
                        launch(result.executable());
                    } else {
                        statusError("La compilación falló.");
                        deactivateRun();
                    }
                });
            } catch (IOException e) {
                EventQueue.invokeLater(() -> {
                    stopIndeterminate();
                    statusError(e);
                    deactivateRun();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                EventQueue.invokeLater(() -> {
                    stopIndeterminate();
                    statusError("La compilación se interrumpió.");
                    deactivateRun();
                });
            }
        }, "cpp-build").start();
    }

    private void launch(File executable) {
        try {
            runner.start(executable, new CppRunner.OutputListener() {
                @Override
                public void onOutput(String line, boolean isError) {
                    (isError ? System.err : System.out).println(line);
                }

                @Override
                public void onExit(int exitCode) {
                    EventQueue.invokeLater(() -> {
                        statusNotice("El sketch terminó (código " + exitCode + ").");
                        deactivateRun();
                    });
                }
            });
            statusNotice("Ejecutando.");
        } catch (IOException e) {
            statusError(e);
            deactivateRun();
        }
    }

    /** Detiene la ejecución en marcha, si la hay. */
    public void handleStop() {
        runner.stop();
        getToolbar().activateStop();
        deactivateRun();
        getToolbar().deactivateStop();
        statusEmpty();
    }

    private void reportProblems(List<CppBuild.Problem> problems) {
        if (problems.isEmpty()) {
            return;
        }
        for (CppBuild.Problem problem : problems) {
            System.err.println(problem);
        }
        problems.stream()
            .filter(p -> p.severity() == CppBuild.Severity.ERROR)
            .findFirst()
            .ifPresent(first -> statusError(first.message()));
    }
}
