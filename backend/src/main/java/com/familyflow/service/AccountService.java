package com.familyflow.service;

import com.familyflow.api.ApiDtos.*;
import com.familyflow.domain.Models.Account;
import com.familyflow.domain.Models.StoredUser;
import com.familyflow.domain.Models.Transaction;
import com.familyflow.infrastructure.JsonStore;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private final JsonStore store;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public AccountService(JsonStore store) { this.store = store; }

    public List<Account> forUser(StoredUser user) {
        return store.read("accounts.json", Account.class).stream()
                .filter(account -> user.userId.equals(account.ownerId) && !"ParentInfinite".equals(account.accountType))
                .peek(account -> account.password = null).toList();
    }

    public Account create(StoredUser user, CreateAccountRequest request) {
        List<Account> accounts = store.read("accounts.json", Account.class);
        Account account = new Account(); account.accountId = UUID.randomUUID().toString().replace("-", "").substring(0, 19);
        account.ownerId = user.userId; account.password = encoder.encode(request.password()); account.balance = 0;
        account.accountType = request.accountType();
        account.interestRate = "Checking".equals(request.accountType()) ? 0.01 : request.interestRate();
        accounts.add(account); store.write("accounts.json", accounts);
        account.password = null; return account;
    }

    public void deposit(StoredUser user, String accountId, AmountRequest request) {
        List<Account> accounts = store.read("accounts.json", Account.class); Account account = owned(accounts, accountId, user);
        requireAmount(request.amount()); account.balance += request.amount();
        store.write("accounts.json", accounts); record("deposit", accountId, null, request.amount(), user.userId, null);
    }

    public void withdraw(StoredUser user, String accountId, AmountRequest request) {
        List<Account> accounts = store.read("accounts.json", Account.class); Account account = owned(accounts, accountId, user);
        authenticate(account, request.password()); requireAmount(request.amount());
        if (account.balance < request.amount()) throw new IllegalStateException("Insufficient funds.");
        account.balance -= request.amount(); store.write("accounts.json", accounts);
        record("withdraw", accountId, null, request.amount(), user.userId, null);
    }

    public void transfer(StoredUser user, String accountId, TransferRequest request) {
        List<Account> accounts = store.read("accounts.json", Account.class); Account from = owned(accounts, accountId, user);
        Account to = accounts.stream().filter(account -> request.targetAccountId().equals(account.accountId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Target account was not found."));
        if (from.accountId.equals(to.accountId)) throw new IllegalArgumentException("Choose a different target account.");
        authenticate(from, request.password()); requireAmount(request.amount());
        if (from.balance < request.amount()) throw new IllegalStateException("Insufficient funds.");
        from.balance -= request.amount(); to.balance += request.amount(); store.write("accounts.json", accounts);
        record("transfer", from.accountId, to.accountId, request.amount(), from.ownerId, to.ownerId);
    }

    public double totalBalance(StoredUser user) { return forUser(user).stream().mapToDouble(account -> account.balance).sum(); }

    public void payReward(StoredUser parent, String childId, double amount) {
        if (!"parent".equals(parent.userType)) throw new SecurityException("Only parents can confirm rewards.");
        if (amount <= 0) return;
        List<Account> accounts = store.read("accounts.json", Account.class);
        Account child = accounts.stream().filter(account -> childId.equals(account.ownerId) && "Checking".equals(account.accountType))
                .findFirst().orElseThrow(() -> new IllegalStateException("The assignee needs a checking account."));
        child.balance += amount; store.write("accounts.json", accounts);
        record("reward", null, child.accountId, amount, parent.userId, child.ownerId);
    }

    private Account owned(List<Account> accounts, String id, StoredUser user) {
        return accounts.stream().filter(account -> id.equals(account.accountId) && user.userId.equals(account.ownerId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Account was not found."));
    }
    private void authenticate(Account account, String password) {
        if (password == null) throw new SecurityException("Account password is required.");
        boolean legacy = account.password != null && !account.password.startsWith("$2");
        boolean matches = legacy ? account.password.equals(password) : encoder.matches(password, account.password);
        if (!matches) throw new SecurityException("Invalid account password.");
        if (legacy) account.password = encoder.encode(password);
    }
    private void requireAmount(double amount) { if (!Double.isFinite(amount) || amount <= 0) throw new IllegalArgumentException("Amount must be positive."); }
    private void record(String type, String from, String to, double amount, String fromOwner, String toOwner) {
        List<Transaction> transactions = store.read("transactions.json", Transaction.class); Transaction transaction = new Transaction();
        transaction.transactionId = UUID.randomUUID().toString(); transaction.type = type; transaction.timestamp = Instant.now().toString();
        transaction.fromAccountId = from; transaction.toAccountId = to; transaction.amount = amount;
        transaction.remark = type; transaction.fromOwnerId = fromOwner; transaction.toOwnerId = toOwner;
        transactions.add(transaction); store.write("transactions.json", transactions);
    }
}
