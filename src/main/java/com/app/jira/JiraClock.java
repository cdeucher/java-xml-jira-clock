package com.app.jira;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class JiraClock {

    private static Logger LOG = LoggerFactory.getLogger(JiraClock.class);
    public static final String xmlFilePath = "C:\\Dev\\Java\\Jira\\xmlfile.xml";

    public static void main(String[] args) throws ParserConfigurationException, TransformerException {
        LOG.info("EXECUTING : command line runner");
        for (int i = 0; i < args.length; ++i) {
            LOG.info("args[{}]: {}", i, args[i]);
        }
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        documentFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

        try {
            File xmlFile = new File(xmlFilePath);
            Document doc;

            boolean exists = xmlFile.exists();
            if(exists) {
                doc = loadXmlDocument(xmlFile, documentBuilder);
            }else{
                doc = documentBuilder.newDocument();
            }
            createNewDocument(doc);

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Document loadXmlDocument(File xmlFile, DocumentBuilder documentBuilder) throws SAXException, IOException {
        Document doc = documentBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
        System.out.println("------");
        return doc;
    }

    private static void createNewDocument(Document document) throws TransformerException {
        populateXmlDocument(document);
        saveXmlFile(document);
        System.out.println("Done creating XML File");
    }

    private static void populateXmlDocument(Document document) {
        Element root = createOrSearchRootElement(document);
        // employee element
        Element employee = document.createElement("employee");

        root.appendChild(employee);

        // set an attribute to staff element
        Attr attr = document.createAttribute("id");
        attr.setValue("10");
        employee.setAttributeNode(attr);

        // firstname element
        Element firstName = document.createElement("firstname");
        firstName.appendChild(document.createTextNode("James"));
        employee.appendChild(firstName);

        // lastname element
        Element lastname = document.createElement("lastname");
        lastname.appendChild(document.createTextNode("Harley"));
        employee.appendChild(lastname);

        // email element
        Element email = document.createElement("email");
        email.appendChild(document.createTextNode("james@example.org"));
        employee.appendChild(email);

        // department elements
        Element department = document.createElement("department");
        department.appendChild(document.createTextNode("Human Resources"));
        employee.appendChild(department);
    }

    private static Element createOrSearchRootElement(Document document) {
        NodeList nodeRoot = document.getElementsByTagName("company");
        Element root;
        if(nodeRoot.getLength() > 0){
            root = nodeRoot.item(0).getOwnerDocument().getDocumentElement();
        }else{
            root = document.createElement("company");
            document.appendChild(root);
        }
        return root;
    }

    private static void saveXmlFile(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(xmlFilePath));

        transformer.transform(domSource, streamResult);
    }


}
