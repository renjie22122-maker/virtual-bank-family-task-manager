public class ParentInfiniteAccount implements IAccount {
    private String accountId;
    private String ownerId;
    private String accountType;

    public ParentInfiniteAccount(String accountId, String ownerId) {
        this.accountId = accountId;
        this.ownerId = ownerId;
        this.accountType = "ParentInfinite";
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
        return Double.MAX_VALUE;
    }

    @Override
    public double getInterestRate() {
        return 0.0;
    }

    @Override
    public void deposit(double amount) {
        // Do nothing
    }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        // Do nothing
    }

    @Override
    public void transfer(String targetAccountId, double amount) throws InsufficientFundsException {
        // Do nothing
    }

    @Override
    public boolean authenticate(String password) {
        return true;
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public String getPassword() {
        return null;
    }
}
