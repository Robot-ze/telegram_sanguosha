package sanguosha.user;

import org.telegram.telegrambots.meta.api.objects.User;

/**
 * 用户类
 */
public class SanUser {

    // 用户的全名
    public String full_name;
    // 用户的ID
    public long user_id;
    // 用户的用户名
    public String user_name;
    // 游戏ID
    private long gameId;

    /**
     * 获取游戏ID
     * @return 游戏ID
     */
    public long getGameId() {
        return gameId;
    }

    /**
     * 设置游戏ID
     * @param gameId 游戏ID
     */
    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    /**
     * 构造函数，初始化用户信息
     * @param full_name 用户的全名
     * @param user_id 用户的ID
     * @param user_name 用户的用户名
     */
    public SanUser(String full_name, long user_id, final String user_name) {
        this.full_name = full_name;
        // 如果全名长度超过20，截取前20个字符
        if (this.full_name.length() > 20) {
            this.full_name = full_name.substring(0, 20);
        }
   
        this.user_id = user_id;
        this.user_name = user_name;
    }

    /**
     * 生成用户全名
     * @param user Telegram用户对象
     * @return 用户全名
     */
    public static String genFullName(User user){
        String full_name = "";
        // 如果用户有名字，添加到全名
        if (user.getFirstName() != null) {
            full_name += user.getFirstName();
        }
        // 如果用户有姓氏，添加到全名
        if (user.getLastName() != null) {
            full_name += " " + user.getLastName();
        }
        // 如果全名长度超过20，截取前20个字符
        if (full_name.length() > 20) {
            full_name = full_name.substring(0, 20);
        }
  
        return full_name;
    }

    /**
     * 获取用户提及文本
     * @return 用户提及文本
     */
    public String getUserMentionText() {
        return "<i><a href=\"tg://user?id=" + user_id
                + "\">" + full_name + "</a></i>";
    }

    /**
     * 静态方法，获取用户提及文本
     * @param user Telegram用户对象
     * @return 用户提及文本
     */
    public static String getUserMentionText(User user) {
        String full_name = "";
        // 如果用户有名字，添加到全名
        if (user.getFirstName() != null) {
            full_name += user.getFirstName();
        }
        // 如果用户有姓氏，添加到全名
        if (user.getLastName() != null) {
            full_name += " " + user.getLastName();
        }
        // 如果全名长度超过20，截取前20个字符
        if (full_name.length() > 20) {
            full_name = full_name.substring(0, 20);
        }

        return "<i><a href=\"tg://user?id=" + user.getId()
                + "\">" + full_name + "</a></i>";
    }
}
