package processing.mode.cpp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CppBuild.java
 *
 * Invoca a g++ sobre el archivo generado por CppSketch (junto a las
 * fuentes del runtime, compiladas en la misma invocación: no hace falta
 * un paso previo que construya una librería estática), y parsea los
 * errores/warnings de salida para mapearlos a la tab/línea del sketch
 * original usando CppSketch.Preprocessed.locate().
 *
 * No depende de las clases del PDE: es un proceso externo (g++) más
 * parseo de texto, así que se puede validar de forma aislada (ver el
 * main() al final) igual que CppSketch.
 */
public class CppBuild {

    public enum Severity { ERROR, WARNING }

    /**
     * Un error o warning del compilador, ya mapeado a una tab del sketch
     * cuando es posible. tabName es null si la línea reportada cae en
     * código inyectado por el template (no en el sketch del usuario).
     */
    public record Problem(Severity severity, String tabName, int line, int column, String message) {
        @Override
        public String toString() {
            String where = (tabName != null) ? (tabName + ":" + line) : ("(generado):" + line);
            String kind = (severity == Severity.ERROR) ? "error" : "warning";
            return where + ": " + kind + ": " + message;
        }
    }

    public record Result(boolean success, File executable, List<Problem> problems) {}

    // Ejemplo de línea que produce g++/clang++:
    //   build/default.cpp:12:5: error: 'foo' was not declared in this scope
    private static final Pattern DIAGNOSTIC = Pattern.compile(
        "^(?<file>[^:]+):(?<line>\\d+):(?<col>\\d+):\\s*(?<severity>error|warning):\\s*(?<msg>.*)$");

    /**
     * Preprocesa el sketch (tabs ya en memoria, con ediciones sin
     * guardar incluidas) y lo compila junto a las fuentes del runtime.
     *
     * @param sketchName        nombre del sketch (y del ejecutable resultante).
     * @param tabs               tabs .cpp del sketch, tab principal primero.
     * @param buildDir           carpeta donde escribir el .cpp generado y el binario.
     * @param templateFile       runtime/src/main_template.cpp.in.
     * @param runtimeIncludeDir  runtime/include (contiene processing/Processing.h).
     * @param runtimeSrcDir      runtime/src (contiene Window.cpp y Renderer.cpp).
     */
    public static Result build(String sketchName, List<CppSketch.Tab> tabs, File buildDir,
                               File templateFile, File runtimeIncludeDir, File runtimeSrcDir)
            throws IOException, InterruptedException {
        String templateSource = readFile(templateFile);
        CppSketch.Preprocessed preprocessed = CppSketch.preprocess(templateSource, tabs);
        File generatedFile = CppSketch.writeGenerated(preprocessed, buildDir, sketchName);
        File executable = new File(buildDir, sketchName);

        List<String> command = new ArrayList<>(List.of(
            "g++", "-std=c++17", "-Wall", "-Wextra",
            "-I", runtimeIncludeDir.getAbsolutePath(),
            generatedFile.getAbsolutePath(),
            new File(runtimeSrcDir, "Window.cpp").getAbsolutePath(),
            new File(runtimeSrcDir, "Renderer.cpp").getAbsolutePath()
        ));
        command.addAll(sdl2Flags());
        command.add("-o");
        command.add(executable.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);
        Process process = pb.start();

        String stderr = readAll(process.getErrorStream());
        int exitCode = process.waitFor();

        List<Problem> problems = parseProblems(stderr, generatedFile.getName(), preprocessed);
        boolean success = exitCode == 0 && executable.exists();
        return new Result(success, success ? executable : null, problems);
    }

    static List<Problem> parseProblems(String compilerOutput, String generatedFileName,
                                       CppSketch.Preprocessed preprocessed) {
        List<Problem> problems = new ArrayList<>();
        for (String rawLine : compilerOutput.split("\n")) {
            Matcher m = DIAGNOSTIC.matcher(rawLine.strip());
            if (!m.matches()) {
                continue;
            }

            // Solo nos interesan los diagnósticos sobre el archivo
            // generado; uno sobre un header del runtime (processing/*.h)
            // apuntaría a un bug del runtime, no del sketch del usuario.
            String file = m.group("file");
            if (!file.endsWith(generatedFileName)) {
                continue;
            }

            int genLine = Integer.parseInt(m.group("line"));
            int col = Integer.parseInt(m.group("col"));
            Severity severity = m.group("severity").equals("error") ? Severity.ERROR : Severity.WARNING;
            String message = m.group("msg");

            CppSketch.TabLocation loc = preprocessed.locate(genLine);
            if (loc != null) {
                problems.add(new Problem(severity, loc.tabName(), loc.line(), col, message));
            } else {
                problems.add(new Problem(severity, null, genLine, col, message));
            }
        }
        return problems;
    }

    /**
     * Flags de compilación/enlazado para SDL2 vía pkg-config. Solo
     * Linux/macOS por ahora (riesgo de portabilidad conocido, ver
     * CLAUDE.md: en Windows habrá que resolver esto de otra forma,
     * probablemente vendorizando SDL2 con el propio Mode).
     */
    private static List<String> sdl2Flags() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("pkg-config", "--cflags", "--libs", "sdl2");
        pb.redirectErrorStream(false);
        Process process = pb.start();
        String out = readAll(process.getInputStream());
        int exitCode = process.waitFor();
        if (exitCode != 0 || out.isBlank()) {
            // Fallback razonable si pkg-config no está disponible.
            return List.of("-lSDL2");
        }
        return List.of(out.trim().split("\\s+"));
    }

    private static String readAll(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    private static String readFile(File file) throws IOException {
        return java.nio.file.Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Entrada de línea de comandos para validar la build a mano, sin
     * pasar por el PDE.
     *
     * Uso: java processing.mode.cpp.CppBuild <sketchFolder> <templateFile>
     *      <runtimeIncludeDir> <runtimeSrcDir> <buildDir>
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            System.err.println("Uso: CppBuild <sketchFolder> <templateFile> "
                + "<runtimeIncludeDir> <runtimeSrcDir> <buildDir>");
            System.exit(1);
        }

        File sketchFolder = new File(args[0]);
        File templateFile = new File(args[1]);
        File runtimeIncludeDir = new File(args[2]);
        File runtimeSrcDir = new File(args[3]);
        File buildDir = new File(args[4]);

        List<CppSketch.Tab> tabs = CppSketch.readTabs(sketchFolder);
        Result result = build(sketchFolder.getName(), tabs, buildDir,
            templateFile, runtimeIncludeDir, runtimeSrcDir);

        for (Problem problem : result.problems()) {
            System.out.println(problem);
        }

        if (result.success()) {
            System.out.println("OK: " + result.executable().getAbsolutePath());
        } else {
            System.err.println("FALLÓ la compilación.");
            System.exit(1);
        }
    }
}
