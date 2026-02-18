/// usr/bin/env jbang "$0" "$@" ; exit $?

//JAVA 17+

//DEPS info.picocli:picocli:4.6.3

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "JUUIDTool", mixinStandardHelpOptions = true, version = "JUUIDTool 0.1",
        description = "Generate a UUID and copy it to the clipboard")
class JUUIDTool implements Callable<Integer> {

    @Option(names = {"-t", "--type"},
            description = "UUID type: 1 (time-based), 3 (MD5 name-based), 4 (random, default), 5 (SHA-1 name-based)",
            defaultValue = "4")
    private int uuidType;

    @Option(names = {"-n", "--name"},
            description = "Name for UUID v3/v5 generation (required for v3 and v5)")
    private String name;

    @Option(names = {"-ns", "--namespace"},
            description = "Namespace UUID for v3/v5 (defaults to DNS namespace)",
            defaultValue = "6ba7b810-9dad-11d1-80b4-00c04fd430c8")
    private String namespace;

    public static void main(String... args) {
        int exitCode = new CommandLine(new JUUIDTool()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        validateInput();
        String uuid = generateAndDisplayUUID();
        copyToClipboard(uuid);
        displayConfirmation();
        return 0;
    }

    private void validateInput() {
        if ((uuidType == 3 || uuidType == 5) && name == null) {
            throw new IllegalArgumentException("UUID v" + uuidType + " requires a name (use -n or --name)");
        }
    }

    private String generateAndDisplayUUID() {
        String uuid = UUIDGenerator.generate(uuidType, name, namespace);
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
     * Handles UUID generation logic for different versions
     */
    static class UUIDGenerator {
        static String generate(int version, String name, String namespace) {
            return switch (version) {
                case 1 -> generateV1();
                case 3 -> generateV3(name, namespace);
                case 4 -> generateV4();
                case 5 -> generateV5(name, namespace);
                default -> throw new IllegalArgumentException("Unsupported UUID version: " + version);
            };
        }

        private static String generateV1() {
            return TimeBasedUUIDGenerator.generate();
        }

        private static String generateV3(String name, String namespace) {
            return NameBasedUUIDGenerator.generateV3(name, namespace);
        }

        private static String generateV4() {
            return UUID.randomUUID().toString();
        }

        private static String generateV5(String name, String namespace) {
            return NameBasedUUIDGenerator.generateV5(name, namespace);
        }

        /**
         * Generates time-based UUIDs (Version 1)
         */
        static class TimeBasedUUIDGenerator {
            static String generate() {
                long timestamp = getCurrentTimestamp();
                long clockSequence = getClockSequence();
                long node = getNodeIdentifier();

                return constructV1UUID(timestamp, clockSequence, node);
            }

            private static long getCurrentTimestamp() {
                // UUID v1 uses 100-nanosecond intervals since October 15, 1582
                long currentTime = System.currentTimeMillis();
                return (currentTime * 10000) + 0x01B21DD213814000L;
            }

            private static long getClockSequence() {
                return (long) (Math.random() * 0x3FFF);
            }

            private static long getNodeIdentifier() {
                // Use random multicast node (LSB = 1) as per RFC 4122
                long node = (long) (Math.random() * 0xFFFFFFFFFFFFL);
                return node | 0x010000000000L;
            }

            private static String constructV1UUID(long timestamp, long clockSequence, long node) {
                long timeLow = timestamp & 0xFFFFFFFFL;
                long timeMid = (timestamp >> 32) & 0xFFFFL;
                long timeHiAndVersion = ((timestamp >> 48) & 0x0FFFL) | 0x1000L;
                long clockSeqHiAndReserved = ((clockSequence >> 8) & 0x3FL) | 0x80L;
                long clockSeqLow = clockSequence & 0xFFL;

                return formatUUID(timeLow, timeMid, timeHiAndVersion,
                        clockSeqHiAndReserved, clockSeqLow, node);
            }

            private static String formatUUID(long timeLow, long timeMid, long timeHiAndVersion,
                                             long clockSeqHiAndReserved, long clockSeqLow, long node) {
                return String.format("%08x-%04x-%04x-%02x%02x-%012x",
                        timeLow, timeMid, timeHiAndVersion,
                        clockSeqHiAndReserved, clockSeqLow, node);
            }
        }

        /**
         * Generates name-based UUIDs (Version 3 and 5)
         */
        static class NameBasedUUIDGenerator {
            private static final UUID DNS_NAMESPACE = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

            static String generateV3(String name, String namespace) {
                return generateNameBased(name, namespace, "MD5", 3);
            }

            static String generateV5(String name, String namespace) {
                return generateNameBased(name, namespace, "SHA-1", 5);
            }

            private static String generateNameBased(String name, String namespace, String algorithm, int version) {
                UUID namespaceUUID = parseNamespace(namespace);
                byte[] hash = computeHash(namespaceUUID, name, algorithm);
                return constructNameBasedUUID(hash, version);
            }

            private static UUID parseNamespace(String namespace) {
                try {
                    return UUID.fromString(namespace);
                } catch (IllegalArgumentException e) {
                    return DNS_NAMESPACE;
                }
            }

            private static byte[] computeHash(UUID namespace, String name, String algorithm) {
                try {
                    MessageDigest digest = MessageDigest.getInstance(algorithm);
                    updateDigestWithNamespace(digest, namespace);
                    updateDigestWithName(digest, name);
                    return digest.digest();
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("Hash algorithm not available: " + algorithm, e);
                }
            }

            private static void updateDigestWithNamespace(MessageDigest digest, UUID namespace) {
                digest.update(toBytes(namespace.getMostSignificantBits()));
                digest.update(toBytes(namespace.getLeastSignificantBits()));
            }

            private static void updateDigestWithName(MessageDigest digest, String name) {
                digest.update(name.getBytes(StandardCharsets.UTF_8));
            }

            private static byte[] toBytes(long value) {
                byte[] bytes = new byte[8];
                for (int i = 7; i >= 0; i--) {
                    bytes[i] = (byte) (value & 0xFF);
                    value >>= 8;
                }
                return bytes;
            }

            private static String constructNameBasedUUID(byte[] hash, int version) {
                long msb = extractMostSignificantBits(hash, version);
                long lsb = extractLeastSignificantBits(hash);
                return new UUID(msb, lsb).toString();
            }

            private static long extractMostSignificantBits(byte[] hash, int version) {
                long msb = 0;
                for (int i = 0; i < 8; i++) {
                    msb = (msb << 8) | (hash[i] & 0xFF);
                }
                msb &= ~(0xF000L << 48);
                msb |= ((long) version << 12) << 48;
                return msb;
            }

            private static long extractLeastSignificantBits(byte[] hash) {
                long lsb = 0;
                for (int i = 8; i < 16; i++) {
                    lsb = (lsb << 8) | (hash[i] & 0xFF);
                }
                lsb &= ~(0xC000000000000000L);
                lsb |= 0x8000000000000000L;
                return lsb;
            }
        }
    }

    /**
     * Handles cross-platform clipboard operations with fallback strategies
     */
    static class ClipboardManager {
        static void copy(String text) {
            boolean success = tryAWTClipboard(text) || tryPlatformSpecificClipboard(text);
            if (!success) {
                System.err.println("Warning: Could not copy to clipboard");
            }
        }

        private static boolean tryAWTClipboard(String text) {
            return AWTClipboardStrategy.copy(text);
        }

        private static boolean tryPlatformSpecificClipboard(String text) {
            return PlatformClipboardStrategy.copy(text);
        }

        /**
         * AWT-based clipboard strategy (works on desktop environments)
         */
        static class AWTClipboardStrategy {
            static boolean copy(String text) {
                if (isHeadless()) {
                    return false;
                }
                return tryCopyToAWTClipboard(text);
            }

            private static boolean isHeadless() {
                return GraphicsEnvironment.isHeadless();
            }

            private static boolean tryCopyToAWTClipboard(String text) {
                try {
                    copyToSystemClipboard(text);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            private static void copyToSystemClipboard(String text) {
                StringSelection selection = new StringSelection(text);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            }
        }

        /**
         * Platform-specific command-based clipboard strategy (fallback)
         */
        static class PlatformClipboardStrategy {
            static boolean copy(String text) {
                String command = getClipboardCommand();
                if (command == null) {
                    return false;
                }
                return executeClipboardCommand(command, text);
            }

            private static String getClipboardCommand() {
                return OSDetector.getClipboardCommand();
            }

            private static boolean executeClipboardCommand(String command, String text) {
                try {
                    Process process = startProcess(command);
                    writeToProcess(process, text);
                    return waitForCompletion(process);
                } catch (Exception e) {
                    return false;
                }
            }

            private static Process startProcess(String command) throws IOException {
                return new ProcessBuilder(command.split(" "))
                        .redirectErrorStream(true)
                        .start();
            }

            private static void writeToProcess(Process process, String text) throws IOException {
                process.getOutputStream().write(text.getBytes());
                process.getOutputStream().close();
            }

            private static boolean waitForCompletion(Process process) throws InterruptedException {
                return process.waitFor() == 0;
            }
        }

        /**
         * Detects the operating system and provides appropriate clipboard commands
         */
        static class OSDetector {
            private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

            static String getClipboardCommand() {
                if (isWindows()) {
                    return "clip.exe";
                } else if (isMac()) {
                    return "pbcopy";
                } else if (isLinux()) {
                    return getLinuxClipboardCommand();
                }
                return null;
            }

            private static boolean isWindows() {
                return OS_NAME.contains("win");
            }

            private static boolean isMac() {
                return OS_NAME.contains("mac");
            }

            private static boolean isLinux() {
                return OS_NAME.contains("nux") || OS_NAME.contains("nix");
            }

            private static String getLinuxClipboardCommand() {
                if (commandExists("xclip")) {
                    return "xclip -selection clipboard";
                } else if (commandExists("xsel")) {
                    return "xsel --clipboard --input";
                }
                return null;
            }

            private static boolean commandExists(String command) {
                try {
                    Process process = new ProcessBuilder("which", command).start();
                    return process.waitFor() == 0;
                } catch (Exception e) {
                    return false;
                }
            }
        }
    }
}
