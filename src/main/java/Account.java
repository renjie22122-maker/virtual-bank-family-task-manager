import java.math.BigDecimal;
import java.util.UUID;

public abstract class Account {
    protected String accountId;
    protected String userId;
    protected BigDecimal balance;
    protected BigDecimal interestRate;
    protected String accountPassword;

    public Account(String userId, BigDecimal interestRate, String accountPassword) {
        this.accountId = generateAccountId();
        this.userId = userId;
        this.balance = BigDecimal.ZERO;
        this.interestRate = interestRate;
        this.accountPassword = accountPassword;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void deposit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public abstract void withdraw(BigDecimal amount, String password) throws SecurityException, InsufficientFundsException;

    public abstract void transfer(BigDecimal amount, Account targetAccount, String password) throws SecurityException, InsufficientFundsException;

    public abstract void applyInterest();

    private String generateAccountId() {
        // Generate a 19-digit account ID
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19);
    }

    // Check if the provided password matches the account's password
    protected void checkPassword(String password) throws SecurityException {
        if (!this.accountPassword.equals(password)) {
            throw new SecurityException("Invalid account password.");
        }
    }
}
