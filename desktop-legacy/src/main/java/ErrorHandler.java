import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorHandler {

    private static final String LOG_FILE = "error_log.txt";

    /**
     * 处理异常并显示错误消息
     *
     * @param e 捕获的异常
     */
    public static void handle(Exception e) {
        logError(e);
        showErrorDialog(e);
    }

    /**
     * 将错误日志记录到文件
     *
     * @param e 捕获的异常
     */
    private static void logError(Exception e) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            pw.println(now.format(formatter));
            pw.println(getStackTrace(e));
            pw.println();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * 显示错误对话框
     *
     * @param e 捕获的异常
     */
    private static void showErrorDialog(Exception e) {
        JOptionPane.showMessageDialog(null,
                "An error occurred: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 获取异常的堆栈跟踪信息
     *
     * @param e 捕获的异常
     * @return 异常的堆栈跟踪信息字符串
     */
    private static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 捕获所有未处理的异常并进行处理
     */
    public static void setDefaultExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> handle(new Exception(e)));
    }
}
