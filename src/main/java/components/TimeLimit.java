package components;



/**
 * 这里要实现一个判断超时挂机的操作
 */
public class TimeLimit {
    private long startTime;
    private long timeout;

    /**
     * 
     * @param timeout 单位是毫秒
     */
    public TimeLimit(long timeout) {
        this.timeout = timeout;
        this.startTime = System.currentTimeMillis();
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() - startTime > timeout;
    }

    public boolean isNotTimeout() {
        return System.currentTimeMillis() - startTime <= timeout;
    }
}
