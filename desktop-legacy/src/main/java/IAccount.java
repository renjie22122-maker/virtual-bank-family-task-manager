public interface IAccount {
    /**
     * 获取账户ID
     *
     * @return 账户ID
     */
    String getAccountId();

    /**
     * 获取账户类型
     *
     * @return 账户类型
     */
    String getAccountType();

    /**
     * 获取账户余额
     *
     * @return 账户余额
     */
    double getBalance();

    /**
     * 获取账户利率
     *
     * @return 账户利率
     */
    double getInterestRate();

    /**
     * 存款操作
     *
     * @param amount 存款金额
     */
    void deposit(double amount);

    /**
     * 取款操作
     *
     * @param amount 取款金额
     * @throws InsufficientFundsException 余额不足时抛出异常
     */
    void withdraw(double amount) throws InsufficientFundsException;

    /**
     * 转账操作
     *
     * @param targetAccountId 目标账户ID
     * @param amount          转账金额
     * @throws InsufficientFundsException 余额不足时抛出异常
     */
    void transfer(String targetAccountId, double amount) throws InsufficientFundsException;

    /**
     * 验证账户密码
     *
     * @param password 账户密码
     * @return 如果密码正确，返回true；否则返回false
     */
    boolean authenticate(String password);

    /**
     * 获取账户所有者ID
     *
     * @return 账户所有者ID
     */
    String getOwnerId();

    /**
     * Returns the credential used to persist this account. The application is a
     * local teaching project, so credentials are kept in its local JSON store.
     */
    String getPassword();
}
