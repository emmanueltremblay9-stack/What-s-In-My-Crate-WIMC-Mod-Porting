package com.emmanueltremblay.wimc.crate;

import java.util.List;

public final class CratePreviewContentsQa {
    private CratePreviewContentsQa() {
    }

    public static void main(String[] args) {
        trimmedEmptySlotsAreNotReportedAsHidden();
    }

    private static void trimmedEmptySlotsAreNotReportedAsHidden() {
        CratePreviewContents contents = CratePreviewContents.create(
                List.of(),
                27,
                6,
                9,
                false,
                CratePreviewStatus.EMPTY
        );

        requireEqual(0, contents.slots().size(), "trimmed slot count");
        requireEqual(0, contents.rows(), "trimmed row count");
        requireEqual(0, contents.hiddenSlots(), "trimmed hidden slots");
        requireFalse(contents.hasRenderableGrid(), "trimmed grid renderability");
    }

    private static void requireEqual(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }

    private static void requireFalse(boolean value, String label) {
        if (value) {
            throw new AssertionError(label + ": expected false");
        }
    }
}
