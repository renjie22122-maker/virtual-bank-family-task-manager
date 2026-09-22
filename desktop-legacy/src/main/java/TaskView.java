import java.util.UUID;

/** Read-only task data prepared for the presentation layer. */
public record TaskView(
        UUID id, String name, String description, int urgency, String repeat,
        double reward, double maxBonus, double bonus, String startTime, String endTime,
        String assigneeId, String assigneeName, String collaboratorName, String creatorName,
        String status, String settlementType, String actualStartTime, String timedDuration) {
}
