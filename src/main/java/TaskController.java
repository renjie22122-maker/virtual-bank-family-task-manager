import org.json.simple.JSONObject;
import java.util.UUID;

public class TaskController {
    private TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    public void createTask(JSONObject taskData, UserP user) {
        try {
            taskService.createTask(taskData, user);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void updateTaskStatus(UUID taskId, String status, UserP user) {
        try {
            taskService.updateTaskStatus(taskId, status, user);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void modifyTask(UUID taskId, JSONObject taskData, UserP user) {
        try {
            taskService.modifyTask(taskId, taskData, user);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void deleteTask(UUID taskId, UserP user) {
        try {
            taskService.deleteTask(taskId, user);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public JSONObject viewTaskList(String status, UserP user, String sortOption) {
        try {
            return taskService.viewTaskList(status, user, sortOption);
        } catch (Exception e) {
            ErrorHandler.handle(e);
            return new JSONObject();
        }
    }


    public JSONObject viewTaskDetails(UUID taskId) {
        try {
            return taskService.findTaskById(taskId);
        } catch (Exception e) {
            ErrorHandler.handle(e);
            return new JSONObject();
        }
    }
}
