import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class TaskListPanel extends JPanel {
    private JComboBox<String> statusComboBox;
    private JComboBox<String> sortComboBox;
    private JList<TaskEntry> taskList;
    private DefaultListModel<TaskEntry> taskListModel;
    private TaskController taskController;
    private UserP user;
    private AccountManager accountManager;

    public TaskListPanel(UserP user, TaskController taskController, AccountManager accountManager) {
        this.user = user;
        this.taskController = taskController;
        this.accountManager = accountManager;
        initializeComponents();
        layoutComponents();
        registerEventHandlers();
        loadTasks();
    }

    private void initializeComponents() {
        statusComboBox = new JComboBox<>(new String[]{"WaitforCheck", "Reject", "ToDo", "Doing", "WaitforConfirm", "Done", "OverDue"});
        sortComboBox = new JComboBox<>(new String[]{"reward", "time", "urgency"});
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        JPanel dropdownPanel = new JPanel(new GridLayout(1, 2));
        dropdownPanel.add(statusComboBox);
        dropdownPanel.add(sortComboBox);
        add(dropdownPanel, BorderLayout.NORTH);

        add(new JScrollPane(taskList), BorderLayout.CENTER);
    }

    private void registerEventHandlers() {
        statusComboBox.addActionListener(e -> loadTasks());
        sortComboBox.addActionListener(e -> loadTasks());
        taskList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = taskList.locationToIndex(evt.getPoint());
                    TaskEntry entry = taskListModel.getElementAt(index);
                    UUID taskId = entry.getTaskId();
                    JSONObject task = taskController.viewTaskDetails(taskId);

                    if (task != null) {
                        TaskDetailsDialog detailsDialog = new TaskDetailsDialog(
                                SwingUtilities.getWindowAncestor(TaskListPanel.this),
                                task,
                                user,
                                taskController,
                                accountManager
                        );
                        detailsDialog.setVisible(true);
                        loadTasks();
                    }
                }
            }
        });
    }

    private void loadTasks() {
        String selectedStatus = (String) statusComboBox.getSelectedItem();
        String selectedSort = (String) sortComboBox.getSelectedItem();
        JSONObject tasks = taskController.viewTaskList(selectedStatus, user, selectedSort);
        JSONArray tasksArray = (JSONArray) tasks.get("tasks");

        SwingUtilities.invokeLater(() -> {
            taskListModel.clear();
            for (Object taskObj : tasksArray) {
                JSONObject task = (JSONObject) taskObj;
                String taskIdStr = (String) task.get("TaskID");
                UUID uuid = UUID.fromString(taskIdStr);
                String taskName = (String) task.get("name");
                String assigneeName = (String) task.get("assigneeName");
                String collaboratorName = (String) task.get("collaboratorName");
                String startTime = (String) task.get("startTime");
                String endTime = (String) task.get("endTime");
                String displayText = taskName + " - " + assigneeName + " - " + collaboratorName + " - " + startTime + " - " + endTime;
                taskListModel.addElement(new TaskEntry(uuid, displayText));
            }
        });
    }
}
