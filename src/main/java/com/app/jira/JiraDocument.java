package com.app.jira;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
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

    protected void createNewDocument(Document document, String command, String issue, String value) throws TransformerException {
        populateXmlDocument(document, command, issue, value);
        saveXmlFile(document);
        System.out.println("Done creating XML File");
    }

    private void populateXmlDocument(Document document, String command, String issue, String value) {
        LocalDate localDate = getLocalDate();


        Element root = createOrSearchRootElement(document, "Jira");

        setCurrentIssue(document, root, issue);

        Element monthEntry = createOrSearchElement(document, root,"month"+String.valueOf(localDate.getMonthValue()));
        Element dayEntry = createOrSearchElement(document, monthEntry,"day"+String.valueOf(localDate.getDayOfMonth()));
        Element issueEntry = createOrSearchElement(document, dayEntry, issue);

        Element entryValue = document.createElement("issue");
        entryValue.appendChild(document.createTextNode(value));

        Attr attrCommand = document.createAttribute("action");
        attrCommand.setValue(command);
        entryValue.setAttributeNode(attrCommand);

        Attr attr = document.createAttribute("date");
        attr.setValue(getDateWithoutTimeUsingCalendar().toString());
        entryValue.setAttributeNode(attr);

        issueEntry.appendChild(entryValue);

    }

    public static Date getDateWithoutTimeUsingCalendar() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    public static Date getDateWithoutTimeUsingFormat() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.parse(formatter.format(new Date()));
    }

    public static LocalDate getLocalDate() {
        return LocalDate.now();
    }

    static void setCurrentIssue(Document document, Element root, String issue) {
        Attr attrCurrentIssue = document.createAttribute("currentIssue");
        attrCurrentIssue.setValue(issue);
        root.setAttributeNode(attrCurrentIssue);
    }

    public static String getCurrentIssue(Element root){
        return root.getAttribute("currentIssue");
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
        Element root = searchElementByName(document, element);
        if (root == null) {
            root = document.createElement(element);
            rootElement.appendChild(root);
        }
        return root;
    }

    public static Element searchElementByName(Document document, String attribute){
        NodeList nodeRoot = document.getElementsByTagName(attribute);
        if (nodeRoot.getLength() > 0)
            return (Element) nodeRoot.item(0);
        else
            return null;
    }

    private void saveXmlFile(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(xmlFilePath));
        transformer.transform(domSource, streamResult);
    }
}
