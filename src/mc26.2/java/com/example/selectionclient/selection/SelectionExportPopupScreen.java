package com.example.selectionclient.selection;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * P2 - 导出选区选择页面：点击"导出选区"后跳转到此页面，
 * 提供"导出.txt"和"导出.nbt"两个选项。
 */
public class SelectionExportPopupScreen extends Screen {

    private final Screen parent;
    private final SelectionAnalyzer.AnalysisResult result;
    private final java.util.Set<String> ignoredBlocks;

    // 弹窗尺寸
    private static final int POPUP_WIDTH = 260;
    private static final int POPUP_HEIGHT = 110;

    public SelectionExportPopupScreen(Screen parent, SelectionAnalyzer.AnalysisResult result) {
        this(parent, result, java.util.Collections.emptySet());
    }

    public SelectionExportPopupScreen(Screen parent, SelectionAnalyzer.AnalysisResult result,
                                      java.util.Set<String> ignoredBlocks) {
        super(Component.literal(com.example.selectionclient.I18n.tr("selection.export.popup.title")));
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

        int btnW = POPUP_WIDTH - 40;
        int btnLeft = popLeft + 20;

        // "导出.txt" 按钮 → 跳转到 P4 (ExportTxtScreen)
        int txtBtnY = popTop + 28;
        this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("selection.export.popup.txt")), button -> {
            this.minecraft.gui.setScreen(new ExportTxtScreen(this, result, ignoredBlocks));
        }).bounds(btnLeft, txtBtnY, btnW, 20).build());

        // "导出.nbt" 按钮 → 跳转到 P3 (ExportNbtScreen)
        int nbtBtnY = txtBtnY + 26;
        this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("selection.export.popup.nbt")), button -> {
            this.minecraft.gui.setScreen(new ExportNbtScreen(this, result, ignoredBlocks));
        }).bounds(btnLeft, nbtBtnY, btnW, 20).build());

        // 返回按钮
        int cancelY = nbtBtnY + 26;
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

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
