package processing.mode.cpp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * CppSketch.java
 *
 * Preprocesador: envuelve el/los .cpp del usuario (que solo definen
 * setup() y draw(), sin main()) en el archivo real a compilar,
 * inyectando main() a partir de runtime/src/main_template.cpp.in.
 *
 * Deliberadamente no depende de las clases del PDE (Editor, Mode,
 * Sketch...): es lógica de texto/archivos pura, así se puede validar de
 * forma aislada (ver main() más abajo) antes de la integración con el
 * PDE (Fase 3), que es quien la invocará: CppEditor lee las tabs en
 * memoria (con ediciones sin guardar incluidas, vía SketchCode.getProgram())
 * y CppBuild usa Preprocessed.locate() para mapear los errores del
 * compilador de vuelta a la tab/línea originales.
 */
public class CppSketch {

    /** Línea del template que se sustituye por el código del usuario. */
    public static final String USER_CODE_MARKER = "// {{USER_SKETCH_CODE}}";

    /** Una tab de código fuente del sketch: nombre de archivo + contenido. */
    public record Tab(String name, String content) {}

    /** Ubicación de una línea del archivo generado dentro del sketch original. */
    public record TabLocation(String tabName, int line) {}

    private record TabRange(String tabName, int startLine, int lineCount) {}

    /**
     * Resultado de preprocesar un sketch: el .cpp generado listo para
     * compilar, más la información necesaria para mapear los números de
     * línea que reporte el compilador de vuelta a la tab y línea
     * originales del sketch (ver locate()).
     */
    public static class Preprocessed {
        public final String source;
        private final List<TabRange> ranges;

        private Preprocessed(String source, List<TabRange> ranges) {
            this.source = source;
            this.ranges = ranges;
        }

        /**
         * Traduce una línea del archivo generado (1-indexado) a la tab y
         * línea del sketch correspondientes. Devuelve null si la línea
         * cae fuera del código de usuario (pertenece a la plantilla:
         * el #include, el using-directive o el main() inyectados).
         */
        public TabLocation locate(int generatedLine) {
            for (TabRange range : ranges) {
                int offset = generatedLine - range.startLine();
                if (offset >= 0 && offset < range.lineCount()) {
                    return new TabLocation(range.tabName(), offset + 1);
                }
            }
            return null;
        }
    }

    /**
     * Sustituye el marcador del template por las tabs de usuario
     * concatenadas (tab principal primero, resto en orden alfabético,
     * igual que hace el PDE con tabs .pde), registrando en qué línea del
     * resultado empieza cada una.
     *
     * @throws IllegalStateException si el template no contiene el marcador.
     */
    public static Preprocessed preprocess(String templateSource, List<Tab> tabs) {
        int markerLine = -1;
        String[] templateLines = templateSource.split("\n", -1);
        for (int i = 0; i < templateLines.length; i++) {
            if (templateLines[i].equals(USER_CODE_MARKER)) {
                markerLine = i;
                break;
            }
        }
        if (markerLine == -1) {
            throw new IllegalStateException(
                "El template no contiene el marcador " + USER_CODE_MARKER);
        }

        List<Tab> ordered = sortTabs(tabs);

        StringBuilder userCode = new StringBuilder();
        List<TabRange> ranges = new ArrayList<>();
        // La primera línea de código de usuario cae en la propia línea del
        // marcador (1-indexado: markerLine líneas de template la preceden).
        int nextLine = markerLine + 1;
        for (Tab tab : ordered) {
            String content = tab.content();
            String[] tabLines = content.split("\n", -1);
            // split("\n", -1) produce siempre (nº de '\n' en content) + 1
            // trozos; si content termina en '\n' (caso normal de un
            // archivo guardado), el último trozo es "" y no es una línea
            // real, así que no debe contar como línea del sketch.
            int newlineCount = tabLines.length - 1;
            int lineCount = content.endsWith("\n") ? newlineCount : tabLines.length;

            if (userCode.length() > 0) {
                userCode.append("\n\n");
                nextLine += 2;
            }
            userCode.append("// --- ").append(tab.name()).append(" ---\n");
            nextLine += 1;
            // Solo ahora nextLine apunta a la primera línea real del
            // contenido de la tab (tras el comentario "// --- name ---").
            ranges.add(new TabRange(tab.name(), nextLine, lineCount));
            userCode.append(content);
            nextLine += newlineCount;
        }

        String generated = templateSource.replace(USER_CODE_MARKER, userCode.toString());
        return new Preprocessed(generated, ranges);
    }

    private static List<Tab> sortTabs(List<Tab> tabs) {
        List<Tab> ordered = new ArrayList<>(tabs);
        // La tab "main" (la única con un nombre que no viene de otra tab
        // explícita) no se distingue aquí por convención de nombre: quien
        // llama (CppEditor, o readTabs() más abajo) es responsable de
        // pasar la tab principal primero si el orden importa. Como
        // salvaguarda, se ordena el resto alfabéticamente detrás de ella.
        if (ordered.size() > 1) {
            Tab first = ordered.get(0);
            List<Tab> rest = new ArrayList<>(ordered.subList(1, ordered.size()));
            rest.sort((a, b) -> a.name().compareTo(b.name()));
            ordered = new ArrayList<>();
            ordered.add(first);
            ordered.addAll(rest);
        }
        return ordered;
    }

    /**
     * Lee las tabs de un sketch desde disco: primero las .cpp (tab
     * principal — &lt;sketchFolder.getName()&gt;.cpp, si existe —
     * primero, resto en orden alfabético), luego las .h/.hpp (headers
     * auxiliares, orden alfabético; no se concatenan como las .cpp, ver
     * CppBuild.build()).
     */
    public static List<Tab> readTabs(File sketchFolder) throws IOException {
        String sketchName = sketchFolder.getName();

        File[] cppFiles = sketchFolder.listFiles((dir, name) -> name.endsWith(".cpp"));
        if (cppFiles == null || cppFiles.length == 0) {
            throw new IOException("No se encontraron archivos .cpp en " + sketchFolder);
        }
        List<File> ordered = new ArrayList<>(List.of(cppFiles));
        ordered.sort((a, b) -> {
            boolean aMain = isMainTab(a, sketchName);
            boolean bMain = isMainTab(b, sketchName);
            if (aMain != bMain) {
                return aMain ? -1 : 1;
            }
            return a.getName().compareTo(b.getName());
        });

        File[] headerFiles = sketchFolder.listFiles(
            (dir, name) -> name.endsWith(".h") || name.endsWith(".hpp"));
        if (headerFiles != null) {
            List<File> headers = new ArrayList<>(List.of(headerFiles));
            headers.sort((a, b) -> a.getName().compareTo(b.getName()));
            ordered.addAll(headers);
        }

        List<Tab> tabs = new ArrayList<>();
        for (File file : ordered) {
            tabs.add(new Tab(file.getName(), readFile(file)));
        }
        return tabs;
    }

    private static boolean isMainTab(File file, String sketchName) {
        String name = file.getName();
        String base = name.substring(0, name.length() - ".cpp".length());
        return base.equals(sketchName);
    }

    /** Combina readTabs() + preprocess() a partir de un sketch en disco. */
    public static Preprocessed preprocessSketch(File sketchFolder, File templateFile)
            throws IOException {
        List<Tab> tabs = readTabs(sketchFolder);
        String templateSource = readFile(templateFile);
        return preprocess(templateSource, tabs);
    }

    /** Escribe el resultado en outputDir/&lt;sketchName&gt;.cpp y devuelve el archivo. */
    public static File writeGenerated(Preprocessed preprocessed, File outputDir, String sketchName)
            throws IOException {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("No se pudo crear el directorio de build " + outputDir);
        }
        File outputFile = new File(outputDir, sketchName + ".cpp");
        Files.write(outputFile.toPath(), preprocessed.source.getBytes(StandardCharsets.UTF_8));
        return outputFile;
    }

    private static String readFile(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Entrada de línea de comandos para validar el preprocesador a mano,
     * sin pasar por el PDE: genera el .cpp de un sketch y escribe la
     * ruta resultante en stdout.
     *
     * Uso: java processing.mode.cpp.CppSketch &lt;sketchFolder&gt;
     *      &lt;templateFile&gt; &lt;outputDir&gt;
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println(
                "Uso: CppSketch <sketchFolder> <templateFile> <outputDir>");
            System.exit(1);
        }

        File sketchFolder = new File(args[0]);
        File templateFile = new File(args[1]);
        File outputDir = new File(args[2]);

        Preprocessed preprocessed = preprocessSketch(sketchFolder, templateFile);
        File outputFile = writeGenerated(preprocessed, outputDir, sketchFolder.getName());
        System.out.println(outputFile.getAbsolutePath());
    }
}
