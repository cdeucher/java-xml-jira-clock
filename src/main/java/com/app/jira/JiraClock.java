package com.app.jira;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

public class JiraClock {

    public static final String xmlFilePath = System.getProperty("user.home")+"\\.jiraXmlfile.xml";

    public static void main(String[] args) throws ParserConfigurationException, TransformerException {
        System.out.println(String.format("Path: %s", xmlFilePath));
        if( args.length <= 0){
            System.out.println ("Command Args: [start/stop] [issue number] [comment]");
            return;
        }

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
            //jiraDoc.createNewDocument(doc, "start","ISSUE-1251", "fazendo start");
            //jiraDoc.createNewDocument(doc, "stop","ISSUE-1251", "fazendo stop");
            jiraDoc.createNewDocument(doc, args[0], args[1], args[2]);

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
