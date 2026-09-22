package com.familyflow.api;

import jakarta.validation.constraints.*;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}

    public record UserView(String userId, String userName, String userType, String familyGroupId) {}
    public record AuthResponse(String token, UserView user) {}
    public record LoginRequest(@NotBlank String userName, @NotBlank String password) {}
    public record RegisterRequest(@NotBlank @Size(min = 2, max = 40) String userName,
                                  @NotBlank @Size(min = 6, max = 72) String password,
                                  @Pattern(regexp = "parent|child") String userType,
                                  String familyGroupId) {}
    public record DashboardResponse(UserView user, int accountCount, double totalBalance,
                                    long openTasks, long completedTasks, List<UserView> familyMembers) {}
    public record CreateAccountRequest(@Pattern(regexp = "Checking|FixedDeposit") String accountType,
                                       @NotBlank @Size(min = 4, max = 72) String password,
                                       @DecimalMin("0.0") @DecimalMax("1.0") double interestRate) {}
    public record AmountRequest(@DecimalMin(value = "0.0", inclusive = false) double amount, String password) {}
    public record TransferRequest(@NotBlank String targetAccountId,
                                  @DecimalMin(value = "0.0", inclusive = false) double amount,
                                  @NotBlank String password) {}
    public record CreateTaskRequest(@NotBlank @Size(max = 100) String name, @Size(max = 1000) String description,
                                    @Min(0) @Max(5) int urgency,
                                    @Pattern(regexp = "None|Daily|Weekly|Monthly") String repeat,
                                    @PositiveOrZero double reward, @PositiveOrZero double maxBonus,
                                    @NotBlank String startTime, @NotBlank String endTime,
                                    String assigneeId, String collaboratorId) {}
    public record UpdateTaskRequest(@NotBlank @Size(max = 100) String name, @Size(max = 1000) String description,
                                    @Min(0) @Max(5) int urgency,
                                    @Pattern(regexp = "None|Daily|Weekly|Monthly") String repeat,
                                    @PositiveOrZero double reward, @PositiveOrZero double maxBonus,
                                    @NotBlank String startTime, @NotBlank String endTime) {}
    public record StatusRequest(@NotBlank String status) {}
    public record ConfirmTaskRequest(@PositiveOrZero double bonus) {}
}
