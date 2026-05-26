package com.rollylindenshnizzer.nexuscore.data;

import com.rollylindenshnizzer.nexuscore.core.NexusException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class DatagenBootstrap {
    private final List<NexusData.DataPlan> plans = new ArrayList<>();
    private boolean strict;

    public DatagenBootstrap plan(NexusData.DataPlan plan) {
        plans.add(plan);
        return this;
    }

    public DatagenBootstrap strict() {
        strict = true;
        return this;
    }

    public List<DataValidationReport> writeAll(Path outputRoot) {
        List<DataValidationReport> reports = new ArrayList<>();
        for (NexusData.DataPlan plan : plans) {
            DataValidationReport report = NexusDataValidator.validatePlan(plan);
            if (strict && report.hasErrors()) {
                throw new NexusException(report.summary());
            }
            plan.writeTo(outputRoot);
            reports.add(report);
        }
        return reports;
    }
}
