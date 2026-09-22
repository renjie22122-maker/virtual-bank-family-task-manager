import javax.swing.*;
import java.time.LocalDateTime;

/**
 * DateValidator is a utility class that provides methods for date validation.
 */
public class DateValidator {

    /**
     * Checks if the given startTime is before the endTime.
     *
     * @param startTime The start time to check.
     * @param endTime   The end time to check.
     * @return True if startTime is before endTime, false otherwise.
     */
    public static boolean isLogicalDate(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            JOptionPane.showMessageDialog(null, "开始时间必须早于结束时间。");
            return false;
        }
        return true;
    }
}
