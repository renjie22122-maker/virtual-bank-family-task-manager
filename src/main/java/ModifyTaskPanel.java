import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import org.json.simple.JSONObject;

public class ModifyTaskPanel {
    private JPanel mainPanel;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JComboBox<Integer> urgencyComboBox;
    private JComboBox<String> repeatComboBox;
    private JTextField rewardField;
    private JTextField maxBonusField;
    private JSpinner startTimeSpinner;
    private JSpinner endTimeSpinner;
    private JButton saveButton;
    private JButton cancelButton;

    private JSONObject task;
    private UserP user;
    private TaskController taskController;

    public ModifyTaskPanel(JSONObject task, UserP user, TaskController taskController) {
        this.task = task;
        this.user = user;
        this.taskController = taskController;
        initializeComponents();
        layoutComponents();
        registerEventHandlers();
        populateFields();
    }

    private void initializeComponents() {
        mainPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        nameField = new JTextField();
        descriptionArea = new JTextArea(5, 20);
        urgencyComboBox = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5});
        repeatComboBox = new JComboBox<>(new String[]{"None", "Daily", "Weekly", "Monthly"});
        rewardField = new JTextField();
        maxBonusField = new JTextField();
        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        // Configure the date spinners
        configureDateSpinner(startTimeSpinner);
        configureDateSpinner(endTimeSpinner);
    }

    private void layoutComponents() {
        mainPanel.add(new JLabel("Task Name:"));
        mainPanel.add(nameField);
        mainPanel.add(new JLabel("Description:"));
        mainPanel.add(new JScrollPane(descriptionArea));
        mainPanel.add(new JLabel("Urgency:"));
        mainPanel.add(urgencyComboBox);
        mainPanel.add(new JLabel("Repeat:"));
        mainPanel.add(repeatComboBox);
        mainPanel.add(new JLabel("Reward:"));
        mainPanel.add(rewardField);
        mainPanel.add(new JLabel("Max Bonus:"));
        mainPanel.add(maxBonusField);
        mainPanel.add(new JLabel("Start Time:"));
        mainPanel.add(startTimeSpinner);
        mainPanel.add(new JLabel("End Time:"));
        mainPanel.add(endTimeSpinner);
        mainPanel.add(saveButton);
        mainPanel.add(cancelButton);
    }

    private void configureDateSpinner(JSpinner spinner) {
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd HH:mm:ss");
        spinner.setEditor(editor);
        spinner.setValue(new Date()); // Set current date as default
    }

    private void registerEventHandlers() {
        saveButton.addActionListener(this::onSave);
        cancelButton.addActionListener(e -> ((JFrame) SwingUtilities.getWindowAncestor(mainPanel)).dispose());
    }

    private void populateFields() {
        nameField.setText((String) task.get("name"));
        descriptionArea.setText((String) task.get("description"));
        urgencyComboBox.setSelectedItem(task.get("urgency"));
        repeatComboBox.setSelectedItem(task.get("repeat"));
        rewardField.setText(task.get("reward").toString());
        maxBonusField.setText(task.get("maxBonus").toString());
        startTimeSpinner.setValue(Date.from(LocalDateTime.parse((String) task.get("startTime")).atZone(ZoneId.systemDefault()).toInstant()));
        endTimeSpinner.setValue(Date.from(LocalDateTime.parse((String) task.get("endTime")).atZone(ZoneId.systemDefault()).toInstant()));
    }

    private void onSave(ActionEvent e) {
        task.put("name", nameField.getText());
        task.put("description", descriptionArea.getText());
        task.put("urgency", urgencyComboBox.getSelectedItem());
        task.put("repeat", repeatComboBox.getSelectedItem());
        task.put("reward", Double.parseDouble(rewardField.getText()));
        task.put("maxBonus", Double.parseDouble(maxBonusField.getText()));
        task.put("startTime", LocalDateTime.ofInstant(((Date) startTimeSpinner.getValue()).toInstant(), ZoneId.systemDefault()).toString());
        task.put("endTime", LocalDateTime.ofInstant(((Date) endTimeSpinner.getValue()).toInstant(), ZoneId.systemDefault()).toString());

        taskController.modifyTask(UUID.fromString((String) task.get("TaskID")), task, user);

        JOptionPane.showMessageDialog(mainPanel, "Task modified successfully!");
        ((JFrame) SwingUtilities.getWindowAncestor(mainPanel)).dispose();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
