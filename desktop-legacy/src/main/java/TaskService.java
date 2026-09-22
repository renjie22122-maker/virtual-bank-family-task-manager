import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Comparator;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;


/**
 * TaskService handles all business logic related to tasks.
 */
public class TaskService {
    private TaskDAO taskDao;

    public TaskService(TaskDAO taskDao) {
        this.taskDao = taskDao;
    }

    /**
     * Creates a new task and schedules repeated tasks if necessary.
     */
    public void createTask(JSONObject taskData, UserP user) {
        validateTaskData(taskData);
        JSONArray tasks = taskDao.readAllTasks();
        String newTaskId = (String) taskData.get("TaskID");

        for (Object taskObj : tasks) {
            JSONObject task = (JSONObject) taskObj;
            String taskId = (String) task.get("TaskID");
            if (newTaskId.equals(taskId)) {
                throw new IllegalArgumentException("Task already exists: " + taskId);
            }
        }

        String status = user.getUserType().equals("parent") ? "ToDo" : "WaitforCheck";
        taskData.put("status", status);
        validateRepeat((String) taskData.get("repeat"));
        taskDao.saveTask(taskData);
    }




    /**
     * Calculates the next start time based on repetition type.
     */
    private LocalDateTime calculateNextStartTime(LocalDateTime current, String repeat) {
        switch (repeat) {
            case "Daily": return current.plusDays(1);
            case "Weekly": return current.plusWeeks(1);
            case "Monthly": return current.plusMonths(1);
            default: throw new IllegalArgumentException("Unsupported repeat option: " + repeat);
        }
    }

    /**
     * Updates the status of a task.
     */
    public void updateTaskStatus(UUID taskId, String newStatus, UserP user) {
        JSONObject task = requireTask(taskId);
        validateStatusChange(task, newStatus, user);
        task.put("status", newStatus);
        taskDao.updateTask(taskId, task);

        // 如果任务完成，并且设置了重复，则创建新的重复任务
        if ("Done".equals(newStatus) && task.containsKey("repeat")) {
            String repeat = (String) task.get("repeat");
            if (!"不重复".equals(repeat) && !"None".equals(repeat)) {
                createNextRepeatedTask(task, user);
            }
        }
    }
    /**
     * Creates the next repeated task based on the original task's repetition settings.
     */
    private void createNextRepeatedTask(JSONObject originalTaskData, UserP user) {
        String repeat = (String) originalTaskData.get("repeat");
        LocalDateTime startTime = LocalDateTime.parse((String) originalTaskData.get("startTime"));
        LocalDateTime endTime = LocalDateTime.parse((String) originalTaskData.get("endTime"));
        Duration duration = Duration.between(startTime, endTime);

        LocalDateTime nextStartTime = calculateNextStartTime(startTime, repeat);
        JSONObject newTaskData = (JSONObject) originalTaskData.clone();
        newTaskData.put("startTime", nextStartTime.toString());
        newTaskData.put("endTime", nextStartTime.plus(duration).toString());
        newTaskData.put("TaskID", UUID.randomUUID().toString());
        newTaskData.put("status", user.getUserType().equals("parent") ? "ToDo" : "WaitforCheck");

        taskDao.saveTask(newTaskData);
    }

    /**
     * Validates that the status change is logical and permitted.
     */
    private void validateStatusChange(JSONObject task, String status, UserP user) {
        String currentStatus = (String) task.get("status");
        List<String> validStatuses = Arrays.asList("ToDo", "Doing", "WaitforConfirm", "Done", "OverDue", "WaitforCheck", "Reject");

        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        String taskAssigneeId = (String) task.get("assignee");
        String taskCollaboratorId = (String) task.get("collaborator");
        String taskCreatorId = (String) task.get("creator");

        if (!user.getUserId().equals(taskAssigneeId) && !user.getUserId().equals(taskCollaboratorId) && !user.getUserId().equals(taskCreatorId)) {
            throw new SecurityException("User " + user.getUserName() + " is not authorized to change the status of this task.");
        }

        switch (status) {
            case "Done":
                if (!"WaitforConfirm".equals(currentStatus)) {
                    throw new IllegalStateException("Task must be in 'WaitforConfirm' status before it can be set to 'Done'.");
                }
                if (user.getUserType().equals("parent")) {
                    distributeReward(UUID.fromString((String) task.get("TaskID")));
                } else {
                    throw new SecurityException("Only the parent can set the task status to 'Done'.");
                }
                break;
            case "OverDue":
                LocalDateTime endTime = LocalDateTime.parse((String) task.get("endTime"));
                if (LocalDateTime.now().isBefore(endTime)) {
                    throw new IllegalStateException("Task cannot be set to 'OverDue' before the end time.");
                }
                if (!"ToDo".equals(currentStatus) && !"Doing".equals(currentStatus)) {
                    throw new IllegalStateException("Only tasks in 'ToDo' or 'Doing' status can become 'OverDue'.");
                }
                break;
        }
    }


    public JSONObject viewTaskList(String status, UserP user, String sortOption) {
        return filterAndSortTasks(status, user, sortOption);
    }



    public void modifyTask(UUID taskId, JSONObject taskData, UserP user) {
        JSONObject task = taskDao.findTaskById(taskId);
        validateTaskModification(task, user);
        taskData.forEach(task::put);
        taskDao.updateTask(taskId, task);
    }




    public void deleteTask(UUID taskId, UserP user) {
        JSONObject task = requireTask(taskId);
        if (!"parent".equals(user.getUserType())
                || !user.getFamilyGroupId().equals(task.get("familyGroupID"))) {
            throw new SecurityException("Only a parent in this family can delete the task.");
        }
        taskDao.deleteTask(taskId);
    }

    public JSONObject findTaskById(UUID taskId) {
        return taskDao.findTaskById(taskId);
    }


    private void validateTaskModification(JSONObject task, UserP user) {
        if (task == null) {
            throw new IllegalArgumentException("Task was not found.");
        }
        if (!"parent".equals(user.getUserType())) {
            throw new SecurityException("Only users with parent type can modify tasks.");
        }
        String status = (String) task.get("status");
        if ("Doing".equals(status) || "Done".equals(status)) {
            throw new IllegalStateException("Tasks can only be modified if their status is 'ToDo' or 'WaitforConfirm'.");
        }
    }

    private JSONObject filterAndSortTasks(String status, UserP user, String sortOption) {
        List<String> validStatuses = Arrays.asList("ToDo", "Doing", "WaitforConfirm", "Done", "OverDue", "WaitforCheck", "Reject");
        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        markOverdueTasks();

        JSONArray tasks = taskDao.readAllTasks();
        JSONArray filteredTasks = (JSONArray) tasks.stream()
                .filter(task -> status.equals(((JSONObject) task).get("status")) &&
                        user.getFamilyGroupId().equals(((JSONObject) task).get("familyGroupID")))
                .collect(Collectors.toCollection(JSONArray::new));

        switch (status) {
            case "ToDo":
                switch (sortOption) {
                    case "reward":
                        filteredTasks.sort(Comparator.comparing((JSONObject task) -> ((Number) task.get("reward")).doubleValue()).reversed());
                        break;
                    case "time":
                        filteredTasks.sort(Comparator.comparing((JSONObject task) -> LocalDateTime.parse((String) task.get("endTime"))));
                        break;
                    case "urgency":
                        filteredTasks.sort(Comparator.comparing((JSONObject task) -> ((Number) task.get("urgency")).intValue()));
                        break;
                }
                break;
            case "Doing":
                filteredTasks.sort(Comparator.comparing((JSONObject task) -> ((Number) task.get("reward")).doubleValue()).reversed()
                        .thenComparing((JSONObject task) -> LocalDateTime.parse((String) task.get("endTime")))
                        .thenComparing((JSONObject task) -> ((Number) task.get("urgency")).intValue()));
                break;
            case "OverDue":
                filteredTasks.sort(Comparator.comparing((JSONObject task) -> getOverdueDuration(UUID.fromString((String) task.get("TaskID"))))
                        .thenComparing((JSONObject task) -> (String) task.get("name")));
                break;
            case "WaitforCheck":
                filteredTasks.sort(Comparator.comparing((JSONObject task) -> ((Number) task.get("urgency")).intValue())
                        .thenComparing((JSONObject task) -> ((Number) task.get("reward")).doubleValue()).reversed());
                break;
            case "Reject":
                filteredTasks.sort(Comparator.comparing((JSONObject task) -> (String) task.get("name"))
                        .thenComparing((JSONObject task) -> LocalDateTime.parse((String) task.get("endTime"))));
                break;
        }

        JSONObject result = new JSONObject();
        result.put("tasks", filteredTasks);
        return result;
    }



    /**
     * Marks tasks as overdue based on their end time.
     */
    public void markOverdueTasks() {
        JSONArray tasks = taskDao.readAllTasks();
        boolean modified = false;
        for (Object taskObj : tasks) {
            JSONObject task = (JSONObject) taskObj;
            LocalDateTime endTime = LocalDateTime.parse((String) task.get("endTime"));
            if (LocalDateTime.now().isAfter(endTime) && ("ToDo".equals(task.get("status")) || "Doing".equals(task.get("status")))) {
                task.put("status", "OverDue");
                modified = true;
            }
        }
        if (modified) {
            taskDao.updateAllTasks(tasks);
        }
    }

    private Duration getOverdueDuration(UUID taskId) {
        JSONObject task = findTaskById(taskId);
        LocalDateTime endTime = LocalDateTime.parse((String) task.get("endTime"));
        return LocalDateTime.now().isAfter(endTime) ? Duration.between(endTime, LocalDateTime.now()) : Duration.ZERO;
    }

    /**
     * Distributes rewards to the task assignee upon completion.
     */
    private void distributeReward(UUID taskId) {
        JSONObject task = taskDao.findTaskById(taskId);
        // Business logic for distributing rewards
        // Update task with reward details
        taskDao.updateTask(taskId, task);
    }

    private JSONObject requireTask(UUID taskId) {
        JSONObject task = taskDao.findTaskById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        return task;
    }

    private void validateRepeat(String repeat) {
        if (!Arrays.asList("None", "Daily", "Weekly", "Monthly").contains(repeat)) {
            throw new IllegalArgumentException("Unsupported repeat option: " + repeat);
        }
    }

    private void validateTaskData(JSONObject taskData) {
        if (taskData == null || taskData.get("TaskID") == null) {
            throw new IllegalArgumentException("Task ID is required.");
        }
        UUID.fromString((String) taskData.get("TaskID"));
        LocalDateTime start = LocalDateTime.parse((String) taskData.get("startTime"));
        LocalDateTime end = LocalDateTime.parse((String) taskData.get("endTime"));
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Task start time must be before its end time.");
        }
    }
}
