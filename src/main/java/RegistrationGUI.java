import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

public class RegistrationGUI extends JFrame {
    private JTextField userNameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> userTypeComboBox;
    private JTextField familyGroupIdField;
    private JButton registerButton;
    private UserController userController;

    public RegistrationGUI() {
        userController = new UserController();
        initializeComponents();
        layoutComponents();
        registerEventHandlers();
    }

    private void initializeComponents() {
        setTitle("User Registration");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        userNameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        confirmPasswordField = new JPasswordField(15);
        userTypeComboBox = new JComboBox<>(new String[]{"parent", "child"});
        familyGroupIdField = new JTextField(15);
        registerButton = new JButton("Register");
    }

    private void layoutComponents() {
        setLayout(new GridLayout(7, 2));

        add(new JLabel("Username:"));
        add(userNameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(new JLabel("Confirm Password:"));
        add(confirmPasswordField);
        add(new JLabel("User Type:"));
        add(userTypeComboBox);
        add(new JLabel("Family Group ID:"));
        add(familyGroupIdField);
        add(registerButton);
    }

    private void registerEventHandlers() {
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });

        userTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (userTypeComboBox.getSelectedItem().equals("parent")) {
                    familyGroupIdField.setEditable(true);
                } else {
                    familyGroupIdField.setEditable(true);
                }
            }
        });
    }

    private void registerUser() {
        String userName = userNameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String userType = (String) userTypeComboBox.getSelectedItem();
        String familyGroupId = familyGroupIdField.getText().trim();

        if (userName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must contain at least 6 characters!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userType.equals("parent") && familyGroupId.isEmpty()) {
            familyGroupId = UUID.randomUUID().toString();
        } else if (userType.equals("child") && familyGroupId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Family Group ID is required for children!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UserP newUser = new UserP();
        newUser.setUserName(userName);
        newUser.setPassword(password);
        newUser.setUserType(userType);
        newUser.setFamilyGroupId(familyGroupId);

        try {
        if (userController.register(newUser)) {
            String userId = newUser.getUserId(); // 确保获取注册后的UserP对象的userId
            JOptionPane.showMessageDialog(this, "Registration successful!\nFamily Group ID: " + familyGroupId + "\nUser ID: " + userId, "Success", JOptionPane.INFORMATION_MESSAGE);
            // 复制信息到剪贴板
            StringSelection selection = new StringSelection("Family Group ID: " + familyGroupId + "\nUser ID: " + userId);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "That username is already registered!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistrationGUI().setVisible(true));
    }
}
