package com.grademe.grademe.server;

import com.grademe.grademe.beans.Report;
import com.grademe.grademe.beans.TestCase;
import com.grademe.grademe.util.FileUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class EvaluationEngine {

    private boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public List<String> compile(File fileDirectory){
        try{
            ProcessBuilder builder = new ProcessBuilder();

            if (isWindows) {
                System.out.println("Compilation in Windows:");
                builder.command("cmd.exe", "/c", "echo javac Project.java");
               // builder.command("cmd.exe", "/c", "dir");

            } else {
                System.out.println("Compilation in Linux:");
                builder.command("sh", "-c", "javac Project.java");
            }

            builder.directory(fileDirectory);
            Process process = builder.start();
            List<String> consoleContent = streamResults(process.getErrorStream());
            process.destroy();

            return consoleContent;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<String> run(File fileDirectory){
        try{
            ProcessBuilder builder = new ProcessBuilder();
            if (isWindows) {
                System.out.println("Compilation in Windows:");
                builder.command("cmd.exe", "/c", "java Project");

            } else {
                System.out.println("Compilation in Linux:");
                builder.command("sh", "-c", "java Project");
            }

            builder.directory(fileDirectory);
            Process process = builder.start();
            List<String> consoleContent = streamResults(process.getInputStream());
            process.destroy();

            return consoleContent;
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private List<String> streamResults(InputStream inputStream){
        try{
            List<String> content = new ArrayList<>();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            System.out.println("Streamin..");
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Line: "+line);
                content.add(line);
            }

            inputStream.close();
            inputStreamReader.close();
            reader.close();

            System.out.println("Streaming is done.");

            return content;

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<String> evaluateProject(File project) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            if (isWindows) {
                System.out.println("Evaluation in Windows:");
                builder.command("cmd.exe", "/c", "mvn clean test");
            } else {
                System.out.println("Evaluation in Linux:");
                builder.command("sh", "-c", "mvn clean test");
            }

            builder.directory(project);
            Process process = builder.start();
            List<String> errorContent = streamErrorResults(process.getInputStream());
            process.destroy();

            return errorContent;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

    }

    private List<String> streamErrorResults(InputStream inputStream){
        try{
            List<String> errorContent = new ArrayList<>();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            boolean compileErrorFound = false;

            String line;
            int numberOfLines = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);

                if(line.contains("[Help 1]")){
                    compileErrorFound = false;
                }

                if(line.contains("Compilation failure")){
                    compileErrorFound = true;
                }

                if(compileErrorFound){
                    errorContent.add(line);
                }

                numberOfLines++;
                if(numberOfLines > 1000){
                    errorContent.add("System limitations: Number of lines exited over 1000");
                    break;
                }
            }

            inputStream.close();
            inputStreamReader.close();
            reader.close();

            return errorContent;

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Report getReportObject(File project,long projectId){
        try{
            System.out.println("Getting Reports..");
            //Get Test class content and revers lines
            List<String> testClassContent = FileUtils.getFileContent(project.getAbsolutePath()+"/src/test/java/TestProject.java");
            Collections.reverse(testClassContent);
            int totalTestCases;
            int passTestCases = 0;
            int grade;

            Report report = new Report();
            report.setProjectId(projectId);
            List<TestCase> testCases = new ArrayList<>();

            File reportXml = new File(project.getAbsolutePath()+"/target/surefire-reports/TEST-TestProject.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(reportXml);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("testcase");
            totalTestCases = nList.getLength();

            for(int i = 0; i < totalTestCases; i++)
            {
                TestCase caseBean = new TestCase();


                Node testcase = nList.item(i);
                if(testcase.hasChildNodes())
                {
                    Element caseEl = (Element)testcase;
                    caseBean.setCaseName(caseEl.getAttribute("name"));
                    caseBean.setPass(false);
                    caseBean.setMessage("FAILED: "+testcase.getTextContent().trim());
                    testCases.add(caseBean);

                }else {
                    passTestCases++;
                    Element caseEl = (Element)testcase;
                    caseBean.setCaseName(caseEl.getAttribute("name"));
                    caseBean.setPass(true);
                    caseBean.setMessage("PASS: ");
                    testCases.add(caseBean);
                }

                String caseName = caseBean.getCaseName().split(Pattern.quote("."))[1];
                System.out.println("CASE NAME: "+caseName);
                caseBean.setCaseDesc(getCaseDescByName(testClassContent, caseName));
            }

            System.out.println("PASS TEST CASES: "+(passTestCases * 100));
            System.out.println("TOTAL TEST CASES: "+totalTestCases);

            grade = (passTestCases * 100) / totalTestCases;
            report.setGrade(grade);
            report.setTestCases(testCases);
            report.setCompiled(true);
            return report;

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private String getCaseDescByName(List<String> content, String caseName){
        boolean searchDesc = false;
        for(String line : content){
            if(line.contains("void "+caseName+"()")){
                System.out.println("Setting true..");
                searchDesc = true;
            }
            if(searchDesc && line.contains("@DisplayName")){
                System.out.println("Returning: "+line.split("\"")[1].trim());
                return line.split("\"")[1].trim();
            }
        }
        return caseName;
    }

}
