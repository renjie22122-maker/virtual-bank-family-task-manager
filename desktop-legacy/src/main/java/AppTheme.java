import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.DecimalFormat;

/** Shared visual language and small UI factories for the desktop application. */
public final class AppTheme {
    public static final Color BACKGROUND = new Color(244, 247, 251);
    public static final Color SURFACE = Color.WHITE;
    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color TEXT = new Color(15, 23, 42);
    public static final Color MUTED = new Color(100, 116, 139);
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color WARNING = new Color(217, 119, 6);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color SOFT_BLUE = new Color(239, 246, 255);
    public static final Font TITLE = new Font("Dialog", Font.BOLD, 28);
    public static final Font HEADING = new Font("Dialog", Font.BOLD, 18);
    public static final Font BODY = new Font("Dialog", Font.PLAIN, 14);
    public static final Font SMALL = new Font("Dialog", Font.PLAIN, 12);
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    private AppTheme() {}

    public static void install() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
        UIManager.put("control", SURFACE);
        UIManager.put("info", SOFT_BLUE);
        UIManager.put("nimbusBase", PRIMARY);
        UIManager.put("nimbusFocus", new Color(147, 197, 253));
        UIManager.put("text", TEXT);
        UIManager.put("Label.font", BODY);
        UIManager.put("Button.font", new Font("Dialog", Font.BOLD, 14));
        UIManager.put("TextField.font", BODY);
        UIManager.put("PasswordField.font", BODY);
        UIManager.put("ComboBox.font", BODY);
        UIManager.put("TextArea.font", BODY);
        UIManager.put("OptionPane.messageFont", BODY);
    }

    public static void configureFrame(JFrame frame, String title, int width, int height) {
        frame.setTitle(title);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(width, height));
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BACKGROUND);
    }

    public static JPanel page() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        return panel;
    }

    public static JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(SURFACE);
        panel.setBorder(compoundBorder(18));
        return panel;
    }

    public static Border compoundBorder(int padding) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding));
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADING);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY);
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Dialog", Font.BOLD, 13));
        label.setForeground(TEXT);
        return label;
    }

    public static JButton primaryButton(String text) {
        return button(text, PRIMARY, Color.WHITE);
    }

    public static JButton secondaryButton(String text) {
        JButton button = button(text, SURFACE, TEXT);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        return button;
    }

    public static JButton dangerButton(String text) {
        return button(text, DANGER, Color.WHITE);
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(11, 20, 11, 20));
        return button;
    }

    public static <T extends JComponent> T field(T component) {
        component.setFont(BODY);
        component.setBackground(SURFACE);
        component.setForeground(TEXT);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        component.setPreferredSize(new Dimension(component.getPreferredSize().width, 40));
        return component;
    }

    public static JScrollPane scroll(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(SURFACE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        return scrollPane;
    }

    public static String money(double amount) {
        return "$" + MONEY.format(amount);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Something went wrong", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
