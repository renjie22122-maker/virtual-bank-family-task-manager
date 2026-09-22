package com.familyflow.api;

import com.familyflow.api.ApiDtos.*;
import com.familyflow.domain.Models.Account;
import com.familyflow.domain.Models.StoredUser;
import com.familyflow.service.AccountService;
import com.familyflow.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AuthService auth; private final AccountService accounts;
    public AccountController(AuthService auth, AccountService accounts) { this.auth = auth; this.accounts = accounts; }
    @GetMapping public List<Account> list(@RequestHeader("Authorization") String token) { return accounts.forUser(auth.requireUser(token)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Account create(@RequestHeader("Authorization") String token, @Valid @RequestBody CreateAccountRequest request) { return accounts.create(auth.requireUser(token), request); }
    @PostMapping("/{id}/deposit") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deposit(@RequestHeader("Authorization") String token, @PathVariable String id, @Valid @RequestBody AmountRequest request) { accounts.deposit(auth.requireUser(token), id, request); }
    @PostMapping("/{id}/withdraw") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@RequestHeader("Authorization") String token, @PathVariable String id, @Valid @RequestBody AmountRequest request) { accounts.withdraw(auth.requireUser(token), id, request); }
    @PostMapping("/{id}/transfer") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transfer(@RequestHeader("Authorization") String token, @PathVariable String id, @Valid @RequestBody TransferRequest request) { accounts.transfer(auth.requireUser(token), id, request); }
}
