import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UserController implements IUserAuthentication {
    private JSONParser parser;
    private JSONArray users;
    private Set<String> loggedInUsers;

    public UserController() {
        this.parser = new JSONParser();
        this.loggedInUsers = new HashSet<>();
        loadUsers();
    }

    /**
     * Loads users from a JSON file.
     */
    private void loadUsers() {
        try (FileReader reader = new FileReader("users.json")) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONArray) {
                users = (JSONArray) obj;
            } else {
                users = new JSONArray();
                users.add(obj);
            }
        } catch (IOException | ParseException e) {
            users = new JSONArray();
        }
    }

    /**
     * Attempts to log in a user with the provided username and password.
     *
     * @param userName The username of the user attempting to log in
     * @param password The password provided by the user
     * @return A UserP object if credentials are valid; otherwise, null.
     */
    @Override
    public UserP login(String userName, String password) {
        for (Object userObj : users) {
            JSONObject userJson = (JSONObject) userObj;
            String storedUserName = (String) userJson.get("userName");
            String storedPassword = (String) userJson.get("password");
            if (storedUserName != null && storedPassword != null
                    && storedUserName.equals(userName) && storedPassword.equals(password)) {
                UserP user = constructUser(userJson);
                loggedInUsers.add(user.getUserId());
                return user;
            }
        }
        return null;
    }

    public UserP loginWithUserId(String userId, String password) {
        for (Object userObj : users) {
            JSONObject userJson = (JSONObject) userObj;
            String storedUserId = (String) userJson.get("userId");
            String storedPassword = (String) userJson.get("password");
            if (storedUserId != null && storedPassword != null
                    && storedUserId.equals(userId) && storedPassword.equals(password)) {
                UserP user = constructUser(userJson);
                loggedInUsers.add(user.getUserId());
                return user;
            }
        }
        return null;
    }

    public List<String> getUserIdsByUsername(String userName) {
        List<String> userIds = new ArrayList<>();
        for (Object userObj : users) {
            JSONObject userJson = (JSONObject) userObj;
            String storedUserName = (String) userJson.get("userName");
            if (storedUserName != null && storedUserName.equals(userName)) {
                userIds.add((String) userJson.get("userId"));
            }
        }
        return userIds;
    }

    /**
     * Registers a new user.
     *
     * @param newUser The UserP object containing the new user's data
     * @return true if registration is successful, false if the username is already taken
     */
    @Override
    public boolean register(UserP newUser) {
        if (newUser == null) {
            throw new IllegalArgumentException("User is required.");
        }
        String userName = newUser.getUserName() == null ? "" : newUser.getUserName().trim();
        String password = newUser.getPassword() == null ? "" : newUser.getPassword();
        String userType = newUser.getUserType();
        String familyGroupId = newUser.getFamilyGroupId() == null ? "" : newUser.getFamilyGroupId().trim();
        if (userName.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters.");
        }
        if (!"parent".equals(userType) && !"child".equals(userType)) {
            throw new IllegalArgumentException("User type must be parent or child.");
        }
        if (familyGroupId.isEmpty()) {
            throw new IllegalArgumentException("Family group ID cannot be empty.");
        }
        if (isUserNameTaken(userName)) {
            return false;
        }
        newUser.setUserName(userName);
        newUser.setFamilyGroupId(familyGroupId);
        String userId = UUID.randomUUID().toString();
        newUser.setUserId(userId); // Ensure UserP object has the correct userId
        JSONObject newUserJson = new JSONObject();
        newUserJson.put("userId", userId);
        newUserJson.put("userName", newUser.getUserName());
        newUserJson.put("userType", newUser.getUserType());
        newUserJson.put("password", newUser.getPassword());
        newUserJson.put("familyGroupId", newUser.getFamilyGroupId());
        newUserJson.put("accountIds", newUser.getAccountIds());
        users.add(newUserJson);
        saveUsers();
        return true;
    }

    /**
     * Checks if the username is already used.
     *
     * @param userName The username to check
     * @return true if the username is already taken, otherwise false
     */
    private boolean isUserNameTaken(String userName) {
        for (Object userObj : users) {
            JSONObject userJson = (JSONObject) userObj;
            String storedUserName = (String) userJson.get("userName");
            if (storedUserName != null && storedUserName.equals(userName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saves the current users to the JSON file.
     */
    private void saveUsers() {
        try (FileWriter file = new FileWriter("users.json")) {
            file.write(users.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs a UserP object from a JSONObject.
     *
     * @param userJson The JSONObject containing user data
     * @return A UserP object
     */
    private UserP constructUser(JSONObject userJson) {
        UserP user = new UserP();
        user.setUserId((String) userJson.get("userId"));
        user.setUserName((String) userJson.get("userName"));
        user.setUserType((String) userJson.get("userType"));
        user.setPassword((String) userJson.get("password"));
        user.setFamilyGroupId((String) userJson.get("familyGroupId"));

        Object accountIds = userJson.get("accountIds");
        if (accountIds instanceof JSONArray) {
            for (Object accountId : (JSONArray) accountIds) {
                user.addAccount((String) accountId);
            }
        }

        return user;
    }

    /**
     * Checks if a user has the required role.
     *
     * @param user         The user to check
     * @param requiredRole The required role
     * @return true if the user has the required role, otherwise false
     */
    @Override
    public boolean hasPermission(UserP user, String requiredRole) {
        return requiredRole.equals(user.getUserType());
    }

    /**
     * Updates user information.
     *
     * @param user The user object containing updated information
     * @return true if the update is successful, otherwise false
     */
    @Override
    public boolean updateUser(UserP user) {
        for (int i = 0; i < users.size(); i++) {
            JSONObject userJson = (JSONObject) users.get(i);
            if (userJson.get("userId").equals(user.getUserId())) {
                users.set(i, userToJson(user));
                saveUsers();
                return true;
            }
        }
        return false;
    }

    /**
     * Logs out the user.
     *
     * @param userName The username of the user to log out
     * @return true if the logout is successful, otherwise false
     */
    @Override
    public boolean logout(String userName) {
        for (Object userObj : users) {
            JSONObject userJson = (JSONObject) userObj;
            String storedUserName = (String) userJson.get("userName");
            if (storedUserName != null && storedUserName.equals(userName)) {
                String userId = (String) userJson.get("userId");
                if (loggedInUsers.contains(userId)) {
                    loggedInUsers.remove(userId); // 从已登录用户集合中移除
                    logLogoutEvent(userId);
                    return true;
                }
            }
        }
        return false;
    }

    private void logLogoutEvent(String userId) {
        try (FileWriter fw = new FileWriter("logout_log.txt", true)) {
            fw.write("User ID: " + userId + " logged out at " + new Date() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts a UserP object to a JSONObject.
     *
     * @param user The UserP object to convert
     * @return The JSONObject representing the user
     */
    private JSONObject userToJson(UserP user) {
        JSONObject userJson = new JSONObject();
        userJson.put("userId", user.getUserId());
        userJson.put("userName", user.getUserName());
        userJson.put("userType", user.getUserType());
        userJson.put("password", user.getPassword());
        userJson.put("familyGroupId", user.getFamilyGroupId());

        JSONArray accountIdsArray = new JSONArray();
        for (String accountId : user.getAccountIds()) {
            accountIdsArray.add(accountId);
        }
        userJson.put("accountIds", accountIdsArray);

        return userJson;
    }
}
