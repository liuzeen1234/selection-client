package com.example.selectionclient.selection;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 选区工具界面：输入两个坐标，确认后高亮。
 * 退出时自动保留输入框内容（草稿）。
 */
public class SelectionScreen extends Screen {

    private final Screen parent;
    private EditBox x1Field, y1Field, z1Field;
    private EditBox x2Field, y2Field, z2Field;

    public SelectionScreen(Screen parent) {
        super(Component.literal(com.example.selectionclient.I18n.tr("selection.title")));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int fieldW = 50;
        int gap = 8;
        int totalW = fieldW * 3 + gap * 2;
        int leftX = cx - totalW / 2;
        SelectionManager mgr = SelectionManager.getInstance();

        // --- 坐标 1 ---
        int fieldY1 = coordinatesY();
        x1Field = new EditBox(this.font, leftX, fieldY1, fieldW, 18, Component.literal("X1"));
        y1Field = new EditBox(this.font, leftX + fieldW + gap, fieldY1, fieldW, 18, Component.literal("Y1"));
        z1Field = new EditBox(this.font, leftX + (fieldW + gap) * 2, fieldY1, fieldW, 18, Component.literal("Z1"));

        // 恢复草稿或已确认坐标
        if (mgr.hasDraft()) {
            x1Field.setValue(mgr.getDraftX1());
            y1Field.setValue(mgr.getDraftY1());
            z1Field.setValue(mgr.getDraftZ1());
        } else if (mgr.getPos1() != null) {
            x1Field.setValue(String.valueOf(mgr.getPos1().getX()));
            y1Field.setValue(String.valueOf(mgr.getPos1().getY()));
            z1Field.setValue(String.valueOf(mgr.getPos1().getZ()));
        }
        this.addRenderableWidget(x1Field);
        this.addRenderableWidget(y1Field);
        this.addRenderableWidget(z1Field);

        // --- 坐标 2 ---
        int fieldY2 = fieldY1 + 36;
        x2Field = new EditBox(this.font, leftX, fieldY2, fieldW, 18, Component.literal("X2"));
        y2Field = new EditBox(this.font, leftX + fieldW + gap, fieldY2, fieldW, 18, Component.literal("Y2"));
        z2Field = new EditBox(this.font, leftX + (fieldW + gap) * 2, fieldY2, fieldW, 18, Component.literal("Z2"));

        if (mgr.hasDraft()) {
            x2Field.setValue(mgr.getDraftX2());
            y2Field.setValue(mgr.getDraftY2());
            z2Field.setValue(mgr.getDraftZ2());
        } else if (mgr.getPos2() != null) {
            x2Field.setValue(String.valueOf(mgr.getPos2().getX()));
            y2Field.setValue(String.valueOf(mgr.getPos2().getY()));
            z2Field.setValue(String.valueOf(mgr.getPos2().getZ()));
        }
        this.addRenderableWidget(x2Field);
        this.addRenderableWidget(y2Field);
        this.addRenderableWidget(z2Field);

        // --- 快捷按钮：当前位置 ---
        int quickY = fieldY2 + 26;
        this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("selection.pos1.current")), button -> {
            if (this.minecraft != null && this.minecraft.player != null) {
                BlockPos pos = this.minecraft.player.blockPosition();
                x1Field.setValue(String.valueOf(pos.getX()));
                y1Field.setValue(String.valueOf(pos.getY()));
                z1Field.setValue(String.valueOf(pos.getZ()));
            }
        }).bounds(cx - totalW / 2, quickY, totalW / 2 - 2, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("selection.pos2.current")), button -> {
            if (this.minecraft != null && this.minecraft.player != null) {
                BlockPos pos = this.minecraft.player.blockPosition();
                x2Field.setValue(String.valueOf(pos.getX()));
                y2Field.setValue(String.valueOf(pos.getY()));
                z2Field.setValue(String.valueOf(pos.getZ()));
            }
        }).bounds(cx + 2, quickY, totalW / 2 - 2, 20).build());

        // --- 确认 / 清除 ---
        int actionY = quickY + 24;
        this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("selection.confirm")), button -> applySelection())
                .bounds(cx - totalW / 2, actionY, totalW / 2 - 2, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("selection.clear")), button -> {
            mgr.clear();
            mgr.clearDraft();
            x1Field.setValue(""); y1Field.setValue(""); z1Field.setValue("");
            x2Field.setValue(""); y2Field.setValue(""); z2Field.setValue("");
        }).bounds(cx + 2, actionY, totalW / 2 - 2, 20).build());

        // --- 分析导出 ---
        int analyzeBtnY = actionY + 30;
        this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("selection.analyze")), button -> analyzeAndExport())
                .bounds(cx - totalW / 2, analyzeBtnY, totalW, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("selection.structures.open")),
                button -> openStructuresDirectory())
                .bounds(cx - totalW / 2, analyzeBtnY + 24, totalW, 20).build());
        // --- 返回 ---
        this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("button.back")), button -> goBack())
                .bounds(cx - 50, analyzeBtnY + 52, 100, 20).build());
    }

    private void openStructuresDirectory() {
        saveDraft();
        Path directory = com.example.selectionclient.ModPaths.getStructuresDir();
        try {
            Files.createDirectories(directory);
            Util.getPlatform().openPath(directory);
        } catch (IOException | RuntimeException e) {
            com.example.selectionclient.SelectionClient.LOGGER.error("无法打开结构文件目录: {}", directory, e);
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal(com.example.selectionclient.I18n.tr("selection.structures.open_failed", directory)));
            }
        }
    }

    private int coordinatesY() {
        return Math.min(60, Math.max(40, this.height - 204));
    }

    /** 退出前保存草稿 */
    private void saveDraft() {
        SelectionManager.getInstance().saveDraft(
            x1Field.getValue(), y1Field.getValue(), z1Field.getValue(),
            x2Field.getValue(), y2Field.getValue(), z2Field.getValue()
        );
    }

    private void goBack() {
        saveDraft();
        this.minecraft.gui.setScreen(this.parent);
    }

    private void analyzeAndExport() {
        SelectionManager mgr = SelectionManager.getInstance();
        if (!mgr.isComplete()) {
            // 尝试自动确认：从输入框解析坐标
            if (!tryAutoConfirm()) {
                return;
            }
        }
        SelectionAnalyzer.AnalysisResult result = SelectionAnalyzer.analyze(mgr.getPos1(), mgr.getPos2());
        if (result == null) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal(com.example.selectionclient.I18n.tr("selection.analyze.failed")));
            }
            return;
        }
        saveDraft();
        this.minecraft.gui.setScreen(new SelectionExportScreen(this, result));
    }

    private void applySelection() {
        try {
            int x1 = Integer.parseInt(x1Field.getValue().trim());
            int y1 = Integer.parseInt(y1Field.getValue().trim());
            int z1 = Integer.parseInt(z1Field.getValue().trim());
            int x2 = Integer.parseInt(x2Field.getValue().trim());
            int y2 = Integer.parseInt(y2Field.getValue().trim());
            int z2 = Integer.parseInt(z2Field.getValue().trim());

            SelectionManager mgr = SelectionManager.getInstance();
            mgr.setPos1(new BlockPos(x1, y1, z1));
            mgr.setPos2(new BlockPos(x2, y2, z2));
            mgr.clearDraft();
            SelectionMemory.addEntry(x1 + "," + y1 + "," + z1 + " → " + x2 + "," + y2 + "," + z2,
                    mgr.getPos1(), mgr.getPos2());

            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(
                    Component.literal(com.example.selectionclient.I18n.tr("selection.set", x1 + "," + y1 + "," + z1, x2 + "," + y2 + "," + z2)));
            }
            this.minecraft.gui.setScreen(null);
        } catch (NumberFormatException e) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal(com.example.selectionclient.I18n.tr("selection.invalid_coords")));
            }
        }
    }

    /**
     * 尝试从输入框自动确认选区（用于点导出时自动确认）。
     * @return true 表示确认成功，false 表示坐标无效无法确认
     */
    private boolean tryAutoConfirm() {
        try {
            int x1 = Integer.parseInt(x1Field.getValue().trim());
            int y1 = Integer.parseInt(y1Field.getValue().trim());
            int z1 = Integer.parseInt(z1Field.getValue().trim());
            int x2 = Integer.parseInt(x2Field.getValue().trim());
            int y2 = Integer.parseInt(y2Field.getValue().trim());
            int z2 = Integer.parseInt(z2Field.getValue().trim());

            SelectionManager mgr = SelectionManager.getInstance();
            mgr.setPos1(new BlockPos(x1, y1, z1));
            mgr.setPos2(new BlockPos(x2, y2, z2));
            mgr.clearDraft();
            SelectionMemory.addEntry(x1 + "," + y1 + "," + z1 + " → " + x2 + "," + y2 + "," + z2,
                    mgr.getPos1(), mgr.getPos2());

            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(
                    Component.literal(com.example.selectionclient.I18n.tr("selection.auto_confirmed", x1 + "," + y1 + "," + z1, x2 + "," + y2 + "," + z2)));
            }
            return true;
        } catch (NumberFormatException e) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal(com.example.selectionclient.I18n.tr("selection.invalid_coords")));
            }
            return false;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int cx = this.width / 2;
        int fieldY1 = coordinatesY();
        context.centeredText(this.font, this.title, cx, fieldY1 - 26, 0xFFFFFFFF);
        context.text(this.font, com.example.selectionclient.I18n.tr("selection.pos1.label"), cx - 84, fieldY1 - 12, 0xFFAAAAAA);
        context.text(this.font, com.example.selectionclient.I18n.tr("selection.pos2.label"), cx - 84, fieldY1 + 24, 0xFFAAAAAA);
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        saveDraft();
        this.minecraft.gui.setScreen(this.parent);
    }
}
