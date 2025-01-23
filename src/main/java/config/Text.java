package config;

public class Text {
       public final static String INTRO = "<b>🔥群雄你方唱罢我登台,一起来,加入这场混沌❗️</b>" +
                     "\n\n<i>玩家可扮演三国时期人物，根据随机抽中的隐藏身份（主公、反贼、忠臣、内奸），通过使用独特的人物技能，合理打出各种类型的手牌，运筹帷幄、智取搏杀，获得自己所属身份的胜利！</i>"
                     + "\n\n基础✅风✅林✅火✅山✅"
                     + "\n\n" + DescUrl.getDescHtml("三国杀基础教程");
       public final static String[] POKER_NUM_LIST = "Ⓐ②③④⑤⑥⑦⑧⑨⑩ⒿⓆⓀ".split("");
       public final static String[] CIRCLE_NUM_LIST = "1️⃣ 2️⃣ 3️⃣ 4️⃣ 5️⃣ 6️⃣ 7️⃣ 8️⃣ 9️⃣ 🔟 ⓫ ⓬ ⓭ ⓮ ⓯ ⓰ ⓱ ⓲ ⓳ ⓴"
                     .split(" ");
       public final static String[] CIRCLE_NUM = "①②③④⑤⑥⑦⑧⑨⑩⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳㉑㉒㉓㉔㉕㉖㉗㉘㉙㉚㉛㉜㉝㉞㉟㊱㊲㊳㊴㊵㊶㊷㊸㊹㊺㊻㊼㊽㊾㊿".split("");

       /**
        * 包装一下 String.format ，要不老是报错
        * 
        * @param format
        * @param args
        * @return
        */
       public static String format(String format, Object... args) {
              try {
                     return String.format(format, args);
              } catch (Exception e) {
                     e.printStackTrace();
                     return format.replace("%s", "未知");
              }
       }

       /**
        * 扑克牌带圈数字
        * 
        * @param num
        * @return
        */
       public static String pokerNum(int num) {
              if (num <= 0 || num > 13) {
                     return num + "";
              } else {
                     return POKER_NUM_LIST[num - 1];
              }
       }

       /**
        * 带实心圈数字
        * 
        * @param num
        * @return
        */
       public static String circleNum(int num) {
              if (num <= 0 || num > 20) {
                     return num + "";
              } else {
                     return CIRCLE_NUM_LIST[num - 1];
              }
       }

       // public static void main(String[] args) {
       // String a="%s %s %s";
       // System.out.println(format(a, 1,2));
       // }
}
