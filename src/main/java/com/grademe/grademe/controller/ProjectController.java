package com.grademe.grademe.controller;

import com.grademe.grademe.beans.Report;
import com.grademe.grademe.server.EvaluationEngine;
import com.grademe.grademe.server.S3Service;
import com.grademe.grademe.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


@RestController
public class ProjectController {
    @Autowired
    S3Service s3Service;

    @Autowired
    EvaluationEngine engine;

    @PostMapping("/testProject")
    public @ResponseBody Report testProject(@RequestParam(value="projectId") String projectId, @RequestParam("implFile") MultipartFile mFileFromUser) throws Exception {

        File projectZip = new File("src/main/resources/projects/project.zip");
        s3Service.getProject(projectId,projectZip);

        File unzipDir = new File("src/main/resources/projects");
        FileUtils.unzip(projectZip.getAbsolutePath(),unzipDir.getAbsolutePath());

        File project = new File(unzipDir.getAbsolutePath()+"/project");


        File implFromUser = new File("src/main/resources/Project.java");
        FileUtils.writeStream(mFileFromUser.getInputStream(), implFromUser);

        File implInProject = new File(project.getAbsolutePath()+"/src/main/java/Project.java");

        FileUtils.replaceFile(implFromUser,implInProject);

        engine.evaluateProject(project);
        Report report = engine.getReportObject(Long.parseLong(projectId));

        FileUtils.deleteAll(project);
        FileUtils.deleteAll(projectZip);
        FileUtils.deleteAll(implFromUser);

        System.out.println("DONE!");
        return report;
    }
}
