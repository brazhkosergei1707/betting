package betting.parserBetting;

import UI.startFrame.StartFrame;
import betting.Betting;
import betting.entity.MatchLiveBetting;
import betting.entity.Bet;
import betting.enumeration.OddEven;
import betting.enumeration.SelectionBet;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;

import java.util.*;

public class BettingParserWilliamHill implements BettingParser {
    private static Logger log = Logger.getLogger(BettingParserWilliamHill.class);

    @Override
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

            WebElement userNameElement = driver.findElement(By.id("username"));
//            JavascriptExecutor js = (JavascriptExecutor) driver;
//            js.executeScript("arguments[0].scrollIntoView();", userNameElement);
            userNameElement.sendKeys(username);
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

    @Override
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
    public double getMinBet(WebDriver driver) {
        double minBet = 0.2;
        if (minBet > StartFrame.getMinBet()) {
            return minBet;
        } else {
            return StartFrame.getMinBet();
        }
    }

    private void updatePage(WebDriver driver) {
        try {
            WebElement element1 = driver.findElement(By.xpath("//a[text()='Last Login']"));//Last Login   By.xpath("//a[text()='Live-bets']")
            System.out.println("Нашли таки элемент - " + element1.getText());
            Actions actions = new Actions(driver);
            actions.keyDown(Keys.LEFT_CONTROL)
                    .click(element1)
                    .keyUp(Keys.LEFT_CONTROL)
                    .build()
                    .perform();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WebDriver window = driver.switchTo().window(driver.getWindowHandles().toArray()[0].toString());
            window.close();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.switchTo().window(driver.getWindowHandles().toArray()[0].toString());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateCurrentMatchesLive(Map<String, MatchLiveBetting> currentMatches, WebDriver driver, double margin, double minBet, Betting betting) {
        try {
            updatePage(driver);

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
                String nameOfMatch = null;
                for (WebElement element : matchesElements) {
                    String matchNameString = element.findElement(By.cssSelector("td.CentrePad")).findElement(By.cssSelector("a")).getText();
                    if (matchNameString != null) {
                        String[] split;
                        if (matchNameString.contains("   v   ")) {
                            split = matchNameString.split("   v   ");
                        } else if (matchNameString.contains("  v  ")) {
                            split = matchNameString.split("  v  ");
                        } else {
                            split = matchNameString.split(" v ");
                        }
                        String firstTeamName = correctString(split[0]);
                        String secondTeamName = correctString(split[1]);
                        nameOfMatch = firstTeamName + " vs " + secondTeamName;
                    }

                    MatchLiveBetting matchLive;
                    if (!currentMatches.containsKey(nameOfMatch)) {
                        matchLive = new MatchLiveBetting(betting, nameOfMatch, margin, minBet);
                        currentMatches.put(nameOfMatch, matchLive);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateHistoryBet(WebDriver driver, List<Bet> historyList) {
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
            }
            if (bets != null) {
                Bet bet;
                long dateOfBet = 0L;
                String dateString = null;
                String nameOfMatch = null;
                OddEven oddEven = null;
                SelectionBet selectionBet = null;
                int periodBetInt = 0;
                Double priceOfBet;
                Boolean isWin;
                for (WebElement betElement : bets) {
                    try {
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
                                    for (Bet bet1 : historyList) {
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

                        if (!oldBet) {
                            bet = new Bet(nameOfMatch, selectionBet, priceOfBet, oddEven);
                            bet.setWin(isWin);
                            bet.setTimeBet(dateOfBet);
                            bet.setMatchPeriod(periodBetInt);
                            historyList.add(bet);
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
    public void updateCurrentBet(WebDriver driver, List<Bet> list) {
        try {
            boolean isBets = false;
            driver.get("http://sports.williamhill.com/bet/en-gb");
            WebElement openBetCount = driver.findElement(By.id("open_bet_count"));
            openBetCount.click();
            openBetCount.click();
//            clickOnInvisibleElement(openBetCount, driver);
            Thread.sleep(2000);
            WebElement slipopenBets = driver.findElement(By.id("slipopenBets"));
            List<WebElement> betElements = null;
            try {
                betElements = slipopenBets.findElements(By.cssSelector("div.cashout_noCash"));
                isBets = true;
            } catch (Exception e) {
                System.out.println("Сейчас нет текущих ставок.");
            }

            if (betElements != null && isBets && betElements.size() > 0) {
                Bet bet;
                String nameOfMatch = null;
                OddEven oddEven = null;
                SelectionBet selectionBet = null;
                int periodBetInt = 0;
                double priceOfBet = 0.0;
                String priceOfBetString;

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
                        }
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
    public void setCurrentBalance(WebDriver driver) {
        try {
            String balance = driver.findElement(By.cssSelector("span.mainBalanceValue")).getText();
            StartFrame.getStorage(this).setCurrentBalanceString(balance);
            Double doubleBalance = balanceDouble(balance);
            if (doubleBalance != null) {
                StartFrame.getStorage(this).setCurrentBalance(doubleBalance);
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
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
    public void lookingForMatches(WebDriver driver, Map<String, MatchLiveBetting> matchLiveMap, double margin, double minBet, Betting betting) {
        try {
            updatePage(driver);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Thread.sleep(3000);
            driver.get("http://sports.williamhill.com/bet/en-gb/betlive/27");
            List<WebElement> matchesElements = null;
            boolean isMatchesNow = false;
            try {
                matchesElements = driver.findElements(By.cssSelector("tr.rowLive"));
                isMatchesNow = true;
            } catch (Exception ignored) {
            }

            Thread.sleep(3000);
            for (String name : matchLiveMap.keySet()) {
                Thread.sleep(1000);
                boolean matchIsFinish = true;
                MatchLiveBetting matchLive = matchLiveMap.get(name);
                if (matchLive == null) {
                    continue;
                }

                if (matchLive.isFinished()) {
                    if (!matchLive.isEventMade()) {
                        matchLive.makeNextEvent();
                    }
                    continue;
                }

                String[] split = matchLive.getNameOfMatch().split(" vs ");
                for (WebElement element : matchesElements) {
                    if (split[0].contains("Women")) {
                        split[0] = split[0].replace("Women", "(W)");
                    }
                    if (split[1].contains("Women")) {
                        split[1] = split[1].replace("Women", "(W)");
                    }
                    String matchNameString = element.findElement(By.cssSelector("td.CentrePad")).findElement(By.cssSelector("a")).getText();
                    if (matchNameString.contains(split[0]) && matchNameString.contains(split[1])) {
                        matchIsFinish = false;
                        break;
                    }
                }

                if (matchIsFinish) {
                    if (matchLive.isAlmostFinish()) {
                        matchLive.setFinished(true);
                        log.info("Матч " + matchLive.getNameOfMatch() + " уже окончен.");
                        System.out.println("Матч " + matchLive.getNameOfMatch() + " уже окончен.");
                    } else {
                        System.out.println("Возможно матч " + matchLive.getNameOfMatch() + " закончился, первая проверка.");
                        log.info("Возможно матч " + matchLive.getNameOfMatch() + " закончился, первая проверка.");
                        matchLive.setAlmostFinish(true);
                    }
                } else {
                    matchLive.setAlmostFinish(false);
                }
            }

            if (isMatchesNow) {
//                for (int i = 0; i < matchesElements.size(); i++) {
                for (int i = matchesElements.size() - 1; i >= 0; i--) {
                    try {
                        String nameOfMatch = null;
                        SelectionBet currentPeriod = null;
                        boolean isPossibleToMakeBet;
                        WebElement element = driver.findElements(By.cssSelector("tr.rowLive")).get(i);
                        String matchNameString = element.findElement(By.cssSelector("td.CentrePad")).findElement(By.cssSelector("a")).getText();
                        if (matchNameString != null) {
                            String[] split;
                            if (matchNameString.contains("   v   ")) {
                                split = matchNameString.split("   v   ");
                            } else if (matchNameString.contains("  v  ")) {
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
                            JavascriptExecutor js = (JavascriptExecutor) driver;
                            js.executeScript("arguments[0].scrollIntoView();", makeBetElement);
                            makeBetElement.click();
                            Thread.sleep(2000);
                            WebElement clearElement = driver.findElement(By.xpath("//a[text()='Clear Slip']"));
                            isPossibleToMakeBet = clearElement.isDisplayed();
                            if(isPossibleToMakeBet){
                                js.executeScript("arguments[0].scrollIntoView();", clearElement);
                                clearElement.click();
                            }
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                            isPossibleToMakeBet = false;
                        }

                        MatchLiveBetting matchLive;
                        if (matchLiveMap.containsKey(nameOfMatch)) {
                            System.out.println("Старый матч");
                            matchLive = matchLiveMap.get(nameOfMatch);
                            matchLive.setFinished(false);
                            matchLive.setAlmostFinish(false);
                            matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
                            if (currentPeriod != null) {
                                matchLive.setCurrentPeriod(currentPeriod);
                            }
                        } else {
                            System.out.println("Новый матч");
                            matchLive = new MatchLiveBetting(betting, nameOfMatch, margin, minBet);
                            matchLiveMap.put(nameOfMatch, matchLive);
                        }

                        System.out.println("Матч " + matchLive.getNameOfMatch());
                        if (matchLive.isPossibleToMakeBet() && !matchLive.isBetIsDone()) {
                            if (!matchLive.isEventMade()) {
                                if (clickToMatch(driver, matchLive.getNameOfMatch())) {
                                    checkGameScore(driver, matchLive);
                                    checkMaximumBetSizeAndPossibleBetSelection(driver, matchLive);
                                    goBackToOverview(driver);
                                    matchLive.makeNextEvent();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (NoSuchElementException | InterruptedException e) {
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
                            WebElement matchElement = element.findElement(By.cssSelector("td.CentrePad")).findElement(By.cssSelector("a"));
                            JavascriptExecutor js = (JavascriptExecutor) driver;
                            js.executeScript("arguments[0].scrollIntoView();", matchElement);
                            matchElement.click();
                            Thread.sleep(1000);
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
    public boolean checkGameScore(WebDriver driver, MatchLiveBetting matchLive) {
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
//                    System.out.println("Счет - " + text);

                    if (text.compareTo("") == 0 || text.compareTo("-") == 0) {
                        text = "0";
                    }
//                    System.out.println("Новый счет - " + text);
                    if (firstTeamScore == 0) {
                        firstTeamScore = Integer.parseInt(text);
                    } else {
                        matchScoreStatistic.put(SelectionBet.valueOf("Q" + period), firstTeamScore + Integer.parseInt(text));
                        firstTeamScore = 0;
                        period++;
                    }
                }
            } catch (Exception ignored) {
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
//                    System.out.println("Счет первой команды - " + text0);
                    String text1 = list1.get(i).getText();
//                    System.out.println("Счет второй команды - " + text1);
                    matchScoreStatistic.put(SelectionBet.valueOf("Q" + (i + 1)), Integer.parseInt(text0) + Integer.parseInt(text1));
                }
            } catch (Exception ignored) {
            }

            if (matchScoreStatistic.get(SelectionBet.Q1) != null && matchScoreStatistic.get(SelectionBet.Q2) != null) {
                matchScoreStatistic.put(SelectionBet.Half1st, matchScoreStatistic.get(SelectionBet.Q1) + matchScoreStatistic.get(SelectionBet.Q2));
            }

            for (SelectionBet selectionBet : matchLive.getMatchScoreStatistic().keySet()) {
                Integer score = matchLive.getMatchScoreStatistic().get(selectionBet);
                if (score % 2 == 0) {
                    matchLive.getMatchOddEvenStatistic().put(selectionBet, OddEven.Even);//Even
                } else if (score % 2 == 1) {
                    matchLive.getMatchOddEvenStatistic().put(selectionBet, OddEven.Odd);//Odd
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        } finally {
            driver.switchTo().defaultContent();
        }
        return true;
    }

    @Override
    public boolean checkMaximumBetSizeAndPossibleBetSelection(WebDriver driver, MatchLiveBetting matchLive) {
        matchLive.getPossibleBetSelectionBet().clear();
        try {
            List<WebElement> elements = driver.findElements(By.cssSelector("div.marketHolderExpanded"));
            for (WebElement element : elements) {
                String span = element.findElement(By.cssSelector("span")).getText();
                if (span.contains("1st Quarter Total Points Odd/Even")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q1);
                } else if (span.contains("2nd Quarter Total Points Odd/Even")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q2);
                } else if (span.contains("3rd Quarter Total Points Odd/Even")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q3);
                } else if (span.contains("4th Quarter Total Points Odd/Even")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q4);
                } else if (span.contains("1st Half Total Points Odd/Even")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Half1st);
                } else if (span.contains("2nd Half Total Points Odd/Even")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Half2nd);
                }
            }

//            System.out.println("Матч - " + matchLive.getNameOfMatch() + ". Можно ставить на");
//            for (SelectionBet selectionBet : matchLive.getPossibleBetSelectionBet()) {
//                System.out.println("Можно ставить на  " + selectionBet);
//            }
//            System.out.println("======================================================");
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean goBackToOverview(WebDriver driver) {
        boolean goBack = false;
        try {
            driver.get("http://sports.williamhill.com/bet/en-gb/betlive/27");
            Thread.sleep(1000);
            goBack = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return goBack;
    }

    private void clickOnInvisibleElement(WebElement element, WebDriver driver) {
        System.out.println("Элемент, на который будем кликать - " + element.getText());
        String script = "var object = arguments[0];"
                + "var theEvent = document.createEvent(\"MouseEvent\");"
                + "theEvent.initMouseEvent(\"click\", true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
                + "object.dispatchEvent(theEvent);";
        ((JavascriptExecutor) driver).executeScript(script, element);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean betAlreadyNotPlayed(Bet lastBet, WebDriver driver) {
        Boolean betNotPlayed = true;
        try {
            driver.get("http://sports.williamhill.com/bet/en-gb");
            WebElement openBetCount = driver.findElement(By.id("open_bet_count"));

            try {
                driver.findElement(By.xpath("//a[text()='Clear Slip']")).click();
            } catch (Exception ignored) {
            }
//            clickOnInvisibleElement(openBetCount, driver);
            Thread.sleep(1000);

            openBetCount.click();
            Thread.sleep(1000);
            WebElement slipOpenBets = driver.findElement(By.id("slipopenBets"));
            List<WebElement> betElements = null;
            try {
                betElements = slipOpenBets.findElements(By.cssSelector("div.cashout_noCash"));
            } catch (Exception e) {
                System.out.println("Сейчас нет текущих ставок.");
            }

            System.out.println("Ищем ставку:");
            System.out.println("Имя матча - " + lastBet.getMatchName());
            System.out.println("Выбор ставки - " + lastBet.getSelectionBet());

            if (betElements != null) {
                System.out.println("Нашли ставок в списке текущих - " + betElements.size());
                String nameOfMatch;
                SelectionBet selectionBet = null;
                for (WebElement betElement : betElements) {
                    nameOfMatch = betElement.findElement(By.cssSelector("p.cashout_link")).findElement(By.cssSelector("a")).getText();
                    if (nameOfMatch.contains("  v  ")) {
                        nameOfMatch = nameOfMatch.replace("  v  ", " vs ");
                    } else if (nameOfMatch.contains(" v ")) {
                        nameOfMatch = nameOfMatch.replace(" v ", " vs ");
                    }
                    nameOfMatch = correctString(nameOfMatch);
                    String selectionBetString = betElement.findElement(By.cssSelector("span.cashout_col2")).getText();
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

                    System.out.println("Имя матча в текущих ставках - " + nameOfMatch);
                    System.out.println("Выбор ставки в текущих ставках - " + selectionBet);

                    if (nameOfMatch.compareTo((lastBet.getMatchName())) == 0) {
                        if (Bet.compareBetSelection(selectionBet, lastBet.getSelectionBet())) {
                            lastBet.zeroCountCheck();
                            betNotPlayed = false;
                            break;
                        }
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Не НАШИЛ СТАВОК ТЕКУЩИХ, ИЛИ НЕ ПЕРЕКЛЮЧИЛИ К НИМ.");
            }
        } catch (Exception e) {
            betNotPlayed = null;
            e.printStackTrace();
        }
        System.out.println("Ставка еще не играет - " + betNotPlayed);
        System.out.println("===============================================");
        return betNotPlayed;
    }

    @Override
    public Bet makeBet(Bet bet, WebDriver driver, MatchLiveBetting matchLive) {

        updatePage(driver);

        if (bet.getMatchName() == null) {
            bet.setMatchName(matchLive.getNameOfMatch());
        }

        boolean betAlreadyNotDone;
        Boolean b1 = betAlreadyNotPlayed(bet, driver);
        if (b1 != null) {
            betAlreadyNotDone = b1;
        } else {
            betAlreadyNotDone = true;
        }

        if (betAlreadyNotDone) {
            goBackToOverview(driver);
            if (clickToMatch(driver, bet.getMatchName())) {
                bet.setMatchPeriod(matchLive.getCurrentPeriodInt());
                try {
                    Thread.sleep(3000);
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
                    }

                    if (text != null) {
                        List<WebElement> elements = driver.findElements(By.cssSelector("div.marketHolderExpanded"));
                        for (WebElement e : elements) {
                            try {

                                String span = e.findElement(By.cssSelector("span")).getText();
                                if (span.contains(text)) {
                                    List<WebElement> oddEvenElements = e.findElement(By.cssSelector("tbody")).findElements(By.cssSelector("td"));

                                    WebElement element;
                                    if (bet.getOddEven() == OddEven.Odd) {
                                        element = oddEvenElements.get(0);
                                    } else {
                                        element = oddEvenElements.get(1);
                                    }
                                    System.out.println("Хедер Елемента, на который будем кликать - " + e.getText());
                                    System.out.println("Сам елемент, на который будем кликать - " + element.getText());

                                    JavascriptExecutor js = (JavascriptExecutor) driver;
                                    js.executeScript("arguments[0].scrollIntoView();", element);
                                    Thread.sleep(1000);
                                    element.click();
                                    WebElement inputField = driver.findElement(By.cssSelector("div.slipStake")).findElement(By.cssSelector("input"));
                                    inputField.clear();
                                    Thread.sleep(1000);
                                    inputField.sendKeys(String.valueOf(bet.getPrice()));
                                    Thread.sleep(2000);
                                    driver.findElement(By.id("slipBtnPlaceBet")).click();
                                    Thread.sleep(10000);
                                    try {
                                        driver.findElement(By.xpath("//div[contains(text(),'Your bet has been placed with William Hill')]"));
                                        //div.text() = Your bet has been placed with William Hill
                                        betAlreadyNotDone = false;
                                    } catch (Exception ex) {
                                        try {
                                            driver.findElement(By.xpath("//a[contains(text(),'Amend your Bet')]")).click();
                                            Thread.sleep(1000);
                                            driver.findElement(By.xpath("//a[contains(text(),'Clear Slip')]")).click();
                                            Thread.sleep(1000);
                                        } catch (Exception exc) {
                                            exc.printStackTrace();
                                        }
                                    }
                                    break;
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    if (betAlreadyNotDone) {
                        Boolean b = betAlreadyNotPlayed(bet, driver);
                        if (b != null) {
                            betAlreadyNotDone = b;
                        } else {
                            System.out.println("НЕИЗВЕСТНО ПОСТАВИЛИ ЛИ СТАВКУ, ТАК КАК ВЕРНУЛСЯ НУЛЛ");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (!betAlreadyNotDone) {
            bet.setTimeBet(System.currentTimeMillis());
            System.out.println("Ставка сделана, время ставки " + bet.getTimeBet());
            log.info("Ставка сделана, время ставки " + bet.getTimeBet());
        }
        return bet;
    }

    @Override
    public Bet checkBet(Bet bet, WebDriver driver) {

        updatePage(driver);

        System.out.println("Проверяем ставку на вильям хилл");
        try {
            if (betAlreadyNotPlayed(bet, driver)) {
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
                    SelectionBet selectionBet = null;
                    Boolean isWin = null;
                    for (WebElement betElement : bets) {
                        if (betElement.findElements(By.cssSelector("td")).size() == 0) {
                            continue;
                        }
                        dateString = betElement.findElements(By.cssSelector("td")).get(4).getText();
                        if (dateString != null) {
                            String[] split = dateString.split("  –  ");

                            if (split[0].contains("  v  ")) {
                                nameOfMatch = split[0].replace("  v  ", " vs ");
                            } else if (split[0].contains(" v ")) {
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

                    if (bet.getWin() == null) {
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
            } else {
                bet.zeroCountCheck();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bet;
    }

    private String correctString(String oldString) {
        if (oldString.contains("(W)")) {
            oldString = oldString.replace("(W)", "Women");
        }
        if (oldString.contains("(")) {
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
    public String getNameOfSite() {
        return "williamHill.com";
    }

    @Override
    public String toString() {
        return "www.williamHill.com";
    }
}
