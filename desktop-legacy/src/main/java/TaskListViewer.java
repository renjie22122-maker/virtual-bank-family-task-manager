import javax.swing.*;

public class TaskListViewer extends JFrame {
    public TaskListViewer(UserP user, TaskController taskController, AccountManager accountManager) {
        AppTheme.configureFrame(this, "Task board", 960, 650);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setContentPane(new TaskListPanel(user, new TaskApplicationService(taskController, accountManager)));
    }
}
