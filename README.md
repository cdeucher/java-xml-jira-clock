
## Task Time Manager

### Under construction

```shell
mvn test
mvn package
```

Usage:
```shell
Command Args: [start/stop] [issue] [comment]
Command Args: [set] [IssueID] [timestamp]
Command Args: [del] [IssueID]
Command Args: [list]

./target/Jira-1.0-SNAPSHOT.jar start ISSUE-BB "starting"
./target/Jira-1.0-SNAPSHOT.jar stop
./target/Jira-1.0-SNAPSHOT.jar list
./target/Jira-1.0-SNAPSHOT.jar set 4 STOP 1630714367979
./target/Jira-1.0-SNAPSHOT.jar del 1
```
