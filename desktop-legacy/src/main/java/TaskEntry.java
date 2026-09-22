import java.util.UUID;

/**
 * TaskEntry class for mapping task UUID to displayable text.
 * This class is useful in GUI components where a simple representation of a task is needed.
 */
public class TaskEntry {
    private UUID taskId;  // Unique identifier for the task
    private String displayText;  // Text to be displayed in the GUI, describing the task

    /**
     * Constructor for creating a new TaskEntry object.
     * @param taskId the UUID of the task
     * @param displayText the text that describes the task for display purposes
     */
    public TaskEntry(UUID taskId, String displayText) {
        this.taskId = taskId;
        this.displayText = displayText;
    }

    /**
     * Gets the UUID of the task.
     * @return the UUID of the task
     */
    public UUID getTaskId() {
        return taskId;
    }

    /**
     * Sets the UUID of the task.
     * @param taskId the UUID of the task to set
     */
    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    /**
     * Gets the display text of the task.
     * @return the display text
     */
    public String getDisplayText() {
        return displayText;
    }

    /**
     * Sets the display text of the task.
     * @param displayText the text to set
     */
    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    /**
     * Provides a string representation of the TaskEntry, which is the display text.
     * This is used by GUI components like JComboBox, JList, etc., to show the task information.
     * @return the display text
     */
    @Override
    public String toString() {
        return displayText;
    }
}
