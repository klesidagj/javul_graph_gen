package org.example.model;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.Map;

public class MethodResult {

    private String name;
    private String parseStrategy;
    private String error;
    private boolean success = true;

    // Parsed method
    private MethodDeclaration methodDeclaration;

    // Raw graph maps
    private Map<String, Object> astGraph;
    private Map<String, Object> cfgGraph;
    private Map<String, Object> dfgGraph;

    // Serialized JSON (used only when writing to DB)
    private String astJson;
    private String cfgJson;
    private String dfgJson;


    // ------------------------------------------------------
    // Constructor
    // ------------------------------------------------------
    public MethodResult(String name) {
        this.name = name;
    }


    // ------------------------------------------------------
    // Getters / setters
    // ------------------------------------------------------
    public MethodDeclaration getMethodDeclaration() {
        return methodDeclaration;
    }

    public void setMethodDeclaration(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    public Map<String, Object> getAstGraph() {
        return astGraph;
    }

    public void setAstGraph(Map<String, Object> astGraph) {
        this.astGraph = astGraph;
    }

    public Map<String, Object> getCfgGraph() {
        return cfgGraph;
    }

    public void setCfgGraph(Map<String, Object> cfgGraph) {
        this.cfgGraph = cfgGraph;
    }

    public Map<String, Object> getDfgGraph() {
        return dfgGraph;
    }

    public void setDfgGraph(Map<String, Object> dfgGraph) {
        this.dfgGraph = dfgGraph;
    }

    public String getAstJson() {
        return astJson;
    }

    public void setAstJson(String astJson) {
        this.astJson = astJson;
    }

    public String getCfgJson() {
        return cfgJson;
    }

    public void setCfgJson(String cfgJson) {
        this.cfgJson = cfgJson;
    }

    public String getDfgJson() {
        return dfgJson;
    }

    public void setDfgJson(String dfgJson) {
        this.dfgJson = dfgJson;
    }

    public String getName() {
        return name;
    }

    public String getParseStrategy() {
        return parseStrategy;
    }

    public void setParseStrategy(String parseStrategy) {
        this.parseStrategy = parseStrategy;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        this.success = false;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}