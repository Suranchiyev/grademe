package com.grademe.grademe.controller;

import com.grademe.grademe.beans.ProjectStatus;
import com.grademe.grademe.beans.Report;
import com.grademe.grademe.server.EvaluationEngine;
import com.grademe.grademe.server.S3Service;
import com.grademe.grademe.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;


@RestController
public class ProjectController {
    @Autowired
    S3Service s3Service;

    @Autowired
    EvaluationEngine engine;

    @PostMapping("/compileProject")
    public @ResponseBody
    List<String> compileProject(@RequestParam(value = "projectId") String projectId, @RequestParam("implStr") String mFileFromUser) {
        String timestampId = getCurrentTimestamp();

        String tmpDir = System.getProperty("user.home") + "/" + timestampId + "/tmp";
        File tmpDirFile = new File(tmpDir);
        try {
            if (!tmpDirFile.exists()) {
                tmpDirFile.mkdirs();
            }

            File implFromUser = new File(tmpDirFile.getAbsolutePath() + "/Project.java");
            FileUtils.writeString(mFileFromUser, implFromUser);

            engine.compile(tmpDirFile);
            return engine.run(tmpDirFile);

        } catch (Exception e) {
            e.printStackTrace();
            FileUtils.deleteAll(tmpDirFile);
            throw new RuntimeException("Error while compiling project");
        }
    }

    @PostMapping("/testProjectStr")
    public @ResponseBody
    Report testProjectString(@RequestBody ProjectStatus projectStatus) throws Exception {
        String timestampId = getCurrentTimestamp();
        String currentUsersHomeDir = System.getProperty("user.home") + "/" + timestampId + "/projects";
        File projectsDir = new File(currentUsersHomeDir);
        if (!projectsDir.exists()) {
            projectsDir.mkdirs();
        }

        String tmpDir = System.getProperty("user.home") + "/" + timestampId + "/tmp";
        File tmpDirFile = new File(tmpDir);
        if (!tmpDirFile.exists()) {
            tmpDirFile.mkdirs();
        }

        try {
            File projectZip = new File(projectsDir.getAbsolutePath() + "/project.zip");
            s3Service.getProject(projectStatus.getProjectId(), projectZip, 3);

            File unzipDir = new File(projectsDir.getAbsolutePath());
            FileUtils.unzip(projectZip.getAbsolutePath(), unzipDir.getAbsolutePath());

            File project = new File(unzipDir.getAbsolutePath() + "/project");
            System.out.println("Project Dir:");
            System.out.println(project.getAbsolutePath());

            File implFromUser = new File(tmpDirFile.getAbsolutePath() + "/Project.java");
            FileUtils.writeString(projectStatus.getStudentCode(), implFromUser);

            File implInProject = new File(project.getAbsolutePath() + "/src/main/java/Project.java");

            FileUtils.replaceFile(implFromUser, implInProject);

            List<String> errorContent = engine.evaluateProject(project);

            Report report;
            if (errorContent.size() == 0) {
                report = engine.getReportObject(project, Long.parseLong(projectStatus.getProjectId()));
            } else {
                report = getErrorReport(errorContent, Long.parseLong(projectStatus.getProjectId()));
            }

            FileUtils.deleteAll(project);
            System.out.println("Project Cleaned Up: ");
            System.out.println(project.getAbsolutePath());

            FileUtils.deleteAll(projectsDir);
            FileUtils.deleteAll(tmpDirFile);

            System.out.println("DONE!");
            return report;
        } catch (Exception e) {
            e.printStackTrace();
            FileUtils.deleteAll(projectsDir.getParentFile());
            FileUtils.deleteAll(tmpDirFile);

            throw new RuntimeException("Error while evaluating project");
        }

    }

    private String getCurrentTimestamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return String.valueOf(timestamp.getTime());
    }

    private Report getErrorReport(List<String> errorContent, long projectId) {
        Report report = new Report();
        report.setErrorContent(errorContent);
        report.setProjectId(projectId);
        report.setCompiled(false);
        return report;
    }
}
