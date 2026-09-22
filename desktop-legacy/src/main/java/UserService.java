import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * UserService handles all business logic related to user management.
 */
public class UserService {
    private UserDAO userDao;

    public UserService(UserDAO userDao) {
        this.userDao = userDao;
    }

    /**
     * Authenticates a user based on username and password.
     */
    public UserP authenticateUser(String userName, String password) {
        JSONArray users = userDao.readAllUsers();
        for (Object userObj : users) {
            JSONObject userJson = (JSONObject) userObj;
            if (userJson.get("userName").equals(userName) && userJson.get("password").equals(password)) {
                return jsonToUser(userJson);
            }
        }
        throw new SecurityException("Invalid username or password.");
    }

    /**
     * Updates user information.
     */
    public void updateUser(UserP user) {
        JSONArray users = userDao.readAllUsers();
        for (int i = 0; i < users.size(); i++) {
            JSONObject userJson = (JSONObject) users.get(i);
            if (userJson.get("userId").equals(user.getUserId())) {
                users.set(i, userToJson(user));
                userDao.writeAllUsers(users);
                return;
            }
        }
        throw new IllegalArgumentException("User with ID " + user.getUserId() + " not found.");
    }

    /**
     * Converts a JSONObject to a UserP object.
     */
    private UserP jsonToUser(JSONObject userJson) {
        UserP user = new UserP();
        user.setUserId((String) userJson.get("userId"));
        user.setUserName((String) userJson.get("userName"));
        user.setUserType((String) userJson.get("userType"));
        user.setPassword((String) userJson.get("password"));
        user.setFamilyGroupId((String) userJson.get("familyGroupId"));
        return user;
    }

    /**
     * Converts a UserP object to a JSONObject.
     */
    private JSONObject userToJson(UserP user) {
        JSONObject userJson = new JSONObject();
        userJson.put("userId", user.getUserId());
        userJson.put("userName", user.getUserName());
        userJson.put("userType", user.getUserType());
        userJson.put("password", user.getPassword());
        userJson.put("familyGroupId", user.getFamilyGroupId());
        return userJson;
    }
}
