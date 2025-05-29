import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.rmi.*;

public class LoginUI {

    private JFrame frame = new JFrame("Login");
    private JTextField loginNameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private UserManage userManager;
    
    public LoginUI() {
        try {
            userManager = (UserManage) Naming.lookup("UserManager");
        } catch (Exception e) {
            System.err.println("Failed accessing RMI: "+e);
        }
    }

    public void open() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 250);
        frame.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        TitledBorder titledBorder = BorderFactory.createTitledBorder("Login");
        EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            emptyBorder, 
            BorderFactory.createCompoundBorder(titledBorder, emptyBorder)
            ));

        JLabel loginNameLabel = new JLabel("Login Name");
        mainPanel.add(loginNameLabel);
        loginNameField.setPreferredSize(new Dimension(loginNameField.getPreferredSize().width, 50));
        loginNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        mainPanel.add(loginNameField);

        JLabel passwordLabel = new JLabel("Password");
        mainPanel.add(passwordLabel);
        passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, 50));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        mainPanel.add(passwordField);

        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        loginButton.addActionListener(new LoginButtonListener());
        registerButton.addActionListener(new RegisterButtonListener());
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(buttonPanel);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    class LoginButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String loginName = loginNameField.getText();
            String password = new String( passwordField.getPassword() );
            if ( loginName.isEmpty() ) {
                JOptionPane.showMessageDialog(
                    frame, 
                    "Login name should not be empty", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE
                );
            } else if ( password.isEmpty() ) {
                JOptionPane.showMessageDialog(
                    frame, 
                    "Password should not be empty", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE
                );
            } else {
                try {
                    String errorMessage = userManager.login(loginName, password);
                    if ( errorMessage.isEmpty() ) {
                        frame.dispose();
                        GameUI gameUI = new GameUI(loginName);
                        gameUI.open();
                    } else {
                        JOptionPane.showMessageDialog(
                            frame, 
                            errorMessage, 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
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
    }

    class RegisterButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            frame.dispose();
            RegisterUI registerUI = new RegisterUI();
            registerUI.open();
        }
    }

}
