import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class MainAppGUI extends JFrame {
    private UserP user;
    private AccountManager accountManager;

    public MainAppGUI(UserP user) {
        this.user = user;
        this.accountManager = new AccountManager();
        accountManager.loadAccountsFromFile();
        loadUserAccounts(); // Load user accounts upon login
        initializeComponents();
        layoutComponents();
    }

    private void loadUserAccounts() {
        List<IAccount> userAccounts = accountManager.getAccountsByUserId(user.getUserId());
        for (IAccount account : userAccounts) {
            user.addAccount(account.getAccountId());
        }
    }

    private void initializeComponents() {
        setTitle("Main Application");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getUserName() + "!", SwingConstants.CENTER);
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        JButton createTaskButton = new JButton("Create Task");
        JButton viewTasksButton = new JButton("View Tasks");
        JButton createCheckingAccountButton = new JButton("Create Checking Account");
        JButton createFixedDepositAccountButton = new JButton("Create Fixed Deposit Account");
        JButton viewAccountsButton = new JButton("View Accounts");
        JButton logoutButton = new JButton("Logout");

        buttonPanel.add(createTaskButton);
        buttonPanel.add(viewTasksButton);
        add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(createCheckingAccountButton);
        buttonPanel.add(createFixedDepositAccountButton);
        buttonPanel.add(viewAccountsButton);
        buttonPanel.add(logoutButton);

        add(buttonPanel, BorderLayout.CENTER);

        createCheckingAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createCheckingAccount();
            }
        });

        createFixedDepositAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createFixedDepositAccount();
            }
        });

        viewAccountsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewAccounts();
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        createTaskButton.addActionListener(e -> {
            JFrame createTaskFrame = new JFrame("Create Task");
            createTaskFrame.setContentPane(new TaskTestGUI(user).getMainPanel());
            createTaskFrame.pack();
            createTaskFrame.setLocationRelativeTo(null);
            createTaskFrame.setVisible(true);
        });

        viewTasksButton.addActionListener(e -> {
            JFrame viewTasksFrame = new JFrame("View Tasks");
            viewTasksFrame.setContentPane(new TaskListViewer(user, new TaskController(new TaskService(new TaskDAO())), accountManager).getContentPane());
            viewTasksFrame.pack();
            viewTasksFrame.setLocationRelativeTo(null);
            viewTasksFrame.setVisible(true);
        });
    }

    private void createCheckingAccount() {
        String password = JOptionPane.showInputDialog(this, "Enter password for new Checking Account:");
        if (password != null && !password.isEmpty()) {
            accountManager.createCheckingAccount(user, password);
            accountManager.saveAccountsToFile();
            JOptionPane.showMessageDialog(this, "Checking Account created successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.");
        }
    }

    private void createFixedDepositAccount() {
        String password = JOptionPane.showInputDialog(this, "Enter password for new Fixed Deposit Account:");
        if (password != null && !password.isEmpty()) {
            String interestRateStr = JOptionPane.showInputDialog(this, "Enter interest rate for Fixed Deposit Account:");
            try {
                double interestRate = Double.parseDouble(interestRateStr);
                accountManager.createFixedDepositAccount(user, password, interestRate);
                accountManager.saveAccountsToFile();
                JOptionPane.showMessageDialog(this, "Fixed Deposit Account created successfully!");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid interest rate.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.");
        }
    }

    private void viewAccounts() {
        JFrame accountsFrame = new JFrame("Your Accounts");
        accountsFrame.setSize(600, 400);
        accountsFrame.setLocationRelativeTo(null);
        accountsFrame.setLayout(new BorderLayout());

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String accountId : user.getAccountIds()) {
            IAccount account = accountManager.getAccountById(accountId);
            if (account != null) {
                listModel.addElement("ID: " + account.getAccountId() + " | Type: " + account.getAccountType() +
                        " | Balance: " + account.getBalance() + " | Interest Rate: " + account.getInterestRate());
            }
        }

        JList<String> accountList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(accountList);
        accountsFrame.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton viewDetailsButton = new JButton("View Details");
        JButton closeButton = new JButton("Close");
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(closeButton);
        accountsFrame.add(buttonPanel, BorderLayout.SOUTH);

        viewDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = accountList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String accountInfo = listModel.getElementAt(selectedIndex);
                    String accountId = accountInfo.split("\\|")[0].split(":")[1].trim();
                    IAccount account = accountManager.getAccountById(accountId);
                    if (account != null) {
                        viewAccountDetails(account);
                    }
                } else {
                    JOptionPane.showMessageDialog(accountsFrame, "Please select an account.");
                }
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accountsFrame.dispose();
            }
        });

        accountsFrame.setVisible(true);
    }

    private void viewAccountDetails(IAccount account) {
        JFrame detailsFrame = new JFrame("Account Details");
        detailsFrame.setSize(400, 300);
        detailsFrame.setLocationRelativeTo(null);
        detailsFrame.setLayout(new BorderLayout());

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setText("Account ID: " + account.getAccountId() +
                "\nAccount Type: " + account.getAccountType() +
                "\nBalance: " + account.getBalance() +
                "\nInterest Rate: " + account.getInterestRate());
        detailsFrame.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        JButton transferButton = new JButton("Transfer");
        JButton closeButton = new JButton("Close");
        buttonPanel.add(depositButton);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(transferButton);
        buttonPanel.add(closeButton);
        detailsFrame.add(buttonPanel, BorderLayout.SOUTH);

        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String amountStr = JOptionPane.showInputDialog(detailsFrame, "Enter amount to deposit:");
                try {
                    double amount = Double.parseDouble(amountStr);
                    accountManager.deposit(account.getAccountId(), amount);
                    accountManager.saveAccountsToFile();
                    detailsArea.setText("Account ID: " + account.getAccountId() +
                            "\nAccount Type: " + account.getAccountType() +
                            "\nBalance: " + account.getBalance() +
                            "\nInterest Rate: " + account.getInterestRate());
                    JOptionPane.showMessageDialog(detailsFrame, "Deposit successful!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(detailsFrame, "Invalid amount.");
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(detailsFrame, ex.getMessage());
                }
            }
        });

        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String amountStr = JOptionPane.showInputDialog(detailsFrame, "Enter amount to withdraw:");
                if (amountStr == null) return;
                String password = JOptionPane.showInputDialog(detailsFrame, "Enter account password:");
                if (password == null) return;
                try {
                    double amount = Double.parseDouble(amountStr);
                    accountManager.withdraw(account.getAccountId(), amount, password);
                    accountManager.saveAccountsToFile();
                    detailsArea.setText("Account ID: " + account.getAccountId() +
                            "\nAccount Type: " + account.getAccountType() +
                            "\nBalance: " + account.getBalance() +
                            "\nInterest Rate: " + account.getInterestRate());
                    JOptionPane.showMessageDialog(detailsFrame, "Withdrawal successful!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(detailsFrame, "Invalid amount.");
                } catch (InsufficientFundsException ex) {
                    JOptionPane.showMessageDialog(detailsFrame, ex.getMessage());
                } catch (IllegalArgumentException | SecurityException ex) {
                    JOptionPane.showMessageDialog(detailsFrame, ex.getMessage());
                }
            }
        });

        transferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String targetAccountId = JOptionPane.showInputDialog(detailsFrame, "Enter target account ID:");
                String amountStr = JOptionPane.showInputDialog(detailsFrame, "Enter amount to transfer:");
                String password = null;
                if (!(account instanceof ParentInfiniteAccount)) {
                    password = JOptionPane.showInputDialog(detailsFrame, "Enter account password:");
                    if (password == null) return;
                }
                try {
                    double amount = Double.parseDouble(amountStr);
                    accountManager.transfer(account.getAccountId(), targetAccountId, amount, password);
                    accountManager.saveAccountsToFile();
                    detailsArea.setText("Account ID: " + account.getAccountId() +
                            "\nAccount Type: " + account.getAccountType() +
                            "\nBalance: " + account.getBalance() +
                            "\nInterest Rate: " + account.getInterestRate());
                    JOptionPane.showMessageDialog(detailsFrame, "Transfer successful!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(detailsFrame, "Invalid amount.");
                } catch (InsufficientFundsException ex) {
                    JOptionPane.showMessageDialog(detailsFrame, ex.getMessage());
                } catch (IllegalArgumentException | SecurityException ex) {
                    JOptionPane.showMessageDialog(detailsFrame, ex.getMessage());
                }
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                detailsFrame.dispose();
            }
        });

        detailsFrame.setVisible(true);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginGUI().setVisible(true);
            dispose();
        }
    }

    public static void main(String[] args) {
        UserP demoUser = new UserP();
        demoUser.setUserName("Demo User");
        demoUser.setUserId("demo-user-id");

        SwingUtilities.invokeLater(() -> new MainAppGUI(demoUser).setVisible(true));
    }
}
