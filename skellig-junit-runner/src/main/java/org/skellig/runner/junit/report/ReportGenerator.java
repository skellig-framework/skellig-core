package org.skellig.runner.junit.report;

import org.skellig.runner.junit.report.model.FeatureReportDetails;

import java.util.List;

public interface ReportGenerator {

    void generate(List<FeatureReportDetails> testReportDetails);
}
