package sanguosha.manager;

import sanguosha.cards.Card;
import sanguosha.cards.EquipType;
import sanguosha.cardsheap.CardsHeap;
import sanguosha.people.Person;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {
    private static Random random;
    static{
        random=new Random();
    }
    public static void assertTrue(boolean bool, String s) {
        if (!bool) {
            panic("assertion failed: " + s);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> deepCopy(List<T> src) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);

            return new ArrayList<>((List<T>) in.readObject());
        } catch (Exception e) {
            e.printStackTrace();
            //System.exit(1);
        }
        return null;
    }

    /**
     * 返回从 num1 到 num2 的随机数，闭区间
     * @param num1
     * @param num2
     * @return
     */
    public static int randint(int num1, int num2) {
        return num1 + random.nextInt(num2 - num1 + 1);
    }

    // public static <E> E choice(List<E> options) {
    //     return options.get(randint(0, options.size() - 1));
    // }
    
    public static void checkCardsNum(GameManager gameManager ) {
        if(!gameManager.getTestMode()){
            return;
        }
        gameManager.test_clear_oditor();
        //int ans = gameManager.getCardsHeap().getDrawCards(0).size() + gameManager.getCardsHeap().getUsedCards().size();
        for(Card c:gameManager.getCardsHeap().getUnTakenCard()){
            gameManager.test_count_card(   c);
        }

        for(Card c:gameManager.getCardsHeap().getUsedCards()){
            gameManager.test_count_card("getUsedCards",c);
        }

        for (Person p : gameManager.getPlayers()) {
            //System.out.println("检查 ："+p);
            // ans += p.getCards().size();
            // ans += p.getEquipments().size();
            // ans += p.getJudgeCards().size();
            // ans += p.getExtraCards() == null ? 0 : p.getExtraCards().size();
            for(Card c:p.getCards()){
                gameManager.test_count_card(p+" getCards",c);
            }
            for(EquipType type:p.getEquipments().keySet()){
                gameManager.test_count_card(p+" getEquipments",p.getEquipments().get(type));
            }

            if(p.getJudgeCards().size()>0){
                for(Card c:p.getJudgeCards()){
                    gameManager.test_count_card(p+" getJudgeCards",c);
                    //System.out.println(p+":"+c.info()+c.toString());
                }
            }
            
            if(p.getExtraCards() != null){

                for(Card c:p.getExtraCards()){
                    gameManager.test_count_card(p+" getExtraCards",c);
                }
            }

        }
        // System.out.println (" ===================================");
        // System.out.println ("gameManager.getCardsHeap().getDrawCards(0).size()="+gameManager.getCardsHeap().getDrawCards(0).size());
        // System.out.println ("gameManager.getCardsHeap().getUsedCards().size()="+gameManager.getCardsHeap().getUsedCards().size());

        // gameManager.getIo().println("card number not consistent");
        // for (Person p : gameManager.getPlayers()) {
        //     System.out.println (p+p.showAllCards());
        // }
         // gameManager.getIo().println("card number not consistent");
        // for (Card c : gameManager.getCardsHeap().getUsedCards()) {
        //     System.out.println (c.info()+ c.toString());
        // }

        gameManager.test_show_card_illegle();

    }

    
    public static void panic(String s) {
        String panic="";
        panic += "panic at " + Thread.currentThread().getStackTrace()[1].getFileName();
        panic += " line" + Thread.currentThread().getStackTrace()[1].getLineNumber() + ": " + s;
        System.out.println(panic);
  

    }
}
