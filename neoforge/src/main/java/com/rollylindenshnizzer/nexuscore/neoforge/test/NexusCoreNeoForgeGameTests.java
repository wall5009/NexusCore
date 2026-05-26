package com.rollylindenshnizzer.nexuscore.neoforge.test;

import com.rollylindenshnizzer.nexuscore.NexusCore;
import com.rollylindenshnizzer.nexuscore.energy.EnergyStorage;
import com.rollylindenshnizzer.nexuscore.test.ValidationSuite;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NexusCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NexusCoreNeoForgeGameTests {
    @GameTest(templateNamespace = NexusCore.MOD_ID, template = "bootstrap", timeoutTicks = 100)
    public static void validationSuitePasses(GameTestHelper helper) {
        EnergyStorage energy = new EnergyStorage(1_000, 250, 100);
        ValidationSuite.Result result = new ValidationSuite()
                .check("insert is rate limited", () -> {
                    if (energy.insert(500, false) != 250) {
                        throw new AssertionError("Expected max insert to limit transfer");
                    }
                })
                .check("extract is rate limited", () -> {
                    if (energy.extract(500, false) != 100) {
                        throw new AssertionError("Expected max extract to limit transfer");
                    }
                })
                .check("remaining energy is tracked", () -> {
                    if (energy.amount() != 150) {
                        throw new AssertionError("Expected 150 energy remaining");
                    }
                })
                .run();
        helper.assertTrue(result.passed(), String.join(", ", result.failures()));
        helper.succeed();
    }
}
