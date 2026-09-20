public interface ITransaction {
    /**
     * 获取交易ID
     *
     * @return 交易ID
     */
    String getTransactionId();

    /**
     * 获取交易类型
     *
     * @return 交易类型
     */
    String getTransactionType();

    /**
     * 获取交易时间戳
     *
     * @return 交易时间戳
     */
    String getTimestamp();

    /**
     * 获取源账户ID
     *
     * @return 源账户ID
     */
    String getFromAccountId();

    /**
     * 获取目标账户ID
     *
     * @return 目标账户ID
     */
    String getToAccountId();

    /**
     * 获取交易金额
     *
     * @return 交易金额
     */
    double getAmount();

    /**
     * 获取交易备注
     *
     * @return 交易备注
     */
    String getRemark();

    /**
     * 获取源账户所有者ID
     *
     * @return 源账户所有者ID
     */
    String getFromOwnerId();

    /**
     * 获取目标账户所有者ID
     *
     * @return 目标账户所有者ID
     */
    String getToOwnerId();
}
