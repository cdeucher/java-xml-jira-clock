package com.app.jira;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.*;


public final class JiraDocument extends Report {

    private final String xmlFilePath;
    private static JiraDocument instance;
    private Document doc;

    private JiraDocument(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
        this.doc = initializeXmlFile();
    }

    public static JiraDocument getInstance(String xmlFilePath){
        if(instance == null){
            instance = new JiraDocument(xmlFilePath);
        }
        return instance;
    }

    protected Document initializeXmlFile() {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            documentFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

            File xmlFile = new File(xmlFilePath);
            if (xmlFile.exists()) {
                return loadXmlDocument(documentBuilder);
            } else {
                return documentBuilder.newDocument();
            }
        }catch (IOException | SAXException | ParserConfigurationException e){
            e.printStackTrace();
            return null;
        }
    }

    protected Document loadXmlDocument(DocumentBuilder documentBuilder) throws SAXException, IOException {
        Document doc = documentBuilder.parse(xmlFilePath);
        doc.getDocumentElement().normalize();

        System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
        System.out.println("------");
        return doc;
    }

    protected Document getDocument(){
        return doc;
    }

    protected void setDocument(Document newDoc){
        doc = newDoc;
    }

    private void processCommandCmd(String command, String comment, Element issueEntry, Element monthEntry) {
        Element lastIssue = getElementByName(monthEntry, "issue");
        if( Objects.isNull(lastIssue) )
            lastIssue = createOrSearchElement(doc, issueEntry, "issue");

        String startIssue = getAttributeValue(lastIssue, ActionEnum.START.toString());
        String stopIssue = getAttributeValue(lastIssue, ActionEnum.STOP.toString());

        if(command.equals(ActionEnum.START.toString())){
            Element newIssue = (getAttributeValue(lastIssue,"ID").isEmpty()) ? lastIssue : doc.createElement("issue");
            setIssueId(lastIssue, newIssue);
            addNewEntryIssue(doc, comment, issueEntry, newIssue);
        }else if(command.equals(ActionEnum.STOP.toString()) && stopIssue.isEmpty() && !startIssue.isEmpty()) {
            closeEntryIssue(doc, command, lastIssue);
        }else{
            throw new RuntimeException(String.format("Invalid command! start:%s, stop:%s", startIssue, stopIssue));
        }
    }

    private void setIssueId(Element lastIssue, Element newIssue) {
        String id = getAttributeValue(lastIssue,"ID");
        int issueId = id.isBlank() ? 1 : Integer.parseInt(id)+1;
        setAttributeValue(newIssue, "ID", String.valueOf(issueId));
    }

    private void closeEntryIssue(Document document, String command, Element lastIssue){
        Attr attrCommand = document.createAttribute(command);
        attrCommand.setValue(Util.getDateWithoutTimeUsingCalendar().toString());
        lastIssue.setAttributeNode(attrCommand);
    }

    private void addNewEntryIssue(Document document, String comment, Element issueEntry, Element lastIssue) {
        issueEntry.appendChild(lastIssue);
        lastIssue.appendChild(document.createTextNode(comment));

        Attr attrCommand = document.createAttribute(ActionEnum.START.toString());
        attrCommand.setValue(Util.getDateWithoutTimeUsingCalendar().toString());
        lastIssue.setAttributeNode(attrCommand);
    }

    private Element createNewElementWithAttr(Document document, Element rootElement, String tag, String attr) {
        Element newElement = document.createElement(tag);
        rootElement.appendChild(newElement);

        Attr attrCommand = document.createAttribute("ITEM");
        attrCommand.setValue(String.valueOf(attr));
        newElement.setAttributeNode(attrCommand);

        return newElement;
    }

    protected void addNewIssue(String command, String issue, String comment) {
        Element rootDocument = createOrSearchRootDocument(doc, "Jira");
        if(issue.isEmpty())
            issue = checkCurrentIssue(rootDocument);

        try {
            populateXmlDocument(rootDocument, command, issue, comment);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void populateXmlDocument(Element rootDocument, String command, String issue, String comment) {
        setAttributeValue(rootDocument, "currentIssue", issue);

        Element monthEntry = createOrSearchElement(doc, rootDocument,"month"+ Util.getLocalDate().getMonthValue());
        Element dayEntry = searchElementByAttribute(monthEntry, "day","ITEM", String.valueOf(Util.getLocalDate().getDayOfMonth()));
        if( Objects.isNull(dayEntry) )
            dayEntry = createNewElementWithAttr(doc, monthEntry,"day",String.valueOf(Util.getLocalDate().getDayOfMonth()));
        Element issueEntry = createOrSearchElement(doc, dayEntry, issue);

        processCommandCmd(command, comment, issueEntry, monthEntry);

    }

    private String checkCurrentIssue(Element rootDocument) {
        String issue = getAttributeValue(rootDocument, "currentIssue");
        if (issue.isEmpty())
            throw new RuntimeException("Document isEmpty");
        return issue;
    }

    public void setAttributeValue(Element element, String attributeName, String attributeValue){
        Attr attrCurrentIssue = doc.createAttribute(attributeName);
        attrCurrentIssue.setValue(String.valueOf(attributeValue));
        element.setAttributeNode(attrCurrentIssue);
    }

    public static String getAttributeValue(Element element, String attribute){
        return element.getAttribute(attribute);
    }

    public Element createOrSearchRootDocument(Document document, String element) {
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
        Element root = getElementByName(rootElement, element);
        if (root == null) {
            root = document.createElement(element);
            rootElement.appendChild(root);
        }
        return root;
    }

    public static Element getElementByName(Element rootElement, String elementTag) {
        NodeList nodeRoot = rootElement.getElementsByTagName(elementTag);
        if (nodeRoot.getLength() > 0)
            return (Element) nodeRoot.item(nodeRoot.getLength()-1);
        else
            return null;
    }

    public static NodeList searchElementByName(Element rootElement, String elementTag) {
        NodeList nodeRoot = rootElement.getElementsByTagName(elementTag);
        if (nodeRoot.getLength() > 0)
            return nodeRoot;
        else
            return null;
    }

    public static HashSet<Element> getIssuesByMounth(Element rootElement) {
        HashSet<Element> listOfIssues = new HashSet<>();
        for(int i=0; i< rootElement.getChildNodes().getLength(); i++) {
            if(!rootElement.getChildNodes().item(i).getNodeName().equals("#text"))
                listOfIssues.add ((Element) rootElement.getChildNodes().item(i));
        }
        return listOfIssues;
    }

    public static Element searchElementByAttribute(Element rootElement, String elementTag, String attributeName, String attributeValue) {
        NodeList nodeRoot = rootElement.getElementsByTagName(elementTag);
        if (nodeRoot.getLength() > 0) {
            Element element;
            String value;
            for(int i=0; i < nodeRoot.getLength(); i++){
                element = (Element) nodeRoot.item(i);
                value = getAttributeValue(element, attributeName);
                if(Integer.parseInt(value) == Integer.parseInt(attributeValue))
                    return element;
            }
        }
        return null;
    }

    protected Map<Integer, List> generateIssuesReport(String month) {
        Map<Integer, List> report = new HashMap<>();
        Element rootDocument = createOrSearchRootDocument(doc, "Jira");
        Element monthEntry = getElementByName(rootDocument, month);

        report.putAll(listDays(monthEntry));
        return report;
    }

    protected void saveXmlFile() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(new File(xmlFilePath));
            transformer.transform(domSource, streamResult);
            System.out.println("Done creating XML File");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setIssueAttribute(String issueId, String attributeName, String attributeValue) {
        Element rootDocument = createOrSearchRootDocument(doc, "Jira");
        Element issue = searchElementByAttribute(rootDocument, "issue","ID",issueId);

        setAttributeValue(issue,attributeName,attributeValue);
    }

    public void deleteIssue(String issueId) {
        Element rootDocument = createOrSearchRootDocument(doc, "Jira");
        Element issue = searchElementByAttribute(rootDocument, "issue","ID",issueId);

        issue.getParentNode().removeChild(issue);
    }
}
