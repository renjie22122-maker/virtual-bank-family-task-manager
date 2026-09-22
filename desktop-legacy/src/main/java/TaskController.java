import org.json.simple.JSONObject;
import java.util.UUID;

public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    public void createTask(JSONObject taskData, UserP user) {
        taskService.createTask(taskData, user);
    }

    public void updateTaskStatus(UUID taskId, String status, UserP user) {
        taskService.updateTaskStatus(taskId, status, user);
    }

    public void modifyTask(UUID taskId, JSONObject taskData, UserP user) {
        taskService.modifyTask(taskId, taskData, user);
    }

    public void deleteTask(UUID taskId, UserP user) {
        taskService.deleteTask(taskId, user);
    }

    public JSONObject viewTaskList(String status, UserP user, String sortOption) {
        return taskService.viewTaskList(status, user, sortOption);
    }


    public JSONObject viewTaskDetails(UUID taskId) {
        return taskService.findTaskById(taskId);
    }
}
