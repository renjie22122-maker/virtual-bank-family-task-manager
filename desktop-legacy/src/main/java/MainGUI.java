import javax.swing.*;
import java.awt.*;

/** Application landing screen. */
public class MainGUI {
    private final JFrame frame = new JFrame();

    public MainGUI() {
        AppTheme.configureFrame(frame, "FamilyFlow Bank", 820, 520);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(buildContent());
        frame.setVisible(true);
    }

    private JPanel buildContent() {
        JPanel root = AppTheme.page();
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("FAMILY FINANCE  •  TASKS  •  REWARDS");
        eyebrow.setForeground(AppTheme.PRIMARY);
        eyebrow.setFont(new Font("Dialog", Font.BOLD, 12));
        brand.add(eyebrow);
        brand.add(Box.createVerticalStrut(10));
        brand.add(AppTheme.title("Build good habits together."));
        brand.add(Box.createVerticalStrut(8));
        brand.add(AppTheme.muted("Plan tasks, reward progress, and learn money skills in one family space."));
        root.add(brand, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 18, 0));
        center.setOpaque(false);
        center.add(featureCard("01", "Plan together", "Create, assign, and review family tasks with clear deadlines and rewards."));
        center.add(featureCard("02", "Learn by doing", "Use safe virtual accounts to practice saving, transfers, and responsible spending."));
        root.add(center, BorderLayout.CENTER);

        JPanel actions = AppTheme.card(new BorderLayout(18, 0));
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(AppTheme.heading("Ready to get started?"));
        copy.add(Box.createVerticalStrut(4));
        copy.add(AppTheme.muted("Sign in to your family space or create a new profile."));
        actions.add(copy, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton register = AppTheme.secondaryButton("Create account");
        JButton login = AppTheme.primaryButton("Sign in");
        register.addActionListener(e -> new RegistrationGUI().setVisible(true));
        login.addActionListener(e -> new LoginGUI().setVisible(true));
        buttons.add(register);
        buttons.add(login);
        actions.add(buttons, BorderLayout.EAST);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private JPanel featureCard(String number, String title, String description) {
        JPanel card = AppTheme.card(new BorderLayout(0, 14));
        JLabel badge = new JLabel(number);
        badge.setOpaque(true);
        badge.setBackground(AppTheme.SOFT_BLUE);
        badge.setForeground(AppTheme.PRIMARY);
        badge.setFont(new Font("Dialog", Font.BOLD, 18));
        badge.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(badge);
        card.add(top, BorderLayout.NORTH);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(AppTheme.heading(title));
        text.add(Box.createVerticalStrut(8));
        JTextArea body = new JTextArea(description);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setOpaque(false);
        body.setForeground(AppTheme.MUTED);
        body.setFont(AppTheme.BODY);
        text.add(body);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    public static void main(String[] args) {
        AppTheme.install();
        SwingUtilities.invokeLater(MainGUI::new);
    }
}
