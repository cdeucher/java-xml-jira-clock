package com.app.jira;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class JiraClock {

    public static final String xmlFilePath = System.getProperty("user.home")+"/.jiraXmlfile.xml";
    //public static final String xmlFilePath = System.getProperty("user.dir")+"\\.jiraXmlfile.xml";
    public static final JiraDocument jiraDoc = JiraDocument.getInstance(xmlFilePath);

    public static void main(String[] args) throws ParserConfigurationException {
        System.out.printf("Path: %s%n", xmlFilePath);
        if( args.length <= 0){
            System.out.println ("Command Args: [start/stop] [issue number] [comment]");
            return;
        }
        String command = getCommandFromArgs(args);
        String issue = (args.length >= 2) ?  args[1].toUpperCase()  : "";
        String comment = (args.length >= 3) ? args[2] : "";

        for (int i = 0; i < args.length; ++i) {
            System.out.printf("args[%s]: %s%n", i, args[i]);
        }
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        documentFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

        try {
            File xmlFile = new File(xmlFilePath);
            Document doc;

            boolean exists = xmlFile.exists();
            if(exists) {
                doc = jiraDoc.loadXmlDocument(xmlFile, documentBuilder);
            }else{
                doc = documentBuilder.newDocument();
            }

            if(command.equals(ActionEnum.LIST.toString()))
                generateIssuesReport(doc);
            else
                addNewIssue(doc, command, issue, comment);

        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateIssuesReport(Document doc) {
        jiraDoc.generateIssuesReport(doc, "month"+ Util.getLocalDate().getMonthValue());
    }

    private static void addNewIssue(Document doc,String command, String issue, String comment) {
        jiraDoc.addNewIssue(doc, command, issue, comment);
        jiraDoc.saveXmlFile(doc);
    }

    private static String getCommandFromArgs(String[] args) {
        if(args[0].toUpperCase().equals(ActionEnum.START.toString()))
            return ActionEnum.START.toString();
        else if(args[0].toUpperCase().equals(ActionEnum.STOP.toString()))
            return ActionEnum.STOP.toString();
        else
            return ActionEnum.LIST.toString();
    }


}
