package com.familyflow;

import com.familyflow.api.ApiDtos.*;
import com.familyflow.domain.Models.StoredUser;
import com.familyflow.infrastructure.JsonStore;
import com.familyflow.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationFlowTest {
    @TempDir Path dataDir;
    private JsonStore store;
    private AuthService auth;
    private AccountService accounts;
    private TaskService tasks;

    @BeforeEach
    void setUp() {
        store = new JsonStore(new ObjectMapper(), dataDir.toString(), dataDir.resolve("legacy").toString());
        auth = new AuthService(store);
        accounts = new AccountService(store);
        tasks = new TaskService(store, auth, accounts);
    }

    @Test
    void familyTaskRewardFlowIsKeptBehindServices() {
        AuthResponse parentSession = auth.register(new RegisterRequest("parent", "test1234", "parent", null));
        AuthResponse childSession = auth.register(new RegisterRequest("child", "test1234", "child", parentSession.user().familyGroupId()));
        StoredUser parent = auth.requireUser("Bearer " + parentSession.token());
        StoredUser child = auth.requireUser("Bearer " + childSession.token());
        accounts.create(child, new CreateAccountRequest("Checking", "2468", 0.01));

        var created = tasks.create(parent, new CreateTaskRequest("Clean the kitchen", "", 3, "None", 10, 5,
                LocalDateTime.now().plusHours(1).toString(), LocalDateTime.now().plusDays(1).toString(), child.userId, null));
        tasks.changeStatus(child, created.taskId, "Doing");
        tasks.changeStatus(child, created.taskId, "WaitforConfirm");
        var completed = tasks.confirm(parent, created.taskId, new ConfirmTaskRequest(2));

        assertEquals("Done", completed.status);
        assertEquals(12, accounts.forUser(child).get(0).balance);
        assertTrue(store.read("users.json", StoredUser.class).get(0).password.startsWith("$2"));
    }

    @Test
    void childCannotCreateAnUnknownFamily() {
        var error = assertThrows(IllegalArgumentException.class,
                () -> auth.register(new RegisterRequest("child", "test1234", "child", "missing")));
        assertEquals("Enter an existing family group ID.", error.getMessage());
    }
}
