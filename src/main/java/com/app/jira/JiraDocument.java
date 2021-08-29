package com.app.jira;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.Date;


public final class JiraDocument {

    private final String xmlFilePath;
    private static JiraDocument instance;

    public JiraDocument(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
    }

    public static JiraDocument getInstance(String xmlFilePath){
        if(instance == null){
            instance = new JiraDocument(xmlFilePath);
        }
        return instance;
    }

    protected Document loadXmlDocument(File xmlFile, DocumentBuilder documentBuilder) throws SAXException, IOException {
        Document doc = documentBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
        System.out.println("------");
        return doc;
    }

    protected void createNewDocument(Document document, String issue, String value) throws TransformerException {
        populateXmlDocument(document, issue, value);
        saveXmlFile(document);
        System.out.println("Done creating XML File");
    }

    private void populateXmlDocument(Document document, String issue, String value) {
        Date date = new Date();

        Element root = createOrSearchRootElement(document, "Jira");

        Element monthEntry = createOrSearchElement(document, root,"month"+String.valueOf(date.getMonth()));
        Element dayEntry = createOrSearchElement(document, monthEntry,"day"+String.valueOf(date.getDay()));
        Element issueEntry = createOrSearchElement(document, dayEntry, issue);

        Attr attr = document.createAttribute("date");
        attr.setValue(date.toString());
        issueEntry.setAttributeNode(attr);

        Element entryValue = document.createElement("issue");
        entryValue.appendChild(document.createTextNode(value));
        issueEntry.appendChild(entryValue);

    }

    private Element createOrSearchRootElement(Document document, String element) {
        NodeList nodeRoot = document.getElementsByTagName(element);
        Element root;
        if (nodeRoot.getLength() > 0) {
            root = (Element) nodeRoot.item(0);
        } else {
            root = document.createElement(element);
            document.appendChild(root);
        }
        return root;
    }

    private Element createOrSearchElement(Document document, Element rootElement, String element) {
        NodeList nodeRoot = document.getElementsByTagName(element);
        Element root;
        if (nodeRoot.getLength() > 0) {
            root = (Element) nodeRoot.item(0);
        } else {
            root = document.createElement(element);
            rootElement.appendChild(root);
        }
        return root;
    }

    private void saveXmlFile(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(xmlFilePath));

        transformer.transform(domSource, streamResult);
    }
}
