import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TaskListPanel extends JPanel {
    private final UserP user;
    private final TaskApplicationService tasks;
    private final JComboBox<StatusOption> status = new JComboBox<>(StatusOption.values());
    private final JComboBox<SortOption> sort = new JComboBox<>(SortOption.values());
    private final DefaultListModel<TaskView> model = new DefaultListModel<>();
    private final JList<TaskView> list = new JList<>(model);
    private final JLabel count = AppTheme.muted("");
    private final JButton open = AppTheme.primaryButton("Open task");

    public TaskListPanel(UserP user, TaskApplicationService tasks) {
        this.user = user;
        this.tasks = tasks;
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        setBackground(AppTheme.BACKGROUND);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildList(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        registerEvents();
        loadTasks();
    }

    private JComponent buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 14)); wrapper.setOpaque(false);
        JPanel copy = new JPanel(); copy.setOpaque(false); copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(AppTheme.title("Task board")); copy.add(Box.createVerticalStrut(4));
        copy.add(AppTheme.muted("Track family work, approvals and rewards in one place."));
        wrapper.add(copy, BorderLayout.NORTH);
        JPanel filters = AppTheme.card(new GridLayout(1, 2, 14, 0));
        filters.add(fieldGroup("Status", status)); filters.add(fieldGroup("Sort by", sort));
        wrapper.add(filters, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent fieldGroup(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6)); panel.setOpaque(false);
        panel.add(AppTheme.fieldLabel(label), BorderLayout.NORTH); panel.add(AppTheme.field(field), BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildList() {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); list.setFixedCellHeight(92);
        list.setCellRenderer(new TaskRenderer()); list.setBackground(AppTheme.SURFACE);
        return AppTheme.scroll(list);
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout()); footer.setOpaque(false); footer.add(count, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actions.setOpaque(false);
        JButton refresh = AppTheme.secondaryButton("Refresh"); refresh.addActionListener(e -> loadTasks());
        open.setEnabled(false); open.addActionListener(e -> openSelected());
        actions.add(refresh); actions.add(open); footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private void registerEvents() {
        status.addActionListener(e -> loadTasks()); sort.addActionListener(e -> loadTasks());
        list.addListSelectionListener(e -> open.setEnabled(!list.isSelectionEmpty()));
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) { if (event.getClickCount() == 2 && !list.isSelectionEmpty()) openSelected(); }
        });
    }

    private void loadTasks() {
        try {
            StatusOption selectedStatus = (StatusOption) status.getSelectedItem();
            SortOption selectedSort = (SortOption) sort.getSelectedItem();
            List<TaskView> items = tasks.listTasks(selectedStatus.value, user, selectedSort.value);
            model.clear(); items.forEach(model::addElement);
            count.setText(items.isEmpty() ? "No tasks match this filter." : items.size() + " task" + (items.size() == 1 ? "" : "s"));
        } catch (RuntimeException exception) { AppTheme.showError(this, exception.getMessage()); }
    }

    private void openSelected() {
        TaskView selected = list.getSelectedValue(); if (selected == null) return;
        TaskView current = tasks.getTask(selected.id());
        if (current == null) { AppTheme.showError(this, "This task no longer exists."); loadTasks(); return; }
        new TaskDetailsDialog(SwingUtilities.getWindowAncestor(this), current, user, tasks).setVisible(true);
        loadTasks();
    }

    private enum StatusOption {
        PENDING("WaitforCheck", "Pending approval"), REJECTED("Reject", "Rejected"), TODO("ToDo", "To do"),
        DOING("Doing", "In progress"), CONFIRM("WaitforConfirm", "Awaiting confirmation"),
        DONE("Done", "Completed"), OVERDUE("OverDue", "Overdue");
        final String value; final String label;
        StatusOption(String value, String label) { this.value = value; this.label = label; }
        public String toString() { return label; }
    }

    private enum SortOption {
        REWARD("reward", "Highest reward"), TIME("time", "Deadline"), URGENCY("urgency", "Urgency");
        final String value; final String label;
        SortOption(String value, String label) { this.value = value; this.label = label; }
        public String toString() { return label; }
    }

    private static class TaskRenderer extends JPanel implements ListCellRenderer<TaskView> {
        private final JLabel title = AppTheme.heading(""); private final JLabel detail = AppTheme.muted("");
        private final JLabel reward = new JLabel();
        TaskRenderer() {
            setLayout(new BorderLayout(14, 4)); setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            JPanel copy = new JPanel(); copy.setOpaque(false); copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
            copy.add(title); copy.add(Box.createVerticalStrut(6)); copy.add(detail);
            reward.setFont(new Font("Dialog", Font.BOLD, 16)); reward.setForeground(AppTheme.PRIMARY);
            add(copy, BorderLayout.CENTER); add(reward, BorderLayout.EAST);
        }
        public Component getListCellRendererComponent(JList<? extends TaskView> list, TaskView task, int index, boolean selected, boolean focused) {
            title.setText(task.name().isBlank() ? "Untitled task" : task.name());
            String owner = task.assigneeName().isBlank() ? "Unassigned" : task.assigneeName();
            detail.setText(owner + "  •  Due " + friendlyDate(task.endTime()) + "  •  Urgency " + task.urgency());
            reward.setText(AppTheme.money(task.reward() + task.bonus()));
            setBackground(selected ? AppTheme.SOFT_BLUE : AppTheme.SURFACE); setOpaque(true); return this;
        }
        private static String friendlyDate(String value) {
            try { return LocalDateTime.parse(value).format(DateTimeFormatter.ofPattern("MMM d, yyyy · HH:mm")); }
            catch (DateTimeParseException exception) { return value; }
        }
    }
}
