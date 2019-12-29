package com.grademe.grademe.server;

import com.grademe.grademe.beans.Report;
import com.grademe.grademe.beans.TestCase;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Component
public class EvaluationEngine {

    private boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public void evaluateProject(File project) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            if (isWindows) {
                builder.command("cmd.exe", "/c", "mvn clean test");
            } else {
                builder.command("sh", "-c", "mvn clean test");
            }

            builder.directory(project);
            Process process = builder.start();
            streamResults(process.getInputStream());
            process.destroy();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void streamResults(InputStream inputStream){
        try{
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            inputStream.close();
            inputStreamReader.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Report getReportObject(long projectId){
        try{
            System.out.println("Getting Reports..");

            int totalTestCases;
            int passTestCases = 0;
            int grade;

            Report report = new Report();
            report.setProjectId(projectId);
            List<TestCase> testCases = new ArrayList<>();

            File reportXml = new File("src/main/resources/projects/project/target/surefire-reports/TEST-TestProject.xml");
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
            }

            grade = (passTestCases * 100) / totalTestCases;
            report.setGrade(grade);
            report.setTestCases(testCases);
            return report;

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
