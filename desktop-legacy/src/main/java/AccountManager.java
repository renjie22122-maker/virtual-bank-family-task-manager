import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountManager {
    private List<IAccount> accounts;
    private List<ITransaction> transactions;
    private TransactionManager transactionManager;

    public AccountManager() {
        this.accounts = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.transactionManager = new TransactionManager();
    }

    public void createCheckingAccount(UserP user, String password) {
        String accountId = generateUniqueAccountId();
        IAccount account = new CheckingAccount(accountId, user.getUserId(), password, 0.0, 0.01);
        accounts.add(account);
        user.addAccount(accountId);
        saveAccountsToFile(); // Save accounts to file after creating a new one
    }

    public void createFixedDepositAccount(UserP user, String password, double interestRate) {
        String accountId = generateUniqueAccountId();
        IAccount account = new FixedDepositAccount(accountId, user.getUserId(), password, 0.0, interestRate);
        accounts.add(account);
        user.addAccount(accountId);
        saveAccountsToFile(); // Save accounts to file after creating a new one
    }

    public void createParentInfiniteAccount(UserP user) {
        String accountId = "PARENT_INFINITE_ACCOUNT_" + user.getUserId();
        IAccount account = new ParentInfiniteAccount(accountId, user.getUserId());
        accounts.add(account);
        user.addAccount(accountId);
        saveAccountsToFile(); // Save accounts to file after creating a new one
    }

    public IAccount getAccountById(String accountId) {
        for (IAccount account : accounts) {
            if (account.getAccountId().equals(accountId)) {
                return account;
            }
        }
        return null;
    }

    public List<IAccount> getAccountsByUserId(String userId) {
        List<IAccount> userAccounts = new ArrayList<>();
        for (IAccount account : accounts) {
            if (account.getOwnerId().equals(userId)) {
                userAccounts.add(account);
            }
        }
        return userAccounts;
    }

    private String generateUniqueAccountId() {
        String accountId;
        do {
            accountId = UUID.randomUUID().toString().replace("-", "").substring(0, 19);
        } while (getAccountById(accountId) != null);
        return accountId;
    }

    public void deposit(String accountId, double amount) {
        IAccount account = getAccountById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        if (account instanceof ParentInfiniteAccount) {
            throw new IllegalArgumentException("The parent funding account cannot receive deposits.");
        }
        account.deposit(amount);
        recordTransaction("deposit", accountId, null, amount, "Deposit to account", account.getOwnerId(), null);
        saveAccountsToFile();
    }

    public void withdraw(String accountId, double amount, String password) throws InsufficientFundsException {
        IAccount account = getAccountById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        if (account instanceof ParentInfiniteAccount) {
            throw new IllegalArgumentException("The parent funding account cannot be withdrawn from directly.");
        }
        if (!account.authenticate(password)) {
            throw new SecurityException("Invalid account password.");
        }
        account.withdraw(amount);
        recordTransaction("withdraw", accountId, null, amount, "Withdraw from account", account.getOwnerId(), null);
        saveAccountsToFile();
    }

    public void transfer(String fromAccountId, String toAccountId, double amount, String password) throws InsufficientFundsException {
        IAccount fromAccount = getAccountById(fromAccountId);
        IAccount toAccount = getAccountById(toAccountId);
        if (fromAccount == null || toAccount == null) {
            throw new IllegalArgumentException("Source or target account was not found.");
        }
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Source and target accounts must be different.");
        }
        if (!(fromAccount instanceof ParentInfiniteAccount) && !fromAccount.authenticate(password)) {
            throw new SecurityException("Invalid account password.");
        }
        fromAccount.withdraw(amount);
        toAccount.deposit(amount);
        recordTransaction("transfer", fromAccountId, toAccountId, amount, "Transfer between accounts", fromAccount.getOwnerId(), toAccount.getOwnerId());
        saveAccountsToFile();
    }


    private void recordTransaction(String type, String fromAccountId, String toAccountId, double amount, String remark, String fromOwnerId, String toOwnerId) {
        String transactionId = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().toString();
        ITransaction transaction = new BankTransaction(transactionId, type, timestamp, fromAccountId, toAccountId, amount, remark, fromOwnerId, toOwnerId);
        transactions.add(transaction);
        transactionManager.recordTransaction(transaction);
    }

    public List<ITransaction> getTransactions() {
        return transactionManager.getTransactions();
    }

    public List<ITransaction> getTransactionsByAccountId(String accountId) {
        return transactionManager.getTransactionsByAccountId(accountId);
    }

    public void loadAccountsFromFile() {
        accounts.clear();
        JSONArray jsonArray = JsonUtil.readJsonArrayFromFile("accounts.json");
        for (Object obj : jsonArray) {
            JSONObject json = (JSONObject) obj;
            IAccount account = null;
            String accountType = (String) json.get("accountType");
            if ("Checking".equals(accountType)) {
                account = new CheckingAccount(
                        (String) json.get("accountId"),
                        (String) json.get("ownerId"),
                        (String) json.get("password"),
                        ((Number) json.get("balance")).doubleValue(),
                        ((Number) json.get("interestRate")).doubleValue()
                );
            } else if ("FixedDeposit".equals(accountType)) {
                account = new FixedDepositAccount(
                        (String) json.get("accountId"),
                        (String) json.get("ownerId"),
                        (String) json.get("password"),
                        ((Number) json.get("balance")).doubleValue(),
                        ((Number) json.get("interestRate")).doubleValue()
                );
            } else if ("ParentInfinite".equals(accountType)) {
                account = new ParentInfiniteAccount(
                        (String) json.get("accountId"),
                        (String) json.get("ownerId")
                );
            }
            if (account != null) {
                accounts.add(account);
            }
        }
    }

    public void saveAccountsToFile() {
        JSONArray jsonArray = new JSONArray();
        for (IAccount account : accounts) {
            JSONObject json = new JSONObject();
            json.put("accountId", account.getAccountId());
            json.put("ownerId", account.getOwnerId());
            json.put("balance", account.getBalance());
            json.put("interestRate", account.getInterestRate());
            json.put("accountType", account.getAccountType());
            if (account.getPassword() != null) {
                json.put("password", account.getPassword());
            }
            jsonArray.add(json);
        }
        JsonUtil.writeJsonArrayToFile(jsonArray, "accounts.json");
    }

    public IAccount getParentAccount(String userId) {
        for (IAccount account : accounts) {
            if (account instanceof ParentInfiniteAccount && account.getOwnerId().equals(userId)) {
                return account;
            }
        }
        return null;
    }

    public IAccount getChildCheckingAccount(String userId) {
        for (IAccount account : accounts) {
            if (account instanceof CheckingAccount && account.getOwnerId().equals(userId)) {
                return account;
            }
        }
        return null;
    }
}
