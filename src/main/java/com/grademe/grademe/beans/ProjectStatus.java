package com.grademe.grademe.beans;

public class ProjectStatus {
    private String _id;
    private String name;
    private String description;
    private String projectId;
    private double grade;
    private String message;
    private String status;
    private String student;
    private String courseName;
    private int week;
    private String topic;
    private String studentCode;
    private String instructorCode;
    private String urlToRequirementFile;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getInstructorCode() {
        return instructorCode;
    }

    public void setInstructorCode(String instructorCode) {
        this.instructorCode = instructorCode;
    }

    public String getUrlToRequirementFile() {
        return urlToRequirementFile;
    }

    public void setUrlToRequirementFile(String urlToRequirementFile) {
        this.urlToRequirementFile = urlToRequirementFile;
    }

    @Override
    public String toString() {
        return "ProjectStatus{" +
                "_id='" + _id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", projectId='" + projectId + '\'' +
                ", grade=" + grade +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", student='" + student + '\'' +
                ", courseName='" + courseName + '\'' +
                ", week=" + week +
                ", topic='" + topic + '\'' +
                ", studentCode='" + studentCode + '\'' +
                ", instructorCode='" + instructorCode + '\'' +
                ", urlToRequirementFile='" + urlToRequirementFile + '\'' +
                '}';
    }
}
