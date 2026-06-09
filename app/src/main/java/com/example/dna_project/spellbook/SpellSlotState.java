package com.example.dna_project.spellbook;

// Skaita burvestibu slotu stavokli: cik ir kopa, cik izlietoti un cik palikusi.
public class SpellSlotState {
    private final int[] maximumSlots;
    private final int[] usedSlots;

    public SpellSlotState(int[] maximumSlots, int[] usedSlots) {
        this.maximumSlots = maximumSlots;
        this.usedSlots = usedSlots;
    }

    public int maximum(int level) {
        // 0. limena burvestibas netere slotus, tapec skaitam tikai 1-9 limeni.
        if (level < 1 || level >= maximumSlots.length) {
            return 0;
        }
        return maximumSlots[level];
    }

    public int used(int level) {
        if (level < 1 || level >= usedSlots.length) {
            return 0;
        }
        // Izlietoto slotu skaits nedrikst but lielaks par maksimu saja limeni.
        return Math.min(usedSlots[level], maximum(level));
    }

    public int remaining(int level) {
        return Math.max(maximum(level) - used(level), 0);
    }

    public int totalMaximum() {
        int total = 0;
        for (int level = 1; level <= 9; level++) {
            total += maximum(level);
        }
        return total;
    }

    public int totalRemaining() {
        int total = 0;
        for (int level = 1; level <= 9; level++) {
            total += remaining(level);
        }
        return total;
    }

    public boolean hasAvailableSlot(int level) {
        return remaining(level) > 0;
    }
}
