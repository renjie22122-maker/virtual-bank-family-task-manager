import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

public class LoginGUI extends JFrame {
    private JTextField userNameField;
    JPasswordField passwordField;
    private JComboBox<String> userIdComboBox;
    private JButton loginButton;
    private UserController userController;

    public LoginGUI() {
        userController = new UserController();
        initializeComponents();
        layoutComponents();
        registerEventHandlers();
    }

    private void initializeComponents() {
        setTitle("User Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        userNameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        userIdComboBox = new JComboBox<>();
        loginButton = new JButton("Login");
    }

    private void layoutComponents() {
        setLayout(new GridLayout(4, 2));

        add(new JLabel("Username:"));
        add(userNameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(new JLabel("User ID:"));
        add(userIdComboBox);
        add(loginButton);
    }

    private void registerEventHandlers() {
        userNameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                populateUserIdComboBox();
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginUser();
            }
        });
    }

    private void populateUserIdComboBox() {
        String userName = userNameField.getText();
        List<String> userIds = userController.getUserIdsByUsername(userName);
        userIdComboBox.removeAllItems();
        for (String userId : userIds) {
            userIdComboBox.addItem(userId);
        }
        if (userIdComboBox.getItemCount() > 0) {
            userIdComboBox.setSelectedIndex(0);
        }
    }

    void loginUser() {
        String userName = userNameField.getText();
        String password = new String(passwordField.getPassword());
        String userId = (String) userIdComboBox.getSelectedItem();

        if (userId == null || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a user ID!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UserP user = userController.loginWithUserId(userId, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            new MainAppGUI(user).setVisible(true); // Assuming MainAppGUI is the main application window
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}
