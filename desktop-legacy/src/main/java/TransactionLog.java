import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TransactionLog {
    private List<Transaction> transactions;

    public TransactionLog() {
        transactions = new ArrayList<>();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }


    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions); // 返回交易记录的副本，保证原始数据不被修改
    }

    public List<Transaction> getTransactionsByAccount(String accountId) {
        List<Transaction> accountTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (accountId.equals(transaction.getFromAccountId()) || accountId.equals(transaction.getToAccountId())) {
                accountTransactions.add(transaction);
            }
        }
        return accountTransactions;
    }

    public List<Transaction> getTransactionsByUser(String userId, List<Account> accounts) {
        List<Transaction> userTransactions = new ArrayList<>();
        for (Account account : accounts) {
            if (userId.equals(account.getUserId())) {
                userTransactions.addAll(getTransactionsByAccount(account.getAccountId()));
            }
        }
        return userTransactions;
    }

    public JSONArray toJsonArray() {
        JSONArray jsonArray = new JSONArray();
        for (Transaction transaction : transactions) {
            jsonArray.add(transaction.toJson());
        }
        return jsonArray;
    }
}
