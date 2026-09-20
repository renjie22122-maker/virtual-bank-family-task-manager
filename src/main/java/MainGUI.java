import javax.swing.*;
import java.awt.*;

/**
 * MainGUI class provides the main window for the task management system,
 * allowing navigation to task creation and task viewing functionalities.
 */
public class MainGUI {
    private JFrame frame;
    private JButton registerButton;
    private JButton loginButton;

    public MainGUI() {
        initializeWindow();
        addComponents();
        finalizeWindow();
    }

    private void initializeWindow() {
        frame = new JFrame("Task Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
    }

    private void addComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 30, 10, 30);

        registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> new RegistrationGUI().setVisible(true));
        });
        frame.add(registerButton, gbc);

        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
        });
        frame.add(loginButton, gbc);
    }

    private void finalizeWindow() {
        frame.pack();
        frame.setSize(350, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI());
    }
}
