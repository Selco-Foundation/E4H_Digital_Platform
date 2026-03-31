package org.egov.field_planner.web.models;

public class NameResult {
    private final boolean isDuplicate;
    private final String generatedName;

    public NameResult(boolean isDuplicate, String generatedName) {
        this.isDuplicate = isDuplicate;
        this.generatedName = generatedName;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public String getGeneratedName() {
        return generatedName;
    }

    @Override
    public String toString() {
        return "NameResult{isDuplicate=" + isDuplicate + ", generatedName='" + generatedName + "'}";
    }
}
