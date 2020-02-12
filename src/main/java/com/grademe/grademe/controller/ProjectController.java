package com.grademe.grademe.controller;

import com.grademe.grademe.beans.Report;
import com.grademe.grademe.server.EvaluationEngine;
import com.grademe.grademe.server.S3Service;
import com.grademe.grademe.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.sql.Timestamp;


@RestController
public class ProjectController {
    @Autowired
    S3Service s3Service;

    @Autowired
    EvaluationEngine engine;

    @PostMapping("/testProjectStr")
    public @ResponseBody Report testProjectString(@RequestParam(value="projectId") String projectId, @RequestParam("implStr") String mFileFromUser) throws Exception {

            String timestampId = getCurrentTimestamp();
            String currentUsersHomeDir = System.getProperty("user.home")+"/"+timestampId+"/projects";
            File projectsDir = new File(currentUsersHomeDir);
            if(!projectsDir.exists()){
                projectsDir.mkdirs();
            }

            String tmpDir = System.getProperty("user.home")+"/"+timestampId+"/tmp";
            File tmpDirFile = new File(tmpDir);
            if(!tmpDirFile.exists()){
                tmpDirFile.mkdirs();
            }

            try{
            File projectZip = new File(projectsDir.getAbsolutePath()+"/project.zip");
            s3Service.getProject(projectId,projectZip,3);

            File unzipDir = new File(projectsDir.getAbsolutePath());
            FileUtils.unzip(projectZip.getAbsolutePath(),unzipDir.getAbsolutePath());

            File project = new File(unzipDir.getAbsolutePath()+"/project");
            System.out.println("Project Dir:");
            System.out.println(project.getAbsolutePath());

            File implFromUser = new File(tmpDirFile.getAbsolutePath()+"/Project.java");
            FileUtils.writeString(mFileFromUser, implFromUser);

            File implInProject = new File(project.getAbsolutePath()+"/src/main/java/Project.java");

            FileUtils.replaceFile(implFromUser,implInProject);

            engine.evaluateProject(project);
            Report report = engine.getReportObject(project,Long.parseLong(projectId));

            FileUtils.deleteAll(project);
            System.out.println("Project Cleaned Up: ");
            System.out.println(project.getAbsolutePath());

            FileUtils.deleteAll(projectsDir);
            FileUtils.deleteAll(tmpDirFile);

            System.out.println("DONE!");
            return report;
        }catch(Exception e){
            e.printStackTrace();
            FileUtils.deleteAll(projectsDir);
            FileUtils.deleteAll(tmpDirFile);

            throw  new RuntimeException("Error while evaluating project");
        }

    }

    private String getCurrentTimestamp(){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return String.valueOf(timestamp.getTime());
    }
}
