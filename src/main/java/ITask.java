import org.json.simple.JSONObject;

import java.util.UUID;

public interface ITask {
    /**
     * 创建一个新任务
     * @param taskData 包含任务详细信息的JSONObject
     * @param user 创建任务的用户
     */
    void createTask(JSONObject taskData, UserP user);

    /**
     * 更新任务状态
     * @param taskId 要更新的任务ID
     * @param status 新的任务状态
     * @param user 更新任务状态的用户
     */
    void updateTaskStatus(UUID taskId, String status, UserP user);

    /**
     * 查看任务列表
     * @param status 任务的状态
     * @param user 查看任务的用户
     * @param sortOption 排序选项
     * @return 包含任务列表的JSONObject
     */
    JSONObject viewTaskList(String status, UserP user, String sortOption);

    /**
     * 修改任务
     * @param taskId 要修改的任务ID
     * @param taskData 包含任务新详细信息的JSONObject
     * @param user 修改任务的用户
     */
    void modifyTask(UUID taskId, JSONObject taskData, UserP user);

    /**
     * 删除任务
     * @param taskId 要删除的任务ID
     * @param user 删除任务的用户
     */
    void deleteTask(UUID taskId, UserP user);
}
