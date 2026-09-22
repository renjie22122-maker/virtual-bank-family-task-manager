import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class AuthenticationMiddleware {

    /**
     * 验证用户的凭据
     *
     * @param userId   用户ID
     * @param password 用户密码
     * @return 如果用户的凭据有效，返回true；否则返回false。
     */
    public static boolean authenticate(String userId, String password) {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader("users.json")) {
            JSONArray usersArray = (JSONArray) parser.parse(reader);

            for (Object userObj : usersArray) {
                JSONObject userJson = (JSONObject) userObj;
                String storedUserId = (String) userJson.get("userId");
                String storedPassword = (String) userJson.get("password");

                if (storedUserId.equals(userId) && storedPassword.equals(password)) {
                    return true;
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 验证用户是否有权执行指定的操作
     *
     * @param userId    用户ID
     * @param userType  用户类型（"parent"或"child"）
     * @param operation 要执行的操作
     * @return 如果用户有权执行操作，返回true；否则返回false。
     */
    public static boolean authorize(String userId, String userType, String operation) {
        // 可以在这里定义更多的权限规则
        switch (operation) {
            case "createTask":
            case "deleteTask":
            case "modifyTask":
                return "parent".equals(userType);
            case "viewTaskList":
            case "updateTaskStatus":
                return true;
            default:
                return false;
        }
    }
}
