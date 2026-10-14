// SPDX-License-Identifier: Apache-2.0
// Controlled replacement for upstream's weak first-stdout-line child check.
package mathharness;

import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public final class ChildJvm {
    private ChildJvm() {}
    public static void run(String testClass) throws Exception {
        String phase = System.getProperty("math.harness.phase");
        Path directory = Path.of(System.getProperty("math.harness.childDir"));
        Files.createDirectories(directory);
        Path stdout = directory.resolve("stdout.log");
        Path stderr = directory.resolve("stderr.log");
        Path classes = directory.resolve("class-load.log");
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        for (String option : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (option.equals("-ea") || option.equals("-XX:-OmitStackTraceInFastThrow") || option.startsWith("-Xmx")) command.add(option);
        }
        command.add("-Dseed=" + System.getProperty("seed"));
        if (System.getProperty("subset") != null) command.add("-Dsubset=" + System.getProperty("subset"));
        if (phase.equals("port")) command.add("-Dmath.harness.portJar=" + System.getProperty("math.harness.portJar"));
        command.add("-Xlog:class+load:file=" + classes);
        command.add("-cp"); command.add(System.getProperty("java.class.path"));
        command.add("mathharness.ChildEntry"); command.add(phase); command.add(testClass);
        Properties record = new Properties();
        record.setProperty("phase", phase);
        record.setProperty("class", testClass);
        record.setProperty("command.count", Integer.toString(command.size()));
        for (int i = 0; i < command.size(); i++) record.setProperty("command." + i, command.get(i));
        record.setProperty("stdout", stdout.toString()); record.setProperty("stderr", stderr.toString());
        record.setProperty("classLog", classes.toString());
        try (var stream = Files.newOutputStream(directory.resolve("result.properties"))) { record.store(stream, "Child command before execution"); }
        ProcessBuilder builder = new ProcessBuilder(command).directory(directory.toFile())
            .redirectOutput(stdout.toFile()).redirectError(stderr.toFile());
        for (String name : List.of("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "CLASSPATH")) builder.environment().remove(name);
        long start = System.nanoTime();
        Process process = builder.start();
        long timeout = Long.parseLong(System.getProperty("math.harness.childTimeoutSeconds"));
        boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
        if (!finished) {
            process.descendants().forEach(ProcessHandle::destroy);
            process.destroy();
            if (!process.waitFor(5, TimeUnit.SECONDS)) { process.destroyForcibly(); process.waitFor(); }
        }
        record.setProperty("timedOut", Boolean.toString(!finished));
        record.setProperty("exitCode", Integer.toString(process.exitValue()));
        record.setProperty("elapsedMillis", Long.toString((System.nanoTime() - start) / 1_000_000));
        String output = Files.readString(stdout);
        String diagnostics = Files.readString(stderr);
        boolean success = output.lines().filter(line -> !line.isBlank()).toList().equals(List.of("success"));
        boolean complete = diagnostics.contains("HARNESS_CHILD_COMPLETE " + phase + " " + testClass);
        boolean core = !phase.equals("port") || (Files.isRegularFile(classes) &&
            List.of("BigInteger", "BigDecimal", "MathContext", "RoundingMode").stream().allMatch(name -> {
                try { return diagnostics.contains("CORE_CLASS io.heapy.krogu.math." + name + " ") &&
                    Files.readString(classes).contains("io.heapy.krogu.math." + name); }
                catch (java.io.IOException failure) { return false; }
            }));
        record.setProperty("exactSuccessLine", Boolean.toString(success));
        record.setProperty("completionVerified", Boolean.toString(complete));
        record.setProperty("coreLoadingVerified", phase.equals("port") ? Boolean.toString(core) : "not-required-for-baseline");
        boolean passed = finished && process.exitValue() == 0 && success && complete && core;
        record.setProperty("status", passed ? "PASS" : "FAIL");
        try (var stream = Files.newOutputStream(directory.resolve("result.properties"))) { record.store(stream, "Child terminal result"); }
        if (!passed) throw new AssertionError("Nested math JVM did not prove exit/success/core loading: " + directory);
        System.out.println("HARNESS_CHILD_VERIFIED " + testClass);
    }
}
