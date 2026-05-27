package com.rollylindenshnizzer.nexuscore.security;

import com.rollylindenshnizzer.nexuscore.core.NexusException;

import java.nio.file.Path;

public final class SafePaths {
    public static Path resolveInside(Path root, String relativePath) {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path resolved = normalizedRoot.resolve(relativePath).normalize();
        if (!resolved.startsWith(normalizedRoot)) {
            throw new NexusException("Unsafe path escapes root: " + relativePath);
        }
        return resolved;
    }

    public static Path validateConfigImport(Path configRoot, String relativePath) {
        return resolveInside(configRoot, relativePath);
    }

    public static Path validateExport(Path reportRoot, String relativePath) {
        return resolveInside(reportRoot, relativePath);
    }

    private SafePaths() {
    }
}
