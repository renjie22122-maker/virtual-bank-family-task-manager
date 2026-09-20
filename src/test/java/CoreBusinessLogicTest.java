import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CoreBusinessLogicTest {
    @TempDir
    Path tempDir;

    @Test
    void checkingAccountRejectsInvalidAmountsAndWrongPassword() throws Exception {
        CheckingAccount account = new CheckingAccount("account-1", "owner-1", "secret", 100, 0.01);

        assertThrows(IllegalArgumentException.class, () -> account.deposit(-10));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(0));
        assertFalse(account.authenticate("wrong"));

        account.deposit(25);
        account.withdraw(50);
        assertEquals(75, account.getBalance(), 0.001);
    }

    @Test
    void transactionLookupHandlesDepositAndWithdrawalNullEndpoints() {
        TransactionManager manager = new TransactionManager(tempDir.resolve("transactions.json"));
        manager.recordTransaction(new BankTransaction(
                "tx-1", "deposit", LocalDateTime.now().toString(),
                "account-1", null, 20, "deposit", "owner-1", null));

        assertEquals(1, manager.getTransactionsByAccountId("account-1").size());
        assertTrue(manager.getTransactionsByAccountId("another-account").isEmpty());
    }

    @Test
    void repeatedTaskGetsAUniqueIdOnlyAfterCompletion() {
        TaskDAO dao = new TaskDAO(tempDir.resolve("tasks.json"));
        TaskService service = new TaskService(dao);
        UserP parent = parentUser();
        UUID originalId = UUID.randomUUID();
        JSONObject task = task(originalId, parent);

        service.createTask(task, parent);
        assertEquals(1, dao.readAllTasks().size(), "creation must not pre-generate a year of duplicates");

        JSONObject saved = dao.findTaskById(originalId);
        saved.put("status", "WaitforConfirm");
        dao.updateTask(originalId, saved);
        service.updateTaskStatus(originalId, "Done", parent);

        JSONArray tasks = dao.readAllTasks();
        assertEquals(2, tasks.size());
        JSONObject next = (JSONObject) tasks.get(1);
        assertNotEquals(originalId.toString(), next.get("TaskID"));
        assertEquals("ToDo", next.get("status"));
    }

    @SuppressWarnings("unchecked")
    private JSONObject task(UUID id, UserP user) {
        JSONObject task = new JSONObject();
        task.put("TaskID", id.toString());
        task.put("name", "Weekly chores");
        task.put("creator", user.getUserId());
        task.put("assignee", user.getUserId());
        task.put("collaborator", null);
        task.put("familyGroupID", user.getFamilyGroupId());
        task.put("repeat", "Weekly");
        task.put("startTime", LocalDateTime.now().plusHours(1).toString());
        task.put("endTime", LocalDateTime.now().plusHours(2).toString());
        task.put("reward", 10.0);
        task.put("urgency", 1L);
        return task;
    }

    private UserP parentUser() {
        UserP user = new UserP();
        user.setUserId("parent-1");
        user.setUserName("Parent");
        user.setUserType("parent");
        user.setFamilyGroupId("family-1");
        return user;
    }
}
