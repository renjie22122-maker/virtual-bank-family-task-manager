import javax.swing.*;
import java.awt.*;

public class TaskDetailsDialog extends JDialog {
    private final TaskView task;
    private final UserP user;
    private final TaskApplicationService tasks;
    private final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

    public TaskDetailsDialog(Window owner, TaskView task, UserP user, TaskApplicationService tasks) {
        super(owner, "Task details", ModalityType.APPLICATION_MODAL);
        this.task = task; this.user = user; this.tasks = tasks;
        setContentPane(buildContent());
        setSize(720, 610); setMinimumSize(new Dimension(660, 560)); setLocationRelativeTo(owner);
    }

    private JPanel buildContent() {
        JPanel root = AppTheme.page();
        JPanel header = new JPanel(new BorderLayout(12, 6)); header.setOpaque(false);
        JPanel copy = new JPanel(); copy.setOpaque(false); copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(AppTheme.title(task.name())); copy.add(Box.createVerticalStrut(4));
        copy.add(AppTheme.muted("Created by " + fallback(task.creatorName(), "a family member")));
        JLabel status = new JLabel(friendlyStatus(task.status())); status.setOpaque(true);
        status.setBackground(AppTheme.SOFT_BLUE); status.setForeground(AppTheme.PRIMARY);
        status.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        header.add(copy, BorderLayout.CENTER); header.add(status, BorderLayout.EAST); root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 14)); body.setOpaque(false);
        JPanel facts = AppTheme.card(new GridLayout(0, 2, 14, 12));
        fact(facts, "Assignee", fallback(task.assigneeName(), "Unassigned"));
        fact(facts, "Collaborator", fallback(task.collaboratorName(), "None"));
        fact(facts, "Start", task.startTime()); fact(facts, "Deadline", task.endTime());
        fact(facts, "Urgency", Integer.toString(task.urgency())); fact(facts, "Repeat", fallback(task.repeat(), "None"));
        fact(facts, "Reward", AppTheme.money(task.reward()));
        fact(facts, "Bonus", AppTheme.money(task.bonus()) + " / " + AppTheme.money(task.maxBonus()));
        body.add(facts, BorderLayout.NORTH);
        JPanel description = AppTheme.card(new BorderLayout(0, 8));
        description.add(AppTheme.fieldLabel("Description"), BorderLayout.NORTH);
        JTextArea text = new JTextArea(fallback(task.description(), "No description provided."));
        text.setEditable(false); text.setLineWrap(true); text.setWrapStyleWord(true); text.setOpaque(false);
        text.setForeground(AppTheme.MUTED); text.setFont(AppTheme.BODY); description.add(text, BorderLayout.CENTER);
        body.add(description, BorderLayout.CENTER); root.add(body, BorderLayout.CENTER);

        actions.setOpaque(false); configureActions(); root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private void fact(JPanel panel, String label, String value) {
        JPanel cell = new JPanel(); cell.setOpaque(false); cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.add(AppTheme.muted(label.toUpperCase())); cell.add(Box.createVerticalStrut(3));
        JLabel content = new JLabel(value); content.setFont(AppTheme.BODY); content.setForeground(AppTheme.TEXT); cell.add(content);
        panel.add(cell);
    }

    private void configureActions() {
        if ("child".equals(user.getUserType())) {
            if ("ToDo".equals(task.status())) addPrimary("Accept task", () -> changeStatus("Doing"));
            else if ("Doing".equals(task.status())) addPrimary("Mark complete", () -> changeStatus("WaitforConfirm"));
            return;
        }
        if (!"parent".equals(user.getUserType())) return;
        if ("WaitforCheck".equals(task.status())) {
            addSecondary("Reject", () -> changeStatus("Reject")); addPrimary("Approve", () -> changeStatus("ToDo"));
        } else if ("WaitforConfirm".equals(task.status())) {
            addSecondary("Adjust bonus", this::openRewardDialog); addPrimary("Confirm & pay", this::confirmCompletion);
        } else if ("ToDo".equals(task.status())) {
            addDanger("Delete", this::deleteTask); addPrimary("Edit task", this::openModifyDialog);
        }
    }

    private void changeStatus(String value) {
        try { tasks.changeStatus(task.id(), value, user); AppTheme.showSuccess(this, "Task status updated."); dispose(); }
        catch (RuntimeException exception) { AppTheme.showError(this, exception.getMessage()); }
    }

    private void confirmCompletion() {
        try { tasks.confirmCompletion(task, user); AppTheme.showSuccess(this, "Completion confirmed and reward transferred."); dispose(); }
        catch (InsufficientFundsException | RuntimeException exception) { AppTheme.showError(this, exception.getMessage()); }
    }

    private void deleteTask() {
        if (JOptionPane.showConfirmDialog(this, "Delete this task permanently?", "Delete task", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { tasks.deleteTask(task.id(), user); dispose(); }
        catch (RuntimeException exception) { AppTheme.showError(this, exception.getMessage()); }
    }

    private void openModifyDialog() {
        JDialog dialog = new JDialog(this, "Edit task", true);
        dialog.setContentPane(new ModifyTaskPanel(task, user, tasks).getMainPanel());
        dialog.setSize(720, 660); dialog.setLocationRelativeTo(this); dialog.setVisible(true); dispose();
    }

    private void openRewardDialog() {
        JSlider slider = new JSlider(0, 100, task.maxBonus() == 0 ? 0 : (int) Math.round(task.bonus() / task.maxBonus() * 100));
        slider.setMajorTickSpacing(25); slider.setPaintTicks(true); slider.setPaintLabels(true);
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.add(new JLabel("Choose a percentage of the maximum bonus (" + AppTheme.money(task.maxBonus()) + ")."), BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, panel, "Adjust bonus", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try { tasks.updateBonus(task.id(), slider.getValue() / 100.0 * task.maxBonus(), user); AppTheme.showSuccess(this, "Bonus updated."); dispose(); }
        catch (RuntimeException exception) { AppTheme.showError(this, exception.getMessage()); }
    }

    private void addPrimary(String text, Runnable action) { addButton(AppTheme.primaryButton(text), action); }
    private void addSecondary(String text, Runnable action) { addButton(AppTheme.secondaryButton(text), action); }
    private void addDanger(String text, Runnable action) { addButton(AppTheme.dangerButton(text), action); }
    private void addButton(JButton button, Runnable action) { button.addActionListener(e -> action.run()); actions.add(button); }
    private static String fallback(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    private static String friendlyStatus(String status) {
        return switch (status) {
            case "WaitforCheck" -> "Pending approval"; case "Reject" -> "Rejected"; case "ToDo" -> "To do";
            case "Doing" -> "In progress"; case "WaitforConfirm" -> "Awaiting confirmation";
            case "Done" -> "Completed"; case "OverDue" -> "Overdue"; default -> status;
        };
    }
}
