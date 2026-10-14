// SPDX-License-Identifier: Apache-2.0
package mathharness;

public final class ChildEntry {
    private ChildEntry() {}
    public static void main(String[] args) throws Throwable {
        if (args.length != 2) throw new IllegalArgumentException("phase class");
        if (args[0].equals("port")) CoreGuard.verify(System.err);
        else if (!args[0].equals("baseline")) throw new IllegalArgumentException("Unknown phase");
        try {
            Class.forName(args[1]).getMethod("main", String[].class).invoke(null, (Object) new String[0]);
        } catch (java.lang.reflect.InvocationTargetException failure) {
            throw failure.getCause();
        }
        // Keep guard/completion diagnostics away from the original success line.
        System.err.println("HARNESS_CHILD_COMPLETE " + args[0] + " " + args[1]);
    }
}
