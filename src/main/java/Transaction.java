import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.json.simple.JSONObject;

public class Transaction {
    private String transactionId;
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String type; // deposit, withdraw, transfer, reward

    public Transaction(String fromAccountId, String toAccountId, BigDecimal amount, String type) {
        this.transactionId = UUID.randomUUID().toString();
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.type = type;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("transactionId", transactionId);
        json.put("fromAccountId", fromAccountId);
        json.put("toAccountId", toAccountId);
        json.put("amount", amount.toString());
        json.put("timestamp", timestamp.toString());
        json.put("type", type);
        return json;
    }

    public static Transaction fromJson(JSONObject json) {
        String transactionId = (String) json.get("transactionId");
        String fromAccountId = (String) json.get("fromAccountId");
        String toAccountId = (String) json.get("toAccountId");
        BigDecimal amount = new BigDecimal((String) json.get("amount"));
        LocalDateTime timestamp = LocalDateTime.parse((String) json.get("timestamp"));
        String type = (String) json.get("type");
        Transaction transaction = new Transaction(fromAccountId, toAccountId, amount, type);
        transaction.transactionId = transactionId;
        transaction.timestamp = timestamp;
        return transaction;
    }
}
