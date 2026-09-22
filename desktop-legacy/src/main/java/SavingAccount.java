import java.math.BigDecimal;

public class SavingAccount extends Account {
    private BigDecimal depositGoal;

    public SavingAccount(String userId, String accountPassword) {
        super(userId, new BigDecimal("0.02"), accountPassword); // 假设活期账户利率为2%
        this.depositGoal = BigDecimal.ZERO;
    }

    public BigDecimal getDepositGoal() {
        return depositGoal;
    }

    public void setDepositGoal(BigDecimal depositGoal) {
        this.depositGoal = depositGoal;
    }

    @Override
    public void withdraw(BigDecimal amount, String password) throws SecurityException, InsufficientFundsException {
        checkPassword(password); // 验证密码是否正确
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds.");
        }
        balance = balance.subtract(amount);
    }

    @Override
    public void transfer(BigDecimal amount, Account targetAccount, String password) throws SecurityException, InsufficientFundsException {
        if (!this.userId.equals(targetAccount.getUserId())) {
            checkPassword(password); // 验证密码是否正确
        }
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds.");
        }
        balance = balance.subtract(amount);
        targetAccount.deposit(amount);
    }

    @Override
    public void applyInterest() {
        balance = balance.add(balance.multiply(interestRate));
    }

    public boolean hasReachedDepositGoal() {
        return balance.compareTo(depositGoal) >= 0;
    }
}
