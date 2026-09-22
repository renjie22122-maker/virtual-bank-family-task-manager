import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FixedTermAccount extends Account {
    private int termInMonths;
    private LocalDateTime startTime;
    private BigDecimal depositGoal;

    public FixedTermAccount(String userId, int termInMonths, String accountPassword) {
        super(userId, new BigDecimal("0.05"), accountPassword); // 假设定期账户利率为5%
        this.termInMonths = termInMonths;
        this.startTime = LocalDateTime.now();
        this.depositGoal = BigDecimal.ZERO;
    }

    public int getTermInMonths() {
        return termInMonths;
    }

    public LocalDateTime getStartTime() {
        return startTime;
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
        if (LocalDateTime.now().isBefore(startTime.plusMonths(termInMonths))) {
            throw new SecurityException("Cannot withdraw before the term ends.");
        }
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
        if (LocalDateTime.now().isBefore(startTime.plusMonths(termInMonths))) {
            throw new SecurityException("Cannot transfer before the term ends.");
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

    public boolean canWithdrawOrTransfer() {
        return LocalDateTime.now().isAfter(startTime.plusMonths(termInMonths));
    }

    public boolean hasReachedDepositGoal() {
        return balance.compareTo(depositGoal) >= 0;
    }
}
