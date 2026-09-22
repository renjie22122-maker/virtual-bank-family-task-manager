import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.nio.file.Path;

/**
 * TaskDAO handles the persistence of task data.
 */
public class TaskDAO {
    private final Path tasksFilePath;

    public TaskDAO() {
        this(Path.of("tasks.json"));
    }

    public TaskDAO(Path tasksFilePath) {
        this.tasksFilePath = tasksFilePath;
    }

    /**
     * Reads all tasks from the JSON file.
     *
     * @return A JSONArray containing all tasks.
     */
    public JSONArray readAllTasks() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(tasksFilePath.toFile())) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            } else {
                return new JSONArray(); // If not an array, return empty array
            }
        } catch (java.io.FileNotFoundException e) {
            return new JSONArray(); // Return empty array if file does not exist or parse error
        } catch (IOException | ParseException e) {
            throw new IllegalStateException("Could not read tasks from " + tasksFilePath, e);
        }
    }

    /**
     * Writes all tasks to the JSON file.
     *
     * @param tasks A JSONArray containing all tasks to write.
     */
    public void writeAllTasks(JSONArray tasks) {
        try (FileWriter file = new FileWriter(tasksFilePath.toFile())) {
            file.write(tasks.toJSONString());
            file.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Could not write tasks to " + tasksFilePath, e);
        }
    }

    /**
     * Finds a task by its ID.
     *
     * @param taskId The UUID of the task to find.
     * @return The JSONObject representing the task if found, null otherwise.
     */
    public JSONObject findTaskById(UUID taskId) {
        JSONArray tasks = readAllTasks();
        for (Object taskObj : tasks) {
            JSONObject task = (JSONObject) taskObj;
            String taskIdStr = (String) task.get("TaskID");
            if (taskIdStr != null && UUID.fromString(taskIdStr).equals(taskId)) {
                return task;
            }
        }
        return null;
    }

    /**
     * Updates a task in the JSON file.
     *
     * @param taskId The UUID of the task to update.
     * @param updatedTask The updated JSONObject representing the task.
     */
    public void updateTask(UUID taskId, JSONObject updatedTask) {
        JSONArray tasks = readAllTasks();
        for (int i = 0; i < tasks.size(); i++) {
            JSONObject task = (JSONObject) tasks.get(i);
            String taskIdStr = (String) task.get("TaskID");
            if (taskIdStr != null && UUID.fromString(taskIdStr).equals(taskId)) {
                tasks.set(i, updatedTask);
                writeAllTasks(tasks);
                return;
            }
        }
        throw new IllegalArgumentException("Task with ID " + taskId + " not found.");
    }

    /**
     * Deletes a task from the JSON file.
     *
     * @param taskId The UUID of the task to delete.
     */
    public void deleteTask(UUID taskId) {
        JSONArray tasks = readAllTasks();
        boolean removed = tasks.removeIf(taskObj -> {
            JSONObject task = (JSONObject) taskObj;
            String taskIdStr = (String) task.get("TaskID");
            return taskIdStr != null && UUID.fromString(taskIdStr).equals(taskId);
        });
        if (!removed) {
            throw new IllegalArgumentException("Task with ID " + taskId + " not found.");
        }
        writeAllTasks(tasks);
    }

    /**
     * Adds a new task to the JSON file.
     *
     * @param newTask The JSONObject representing the new task.
     */
    public void saveTask(JSONObject newTask) {
        JSONArray tasks = readAllTasks();
        tasks.add(newTask);
        writeAllTasks(tasks);
    }


    public void updateAllTasks(JSONArray tasks) {
        writeAllTasks(tasks);
    }

    private JSONArray readExistingTasks() {
        try (FileReader reader = new FileReader("tasks.json")) {
            JSONParser parser = new JSONParser();
            return (JSONArray) parser.parse(reader);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to read tasks from file.", e);
        }
    }


    private void saveTasks(JSONArray tasks) {
        try (FileWriter file = new FileWriter("tasks.json")) {
            file.write(tasks.toJSONString());
            file.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write tasks to file.", e);
        }
    }

}
