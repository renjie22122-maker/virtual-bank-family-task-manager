import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.time.ZoneId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.io.IOException;

public class TaskTestGUI {
    private JPanel mainPanel;
    private JTextField nameField, rewardField, bonusField;
    private JTextArea descriptionArea;
    private JComboBox<String> repeatComboBox, assigneeComboBox, collaboratorComboBox;
    private JComboBox<Integer> urgencyComboBox;
    private JSpinner startTimeSpinner, endTimeSpinner;
    private JButton createButton;
    private UserP user;
    private JSONArray usersArray;
    private TaskController taskController;

    public TaskTestGUI(UserP user) {
        this.user = user;
        this.taskController = new TaskController(new TaskService(new TaskDAO())); // Adjusted instantiation
        initializeComponents();
        setupUI();
        loadFamilyGroupMembersAsync(user.getFamilyGroupId()); // Load family group members asynchronously
    }

    private void initializeComponents() {
        mainPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // Grid layout for label and input pairs

        nameField = new JTextField();
        descriptionArea = new JTextArea(5, 20);
        urgencyComboBox = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5});
        repeatComboBox = new JComboBox<>(new String[]{"None", "Daily", "Weekly", "Monthly"});
        rewardField = new JTextField();
        bonusField = new JTextField();
        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        assigneeComboBox = new JComboBox<>();
        collaboratorComboBox = new JComboBox<>();
        createButton = new JButton("Create Task");

        // Configure the date spinners
        configureDateSpinner(startTimeSpinner);
        configureDateSpinner(endTimeSpinner);
    }

    private void setupUI() {
        mainPanel.add(new JLabel("Task Name:"));
        mainPanel.add(nameField);
        mainPanel.add(new JLabel("Task Description:"));
        mainPanel.add(new JScrollPane(descriptionArea));
        mainPanel.add(new JLabel("Urgency:"));
        mainPanel.add(urgencyComboBox);
        mainPanel.add(new JLabel("Repetition:"));
        mainPanel.add(repeatComboBox);
        mainPanel.add(new JLabel("Reward:"));
        mainPanel.add(rewardField);
        mainPanel.add(new JLabel("Max Bonus:"));
        mainPanel.add(bonusField);
        mainPanel.add(new JLabel("Start Time:"));
        mainPanel.add(startTimeSpinner);
        mainPanel.add(new JLabel("End Time:"));
        mainPanel.add(endTimeSpinner);
        mainPanel.add(new JLabel("Assignee:"));
        mainPanel.add(assigneeComboBox);
        mainPanel.add(new JLabel("Collaborator:"));
        mainPanel.add(collaboratorComboBox);
        mainPanel.add(createButton);

        createButton.addActionListener(this::createTaskAction);
    }

    private void configureDateSpinner(JSpinner spinner) {
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd HH:mm:ss");
        spinner.setEditor(editor);
        spinner.setValue(new Date()); // Set current date as default
    }

    private void loadFamilyGroupMembersAsync(String familyGroupId) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                loadFamilyGroupMembers(familyGroupId);
                return null;
            }

            @Override
            protected void done() {
                // Optional: Update the UI or perform any actions needed after loading members
                SwingUtilities.invokeLater(() -> {
                    assigneeComboBox.addItem("None");
                    collaboratorComboBox.addItem("None");
                    for (Object userObj : usersArray) {
                        JSONObject userJson = (JSONObject) userObj;
                        String userFamilyGroupId = (String) userJson.get("familyGroupId");
                        if (userFamilyGroupId.equals(familyGroupId)) {
                            String userName = (String) userJson.get("userName");
                            assigneeComboBox.addItem(userName);
                            collaboratorComboBox.addItem(userName);
                        }
                    }
                });
            }
        }.execute();
    }

    private void loadFamilyGroupMembers(String familyGroupId) {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader("users.json")) {
            usersArray = (JSONArray) parser.parse(reader);
        } catch (IOException | org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
    }

    private String findUserIdByName(String userName, String familyGroupId) {
        if ("None".equals(userName)) {
            return null;
        }
        for (Object userObj : usersArray) {
            JSONObject userJson = (JSONObject) userObj;
            String currentFamilyGroupId = (String) userJson.get("familyGroupId");
            String currentUserName = (String) userJson.get("userName");
            if (currentFamilyGroupId.equals(familyGroupId) && currentUserName.equals(userName)) {
                return (String) userJson.get("userId");
            }
        }
        return null; // Return null if not found
    }

    private LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private void createTaskAction(ActionEvent e) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                // Collect task data
                String name = nameField.getText();
                String description = descriptionArea.getText();
                int urgency = (int) urgencyComboBox.getSelectedItem();
                String repeat = (String) repeatComboBox.getSelectedItem();
                double reward = Double.parseDouble(rewardField.getText());
                double bonus = Double.parseDouble(bonusField.getText());
                String assigneeName = (String) assigneeComboBox.getSelectedItem();
                String collaboratorName = (String) collaboratorComboBox.getSelectedItem();
                String assigneeId = findUserIdByName(assigneeName, user.getFamilyGroupId());
                String collaboratorId = findUserIdByName(collaboratorName, user.getFamilyGroupId());
                LocalDateTime startTime = convertToLocalDateTimeViaInstant((Date) startTimeSpinner.getValue());
                LocalDateTime endTime = convertToLocalDateTimeViaInstant((Date) endTimeSpinner.getValue());

                // Validate dates
                if (!DateValidator.isLogicalDate(startTime, endTime)) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainPanel, "End time must be after start time!"));
                    return null;
                }

                // Create task data
                JSONObject taskData = new JSONObject();
                taskData.put("TaskID", UUID.randomUUID().toString());
                taskData.put("name", name);
                taskData.put("description", description);
                taskData.put("urgency", urgency);
                taskData.put("repeat", repeat);
                taskData.put("reward", reward);
                taskData.put("maxBonus", bonus);
                taskData.put("bonus", null);
                taskData.put("startTime", startTime.toString());
                taskData.put("endTime", endTime.toString());
                taskData.put("assignee", assigneeId);
                taskData.put("assigneeName", assigneeName);
                taskData.put("collaborator", collaboratorId);
                taskData.put("collaboratorName", collaboratorName);
                taskData.put("creatorName", user.getUserName());
                taskData.put("creator", user.getUserId());
                taskData.put("familyGroupID", user.getFamilyGroupId());
                taskData.put("actualStartTime", null);
                taskData.put("timedDuration", null);
                taskData.put("settlementType", "countByTime");

                // Call to create task
                taskController.createTask(taskData, user);

                // Notify user of success
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainPanel, "Task created successfully!"));
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    JOptionPane.showMessageDialog(mainPanel, cause.getMessage(), "Unable to create task", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
