import java.util.ArrayList;
import java.util.List;

public class UserP {
    private String userId;          // Unique identifier for the user
    private String userName;        // User's name
    private String userType;        // Type of user, e.g., "parent" or "child"
    private String familyGroupId;   // Identifier of the family group to which the user belongs
    private String password;        // User's password for authentication purposes
    private List<String> accountIds; // List of account IDs associated with the user

    // Constructor
    public UserP() {
        this.accountIds = new ArrayList<>();
    }

    // Getter and Setter for userId
    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getter and Setter for userName
    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    // Getter and Setter for userType
    public String getUserType() {
        return this.userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    // Getter and Setter for password
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Getter and Setter for familyGroupId
    public String getFamilyGroupId() {
        return this.familyGroupId;
    }

    public void setFamilyGroupId(String familyGroupId) {
        this.familyGroupId = familyGroupId;
    }

    // Getter and Setter for accountIds
    public List<String> getAccountIds() {
        return this.accountIds;
    }

    public void addAccount(String accountId) {
        this.accountIds.add(accountId);
    }

    public void removeAccount(String accountId) {
        this.accountIds.remove(accountId);
    }
}
