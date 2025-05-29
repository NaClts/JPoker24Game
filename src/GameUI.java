import java.awt.*;
import java.awt.event.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Arrays;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class GameUI {

    private String loginName;
    private JFrame frame = new JFrame("JPoker 24-Game");
    private static final Font H1 = new Font("Arial", Font.BOLD, 24);
    private static final Font H2 = new Font("Arial", Font.BOLD, 20);
    private static final Font H3 = new Font("Arial", Font.BOLD, 16);
    private UserManage userManager;

    private String host = "localhost";
    private GameQueueSender sender = null;
    private GameTopicSubscriber subscriber = null;

    private boolean isPlayingGame = false;
    private boolean isGameStarted = false;
    private String[] cards = {"A", "2", "3", "4"};
    private JTextField answerTextField;

    private LeaderBoardHandle leaderBoardHandler;

    private String[] players;

    public GameUI() {
        try {
            userManager = (UserManage) Naming.lookup("UserManager");
            leaderBoardHandler = (LeaderBoardHandle) Naming.lookup("LeaderBoardHandler");
        } catch (Exception e) {
            System.err.println("Failed accessing RMI: "+e);
        }
        try {
            sender = new GameQueueSender(host);
            subscriber = new GameTopicSubscriber(host);
            subscriber.setMessageListener(new GameTopicListener());
            subscriber.start();
        } catch (NamingException | JMSException e) {
            System.err.println("Failed accessing JMS: "+e);
        }
    }
    public GameUI(String loginName) {
        this();
        this.loginName = loginName;
    }

    private void close() {
        try {
            userManager.logout(loginName);
        } catch (RemoteException re) {
            System.err.println("Failed invoking RMI: "+re);
        }
        if(sender != null) {
            try {
                if (isPlayingGame) {
                    sender.sendMessages(("quit,"+loginName));
                }
                sender.close();
                subscriber.close();
            } catch (Exception e) {
                System.err.println("Failed invoking JMS: "+e);
            }
        }
        frame.dispose();
    }

    public void open() {
        frame.setSize(700, 400);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4));

        JButton userProfileButton = new JButton("User Profile");
        JButton playGameButton = new JButton("Play Game");
        JButton leaderBoardButton = new JButton("Leader Board");
        JButton logoutButton = new JButton("Logout");

        userProfileButton.addActionListener(new UserProfileButtonListener());
        playGameButton.addActionListener(new PlayGameButtonListener());
        leaderBoardButton.addActionListener(new LeaderBoardButtonListener());
        logoutButton.addActionListener(new LogoutButtonListener());

        buttonPanel.add(userProfileButton);
        buttonPanel.add(playGameButton);
        buttonPanel.add(leaderBoardButton);
        buttonPanel.add(logoutButton);
        frame.add(buttonPanel, BorderLayout.NORTH);

        JPanel userProfilePanel = getUserProfilePanel();
        frame.add(userProfilePanel, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
                System.exit(0);
            }
        });

        frame.setVisible(true);
    }

    private JPanel getUserProfilePanel() {
        JPanel userProfilePanel = new JPanel();
        userProfilePanel.setLayout(new BoxLayout(userProfilePanel, BoxLayout.Y_AXIS));
        userProfilePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel loginNameLabel = new JLabel(loginName);
        loginNameLabel.setFont(H1);

        JLabel numberOfWinsLabel = new JLabel("Number of wins: ");
        numberOfWinsLabel.setFont(H3);

        JLabel numberOfGamesLabel = new JLabel("Number of games: ");
        numberOfGamesLabel.setFont(H3);

        JLabel averageTimeToWinLabel = new JLabel("Average time to win: ");
        averageTimeToWinLabel.setFont(H3);

        JLabel rankLabel = new JLabel("Rank: #");
        rankLabel.setFont(H2);

        try {
            PlayerStatistics playerStatistics = leaderBoardHandler.getPlayerStatistics(loginName);
            numberOfWinsLabel.setText( numberOfWinsLabel.getText() + playerStatistics.games_won );
            numberOfGamesLabel.setText( numberOfGamesLabel.getText() + playerStatistics.ganes_played );
            averageTimeToWinLabel.setText( averageTimeToWinLabel.getText() + playerStatistics.avg_winning_time + "s");
            rankLabel.setText( rankLabel.getText() + playerStatistics.player_rank );
        } catch (RemoteException e) {
            System.err.println("Failed invoking RMI: "+e);
        }

        userProfilePanel.add(loginNameLabel);
        userProfilePanel.add(numberOfWinsLabel);
        userProfilePanel.add(numberOfGamesLabel);
        userProfilePanel.add(averageTimeToWinLabel);
        userProfilePanel.add(rankLabel);
        return userProfilePanel;
    }

    class UserProfileButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            frame.getContentPane().remove(
                ((BorderLayout)frame.getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER)
            );
            JPanel userProfilePanel = getUserProfilePanel();
            frame.add(userProfilePanel, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();
        }
    }

    class LeaderBoardButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            frame.getContentPane().remove(
                ((BorderLayout)frame.getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER)
            );
            try {
                String[][] data = leaderBoardHandler.getLeaderBoard();
                String[] columnNames = {"Rank", "Player", "Games won", "Games played", "Avg. winning time"};
                JTable leaderBoardTable = new JTable(data, columnNames) {@Override public boolean isCellEditable(int row, int column) {return false;}};
                JScrollPane leaderBoardScrollPane = new JScrollPane(leaderBoardTable);

                frame.add(leaderBoardScrollPane, BorderLayout.CENTER);
                frame.revalidate();
                frame.repaint();
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(
                    frame, 
                    ("Failed invoking RMI. Please try again later: "+e), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    class LogoutButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            close();
            LoginUI loginUI = new LoginUI();
            loginUI.open();
        }
    }

    class PlayGameButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if ( ! isPlayingGame ) {
                frame.getContentPane().remove(
                    ((BorderLayout)frame.getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER)
                );
                JButton newGameButton = new JButton("New game");
                newGameButton.addActionListener(new NewGameButtonListener());
                frame.add(newGameButton, BorderLayout.CENTER);
                frame.revalidate();
                frame.repaint();
            } else if ( ! isGameStarted ) {
                repaintFrameWithWaitingForPlayers();
            } else {
                repaintFrameWithPlayGame();
            }
        }
    }

    class NewGameButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            repaintFrameWithWaitingForPlayers();
            isPlayingGame = true;
            try {
                sender.sendMessages( ("ready,"+loginName) );
            } catch (Exception e) {
                System.err.println("Failed invoking JMS: "+e);
            }
        }
    }

    class GameTopicListener implements MessageListener {
        @Override
        public void onMessage(Message message) {
            TextMessage textMessage = null;
            try {
                if (message instanceof TextMessage) {
                    textMessage = (TextMessage) message;
                    String stringifiedMessage = textMessage.getText();
                    System.out.println("Received message: "+stringifiedMessage);

                    if (isPlayingGame) {
                        if (stringifiedMessage.startsWith("start")) {
                            String[] splittedMessages = stringifiedMessage.split(",");  // 0:"start" , 1:"{card_1}" , ... , 4:"{card_4}" , 5:"{player_1}" , ...
                            cards = Arrays.copyOfRange(splittedMessages, 1, 5);
                            players = Arrays.copyOfRange(splittedMessages, 5, splittedMessages.length);
                            isGameStarted = true;
                            repaintFrameWithPlayGame();
                        } else if (stringifiedMessage.startsWith("incorrect")) {
                            if (stringifiedMessage.substring(10).equals(loginName)) {
                                JOptionPane.showMessageDialog(
                                    frame, 
                                    "Wrong answer!", 
                                    "Error", 
                                    JOptionPane.ERROR_MESSAGE
                                );
                            }
                        } else if (stringifiedMessage.startsWith("winner")) {
                            frame.getContentPane().remove(
                                ((BorderLayout)frame.getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER)
                            );
                            String[] splittedMessage = stringifiedMessage.split(",");
                            String winner = splittedMessage[1];
                            String expression = splittedMessage[2];

                            JPanel winnerPanel = new JPanel(new BorderLayout());;

                            JPanel winnerInfoPanel = new JPanel();
                            winnerInfoPanel.setLayout(new BoxLayout(winnerInfoPanel, BoxLayout.Y_AXIS));

                            winnerInfoPanel.add(Box.createVerticalGlue());

                            JLabel winnerNameLabel = new JLabel(("Winner: "+winner));
                            winnerNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                            winnerInfoPanel.add(winnerNameLabel);

                            JLabel winnerExpressionLabel = new JLabel(expression);
                            winnerExpressionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                            winnerExpressionLabel.setFont(H2);
                            winnerInfoPanel.add(winnerExpressionLabel);

                            winnerInfoPanel.add(Box.createVerticalGlue());

                            winnerPanel.add(winnerInfoPanel, BorderLayout.CENTER);

                            JButton nextGameButton = new JButton("Next game");
                            nextGameButton.addActionListener(new NewGameButtonListener());
                            winnerPanel.add(nextGameButton, BorderLayout.SOUTH);

                            frame.add(winnerPanel, BorderLayout.CENTER);
                            frame.revalidate();
                            frame.repaint();
                            isPlayingGame = false;
                            isGameStarted = false;
                        }
                    }

                } else {
                    System.out.println("Message of wrong type: "+message.getClass().getName());
                }
            } catch (JMSException e) {
                System.err.println("Failed invoking JMS: "+e);
            }
        }
    }

    class AnswerSubmitButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                sender.sendMessages( ("answer,"+loginName+", "+answerTextField.getText())+" ");
            } catch (Exception e) {
                System.err.println("Failed invoking JMS: "+e);
            }
        }
    }

    private void repaintFrameWithWaitingForPlayers() {
        frame.getContentPane().remove(
            ((BorderLayout)frame.getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER)
        );
        JLabel waitingForPlayersLabel = new JLabel("Waiting for players...", SwingConstants.CENTER);
        frame.add(waitingForPlayersLabel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private void repaintFrameWithPlayGame() {
        frame.getContentPane().remove(
            ((BorderLayout)frame.getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER)
        );

        JPanel playGamePanel = new JPanel(new BorderLayout());

        PokerCardsPanel pokerCardsPanel = new PokerCardsPanel(cards);
        playGamePanel.add(pokerCardsPanel, BorderLayout.CENTER);

        JPanel  answerPanel         = new JPanel(new BorderLayout());
                answerTextField     = new JTextField();
        JButton answerSubmitButton  = new JButton("Submit");
        answerSubmitButton.addActionListener(new AnswerSubmitButtonListener());
        answerPanel.add(answerTextField, BorderLayout.CENTER);
        answerPanel.add(answerSubmitButton, BorderLayout.EAST);
        playGamePanel.add(answerPanel, BorderLayout.SOUTH);

        try {
            JPanel playersPanel = new JPanel();
            playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
            
            for (String player : players) {
                JPanel playerPanel = new JPanel();
                playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
                playerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JLabel playerNameLabel = new JLabel(player);
                playerNameLabel.setFont(H2);
                playerPanel.add(playerNameLabel);
                
                PlayerStatistics playerStatistics = leaderBoardHandler.getPlayerStatistics(player);
                JLabel playerStatisticsLabel = new JLabel(
                    "Win: "+
                    playerStatistics.games_won+
                    "/"+
                    playerStatistics.ganes_played+
                    " avg: "+
                    playerStatistics.avg_winning_time+
                    "s"
                    );
                playerPanel.add(playerStatisticsLabel);
                playersPanel.add(playerPanel);
            }
            playGamePanel.add(playersPanel, BorderLayout.EAST);
        } catch (RemoteException e) {
            System.err.println("Failed invoking RMI: "+e);
        }
        
        frame.add(playGamePanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }
}
