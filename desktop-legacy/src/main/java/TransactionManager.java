import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.nio.file.Path;

public class TransactionManager {
    private List<ITransaction> transactions;
    private final Path transactionsFilePath;

    public TransactionManager() {
        this(Path.of("transactions.json"));
    }

    public TransactionManager(Path transactionsFilePath) {
        this.transactions = new ArrayList<>();
        this.transactionsFilePath = transactionsFilePath;
    }

    public void recordTransaction(ITransaction transaction) {
        transactions.add(transaction);
        saveTransactionsToFile();
    }

    public List<ITransaction> getTransactions() {
        return transactions;
    }

    public List<ITransaction> getTransactionsByAccountId(String accountId) {
        List<ITransaction> result = new ArrayList<>();
        for (ITransaction transaction : transactions) {
            if (accountId.equals(transaction.getFromAccountId()) || accountId.equals(transaction.getToAccountId())) {
                result.add(transaction);
            }
        }
        return result;
    }

    public void loadTransactionsFromFile() {
        transactions.clear();
        JSONArray jsonArray = JsonUtil.readJsonArrayFromFile(transactionsFilePath.toString());
        for (Object obj : jsonArray) {
            JSONObject json = (JSONObject) obj;
            ITransaction transaction = new BankTransaction(
                    (String) json.get("transactionId"),
                    (String) json.get("transactionType"),
                    (String) json.get("timestamp"),
                    (String) json.get("fromAccountId"),
                    (String) json.get("toAccountId"),
                    ((Number) json.get("amount")).doubleValue(),
                    (String) json.get("remark"),
                    (String) json.get("fromOwnerId"),
                    (String) json.get("toOwnerId")
            );
            transactions.add(transaction);
        }
    }

    public void saveTransactionsToFile() {
        JSONArray jsonArray = new JSONArray();
        for (ITransaction transaction : transactions) {
            JSONObject json = new JSONObject();
            json.put("transactionId", transaction.getTransactionId());
            json.put("transactionType", transaction.getTransactionType());
            json.put("timestamp", transaction.getTimestamp());
            json.put("fromAccountId", transaction.getFromAccountId());
            json.put("toAccountId", transaction.getToAccountId());
            json.put("amount", transaction.getAmount());
            json.put("remark", transaction.getRemark());
            json.put("fromOwnerId", transaction.getFromOwnerId());
            json.put("toOwnerId", transaction.getToOwnerId());
            jsonArray.add(json);
        }
        JsonUtil.writeJsonArrayToFile(jsonArray, transactionsFilePath.toString());
    }
}
