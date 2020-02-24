package com.grademe.grademe.beans;


import java.util.List;

public class Report {
    private long projectId;
    private int grade;
    private boolean isCompiled;
    private List<String> errorContent;
    private List<TestCase> testCases;

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    public boolean isCompiled() {
        return isCompiled;
    }

    public void setCompiled(boolean compiled) {
        isCompiled = compiled;
    }

    public List<String> getErrorContent() {
        return errorContent;
    }

    public void setErrorContent(List<String> errorContent) {
        this.errorContent = errorContent;
    }
}

