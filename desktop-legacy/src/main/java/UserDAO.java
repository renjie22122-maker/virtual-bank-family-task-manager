import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * UserDAO handles the persistence of user data.
 */
public class UserDAO {
    private static final String USERS_FILE_PATH = "users.json";

    /**
     * Reads all users from the JSON file.
     *
     * @return A JSONArray containing all users.
     */
    public JSONArray readAllUsers() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(USERS_FILE_PATH)) {
            return (JSONArray) parser.parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return new JSONArray(); // Return empty array if file does not exist or parse error
        }
    }

    /**
     * Writes all users to the JSON file.
     *
     * @param users A JSONArray containing all users to write.
     */
    public void writeAllUsers(JSONArray users) {
        try (FileWriter file = new FileWriter(USERS_FILE_PATH)) {
            file.write(users.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds a user by their ID.
     *
     * @param userId The ID of the user to find.
     * @return The JSONObject representing the user if found, null otherwise.
     */
    public JSONObject findUserById(String userId) {
        JSONArray users = readAllUsers();
        for (Object userObj : users) {
            JSONObject user = (JSONObject) userObj;
            String currentUserId = (String) user.get("userId");
            if (currentUserId != null && currentUserId.equals(userId)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Adds a new user to the JSON file.
     *
     * @param newUser The JSONObject representing the new user.
     */
    public void addUser(JSONObject newUser) {
        JSONArray users = readAllUsers();
        users.add(newUser);
        writeAllUsers(users);
    }

    /**
     * Updates a user in the JSON file.
     *
     * @param userId   The ID of the user to update.
     * @param updatedUser The updated JSONObject representing the user.
     */
    public void updateUser(String userId, JSONObject updatedUser) {
        JSONArray users = readAllUsers();
        for (int i = 0; i < users.size(); i++) {
            JSONObject user = (JSONObject) users.get(i);
            String currentUserId = (String) user.get("userId");
            if (currentUserId != null && currentUserId.equals(userId)) {
                users.set(i, updatedUser);
                writeAllUsers(users);
                return;
            }
        }
        throw new IllegalArgumentException("User with ID " + userId + " not found.");
    }

    /**
     * Deletes a user from the JSON file.
     *
     * @param userId The ID of the user to delete.
     */
    public void deleteUser(String userId) {
        JSONArray users = readAllUsers();
        boolean isRemoved = users.removeIf(userObj -> {
            JSONObject user = (JSONObject) userObj;
            String currentUserId = (String) user.get("userId");
            return currentUserId != null && currentUserId.equals(userId);
        });
        if (isRemoved) {
            writeAllUsers(users);
        } else {
            throw new IllegalArgumentException("User with ID " + userId + " not found.");
        }
    }
}
