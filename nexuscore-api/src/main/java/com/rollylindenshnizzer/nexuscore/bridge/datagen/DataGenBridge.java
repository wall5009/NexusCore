package com.rollylindenshnizzer.nexuscore.bridge.datagen;

import com.rollylindenshnizzer.nexuscore.api.datagen.NexusDataGen;

import java.nio.file.Path;
import java.util.List;

public interface DataGenBridge {
    List<Path> generate(NexusDataGen dataGen);
}
