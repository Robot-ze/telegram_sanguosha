package components;

import msg.CallbackEven;
import msg.MsgAPI;
import msg.MsgObj;

public class SanProgress {
    private final static String W = "▒";
    private final static String B = "░";
    private final static int DELTA = 5;
    private int BLOCK = 20;
    private MsgAPI msgApi;
    private MsgObj progressMsg;
    private LatchManager lt;
    private float totalSec;
    private float currentSec;
    private long chatId;
    private String preShowString;
    private CallbackEven timeOutEven;
    /*
     * 中止
     */
    private boolean abort;

    private SanProgress() {
    }

    public static SanProgress newInst(MsgAPI msgApi, LatchManager lt, long chatId, int totalSec) {
        SanProgress s = new SanProgress();
        s.msgApi = msgApi;
        s.lt = lt;
        s.chatId = chatId;
        s.totalSec = totalSec;
        return s;
    }

    /*
     * 中止
     */
    public void abort() {
        abort = true;
    }

    public boolean isAborted() {
        return abort;
    }

    /**
     * 开始计时
     * 
     * @param timeOutEven 超时回调
     */
    public void start(CallbackEven timeOutEven1) {
        this.timeOutEven = timeOutEven1;
        abort = false;
        progressMsg = MsgObj.newMsgObj(lt);
        progressMsg.chatId = chatId;
        currentSec = totalSec;
        String showString = buildString();
        preShowString = showString;
        progressMsg.text = "<b>" + showString + "</b>";
        msgApi.sendMsg(progressMsg);
        msgApi.submitRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    int sencond = 0;
                    while (!abort && currentSec > 0) {
                        sleep(1000L);// 这里不一定是1秒
                        if ((timeOutEven != null && timeOutEven.progressStopCount())) {
                            continue;// 如果是暂停，则循环
                        }
                        currentSec -= 1f;
                        if (sencond < DELTA) {
                            sencond++;
                            continue;
                        } else {
                            sencond = 0;
                        }

                        String showString = buildString();
                        //System.out.println("currentSec=" + currentSec);
                        if (showString.equals(preShowString)) {
                            continue;
                        }
                        preShowString = showString;
                        progressMsg.text = "<b>" + showString + "</b>";
                        msgApi.editMsg(progressMsg);
                    }
                    if (!abort && timeOutEven != null) {
                        timeOutEven.progressTimeOut();
                    }
                    abort = true;
                    sleep(1000L);//
                    msgApi.delMsg(progressMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    /**
     * 重新设置一下回调控制
     * @param timeOutEven1
     */
    public void setContrlCallBack(CallbackEven timeOutEven1) {
        this.timeOutEven = timeOutEven1;
    }

    private String buildString() {
        // 加粗就看得现眼一点
        StringBuilder sb = new StringBuilder();
        currentSec = Math.min(currentSec, totalSec);
        float progress_percentage = currentSec / totalSec * 100f;
        int filled_blocks = (int) (progress_percentage / (100f / BLOCK));
        int empty_blocks = BLOCK - filled_blocks;

        // System.out.println("filled_blocks=" + filled_blocks);
        // System.out.println("empty_blocks=" + empty_blocks);
        for (int i = 0; i < filled_blocks; i++) {
            sb.append(W);
        }
        for (int i = 0; i < empty_blocks; i++) {
            sb.append(B);
        }
        return sb.toString();
    }

    public void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
