package com.familyflow.service;

import com.familyflow.api.ApiDtos.*;
import com.familyflow.domain.Models.StoredUser;
import com.familyflow.domain.Models.Task;
import com.familyflow.infrastructure.JsonStore;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TaskService {
    private static final Set<String> STATUSES = Set.of("WaitforCheck", "Reject", "ToDo", "Doing", "WaitforConfirm", "Done", "OverDue");
    private final JsonStore store;
    private final AuthService auth;
    private final AccountService accounts;

    public TaskService(JsonStore store, AuthService auth, AccountService accounts) {
        this.store = store; this.auth = auth; this.accounts = accounts;
    }

    public List<Task> list(StoredUser user, String status, String sort) {
        List<Task> tasks = loadNormalized(); markOverdue(tasks);
        Comparator<Task> comparator = switch (sort == null ? "deadline" : sort) {
            case "reward" -> Comparator.comparingDouble((Task task) -> task.reward).reversed();
            case "urgency" -> Comparator.comparingInt((Task task) -> task.urgency).reversed();
            default -> Comparator.comparing(task -> parse(task.endTime));
        };
        return tasks.stream().filter(task -> user.familyGroupId.equals(task.familyGroupId))
                .filter(task -> status == null || status.isBlank() || "All".equals(status) || status.equals(task.status))
                .sorted(comparator).toList();
    }

    public Task get(StoredUser user, String taskId) {
        Task task = find(loadNormalized(), taskId);
        requireFamily(user, task); return task;
    }

    public Task create(StoredUser user, CreateTaskRequest request) {
        LocalDateTime start = parse(request.startTime()); LocalDateTime end = parse(request.endTime());
        if (!start.isBefore(end)) throw new IllegalArgumentException("End time must be after start time.");
        Map<String, UserView> family = new HashMap<>();
        auth.familyMembers(user).forEach(member -> family.put(member.userId(), member));
        UserView assignee = member(family, request.assigneeId()); UserView collaborator = member(family, request.collaboratorId());
        Task task = new Task(); task.taskId = UUID.randomUUID().toString(); task.name = request.name().trim();
        task.description = request.description() == null ? "" : request.description().trim(); task.urgency = request.urgency();
        task.repeat = request.repeat(); task.reward = request.reward(); task.maxBonus = request.maxBonus(); task.bonus = 0.0;
        task.startTime = start.toString(); task.endTime = end.toString();
        task.assignee = assignee == null ? null : assignee.userId(); task.assigneeName = assignee == null ? "Unassigned" : assignee.userName();
        task.collaborator = collaborator == null ? null : collaborator.userId();
        task.collaboratorName = collaborator == null ? "None" : collaborator.userName();
        task.creator = user.userId; task.creatorName = user.userName; task.familyGroupId = user.familyGroupId;
        task.status = "parent".equals(user.userType) ? "ToDo" : "WaitforCheck"; task.settlementType = "fixed";
        List<Task> tasks = loadNormalized(); tasks.add(task); store.write("tasks.json", tasks); return task;
    }

    public Task update(StoredUser user, String id, UpdateTaskRequest request) {
        requireParent(user); List<Task> tasks = loadNormalized(); Task task = find(tasks, id); requireFamily(user, task);
        if (Set.of("Doing", "Done", "OverDue").contains(task.status)) throw new IllegalStateException("This task can no longer be edited.");
        LocalDateTime start = parse(request.startTime()); LocalDateTime end = parse(request.endTime());
        if (!start.isBefore(end)) throw new IllegalArgumentException("End time must be after start time.");
        task.name = request.name().trim(); task.description = request.description() == null ? "" : request.description().trim();
        task.urgency = request.urgency(); task.repeat = request.repeat(); task.reward = request.reward();
        task.maxBonus = request.maxBonus(); task.startTime = start.toString(); task.endTime = end.toString();
        store.write("tasks.json", tasks); return task;
    }

    public Task changeStatus(StoredUser user, String id, String target) {
        if (!STATUSES.contains(target)) throw new IllegalArgumentException("Unknown task status.");
        List<Task> tasks = loadNormalized(); Task task = find(tasks, id); requireFamily(user, task);
        boolean participant = user.userId.equals(task.assignee) || user.userId.equals(task.collaborator);
        boolean parent = "parent".equals(user.userType);
        boolean allowed = switch (target) {
            case "ToDo", "Reject" -> parent && "WaitforCheck".equals(task.status);
            case "Doing" -> participant && "ToDo".equals(task.status);
            case "WaitforConfirm" -> participant && "Doing".equals(task.status);
            default -> false;
        };
        if (!allowed) throw new SecurityException("That status transition is not allowed.");
        task.status = target; store.write("tasks.json", tasks); return task;
    }

    public Task confirm(StoredUser user, String id, ConfirmTaskRequest request) {
        requireParent(user); List<Task> tasks = loadNormalized(); Task task = find(tasks, id); requireFamily(user, task);
        if (!"WaitforConfirm".equals(task.status)) throw new IllegalStateException("Task is not awaiting confirmation.");
        if (request.bonus() > task.maxBonus) throw new IllegalArgumentException("Bonus exceeds the task maximum.");
        task.bonus = request.bonus();
        if (task.assignee == null && task.reward + request.bonus() > 0) throw new IllegalStateException("Assign the task before paying a reward.");
        if (task.assignee != null) accounts.payReward(user, task.assignee, task.reward + request.bonus());
        task.status = "Done";
        if (!"None".equals(task.repeat)) tasks.add(nextOccurrence(task));
        store.write("tasks.json", tasks); return task;
    }

    public void delete(StoredUser user, String id) {
        requireParent(user); List<Task> tasks = loadNormalized(); Task task = find(tasks, id); requireFamily(user, task);
        if (Set.of("Doing", "Done").contains(task.status)) throw new IllegalStateException("Active or completed tasks cannot be deleted.");
        tasks.remove(task); store.write("tasks.json", tasks);
    }

    private Task nextOccurrence(Task source) {
        LocalDateTime start = parse(source.startTime); Duration duration = Duration.between(start, parse(source.endTime));
        LocalDateTime next = switch (source.repeat) {
            case "Daily" -> start.plusDays(1); case "Weekly" -> start.plusWeeks(1); case "Monthly" -> start.plusMonths(1);
            default -> throw new IllegalArgumentException("Unsupported repeat option.");
        };
        Task task = new Task(); task.taskId = UUID.randomUUID().toString(); task.name = source.name; task.description = source.description;
        task.urgency = source.urgency; task.repeat = source.repeat; task.reward = source.reward; task.maxBonus = source.maxBonus; task.bonus = 0.0;
        task.startTime = next.toString(); task.endTime = next.plus(duration).toString(); task.assignee = source.assignee;
        task.assigneeName = source.assigneeName; task.collaborator = source.collaborator; task.collaboratorName = source.collaboratorName;
        task.creator = source.creator; task.creatorName = source.creatorName; task.familyGroupId = source.familyGroupId;
        task.status = "ToDo"; task.settlementType = source.settlementType; return task;
    }

    private List<Task> loadNormalized() {
        List<Task> tasks = store.read("tasks.json", Task.class); Set<String> ids = new HashSet<>(); boolean changed = false;
        for (Task task : tasks) {
            if (task.taskId == null || !ids.add(task.taskId)) { task.taskId = UUID.randomUUID().toString(); ids.add(task.taskId); changed = true; }
            if (task.bonus == null) task.bonus = 0.0;
        }
        if (changed) store.write("tasks.json", tasks); return tasks;
    }
    private void markOverdue(List<Task> tasks) {
        boolean changed = false; LocalDateTime now = LocalDateTime.now();
        for (Task task : tasks) if (("ToDo".equals(task.status) || "Doing".equals(task.status)) && now.isAfter(parse(task.endTime))) {
            task.status = "OverDue"; changed = true;
        }
        if (changed) store.write("tasks.json", tasks);
    }
    private UserView member(Map<String, UserView> family, String id) {
        if (id == null || id.isBlank()) return null;
        UserView member = family.get(id); if (member == null) throw new IllegalArgumentException("Selected family member was not found."); return member;
    }
    private Task find(List<Task> tasks, String id) { return tasks.stream().filter(task -> id.equals(task.taskId)).findFirst().orElseThrow(() -> new IllegalArgumentException("Task was not found.")); }
    private void requireFamily(StoredUser user, Task task) { if (!user.familyGroupId.equals(task.familyGroupId)) throw new SecurityException("Task belongs to another family."); }
    private void requireParent(StoredUser user) { if (!"parent".equals(user.userType)) throw new SecurityException("Parent access required."); }
    private LocalDateTime parse(String value) { try { return LocalDateTime.parse(value); } catch (RuntimeException error) { throw new IllegalArgumentException("Invalid date and time."); } }
}
