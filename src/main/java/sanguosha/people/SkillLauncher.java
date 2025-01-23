package sanguosha.people;

import sanguosha.cards.Card;
import sanguosha.cards.basic.Sha;
import sanguosha.manager.GameManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import msg.MsgObj;

public interface SkillLauncher {
    default void shaGotShan(Person p) {

    }

    default void shaSuccess(Person p) {

    }

    default void lostHandCardAction() {

    }

    default void lostEquipment() {

    }

    default void useStrategy() {

    }

    default void gotHurt(List<Card> cards, Person p, int num) {

    }

    default void gotShaBegin(Sha sha) {

    }

    default Person changeSha(Sha sha) {
        return null;
    }

    default boolean skillSha(Person source) {
        return false;
    }

    default boolean skillShan(Person source) {
        return false;
    }

    /**
     * 这个技能无懈也是竞争的
     * @param throwedPerson
     * @param msgObj
     * @return
     */
    default Card skillWuxie(AtomicReference<PlayerIO> throwedPerson ,MsgObj privMsgObj) {
        return null;
    }

    default boolean isNaked() {
        return false;
    }

    default void shaBegin(Card card) {

    }

    default void jueDouBegin() {

    }

    default void receiveJudge(Card card) {

    }

    default int gotSavedBy(Person p) {
        return 0;
    }

    default boolean shaCanBeShan(Person p) {
        return true;
    }

    /**
     * 改判牌，如果有多个人可以改判，是按照先后顺序改判的，
     * @param target
     * @param d
     * @return
     */
    default Card changeJudge(Person target, Card d) {
        return null;
    }

    default int numOfTian() {
        return 0;
    }

    /**
     * 这个以后要修改，需要从string转回int，目前太乱了
     * 用来给人物实现释放技能的接口，如果返回true则跳出parseOrder的流程，不进入弃牌等，如果返回false则要进行弃牌
     * @param order
     * @return
     */
    default boolean useSkillInUsePhase(int order) {
        return false;
    }

    default boolean hasMaShu() {
        return false;
    }

    default boolean hasQiCai() {
        return false;
    }

    default boolean hasKongCheng() {
        return false;
    }

    default boolean hasQianXun() {
        return false;
    }

    default boolean hasHongYan() {
        return false;
    }

    default boolean hasHuoShou() {
        return false;
    }

    default boolean hasDuanLiang() {
        return false;
    }

    default boolean hasJuXiang() {
        return false;
    }

    default boolean hasBaZhen() {
        return false;
    }

    default boolean hasFeiYing() {
        return false;
    }

    default void setBaZhen(boolean bool) {

    }

    default void wushuangSha() {

    }


    default boolean hasWuShuang() {
        return false;
    }

    default boolean hasRouLin() {
        return false;
    }

    default boolean hasWeiMu() {
        return false;
    }

    default boolean hasWanSha() {
        return false;
    }

    /**
     * 袁术的行殇
     * @param deadPerson
     * @return
     */
    default boolean usesXingShang(Person deadPerson) {
        return false;
    }

    default String usesYingYang() {
        return "";
    }

    default boolean usesZhiBa() {
        return false;
    }

    default boolean skipJudge() {
        return false;
    }

    default boolean skipDraw(boolean fastMode) {
        return false;
    }

     /**
     * 跳过在群里通知出牌
     * @return
     */
    default void setskipNoticeUsePublic(boolean skipNoticeUsePublic) {
         
    }

    /**
     * 跳过在群里通知出牌
     * @return
     */
    default boolean getSkipNoticeUsePublic() {
        return false;
    }

    default boolean skipUse(boolean fastMode) {
        return false;
    }

    default boolean skipThrow(boolean fastMode) {
        return false;
    }

    default void hurtOther(Person p, int num) {

    }

    // default void otherPersonUsePhase(Person p) {

    // }

    default void otherPersonThrowPhase(Person p, ArrayList<Card> cards) {

    }

    default void otherPersonGetJudge(Person p,Card juCard) {

    }

    default void otherPersonMakeHurt(Person source, Person target) {

    }

    default void otherPersonHurtBySha(Person source, Person target) {

    }

    default void initialize(Identity identity,int userPos) {

    }

    default ArrayList<Card> getExtraCards() {
        return null;
    }

    default void killOther() {

    }

    default String getExtraInfo() {
        return "";
    }

    default void selfDeadAction(Person source){
        
    }

    /**
     * 是否能打出 无懈可击的替换牌
     * @return
     */
    default boolean hasWuXieReplace(){
        return false;
    }
}
