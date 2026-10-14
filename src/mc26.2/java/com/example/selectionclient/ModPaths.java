package com.example.selectionclient;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;
public final class ModPaths {
    private ModPaths() {}
    public static Path getStructuresDir() { return FabricLoader.getInstance().getGameDir().resolve("selection-client/structures"); }
    public static Path getTxtsDir() { return getStructuresDir().resolve("txts"); }
    public static Path getNbtsDir() { return getStructuresDir().resolve("nbts"); }
    private static final java.util.regex.Pattern ILLEGAL_FILENAME_CHARS =
            java.util.regex.Pattern.compile("[\\\\/:*?\"<>|\\x00-\\x1F]");

    /** 文件名（不含扩展名）的最大长度，避免过长文件名在某些文件系统上报错。 */
    private static final int MAX_FILENAME_LENGTH = 100;

    /**
     * 清理用户输入的文件名：只移除文件系统真正不允许的非法字符（如 {@code / \ : * ? " < > |}），
     * 保留中文、日文等 Unicode 文字、空格及大部分常见符号，避免把中文或特殊符号误替换为下划线。
     *
     * @param name       原始文件名（不含扩展名）
     * @param fallback   清理后为空时使用的默认名称
     * @return 清理后的安全文件名
     */
    public static String sanitizeFileName(String name, String fallback) {
        if (name == null) {
            return fallback;
        }
        String cleaned = ILLEGAL_FILENAME_CHARS.matcher(name.trim()).replaceAll("_");
        // 折叠连续空白为单个空格，避免异常排版
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        // 去除首尾的点号（Windows 上末尾的点会被自动去除，可能引发歧义）
        cleaned = cleaned.replaceAll("^\\.+|\\.+$", "");
        if (cleaned.length() > MAX_FILENAME_LENGTH) {
            cleaned = cleaned.substring(0, MAX_FILENAME_LENGTH);
        }
        return cleaned.isEmpty() ? fallback : cleaned;
    }
}
