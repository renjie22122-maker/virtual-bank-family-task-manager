public class AccountFactory {
    /**
     * Creates a new account based on the provided type.
     *
     * @param accountType   the type of the account, "saving" or "fixedTerm"
     * @param userId        the user ID associated with the account
     * @param accountPassword the password for the new account
     * @param termInMonths  (optional) the term in months for a fixed term account, ignored for saving accounts
     * @return              the created Account object
     */
    public static Account createAccount(String accountType, String userId, String accountPassword, Integer... termInMonths) {
        switch (accountType) {
            case "saving":
                return new SavingAccount(userId, accountPassword);
            case "fixedTerm":
                if (termInMonths.length > 0 && termInMonths[0] != null) {
                    return new FixedTermAccount(userId, termInMonths[0], accountPassword);
                } else {
                    throw new IllegalArgumentException("Term in months must be provided for fixed term accounts.");
                }
            default:
                throw new IllegalArgumentException("Invalid account type: " + accountType);
        }
    }
}
