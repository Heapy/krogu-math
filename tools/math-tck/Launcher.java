// SPDX-License-Identifier: Apache-2.0
package mathharness;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.junit.platform.console.ConsoleLauncher;

public final class Launcher {
    private Launcher() {}
    public static void main(String[] args) throws Throwable {
        if (args.length < 3) throw new IllegalArgumentException("phase runner class [main arguments]");
        System.setProperty("math.harness.phase", args[0]);
        if (args[0].equals("port")) CoreGuard.verify();
        else if (!args[0].equals("baseline")) throw new IllegalArgumentException("Unknown phase");
        Class<?> test = Class.forName(args[2]);
        switch (args[1]) {
            case "main" -> {
                String methods = System.getProperty("math.harness.methods", "");
                if (methods.isEmpty()) {
                    invoke(test.getMethod("main", String[].class), (Object) Arrays.copyOfRange(args, 3, args.length));
                } else {
                    for (String name : methods.split(",")) {
                        var method = test.getDeclaredMethod(name);
                        method.setAccessible(true);
                        invoke(method);
                        System.out.println("HARNESS_CASE PASS " + name);
                    }
                }
            }
            case "junit" -> {
                var result = ConsoleLauncher.run(new java.io.PrintWriter(System.out, true), new java.io.PrintWriter(System.err, true),
                    "execute", "--select-class=" + test.getName(), "--fail-if-no-tests",
                    "--reports-dir=" + System.getProperty("math.harness.reports"));
                if (result.getExitCode() != 0) throw new AssertionError("JUnit exit code " + result.getExitCode());
            }
            case "testng" -> {
                AtomicInteger executed = new AtomicInteger();
                AtomicInteger failed = new AtomicInteger();
                AtomicInteger skipped = new AtomicInteger();
                TestNG runner = new TestNG();
                runner.setUseDefaultListeners(false);
                runner.setTestClasses(new Class<?>[] {test});
                runner.setOutputDirectory(System.getProperty("math.harness.reports"));
                runner.addListener(new ITestListener() {
                    private String name(ITestResult r) { return r.getName() + Arrays.deepToString(r.getParameters()); }
                    public void onTestSuccess(ITestResult r) { executed.incrementAndGet(); System.out.println("HARNESS_CASE PASS " + name(r)); }
                    public void onTestFailure(ITestResult r) { executed.incrementAndGet(); failed.incrementAndGet(); System.out.println("HARNESS_CASE FAIL " + name(r)); r.getThrowable().printStackTrace(); }
                    public void onTestSkipped(ITestResult r) { skipped.incrementAndGet(); System.out.println("HARNESS_CASE SKIP " + name(r)); }
                });
                runner.run();
                if (runner.getStatus() != 0 || executed.get() == 0 || failed.get() != 0 || skipped.get() != 0)
                    throw new AssertionError("TestNG status=" + runner.getStatus() + " executed=" + executed + " failed=" + failed + " skipped=" + skipped);
            }
            default -> throw new IllegalArgumentException("Unknown runner " + args[1]);
        }
        System.out.println("HARNESS_COMPLETE " + args[0] + " " + args[2]);
    }
    private static void invoke(java.lang.reflect.Method method, Object... args) throws Throwable {
        try { method.invoke(null, args); }
        catch (InvocationTargetException failure) { throw failure.getCause(); }
    }
}
