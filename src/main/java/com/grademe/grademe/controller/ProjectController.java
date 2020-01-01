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

        String currentUsersHomeDir = System.getProperty("user.home")+"/projects";
        File projectsDir = new File(currentUsersHomeDir);
        if(!projectsDir.exists()){
            projectsDir.mkdir();
        }

        String tmpDir = System.getProperty("user.home")+"/tmp";
        File tmpDirFile = new File(tmpDir);
        if(!tmpDirFile.exists()){
            tmpDirFile.mkdir();
        }

        File projectZip = new File(projectsDir.getAbsolutePath()+"/project.zip");
        s3Service.getProject(projectId,projectZip,3);

        File unzipDir = new File(projectsDir.getAbsolutePath());
        FileUtils.unzip(projectZip.getAbsolutePath(),unzipDir.getAbsolutePath());

        File project = new File(unzipDir.getAbsolutePath()+"/project");
        System.out.println("Project Dir:");
        System.out.println(project.getAbsolutePath());

        File implFromUser = new File(tmpDirFile.getAbsolutePath()+"/Project.java");
        FileUtils.writeStream(mFileFromUser.getInputStream(), implFromUser);

        File implInProject = new File(project.getAbsolutePath()+"/src/main/java/Project.java");

        FileUtils.replaceFile(implFromUser,implInProject);

        engine.evaluateProject(project);
        Report report = engine.getReportObject(project,Long.parseLong(projectId));

        FileUtils.deleteAll(project);
        System.out.println("Project Cleaned Up: ");
        System.out.println(project.getAbsolutePath());

        FileUtils.deleteAll(projectZip);
        FileUtils.deleteAll(implFromUser);

        System.out.println("DONE!");
        return report;
    }
}
