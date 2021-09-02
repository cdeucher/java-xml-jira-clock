package com.app.jira;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;


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

    private void populateXmlDocument(Document document, String command, String issue, String comment) {
        Element root = createOrSearchRootDocument(document, "Jira");

        setCurrentIssue(document, root, issue);

        Element monthEntry = createOrSearchElement(document, root,"month"+ Util.getLocalDate().getMonthValue());
        Element dayEntry = searchElementByAttribute(monthEntry, "day",String.valueOf(Util.getLocalDate().getDayOfMonth()));
        if(dayEntry == null)
            dayEntry = createNewElementWithAttr(document, monthEntry,"day",String.valueOf(Util.getLocalDate().getDayOfMonth()));
        Element issueEntry = createOrSearchElement(document, dayEntry, issue);

        updateAttributeOrCreateNewOne(document, command, comment, issueEntry);

    }

    private Element createNewElementWithAttr(Document document, Element rootElement, String tag, String attr) {
        Element newElement = document.createElement(tag);
        rootElement.appendChild(newElement);

        Attr attrCommand = document.createAttribute("ITEM");
        attrCommand.setValue(String.valueOf(attr));
        newElement.setAttributeNode(attrCommand);

        return newElement;
    }

    private void listDays(Element monthEntry) {
        NodeList days = searchElementByName(monthEntry, "day");
        Element day;
        for (int i = 0; i < Objects.requireNonNull(days).getLength(); i++) {
             day = (Element) days.item(i);

            System.out.printf(" Day %s%n",getAttributeValue(day,"ITEM"));
            listIssues(day);
        }
    }

    private void listIssues(Element dayEntry) {
        HashSet<Element> issues = getIssuesByMounth(dayEntry);
        issues.forEach(issue -> {
            NodeList issueEntrys = searchElementByName(issue, "issue");

            System.out.printf(" Issue %s%n", issue.getNodeName());
            listEntryIssues(issueEntrys);
        });
    }

    private void listEntryIssues(NodeList issueEntrys) {
        Element issue;
        for (int i = 0; i < issueEntrys.getLength(); i++) {
            issue = (Element) issueEntrys.item(i);

            String startIssue = getAttributeValue(issue,ActionEnum.START.toString());
            String stopIssue  = getAttributeValue(issue,ActionEnum.STOP.toString());

            System.out.printf(" Issue (%s) : %s -> %s , %s%n"
                    , Util.extractTimeFromIssue(startIssue, stopIssue)
                    , Util.convertTimestampToDate( getAttributeValue(issue,"START"))
                    , Util.convertTimestampToDate( getAttributeValue(issue,"STOP"))
                    , issue.getTextContent() );
        }
    }

    protected void generateIssuesReport(Document document, String month) {
        Element root = createOrSearchRootDocument(document, "Jira");
        Element monthEntry = getElementByName(root, month);

        listDays(monthEntry);
    }

    protected void addNewIssue(Document doc, String command, String issue, String comment) {
        Element currentDocument = searchElementByName(doc, "Jira");
        if(issue.isEmpty()) {
            if (currentDocument != null)
                issue = getAttributeValue(currentDocument, "currentIssue");
            if (issue.isEmpty())
                throw new RuntimeException("Issue isEmpty");
        }
        try {
            populateXmlDocument(doc, command, issue, comment);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateAttributeOrCreateNewOne(Document document, String command, String comment, Element issueEntry) {
        Element lastIssue = createOrSearchElement(document, issueEntry, "issue");
        String startIssue = getAttributeValue(lastIssue,ActionEnum.START.toString());
        String stopIssue  = getAttributeValue(lastIssue,ActionEnum.STOP.toString());

        if(command.equals(ActionEnum.START.toString()) && !startIssue.isEmpty() && !stopIssue.isEmpty()){
            Element entryValue = document.createElement("issue");
            addNewEntryIssue(document, comment, issueEntry, entryValue);
        }else if(command.equals(ActionEnum.START.toString()) && startIssue.isEmpty()) {
            addNewEntryIssue(document, comment, issueEntry, lastIssue);
        }else if(command.equals(ActionEnum.STOP.toString()) && stopIssue.isEmpty() && !startIssue.isEmpty()) {
            closeEntryIssue(document, command, lastIssue);
        }else{
            throw new RuntimeException(String.format("Invalid command! start:%s, stop:%s", startIssue, stopIssue));
        }
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

    static void setCurrentIssue(Document document, Element root, String issue) {
        Attr attrCurrentIssue = document.createAttribute("currentIssue");
        attrCurrentIssue.setValue(issue);
        root.setAttributeNode(attrCurrentIssue);
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

    public static Element searchElementByName(Document document, String attribute) {
        NodeList nodeRoot = document.getElementsByTagName(attribute);
        if (nodeRoot.getLength() > 0)
            return (Element) nodeRoot.item(0);
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

    public static Element searchElementByAttribute(Element rootElement, String elementTag, String attribute) {
        NodeList nodeRoot = rootElement.getElementsByTagName(elementTag);
        if (nodeRoot.getLength() > 0) {
            Element element;
            String value;
            for(int i=0; i < nodeRoot.getLength(); i++){
                element = (Element) nodeRoot.item(i);
                value = getAttributeValue(element, "ITEM");
                if(Integer.parseInt(value) == Integer.parseInt(attribute))
                    return element;
            }
        }
        return null;
    }

    protected void saveXmlFile(Document document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(xmlFilePath));
            transformer.transform(domSource, streamResult);
            System.out.println("Done creating XML File");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
