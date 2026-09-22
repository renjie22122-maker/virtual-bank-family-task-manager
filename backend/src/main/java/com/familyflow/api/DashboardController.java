package com.familyflow.api;

import com.familyflow.api.ApiDtos.DashboardResponse;
import com.familyflow.domain.Models.StoredUser;
import com.familyflow.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final AuthService auth; private final AccountService accounts; private final TaskService tasks;
    public DashboardController(AuthService auth, AccountService accounts, TaskService tasks) { this.auth = auth; this.accounts = accounts; this.tasks = tasks; }
    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@RequestHeader("Authorization") String authorization) {
        StoredUser user = auth.requireUser(authorization);
        var allTasks = tasks.list(user, "All", "deadline");
        long done = allTasks.stream().filter(task -> "Done".equals(task.status)).count();
        return new DashboardResponse(AuthService.view(user), accounts.forUser(user).size(), accounts.totalBalance(user),
                allTasks.size() - done, done, auth.familyMembers(user));
    }
}
