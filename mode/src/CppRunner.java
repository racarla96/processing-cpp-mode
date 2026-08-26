package processing.mode.cpp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * CppRunner.java
 *
 * Lanza y gestiona el ciclo de vida del proceso compilado (Play/Stop):
 * arranca el binario resultante de CppBuild y lo mata cuando el usuario
 * pulsa Stop o se recompila el sketch. No depende de las clases del
 * PDE: es un wrapper fino sobre ProcessBuilder, testable de forma
 * aislada (ver main() al final).
 */
public class CppRunner {

    /** Recibe la salida del proceso en vivo y su código de salida al terminar. */
    public interface OutputListener {
        void onOutput(String line, boolean isError);
        void onExit(int exitCode);
    }

    private Process process;

    /**
     * Arranca executable. Si ya había un proceso corriendo (de una
     * ejecución anterior), lo mata primero.
     */
    public synchronized void start(File executable, OutputListener listener) throws IOException {
        stop();

        ProcessBuilder pb = new ProcessBuilder(executable.getAbsolutePath());
        pb.directory(executable.getParentFile());
        process = pb.start();

        pipe(process.getInputStream(), false, listener);
        pipe(process.getErrorStream(), true, listener);

        Process started = process;
        Thread waiter = new Thread(() -> {
            try {
                int code = started.waitFor();
                if (listener != null) {
                    listener.onExit(code);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "cpp-runner-wait");
        waiter.setDaemon(true);
        waiter.start();
    }

    private void pipe(InputStream in, boolean isError, OutputListener listener) {
        Thread pump = new Thread(() -> {
            try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (listener != null) {
                        listener.onOutput(line, isError);
                    }
                }
            } catch (IOException ignored) {
                // El stream se cierra cuando el proceso termina; nada que hacer.
            }
        }, isError ? "cpp-runner-stderr" : "cpp-runner-stdout");
        pump.setDaemon(true);
        pump.start();
    }

    public synchronized boolean isRunning() {
        return process != null && process.isAlive();
    }

    /** Termina el proceso en marcha, si lo hay. No falla si ya había terminado. */
    public synchronized void stop() {
        if (process != null && process.isAlive()) {
            process.destroy();
            try {
                if (!process.waitFor(2, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }
        process = null;
    }

    /**
     * Entrada de línea de comandos para validar el runner a mano: arranca
     * el binario, deja que corra unos segundos y lo termina.
     *
     * Uso: java processing.mode.cpp.CppRunner <executable> [segundos]
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Uso: CppRunner <executable> [segundos]");
            System.exit(1);
        }

        File executable = new File(args[0]);
        long seconds = args.length >= 2 ? Long.parseLong(args[1]) : 4;

        CppRunner runner = new CppRunner();
        runner.start(executable, new OutputListener() {
            @Override
            public void onOutput(String line, boolean isError) {
                (isError ? System.err : System.out).println(line);
            }

            @Override
            public void onExit(int exitCode) {
                System.out.println("[CppRunner] proceso terminado, código " + exitCode);
            }
        });

        Thread.sleep(seconds * 1000);
        System.out.println("[CppRunner] deteniendo el proceso...");
        runner.stop();
        System.out.println("[CppRunner] isRunning() = " + runner.isRunning());
    }
}
