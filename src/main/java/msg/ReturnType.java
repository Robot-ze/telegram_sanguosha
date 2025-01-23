package msg;

public enum ReturnType {
    Msgid,
    Result,
    CallBackId,
    LastAnswer,
    /**
     * 在私聊页面中选一个
     */
    ChooseOneCard,
    /**
     * 在群聊页面中选一个
     */
    ChooseOneCardPublic,

    /**
     * 在群聊页面中选一个选项
     */
    ChooseOneOpinionPublic,
    ChooseMany,
    ChooseManyThisTime,
    //ImgFileId,
    Deeplink,
    DeeplinkMulty,
    DeeplinkMultyCallIdSet,
    UpdateStatus,
    //-----------------------
    CurrentTime,
    JoinTimeLimit,
    JoinLatch,
    MainMsg,

    //-----wuxie---------
    /**
     * 用来中止一些无懈技能的
     */
    wuxieStop,
    doneAskCardPublic,
 
}
