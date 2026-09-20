import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class LoginGUI extends JFrame {
    private final JTextField userNameField = AppTheme.field(new JTextField(22));
    final JPasswordField passwordField = AppTheme.field(new JPasswordField(22));
    private final JComboBox<String> userIdComboBox = AppTheme.field(new JComboBox<>());
    private final JButton loginButton = AppTheme.primaryButton("Sign in");
    private final UserController userController = new UserController();

    public LoginGUI() {
        AppTheme.install();
        AppTheme.configureFrame(this, "Sign in · FamilyFlow Bank", 480, 560);
        setContentPane(buildContent());
        getRootPane().setDefaultButton(loginButton);
        registerEventHandlers();
    }

    private JPanel buildContent() {
        JPanel root = AppTheme.page();
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(AppTheme.title("Welcome back"));
        header.add(Box.createVerticalStrut(6));
        header.add(AppTheme.muted("Sign in to manage your family's tasks and virtual accounts."));
        root.add(header, BorderLayout.NORTH);

        JPanel form = AppTheme.card(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 7, 0);
        addField(form, gbc, 0, "Username", userNameField);
        addField(form, gbc, 2, "Profile", userIdComboBox);
        addField(form, gbc, 4, "Password", passwordField);
        gbc.gridy = 6;
        gbc.insets = new Insets(16, 0, 0, 0);
        form.add(loginButton, gbc);
        root.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        footer.setOpaque(false);
        footer.add(AppTheme.muted("New to FamilyFlow?"));
        JButton create = AppTheme.secondaryButton("Create account");
        create.addActionListener(e -> new RegistrationGUI().setVisible(true));
        footer.add(create);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.insets = new Insets(row == 0 ? 0 : 14, 0, 7, 0);
        panel.add(AppTheme.fieldLabel(label), gbc);
        gbc.gridy = row + 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(field, gbc);
    }

    private void registerEventHandlers() {
        userNameField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { populateUserIdComboBox(); }
            public void removeUpdate(DocumentEvent e) { populateUserIdComboBox(); }
            public void changedUpdate(DocumentEvent e) { populateUserIdComboBox(); }
        });
        loginButton.addActionListener(e -> loginUser());
    }

    private void populateUserIdComboBox() {
        Object previous = userIdComboBox.getSelectedItem();
        List<String> userIds = userController.getUserIdsByUsername(userNameField.getText().trim());
        userIdComboBox.removeAllItems();
        for (String userId : userIds) userIdComboBox.addItem(userId);
        if (previous != null) userIdComboBox.setSelectedItem(previous);
        userIdComboBox.setEnabled(!userIds.isEmpty());
    }

    void loginUser() {
        String userId = (String) userIdComboBox.getSelectedItem();
        String password = new String(passwordField.getPassword());
        if (userId == null || userId.isBlank()) {
            AppTheme.showError(this, "Enter a registered username and select its profile.");
            return;
        }
        if (password.isEmpty()) {
            AppTheme.showError(this, "Password cannot be empty.");
            return;
        }
        UserP user = userController.loginWithUserId(userId, password);
        if (user == null) {
            AppTheme.showError(this, "The password is incorrect for the selected profile.");
            passwordField.selectAll();
            passwordField.requestFocusInWindow();
            return;
        }
        new MainAppGUI(user).setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        AppTheme.install();
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}
