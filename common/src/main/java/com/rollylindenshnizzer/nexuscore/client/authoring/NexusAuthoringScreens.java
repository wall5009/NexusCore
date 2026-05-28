package com.rollylindenshnizzer.nexuscore.client.authoring;

import com.rollylindenshnizzer.nexuscore.authoring.NexusAuthoring;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class NexusAuthoringScreens {
    public static void openWorkbench(Screen parent) {
        Minecraft.getInstance().setScreen(new NexusAuthoringWorkbenchScreen(session(false), parent));
    }

    public static void openEditor(NexusAuthoring.WorkbenchSession session, NexusAuthoring.EditorDefinition editor, Screen parent) {
        Minecraft.getInstance().setScreen(new NexusVisualAuthoringScreen(session, editor, parent));
    }

    private static NexusAuthoring.WorkbenchSession session(boolean readOnly) {
        Minecraft minecraft = Minecraft.getInstance();
        String user = minecraft.player == null ? System.getProperty("user.name", "local") : minecraft.player.getGameProfile().getName();
        return NexusAuthoring.openSession(user, true, true, readOnly);
    }

    private NexusAuthoringScreens() {
    }
}
