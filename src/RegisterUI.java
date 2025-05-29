import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class RegisterUI {

    private JFrame frame = new JFrame("Register");
    private JTextField loginNameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JPasswordField confirmPasswordField = new JPasswordField();
    private UserManage userManager;

    public RegisterUI() {
        try {
            userManager = (UserManage) Naming.lookup("UserManager");
        } catch (Exception e) {
            System.err.println("Failed accessing RMI: "+e);
        }
    }

    public void open() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 300);
        frame.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        TitledBorder titledBorder = BorderFactory.createTitledBorder("Register");
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

        JLabel confirmPasswordLabel = new JLabel("Confirm Password");
        mainPanel.add(confirmPasswordLabel);
        confirmPasswordField.setPreferredSize(new Dimension(confirmPasswordField.getPreferredSize().width, 50));
        confirmPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        mainPanel.add(confirmPasswordField);

        JPanel buttonPanel = new JPanel();
        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");
        registerButton.addActionListener(new RegisterButtonListener());
        cancelButton.addActionListener(new CancelButtonListener());
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(buttonPanel);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    class RegisterButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String loginName = loginNameField.getText();
            String password = new String( passwordField.getPassword() );
            String confirmPassword = new String( confirmPasswordField.getPassword() );
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
            } else if ( ! confirmPassword.equals(password) ) {
                JOptionPane.showMessageDialog(
                    frame, 
                    "Passwords do not match", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE
                );
            } else {
                try {
                    String errorMessage = userManager.register(loginName, password);
                    if ( ! errorMessage.isEmpty() ) {
                        JOptionPane.showMessageDialog(
                            frame, 
                            errorMessage, 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE
                        );
                    } else if ( ! (errorMessage=userManager.login(loginName, password)).isEmpty() ) {
                        JOptionPane.showMessageDialog(
                            frame, 
                            errorMessage, 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE
                        );
                    } else {
                        frame.dispose();
                        GameUI gameUI = new GameUI(loginName);
                        gameUI.open();
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

    class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            frame.dispose();
            LoginUI loginUI = new LoginUI();
            loginUI.open();
        }
    }

}
