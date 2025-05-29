import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import javax.jms.JMSException;
import javax.naming.NamingException;

public class Game {
    LeaderBoardHandler leaderBoardHandler;

    public Game(LeaderBoardHandler leaderBoardHandler) {
        this.leaderBoardHandler = leaderBoardHandler;
    }

    public void start() {
        String host = "localhost";
        GameQueueReceiver receiver = null;
        GameTopicPublisher publisher = null;
        try {

            receiver = new GameQueueReceiver(host);
            publisher = new GameTopicPublisher(host);

            ArrayList<String> cards = new ArrayList<>();
            cards.add("A");
            for (int i = 2; i <= 10; i++) {
                cards.add(Integer.toString(i));
            }
            cards.add("J");
            cards.add("Q");
            cards.add("K");

            while (true) {
                ArrayList<String> players = new ArrayList<>();

                // 1st player
                String receivedMessage = receiver.receiveMessage();
                while ( ( receivedMessage == null ) || ( ! receivedMessage.startsWith("ready") ) ) {
                    receivedMessage = receiver.receiveMessage();
                }
                players.add(receivedMessage.substring(6));
                LocalDateTime timeWhenFirstPlayerJoined = LocalDateTime.now();

                long timeSinceFirstPlayerJoined = Duration.between(timeWhenFirstPlayerJoined, LocalDateTime.now()).getSeconds();
                while ( ( players.size() < 2 ) || ( ( timeSinceFirstPlayerJoined < 10 ) && ( players.size() < 4 ) ) ) {
                    if (players.size() < 2) {
                        receivedMessage = receiver.receiveMessage();
                    } else {
                        receivedMessage = receiver.receiveMessage(10-timeSinceFirstPlayerJoined);
                    }
                    if ( ( receivedMessage != null ) && receivedMessage.startsWith("ready") ) {
                        players.add(receivedMessage.substring(6));
                    } else if ( ( receivedMessage != null ) && ( receivedMessage.startsWith("quit") ) ) {
                        String quittingPlayer = receivedMessage.substring(5);
                        players.remove(quittingPlayer);
                    }
                    timeSinceFirstPlayerJoined = Duration.between(timeWhenFirstPlayerJoined, LocalDateTime.now()).getSeconds();
                }

                Collections.shuffle(cards);
                
                String message = "start," + String.join(",", cards.subList(0, 4)) + "," + String.join(",", players);
                publisher.publishMessage(message);

                LocalDateTime timeWhenGameStarted = LocalDateTime.now();
                String winner = "";
                boolean isGameEnded = false;
                while ( ( ! isGameEnded ) && ( ! players.isEmpty() ) ) {
                    receivedMessage = receiver.receiveMessage();
                    if ( ( receivedMessage != null ) && ( receivedMessage.startsWith("answer") ) ) {
                        String[] answer = receivedMessage.split(",");
                        String user = answer[1];
                        String mathsExpression = answer[2];
                        if (TwentyFourChecker.isExpressionEqualTo24(mathsExpression, cards.subList(0, 4).toArray(new String[4]))) {
                            winner = user;
                            message = "winner," + winner + "," + mathsExpression;
                            isGameEnded = true;
                        } else {
                            message = "incorrect," + user;
                        }
                        publisher.publishMessage(message);
                    } else if ( ( receivedMessage != null ) && ( receivedMessage.startsWith("quit") ) ) {
                        String quittingPlayer = receivedMessage.substring(5);
                        players.remove(quittingPlayer);
                    }
                }

                int gameDuration = (int) Duration.between(timeWhenGameStarted, LocalDateTime.now()).getSeconds();
                System.out.println("Game ended in "+gameDuration+"s");

                if (isGameEnded) {
                    leaderBoardHandler.setNewPlayerRecord(winner, true, gameDuration);
                    players.remove(winner);
                }
                for ( String player : players ) {
                    leaderBoardHandler.setNewPlayerRecord(player, false, gameDuration);
                }
            }

        } catch (NamingException | JMSException e) {
			System.err.println("Program aborted");
		} finally {
			if(receiver != null) {
				try {
					receiver.close();
				} catch (Exception e) { }
			}
            if(publisher != null) {
				try {
					publisher.close();
				} catch (Exception e) { }
			}
		}
    }
}
