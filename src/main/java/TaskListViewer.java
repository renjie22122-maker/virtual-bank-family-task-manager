import javax.swing.*;
import java.awt.*;

public class TaskListViewer extends JFrame {
    private TaskListPanel taskListPanel;

    public TaskListViewer(UserP user, TaskController taskController, AccountManager accountManager) {
        setTitle("TaskListViewer");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        taskListPanel = new TaskListPanel(user, taskController, accountManager);
        add(taskListPanel, BorderLayout.CENTER);

        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
