import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;
import org.json.simple.JSONObject;

public class TaskDetailsDialog extends JDialog {
    private JTextArea detailsArea;
    private JPanel buttonPanel;
    private JSONObject task;
    private UserP user;
    private TaskController taskController;
    private AccountManager accountManager;

    public TaskDetailsDialog(Window owner, JSONObject task, UserP user, TaskController taskController, AccountManager accountManager) {
        super(owner, "Task Details", ModalityType.APPLICATION_MODAL);
        this.task = task;
        this.user = user;
        this.taskController = taskController;
        this.accountManager = accountManager;

        initializeComponents();
        layoutComponents();
        registerEventHandlers();
        populateFields();
    }

    private void initializeComponents() {
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        buttonPanel = new JPanel(new FlowLayout());
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(500, 300);
        setLocationRelativeTo(getOwner());
    }

    private void registerEventHandlers() {
        String userType = user.getUserType();
        String taskStatus = (String) task.get("status");

        if ("child".equals(userType)) {
            if ("ToDo".equals(taskStatus)) {
                addButton("Accept Task", e -> updateTaskStatus("Doing"));
            } else if ("Doing".equals(taskStatus)) {
                addButton("Complete Task", e -> updateTaskStatus("WaitforConfirm"));
            }
        } else if ("parent".equals(userType)) {
            if ("WaitforCheck".equals(taskStatus)) {
                addButton("Approve Task", e -> approveTask());
                addButton("Reject Task", e -> rejectTask());
            } else if ("WaitforConfirm".equals(taskStatus)) {
                addButton("Confirm Completion", e -> confirmCompletion());
                addButton("Modify Reward", e -> openModifyRewardDialog());
            } else if ("ToDo".equals(taskStatus) || "WaitforCheck".equals(taskStatus)) {
                addButton("Modify Task", e -> openModifyTaskDialog());
                addButton("Delete Task", e -> deleteTask());
            }
        }
    }

    private void approveTask() {
        if ("parent".equals(user.getUserType())) {
            taskController.updateTaskStatus(UUID.fromString((String) task.get("TaskID")), "ToDo", user);
            JOptionPane.showMessageDialog(this, "Task approved.");
            dispose();
        }
    }

    private void rejectTask() {
        if ("parent".equals(user.getUserType())) {
            taskController.updateTaskStatus(UUID.fromString((String) task.get("TaskID")), "Reject", user);
            JOptionPane.showMessageDialog(this, "Task rejected.");
            dispose();
        }
    }

    private void confirmCompletion() {
        if ("parent".equals(user.getUserType())) {
            double reward = (Double) task.get("reward");
            double bonus = (Double) task.get("bonus");
            double totalAmount = reward + bonus;

            IAccount parentAccount = accountManager.getParentAccount(user.getUserId());
            IAccount childAccount = accountManager.getChildCheckingAccount((String) task.get("assignee"));

            if (childAccount != null) {
                try {
                    accountManager.transfer(parentAccount.getAccountId(), childAccount.getAccountId(), totalAmount, null);
                    taskController.updateTaskStatus(UUID.fromString((String) task.get("TaskID")), "Done", user);
                    JOptionPane.showMessageDialog(this, "Task confirmed and reward transferred.");
                    dispose();
                } catch (InsufficientFundsException e) {
                    JOptionPane.showMessageDialog(this, "Transfer failed: " + e.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Child does not have a checking account to receive the reward.");
            }
        }
    }

    private void addButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        buttonPanel.add(button);
    }

    private void populateFields() {
        detailsArea.setText(formatTaskDetails(task));
    }

    private void updateTaskStatus(String newStatus) {
        taskController.updateTaskStatus(UUID.fromString((String) task.get("TaskID")), newStatus, user);
        dispose();
    }

    private void deleteTask() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this task?", "Delete Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            taskController.deleteTask(UUID.fromString((String) task.get("TaskID")), user);
            dispose();
        }
    }

    private void openModifyTaskDialog() {
        JFrame modifyTaskFrame = new JFrame("Modify Task");
        modifyTaskFrame.setContentPane(new ModifyTaskPanel(task, user, taskController).getMainPanel());
        modifyTaskFrame.pack();
        modifyTaskFrame.setLocationRelativeTo(null);
        modifyTaskFrame.setVisible(true);
    }

    private void openModifyRewardDialog() {
        JDialog rewardEditDialog = new JDialog(this, "Modify Reward", true);
        rewardEditDialog.setLayout(new BorderLayout());

        Double maxBonus = (Double) task.get("maxBonus");

        JSlider bonusSlider = new JSlider(0, 100, 0);
        bonusSlider.setMajorTickSpacing(10);
        bonusSlider.setMinorTickSpacing(1);
        bonusSlider.setPaintTicks(true);
        bonusSlider.setPaintLabels(true);
        bonusSlider.setBackground(Color.WHITE);

        rewardEditDialog.add(bonusSlider, BorderLayout.CENTER);

        JPanel confirmPanel = new JPanel();
        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            int bonusPercentage = bonusSlider.getValue();
            double actualBonus = bonusPercentage / 100.0 * maxBonus;
            task.put("bonus", actualBonus);
            taskController.modifyTask(UUID.fromString((String) task.get("TaskID")), task, user);
            rewardEditDialog.dispose();
            dispose();
        });
        confirmPanel.add(confirmButton);

        rewardEditDialog.add(confirmPanel, BorderLayout.SOUTH);
        rewardEditDialog.setSize(300, 200);
        rewardEditDialog.setLocationRelativeTo(null);
        rewardEditDialog.setVisible(true);
    }

    private String formatTaskDetails(JSONObject taskData) {
        StringBuilder details = new StringBuilder();
        details.append("Task Name: ").append(taskData.get("name")).append("\n");
        details.append("Description: ").append(taskData.get("description")).append("\n");
        details.append("Urgency: ").append(taskData.get("urgency")).append("\n");
        details.append("Repeat: ").append(taskData.get("repeat")).append("\n");
        details.append("Reward: ").append(taskData.get("reward")).append("\n");
        details.append("Max Bonus: ").append(taskData.get("maxBonus")).append("\n");
        details.append("Bonus: ").append(taskData.get("bonus")).append("\n");
        details.append("Start Time: ").append(taskData.get("startTime")).append("\n");
        details.append("End Time: ").append(taskData.get("endTime")).append("\n");
        details.append("Assignee: ").append(taskData.get("assigneeName")).append("\n");
        details.append("Collaborator: ").append(taskData.get("collaboratorName")).append("\n");
        details.append("Creator: ").append(taskData.get("creatorName")).append("\n");
        details.append("Status: ").append(taskData.get("status")).append("\n");
        details.append("Settlement Type: ").append(taskData.get("settlementType")).append("\n");
        if (taskData.get("actualStartTime") != null) {
            details.append("Actual Start Time: ").append(taskData.get("actualStartTime")).append("\n");
        }
        if (taskData.get("timedDuration") != null) {
            details.append("Timed Duration: ").append(taskData.get("timedDuration")).append("\n");
        }
        return details.toString();
    }
}
