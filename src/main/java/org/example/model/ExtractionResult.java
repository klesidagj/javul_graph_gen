package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class ExtractionResult {
    private boolean success;
    private List<MethodResult> methods = new ArrayList<>();
    private String overallError;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOverallError() {
        return overallError;
    }

    public void setOverallError(String overallError) {
        this.overallError = overallError;
    }

    public List<MethodResult> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodResult> methods) {
        this.methods = methods;
    }

    public boolean hasAnySuccessfulMethod() {
        return methods.stream().anyMatch(MethodResult::isSuccess);
    }
}
