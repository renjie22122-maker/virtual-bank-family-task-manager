import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Task class implementing ITask interface.
 * Manages task creation, modification, deletion, and status updates.
 */
public class Task implements ITask {

    public Task() {}

    @Override
    public void createTask(JSONObject taskData, UserP user) {
        String status = user.getUserType().equals("parent") ? "ToDo" : "WaitforCheck";
        taskData.put("status", status);
        handleRepetition(taskData, (String) taskData.get("repeat"), user);
        saveTask(taskData);
    }

    @Override
    public void updateTaskStatus(UUID taskId, String status, UserP user) {
        JSONObject task = findTaskById(taskId);
        validateStatusChange(task, status, user);
        task.put("status", status);
        updateJsonFile(taskId, task);
    }

    @Override
    public JSONObject viewTaskList(String status, UserP user, String sortOption) {
        return filterAndSortTasks(status, user, sortOption);
    }

    @Override
    public void modifyTask(UUID taskId, JSONObject taskData, UserP user) {
        JSONObject task = findTaskById(taskId);
        validateTaskModification(task, user);
        taskData.forEach(task::put);
        updateJsonFile(taskId, task);
    }

    @Override
    public void deleteTask(UUID taskId, UserP user) {
        JSONArray tasks = readExistingTasks();
        tasks.removeIf(task -> taskId.equals(UUID.fromString((String) ((JSONObject) task).get("TaskID"))));
        saveTasks(tasks);
    }

    private void handleRepetition(JSONObject taskData, String repeat, UserP user) {
        if (!"不重复".equals(repeat)) {
            scheduleRepeatedTasks(taskData, repeat, user);
        }
    }

    private void scheduleRepeatedTasks(JSONObject originalTaskData, String repeat, UserP user) {
        LocalDateTime startTime = LocalDateTime.parse((String) originalTaskData.get("startTime"));
        LocalDateTime endTime = LocalDateTime.parse((String) originalTaskData.get("endTime"));
        Duration duration = Duration.between(startTime, endTime);
        LocalDateTime nextStartTime = calculateNextStartTime(startTime, repeat);

        while (nextStartTime.isBefore(startTime.plusYears(1))) {
            JSONObject newTaskData = (JSONObject) originalTaskData.clone();
            newTaskData.put("startTime", nextStartTime.toString());
            newTaskData.put("endTime", nextStartTime.plus(duration).toString());

            saveTask(newTaskData);
            nextStartTime = calculateNextStartTime(nextStartTime, repeat);
        }
    }

    private LocalDateTime calculateNextStartTime(LocalDateTime current, String repeat) {
        switch (repeat) {
            case "每天": return current.plusDays(1);
            case "每周": return current.plusWeeks(1);
            case "每月": return current.plusMonths(1);
            default: return current;
        }
    }

    private JSONArray readExistingTasks() {
        try (FileReader reader = new FileReader("tasks.json")) {
            JSONParser parser = new JSONParser();
            return (JSONArray) parser.parse(reader);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to read tasks from file.", e);
        }
    }

    private void saveTask(JSONObject task) {
        JSONArray tasks = readExistingTasks();
        tasks.add(task);
        saveTasks(tasks);
    }

    private void saveTasks(JSONArray tasks) {
        try (FileWriter file = new FileWriter("tasks.json")) {
            file.write(tasks.toJSONString());
            file.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write tasks to file.", e);
        }
    }

    private JSONObject findTaskById(UUID taskId) {
        JSONParser parser = new JSONParser();
        try {
            // 读取JSON文件
            Object obj = parser.parse(new FileReader("tasks.json"));
            JSONArray taskList = (JSONArray) obj;

            // 遍历任务列表
            for (Object taskObj : taskList) {
                if (taskObj instanceof JSONObject) {
                    JSONObject task = (JSONObject) taskObj;
                    String taskIdStr = (String) task.get("TaskID");
                    // 检查TaskID是否为null
                    if (taskIdStr != null) {
                        UUID currentTaskId = UUID.fromString(taskIdStr);
                        if (currentTaskId.equals(taskId)) {
                            return task; // 找到任务，返回JSONObject
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 可以考虑添加更多的用户通知或日志记录
        }
        throw new IllegalArgumentException("Task not found with ID: " + taskId); // 未找到任务，抛出异常
    }




    private void updateJsonFile(UUID taskId, JSONObject task) {
        JSONArray existingTasks = readExistingTasks();

        boolean found = false;
        for (int i = 0; i < existingTasks.size(); i++) {
            JSONObject existingTask = (JSONObject) existingTasks.get(i);
            String taskIdStr = (String) existingTask.get("TaskID");
            if (taskIdStr != null && UUID.fromString(taskIdStr).equals(taskId)) {
                existingTasks.set(i, task);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Task with ID " + taskId + " was not found and cannot be updated.");
        }

        try (FileWriter file = new FileWriter("tasks.json")) {
            file.write(existingTasks.toJSONString());
            file.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write tasks to file.", e);
        }
    }

    private void validateStatusChange(JSONObject task, String status, UserP user) {
        String currentStatus = (String) task.get("status");
        List<String> validStatuses = Arrays.asList("ToDo", "Doing", "WaitforConfirm", "Done", "OverDue");

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

    private void validateTaskModification(JSONObject task, UserP user) {
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

        JSONArray tasks = readExistingTasks();
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

    private void markOverdueTasks() {
        JSONArray tasks = readExistingTasks();
        boolean modified = false;

        for (Object taskObj : tasks) {
            JSONObject task = (JSONObject) taskObj;
            String status = (String) task.get("status");
            LocalDateTime endTime = LocalDateTime.parse((String) task.get("endTime"));

            if (("ToDo".equals(status) || "Doing".equals(status)) && LocalDateTime.now().isAfter(endTime)) {
                task.put("status", "OverDue");
                modified = true;
            }
        }

        if (modified) {
            saveTasks(tasks);
        }
    }

    private Duration getOverdueDuration(UUID taskId) {
        JSONObject task = findTaskById(taskId);
        LocalDateTime endTime = LocalDateTime.parse((String) task.get("endTime"));
        return LocalDateTime.now().isAfter(endTime) ? Duration.between(endTime, LocalDateTime.now()) : Duration.ZERO;
    }

    private void distributeReward(UUID taskId) {
        // Business logic for distributing rewards
        JSONObject task = findTaskById(taskId);
        // Update task with reward details
        updateJsonFile(taskId, task);
    }
}
