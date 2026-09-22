import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.UUID;

public class RegistrationGUI extends JFrame {
    private final JTextField userNameField = AppTheme.field(new JTextField(22));
    private final JPasswordField passwordField = AppTheme.field(new JPasswordField(22));
    private final JPasswordField confirmPasswordField = AppTheme.field(new JPasswordField(22));
    private final JComboBox<String> userTypeComboBox = AppTheme.field(new JComboBox<>(new String[]{"parent", "child"}));
    private final JTextField familyGroupIdField = AppTheme.field(new JTextField(22));
    private final JLabel familyHint = AppTheme.muted("Optional — a new family ID will be generated.");
    private final JButton registerButton = AppTheme.primaryButton("Create account");
    private final UserController userController = new UserController();

    public RegistrationGUI() {
        AppTheme.install();
        AppTheme.configureFrame(this, "Create account · FamilyFlow Bank", 520, 690);
        setContentPane(buildContent());
        getRootPane().setDefaultButton(registerButton);
        userTypeComboBox.addActionListener(e -> updateFamilyHint());
        registerButton.addActionListener(e -> registerUser());
    }

    private JPanel buildContent() {
        JPanel root = AppTheme.page();
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(AppTheme.title("Create your profile"));
        header.add(Box.createVerticalStrut(6));
        header.add(AppTheme.muted("Parents create a family space; children join with its family ID."));
        root.add(header, BorderLayout.NORTH);

        JPanel form = AppTheme.card(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addField(form, gbc, 0, "Username", userNameField);
        addField(form, gbc, 2, "Password (at least 6 characters)", passwordField);
        addField(form, gbc, 4, "Confirm password", confirmPasswordField);
        addField(form, gbc, 6, "Profile type", userTypeComboBox);
        addField(form, gbc, 8, "Family group ID", familyGroupIdField);
        gbc.gridy = 10;
        gbc.insets = new Insets(5, 0, 0, 0);
        form.add(familyHint, gbc);
        gbc.gridy = 11;
        gbc.insets = new Insets(18, 0, 0, 0);
        form.add(registerButton, gbc);
        root.add(form, BorderLayout.CENTER);
        return root;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.insets = new Insets(row == 0 ? 0 : 13, 0, 6, 0);
        panel.add(AppTheme.fieldLabel(label), gbc);
        gbc.gridy = row + 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(field, gbc);
    }

    private void updateFamilyHint() {
        boolean child = "child".equals(userTypeComboBox.getSelectedItem());
        familyHint.setText(child ? "Required — ask a parent for the family ID." : "Optional — a new family ID will be generated.");
        familyGroupIdField.setToolTipText(familyHint.getText());
    }

    private void registerUser() {
        String userName = userNameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmation = new String(confirmPasswordField.getPassword());
        String userType = (String) userTypeComboBox.getSelectedItem();
        String familyGroupId = familyGroupIdField.getText().trim();
        if (userName.isEmpty()) { AppTheme.showError(this, "Username cannot be empty."); return; }
        if (password.length() < 6) { AppTheme.showError(this, "Password must contain at least 6 characters."); return; }
        if (!password.equals(confirmation)) { AppTheme.showError(this, "Passwords do not match."); return; }
        if ("parent".equals(userType) && familyGroupId.isEmpty()) familyGroupId = UUID.randomUUID().toString();
        if ("child".equals(userType) && familyGroupId.isEmpty()) { AppTheme.showError(this, "Children need a family group ID."); return; }

        UserP newUser = new UserP();
        newUser.setUserName(userName);
        newUser.setPassword(password);
        newUser.setUserType(userType);
        newUser.setFamilyGroupId(familyGroupId);
        try {
            if (!userController.register(newUser)) {
                AppTheme.showError(this, "That username is already registered.");
                return;
            }
            String summary = "Family Group ID: " + familyGroupId + "\nUser ID: " + newUser.getUserId();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(summary), null);
            AppTheme.showSuccess(this, "Account created. Your family and user IDs were copied to the clipboard.\n\n" + summary);
            dispose();
        } catch (IllegalArgumentException ex) {
            AppTheme.showError(this, ex.getMessage());
        }
    }
}
