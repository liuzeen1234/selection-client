package com.example.selectionclient.selection;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

import java.util.List;

/**
 * 已保存选区列表界面。点击条目可加载到选区管理器并高亮。
 */
public class SelectionMemoryScreen extends Screen {

    private final Screen parent;
    private int scrollOffset = 0;
    private static final int PAGE_SIZE = 6;
    private static final int ROW_HEIGHT = 22;

    public SelectionMemoryScreen(Screen parent) {
        super(Component.literal(com.example.selectionclient.I18n.tr("selection.memory.title")));
        this.parent = parent;
    }

    @Override
    protected void init() {
        List<SelectionMemory.Entry> entries = SelectionMemory.load();
        int cx = this.width / 2;
        int listW = 260;
        int listLeft = cx - listW / 2;
        int startY = 50;

        // 列表条目
        int end = Math.min(entries.size(), scrollOffset + PAGE_SIZE);
        for (int i = scrollOffset; i < end; i++) {
            SelectionMemory.Entry entry = entries.get(i);
            int row = i - scrollOffset;
            int rowY = startY + row * ROW_HEIGHT;
            final int idx = i;

            // 加载按钮：名称 + 坐标摘要
            BlockPos p1 = entry.pos1();
            BlockPos p2 = entry.pos2();
            String label = "§f" + entry.name() + " §7(" + p1.getX() + "," + p1.getY() + "," + p1.getZ()
                    + ")→(" + p2.getX() + "," + p2.getY() + "," + p2.getZ() + ")";
            this.addRenderableWidget(Button.builder(Component.literal(label), button -> {
                SelectionManager mgr = SelectionManager.getInstance();
                mgr.setPos1(entry.pos1());
                mgr.setPos2(entry.pos2());
                mgr.clearDraft();
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.sendSystemMessage(
                        Component.literal(com.example.selectionclient.I18n.tr("selection.memory.loaded", entry.name())));
                }
            }).bounds(listLeft, rowY, listW - 24, 20).build());

            // 删除按钮
            this.addRenderableWidget(Button.builder(Component.literal("§cx"), button -> {
                SelectionMemory.removeEntry(idx);
                if (scrollOffset > 0 && scrollOffset >= SelectionMemory.load().size()) scrollOffset--;
                rebuildUI();
            }).bounds(listLeft + listW - 20, rowY, 20, 20).build());
        }

        // 翻页
        int navY = startY + PAGE_SIZE * ROW_HEIGHT + 4;
        if (entries.size() > PAGE_SIZE) {
            this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("selection.memory.prev")), button -> {
                if (scrollOffset > 0) { scrollOffset -= PAGE_SIZE; if (scrollOffset < 0) scrollOffset = 0; rebuildUI(); }
            }).bounds(cx - 104, navY, 100, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("selection.memory.next")), button -> {
                if (scrollOffset + PAGE_SIZE < entries.size()) { scrollOffset += PAGE_SIZE; rebuildUI(); }
            }).bounds(cx + 4, navY, 100, 20).build());
        }

        // 返回
        this.addRenderableWidget(Button.builder(Component.literal(com.example.selectionclient.I18n.tr("button.back")), button -> this.minecraft.gui.setScreen(this.parent))
                .bounds(cx - 50, this.height - 30, 100, 20).build());
    }

    private void rebuildUI() {
        this.clearWidgets();
        this.init();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int cx = this.width / 2;
        context.centeredText(this.font, this.title, cx, 28, 0xFFFFFFFF);

        List<SelectionMemory.Entry> entries = SelectionMemory.load();
        if (entries.isEmpty()) {
            context.centeredText(this.font, com.example.selectionclient.I18n.tr("selection.memory.empty"), cx, this.height / 2, 0xFF888888);
        } else {
            String info = com.example.selectionclient.I18n.tr("selection.memory.info",
                entries.size(),
                (scrollOffset + 1),
                Math.min(scrollOffset + PAGE_SIZE, entries.size())
            );
            context.centeredText(this.font, info, cx, 40, 0xFF888888);
        }

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
