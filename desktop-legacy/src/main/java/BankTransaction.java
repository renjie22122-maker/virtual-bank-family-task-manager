public class BankTransaction implements ITransaction {
    private String transactionId;
    private String transactionType;
    private String timestamp;
    private String fromAccountId;
    private String toAccountId;
    private double amount;
    private String remark;
    private String fromOwnerId;
    private String toOwnerId;

    public BankTransaction(String transactionId, String transactionType, String timestamp,
                           String fromAccountId, String toAccountId, double amount, String remark,
                           String fromOwnerId, String toOwnerId) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.timestamp = timestamp;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.remark = remark;
        this.fromOwnerId = fromOwnerId;
        this.toOwnerId = toOwnerId;
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public String getTransactionType() {
        return transactionType;
    }

    @Override
    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String getFromAccountId() {
        return fromAccountId;
    }

    @Override
    public String getToAccountId() {
        return toAccountId;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public String getRemark() {
        return remark;
    }

    @Override
    public String getFromOwnerId() {
        return fromOwnerId;
    }

    @Override
    public String getToOwnerId() {
        return toOwnerId;
    }
}
