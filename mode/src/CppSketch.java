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
 * PDE (Fase 3), que es quien la invocará desde CppBuild.
 */
public class CppSketch {

    /** Línea del template que se sustituye por el código del usuario. */
    public static final String USER_CODE_MARKER = "// {{USER_SKETCH_CODE}}";

    /**
     * Sustituye el marcador del template por el código de usuario.
     *
     * @throws IllegalStateException si el template no contiene el marcador.
     */
    public static String preprocess(String templateSource, String userCode) {
        if (!templateSource.contains(USER_CODE_MARKER)) {
            throw new IllegalStateException(
                "El template no contiene el marcador " + USER_CODE_MARKER);
        }
        return templateSource.replace(USER_CODE_MARKER, userCode);
    }

    /**
     * Concatena las tabs .cpp de un sketch en un único bloque de código,
     * igual que hace el PDE con las tabs .pde: la tab principal
     * (&lt;sketchName&gt;.cpp, si existe) va primero, el resto en orden
     * alfabético.
     */
    public static String concatenateTabs(File sketchFolder) throws IOException {
        String sketchName = sketchFolder.getName();
        File[] found = sketchFolder.listFiles((dir, name) -> name.endsWith(".cpp"));
        if (found == null || found.length == 0) {
            throw new IOException("No se encontraron archivos .cpp en " + sketchFolder);
        }

        List<File> tabs = new ArrayList<>(List.of(found));
        tabs.sort((a, b) -> {
            boolean aMain = isMainTab(a, sketchName);
            boolean bMain = isMainTab(b, sketchName);
            if (aMain != bMain) {
                return aMain ? -1 : 1;
            }
            return a.getName().compareTo(b.getName());
        });

        StringBuilder combined = new StringBuilder();
        for (File tab : tabs) {
            if (combined.length() > 0) {
                combined.append("\n\n");
            }
            combined.append("// --- ").append(tab.getName()).append(" ---\n");
            combined.append(readFile(tab));
        }
        return combined.toString();
    }

    private static boolean isMainTab(File file, String sketchName) {
        String name = file.getName();
        String base = name.substring(0, name.length() - ".cpp".length());
        return base.equals(sketchName);
    }

    /**
     * Genera el .cpp real a compilar a partir de las tabs de un sketch y
     * el template, y lo escribe en outputDir/&lt;sketchName&gt;.cpp.
     *
     * @return el archivo generado.
     */
    public static File preprocessSketch(File sketchFolder, File templateFile, File outputDir)
            throws IOException {
        String userCode = concatenateTabs(sketchFolder);
        String templateSource = readFile(templateFile);
        String generated = preprocess(templateSource, userCode);

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("No se pudo crear el directorio de build " + outputDir);
        }

        File outputFile = new File(outputDir, sketchFolder.getName() + ".cpp");
        Files.write(outputFile.toPath(), generated.getBytes(StandardCharsets.UTF_8));
        return outputFile;
    }

    private static String readFile(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Entrada de línea de comandos para validar el preprocesador a mano,
     * sin pasar por el PDE (Fase 2): genera el .cpp de un sketch y
     * escribe la ruta resultante en stdout.
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

        File outputFile = preprocessSketch(sketchFolder, templateFile, outputDir);
        System.out.println(outputFile.getAbsolutePath());
    }
}
