package com.familyflow.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.ArrayList;
import java.util.List;

public final class Models {
    private Models() {}

    public static class User {
        public String userId;
        public String userName;
        public String password;
        public String userType;
        public String familyGroupId;
        public List<String> accountIds = new ArrayList<>();
    }

    public static class StoredUser extends User {
        @Override public String toString() { return userName; }
    }

    public static class Account {
        public String accountId;
        public String ownerId;
        public String password;
        public double balance;
        public double interestRate;
        public String accountType;
    }

    public static class Task {
        @JsonAlias("TaskID") public String taskId;
        public String name;
        public String description;
        public int urgency;
        public String repeat;
        public double reward;
        public double maxBonus;
        public Double bonus;
        public String startTime;
        public String endTime;
        public String assignee;
        public String assigneeName;
        public String collaborator;
        public String collaboratorName;
        public String creator;
        public String creatorName;
        @JsonAlias("familyGroupID") public String familyGroupId;
        public String status;
        public String settlementType;
        public String actualStartTime;
        public String timedDuration;
    }

    public static class Transaction {
        public String transactionId;
        public String type;
        public String timestamp;
        public String fromAccountId;
        public String toAccountId;
        public double amount;
        public String remark;
        public String fromOwnerId;
        public String toOwnerId;
    }
}
