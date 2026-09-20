import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainAppGUI extends JFrame {
    private final UserP user;
    private final AccountManager accountManager = new AccountManager();
    private final TaskController taskController = new TaskController(new TaskService(new TaskDAO()));
    private final JLabel accountCountValue = metricValue("0");
    private final JLabel balanceValue = metricValue("$0.00");

    public MainAppGUI(UserP user) {
        this.user = user;
        AppTheme.install();
        accountManager.loadAccountsFromFile();
        ensureParentFundingAccount();
        loadUserAccounts();
        AppTheme.configureFrame(this, "FamilyFlow Bank", 1020, 680);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(buildContent());
        refreshMetrics();
    }

    private JPanel buildContent() {
        JPanel root = AppTheme.page();
        root.add(buildHeader(), BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setOpaque(false);
        center.add(buildMetrics(), BorderLayout.NORTH);
        center.add(buildActions(), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(AppTheme.title("Hello, " + user.getUserName()));
        copy.add(Box.createVerticalStrut(5));
        copy.add(AppTheme.muted("Your family dashboard · " + capitalize(user.getUserType()) + " profile"));
        header.add(copy, BorderLayout.WEST);
        JButton logout = AppTheme.secondaryButton("Sign out");
        logout.addActionListener(e -> logout());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(logout);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildMetrics() {
        JPanel metrics = new JPanel(new GridLayout(1, 3, 14, 0));
        metrics.setOpaque(false);
        metrics.add(metricCard("Profile", capitalize(user.getUserType()), "Family role"));
        metrics.add(metricCard("Accounts", accountCountValue, "Active virtual accounts"));
        metrics.add(metricCard("Total balance", balanceValue, "Across your standard accounts"));
        return metrics;
    }

    private JPanel metricCard(String label, String value, String caption) {
        return metricCard(label, metricValue(value), caption);
    }

    private JPanel metricCard(String label, JLabel value, String caption) {
        JPanel card = AppTheme.card(new BorderLayout(0, 8));
        JLabel labelComponent = AppTheme.muted(label.toUpperCase());
        labelComponent.setFont(new Font("Dialog", Font.BOLD, 11));
        card.add(labelComponent, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        JLabel captionComponent = AppTheme.muted(caption);
        captionComponent.setFont(AppTheme.SMALL);
        card.add(captionComponent, BorderLayout.SOUTH);
        return card;
    }

    private static JLabel metricValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Dialog", Font.BOLD, 24));
        label.setForeground(AppTheme.TEXT);
        return label;
    }

    private JPanel buildActions() {
        JPanel section = new JPanel(new BorderLayout(0, 12));
        section.setOpaque(false);
        section.add(AppTheme.heading("Quick actions"), BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14));
        grid.setOpaque(false);
        grid.add(actionCard("Create task", "Assign work, deadlines and rewards.", AppTheme.PRIMARY, this::openTaskCreator));
        grid.add(actionCard("Task board", "Review progress and approve completion.", AppTheme.SUCCESS, this::openTaskBoard));
        grid.add(actionCard("My accounts", "View balances and move virtual funds.", new Color(124, 58, 237), this::viewAccounts));
        grid.add(actionCard("New checking account", "Create an everyday spending account.", new Color(8, 145, 178), this::createCheckingAccount));
        grid.add(actionCard("New fixed deposit", "Create a savings account with interest.", AppTheme.WARNING, this::createFixedDepositAccount));
        grid.add(actionCard("Family ID", "Copy the group ID for another family member.", AppTheme.MUTED, this::copyFamilyId));
        section.add(grid, BorderLayout.CENTER);
        return section;
    }

    private JPanel actionCard(String title, String description, Color accent, Runnable action) {
        JPanel card = AppTheme.card(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
                AppTheme.compoundBorder(16)));
        card.add(AppTheme.heading(title), BorderLayout.NORTH);
        JTextArea copy = new JTextArea(description);
        copy.setEditable(false);
        copy.setLineWrap(true);
        copy.setWrapStyleWord(true);
        copy.setOpaque(false);
        copy.setForeground(AppTheme.MUTED);
        copy.setFont(AppTheme.BODY);
        card.add(copy, BorderLayout.CENTER);
        JButton open = AppTheme.secondaryButton("Open");
        open.addActionListener(e -> action.run());
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(open);
        card.add(buttonRow, BorderLayout.SOUTH);
        return card;
    }

    private void ensureParentFundingAccount() {
        if ("parent".equals(user.getUserType()) && accountManager.getParentAccount(user.getUserId()) == null) {
            accountManager.createParentInfiniteAccount(user);
        }
    }

    private void loadUserAccounts() {
        for (IAccount account : accountManager.getAccountsByUserId(user.getUserId())) {
            if (!user.getAccountIds().contains(account.getAccountId())) user.addAccount(account.getAccountId());
        }
    }

    private void refreshMetrics() {
        List<IAccount> accounts = accountManager.getAccountsByUserId(user.getUserId());
        long standardCount = accounts.stream().filter(account -> !(account instanceof ParentInfiniteAccount)).count();
        double total = accounts.stream().filter(account -> !(account instanceof ParentInfiniteAccount)).mapToDouble(IAccount::getBalance).sum();
        accountCountValue.setText(Long.toString(standardCount));
        balanceValue.setText(AppTheme.money(total));
    }

    private void openTaskCreator() {
        JDialog dialog = new JDialog(this, "Create task", true);
        dialog.setContentPane(new TaskTestGUI(user).getMainPanel());
        dialog.setSize(700, 720);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openTaskBoard() {
        new TaskListViewer(user, taskController, accountManager).setVisible(true);
    }

    private void copyFamilyId() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new java.awt.datatransfer.StringSelection(user.getFamilyGroupId()), null);
        AppTheme.showSuccess(this, "Family group ID copied to the clipboard.\n" + user.getFamilyGroupId());
    }

    private void createCheckingAccount() {
        JPasswordField field = new JPasswordField(18);
        int result = JOptionPane.showConfirmDialog(this, field, "Password for the new checking account", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;
        String password = new String(field.getPassword());
        if (password.length() < 4) { AppTheme.showError(this, "Use an account password with at least 4 characters."); return; }
        accountManager.createCheckingAccount(user, password);
        refreshMetrics();
        AppTheme.showSuccess(this, "Checking account created.");
    }

    private void createFixedDepositAccount() {
        JTextField rateField = AppTheme.field(new JTextField("0.03", 12));
        JPasswordField passwordField = AppTheme.field(new JPasswordField(12));
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 7));
        form.add(AppTheme.fieldLabel("Annual interest rate (for example 0.03)"));
        form.add(rateField);
        form.add(AppTheme.fieldLabel("Account password"));
        form.add(passwordField);
        if (JOptionPane.showConfirmDialog(this, form, "New fixed deposit", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            double rate = Double.parseDouble(rateField.getText().trim());
            String password = new String(passwordField.getPassword());
            if (!Double.isFinite(rate) || rate < 0 || rate > 1) throw new IllegalArgumentException("Interest rate must be between 0 and 1.");
            if (password.length() < 4) throw new IllegalArgumentException("Use an account password with at least 4 characters.");
            accountManager.createFixedDepositAccount(user, password, rate);
            refreshMetrics();
            AppTheme.showSuccess(this, "Fixed deposit account created.");
        } catch (NumberFormatException ex) {
            AppTheme.showError(this, "Enter a valid interest rate.");
        } catch (IllegalArgumentException ex) {
            AppTheme.showError(this, ex.getMessage());
        }
    }

    private void viewAccounts() {
        JDialog dialog = new JDialog(this, "My accounts", true);
        DefaultListModel<IAccount> model = new DefaultListModel<>();
        accountManager.getAccountsByUserId(user.getUserId()).stream()
                .filter(account -> !(account instanceof ParentInfiniteAccount))
                .forEach(model::addElement);
        JList<IAccount> list = new JList<>(model);
        list.setFixedCellHeight(62);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new AccountRenderer());
        JPanel root = AppTheme.page();
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(AppTheme.title("My accounts"), BorderLayout.WEST);
        header.add(AppTheme.muted(model.isEmpty() ? "Create an account from the dashboard." : "Select an account to manage it."), BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);
        root.add(AppTheme.scroll(list), BorderLayout.CENTER);
        JButton manage = AppTheme.primaryButton("Manage selected");
        manage.setEnabled(false);
        list.addListSelectionListener(e -> manage.setEnabled(!list.isSelectionEmpty()));
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2 && !list.isSelectionEmpty()) viewAccountDetails(list.getSelectedValue()); }
        });
        manage.addActionListener(e -> viewAccountDetails(list.getSelectedValue()));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(manage);
        root.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setSize(720, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        refreshMetrics();
    }

    private void viewAccountDetails(IAccount account) {
        JDialog dialog = new JDialog(this, "Manage account", true);
        JLabel balance = metricValue(AppTheme.money(account.getBalance()));
        JPanel root = AppTheme.page();
        JPanel summary = AppTheme.card(new BorderLayout(0, 8));
        summary.add(AppTheme.muted(account.getAccountType().toUpperCase() + "  •  " + account.getAccountId()), BorderLayout.NORTH);
        summary.add(balance, BorderLayout.CENTER);
        summary.add(AppTheme.muted("Interest rate: " + account.getInterestRate()), BorderLayout.SOUTH);
        root.add(summary, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton deposit = AppTheme.secondaryButton("Deposit");
        JButton withdraw = AppTheme.secondaryButton("Withdraw");
        JButton transfer = AppTheme.primaryButton("Transfer");
        deposit.addActionListener(e -> performDeposit(account, balance));
        withdraw.addActionListener(e -> performWithdrawal(account, balance));
        transfer.addActionListener(e -> performTransfer(account, balance));
        actions.add(deposit);
        actions.add(withdraw);
        actions.add(transfer);
        root.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setSize(610, 330);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void performDeposit(IAccount account, JLabel balance) {
        Double amount = promptAmount("Deposit amount");
        if (amount == null) return;
        try {
            accountManager.deposit(account.getAccountId(), amount);
            balance.setText(AppTheme.money(account.getBalance()));
            refreshMetrics();
        } catch (RuntimeException ex) { AppTheme.showError(this, ex.getMessage()); }
    }

    private void performWithdrawal(IAccount account, JLabel balance) {
        Double amount = promptAmount("Withdrawal amount");
        if (amount == null) return;
        String password = promptPassword("Account password");
        if (password == null) return;
        try {
            accountManager.withdraw(account.getAccountId(), amount, password);
            balance.setText(AppTheme.money(account.getBalance()));
            refreshMetrics();
        } catch (InsufficientFundsException | RuntimeException ex) { AppTheme.showError(this, ex.getMessage()); }
    }

    private void performTransfer(IAccount account, JLabel balance) {
        JTextField target = AppTheme.field(new JTextField(18));
        JTextField amount = AppTheme.field(new JTextField(18));
        JPasswordField password = AppTheme.field(new JPasswordField(18));
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 7));
        form.add(AppTheme.fieldLabel("Target account ID")); form.add(target);
        form.add(AppTheme.fieldLabel("Amount")); form.add(amount);
        form.add(AppTheme.fieldLabel("Account password")); form.add(password);
        if (JOptionPane.showConfirmDialog(this, form, "Transfer funds", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            accountManager.transfer(account.getAccountId(), target.getText().trim(), Double.parseDouble(amount.getText().trim()), new String(password.getPassword()));
            balance.setText(AppTheme.money(account.getBalance()));
            refreshMetrics();
            AppTheme.showSuccess(this, "Transfer completed.");
        } catch (InsufficientFundsException | RuntimeException ex) { AppTheme.showError(this, ex.getMessage()); }
    }

    private Double promptAmount(String title) {
        String value = JOptionPane.showInputDialog(this, title + ":");
        if (value == null) return null;
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException ex) { AppTheme.showError(this, "Enter a valid amount."); return null; }
    }

    private String promptPassword(String title) {
        JPasswordField password = new JPasswordField(18);
        return JOptionPane.showConfirmDialog(this, password, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION
                ? new String(password.getPassword()) : null;
    }

    private void logout() {
        if (JOptionPane.showConfirmDialog(this, "Sign out of FamilyFlow?", "Sign out", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new LoginGUI().setVisible(true);
            dispose();
        }
    }

    private String capitalize(String value) {
        return value == null || value.isEmpty() ? "Unknown" : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static class AccountRenderer extends JPanel implements ListCellRenderer<IAccount> {
        private final JLabel type = AppTheme.heading("");
        private final JLabel detail = AppTheme.muted("");
        private final JLabel balance = metricValue("");

        AccountRenderer() {
            setLayout(new BorderLayout(12, 0));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            JPanel copy = new JPanel();
            copy.setOpaque(false);
            copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
            copy.add(type);
            copy.add(detail);
            add(copy, BorderLayout.CENTER);
            add(balance, BorderLayout.EAST);
        }

        public Component getListCellRendererComponent(JList<? extends IAccount> list, IAccount value, int index, boolean selected, boolean focused) {
            type.setText(value.getAccountType());
            detail.setText(value.getAccountId() + "  •  " + value.getInterestRate() + " interest");
            balance.setText(AppTheme.money(value.getBalance()));
            setBackground(selected ? AppTheme.SOFT_BLUE : AppTheme.SURFACE);
            setOpaque(true);
            return this;
        }
    }
}
