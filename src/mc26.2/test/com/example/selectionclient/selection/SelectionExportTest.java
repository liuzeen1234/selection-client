package com.example.selectionclient.selection;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SelectionExportTest {
    @TempDir Path directory;

    private SelectionAnalyzer.AnalysisResult sample() {
        // Air followed by a stair: filtering must preserve the stair's offset and properties.
        return new SelectionAnalyzer.AnalysisResult(new BlockPos(-4, 64, 8), new BlockPos(-3, 64, 8),
                2, 1, 1, 2, 1, Map.of("air", 1, "oak_stairs", 1), List.of(
                new SelectionAnalyzer.BlockInfo("air", Map.of(), 0, 0, 0),
                new SelectionAnalyzer.BlockInfo("oak_stairs", Map.of("facing", "east", "half", "top"), 1, 0, 0)));
    }

    @Test void filteredBlueprintPreservesSizePositionAndState() {
        var filtered = SelectionAnalyzer.filterBlocks(sample(), Set.of("air"));
        var text = SelectionAnalyzer.exportBlueprintV2(filtered, "stairs");
        assertEquals(1, filtered.totalBlocks());
        assertEquals(0, filtered.airBlocks());
        assertTrue(text.contains("# size: 2x1x1"));
        assertTrue(text.contains("1,0,0   oak_stairs"));
        assertTrue(text.contains("facing=east"));
        assertTrue(text.contains("half=top"));
        assertFalse(text.contains("   air"));
    }

    @Test void compressedStructureUsesCurrentDataVersionAndValidPalette() throws Exception {
        SharedConstants.tryDetectVersion();
        var file = directory.resolve("stairs.nbt");
        SelectionAnalyzer.exportNbt(SelectionAnalyzer.filterBlocks(sample(), Set.of("air")), file.toFile());
        var root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        assertEquals(SharedConstants.getCurrentVersion().dataVersion().version(), root.getIntOr("DataVersion", -1));
        assertEquals(2, root.getListOrEmpty("size").getIntOr(0, -1));
        var blocks = root.getListOrEmpty("blocks");
        assertEquals(1, blocks.size());
        var block = blocks.getCompoundOrEmpty(0);
        assertEquals(1, block.getListOrEmpty("pos").getIntOr(0, -1));
        var palette = root.getListOrEmpty("palette");
        var state = palette.getCompoundOrEmpty(block.getIntOr("state", -1));
        assertEquals("minecraft:oak_stairs", state.getStringOr("Name", ""));
        assertEquals("east", state.getCompoundOrEmpty("Properties").getStringOr("facing", ""));
        assertEquals("top", state.getCompoundOrEmpty("Properties").getStringOr("half", ""));
        assertTrue(root.getListOrEmpty("entities").isEmpty());
    }
}
