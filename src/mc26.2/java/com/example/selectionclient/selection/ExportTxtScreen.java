package com.example.selectionclient.selection;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * P4 - 导出 TXT 页面：填写保存路径和文件名，点击"导出.txt"执行导出。
 * 在客户端本地保存方块和状态，不包含容器内容。
 */
public class ExportTxtScreen extends Screen {

    private final Screen parent;
    private final SelectionAnalyzer.AnalysisResult result;

    private EditBox pathField;
    private EditBox nameField;
    private final java.util.Set<String> ignoredBlocks; // 被忽略（不记录）的方块种类

    private static final Path TXTS_DIR = com.example.selectionclient.ModPaths.getTxtsDir();

    // 弹窗尺寸
    private static final int POPUP_WIDTH = 260;
    private static final int POPUP_HEIGHT = 160;

    public ExportTxtScreen(Screen parent, SelectionAnalyzer.AnalysisResult result) {
        this(parent, result, java.util.Collections.emptySet());
    }

    public ExportTxtScreen(Screen parent, SelectionAnalyzer.AnalysisResult result, java.util.Set<String> ignoredBlocks) {
        super(Component.literal(com.example.selectionclient.I18n.tr("export.txt.title")));
        this.parent = parent;
        this.result = result;
        this.ignoredBlocks = ignoredBlocks == null ? java.util.Collections.emptySet() : ignoredBlocks;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int popLeft = cx - POPUP_WIDTH / 2;
        int popTop = cy - POPUP_HEIGHT / 2;

        int fieldW = POPUP_WIDTH - 40;
        int fieldLeft = popLeft + 20;

        // 保存路径输入
        int pathY = popTop + 28;
        this.addRenderableWidget(Button.builder(Component.literal("📁"), button -> openFolderChooser())
                .bounds(fieldLeft, pathY, 20, 18).build());
        pathField = new EditBox(this.font, fieldLeft + 22, pathY, fieldW - 22, 18, Component.literal(com.example.selectionclient.I18n.tr("export.txt.path")));
        pathField.setValue("");
        pathField.setHint(Component.literal(com.example.selectionclient.I18n.tr("export.txt.path.placeholder")));
        pathField.setMaxLength(512);
        this.addRenderableWidget(pathField);

        // 文件名输入
        int nameY = pathY + 24;
        nameField = new EditBox(this.font, fieldLeft, nameY, fieldW, 18, Component.literal(com.example.selectionclient.I18n.tr("export.txt.name")));
        nameField.setValue("exported_blueprint");
        nameField.setMaxLength(64);
        this.addRenderableWidget(nameField);

        // 导出按钮
        int btnY = nameY + 24;
        this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("export.txt.button")), button -> doExportTxt())
                .bounds(fieldLeft, btnY, fieldW, 20).build());

        // 返回按钮
        int cancelY = btnY + 26;
        this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("button.back")), button -> onClose())
                .bounds(cx - 40, cancelY, 80, 20).build());
    }



    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {

        int cx = this.width / 2;
        int cy = this.height / 2;
        int popLeft = cx - POPUP_WIDTH / 2;
        int popTop = cy - POPUP_HEIGHT / 2;

        // 弹窗背景
        context.fill(popLeft - 2, popTop - 2, popLeft + POPUP_WIDTH + 2, popTop + POPUP_HEIGHT + 2, 0xFFAAAAAA);
        context.fill(popLeft, popTop, popLeft + POPUP_WIDTH, popTop + POPUP_HEIGHT, 0xFF000000);

        // 标题
        context.centeredText(this.font, this.title, cx, popTop + 10, 0xFFFFFFFF);

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    private void doExportTxt() {
        String name = nameField.getValue().trim();
        if (name.isEmpty()) name = "exported_blueprint";
        String pathText = pathField.getValue().trim();
        try {
            Path dir = pathText.isEmpty() ? TXTS_DIR : Path.of(pathText);
            Files.createDirectories(dir);
            Path filePath = dir.resolve(com.example.selectionclient.ModPaths.sanitizeFileName(name, "exported_blueprint") + ".txt");
            SelectionAnalyzer.AnalysisResult filtered = SelectionAnalyzer.filterBlocks(result, ignoredBlocks);
            Files.writeString(filePath, SelectionAnalyzer.exportBlueprintV2(filtered, name), StandardCharsets.UTF_8);
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal(com.example.selectionclient.I18n.tr(
                        "export.txt.exported", filePath.toAbsolutePath())));
            }
            onClose();
        } catch (java.io.IOException | IllegalArgumentException e) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal(com.example.selectionclient.I18n.tr(
                        "export.txt.export_failed", e.getMessage())));
            }
        }
    }

    /**
     * 打开原生文件夹选择对话框，选中后将路径填入 pathField。
     */
    private void openFolderChooser() {
        // 默认打开 txts 目录
        Path defaultDir = TXTS_DIR.toAbsolutePath().normalize();
        if (!Files.isDirectory(defaultDir)) {
            defaultDir = Path.of(System.getProperty("user.home"));
        }
        String selected = TinyFileDialogs.tinyfd_selectFolderDialog(
                com.example.selectionclient.I18n.tr("export.txt.choose_folder"), defaultDir.toString());
        if (selected != null) {
            pathField.setValue(selected);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
