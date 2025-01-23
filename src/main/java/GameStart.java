
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import db.Database;
import db.ImgDB;
import telegramBot.RunGameBot;

public class GameStart {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GameStart <token> [test]");
            return;
        }
        String botToken = args[0];
        boolean isTestMode = args.length > 1 && Boolean.parseBoolean(args[1]);
        
        Database.init();
        ImgDB.init(GameStart.class);
        
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            RunGameBot b = new RunGameBot(botToken, isTestMode);
            botsApplication.registerBot(botToken, b);
            System.out.println("sanguoshaBot successfully started!");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}