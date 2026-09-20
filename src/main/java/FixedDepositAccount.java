public class FixedDepositAccount implements IAccount {
    private String accountId;
    private String ownerId;
    private String password;
    private double balance;
    private double interestRate;
    private String accountType;

    public FixedDepositAccount(String accountId, String ownerId, String password, double balance, double interestRate) {
        this.accountId = accountId;
        this.ownerId = ownerId;
        this.password = password;
        this.balance = balance;
        this.interestRate = interestRate;
        this.accountType = "FixedDeposit";
    }

    @Override
    public String getAccountId() {
        return accountId;
    }

    @Override
    public String getAccountType() {
        return accountType;
    }

    @Override
    public double getBalance() {
        return balance;
    }

    @Override
    public double getInterestRate() {
        return interestRate;
    }

    @Override
    public void deposit(double amount) {
        requirePositiveAmount(amount);
        balance += amount;
    }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        requirePositiveAmount(amount);
        if (amount > balance) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal.");
        }
        balance -= amount;
    }

    @Override
    public void transfer(String targetAccountId, double amount) throws InsufficientFundsException {
        // 具体的转账逻辑在 AccountManager 中处理，这里不实现
    }

    @Override
    public boolean authenticate(String password) {
        return this.password != null && this.password.equals(password);
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    private void requirePositiveAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Amount must be a positive finite number.");
        }
    }
}
