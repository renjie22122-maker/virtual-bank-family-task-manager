import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Coordinates task and banking use cases behind one presentation-layer boundary. */
public class TaskApplicationService {
    private final TaskController taskController;
    private final AccountManager accountManager;

    public TaskApplicationService(TaskController taskController, AccountManager accountManager) {
        this.taskController = taskController;
        this.accountManager = accountManager;
    }

    public List<TaskView> listTasks(String status, UserP user, String sortOption) {
        JSONObject result = taskController.viewTaskList(status, user, sortOption);
        Object value = result.get("tasks");
        if (!(value instanceof JSONArray tasks)) return List.of();
        List<TaskView> views = new ArrayList<>();
        for (Object item : tasks) if (item instanceof JSONObject task) views.add(toView(task));
        return views;
    }

    public TaskView getTask(UUID id) {
        JSONObject task = taskController.viewTaskDetails(id);
        return task == null || task.get("TaskID") == null ? null : toView(task);
    }

    public void changeStatus(UUID id, String status, UserP user) { taskController.updateTaskStatus(id, status, user); }
    public void deleteTask(UUID id, UserP user) { taskController.deleteTask(id, user); }

    public void updateTask(UUID id, TaskUpdate update, UserP user) {
        JSONObject task = requireRawTask(id);
        task.put("name", update.name()); task.put("description", update.description());
        task.put("urgency", (long) update.urgency()); task.put("repeat", update.repeat());
        task.put("reward", update.reward()); task.put("maxBonus", update.maxBonus());
        task.put("startTime", update.startTime()); task.put("endTime", update.endTime());
        task.put("settlementType", update.settlementType());
        taskController.modifyTask(id, task, user);
    }

    public void updateBonus(UUID id, double bonus, UserP user) {
        JSONObject task = requireRawTask(id); task.put("bonus", bonus); taskController.modifyTask(id, task, user);
    }

    public void confirmCompletion(TaskView task, UserP parent) throws InsufficientFundsException {
        IAccount parentAccount = accountManager.getParentAccount(parent.getUserId());
        if (parentAccount == null) throw new IllegalStateException("Parent funding account is unavailable.");
        IAccount childAccount = accountManager.getChildCheckingAccount(task.assigneeId());
        if (childAccount == null) throw new IllegalStateException("The assignee needs a checking account before receiving a reward.");
        double total = task.reward() + task.bonus();
        if (total < 0 || !Double.isFinite(total)) throw new IllegalArgumentException("Reward amount is invalid.");
        if (total > 0) accountManager.transfer(parentAccount.getAccountId(), childAccount.getAccountId(), total, null);
        taskController.updateTaskStatus(task.id(), "Done", parent);
    }

    private JSONObject requireRawTask(UUID id) {
        JSONObject task = taskController.viewTaskDetails(id);
        if (task == null || task.get("TaskID") == null) throw new IllegalArgumentException("Task was not found.");
        return task;
    }

    private TaskView toView(JSONObject task) {
        return new TaskView(UUID.fromString(text(task, "TaskID")), text(task, "name"), text(task, "description"),
                integer(task, "urgency"), text(task, "repeat"), number(task, "reward"), number(task, "maxBonus"),
                number(task, "bonus"), text(task, "startTime"), text(task, "endTime"), text(task, "assignee"),
                text(task, "assigneeName"), text(task, "collaboratorName"), text(task, "creatorName"),
                text(task, "status"), text(task, "settlementType"), text(task, "actualStartTime"), text(task, "timedDuration"));
    }

    private static String text(JSONObject object, String key) { Object value = object.get(key); return value == null ? "" : value.toString(); }
    private static double number(JSONObject object, String key) { Object value = object.get(key); return value instanceof Number number ? number.doubleValue() : 0.0; }
    private static int integer(JSONObject object, String key) { Object value = object.get(key); return value instanceof Number number ? number.intValue() : 0; }

    public record TaskUpdate(String name, String description, int urgency, String repeat,
                             double reward, double maxBonus, String startTime, String endTime,
                             String settlementType) { }
}
