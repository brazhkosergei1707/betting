import betting.Betting;
import betting.entity.MatchLiveBetting;
import betting.parserBetting.BettingBetFair;
import betting.parserBetting.BettingParser;
import org.openqa.selenium.WebDriver;
import service.DriverService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestBot {

    public static void main(String[] args) {
        String path = "C:\\chromedriver\\chromedriver.exe";
        String usernameBetFair = "dr070981";
        String passwordBetFair = "365awe734";
        WebDriver driver = DriverService.getInstance().getWebDriver(path);


        BettingParser parser = new BettingBetFair();
        Betting betting = new Betting(path,parser);
        parser.logIn(usernameBetFair, passwordBetFair, driver);

        Map<String, MatchLiveBetting> map = new HashMap<>();
        parser.lookingForMatches(driver,map,0.1,0.2,betting);

        for(String name:map.keySet()){
            MatchLiveBetting matchLive = map.get(name);
            parser.clickToMatch(driver,matchLive.getNameOfMatch());
            parser.checkGameScore(driver, matchLive);
            parser.checkMaximumBetSizeAndPossibleBetSelection(driver, matchLive);
            parser.goBackToOverview(driver);
        }
//        parser.updateCurrentMatchesLive(map,driver,0.1,0.2,betting);
//        List<Bet> list = new ArrayList<>();
//        parser.updateHistoryBet(driver,list);
//        System.out.println("История ставок: ");
//        for(Bet bet:list){
//            System.out.println(bet.getMatchName());
//            System.out.println(bet.getSelectionBet());
//            System.out.println(bet.getOddEven());
//            System.out.println(bet.getPrice());
//            System.out.println(bet.getWin());
//            System.out.println("===============================");
//        }
//        list.clear();
//
//        parser.updateCurrentBet(driver,list);
//        System.out.println("Текущие ставки:");
//        for(Bet bet:list){
//            System.out.println(bet.getMatchName());
//            System.out.println(bet.getSelectionBet());
//            System.out.println(bet.getOddEven());
//            System.out.println(bet.getPrice());
//            System.out.println(bet.getWin());
//            System.out.println("===============================");
//        }
        parser.logOut(driver);
        driver.quit();
//        StartFrame.addSwitcher(new SwitcherMain(usernameParimatch,passwordParimatch,path,parser,parser.getSwitcherId()));

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }



//
//
//        Bet bet = new Bet();
//        bet.setMatchName("LPU Pirates vs MIT Cardinals");
//        bet.setPrice(1.0);
//        bet.setSelectionBet(SelectionBet.Q3);
//        bet.setOddEven(OddEven.Odd);
//
//        parser.makeBet(bet,driver,null);
//
//        System.out.println("=======================================");
//        System.out.println("=======================================");
//        System.out.println("=======================================");
//        System.out.println("Время ставки равно - "+bet.getTimeBet());
//        System.out.println("=======================================");
//        System.out.println("=======================================");
//        System.out.println("=======================================");
//


//        Bet bet1 = new Bet();
//        bet1.setMatchName("Tarnowskie Gory vs Mickiewicz Romus Katowice");
//        bet1.setSelectionBet(SelectionBet.Q2);
//        bet1.setOddEven(OddEven.Even);
//
//
//        Boolean aBoolean = parser.betAlreadyNotPlayed(bet, driver);
//        System.out.println("не играет ли ставка, false должен быть - "+aBoolean);
//
//        Boolean aBoolean1 = parser.betAlreadyNotPlayed(bet1, driver);
//        System.out.println("не играет ли ставка, true должен быть - "+aBoolean1);
//
//
//
//
//
////        parser.updateCurrentBet(driver);
////

//        parser.updateHistoryBet(driver);



//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("Поставлена ли ставка - "+parser.betAlreadyNotPlayed(bet,driver));
//
//        Bet bet = new Bet();
//        bet.setMatchName("Syria U-17 vs Iraq U-17");
//        bet.setSelectionBet(SelectionBet.Q2);
//        bet.setOddEven(OddEven.Even);
//        parser.checkBet(bet,driver);
//
//
//        Bet bet1 = new Bet();
//        bet1.setMatchName("Iran U-17 vs Syria U-17");
//        bet1.setSelectionBet(SelectionBet.Q1);
//        bet1.setOddEven(OddEven.Odd);
//        parser.checkBet(bet1,driver);



//        System.out.println("Ставка выиграла - " + bet.getWin());
//        System.out.println("Ставка выиграла - " + bet1.getWin());
//
//        parser.setCurrentTimeAndCount(driver);

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
////            e.printStackTrace();
//        }

//        parser.updateCurrentMatchesLive(driver);

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        parser.updateCurrentBet(driver);

//
//        Map<Integer, MatchLive> currentMatches = StartFrame.getSwitcher(parser,parser.getSwitcherId()).getStorage().getCurrentMatches();
//        parser.lookingForMatches(driver);

//        for(Integer integer:currentMatches.keySet()){
//            MatchLive matchLive = currentMatches.get(integer);
//            parser.clickToMatch(driver,matchLive.getNameOfMatch());
//            parser.checkMaximumBetSizeAndPossibleBetSelection(driver,matchLive);
//            parser.checkGameScore(driver,matchLive);
//            System.out.println("Имя матча - "+matchLive.getNameOfMatch());
//            System.out.println("Период матча - "+matchLive.getCurrentPeriod());
//            System.out.println("Максимальная ставка матча - " + matchLive.getMaxBet());
//            System.out.println("===================================================");
//        }

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        parser.logOut(driver);
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        driver.quit();
    }

    private static String correctString(String oldString) {
        if (oldString.contains(" (Univ-w)")) {
            oldString = oldString.replace("(Univ-w)", "");
        } else if (oldString.contains(" Uni. Women")) {
            oldString = oldString.replace("Uni. Women", "");
        } else if (oldString.contains(" W (univ)")) {
            oldString = oldString.replace("W (univ)", "");
        } else if (oldString.contains(" (univ)")) {
            oldString = oldString.replace("(univ)", "");
        } else if (oldString.contains("(Univ.)")) {
            oldString = oldString.replace("(Univ.)", "");
        } else if (oldString.contains(" Uni")) {
            oldString = oldString.replace("Uni", "");
        } else if (oldString.contains(" Uni.")) {
            oldString = oldString.replace("Uni.", "");
        } else if (oldString.contains("(w)")) {
            oldString = oldString.replace("(w)", "");
        } else if (oldString.contains(" W ")) {
            oldString = oldString.replace(" W ", "");
        }
//        if(oldString.contains(" Uni")){
//            oldString = oldString.replace("Uni.","");
//        }


        //  Chinese Taipei (univ) vs Hungary (univ)  ||Chinese Taipei W (univ) vs Chile W (univ)
        //  Chinese Taipei (Univ.) vs Hungary (Univ.) ||Chinese Taipei (Univ-w) vs Chile (Univ-w)||
        //  Chinese Taipei Uni vs Hungary Uni.       ||Chinese Taipei Uni. Women vs Chile Uni. Women


        // Israel U-16 (w)     | Israel W U16    |
        // Hungary (Univ-w)    | Hungary W(univ) | Hungary Uni. Woman
        // Germany (Univ)      | Germany (univ)  | Germany Uni
        // Albania U-16 (w)    | Albania W U16   |

        return oldString;
    }
    public static void showLongFromDate(String string) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        dateFormat.applyPattern("mm:ss");
        try {
            Date parse = dateFormat.parse(string);
            System.out.println(parse.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}


