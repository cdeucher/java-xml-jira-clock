package com.app.jira;

import java.util.List;
import java.util.Map;

public class JiraClock {

    //public static final String xmlFilePath = System.getProperty("user.home")+"/.jiraXmlfile.xml";
    public static final String xmlFilePath = System.getProperty("user.dir")+"\\.jiraXmlfile.xml";
    public static final JiraDocument jiraDoc = JiraDocument.getInstance(xmlFilePath);

    public static void main(String[] args) {
        System.out.printf("Path: %s%n", xmlFilePath);
        if( args.length <= 0){
            System.out.println ("Command Args: [start/stop] [issue] [comment]");
            System.out.println ("Command Args: [set] [IssueID] [timestamp]");
            System.out.println ("Command Args: [del] [IssueID]");
            System.out.println ("Command Args: [list]");
            return;
        }
        String command = getCommandFromArgs(args);
        String issue = (args.length >= 2) ?  args[1].toUpperCase()  : "";
        String comment = (args.length >= 3) ? args[2] : "";
        String value = (args.length >= 4) ? args[3] : "";

        for (int i = 0; i < args.length; ++i) {
            System.out.printf("args[%s]: %s%n", i, args[i]);
        }

        if(command.equals(ActionEnum.LIST.toString()))
            generateIssuesReport();
        else if(command.equals(ActionEnum.SET.toString()))
            setIssueAttribute(issue, comment, value);
        else if(command.equals(ActionEnum.DEL.toString()))
            deleteIssue(issue);
        else
            addNewIssue(command, issue, comment);
    }

    private static void addNewIssue(String command, String tag, String comment) {
        jiraDoc.addNewIssue(command, tag, comment);
        jiraDoc.saveXmlFile();
    }

    private static void deleteIssue(String issue) {
        jiraDoc.deleteIssue(issue);
        jiraDoc.saveXmlFile();
    }

    private static void setIssueAttribute(String issueId, String attributeName, String attributeValue) {
        jiraDoc.setIssueAttribute(issueId, attributeName, attributeValue);
        jiraDoc.saveXmlFile();
    }

    private static void generateIssuesReport() {
        Map<Integer, List> report = jiraDoc.generateIssuesReport("month"+ Util.getLocalDate().getMonthValue());
        report.forEach((key,item) -> System.out.printf(" %s ", item.toString()));
    }

    private static String getCommandFromArgs(String[] args) {
        return args[0].toUpperCase();
    }


}
