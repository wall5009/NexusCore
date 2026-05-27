package com.rollylindenshnizzer.nexuscore.client.debug;

import com.rollylindenshnizzer.nexuscore.performance.NamedProfiler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class NexusProfilerHud {
    private static boolean enabled = Boolean.getBoolean("nexus.profilerHud");

    public static boolean enabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static void setEnabled(boolean enabled) {
        NexusProfilerHud.enabled = enabled;
    }

    public static void render(GuiGraphics graphics) {
        if (!enabled) {
            return;
        }
        List<Map.Entry<String, NamedProfiler.Stat>> rows = NamedProfiler.global().top(8);
        int width = 250;
        int height = 24 + Math.max(1, rows.size()) * 11;
        int x = 8;
        int y = 8;
        graphics.fill(x - 4, y - 4, x + width, y + height, 0xAA101820);
        graphics.drawString(Minecraft.getInstance().font, "Nexus Profiler", x, y, 0xFFFFFFFF, true);
        if (rows.isEmpty()) {
            graphics.drawString(Minecraft.getInstance().font, "No samples recorded", x, y + 14, 0xFFB8C1CC, false);
            return;
        }
        int lineY = y + 14;
        for (Map.Entry<String, NamedProfiler.Stat> entry : rows) {
            NamedProfiler.Stat stat = entry.getValue();
            String line = entry.getKey() + "  " + stat.calls() + "x  avg "
                    + String.format(Locale.ROOT, "%.3f ms", stat.averageMillis());
            graphics.drawString(Minecraft.getInstance().font, line, x, lineY, 0xFFE8EEF5, false);
            lineY += 11;
        }
    }

    private NexusProfilerHud() {
    }
}
