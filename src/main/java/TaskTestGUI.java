import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

/** Task creation form retained under its original class name for compatibility. */
public class TaskTestGUI {
    private final JPanel mainPanel = AppTheme.page();
    private final JTextField nameField = AppTheme.field(new JTextField());
    private final JTextArea descriptionArea = new JTextArea(4, 24);
    private final JComboBox<Integer> urgencyComboBox = AppTheme.field(new JComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5}));
    private final JComboBox<String> repeatComboBox = AppTheme.field(new JComboBox<>(new String[]{"None", "Daily", "Weekly", "Monthly"}));
    private final JTextField rewardField = AppTheme.field(new JTextField("0"));
    private final JTextField bonusField = AppTheme.field(new JTextField("0"));
    private final JSpinner startTimeSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner endTimeSpinner = new JSpinner(new SpinnerDateModel());
    private final JComboBox<MemberItem> assigneeComboBox = AppTheme.field(new JComboBox<>());
    private final JComboBox<MemberItem> collaboratorComboBox = AppTheme.field(new JComboBox<>());
    private final JButton createButton = AppTheme.primaryButton("Create task");
    private final UserP user;
    private final TaskController taskController = new TaskController(new TaskService(new TaskDAO()));

    public TaskTestGUI(UserP user) {
        this.user = user;
        configureSpinner(startTimeSpinner, new Date(System.currentTimeMillis() + 3_600_000));
        configureSpinner(endTimeSpinner, new Date(System.currentTimeMillis() + 7_200_000));
        AppTheme.field(startTimeSpinner);
        AppTheme.field(endTimeSpinner);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(AppTheme.BODY);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        buildUI();
        loadFamilyMembers();
        createButton.addActionListener(e -> createTask());
    }

    private void buildUI() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(AppTheme.title("Create a task"));
        header.add(Box.createVerticalStrut(5));
        header.add(AppTheme.muted("Set clear expectations, timing, and a motivating virtual reward."));
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel form = AppTheme.card(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        addField(form, gbc, 0, 0, "Task name", nameField);
        addField(form, gbc, 1, 0, "Description", AppTheme.scroll(descriptionArea));
        addField(form, gbc, 0, 2, "Urgency", urgencyComboBox);
        addField(form, gbc, 1, 2, "Repeat", repeatComboBox);
        addField(form, gbc, 0, 4, "Reward", rewardField);
        addField(form, gbc, 1, 4, "Maximum bonus", bonusField);
        addField(form, gbc, 0, 6, "Start time", startTimeSpinner);
        addField(form, gbc, 1, 6, "End time", endTimeSpinner);
        addField(form, gbc, 0, 8, "Assignee", assigneeComboBox);
        addField(form, gbc, 1, 8, "Collaborator", collaboratorComboBox);
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 6, 2, 6);
        form.add(createButton, gbc);
        mainPanel.add(form, BorderLayout.CENTER);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int column, int row, String label, JComponent field) {
        gbc.gridx = column;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(row == 0 ? 2 : 13, 6, 5, 6);
        panel.add(AppTheme.fieldLabel(label), gbc);
        gbc.gridy = row + 1;
        gbc.insets = new Insets(0, 6, 0, 6);
        panel.add(field, gbc);
    }

    private void configureSpinner(JSpinner spinner, Date value) {
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd HH:mm"));
        spinner.setValue(value);
    }

    private void loadFamilyMembers() {
        MemberItem none = new MemberItem(null, "None");
        assigneeComboBox.addItem(none);
        collaboratorComboBox.addItem(none);
        JSONArray users = new UserDAO().readAllUsers();
        for (Object value : users) {
            JSONObject member = (JSONObject) value;
            if (user.getFamilyGroupId().equals(member.get("familyGroupId"))) {
                MemberItem item = new MemberItem((String) member.get("userId"), (String) member.get("userName"));
                assigneeComboBox.addItem(item);
                collaboratorComboBox.addItem(item);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void createTask() {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("Task name cannot be empty.");
            double reward = parseNonNegative(rewardField.getText(), "Reward");
            double bonus = parseNonNegative(bonusField.getText(), "Maximum bonus");
            LocalDateTime start = toLocalDateTime((Date) startTimeSpinner.getValue());
            LocalDateTime end = toLocalDateTime((Date) endTimeSpinner.getValue());
            if (!start.isBefore(end)) throw new IllegalArgumentException("End time must be after start time.");
            MemberItem assignee = (MemberItem) assigneeComboBox.getSelectedItem();
            MemberItem collaborator = (MemberItem) collaboratorComboBox.getSelectedItem();

            JSONObject task = new JSONObject();
            task.put("TaskID", UUID.randomUUID().toString());
            task.put("name", name);
            task.put("description", descriptionArea.getText().trim());
            task.put("urgency", urgencyComboBox.getSelectedItem());
            task.put("repeat", repeatComboBox.getSelectedItem());
            task.put("reward", reward);
            task.put("maxBonus", bonus);
            task.put("bonus", null);
            task.put("startTime", start.toString());
            task.put("endTime", end.toString());
            task.put("assignee", assignee == null ? null : assignee.id);
            task.put("assigneeName", assignee == null ? "None" : assignee.name);
            task.put("collaborator", collaborator == null ? null : collaborator.id);
            task.put("collaboratorName", collaborator == null ? "None" : collaborator.name);
            task.put("creatorName", user.getUserName());
            task.put("creator", user.getUserId());
            task.put("familyGroupID", user.getFamilyGroupId());
            task.put("actualStartTime", null);
            task.put("timedDuration", null);
            task.put("settlementType", "countByTime");
            taskController.createTask(task, user);
            AppTheme.showSuccess(mainPanel, "Task created successfully.");
            Window window = SwingUtilities.getWindowAncestor(mainPanel);
            if (window != null) window.dispose();
        } catch (RuntimeException ex) {
            AppTheme.showError(mainPanel, ex.getMessage());
        }
    }

    private double parseNonNegative(String text, String fieldName) {
        try {
            double value = Double.parseDouble(text.trim());
            if (!Double.isFinite(value) || value < 0) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a non-negative number.");
        }
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public JPanel getMainPanel() { return mainPanel; }

    private static class MemberItem {
        private final String id;
        private final String name;
        private MemberItem(String id, String name) { this.id = id; this.name = name; }
        public String toString() { return name; }
    }
}
