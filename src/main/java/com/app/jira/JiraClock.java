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

    public static final String xmlFilePath = System.getProperty("user.dir")+"\\xmlfile.xml";

    public static void main(String[] args) throws ParserConfigurationException, TransformerException {
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
            //jiraDoc.createNewDocument(doc, "ISSUE-1251", "fazendo x");
            jiraDoc.createNewDocument(doc, args[0], args[1]);

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
