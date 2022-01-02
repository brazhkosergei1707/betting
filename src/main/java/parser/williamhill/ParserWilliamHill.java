package parser.williamhill;

import UI.startFrame.StartFrame;
import betting.entity.Bet;
import entityes.MatchLive;
import betting.enumeration.OddEven;
import betting.enumeration.SelectionBet;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import parser.Parser;

import java.util.*;

public class ParserWilliamHill implements Parser {
    private Logger log;
    private Appender appender;

    private int switcherId;
    private boolean isActiveHistory = false;
    private boolean isActiveDetails = false;

    private boolean haveEmergencyMatches = false;
    List<MatchLive> emergencyMatches = new ArrayList<>();
    Random random = new Random();

    @Override
    public Bet checkBet(Bet bet, WebDriver driver) {
        System.out.println("Проверяем ставку на вильям хилл");
        try {
            driver.get("https://sports.williamhill.com/acc/en-gb?action=GoHistory");
            Thread.sleep(2000);
            driver.findElement(By.id("stateSettledBets")).click();
            Thread.sleep(1000);
            driver.findElement(By.cssSelector("input.btn")).click();
            Thread.sleep(1000);

            List<WebElement> bets = null;
            try {
                bets = driver.findElement(By.cssSelector("table.tableData")).findElement(By.cssSelector("tbody")).findElements(By.cssSelector("tr"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Нет ставок.");
            }
            if (bets != null) {
                String dateString;
                String nameOfMatch = null;
                OddEven oddEven = null;
                SelectionBet selectionBet = null;
                Boolean isWin = null;
                for (WebElement betElement : bets) {
                    if (betElement.findElements(By.cssSelector("td")).size() == 0) {
                        continue;
                    }
                    dateString = betElement.findElements(By.cssSelector("td")).get(4).getText();
                    if (dateString != null) {
                        String[] split = dateString.split("  –  ");

                        if(split[0].contains("  v  ")){
                            nameOfMatch = split[0].replace("  v  ", " vs ");
                        }else if(split[0].contains(" v ")){
                            nameOfMatch = split[0].replace(" v ", " vs ");
                        }

                        nameOfMatch = correctString(nameOfMatch);


                        String selectionBetString = split[1];
                        if (selectionBetString != null) {
                            if (selectionBetString.contains("1st Quarter")) {
                                selectionBet = SelectionBet.Q1;
                            } else if (selectionBetString.contains("2nd Quarter")) {
                                selectionBet = SelectionBet.Q2;
                            } else if (selectionBetString.contains("3rd Quarter")) {
                                selectionBet = SelectionBet.Q3;
                            } else if (selectionBetString.contains("4th Quarter")) {
                                selectionBet = SelectionBet.Q4;
                            } else if (selectionBetString.contains("1st Half")) {
                                selectionBet = SelectionBet.Half1st;
                            } else if (selectionBetString.contains("2nd Half")) {
                                selectionBet = SelectionBet.Half2nd;
                            } else {
                                selectionBet = SelectionBet.Match;
                            }
                        } else {
                            continue;
                        }
                    }
                    dateString = null;
                    dateString = betElement.findElements(By.cssSelector("td")).get(5).getText();
                    if (dateString != null) {
                        if (dateString.contains("Odd")) {
                            oddEven = OddEven.Odd;
                        } else {
                            oddEven = OddEven.Even;
                        }
                    }

                    dateString = null;
                    dateString = betElement.findElements(By.cssSelector("td")).get(6).getText();

                    if (dateString != null) {
                        if (dateString.contains("Lost")) {
                            isWin = false;
                        } else {
                            isWin = true;
                        }
                    }

                    if (isWin != null) {
                        if (bet.getMatchName().contains(nameOfMatch)) {
                            if (selectionBet == bet.getSelectionBet()) {
                                bet.setWin(isWin);
                                System.out.println("Bet is setted win to " + isWin);
                            }
                        }
                    }

                    if (bet.getWin() != null) {
                        break;
                    }
                }

                if(bet.getWin()==null){
                    driver.findElement(By.cssSelector("option[value='YESTERDAY']")).click();
                    Thread.sleep(1000);
                    driver.findElement(By.cssSelector("input.btn")).click();
                    Thread.sleep(1000);
                    try {
                        bets = driver.findElement(By.cssSelector("table.tableData")).findElement(By.cssSelector("tbody")).findElements(By.cssSelector("tr"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Нет ставок.");
                    }
                    if (bets != null) {
                        for (WebElement betElement : bets) {
                            if (betElement.findElements(By.cssSelector("td")).size() == 0) {
                                continue;
                            }
                            dateString = betElement.findElements(By.cssSelector("td")).get(4).getText();
                            if (dateString != null) {
                                String[] split = dateString.split("  –  ");
                                nameOfMatch = split[0].replace(" v ", " vs ");

                                String selectionBetString = split[1];
                                if (selectionBetString != null) {
                                    if (selectionBetString.contains("1st Quarter")) {
                                        selectionBet = SelectionBet.Q1;
                                    } else if (selectionBetString.contains("2nd Quarter")) {
                                        selectionBet = SelectionBet.Q2;
                                    } else if (selectionBetString.contains("3rd Quarter")) {
                                        selectionBet = SelectionBet.Q3;
                                    } else if (selectionBetString.contains("4th Quarter")) {
                                        selectionBet = SelectionBet.Q4;
                                    } else if (selectionBetString.contains("1st Half")) {
                                        selectionBet = SelectionBet.Half1st;
                                    } else if (selectionBetString.contains("2nd Half")) {
                                        selectionBet = SelectionBet.Half2nd;
                                    } else {
                                        selectionBet = SelectionBet.Match;
                                    }
                                } else {
                                    continue;
                                }
                            }
                            dateString = null;
                            dateString = betElement.findElements(By.cssSelector("td")).get(5).getText();
                            if (dateString != null) {
                                if (dateString.contains("Odd")) {
                                    oddEven = OddEven.Odd;
                                } else {
                                    oddEven = OddEven.Even;
                                }
                            }

                            dateString = null;
                            dateString = betElement.findElements(By.cssSelector("td")).get(6).getText();

                            if (dateString != null) {
                                if (dateString.contains("Lost")) {
                                    isWin = false;
                                } else {
                                    isWin = true;
                                }
                            }

                            if (isWin != null) {
                                if (bet.getMatchName().contains(nameOfMatch)) {
                                    if (selectionBet == bet.getSelectionBet()) {
                                        bet.setWin(isWin);
                                        System.out.println("Bet is setted win to " + isWin);
                                    }
                                }
                            }

                            if (bet.getWin() != null) {
                                break;
                            }
                        }
                    }
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

        if (bet.getMatchName() == null) {
            bet.setMatchName(matchLive.getNameOfMatch());
            System.out.println("Chain did not set the match name.");
        }

        boolean betAlreadyNotDone;
        Boolean b1 = betAlreadyNotPlayed(bet, driver);
        if (b1 != null) {
            betAlreadyNotDone = b1;
        } else {
            betAlreadyNotDone = true;
        }

        try {
            System.out.println("Закрываем прошлые ставки.");
            driver.findElement(By.xpath("//a[text()='Clear Slip']")).click();
        } catch (Exception e) {
            System.out.println("Ставок не было.");
        }
        if (betAlreadyNotDone) {
            try {
                String text = null;
                switch (bet.getSelectionBet()) {
                    case Q1:
                        text = "1st Quarter Total Points Odd/Even";
                        break;
                    case Q2:
                        text = "2nd Quarter Total Points Odd/Even";
                        break;
                    case Q3:
                        text = "3rd Quarter Total Points Odd/Even";
                        break;
                    case Q4:
                        text = "4th Quarter Total Points Odd/Even";
                        break;
                    case Half1st:
                        text = "1st Half Total Points Odd/Even";
                        break;
                    case Half2nd:
                        text = "2nd Half Total Points Odd/Even";
                        break;
//                    case Match:text="Total Points Odd/Even";
//                        break;
                }
                System.out.println("Текст равен - " + text);

                if (text != null) {
                    List<WebElement> elements = driver.findElements(By.cssSelector("div.marketHolderExpanded"));
                    System.out.println("Размер коллекции элементов  " + elements.size());

                    for (WebElement e : elements) {
                        String span = e.findElement(By.cssSelector("span")).getText();
                        System.out.println("Текст с страницы равен -  " + span);
                        if (span.contains(text)) {
                            List<WebElement> oddEvenElements = e.findElement(By.cssSelector("tbody")).findElements(By.cssSelector("td"));
                            if (bet.getOddEven() == OddEven.Odd) {
                                oddEvenElements.get(0).click();
                            } else {
                                oddEvenElements.get(1).click();
                            }

                            WebElement inputField = driver.findElement(By.cssSelector("div.slipStake")).findElement(By.cssSelector("input"));
                            inputField.click();
                            inputField.clear();
                            Thread.sleep(1000);
                            inputField.sendKeys(String.valueOf(bet.getPrice()));
                            Thread.sleep(2000);
                            driver.findElement(By.id("slipBtnPlaceBet")).click();
                            Thread.sleep(4000);
                            betAlreadyNotDone = false;
                            break;
                        }
                    }
                }

                Boolean b = betAlreadyNotPlayed(bet, driver);
                if (b != null) {
                    System.out.println("Еще раз проверили ставку. результат должен быть false - " + b);
                    betAlreadyNotDone = b;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!betAlreadyNotDone) {
            log.info("Ставка сделана, время ставки " + new Date(System.currentTimeMillis()));
            System.out.println("Ставка сделана, время ставки " + new Date(System.currentTimeMillis()));
            bet.setTimeBet(System.currentTimeMillis());
        }
//        if (bet.getPrice() != 0.0) {
//            if (bet.getSelectionBet() != null) {
//                if (bet.getOddEven() != null) {
//                    bet.setMatchPeriod(matchLive.getCurrentPeriodInt());
//                    bet.setTimeBet(1L);
//                }
//            }
//        }
        return bet;
    }

//=======================================================================
//=======================================================================
//=======================================================================
//=======================================================================


    public boolean logIn(String username, String password, WebDriver driver) {
        try {
            driver.get("http://sports.williamhill.com/bet/en-gb");
            Thread.sleep(5000);

            try {
                driver.findElement(By.id("yesBtn")).click();
            } catch (Exception e) {
                System.out.println("Was not button.");
                e.printStackTrace();
            }

            WebElement userName = driver.findElement(By.cssSelector("input[name='tmp_username']"));
            userName.click();
            Thread.sleep(500);
            driver.findElement(By.id("username")).sendKeys(username);//TODO scroll to this element
            Thread.sleep(500);
            driver.findElement(By.id("tmp_password")).click();
            Thread.sleep(500);
            driver.findElement(By.id("password")).sendKeys(password);
            Thread.sleep(1000);
            driver.findElement(By.id("signInBtn")).click();
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean logOut(WebDriver driver) {
        if (driver != null) {
            try {
                driver.findElement(By.cssSelector("a.signOutLink")).click();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void setCurrentTimeAndCount(WebDriver driver) {
//        if (StartFrame.getSwitcher(this,switcherId) != null) {
//            StartFrame.getSwitcher(this,switcherId).getStorage().setCurrentTime(System.currentTimeMillis());
//        }
//
//        try {
//            String balanceString = driver.findElement(By.cssSelector("span.mainBalanceValue")).getText();
//            if (StartFrame.getSwitcher(this,switcherId) != null) {
//                StartFrame.getSwitcher(this,switcherId).getStorage().setCurrentBalance(balanceDouble(balanceString));
//            }
//            StartFrame.getSwitcher(this,switcherId).getStorage().setCurrentBalanceString(balanceString);
//        } catch (NoSuchElementException e) {
//            e.printStackTrace();
//        }
    }

    private Double balanceDouble(String balanceString) {
        Double balance = null;
        if (balanceString.contains("€")) {
            balanceString = balanceString.replace("€", "");
        } else if (balanceString.contains("$")) {
            balanceString = balanceString.replace("$", "");
        }
        try {
            balance = Double.parseDouble(balanceString);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Проблема парсинга баланса. Строка баланса - " + balanceString);
        }

        return balance;
    }

    @Override
    public void updateHistoryBet(WebDriver driver) {
        System.out.println("Обновляем историю сообщений.");
        try {
            driver.get("https://sports.williamhill.com/acc/en-gb?action=GoHistory");
            Thread.sleep(2000);
            driver.findElement(By.id("stateSettledBets")).click();
            Thread.sleep(1000);
            driver.findElement(By.cssSelector("input.btn")).click();
            Thread.sleep(1000);

            List<WebElement> bets = null;
            try {
                bets = driver.findElement(By.cssSelector("table.tableData")).findElement(By.cssSelector("tbody")).findElements(By.cssSelector("tr"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Нет ставок.");
            }
            if (bets != null) {
                List<Bet> currentList = null;//StartFrame.getSwitcher(this,switcherId).getStorage().getHistoryBets();
                Bet bet;
                long dateOfBet = 0L;
                String dateString = null;
                Date date = null;
                String nameOfMatch = null;
                OddEven oddEven = null;
                SelectionBet selectionBet = null;
                int periodBetInt = 0;
                Double priceOfBet = 0.0;
                Boolean isWin = null;
                for (WebElement betElement : bets) {
                    if (betElement.findElements(By.cssSelector("td")).size() == 0) {
                        continue;
                    }
                    dateString = betElement.findElements(By.cssSelector("td")).get(4).getText();
                    if (dateString != null) {
                        String[] split = dateString.split("  –  ");
                        nameOfMatch = split[0].replace(" v ", " vs ");

                        String selectionBetString = split[1];
                        if (selectionBetString != null) {
                            if (selectionBetString.contains("1st Quarter")) {
                                selectionBet = SelectionBet.Q1;
                                periodBetInt = 1;
                            } else if (selectionBetString.contains("2nd Quarter")) {
                                selectionBet = SelectionBet.Q2;
                                periodBetInt = 2;
                            } else if (selectionBetString.contains("3rd Quarter")) {
                                selectionBet = SelectionBet.Q3;
                                periodBetInt = 3;
                            } else if (selectionBetString.contains("4th Quarter")) {
                                selectionBet = SelectionBet.Q4;
                                periodBetInt = 4;
                            } else if (selectionBetString.contains("1st Half")) {
                                selectionBet = SelectionBet.Half1st;
                                periodBetInt = 2;
                            } else if (selectionBetString.contains("2nd Half")) {
                                selectionBet = SelectionBet.Half2nd;
                                periodBetInt = 4;
                            } else {
                                selectionBet = SelectionBet.Match;
                                periodBetInt = 4;
                            }
                        } else {
                            continue;
                        }
                    }
                    dateString = null;
                    dateString = betElement.findElements(By.cssSelector("td")).get(5).getText();
                    if (dateString != null) {
                        if (dateString.contains("Odd")) {
                            oddEven = OddEven.Odd;
                        } else {
                            oddEven = OddEven.Even;
                        }
                    }

                    dateString = null;
                    dateString = betElement.findElements(By.cssSelector("td")).get(6).getText();

                    if (dateString.contains("Lost")) {
                        isWin = false;
                    } else {
                        isWin = true;
                    }

                    dateString = null;
                    dateString = betElement.findElements(By.cssSelector("td")).get(7).getText();
                    priceOfBet = balanceDouble(dateString);

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
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateCurrentMatchesLive(WebDriver driver) {
        try {
            driver.get("http://sports.williamhill.com/bet/en-gb/betlive/27");
            List<WebElement> matchesElements = null;
            boolean isMatchesNow = false;
            try {
                matchesElements = driver.findElements(By.cssSelector("tr.rowLive"));
                isMatchesNow = true;
            } catch (Exception e) {
                System.out.println("Нет матчей сейчас.");
            }

            if (isMatchesNow) {
                Map<Integer, MatchLive> currentMatches = null;//StartFrame.getSwitcher(this,switcherId).getStorage().getCurrentMatches();
                String nameOfMatch = null;
                SelectionBet currentPeriod = null;
                Double maxBet = null;
                boolean isPossibleToMakeBet = true;
                for (WebElement element : matchesElements) {
                    String matchNameString = element.findElement(By.cssSelector("td.CentrePad")).findElement(By.cssSelector("a")).getText();

                    if (matchNameString != null) {
                        String[] split;

                        if (matchNameString.contains("   v   ")) {
                            split = matchNameString.split("   v   ");
                        }else  if (matchNameString.contains("  v  ")) {
                            split = matchNameString.split("  v  ");
                        } else {
                            split = matchNameString.split(" v ");
                        }
                        String firstTeamName = correctString(split[0]);
                        String secondTeamName = correctString(split[1]);
                        nameOfMatch = firstTeamName + " vs " + secondTeamName;
                    }

                    String currentPeriodString = element.findElement(By.cssSelector("td")).findElement(By.cssSelector("a")).getText();
                    switch (currentPeriodString) {//OT
                        case "1st":
                            currentPeriod = SelectionBet.Q1;
                            break;
                        case "2nd":
                            currentPeriod = SelectionBet.Q2;
                            break;
                        case "3rd":
                            currentPeriod = SelectionBet.Q3;
                            break;
                        case "4th":
                            currentPeriod = SelectionBet.Q4;
                            break;
                        case "Live":
                            currentPeriod = SelectionBet.Q1;
                            break;
                    }

                    WebElement makeBetElement = element.findElement(By.cssSelector("div.eventprice"));
                    try {
                        makeBetElement.click();
                        Thread.sleep(2000);
                        isPossibleToMakeBet = true;
                        driver.findElement(By.xpath("//a[text()='Clear Slip']")).click();
                        Thread.sleep(2000);
                    } catch (ElementNotVisibleException e) {
                        isPossibleToMakeBet = false;
                    }

                    System.out.println("====================================");
                    System.out.println(matchNameString);
                    System.out.println(currentPeriodString);
                    System.out.println("========Correct Data===========");
                    System.out.println(nameOfMatch);
                    System.out.println(currentPeriod);
                    System.out.println(isPossibleToMakeBet);
                    System.out.println("====================================");

                    MatchLive matchLive;

                    //if match already in map, update it
                    if (currentMatches.containsKey(nameOfMatch.hashCode())) {
                        matchLive = currentMatches.get(nameOfMatch.hashCode());
                        matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
                        matchLive.setCurrentPeriod(currentPeriod);

                    } else {
                        log.info("Добавляем новый матч " + nameOfMatch + ", время ко конца периода: " + 0L + ", текущий период: " + currentPeriod);
//                        matchLive = StartFrame.getSwitcher(this,switcherId).getStorage().addMatchLive(nameOfMatch, isPossibleToMakeBet, 0L, currentPeriod, maxBet);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String correctString(String oldString) {

        if(oldString.contains("(W)")){
            oldString=oldString.replace("(W)","Women");
        }

        if (oldString.contains("(")) {
            String newString = null;
            int firstChar = oldString.indexOf("(");
            int lastChar = oldString.indexOf(")");
            String toDel = oldString.substring(firstChar - 1, lastChar + 1);
            oldString = oldString.replace(toDel, "");

            if (oldString.contains("(")) {
                oldString = correctString(oldString);
            }
        }
        return oldString;
    }
    @Override
    public void updateCurrentBet(WebDriver driver) {
        try {
            boolean isBets = false;
            driver.findElement(By.id("open_bet_count")).click();
            Thread.sleep(1000);
            WebElement slipopenBets = driver.findElement(By.id("slipopenBets"));
            List<WebElement> betElements = null;
            try {
                betElements = slipopenBets.findElements(By.cssSelector("div.cashout_noCash"));
                isBets = true;
            } catch (Exception e) {
                System.out.println("Сейчас нет текущих ставок.");
            }

            if (betElements != null && isBets && betElements.size() > 0) {
                List<Bet> list = null;//StartFrame.getSwitcher(this,switcherId).getStorage().getCurrentBets();
                Bet bet;
                String nameOfMatch = null;
                OddEven oddEven = null;
                SelectionBet selectionBet = null;
                int periodBetInt = 0;
                double priceOfBet = 0.0;
                String priceOfBetString = null;

                System.out.println("Найдено ставок - " + betElements.size());

                for (WebElement betElement : betElements) {
                    nameOfMatch = betElement.findElement(By.cssSelector("p.cashout_link")).findElement(By.cssSelector("a")).getText();
                    System.out.println("Первое имя матча - " + nameOfMatch);

                    if (nameOfMatch.contains("  v  ")) {
                        nameOfMatch = nameOfMatch.replace("  v  ", " vs ");
                    } else if (nameOfMatch.contains(" v ")) {
                        nameOfMatch = nameOfMatch.replace(" v ", " vs ");
                    }

                    nameOfMatch = correctString(nameOfMatch);

                    String oddEvenString = betElement.findElement(By.cssSelector("span.cashout_col1")).findElement(By.cssSelector("strong")).getText();
                    if (oddEvenString != null) {
                        if (oddEvenString.contains("Odd")) {
                            oddEven = OddEven.Odd;
                        } else {
                            oddEven = OddEven.Even;
                        }
                    }

                    String selectionBetString = betElement.findElement(By.cssSelector("span.cashout_col2")).getText();

                    System.out.println("Выбор ставки с сайта " + selectionBetString);


                    if (selectionBetString != null) {
                        if (selectionBetString.contains("1st Quarter")) {
                            selectionBet = SelectionBet.Q1;
                            periodBetInt = 1;
                        } else if (selectionBetString.contains("2nd Quarter")) {
                            selectionBet = SelectionBet.Q2;
                            periodBetInt = 2;
                        } else if (selectionBetString.contains("3rd Quarter")) {
                            selectionBet = SelectionBet.Q3;
                            periodBetInt = 3;
                        } else if (selectionBetString.contains("4th Quarter")) {
                            selectionBet = SelectionBet.Q4;
                            periodBetInt = 4;
                        } else if (selectionBetString.contains("1st Half")) {
                            selectionBet = SelectionBet.Half1st;
                            periodBetInt = 2;
                        } else if (selectionBetString.contains("2nd Half")) {
                            selectionBet = SelectionBet.Half2nd;
                            periodBetInt = 4;
                        } else {
                            selectionBet = SelectionBet.Match;
                            periodBetInt = 4;
                        }// 1st Half 2nd Half   3rd Quarter   4th Quarter   Total Points
                    }

                    priceOfBetString = betElement.findElement(By.cssSelector("span.cashout_left")).getAttribute("data-val");

                    try {
                        priceOfBet = Double.parseDouble(priceOfBetString);
                    } catch (Exception e) {
                        System.out.println("Проблемы во время парсинга цены ставки.");
                    }

                    System.out.println(nameOfMatch);
                    System.out.println(selectionBet);
                    System.out.println(oddEven);
                    System.out.println(priceOfBet);
                    System.out.println("========================================");

                    boolean newBet = true;
                    for (Bet betList : list) {
                        if (nameOfMatch.contains(betList.getMatchName().split(" vs ")[0])) {
                            if (selectionBet == betList.getSelectionBet()) {
                                System.out.println("Bet for match: " + nameOfMatch + " is finded...");
                                betList.setPlay(true);
                                if (betList.getPrice() == 0.0 || betList.getPrice() == null) {
                                    betList.setPrice(priceOfBet);
                                }
                                newBet = false;
                            }
                        }
                    }

                    if (newBet) {
                        bet = new Bet(nameOfMatch, selectionBet, priceOfBet, oddEven);
                        bet.setPlay(true);
                        bet.setMatchPeriod(periodBetInt);
                        System.out.println("new bet was added...");
                        list.add(bet);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNameOfSite() {
        return "www.williamHill.com - "+switcherId;
    }


    @Override
    public String toString() {
        return "www.williamHill.com";
    }
    @Override
    public void scanMatches(WebDriver driver) {
        try {
            Thread.sleep(2000);
            driver.get("http://sports.williamhill.com/bet/en-gb/betlive/27");
            List<WebElement> matchesElements = null;
            boolean isMatchesNow = false;
            try {
                matchesElements = driver.findElements(By.cssSelector("tr.rowLive"));
                isMatchesNow = true;
            } catch (Exception e) {
                System.out.println("Нет матчей сейчас.");
            }

            Map<Integer, MatchLive> currentMatches = null;
//            if (StartFrame.getSwitcher(this,switcherId) != null) {
//                currentMatches = StartFrame.getSwitcher(this,switcherId).getStorage().getCurrentMatches();
//            }

            for (Integer integer : currentMatches.keySet()) {
                boolean matchIsFinish = true;
                MatchLive matchLive = currentMatches.get(integer);
                String[] split = matchLive.getNameOfMatch().split(" vs ");
                for (WebElement element : matchesElements) {

                    if(split[0].contains("Women")){
                        split[0]=split[0].replace("Women","(W)");
                    }
                    if(split[1].contains("Women")){
                        split[1]=split[1].replace("Women","(W)");
                    }

                    String matchNameString = element.findElement(By.cssSelector("td.CentrePad")).findElement(By.cssSelector("a")).getText();

                    if (matchNameString.contains(split[0]) && matchNameString.contains(split[1])) {
                        matchIsFinish = false;
                        break;
                    }
                }
                if (matchIsFinish) {
                    if(matchLive.isAlmostFinish()){
                        matchLive.setFinished(true);
                        log.info("Матч " + matchLive.getNameOfMatch() + " уже окончен.");
                        System.out.println("the match with name " + matchLive.getNameOfMatch() + " already finished....");
                    } else {
                        System.out.println("Возможно матч " + matchLive.getNameOfMatch() + " закончился, первая проверка.");
                        log.info("Возможно матч " + matchLive.getNameOfMatch() + " закончился, первая проверка.");
                        matchLive.setAlmostFinish(true);
                    }
                }else {
                    matchLive.setAlmostFinish(false);
                }
            }

            for (Integer integer : currentMatches.keySet()) {
                MatchLive matchLive = currentMatches.get(integer);
                if (matchLive.isFinished()) {
                    matchLive.makeNextEvent(driver);
                }
            }

            if (isMatchesNow) {
                String nameOfMatch = null;
                SelectionBet currentPeriod = null;
                Double maxBet = 35.0;
                boolean isPossibleToMakeBet = true;
                for (int i = 0; i < matchesElements.size(); i++) {
                    try{
                        WebElement element = driver.findElements(By.cssSelector("tr.rowLive")).get(i);
                        String matchNameString = element.findElement(By.cssSelector("td.CentrePad")).findElement(By.cssSelector("a")).getText();
                        if (matchNameString != null) {
                            String[] split;
                            if (matchNameString.contains("   v   ")) {
                                split = matchNameString.split("   v   ");
                            }else  if (matchNameString.contains("  v  ")) {
                                split = matchNameString.split("  v  ");
                            } else {
                                split = matchNameString.split(" v ");
                            }
                            String firstTeamName = correctString(split[0]);
                            String secondTeamName = correctString(split[1]);
                            nameOfMatch = firstTeamName + " vs " + secondTeamName;
                        }

                        String currentPeriodString = element.findElement(By.cssSelector("td")).findElement(By.cssSelector("a")).getText();
                        switch (currentPeriodString) {//OT
                            case "1st":
                                currentPeriod = SelectionBet.Q1;
                                break;
                            case "2nd":
                                currentPeriod = SelectionBet.Q2;
                                break;
                            case "3rd":
                                currentPeriod = SelectionBet.Q3;
                                break;
                            case "4th":
                                currentPeriod = SelectionBet.Q4;
                                break;
                            case "Live":
                                currentPeriod = SelectionBet.Q1;
                                break;
                        }

                        WebElement makeBetElement = element.findElement(By.cssSelector("div.eventprice"));
                        try {
                            makeBetElement.click();
                            Thread.sleep(2000);
                            isPossibleToMakeBet = true;
                            driver.findElement(By.xpath("//a[text()='Clear Slip']")).click();
                            Thread.sleep(2000);
                        } catch (ElementNotVisibleException e) {
                            isPossibleToMakeBet = false;
                        }

                        MatchLive matchLive;

                        //if match already in map, update it
                        if (currentMatches.containsKey(nameOfMatch.hashCode())) {
                            matchLive = currentMatches.get(nameOfMatch.hashCode());
                            matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
                            matchLive.setCurrentPeriod(currentPeriod);

                        } else {
                            log.info("Добавляем новый матч " + nameOfMatch + ", время ко конца периода: " + 0L + ", текущий период: " + currentPeriod);
//                            matchLive = StartFrame.getSwitcher(this,switcherId).getStorage().addMatchLive(nameOfMatch, isPossibleToMakeBet, 0L, currentPeriod, maxBet);
                        }

//                        if (matchLive.isPossibleToMakeBet() && !matchLive.isBetIsDone()) {
//                            System.out.println(" Making Event for match " + nameOfMatch);
//                            matchLive.makeNextEvent(driver);
//                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean clickToMatch(WebDriver driver, String nameOfMatch) {
        boolean insideMatch = false;
        try {
            List<WebElement> matchesElements = null;
            try {
                matchesElements = driver.findElements(By.cssSelector("tr.rowLive"));
            } catch (Exception e) {
                System.out.println("Нет матчей сейчас.");
            }

            String[] split = nameOfMatch.split(" vs ");
            if (matchesElements != null) {
                for (WebElement element : matchesElements) {
                    String matchNameString = element.findElement(By.cssSelector("td.CentrePad")).findElement(By.cssSelector("a")).getText();
                    if (matchNameString != null) {
                        if (matchNameString.contains(split[0]) && matchNameString.contains(split[1])) {
                            element.findElement(By.cssSelector("td.CentrePad")).findElement(By.cssSelector("a")).click();
                            insideMatch = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return insideMatch;
    }

    @Override
    public Boolean betAlreadyNotPlayed(Bet lastBet, WebDriver driver) {
        System.out.println(lastBet.getMatchName());
        System.out.println(lastBet.getSelectionBet());
        System.out.println("++++++++++++++++++++++++++++++++");


        Boolean betIsPlayed = true;
        try {
            driver.findElement(By.id("open_bet_count")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.findElement(By.id("open_bet_count")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.findElement(By.id("open_bet_count")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WebElement slipopenBets = driver.findElement(By.id("slipopenBets"));
            List<WebElement> betElements = null;
            try {
                betElements = slipopenBets.findElements(By.cssSelector("div.cashout_noCash"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Сейчас нет текущих ставок.");
            }

            if (betElements != null) {
                String nameOfMatch = null;
                OddEven oddEven = null;
                SelectionBet selectionBet = null;
                double priceOfBet = 0.0;
                String priceOfBetString = null;
                System.out.println("Найдено ставок - " + betElements.size());

                for (WebElement betElement : betElements) {
                    nameOfMatch = betElement.findElement(By.cssSelector("p.cashout_link")).findElement(By.cssSelector("a")).getText();
                    System.out.println("Первое имя матча - " + nameOfMatch);

                    if (nameOfMatch.contains("  v  ")) {
                        nameOfMatch = nameOfMatch.replace("  v  ", " vs ");
                    } else if (nameOfMatch.contains(" v ")) {
                        nameOfMatch = nameOfMatch.replace(" v ", " vs ");
                    }

                    nameOfMatch = correctString(nameOfMatch);

                    String oddEvenString = betElement.findElement(By.cssSelector("span.cashout_col1")).findElement(By.cssSelector("strong")).getText();
                    if (oddEvenString != null) {
                        if (oddEvenString.contains("Odd")) {
                            oddEven = OddEven.Odd;
                        } else {
                            oddEven = OddEven.Even;
                        }
                    }

                    String selectionBetString = betElement.findElement(By.cssSelector("span.cashout_col2")).getText();

                    System.out.println("Выбор ставки с сайта " + selectionBetString);


                    if (selectionBetString != null) {
                        if (selectionBetString.contains("1st Quarter")) {
                            selectionBet = SelectionBet.Q1;
                        } else if (selectionBetString.contains("2nd Quarter")) {
                            selectionBet = SelectionBet.Q2;
                        } else if (selectionBetString.contains("3rd Quarter")) {
                            selectionBet = SelectionBet.Q3;
                        } else if (selectionBetString.contains("4th Quarter")) {
                            selectionBet = SelectionBet.Q4;
                        } else if (selectionBetString.contains("1st Half")) {
                            selectionBet = SelectionBet.Half1st;
                        } else if (selectionBetString.contains("2nd Half")) {
                            selectionBet = SelectionBet.Half2nd;
                        } else {
                            selectionBet = SelectionBet.Match;
                        }
                    }

                    priceOfBetString = betElement.findElement(By.cssSelector("span.cashout_left")).getAttribute("data-val");

                    try {
                        priceOfBet = Double.parseDouble(priceOfBetString);
                    } catch (Exception e) {
                        System.out.println("Проблемы во время парсинга цены ставки.");
                    }


                    if (nameOfMatch.contains(lastBet.getMatchName())) {
                        if (Bet.compareBetSelection(selectionBet, lastBet.getSelectionBet())) {
                            if (priceOfBet == lastBet.getPrice()) {
                                lastBet.zeroCountCheck();
                                betIsPlayed = false;
                                break;
                            }
                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Коллекция равна NULL.");
            }

        } catch (NoSuchElementException e) {
            betIsPlayed = null;
            e.printStackTrace();
        }

        if (betIsPlayed != null) {
            if (betIsPlayed) {
                System.out.println("Ставка еще не играет.");
            } else {
                System.out.println("Ставка уже играет.");
            }
        }

        return betIsPlayed;
    }

    @Override
    public void checkGameScore(WebDriver driver, MatchLive matchLive) {
        try {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WebDriver frame = driver.switchTo().frame(0);
            Map<SelectionBet, Integer> matchScoreStatistic = matchLive.getMatchScoreStatistic();

            WebElement scoreboard0;
            WebElement scoreboard1;
            try {
                scoreboard0 = frame.findElement(By.id("scoreboard")).findElement(By.cssSelector("tr.competitorA"));
                scoreboard1 = frame.findElement(By.id("scoreboard")).findElement(By.cssSelector("tr.competitorB"));
                List<WebElement> list = new ArrayList<>();
                list.addAll(scoreboard0.findElements(By.cssSelector("td.q1")));
                list.addAll(scoreboard1.findElements(By.cssSelector("td.q1")));
                list.addAll(scoreboard0.findElements(By.cssSelector("td.q2")));
                list.addAll(scoreboard1.findElements(By.cssSelector("td.q2")));
                list.addAll(scoreboard0.findElements(By.cssSelector("td.q3")));
                list.addAll(scoreboard1.findElements(By.cssSelector("td.q3")));
                list.addAll(scoreboard0.findElements(By.cssSelector("td.q4")));
                list.addAll(scoreboard1.findElements(By.cssSelector("td.q4")));
                int period = 1;
                int firstTeamScore = 0;
                for (WebElement e : list) {
                    String text = e.getText();
                    System.out.println("Счет - " + text);

                    if (text.compareTo("") == 0 || text.compareTo("-") == 0) {
                        text = "0";
                    }
                    System.out.println("Новый счет - " + text);
                    if (firstTeamScore == 0) {
                        firstTeamScore = Integer.parseInt(text);
                    } else {
                        matchScoreStatistic.put(SelectionBet.valueOf("Q" + period), firstTeamScore + Integer.parseInt(text));
                        firstTeamScore = 0;
                        period++;
                    }
                }
            } catch (Exception e) {
                System.out.println("Другой тип информ табло 1");
            }

            try {
                scoreboard0 = frame.findElement(By.cssSelector("div.first-rendered"));
                scoreboard1 = frame.findElement(By.cssSelector("div.second-rendered"));
                List<WebElement> list0 = new ArrayList<>();
                List<WebElement> list1 = new ArrayList<>();

                list0.addAll(scoreboard0.findElements(By.cssSelector("span.completed-set")));
                list0.add(scoreboard0.findElement(By.cssSelector("span.current-set")));
                list1.addAll(scoreboard1.findElements(By.cssSelector("span.completed-set")));
                list1.add(scoreboard1.findElement(By.cssSelector("span.current-set")));

                for (int i = 0; i < list0.size(); i++) {
                    Thread.sleep(500);
                    String text0 = list0.get(i).getText();
                    System.out.println("Счет первой команды - " + text0);
                    String text1 = list1.get(i).getText();
                    System.out.println("Счет второй команды - " + text1);
                    matchScoreStatistic.put(SelectionBet.valueOf("Q" + (i + 1)), Integer.parseInt(text0) + Integer.parseInt(text1));
                }
            } catch (Exception e) {
                System.out.println("Другой тип информ табло 2");
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

            System.out.println("==Score LIST for " + matchLive.getNameOfMatch() + "==");

            for (SelectionBet selectionBet : matchLive.getMatchOddEvenStatistic().keySet()) {
                System.out.println(selectionBet + " равно " + matchLive.getMatchOddEvenStatistic().get(selectionBet));
            }

            System.out.println("======================================================");

        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } finally {
            driver.switchTo().defaultContent();
        }
    }

    @Override
    public void checkMaximumBetSizeAndPossibleBetSelection(WebDriver driver, MatchLive matchLive) {
        System.out.println("Cheking max bet for match " + matchLive.getNameOfMatch());
        matchLive.getPossibleBetSelectionBet().clear();
        try {
            List<WebElement> elements = driver.findElements(By.cssSelector("div.marketHolderExpanded"));
            for (WebElement element : elements) {
                String span = element.findElement(By.cssSelector("span")).getText();
                if (span.contains("1st Quarter Total Points Odd/Even")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q1);
                    continue;
                } else if (span.contains("2nd Quarter Total Points Odd/Even")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q2);
                    continue;
                } else if (span.contains("3rd Quarter Total Points Odd/Even")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q3);
                    continue;
                } else if (span.contains("4th Quarter Total Points Odd/Even")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q4);
                    continue;
                } else if (span.contains("1st Half Total Points Odd/Even")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Half1st);
                    continue;
                } else if (span.contains("2nd Half Total Points Odd/Even")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Half2nd);
                    continue;
                }
//                else if(span.contains("Total Points Odd/Even")){
//                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Match);
//                    continue;
//                }
            }

            System.out.println("==Selection LIST for " + matchLive.getNameOfMatch() + "==");
            for (SelectionBet selectionBet : matchLive.getPossibleBetSelectionBet()) {
                System.out.println("Можно ставить на  " + selectionBet);
            }
            System.out.println("======================================================");
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void goBackToOverview(WebDriver driver) {
        try {
            driver.get("http://sports.williamhill.com/bet/en-gb/betlive/27");
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getMinBet(WebDriver driver) {
        double minBet = 0.2;
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
        this.log = Logger.getLogger(ParserWilliamHill.class);
        this.log.addAppender(appender);
    }

    @Override
    public Appender getAppender() {
        return appender;
    }
}
