package com.app.jira;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;

import static com.app.jira.JiraDocument.*;

public class Report {

    public static Map<Integer, List> listDays(Element monthEntry) {
        Map<Integer, List> report = new HashMap<>();
        NodeList days = searchElementByName(monthEntry, "day");
        Element day;
        for (int i = 0; i < Objects.requireNonNull(days).getLength(); i++) {
            day = (Element) days.item(i);

            System.out.printf(" Day %s%n",getAttributeValue(day,"ITEM"));
            report.putAll(listIssues(day));
        }
        return report;
    }

    private static Map<Integer, List> listIssues(Element dayEntry) {
        Map<Integer, List> report = new HashMap<>();
        HashSet<Element> issues = getIssuesByMounth(dayEntry);
        issues.forEach(issue -> {
            NodeList issueEntrys = searchElementByName(issue, "issue");

            System.out.printf(" Issue %s%n", issue.getNodeName());
            report.putAll(listEntryIssues(issueEntrys));
        });
        return report;
    }

    private static Map<Integer, List> listEntryIssues(NodeList issueEntrys) {
        Map<Integer, List> report = new HashMap<>();
        Element issue;
        for (int i = 0; i < issueEntrys.getLength(); i++) {
            issue = (Element) issueEntrys.item(i);

            String startIssue = getAttributeValue(issue,ActionEnum.START.toString());
            String stopIssue  = getAttributeValue(issue,ActionEnum.STOP.toString());

            Integer id = Integer.parseInt(getAttributeValue(issue,"ID"));
            report.put(id, List.of(id, Util.extractTimeFromIssue(startIssue, stopIssue), issue.getTextContent()));
            System.out.printf(" Issue :%s (%s) %s -> %s , %s%n"
                    , getAttributeValue(issue,"ID")
                    , Util.extractTimeFromIssue(startIssue, stopIssue)
                    , Util.convertTimestampToDate( getAttributeValue(issue,"START"))
                    , Util.convertTimestampToDate( getAttributeValue(issue,"STOP"))
                    , issue.getTextContent() );
        }
        return report;
    }

}
