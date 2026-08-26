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
import processing.app.SketchException;
import processing.app.ui.Toolkit;

/**
 * CppEditor.java
 *
 * Editor del modo C++. Conecta los botones Play/Stop de la barra de
 * herramientas (vía CppToolbar) con CppBuild (compilar) y CppRunner
 * (arrancar/matar el binario resultante), y salta a la tab/línea del
 * primer error de compilación reusando el mecanismo que ya trae Editor
 * para SketchException (ver reportProblems()/jumpToProblem()). El resto
 * de la UI (menús, resaltado de sintaxis...) lo hereda de Editor sin
 * cambios; el "build/" generado nunca aparece como tab porque Sketch
 * solo escanea archivos sueltos en la carpeta del sketch, no
 * subcarpetas, así que la plantilla ya queda oculta al usuario sin
 * necesidad de nada extra.
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
        getToolbar().activateStop();
        runner.stop();
        deactivateRun();
        getToolbar().deactivateStop();
        statusEmpty();
    }

    /**
     * Imprime todos los errores/warnings en la consola y salta a la tab
     * y línea del primero de los errores (si lo hay).
     */
    private void reportProblems(List<CppBuild.Problem> problems) {
        if (problems.isEmpty()) {
            return;
        }
        for (CppBuild.Problem problem : problems) {
            System.err.println(problem);
        }

        CppBuild.Problem firstError = problems.stream()
            .filter(p -> p.severity() == CppBuild.Severity.ERROR)
            .findFirst()
            .orElse(null);
        if (firstError != null) {
            jumpToProblem(firstError);
        }
    }

    /**
     * Mueve la tab activa a la del problema y selecciona su línea,
     * reusando Editor.statusError(Exception): para un SketchException
     * con codeIndex/codeLine válidos, Editor ya hace exactamente eso.
     */
    private void jumpToProblem(CppBuild.Problem problem) {
        int codeIndex = (problem.tabName() != null) ? findTabIndex(problem.tabName()) : -1;
        if (codeIndex == -1) {
            // Error en código inyectado por el template, fuera de las
            // tabs del usuario: no hay dónde saltar, solo se muestra
            // el mensaje (ya impreso arriba en la consola).
            statusError(problem.message());
            return;
        }
        // SketchException espera línea/columna 0-indexadas;
        // CppBuild.Problem las da 1-indexadas (como g++).
        statusError(new SketchException(
            problem.message(), codeIndex, problem.line() - 1, problem.column() - 1));
    }

    private int findTabIndex(String tabName) {
        SketchCode[] code = sketch.getCode();
        for (int i = 0; i < code.length; i++) {
            if (code[i].getFileName().equals(tabName)) {
                return i;
            }
        }
        return -1;
    }
}
