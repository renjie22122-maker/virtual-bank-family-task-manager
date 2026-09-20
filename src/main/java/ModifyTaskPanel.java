import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class ModifyTaskPanel {
    private final JPanel mainPanel = AppTheme.page();
    private final JTextField nameField = AppTheme.field(new JTextField());
    private final JTextArea descriptionArea = new JTextArea(4, 20);
    private final JComboBox<Integer> urgency = AppTheme.field(new JComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5}));
    private final JComboBox<String> repeat = AppTheme.field(new JComboBox<>(new String[]{"None", "Daily", "Weekly", "Monthly"}));
    private final JTextField reward = AppTheme.field(new JTextField());
    private final JTextField maxBonus = AppTheme.field(new JTextField());
    private final JSpinner start = AppTheme.field(new JSpinner(new SpinnerDateModel()));
    private final JSpinner end = AppTheme.field(new JSpinner(new SpinnerDateModel()));
    private final TaskView task;
    private final UserP user;
    private final TaskApplicationService tasks;

    public ModifyTaskPanel(TaskView task, UserP user, TaskApplicationService tasks) {
        this.task = task; this.user = user; this.tasks = tasks;
        start.setEditor(new JSpinner.DateEditor(start, "yyyy-MM-dd HH:mm"));
        end.setEditor(new JSpinner.DateEditor(end, "yyyy-MM-dd HH:mm"));
        descriptionArea.setLineWrap(true); descriptionArea.setWrapStyleWord(true); descriptionArea.setFont(AppTheme.BODY);
        buildUI(); populateFields();
    }

    private void buildUI() {
        JPanel header = new JPanel(); header.setOpaque(false); header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(AppTheme.title("Edit task")); header.add(AppTheme.muted("Update the task without exposing storage details to this screen."));
        mainPanel.add(header, BorderLayout.NORTH);
        JPanel form = AppTheme.card(new GridLayout(0, 2, 12, 10));
        add(form, "Task name", nameField); add(form, "Description", AppTheme.scroll(descriptionArea));
        add(form, "Urgency", urgency); add(form, "Repeat", repeat); add(form, "Reward", reward);
        add(form, "Maximum bonus", maxBonus); add(form, "Start time", start); add(form, "End time", end);
        mainPanel.add(form, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); actions.setOpaque(false);
        JButton cancel = AppTheme.secondaryButton("Cancel"); JButton save = AppTheme.primaryButton("Save changes");
        cancel.addActionListener(e -> close()); save.addActionListener(e -> save()); actions.add(cancel); actions.add(save);
        mainPanel.add(actions, BorderLayout.SOUTH);
    }

    private void add(JPanel panel, String label, JComponent component) { panel.add(AppTheme.fieldLabel(label)); panel.add(component); }

    private void populateFields() {
        nameField.setText(task.name()); descriptionArea.setText(task.description()); urgency.setSelectedItem(task.urgency());
        repeat.setSelectedItem(task.repeat()); reward.setText(Double.toString(task.reward()));
        maxBonus.setText(Double.toString(task.maxBonus())); start.setValue(toDate(task.startTime())); end.setValue(toDate(task.endTime()));
    }

    private void save() {
        try {
            String name = nameField.getText().trim(); if (name.isEmpty()) throw new IllegalArgumentException("Task name cannot be empty.");
            double rewardValue = parseNumber(reward.getText(), "Reward");
            double bonusValue = parseNumber(maxBonus.getText(), "Maximum bonus");
            LocalDateTime startValue = toLocal(start); LocalDateTime endValue = toLocal(end);
            if (!startValue.isBefore(endValue)) throw new IllegalArgumentException("End time must be after start time.");
            tasks.updateTask(task.id(), new TaskApplicationService.TaskUpdate(name, descriptionArea.getText().trim(),
                    (Integer) urgency.getSelectedItem(), (String) repeat.getSelectedItem(), rewardValue, bonusValue,
                    startValue.toString(), endValue.toString(), task.settlementType()), user);
            AppTheme.showSuccess(mainPanel, "Task updated."); close();
        } catch (RuntimeException exception) { AppTheme.showError(mainPanel, exception.getMessage()); }
    }

    private double parseNumber(String text, String name) {
        try { double value = Double.parseDouble(text.trim()); if (!Double.isFinite(value) || value < 0) throw new NumberFormatException(); return value; }
        catch (NumberFormatException exception) { throw new IllegalArgumentException(name + " must be a non-negative number."); }
    }

    private Date toDate(String value) {
        try { return Date.from(LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant()); }
        catch (RuntimeException exception) { return new Date(); }
    }
    private LocalDateTime toLocal(JSpinner spinner) { return ((Date) spinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(); }
    private void close() { Window window = SwingUtilities.getWindowAncestor(mainPanel); if (window != null) window.dispose(); }
    public JPanel getMainPanel() { return mainPanel; }
}
