package com.app.jira;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import static com.app.jira.JiraDocument.getLocalDate;

public class JiraClock {

    public static final String xmlFilePath = System.getProperty("user.home")+"/.jiraXmlfile.xml";
    //public static final String xmlFilePath = System.getProperty("user.dir")+"\\.jiraXmlfile.xml";

    public static void main(String[] args) throws ParserConfigurationException, TransformerException {
        System.out.println(String.format("Path: %s", xmlFilePath));
        if( args.length <= 0){
            System.out.println ("Command Args: [start/stop] [issue number] [comment]");
            return;
        }
        String command = getCommandFromArgs(args);
        String issue = (args.length >= 2) ?  args[1].toUpperCase()  : "";
        String comment = (args.length >= 3) ? args[2] : "";

        for (int i = 0; i < args.length; ++i) {
            System.out.println (String.format("args[%s]: %s", i, args[i]));
        }
        JiraDocument jiraDoc = JiraDocument.getInstance(xmlFilePath);
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

            if(command == ActionEnum.LIST.toString())
                listIssues(jiraDoc, doc);
            else
                newDocument(command, issue, comment, jiraDoc, doc);

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void listIssues(JiraDocument jiraDoc, Document doc) {
        LocalDate localDate = getLocalDate();
        jiraDoc.listMonths(doc, "month"+String.valueOf(localDate.getMonthValue()));
    }

    private static void newDocument(String command, String issue, String comment, JiraDocument jiraDoc, Document doc) throws TransformerException {
        Element currentDocument = jiraDoc.searchElementByName(doc, "Jira");
        if(issue.isEmpty()) {
            if (currentDocument != null)
                issue = jiraDoc.getAttributeValue(currentDocument, "currentIssue");
                if (issue.isEmpty())
                    throw new RuntimeException("Issue isEmpty");
        }
        jiraDoc.createNewDocument(doc, command, issue, comment);
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
