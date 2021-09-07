package com.app.jira;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class JiraDocumentTest {

    @Test
    public void whenAddTheNewIssue_SouldCreateTheIssue(){
        given()
            .cmdProvider(cmd -> {
                cmd.tag = "AT-1232";
                cmd.command = "START";
                cmd.comment = "comment";
            })
        .whenAddNewIssue()
        .then()
            .verifyStoragedIssues("AT-1232",1);
    }

    @Test
    public void whenAlreadyHasTheIssue_SouldUseTheSameIssue(){
        given()
            .issueProvider(issue -> {
                issue.tag = "AT-1232";
                issue.command = "START";
                issue.comment = "comment";
            })
            .cmdProvider(cmd -> {
                cmd.tag = "AT-1232";
                cmd.command = "START";
                cmd.comment = "comment";
            })
        .whenAddNewIssue()
        .then()
            .verifyStoragedIssues("AT-1232",1);
    }

    @Test
    public void whenAddTwoDifferentIssues_SouldStorageBothIssues(){
        given()
            .issueProvider(issue -> {
                issue.tag = "AT-1232";
                issue.command = "START";
                issue.comment = "comment";
            })
            .cmdProvider(cmd -> {
                cmd.tag = "RA-8781";
                cmd.command = "START";
                cmd.comment = "comment";
            })
        .whenAddNewIssue()
        .then()
            .verifyStoragedIssues("issue",2);
    }

    @Test
    public void whenDeletetheIssue_SouldRemoveTheIssue(){
        given()
            .issueProvider(issue -> {
                issue.tag = "AT-1232";
                issue.command = "START";
                issue.comment = "comment";
            })
            .cmdProvider(cmd -> {
                cmd.issueId = "1";
            })
        .whenDeleteIssue()
        .then()
            .verifyStoragedIssues("issue",0);
    }

    @Test
    public void whenUpdateSomeAttribute_SouldUpdateTheIssue(){
        given()
            .issueProvider(issue -> {
                issue.tag = "AT-1232";
                issue.command = "START";
                issue.comment = "comment";
            })
            .cmdProvider(cmd -> {
                cmd.issueId = "1";
                cmd.command = "START";
                cmd.value = "1630975805";
            })
        .whenUpdateAttribute()
        .then()
            .verifyAttributeIssue("issue","START", "1630975805");
    }

        @Test
    public void whenGenerateReport_SouldListAllIssues(){
        given()
            .issueProvider(issue -> {
                issue.tag = "AT-1232";
                issue.command = "START";
                issue.comment = "comment";
            })
            .issueProvider(issue -> {
                issue.tag = "AT-1233";
                issue.command = "START";
                issue.comment = "comment";
            })
            .cmdProvider(cmd -> {
                cmd.command = "LIST";
            })
        .whenGenerateReport()
        .then()
            .verifyListOfIssue(2);
    }

    private DSL given(){
        return new DSL();
    }

    private static class DSL {

        private final IssueProvider issueProvider;
        private final IssueProvider cmdProvider;
        private JiraDocument jiraDoc = JiraDocument.getInstance("xml.xml");

        public DSL() {
            this.issueProvider = new IssueProvider();
            this.cmdProvider = new IssueProvider();
            jiraDoc.setDocument(jiraDoc.initializeXmlFile());
        }

        private DSL issueProvider(Consumer <IssueProvider> issue) {
            issue.accept(issueProvider);
            jiraDoc.addNewIssue(issueProvider.command, issueProvider.tag, issueProvider.comment);
            return this;
        }

        private DSL cmdProvider(Consumer <IssueProvider> cmd) {
            cmd.accept(cmdProvider);
            return this;
        }

        public ThenDSL whenAddNewIssue() {
            jiraDoc.addNewIssue(cmdProvider.command, cmdProvider.tag, cmdProvider.comment);
            return new ThenDSL();
        }

        public ThenDSL whenDeleteIssue() {
            jiraDoc.deleteIssue(cmdProvider.issueId);
            return new ThenDSL();
        }

        public ThenDSL whenUpdateAttribute() {
            jiraDoc.setIssueAttribute(cmdProvider.issueId, cmdProvider.command, cmdProvider.value);
            return new ThenDSL();
        }

        public ThenDSL whenGenerateReport() {
            Map<Integer, List> report = jiraDoc.generateIssuesReport("month"+ Util.getLocalDate().getMonthValue());
            return new ThenDSL(report);
        }

        private class ThenDSL {

            private Map<Integer, List> report = new HashMap<>();

            public ThenDSL() { }

            public ThenDSL(Map<Integer, List> report) {
                this.report = report;
            }

            public ThenDSL then() {
                return this;
            }

            public void verifyStoragedIssues(String element, int issuesStoraged) {
                NodeList nodeRoot = jiraDoc.getDocument().getElementsByTagName(element);
                assertEquals(issuesStoraged, nodeRoot.getLength());
            }

            public void verifyAttributeIssue(String element, String attribute, String value) {
                NodeList nodeRoot = jiraDoc.getDocument().getElementsByTagName(element);
                Element issue = (Element) nodeRoot.item(0);
                assertEquals(value, issue.getAttribute(attribute));
            }

            public void verifyListOfIssue(int issues) {
                assertEquals(issues, report.size());
            }
        }



    }

    public static class IssueProvider {
         public String tag;
         public String issueId;
         public String command;
         public String comment;
         public String value;
    }

}
