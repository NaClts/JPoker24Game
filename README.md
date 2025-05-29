# JPoker24Game
*A Java GUI application for online 24-game*

## Description

The JPoker24Game is written in Java and tested in Ubuntu 22.04, providing following main features:
* 2 to 4 players playing 24-game remotely and simultaneously
* Player registration and authentication
* Tracking players' performance

The following technologies are utilized:
* Java Remote Method Invocation (RMI)
* Java Database Connectivity (JDBC)
* Java Message Service (JMS)

## Screenshots
![Login](https://raw.githubusercontent.com/NaClts/JPoker24Game/refs/heads/main/screenshots/1_login.png)
![Register](https://raw.githubusercontent.com/NaClts/JPoker24Game/refs/heads/main/screenshots/2_register.png)
![User Profile](https://raw.githubusercontent.com/NaClts/JPoker24Game/refs/heads/main/screenshots/3_user_profile.png)
![Game Initial Stage](https://raw.githubusercontent.com/NaClts/JPoker24Game/refs/heads/main/screenshots/4_game_initial.png)
![Game Joining Stage](https://raw.githubusercontent.com/NaClts/JPoker24Game/refs/heads/main/screenshots/5_game_joining.png)
![Game Playing Stage](https://raw.githubusercontent.com/NaClts/JPoker24Game/refs/heads/main/screenshots/6_game_playing.png)
![Game Over Stage](https://raw.githubusercontent.com/NaClts/JPoker24Game/refs/heads/main/screenshots/7_game_over.png)
![Leaderboard](https://raw.githubusercontent.com/NaClts/JPoker24Game/refs/heads/main/screenshots/8_leaderboard.png)

## Installation / Compilation

1. Make sure the following dependencies are deployed and up on your Ubuntu Desktop:
* `openjdk-8-jre` and `openjdk-8-jdk`
* [MySQL](https://documentation.ubuntu.com/server/how-to/databases/install-mysql/index.html)
* [MySQL JDBC Driver](http://dev.mysql.com/downloads/connector/j/)
* [GlassFish 5 (JMS provider; Download "Full Platform")](https://javaee.github.io/glassfish/download)

2. Prepare the MySQL database:
```sql
CREATE DATABASE c3358;

GRANT ALL ON c3358.* TO ‘c3358@localhost' IDENTIFIED BY ‘c3358PASS';

USE c3358;

CREATE TABLE `UserInfo` (
  `name` varchar(32) NOT NULL,
  `password` varchar(32) NOT NULL,
  PRIMARY KEY (`name`)
)

CREATE TABLE `OnlineUser` (
  `name` varchar(32) NOT NULL,
  PRIMARY KEY (`name`)
)

CREATE TABLE LeaderBoard (
    name varchar(32) NOT NULL,
    games_won int NOT NULL,
    games_played int NOT NULL,
    total_winning_time int NOT NULL,
    PRIMARY KEY (`name`)
)

CREATE VIEW LeaderBoardView AS
SELECT 
    *,
    RANK() OVER (ORDER BY games_won DESC, avg_winning_time ASC) AS player_rank
FROM (
    SELECT
        name,
        games_won,
        games_played,
        total_winning_time / games_won AS avg_winning_time
    FROM LeaderBoard
) AS RankedLeaderBoard ORDER BY player_rank;
```

3. Create the following objects in GlassFish 5...
Connection factory:
* jms/JPoker24GameConnectionFactory
* jms/JPoker24GameTopicConnectionFactory
Destination:
* jms/JPoker24GameQueue
* jms/JPoker24GameTopic

4. Download the repository to your PC.

5. Unzip the repository.

6. Change directory to "src" folder.

7. Add JDBC & GlassFish JAR files to CLASSPATH environment variable: (Please replace the path to the JAR files!)
```export CLASSPATH=$CLASSPATH:/ … /mysql-connector-j-9.3.0.jar```
```export CLASSPATH=$CLASSPATH:/ … /glassfish5/glassfish/lib/gf-client.jar```

8. Compile the Java source files:
```javac *.java```

## Execution

1. Run RMI registry:
```rmiregistry &```

2. Add JDBC & GlassFish JAR files to CLASSPATH environment variable: (Please replace the path to the JAR files!)
```export CLASSPATH=$CLASSPATH:/ … /mysql-connector-j-9.3.0.jar```
```export CLASSPATH=$CLASSPATH:/ … /glassfish5/glassfish/lib/gf-client.jar```

3. Run main server program:
```java -Djava.security.policy=../security.policy JPoker24GameServer```

4. Run main client program:
```java -Djava.security.policy=../security.policy JPoker24Game```

## Remarks

This project is submitted as HKU COMP3358 Course Assignment.
