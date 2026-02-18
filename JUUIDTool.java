///usr/bin/env jbang "$0" "$@" ; exit $?

//JAVA 17+

//DEPS info.picocli:picocli:4.6.3

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "JUUIDTool", mixinStandardHelpOptions = true, version = "JUUIDTool 0.1",
        description = "Generate a random UUID and copy it to the clipboard")
class JUUIDTool implements Callable<Integer> {

    public static void main(String... args) {
        int exitCode = new CommandLine(new JUUIDTool()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        String uuid = generateAndDisplayUUID();
        copyToClipboard(uuid);
        displayConfirmation();
        return 0;
    }

    private String generateAndDisplayUUID() {
        String uuid = UUIDGenerator.generate();
        System.out.println(uuid);
        return uuid;
    }

    private void copyToClipboard(String text) {
        ClipboardManager.copy(text);
    }

    private void displayConfirmation() {
        System.out.println("UUID copied to clipboard!");
    }

    /**
     * Handles UUID generation logic
     */
    static class UUIDGenerator {
        static String generate() {
            return UUID.randomUUID().toString();
        }
    }

    /**
     * Handles clipboard operations
     */
    static class ClipboardManager {
        static void copy(String text) {
            StringSelection selection = createStringSelection(text);
            copyToSystemClipboard(selection);
        }

        private static StringSelection createStringSelection(String text) {
            return new StringSelection(text);
        }

        private static void copyToSystemClipboard(StringSelection selection) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }
    }
}
