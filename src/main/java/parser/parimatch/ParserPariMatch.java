package parser.parimatch;

import UI.startFrame.StartFrame;
import bot.MatchesScanner;
import betting.entity.Bet;
import entityes.MatchLive;
import betting.enumeration.OddEven;
import betting.enumeration.SelectionBet;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import parser.Parser;

import java.text.SimpleDateFormat;
import java.util.*;

public class ParserPariMatch implements Parser {

    private Logger log;
    private Appender appender;

    private  int switcherId;
    private boolean isActiveHistory = false;
    private boolean isActiveDetails = false;
    private boolean haveEmergencyMatches = false;
    List<MatchLive> emergencyMatches = new ArrayList<>();

    Random random = new Random();
    private SimpleDateFormat dateFormat = new SimpleDateFormat();

    @Override
    public Bet checkBet(Bet bet, WebDriver driver) {
        try {
            driver.findElement(By.cssSelector("tab[name='MY_BETS']")).click();
            Thread.sleep(500);

            driver.findElement(By.cssSelector("tab[name='CALCULATED_BETS']")).click();
            Thread.sleep(1000);

            List<WebElement> historyBets = driver.findElement(By.cssSelector("bets-history-list.bets-history-list")).findElements(By.cssSelector("div.bets-history-list__item"));

            String nameOfMatch = null;
            OddEven oddEven = null;
            SelectionBet selectionBet = null;
            Boolean isWin = null;
            String isWinString = null;

            System.out.println("Найдено " + historyBets.size() + " ставок.");
            for (WebElement betElement : historyBets) {
                try {
                    String nameOfMatchString = betElement.findElement(By.cssSelector("h2.bet-block__head")).getText();//name of teams -  Slovakia U-16 - Switzerland U-16
                    if (nameOfMatchString != null) {
                        String[] split = nameOfMatchString.split(" - ");
                        nameOfMatch = split[0] + " vs " + split[1];
                    }

                    String selectionBetString = betElement.findElement(By.cssSelector("div.bet-block__detail")).findElement(By.cssSelector("div.market-name")).getText();//Total. Even/odd. 2nd quarter
                    if (selectionBetString != null) {
                        if (selectionBetString.contains("1st quarter")) {//1st Quarter - Total Odd/Even
                            selectionBet = SelectionBet.Q1;
                        } else if (selectionBetString.contains("2nd quarter")) {//
                            selectionBet = SelectionBet.Q2;
                        } else if (selectionBetString.contains("3rd quarter")) {//3rd Quarter - Total Odd/Even
                            selectionBet = SelectionBet.Q3;
                        } else if (selectionBetString.contains("4th quarter")) {
                            selectionBet = SelectionBet.Q4;
                        } else if (selectionBetString.contains("1st half")) {//1st Half Total Odd/Even
                            selectionBet = SelectionBet.Half1st;
                        } else if (selectionBetString.contains("2nd half")) {//2nd Half - Total Odd/Even
                            selectionBet = SelectionBet.Half2nd;
                        } else {//Game Total - Odd/Even,
                            selectionBet = SelectionBet.Match;
                        }
                    }

                    String oddEvenString = betElement.findElement(By.cssSelector("div.bet-block__detail")).findElement(By.cssSelector("div.outcome-name")).getText();//Even
                    if (oddEvenString != null) {
                        if (oddEvenString.contains("Odd")) {
                            oddEven = OddEven.Odd;
                        } else if (oddEvenString.contains("Even")) {
                            oddEven = OddEven.Even;
                        }
                    }

                    isWinString = betElement.findElement(By.cssSelector("div.bet-block__win_lose")).findElement(By.cssSelector("strong")).getText();//0,00 EUR LOOOSSSEEEE
                    if (isWinString != null) {
                        if (isWinString.contains("0,00")) {
                            isWin = false;
                        }
                    }


                    System.out.println("Имя матча - " + nameOfMatch);
                    System.out.println("Чет/нечет STRING - " + oddEvenString);
                    System.out.println("Чет/нечет - " + oddEven);
                    System.out.println("Выбор ставки STRING - " + selectionBetString);
                    System.out.println("Выбор ставки - " + selectionBet);
                    System.out.println("Выиграла ли савка - " + isWin);
                    System.out.println("===================================================");

                    if(bet.getMatchName().compareTo(nameOfMatch)==0){
                        if(bet.getSelectionBet()==selectionBet){
                            if(bet.getOddEven()==oddEven) {
                                bet.setWin(isWin);
                                break;
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        bet.setWin(random.nextBoolean());
        return bet;
    }

    @Override
    public Bet makeBet(Bet bet, WebDriver driver, MatchLive matchLive) {
        try{
            boolean betAlreadyNoPlay;
            Boolean b1=betAlreadyNotPlayed(bet, driver);
            if(b1!=null){
                betAlreadyNoPlay = b1;
            } else {
                betAlreadyNoPlay = true;
            }


            if(betAlreadyNoPlay){
                List<WebElement> marketGroups = null;
                try {
                    List<WebElement> groupsBetsElements = driver.findElement(By.cssSelector("div.live-block__more-content")).findElements(By.cssSelector("div.event__markets"));
                    marketGroups = groupsBetsElements.get(groupsBetsElements.size() - 1).findElements(By.cssSelector("div.markets"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String text = null;
                switch (bet.getSelectionBet()){
                    case Q1:text="Total. Even/odd. 1st quarter:";
                        break;
                    case Q2:text="Total. Even/odd. 2nd quarter:";
                        break;
                    case Q3:text="Total. Even/odd. 3rd quarter:";
                        break;
                    case Q4:text="Total. Even/odd. 4th quarter:";
                        break;
                    case Half1st:text="TEST";
                        break;
                    case Half2nd:text="TEST";
                        break;
                    case Match:text="Total. Even/odd:";
                        break;
                }

                if (marketGroups != null) {
                    for (WebElement element1 : marketGroups) {
                        try {
                            String totalOddEvenGroupStringHeader = element1.findElement(By.cssSelector("div.markets-header")).findElement(By.cssSelector("span")).getText();
                            if (totalOddEvenGroupStringHeader != null) {
                                if (totalOddEvenGroupStringHeader.contains(text)) {
                                    List<WebElement> oddEvenElements = element1.findElements(By.cssSelector("span.outcome__coeff"));

                                    try{
                                        driver.findElement(By.cssSelector("tab[name = 'COUPON']")).click();
                                        List<WebElement> elements = driver.findElements(By.cssSelector("i.icon-ai-close"));
                                        for(WebElement e:elements){
                                            e.click();
                                            Thread.sleep(500);
                                        }
                                    }catch (Exception e){
                                        log.info("Проблема во время закрытия прошлых ставок.");
                                        System.out.println("Проблема во время закрытия прошлых ставок.");
                                    }

                                    boolean click = false;
                                    if (bet.getOddEven() == OddEven.Even) {
                                        oddEvenElements.get(0).click();
                                        click = true;
                                    } else {
                                        oddEvenElements.get(1).click();
                                        click = true;
                                    }

                                    Thread.sleep(500);

                                    if (click) {
                                        driver.findElement(By.cssSelector("tab[name = 'COUPON']")).click();
                                        Thread.sleep(500);
                                        WebElement inputField = driver.findElement(By.cssSelector("input.stake-value__input"));
                                        inputField.click();
                                        inputField.clear();
                                        Thread.sleep(500);
                                        inputField.sendKeys(String.valueOf(bet.getPrice()));
                                        WebElement confirmButtonElement = driver.findElement(By.cssSelector("button.betslip-footer-place-btn"));
                                        Actions actions = new Actions(driver);
                                        actions.moveToElement(driver.findElement(By.cssSelector("div.main-footer-menu")));
                                        actions.perform();
                                        Thread.sleep(6000);
                                        confirmButtonElement.click();
                                        Thread.sleep(6000);
                                        //TODO Element <button type="button" class="btn betslip-footer-place-btn">...</button> is not clickable at point (1340, 735). Other element would receive the click: <iframe frameborder="0" src="about:blank" style="background-color: transparent; vertical-align: text-bottom; position: relative; width: 100%; height: 100%; min-width: 100%; min-height: 100%; max-width: 100%; max-height: 100%; margin: 0px; overflow: hidden; display: block;" data-test-id="ChatWidgetButton-iframe"></iframe>
                                        betAlreadyNoPlay = false;
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                driver.findElement(By.cssSelector("i.icon-ai-close")).click();
                            }catch (Exception e1){
                                e1.printStackTrace();
                            }
                        }
                    }
                }
                Boolean b=betAlreadyNotPlayed(bet, driver);
                if(b!=null){
                    betAlreadyNoPlay = b;
                }
            }

            if(!betAlreadyNoPlay){
                log.info("Ставка сделана, время ставки " + new Date(System.currentTimeMillis()));
                System.out.println("Ставка сделана, время ставки " + new Date(System.currentTimeMillis()));
                bet.setTimeBet(System.currentTimeMillis());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

//        if (bet.getPrice() != 0.0) {
//            if (bet.getSelectionBet() != null) {
//                if (bet.getOddEven() != null) {
//                    bet.setMatchPeriod(matchLive.getCurrentPeriodInt());
//                    bet.setTimeBet(0L);
//                }
//            }
//        }

        return bet;
    }

//    ==============================================
//    ==============================================
//    ==============================================

    @Override
    public void updateCurrentBet(WebDriver driver) {
        try {
            log.info("Обновляем список текущих ставок на сайте " + getNameOfSite());
            List<Bet> list = null;//StartFrame.getSwitcher(this,switcherId).getStorage().getCurrentBets();

            driver.findElement(By.cssSelector("tab[name='MY_BETS']")).click();
            Thread.sleep(500);


            try {//TODO org.openqa.selenium.NoSuchElementException: no such element: Unable to locate element: {"method":"css selector","selector":"div.modal-foot_has-two"}
                driver.findElement(By.cssSelector("div.modal-foot_has-two")).findElements(By.cssSelector(".modal-btn")).get(1).click();
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }


            driver.findElement(By.cssSelector("tab[name='UNCALCULATED_BETS']")).click();
            Thread.sleep(1000);

            List<WebElement> historyBets = driver.findElement(By.cssSelector("bets-history-list.bets-history-list")).findElements(By.cssSelector("div.bets-history-list__item"));
            System.out.println("Найдено " + historyBets.size() + " ставок.");
            log.info("Найдено " + historyBets.size() + " ставок.");
            boolean isBet = true;
            if (historyBets == null) {
                log.info("Ставок не найдено.");
                isBet = false;
            }
            if (isBet) {

                Bet bet;
                long dateOfBet = 0L;
                String dateString = null;
                Date date = null;
                String nameOfMatch = null;
                OddEven oddEven = null;
                SelectionBet selectionBet = null;
                int periodBetInt = 0;
                Double priceOfBet = 0.0;
                String priceOfBetString;
                System.out.println("Найдено " + historyBets.size() + " ставок.");
                for (WebElement betElement : historyBets) {
                    try {
                        dateString = betElement.findElement(By.cssSelector("div.bet-block-event-info__date")).getText();// 18.08.17, 17:30

                        if (dateString != null) {
                            String[] split = dateString.split(", ");
                            dateString = split[1];
                            dateFormat.applyPattern("mm:ss");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                            date = dateFormat.parse(dateString);
                            dateOfBet = date.getTime();
                        }


                        String nameOfMatchString = betElement.findElement(By.cssSelector("h2.bet-block__head")).getText();//name of teams -  Slovakia U-16 - Switzerland U-16
                        if (nameOfMatchString != null) {
                            String[] split = nameOfMatchString.split(" - ");
                            nameOfMatch = split[0] + " vs " + split[1];
                        }

                        String selectionBetString = betElement.findElement(By.cssSelector("div.bet-block__detail")).findElement(By.cssSelector("div.market-name")).getText();//Total. Even/odd. 2nd quarter
                        if (selectionBetString != null) {
                            if (selectionBetString.contains("1st quarter")) {//1st Quarter - Total Odd/Even
                                selectionBet = SelectionBet.Q1;
                                periodBetInt = 1;
                            } else if (selectionBetString.contains("2nd quarter")) {//
                                selectionBet = SelectionBet.Q2;
                                periodBetInt = 2;
                            } else if (selectionBetString.contains("3rd quarter")) {//3rd Quarter - Total Odd/Even
                                selectionBet = SelectionBet.Q3;
                                periodBetInt = 3;
                            } else if (selectionBetString.contains("4th quarter")) {
                                selectionBet = SelectionBet.Q4;
                                periodBetInt = 4;
                            } else if (selectionBetString.contains("1st half")) {//1st Half Total Odd/Even
                                selectionBet = SelectionBet.Half1st;
                                periodBetInt = 2;
                            } else if (selectionBetString.contains("2nd half")) {//2nd Half - Total Odd/Even
                                selectionBet = SelectionBet.Half2nd;
                                periodBetInt = 4;
                            } else {//Game Total - Odd/Even,
                                selectionBet = SelectionBet.Match;
                                periodBetInt = 4;
                            }
                        }

                        String oddEvenString = betElement.findElement(By.cssSelector("div.bet-block__detail")).findElement(By.cssSelector("div.outcome-name")).getText();//Even
                        if (oddEvenString != null) {
                            if (oddEvenString.contains("Odd")) {
                                oddEven = OddEven.Odd;
                            } else if (oddEvenString.contains("Even")) {
                                oddEven = OddEven.Even;
                            }
                        }

                        priceOfBetString = betElement.findElement(By.cssSelector("div.bet-block__amount")).findElement(By.cssSelector("strong")).getText();//1,00 EUR
                        if (priceOfBetString != null) {
                            String[] split = priceOfBetString.split(" ");
                            String price = split[0].replace(",", ".");
                            priceOfBet = Double.parseDouble(price);
                        }

                        System.out.println("Имя матча - " + nameOfMatch);
                        System.out.println("Чет/нечет STRING - " + oddEvenString);
                        System.out.println("Чет/нечет - " + oddEven);
                        System.out.println("Выбор ставки STRING - " + selectionBetString);
                        System.out.println("Выбор ставки - " + selectionBet);
                        System.out.println("Цена ставки - " + priceOfBet);
                        System.out.println("===================================================");
                        boolean newBet = true;
                        for (Bet betList : list) {
                            if (nameOfMatch.contains(betList.getMatchName().split(" vs ")[0])) {
                                if (selectionBet == betList.getSelectionBet()) {
                                    if (oddEven == betList.getOddEven()) {
                                        System.out.println("Bet for match: " + nameOfMatch + " is finded...");
                                        betList.setPlay(true);
                                        if (betList.getPrice() == 0.0 || betList.getPrice() == null) {
                                            betList.setPrice(priceOfBet);
                                        }
                                        newBet = false;
                                    }
                                }
                            }
                        }

                        if (newBet) {
                            bet = new Bet(nameOfMatch, selectionBet, priceOfBet, oddEven);
                            bet.setTimeBet(dateOfBet);
                            bet.setPlay(true);
                            bet.setMatchPeriod(periodBetInt);
                            System.out.println("new bet was added....");
                            list.add(bet);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateHistoryBet(WebDriver driver) {
        try {
            driver.findElement(By.cssSelector("tab[name='MY_BETS']")).click();
            Thread.sleep(500);

            driver.findElement(By.cssSelector("tab[name='CALCULATED_BETS']")).click();
            Thread.sleep(1000);

            List<WebElement> historyBets = driver.findElement(By.cssSelector("bets-history-list.bets-history-list")).findElements(By.cssSelector("div.bets-history-list__item"));
            List<Bet> currentList =  null;// StartFrame.getSwitcher(this,switcherId).getStorage().getHistoryBets();

            Bet bet;
            long dateOfBet = 0L;
            String dateString = null;
            Date date = null;
            String nameOfMatch = null;
            OddEven oddEven = null;
            SelectionBet selectionBet = null;
            int periodBetInt = 0;
            Double priceOfBet = 0.0;
            String priceOfBetString;
            Boolean isWin = null;
            String isWinString = null;

            System.out.println("Найдено " + historyBets.size() + " ставок.");
            for (WebElement betElement : historyBets) {
                try {
                    Thread.sleep(1000);
                    dateString = betElement.findElement(By.cssSelector("div.bet-block-event-info__date")).getText();// 18.08.17, 17:30
                    if (dateString != null) {
                        String[] split = dateString.split(", ");
                        dateString = split[1];
                        dateFormat.applyPattern("mm:ss");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                        date = dateFormat.parse(dateString);
                        dateOfBet = date.getTime();
                    }

                    Thread.sleep(500);
                    String nameOfMatchString = betElement.findElement(By.cssSelector("h2.bet-block__head")).getText();//name of teams -  Slovakia U-16 - Switzerland U-16
                    if (nameOfMatchString != null) {
                        String[] split = nameOfMatchString.split(" - ");
                        nameOfMatch = split[0] + " vs " + split[1];
                    }


                    Thread.sleep(500);
                    String selectionBetString = betElement.findElement(By.cssSelector("div.bet-block__detail")).findElement(By.cssSelector("div.market-name")).getText();//Total. Even/odd. 2nd quarter
                    if (selectionBetString != null) {
                        if (selectionBetString.contains("1st quarter")) {//1st Quarter - Total Odd/Even
                            selectionBet = SelectionBet.Q1;
                            periodBetInt = 1;
                        } else if (selectionBetString.contains("2nd quarter")) {//
                            selectionBet = SelectionBet.Q2;
                            periodBetInt = 2;
                        } else if (selectionBetString.contains("3rd quarter")) {//3rd Quarter - Total Odd/Even
                            selectionBet = SelectionBet.Q3;
                            periodBetInt = 3;
                        } else if (selectionBetString.contains("4th quarter")) {
                            selectionBet = SelectionBet.Q4;
                            periodBetInt = 4;
                        } else if (selectionBetString.contains("1st half")) {//1st Half Total Odd/Even
                            selectionBet = SelectionBet.Half1st;
                            periodBetInt = 2;
                        } else if (selectionBetString.contains("2nd half")) {//2nd Half - Total Odd/Even
                            selectionBet = SelectionBet.Half2nd;
                            periodBetInt = 4;
                        } else {//Game Total - Odd/Even,
                            selectionBet = SelectionBet.Match;
                            periodBetInt = 4;
                        }
                    }


                    Thread.sleep(500);
                    String oddEvenString = betElement.findElement(By.cssSelector("div.bet-block__detail")).findElement(By.cssSelector("div.outcome-name")).getText();//Even
                    if (oddEvenString != null) {
                        if (oddEvenString.contains("Odd")) {
                            oddEven = OddEven.Odd;
                        } else if (oddEvenString.contains("Even")) {
                            oddEven = OddEven.Even;
                        }
                    }


                    Thread.sleep(500);
                    priceOfBetString = betElement.findElement(By.cssSelector("div.bet-block__amount")).findElement(By.cssSelector("strong")).getText();//1,00 EUR
                    if (priceOfBetString != null) {
                        String[] split = priceOfBetString.split(" ");
                        String price = split[0].replace(",", ".");
                        priceOfBet = Double.parseDouble(price);
                    }


                    try {
                        Thread.sleep(500);
                        isWinString = betElement.findElement(By.cssSelector("div.bet-block__win_lose")).findElement(By.cssSelector("strong")).getText();//0,00 EUR LOOOSSSEEEE
                    } catch (Exception e) {
                        System.out.println("Нельзя делать ставки сейчас.");
                    }
                    if (isWinString != null) {
                        if (isWinString.contains("0,00")) {
                            isWin = false;
                        }
                    }


                    System.out.println("Имя матча - " + nameOfMatch);
                    System.out.println("Чет/нечет STRING - " + oddEvenString);
                    System.out.println("Чет/нечет - " + oddEven);
                    System.out.println("Выбор ставки STRING - " + selectionBetString);
                    System.out.println("Выбор ставки - " + selectionBet);
                    System.out.println("Цена ставки - " + priceOfBet);
                    System.out.println("Выиграла ли тсавка - " + isWin);
                    System.out.println("===================================================");

                    boolean oldBet = false;
                    if (nameOfMatch != null) {
                        if (oddEven != null) {
                            if (selectionBet != null) {
                                for (Bet bet1 : currentList) {
                                    if (bet1.getMatchName().contains(nameOfMatch.split("vs")[0])) {
                                        if (bet1.getSelectionBet() == selectionBet) {
                                            if (bet1.getOddEven() == oddEven) {
                                                oldBet = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!oldBet) {// new bet
                        bet = new Bet(nameOfMatch, selectionBet, priceOfBet, oddEven);
                        bet.setWin(isWin);
                        bet.setTimeBet(dateOfBet);
                        bet.setMatchPeriod(periodBetInt);
                        currentList.add(bet);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean betAlreadyNotPlayed(Bet lastBet, WebDriver driver) {

        Boolean betAlreadyNotPlayed = true;
        try {
            log.info("Обновляем страницу на сайте " + getNameOfSite());
            System.out.println("Обновляем страницу на сайте " + getNameOfSite());
            driver.get(driver.getCurrentUrl());
            Thread.sleep(2000);
            log.info("Проверяем есть ли ставка в списке текущих. На сайте " + getNameOfSite());
            driver.findElement(By.cssSelector("tab[name='MY_BETS']")).click();
            Thread.sleep(500);
            driver.findElement(By.cssSelector("tab[name='MY_BETS']")).click();
            Thread.sleep(500);

            try {
                driver.findElement(By.cssSelector("div.modal-foot_has-two")).findElements(By.cssSelector(".modal-btn")).get(1).click();
            } catch (Exception e) {
                System.out.println("Нет всплывающего сообщения.");
            }

            driver.findElement(By.cssSelector("tab[name='UNCALCULATED_BETS']")).click();
            Thread.sleep(500);
//TODO rg.openqa.selenium.StaleElementReferenceException: stale element reference: element is not attached to the page document
            List<WebElement> historyBets = driver.findElement(By.cssSelector("bets-history-list.bets-history-list")).findElements(By.cssSelector("div.bets-history-list__item"));
            System.out.println("Найдено " + historyBets.size() + " ставок.");
            log.info("Найдено " + historyBets.size() + " ставок.");
            boolean isBet = true;
            if (historyBets == null) {
                log.info("Ставок не найдено.");
                isBet = false;
            }
            if (isBet) {
                String nameOfMatch = null;
                OddEven oddEven = null;
                SelectionBet selectionBet = null;
                System.out.println("Найдено " + historyBets.size() + " ставок.");
                for (WebElement betElement : historyBets) {
                    try {
                        String nameOfMatchString = betElement.findElement(By.cssSelector("h2.bet-block__head")).getText();//name of teams -  Slovakia U-16 - Switzerland U-16
                        if (nameOfMatchString != null) {
                            String[] split = nameOfMatchString.split(" - ");
                            nameOfMatch = split[0] + " vs " + split[1];
                        }

                        String selectionBetString = betElement.findElement(By.cssSelector("div.bet-block__detail")).findElement(By.cssSelector("div.market-name")).getText();//Total. Even/odd. 2nd quarter
                        if (selectionBetString != null) {
                            if (selectionBetString.contains("1st quarter")) {//1st Quarter - Total Odd/Even
                                selectionBet = SelectionBet.Q1;
                            } else if (selectionBetString.contains("2nd quarter")) {//
                                selectionBet = SelectionBet.Q2;
                            } else if (selectionBetString.contains("3rd quarter")) {//3rd Quarter - Total Odd/Even
                                selectionBet = SelectionBet.Q3;
                            } else if (selectionBetString.contains("4th quarter")) {
                                selectionBet = SelectionBet.Q4;
                            } else if (selectionBetString.contains("1st half")) {//1st Half Total Odd/Even
                                selectionBet = SelectionBet.Half1st;
                            } else if (selectionBetString.contains("2nd half")) {//2nd Half - Total Odd/Even
                                selectionBet = SelectionBet.Half2nd;
                            } else {//Game Total - Odd/Even,
                                selectionBet = SelectionBet.Match;
                            }
                        }

                        String oddEvenString = betElement.findElement(By.cssSelector("div.bet-block__detail")).findElement(By.cssSelector("div.outcome-name")).getText();//Even
                        if (oddEvenString != null) {
                            if (oddEvenString.contains("Odd")) {
                                oddEven = OddEven.Odd;
                            } else if (oddEvenString.contains("Even")) {
                                oddEven = OddEven.Even;
                            }
                        }

                        System.out.println("Имя матча - " + nameOfMatch);
                        System.out.println("Чет/нечет STRING - " + oddEvenString);
                        System.out.println("Чет/нечет - " + oddEven);
                        System.out.println("Выбор ставки STRING - " + selectionBetString);
                        System.out.println("Выбор ставки - " + selectionBet);
                        System.out.println("===================================================");

                        if (MatchesScanner.compareMatchNames(lastBet.getMatchName(), nameOfMatch)) {
                            if (Bet.compareBetSelection(lastBet.getSelectionBet(), selectionBet)) {
                                if (lastBet.getOddEven() == oddEven) {
                                    lastBet.zeroCountCheck();
                                    betAlreadyNotPlayed = false;
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            betAlreadyNotPlayed = null;
        }

        if (betAlreadyNotPlayed != null) {
            if (betAlreadyNotPlayed) {
                System.out.println("Ставка еще не играет.");
                log.info("Ставка еще не играет.");
            } else {
                System.out.println("Ставка уже есть в списке текущих ставок.");
                log.info("Ставка уже есть в списке текущих ставок.");
            }
        } else {
            System.out.println("Сбой во время проверки 'установлена ли ставка'.");
            log.info("Сбой во время проверки 'установлена ли ставка'.");
        }

        return betAlreadyNotPlayed;
    }

//==============================================================================
//==============================================================================
//==============================================================================

    @Override
    public boolean logIn(String username, String password, WebDriver driver) {
        try {
            log.info("Заходим на сайт " + getNameOfSite());
            driver.get("https://www.parimatch.com/en/");
            Thread.sleep(2000);
            driver.findElement(By.cssSelector("a.login")).click();
            Thread.sleep(5000);
            WebElement userName = driver.findElement(By.cssSelector("input[name='username']"));
            userName.click();
            Thread.sleep(500);
            userName.sendKeys(username);
            Thread.sleep(500);
            WebElement passwd = driver.findElement(By.cssSelector("input[name='passwd']"));
            passwd.click();
            Thread.sleep(500);
            passwd.sendKeys(password);
            Thread.sleep(1000);
            driver.findElement(By.cssSelector("button.ok")).click();
            Thread.sleep(1000);
            driver.findElement(By.cssSelector("div.dropdown")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.findElement(By.cssSelector("div.dropdown__content")).findElements(By.cssSelector("li.dropdown__item")).get(0).click();
            Thread.sleep(2000);
            driver.get("https://air.parimatch.com/live");
            Thread.sleep(2000);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            log.info("Проблемы во время логирования на сайте " + getNameOfSite());
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Логирование успешно на сайте " + getNameOfSite());
        return true;
    }

    @Override
    public boolean logOut(WebDriver driver) {
        log.info("Выходим из сайта " + getNameOfSite());
        if (driver != null) {
            try {
                driver.findElement(By.cssSelector("a.logout")).click();
                Thread.sleep(2000);
            } catch (Exception e) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setCurrentTimeAndCount(WebDriver driver) {
//        StartFrame.getSwitcher(this,switcherId).getStorage().setCurrentTime(System.currentTimeMillis());
        try {
            driver.findElement(By.cssSelector("div.login-wrapper__balance")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String balance;
            balance = driver.findElement(By.cssSelector("div.balance-wrap__value")).getText();
            if (balance == null) {
                return;
            }

            double balanceDouble = parseDouble(balance);
            System.out.println("Текущий баланс на сайте " + getNameOfSite() + " равен: " + balanceDouble);
            log.info("Текущий баланс на сайте " + getNameOfSite() + " равен: " + balanceDouble);
//            StartFrame.getSwitcher(this,switcherId).getStorage().setCurrentBalance(balanceDouble);
//            StartFrame.getSwitcher(this,switcherId).getStorage().setCurrentBalanceString(balance);
            driver.findElement(By.cssSelector("div.login-wrapper__balance")).click();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double parseDouble(String balance) {
        double doubleBalance = 0.0;
        try {
            doubleBalance = Double.parseDouble(balance.split(" ")[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(" Balance is - " + balance);
        return doubleBalance;
    }

    @Override
    public void updateCurrentMatchesLive(WebDriver driver) {
        try {

            driver.get("https://air.parimatch.com/live");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            log.info("Сканируем матчи на сайте " + getNameOfSite() + ". Первое сканирование.");
            driver.findElement(By.cssSelector("a[href='live/overview']")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<WebElement> groupElements = driver.findElements(By.cssSelector("live-group.live-group"));

            List<WebElement> matchLiveItems = null;
            try {
                for (WebElement element : groupElements) {
                    String text = element.findElement(By.cssSelector("span.live-block-head-title__text")).getText();
                    if (text.compareTo("Basketball") == 0) {
                        matchLiveItems = element.findElements(By.cssSelector("live-block[mixin='live-block-new']"));
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean isMatches = false;
            if (matchLiveItems != null && matchLiveItems.size() > 0) {
                isMatches = true;
            }
            if (isMatches) {
                Map<Integer, MatchLive> currentMatches = null;
//                if (StartFrame.getSwitcher(this,switcherId) != null) {
//                    currentMatches = StartFrame.getSwitcher(this,switcherId).getStorage().getCurrentMatches();
//                }
                String nameOfMatch = null;
                SelectionBet currentPeriod = null;
                Double maxBet = null;
                boolean isPossibleToMakeBet = true;
                if (matchLiveItems != null) {
                    log.info("Найдено " + matchLiveItems.size() + " матчей.");
                    System.out.println("Найдено " + matchLiveItems.size() + " матчей.");
                    outer:
                    for (int i = 0; i < matchLiveItems.size(); i++) {
                        try {
                            Thread.sleep(1000);
                            WebElement element = null;
                            groupElements = driver.findElements(By.cssSelector("live-group.live-group"));

                            inner:
                            for (WebElement elementOne : groupElements) {
                                String text = elementOne.findElement(By.cssSelector("span.live-block-head-title__text")).getText();
                                if (text.compareTo("Basketball") == 0) {
                                    try {
                                        element = elementOne.findElements(By.cssSelector("live-block[mixin='live-block-new']")).get(i);
                                        Actions actions = new Actions(driver);
                                        actions.moveToElement(element);
                                        actions.perform();
                                    } catch (IndexOutOfBoundsException e) {
                                        System.out.println("Сместились матчи.");
                                        log.info("Сместились матчи.");
                                        break;
                                    }
                                    break;
                                }
                            }

                            if (element != null) {
                                WebElement matchNameElement = element.findElement(By.cssSelector("a.competitor-name"));
                                String matchNameString = element.findElement(By.cssSelector("a.competitor-name")).getText();

                                String[] split = matchNameString.split(" - ");
                                nameOfMatch = split[0] + " vs " + split[1];
                                System.out.println("Имя матча : " + nameOfMatch);
                                System.out.println("===============================");

                                String currentPeriodString = element.findElement(By.cssSelector("live-score.live-block-scoreboard")).getText();
                                System.out.println("Текущий период матча: " + currentPeriodString);
                                if (currentPeriodString != null) {
                                    int a = currentPeriodString.indexOf("(");

                                    if (a > 0) {
                                        String correctCurrentPeriodString = currentPeriodString.substring(a, currentPeriodString.length());
                                        System.out.println("Правильный счет матча " + correctCurrentPeriodString);

                                        String[] splitPeriod = correctCurrentPeriodString.split(" ");
                                        switch (splitPeriod.length) {
                                            case 1:
                                                currentPeriod = SelectionBet.Q1;
                                                break;
                                            case 2:
                                                currentPeriod = SelectionBet.Q2;
                                                break;
                                            case 3:
                                                currentPeriod = SelectionBet.Q3;
                                                break;
                                            case 4:
                                                currentPeriod = SelectionBet.Q4;
                                                break;
                                        }
                                        System.out.println("Текущий период равен " + splitPeriod.length + ". Тоесть " + currentPeriod);
                                    } else {
                                        currentPeriod = SelectionBet.Q1;
                                    }
                                }

                                try {
                                    WebElement possibleToMakeBetElement = element.findElements(By.cssSelector("main-markets-group")).get(1);
                                    String isPossibleToMakeBetString = possibleToMakeBetElement.findElement(By.cssSelector("tr")).findElement(By.cssSelector("div")).findElement(By.cssSelector("span")).getText();
                                    if (isPossibleToMakeBetString != null) {
                                        if (isPossibleToMakeBetString.length() > 2) {
                                            isPossibleToMakeBet = true;
                                            System.out.println("Возможно делать ставки для матча " + matchNameString);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                matchNameElement.click();
                                Thread.sleep(500);
                                List<WebElement> marketGroups = null;

                                try {
                                    List<WebElement> groupsBetsElements = driver.findElement(By.cssSelector("div.live-block__more-content")).findElements(By.cssSelector("div.event__markets"));
                                    marketGroups = groupsBetsElements.get(groupsBetsElements.size() - 1).findElements(By.cssSelector("div.markets"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (marketGroups != null) {
                                    for (WebElement element1 : marketGroups) {
                                        try {
                                            String totalOddEvenGroupStringHeader = element1.findElement(By.cssSelector("div.markets-header")).findElement(By.cssSelector("span")).getText();
                                            if (totalOddEvenGroupStringHeader != null) {
                                                if (totalOddEvenGroupStringHeader.contains("Total. Even/odd:")) {
                                                    element1.findElement(By.cssSelector("span.outcome__coeff")).click();
                                                    Thread.sleep(500);

                                                    try {
                                                        driver.findElement(By.cssSelector("i.icon-ai-close")).click();
                                                    } catch (Exception e) {
                                                        System.out.println("Нет ставки что бы закрывать.");
                                                    }

                                                    driver.findElement(By.cssSelector("tab[name = 'COUPON']")).click();
                                                    try {
                                                        driver.findElement(By.cssSelector("max-bet.stake-value__btn")).click();
                                                    } catch (NoSuchElementException e) {
                                                        driver.findElement(By.cssSelector("i.icon-ai-close")).click();//TODO to long time to wait here
                                                        break;
                                                    }

                                                    WebElement inputField = driver.findElement(By.cssSelector("input.stake-value__input"));
                                                    String maxBetString = inputField.getAttribute("value");


                                                    if (maxBetString != null) {
                                                        maxBet = Double.parseDouble(maxBetString);
                                                    }

                                                    try {
                                                        driver.findElement(By.cssSelector("tab[name = 'COUPON']")).click();
                                                        List<WebElement> elements = driver.findElements(By.cssSelector("i.icon-ai-close"));
                                                        for (WebElement e : elements) {
                                                            e.click();
                                                            Thread.sleep(500);
                                                        }
                                                    } catch (Exception e) {
                                                        log.info("Проблема во время закрытия прошлых ставок.");
                                                        System.out.println("Проблема во время закрытия прошлых ставок.");
                                                    }

                                                    System.out.println("Максимальная ставка строка равна " + maxBetString);
                                                    System.out.println("Максимальная ставка равна " + maxBet);
                                                    break;
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                driver.findElement(By.cssSelector("a[href='live/overview']")).click();
                                System.out.println("=================================================");
                                System.out.println("=================================================");
                            }

                            MatchLive matchLive;
                            if (currentMatches.containsKey(nameOfMatch.hashCode())) {
                                matchLive = currentMatches.get(nameOfMatch.hashCode());
                                matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
                                if (currentPeriod != null) {
                                    matchLive.setCurrentPeriod(currentPeriod);
                                }
                                if (matchLive.getMaxBet() == 0) {
                                    System.out.println("Обновляем максимальную ставку для матча.");
                                    if (maxBet != null) {
                                        matchLive.setMaxBet(maxBet);
                                    }
                                }
                            } else {
                                //if not, save new instance of MatchLive
//                                matchLive = StartFrame.getSwitcher(this,switcherId).getStorage().addMatchLive(nameOfMatch, isPossibleToMakeBet, 0L, currentPeriod, maxBet);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNameOfSite() {
        return "www.parimatch.com";
    }

    @Override
    public String toString() {
        return "www.parimatch.com";
    }


    @Override
    public void scanMatches(WebDriver driver) {
        try {
            driver.get("https://air.parimatch.com/live");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                driver.findElement(By.cssSelector("span.modal-foot_has-two")).findElement(By.cssSelector("buutton.modal-btn")).click();
            } catch (Exception e) {
                System.out.println("Не было всплывающего сообщения.");
            }

            log.info("Сканируем матчи на сайте " + getNameOfSite() + ". Первое сканирование.");
            driver.findElement(By.cssSelector("a[href='live/overview']")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            try {
                driver.findElement(By.cssSelector("tab[name = 'COUPON']")).click();
                List<WebElement> elements = driver.findElements(By.cssSelector("i.icon-ai-close"));
                for (WebElement e : elements) {
                    e.click();
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                log.info("Проблема во время закрытия прошлых ставок.");
                System.out.println("Проблема во время закрытия прошлых ставок.");
            }


            List<WebElement> groupElements = driver.findElements(By.cssSelector("live-group.live-group"));

            List<WebElement> matchLiveItems = null;
            try {
                for (WebElement element : groupElements) {
                    String text = element.findElement(By.cssSelector("span.live-block-head-title__text")).getText();
                    if (text.compareTo("Basketball") == 0) {
                        matchLiveItems = element.findElements(By.cssSelector("live-block[mixin='live-block-new']"));
                        Actions actions = new Actions(driver);
                        actions.moveToElement(element);
                        actions.perform();
                        break;
                    }
                }
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }

            boolean isMatches = false;
            if (matchLiveItems != null && matchLiveItems.size() > 0) {
                isMatches = true;
            }
            if (isMatches) {
                Map<Integer, MatchLive> currentMatches = null;
//                if (StartFrame.getSwitcher(this,switcherId) != null) {
//                    currentMatches = StartFrame.getSwitcher(this,switcherId).getStorage().getCurrentMatches();
//                }

                if (currentMatches != null) {
                    for (Integer integer : currentMatches.keySet()) {
                        boolean matchFinish = true;
                        MatchLive matchLive = currentMatches.get(integer);
                        System.out.println("Проверяем не закончился ли матч " + matchLive.getNameOfMatch());
                        String s = matchLive.getNameOfMatch().split(" vs ")[0];
                        String s1 = matchLive.getNameOfMatch().split(" vs ")[1];

                        String matchName = null;
                        for (WebElement element : matchLiveItems) {
                            try {
                                matchName = element.findElement(By.cssSelector("a.competitor-name")).getText();
                                if (matchName.contains(s) && matchName.contains(s1)) {
                                    matchFinish = false;
                                    System.out.println("Матч еще есть в списке матчей - " + matchLive.getNameOfMatch());
                                    break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (matchFinish) {
                            if (matchLive.isAlmostFinish()) {
                                matchLive.setFinished(true);
                                log.info("Матч " + matchLive.getNameOfMatch() + " уже окончен.");
                                System.out.println("the match with name " + matchLive.getNameOfMatch() + " already finished....");
                            } else {
                                System.out.println("Возможно матч " + matchLive.getNameOfMatch() + " закончился, первая проверка.");
                                log.info("Возможно матч " + matchLive.getNameOfMatch() + " закончился, первая проверка.");
                                matchLive.setAlmostFinish(true);
                            }
                        } else {
                            matchLive.setAlmostFinish(false);
                        }
                    }
                }

                for (Integer integer : currentMatches.keySet()) {
                    MatchLive matchLive = currentMatches.get(integer);
                    if (matchLive.isFinished()) {
                        matchLive.makeNextEvent(driver);
                    }
                }

                String nameOfMatch = null;
                SelectionBet currentPeriod = null;
                Double maxBet = null;
                boolean isPossibleToMakeBet = true;
                if (matchLiveItems != null) {
                    log.info("Найдено " + matchLiveItems.size() + " матчей.");
                    System.out.println("Найдено " + matchLiveItems.size() + " матчей.");
                    outer:
                    for (int i = 0; i < matchLiveItems.size(); i++) {

//                        if (!StartFrame.getSwitcher(this,switcherId).isAppWork()) {
//                            return;
//                        }

                        try {
                            Thread.sleep(1000);
                            WebElement element = null;
                            groupElements = driver.findElements(By.cssSelector("live-group.live-group"));

                            inner:
                            for (WebElement elementOne : groupElements) {
                                String text = elementOne.findElement(By.cssSelector("span.live-block-head-title__text")).getText();
                                if (text.compareTo("Basketball") == 0) {
                                    try {
                                        element = elementOne.findElements(By.cssSelector("live-block[mixin='live-block-new']")).get(i);
                                    } catch (IndexOutOfBoundsException e) {
                                        System.out.println("Сместились матчи.");
                                        log.info("Сместились матчи.");
                                        break;
                                    }
                                    break;
                                }
                            }

                            if (element != null) {
                                WebElement matchNameElement = element.findElement(By.cssSelector("a.competitor-name"));
                                String matchNameString = element.findElement(By.cssSelector("a.competitor-name")).getText();
                                String[] split = matchNameString.split(" - ");
                                nameOfMatch = split[0] + " vs " + split[1];
                                System.out.println("Имя матча : " + nameOfMatch);
                                System.out.println("===============================");

                                String currentPeriodString = element.findElement(By.cssSelector("live-score.live-block-scoreboard")).getText();
                                System.out.println("Текущий период матча: " + currentPeriodString);
                                if (currentPeriodString != null) {
                                    if (currentPeriodString.contains("(")) {
                                        int a = currentPeriodString.indexOf("(");
                                        String correctCurrentPeriodString = currentPeriodString.substring(a, currentPeriodString.length());
                                        System.out.println("Правильный счет матча " + correctCurrentPeriodString);
                                        String[] splitPeriod = correctCurrentPeriodString.split(" ");
                                        switch (splitPeriod.length) {
                                            case 1:
                                                currentPeriod = SelectionBet.Q1;
                                                break;
                                            case 2:
                                                currentPeriod = SelectionBet.Q2;
                                                break;
                                            case 3:
                                                currentPeriod = SelectionBet.Q3;
                                                break;
                                            case 4:
                                                currentPeriod = SelectionBet.Q4;
                                                break;
                                        }
                                        System.out.println("Текущий период равен " + splitPeriod.length + ". Тоесть " + currentPeriod);
                                    } else {
                                        currentPeriod = SelectionBet.Q1;
                                    }
                                }

                                try {
                                    WebElement possibleToMakeBetElement = element.findElements(By.cssSelector("main-markets-group")).get(1);
                                    String isPossibleToMakeBetString = possibleToMakeBetElement.findElement(By.cssSelector("tr")).findElement(By.cssSelector("div")).findElement(By.cssSelector("span")).getText();
                                    if (isPossibleToMakeBetString != null) {
                                        if (isPossibleToMakeBetString.length() > 2) {
                                            isPossibleToMakeBet = true;
                                            System.out.println("Возможно делать ставки для матча " + matchNameString);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                MatchLive matchLive;
                                if (currentMatches.containsKey(nameOfMatch.hashCode())) {
                                    matchLive = currentMatches.get(nameOfMatch.hashCode());
                                    matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
                                    if (currentPeriod != null) {
                                        matchLive.setCurrentPeriod(currentPeriod);
                                    }

                                    if (matchLive.getMaxBet() == 0) {
                                        System.out.println("Обновляем максимальную ставку для матча.");
                                        maxBet = setMaxBetForMatch(matchNameElement, driver);
                                        if (maxBet != null) {
                                            matchLive.setMaxBet(maxBet);
                                        }
                                    }
                                } else {
                                    //if not, save new instance of MatchLive
                                    maxBet = setMaxBetForMatch(matchNameElement, driver);
//                                    matchLive = StartFrame.getSwitcher(this,switcherId).getStorage().addMatchLive(nameOfMatch, isPossibleToMakeBet, 0L, currentPeriod, maxBet);
                                }

//                                if (matchLive.isPossibleToMakeBet() && !matchLive.isBetIsDone()) {
//                                    System.out.println(" Делаем событие для " + nameOfMatch + ". На сайте " + getNameOfSite());
//                                    log.info("Делаем событие для " + nameOfMatch + ". На сайте " + getNameOfSite());
//                                    matchLive.makeNextEvent(driver);
//
//                                    if (matchLive.isBetIsDone()) {
//                                        if(emergencyMatches.contains(matchLive)){
//                                            emergencyMatches.remove(matchLive);
//                                        }
//                                    }
//                                }

                                if (haveEmergencyMatches) {
                                    for (MatchLive matchLive1 : emergencyMatches) {
                                        makeEmergencyEvent(matchLive1, driver);
                                    }

                                    if (emergencyMatches.size() == 0) {
                                        haveEmergencyMatches = false;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    public Double setMaxBetForMatch(WebElement matchNameElement, WebDriver driver) {
        Double maxBet = null;
        try {
            matchNameElement.click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<WebElement> marketGroups = null;

            try {
                List<WebElement> groupsBetsElements = driver.findElement(By.cssSelector("div.live-block__more-content")).findElements(By.cssSelector("div.event__markets"));
                marketGroups = groupsBetsElements.get(groupsBetsElements.size() - 1).findElements(By.cssSelector("div.markets"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (marketGroups != null) {
                for (WebElement element1 : marketGroups) {
                    try {
                        String totalOddEvenGroupStringHeader = element1.findElement(By.cssSelector("div.markets-header")).findElement(By.cssSelector("span")).getText();
                        if (totalOddEvenGroupStringHeader != null) {
                            if (totalOddEvenGroupStringHeader.contains("Total. Even/odd:")) {
                                element1.findElement(By.cssSelector("span.outcome__coeff")).click();
                                Thread.sleep(1500);

                                driver.findElement(By.cssSelector("tab[name = 'COUPON']")).click();
                                driver.findElement(By.cssSelector("max-bet.stake-value__btn")).click();
                                WebElement inputField = driver.findElement(By.cssSelector("input.stake-value__input"));
                                String maxBetString = inputField.getAttribute("value");
                                driver.findElement(By.cssSelector("i.icon-ai-close")).click();
                                if (maxBetString != null) {
                                    maxBet = Double.parseDouble(maxBetString);
                                }
                                System.out.println("Максимальная ставка строка равна " + maxBetString);
                                System.out.println("Максимальная ставка равна " + maxBet);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    driver.findElement(By.cssSelector("tab[name = 'COUPON']")).click();
                    List<WebElement> elements = driver.findElements(By.cssSelector("i.icon-ai-close"));
                    for (WebElement e : elements) {
                        e.click();
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    log.info("Проблема во время закрытия прошлых ставок.");
                    System.out.println("Проблема во время закрытия прошлых ставок.");
                }

            }
            driver.findElement(By.cssSelector("a[href='live/overview']")).click();
            System.out.println("=================================================");
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }

        return maxBet;
    }

    @Override
    public boolean clickToMatch(WebDriver driver, String nameOfMatch) {
        boolean insideMatch = false;
        try {
            List<WebElement> groupElements = driver.findElements(By.cssSelector("live-group.live-group"));
            List<WebElement> matchLiveItems = null;
            for (WebElement element : groupElements) {
                String text = element.findElement(By.cssSelector("span.live-block-head-title__text")).getText();
                if (text.compareTo("Basketball") == 0) {
                    matchLiveItems = element.findElements(By.cssSelector("live-block[mixin='live-block-new']"));
                    break;
                }
            }

            String[] split = nameOfMatch.split(" vs ");
            String s = split[0];
            String s1 = split[1];

            WebElement matchNameElement;
            String nameFromSite;
            for (WebElement element : matchLiveItems) {
                matchNameElement = element.findElement(By.cssSelector("a.competitor-name"));
                nameFromSite = matchNameElement.getText();
                if (nameFromSite.contains(s) && nameFromSite.contains(s1)) {
                    matchNameElement.click();
                    insideMatch = true;
                    break;
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        return insideMatch;
    }

    @Override
    public void checkGameScore(WebDriver driver, MatchLive matchLive) {
        try {
            String currentPeriodString = driver.findElement(By.cssSelector("div.all-score")).getText();
            String totalScore = null;
            if (currentPeriodString != null) {
                totalScore = currentPeriodString.replace(" ", ", ");
                totalScore = totalScore.replace("-", ":");
            } else {
                return;
            }
            log.info("Результаты матча " + matchLive.getNameOfMatch() + " - " + totalScore);
            System.out.println("Total score is: " + totalScore);
            Map<SelectionBet, Integer> matchScoreStatistic = matchLive.getMatchScoreStatistic();
            if (totalScore.length() > 4) {
                String[] split = totalScore.split(",");
                for (int i = 0; i < split.length; i++) {
                    if (i == 0) {
                        String substring;
                        if (split.length > 1) {
                            substring = split[0].substring(1, split[0].length());
                        } else {
                            substring = split[0].substring(1, split[0].length() - 1);
                        }
                        System.out.println("The substring for first part is: " + substring);
                        try {
                            int a = Integer.parseInt(substring.split(":")[0]);
                            int a1 = Integer.parseInt(substring.split(":")[1]);
                            matchScoreStatistic.put(SelectionBet.Q1, a + a1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (i == 1) {
                        if (split.length > 2) {
                            String substring = split[1].substring(1, split[1].length());
                            System.out.println("The substring for second part is: " + substring);
                            try {
                                int a = Integer.parseInt(substring.split(":")[0]);
                                int a1 = Integer.parseInt(substring.split(":")[1]);
                                matchScoreStatistic.put(SelectionBet.Q2, a + a1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            String substring = split[1].substring(1, split[1].length() - 1);
                            System.out.println(substring);
                            try {
                                int a = Integer.parseInt(substring.split(":")[0]);
                                int a1 = Integer.parseInt(substring.split(":")[1]);
                                matchScoreStatistic.put(SelectionBet.Q2, a + a1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (i == 2) {
                        if (split.length > 3) {
                            String substring = split[2].substring(1, split[2].length());
                            System.out.println("The substring for third part is: " + substring);
                            try {
                                int a = Integer.parseInt(substring.split(":")[0]);
                                int a1 = Integer.parseInt(substring.split(":")[1]);
                                matchScoreStatistic.put(SelectionBet.Q3, a + a1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            String substring = split[2].substring(1, split[2].length() - 1);
                            System.out.println(substring);
                            try {
                                int a = Integer.parseInt(substring.split(":")[0]);
                                int a1 = Integer.parseInt(substring.split(":")[1]);
                                matchScoreStatistic.put(SelectionBet.Q3, a + a1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (i == 3) {
                        String substring = split[3].substring(1, split[3].length() - 1);
                        System.out.println("The substring for fourth part is: " + substring);
                        try {
                            int a = Integer.parseInt(substring.split(":")[0]);
                            int a1 = Integer.parseInt(substring.split(":")[1]);
                            matchScoreStatistic.put(SelectionBet.Q4, a + a1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (matchScoreStatistic.get(SelectionBet.Q1) != null && matchScoreStatistic.get(SelectionBet.Q2) != null) {
                matchScoreStatistic.put(SelectionBet.Half1st, matchScoreStatistic.get(SelectionBet.Q1) + matchScoreStatistic.get(SelectionBet.Q2));
            }

            for (SelectionBet selectionBet : matchLive.getMatchScoreStatistic().keySet()) {
                Integer score = matchLive.getMatchScoreStatistic().get(selectionBet);
                if (score == 0) {
                    continue;
                } else if (score % 2 == 0) {
                    matchLive.getMatchOddEvenStatistic().put(selectionBet, OddEven.Even);//Even
                } else if (score % 2 == 1) {
                    matchLive.getMatchOddEvenStatistic().put(selectionBet, OddEven.Odd);//Odd
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            System.out.println("First quarter////");
        }
    }

    @Override
    public void checkMaximumBetSizeAndPossibleBetSelection(WebDriver driver, MatchLive matchLive) {
        try {
            List<WebElement> marketGroups = null;
            try {
                List<WebElement> groupsBetsElements = driver.findElement(By.cssSelector("div.live-block__more-content")).findElements(By.cssSelector("div.event__markets"));
                marketGroups = groupsBetsElements.get(groupsBetsElements.size() - 1).findElements(By.cssSelector("div.markets"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (matchLive.getMaxBet() == 0.0) {
                if (marketGroups != null) {
                    for (WebElement element1 : marketGroups) {
                        try {
                            String totalOddEvenGroupStringHeader = element1.findElement(By.cssSelector("div.markets-header")).findElement(By.cssSelector("span")).getText();
                            if (totalOddEvenGroupStringHeader != null) {
                                if (totalOddEvenGroupStringHeader.contains("Total. Even/odd:")) {
                                    element1.findElement(By.cssSelector("span.outcome__coeff")).click();
                                    Thread.sleep(1500);

                                    driver.findElement(By.cssSelector("tab[name = 'COUPON']")).click();
                                    driver.findElement(By.cssSelector("max-bet.stake-value__btn")).click();
                                    WebElement inputField = driver.findElement(By.cssSelector("input.stake-value__input"));
                                    String maxBetString = inputField.getAttribute("value");
                                    driver.findElement(By.cssSelector("i.icon-ai-close")).click();
                                    if (maxBetString != null) {
                                        Double maxBet = Double.parseDouble(maxBetString);
                                        matchLive.setMaxBet(maxBet);
                                    }
                                    System.out.println("Максимальная ставка для  матча " + matchLive.getNameOfMatch() + " установлена " + maxBetString);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


            matchLive.getPossibleBetSelectionBet().clear();
            if (marketGroups != null) {
                for (WebElement element1 : marketGroups) {
                    try {
                        String totalOddEvenGroupStringHeader = element1.findElement(By.cssSelector("div.markets-header")).findElement(By.cssSelector("span")).getText();
                        if (totalOddEvenGroupStringHeader != null) {
                            if (totalOddEvenGroupStringHeader.contains("Total. Even/odd:")) {
                                matchLive.getPossibleBetSelectionBet().add(SelectionBet.Match);
                            }

                            if (totalOddEvenGroupStringHeader.contains("Total. Even/odd. 4th quarter:")) {
                                matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q4);
                            }

                            if (totalOddEvenGroupStringHeader.contains("Total. Even/odd. 3rd quarter:")) {
                                matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q3);
                            }

                            if (totalOddEvenGroupStringHeader.contains("Total. Even/odd. 2nd quarter:")) {
                                matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q2);
                            }

                            if (totalOddEvenGroupStringHeader.contains("Total. Even/odd. 1st quarter:")) {
                                matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q1);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void goBackToOverview(WebDriver driver) {
        try {
            driver.findElement(By.cssSelector("a[href='live/overview']")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getMinBet(WebDriver driver) {
        double minBet = 1.0;
        if(minBet > StartFrame.getMinBet()){
            return minBet;
        } else {
            return StartFrame.getMinBet();
        }
    }

    @Override
    public void setActiveHistory(boolean isActive) {
        this.isActiveHistory = isActive;
    }

    @Override
    public boolean isActiveHistory() {
        return isActiveHistory;
    }

    @Override
    public void setActiveDetails(boolean isActive) {
        this.isActiveDetails = isActive;
    }

    @Override
    public boolean isActiveDetails() {
        return isActiveDetails;
    }

    @Override
    public void addEmergencyEvent(MatchLive matchLive) {
        if (!emergencyMatches.contains(matchLive)) {
            emergencyMatches.add(matchLive);
        }
        haveEmergencyMatches = true;
    }

    @Override
    public void makeEmergencyEvent(MatchLive matchLive, WebDriver driver) {
        try {

            System.out.println("Делаем срочное событие для матча "+matchLive.getNameOfMatch());
            log.info("Делаем срочное событие для матча "+matchLive.getNameOfMatch());

            driver.get("https://air.parimatch.com/live");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                driver.findElement(By.cssSelector("span.modal-foot_has-two")).findElement(By.cssSelector("buutton.modal-btn")).click();
            } catch (Exception e) {
                System.out.println("Не было всплывающего сообщения.");
            }

            log.info("Сканируем матчи на сайте " + getNameOfSite() + ". Первое сканирование.");
            driver.findElement(By.cssSelector("a[href='live/overview']")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            try {
                driver.findElement(By.cssSelector("tab[name = 'COUPON']")).click();
                List<WebElement> elements = driver.findElements(By.cssSelector("i.icon-ai-close"));
                for (WebElement e : elements) {
                    e.click();
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                log.info("Проблема во время закрытия прошлых ставок.");
                System.out.println("Проблема во время закрытия прошлых ставок.");
            }


            List<WebElement> groupElements = driver.findElements(By.cssSelector("live-group.live-group"));

            List<WebElement> matchLiveItems = null;
            try {
                for (WebElement element : groupElements) {
                    String text = element.findElement(By.cssSelector("span.live-block-head-title__text")).getText();
                    if (text.compareTo("Basketball") == 0) {

                        matchLiveItems = element.findElements(By.cssSelector("live-block[mixin='live-block-new']"));
                        Actions actions = new Actions(driver);
                        actions.moveToElement(element);
                        actions.perform();
                        break;
                    }
                }
            } catch (NoSuchElementException e) {
                e.printStackTrace();
            }

            boolean isMatches = false;
            if (matchLiveItems != null && matchLiveItems.size() > 0) {
                isMatches = true;
            }
            if (isMatches) {
                String nameOfMatch = null;
                SelectionBet currentPeriod = null;
                Double maxBet = null;
                boolean isPossibleToMakeBet = true;
                log.info("Найдено " + matchLiveItems.size() + " матчей.");
                System.out.println("Найдено " + matchLiveItems.size() + " матчей.");
                for (int i = 0; i < matchLiveItems.size(); i++) {

//                    if (!StartFrame.getSwitcher(this,switcherId).isAppWork()) {
//                        return;
//                    }

                    try {
                        Thread.sleep(1000);
                        WebElement element = null;
                        groupElements = driver.findElements(By.cssSelector("live-group.live-group"));

                        for (WebElement elementOne : groupElements) {
                            String text = elementOne.findElement(By.cssSelector("span.live-block-head-title__text")).getText();
                            if (text.compareTo("Basketball") == 0) {
                                try {
                                    element = elementOne.findElements(By.cssSelector("live-block[mixin='live-block-new']")).get(i);
                                } catch (IndexOutOfBoundsException e) {
                                    System.out.println("Сместились матчи.");
                                    log.info("Сместились матчи.");
                                    break;
                                }
                                break;
                            }
                        }

                        if (element != null) {


                            String nameOfEmergencyMatch = matchLive.getNameOfMatch();

                            WebElement matchNameElement = element.findElement(By.cssSelector("a.competitor-name"));
                            String matchNameString = element.findElement(By.cssSelector("a.competitor-name")).getText();
                            String[] split = matchNameString.split(" - ");

                            if (nameOfEmergencyMatch.contains(split[0]) && nameOfEmergencyMatch.contains(split[1])) {


                            }

                            String currentPeriodString = element.findElement(By.cssSelector("live-score.live-block-scoreboard")).getText();
                            System.out.println("Текущий период матча: " + currentPeriodString);
                            if (currentPeriodString != null) {
                                if (currentPeriodString.contains("(")) {
                                    int a = currentPeriodString.indexOf("(");
                                    String correctCurrentPeriodString = currentPeriodString.substring(a, currentPeriodString.length());
                                    System.out.println("Правильный счет матча " + correctCurrentPeriodString);
                                    String[] splitPeriod = correctCurrentPeriodString.split(" ");
                                    switch (splitPeriod.length) {
                                        case 1:
                                            currentPeriod = SelectionBet.Q1;
                                            break;
                                        case 2:
                                            currentPeriod = SelectionBet.Q2;
                                            break;
                                        case 3:
                                            currentPeriod = SelectionBet.Q3;
                                            break;
                                        case 4:
                                            currentPeriod = SelectionBet.Q4;
                                            break;
                                    }
                                    System.out.println("Текущий период равен " + splitPeriod.length + ". Тоесть " + currentPeriod);
                                } else {
                                    currentPeriod = SelectionBet.Q1;
                                }
                            }

                            try {
                                WebElement possibleToMakeBetElement = element.findElements(By.cssSelector("main-markets-group")).get(1);
                                String isPossibleToMakeBetString = possibleToMakeBetElement.findElement(By.cssSelector("tr")).findElement(By.cssSelector("div")).findElement(By.cssSelector("span")).getText();
                                if (isPossibleToMakeBetString != null) {
                                    if (isPossibleToMakeBetString.length() > 2) {
                                        isPossibleToMakeBet = true;
                                        System.out.println("Возможно делать ставки для матча " + matchNameString);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
                            if (currentPeriod != null) {
                                matchLive.setCurrentPeriod(currentPeriod);
                            }

                            if (matchLive.getMaxBet() == 0) {
                                System.out.println("Обновляем максимальную ставку для матча.");
                                maxBet = setMaxBetForMatch(matchNameElement, driver);
                                if (maxBet != null) {
                                    matchLive.setMaxBet(maxBet);
                                }
                            }
                            if (matchLive.isPossibleToMakeBet() && !matchLive.isBetIsDone()) {
                                System.out.println(" Делаем событие для " + nameOfMatch + ". На сайте " + getNameOfSite());
                                log.info("Делаем событие для " + nameOfMatch + ". На сайте " + getNameOfSite());
                                matchLive.makeNextEvent(driver);

                                if (matchLive.isBetIsDone()) {
                                    emergencyMatches.remove(matchLive);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSwitcherId(int switcherId) {
        this.switcherId = switcherId;
    }

    @Override
    public int getSwitcherId() {
        return switcherId;
    }

    @Override
    public void setAppender(Appender log) {
        this.appender = log;
        this.log = Logger.getLogger(ParserPariMatch.class);
        this.log.addAppender(appender);
    }

    @Override
    public Appender getAppender() {
        return appender;
    }
}

