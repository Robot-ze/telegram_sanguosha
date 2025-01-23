# telegram_sanguosha
# 电报版本的三国杀

![photo_2025-01-22_16-25-22](https://github.com/user-attachments/assets/2c87fd9d-54cb-4fd3-bbd6-973431bda7ce)


在电报群里玩三国杀，哈哈，没听说过吧
身份局，支持标准包+风火林山神将，少量SP角色，模式为多人

by Robot-ze



## 试玩！[@shanguoshabot](https://t.me/shanguoshabot) 

把机器人 [@shanguoshabot](https://t.me/shanguoshabot) 拉入一个群中，在群中输入 /sha

## 运行

先到 [@BotFather](https://t.me/BotFather) 获取一个 &lt;token&gt;

启动类 GameStart.java

java GameStart &lt;token&gt;

或者打包成jar

java -jar xxxxx.jar &lt;token&gt;
 
## 更新日志
复刻自 https://github.com/wzk1015/sanguosha
* 1.0  ,初次commit
 

## 功能介绍

本软件基于Java实现了电报版三国杀，其中包括了67个武将，41种手牌及若干其他功能类。

主要实现了三国杀对战的功能，涵盖了上百种不同技能，可以进行单机多人对战、玩家挑战AI等，支持自定义游戏人数、身份配置、武将扩展包等功能。玩家们通过电报群与机器人和程序交互。游戏中每名玩家依次执行自己的回合，打出卡牌或使用技能触发一系列复杂的判定过程，并实时判定游戏结束条件，在触发游戏结束条件时结束程序，输出获胜者。



## 整体架构与设计实现

构架设计详见 https://github.com/wzk1015/sanguosha

电报机器人api https://github.com/rubenlagus/TelegramBots

本分支修改了部分出牌等待逻辑,增加了电报机器人接口的输入输出，


 


 
