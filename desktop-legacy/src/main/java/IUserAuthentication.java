public interface IUserAuthentication {
    /**
     * 用户登录验证
     * @param userName 用户名
     * @param password 密码
     * @return 登录成功返回对应的UserP对象，失败返回null
     */
    UserP login(String userName, String password);

    /**
     * 用户注册
     * @param user 用户对象，包含注册信息
     * @return 注册成功返回true，失败返回false
     */
    boolean register(UserP user);

    /**
     * 检查用户是否有权限执行某些操作
     * @param user 用户对象
     * @param requiredRole 所需的用户角色（例如："parent", "child"）
     * @return 如果用户具有所需权限，返回true；否则返回false
     */
    boolean hasPermission(UserP user, String requiredRole);

    /**
     * 更新用户信息
     * @param user 用户对象，包含更新后的信息
     * @return 更新成功返回true，失败返回false
     */
    boolean updateUser(UserP user);

    /**
     * 注销用户
     * @param userName 用户名
     * @return 注销成功返回true，失败返回false
     */
    boolean logout(String userName);
}
