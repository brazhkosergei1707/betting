package betting.parserBetting;

import UI.startFrame.StartFrame;
import betting.Betting;
import betting.entity.MatchLiveBetting;
import betting.entity.Bet;
import betting.enumeration.OddEven;
import betting.enumeration.SelectionBet;
import org.omg.PortableServer.THREAD_POLICY_ID;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BettingBetFair implements BettingParser {
    @Override
    public boolean logIn(String username, String password, WebDriver driver) {
        try {
            driver.get("https://www.betfair.com/");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            WebElement userNameField = driver.findElement(By.id("ssc-liu"));
            userNameField.click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            userNameField.sendKeys(username);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            WebElement passwordField = driver.findElement(By.id("ssc-lipw"));
            passwordField.click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            passwordField.sendKeys(password);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            driver.findElement(By.id("ssc-lis")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            driver.findElement(By.cssSelector("a[href='https://www.betfair.com/']")).click();

        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean logOut(WebDriver driver) {
        try {
            driver.findElement(By.cssSelector("span.ssc-un")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.findElement(By.cssSelector("input#ssc-lis")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
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

    @Override
    public void updateCurrentMatchesLive(Map<String, MatchLiveBetting> currentMatches, WebDriver driver, double margin, double minBet, Betting betting) {
        try {
            driver.get("https://www.betfair.com/sport/basketball");
            Thread.sleep(1000);
            List<WebElement> elements = driver.findElements(By.cssSelector("li.section"));
            String text;
            for (WebElement element : elements) {
                text = element.findElement(By.cssSelector("span.section-header-label")).getText();
                System.out.println("Текст с сайта, группа матчей - " + text);
                if (text != null) {
                    if (text.contains("In-Play")) {
                        List<WebElement> matchesElements = element.findElements(By.cssSelector("li.com-coupon-line-new-layout"));
                        String nameOfMatch = null;
                        System.out.println("Нашли матчей - " + matchesElements.size());
                        for (WebElement matchElement : matchesElements) {
                            List<WebElement> teamNamesElements = matchElement.findElement(By.cssSelector("div.teams-container")).findElements(By.cssSelector("span.team-name"));
                            String text1 = teamNamesElements.get(0).getText();
                            String text2 = teamNamesElements.get(1).getText();
                            if (text1 != null && text2 != null) {
                                nameOfMatch = text1 + " vs " + text2;
                            }

                            System.out.println("Имя матча - " + nameOfMatch);
                            MatchLiveBetting matchLive;
//                            if (!currentMatches.containsKey(nameOfMatch)) {
//                                matchLive = new MatchLiveBetting(betting, nameOfMatch, margin, minBet);
//                                System.out.println("Добавили новый матч - " + matchLive.getNameOfMatch());
//                                currentMatches.put(nameOfMatch, matchLive);
//                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateHistoryBet(WebDriver driver, List<Bet> historyList) {
        try {
            updatePage(driver);
            driver.findElement(By.xpath("//a[text()='My Bets']")).click();
            Thread.sleep(2000);
            driver.findElement(By.xpath("//button[text()='Past']")).click();
            Thread.sleep(1000);
            List<WebElement> historyBetsElements = driver.findElements(By.cssSelector("tr.bet--row"));

            for (WebElement betElement : historyBetsElements) {
                try {
                    Bet bet;
                    String nameOfMatch;
                    OddEven oddEven;
                    SelectionBet selectionBet;
                    int periodBetInt;
                    double priceOfBet;
                    Boolean isWin;
                    String nameFromSite = betElement.findElement(By.cssSelector("span.bet__main-description")).getText();
                    nameOfMatch = nameFromSite.replace(" v ", " vs ");
                    String oddEvenString = betElement.findElement(By.cssSelector("strong")).findElements(By.cssSelector("span")).get(3).getText();
                    if (oddEvenString.contains("Odd")) {
                        oddEven = OddEven.Odd;
                    } else {
                        oddEven = OddEven.Even;
                    }
                    String dataString = betElement.findElement(By.cssSelector("span[ng-if='bet.marketName']")).findElement(By.cssSelector("span")).getText();
                    if (dataString.contains("1st Quarter")) {
                        selectionBet = SelectionBet.Q1;
                        periodBetInt = 1;
                    } else if (dataString.contains("2nd Quarter")) {
                        selectionBet = SelectionBet.Q2;
                        periodBetInt = 2;
                    } else if (dataString.contains("3rd Quarter")) {
                        selectionBet = SelectionBet.Q3;
                        periodBetInt = 3;
                    } else if (dataString.contains("4th Quarter")) {
                        selectionBet = SelectionBet.Q4;
                        periodBetInt = 4;
                    } else if (dataString.contains("1st Half")) {
                        selectionBet = SelectionBet.Half1st;
                        periodBetInt = 2;
                    } else if (dataString.contains("2nd Half")) {
                        selectionBet = SelectionBet.Half2nd;
                        periodBetInt = 4;
                    } else {
                        selectionBet = SelectionBet.Match;
                        periodBetInt = 4;
                    }

                    String priceSting = betElement.findElement(By.cssSelector("td.bet-undertitle-head")).getText();
                    priceOfBet = Double.parseDouble(priceSting);

                    String isWinText = betElement.findElement(By.cssSelector("td.td-status-row")).findElements(By.cssSelector("span")).get(1).getText();
                    isWin = isWinText.contains("Won");

                    boolean oldBet = false;
                    if (nameOfMatch != null) {
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

                    if (!oldBet) {
                        bet = new Bet(nameOfMatch, selectionBet, priceOfBet, oddEven);
                        bet.setWin(isWin);
                        bet.setTimeBet(0L);
                        bet.setMatchPeriod(periodBetInt);
                        historyList.add(bet);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            System.out.println("Найдены ставки В ИСТОРИИ: ");
//            for (Bet bet : historyList) {
//                System.out.println(bet.getMatchName());
//                System.out.println(bet.getSelectionBet());
//                System.out.println(bet.getOddEven());
//                System.out.println(bet.getPrice());
//                System.out.println(bet.getWin());
//                System.out.println("=====================================");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateCurrentBet(WebDriver driver, List<Bet> list) {
        try {
            updatePage(driver);
            driver.findElement(By.xpath("//a[text()='My Bets']")).click();
            Thread.sleep(1000);
            List<WebElement> currentBets = driver.findElements(By.cssSelector("tr.bet--row"));

            for (WebElement betElement : currentBets) {
                try {
                    Bet bet;
                    String nameOfMatch;
                    OddEven oddEven;
                    SelectionBet selectionBet;
                    int periodBetInt;
                    double priceOfBet;
                    String nameFromSite = betElement.findElement(By.cssSelector("span.bet__main-description")).getText();
                    nameOfMatch = nameFromSite.replace(" v ", " vs ");
                    String oddEvenString = betElement.findElement(By.cssSelector("strong")).findElements(By.cssSelector("span")).get(3).getText();
                    if (oddEvenString.contains("Odd")) {
                        oddEven = OddEven.Odd;
                    } else {
                        oddEven = OddEven.Even;
                    }
                    String dataString = betElement.findElement(By.cssSelector("span[ng-if='bet.marketName']")).findElement(By.cssSelector("span")).getText();
                    if (dataString.contains("1st Quarter")) {
                        selectionBet = SelectionBet.Q1;
                        periodBetInt = 1;
                    } else if (dataString.contains("2nd Quarter")) {
                        selectionBet = SelectionBet.Q2;
                        periodBetInt = 2;
                    } else if (dataString.contains("3rd Quarter")) {
                        selectionBet = SelectionBet.Q3;
                        periodBetInt = 3;
                    } else if (dataString.contains("4th Quarter")) {
                        selectionBet = SelectionBet.Q4;
                        periodBetInt = 4;
                    } else if (dataString.contains("1st Half")) {
                        selectionBet = SelectionBet.Half1st;
                        periodBetInt = 2;
                    } else if (dataString.contains("2nd Half")) {
                        selectionBet = SelectionBet.Half2nd;
                        periodBetInt = 4;
                    } else {
                        selectionBet = SelectionBet.Match;
                        periodBetInt = 4;
                    }

                    String priceSting = betElement.findElement(By.cssSelector("td.bet-undertitle-head")).getText();
                    priceOfBet = Double.parseDouble(priceSting);

                    boolean oldBet = false;
                    if (nameOfMatch != null) {
                        if (oddEven != null) {
                            if (selectionBet != null) {
                                for (Bet bet1 : list) {
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
                        bet.setTimeBet(0L);
                        bet.setMatchPeriod(periodBetInt);
                        list.add(bet);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            System.out.println("=====================================");
//            System.out.println("=====================================");
//            System.out.println("Найдены ставки: ");
//            for (Bet bet : list) {
//                System.out.println(bet.getMatchName());
//                System.out.println(bet.getSelectionBet());
//                System.out.println(bet.getOddEven());
//                System.out.println(bet.getPrice());
//                System.out.println(bet.getWin());
//                System.out.println("=====================================");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCurrentBalance(WebDriver driver) {
        try {
            String balance = driver.findElement(By.cssSelector("td[rel='main']")).getText();//€10.01
            Double doubleBalance = parseDouble(balance);
            if (doubleBalance != null) {
                StartFrame.getStorage(this).setCurrentBalance(doubleBalance);
                StartFrame.getStorage(this).setCurrentBalanceString(balance);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    private Double parseDouble(String balance) {
        Double doubleBalance = null;
        try {
            String correctString = "";
            if (balance.contains("€")) {
                correctString = balance.substring(1, balance.length() - 1);
            }
            doubleBalance = Double.parseDouble(correctString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doubleBalance;
    }

    @Override
    public void lookingForMatches(WebDriver driver, Map<String, MatchLiveBetting> matchLiveMap, double margin, double minBet, Betting betting) {
        try {
            updatePage(driver);
            driver.get("https://www.betfair.com/sport/basketball");
            try {
                driver.findElement(By.xpath("//span[contains(text(),'Ok, I get it')]")).click();//Ok, I get it
            } catch (Exception ignored) {
            }

            driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
            for (String name : matchLiveMap.keySet()) {
                boolean matchFinish = true;
                MatchLiveBetting matchLive = matchLiveMap.get(name);
                if (matchLive.isFinished()) {
                    if (!matchLive.isEventMade()) {
                        matchLive.makeNextEvent();
                    }
                    continue;
                }
                String[] split = matchLive.getNameOfMatch().split(" vs ");
                try {
                    String xpath = "//span[contains(text(),'" + split[0] + "')]";
                    driver.findElement(By.xpath(xpath));
                    matchFinish = false;
                } catch (Exception e) {
                    try {
                        String xpath = "//span[contains(text(),'" + split[1] + "')]";
                        driver.findElement(By.xpath(xpath));
                        matchFinish = false;
                    } catch (Exception ignored) {
                    }
                }
                if (matchFinish) {
                    if (matchLive.isAlmostFinish()) {
                        matchLive.setFinished(true);
                        System.out.println("Матч " + matchLive.getNameOfMatch() + " уже окончен.");
                    } else {
                        System.out.println("Возможно матч " + matchLive.getNameOfMatch() + " закончился, первая проверка.");
                        matchLive.setAlmostFinish(true);
                    }
                } else {
                    matchLive.setAlmostFinish(false);
                }
            }
            driver.manage().timeouts().implicitlyWait(7, TimeUnit.SECONDS);
            Thread.sleep(1000);

            List<WebElement> matchesElements = driver
                    .findElement(By.xpath("//span[contains(@class, 'section-header-label') and contains(text(),'In-Play')]/ancestor::li"))
                    .findElements(By.cssSelector("li.com-coupon-line-new-layout"));
            int size = matchesElements.size();

            System.out.println("Нашли матчей - " + size);

            for (int i = 0; i < size; i++) {
                try {
                    matchesElements = driver
                            .findElement(By.xpath("//span[contains(@class, 'section-header-label') and contains(text(),'In-Play')]/ancestor::li"))
                            .findElements(By.cssSelector("li.com-coupon-line-new-layout"));

                    WebElement matchElement = matchesElements.get(i);
                    if (i > 2) {
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        js.executeScript("arguments[0].scrollIntoView();", matchesElements.get(i - 2));
                    }
                    String nameOfMatch = null;
                    SelectionBet currentPeriod = null;
                    boolean isPossibleToMakeBet;
                    List<WebElement> teamNamesElements = matchElement.findElement(By.cssSelector("div.teams-container")).findElements(By.cssSelector("span.team-name"));
                    String text1 = teamNamesElements.get(0).getText();
                    String text2 = teamNamesElements.get(1).getText();
                    if (text1 != null && text2 != null) {
                        nameOfMatch = text1 + " vs " + text2;
                    }

                    String currentPeriodString = matchElement.findElement(By.cssSelector("span.ui-status-format")).getText();
                    if (currentPeriodString != null) {
                        switch (currentPeriodString) {
                            case "Q1":
                                currentPeriod = SelectionBet.Q1;
                                break;
                            case "Q2":
                                currentPeriod = SelectionBet.Q2;
                                break;
                            case "Q3":
                                currentPeriod = SelectionBet.Q3;
                                break;
                            case "Q4":
                                currentPeriod = SelectionBet.Q4;
                                break;
                            case "Live":
                                currentPeriod = SelectionBet.Q1;
                                break;
                            case "HT":
                                currentPeriod = SelectionBet.Q3;
                                break;
                            default:
                                currentPeriod = SelectionBet.Q1;
                                break;
                        }
                    }

                    try {
                        WebElement betElement = matchElement.findElement(By.cssSelector("li.selection"));
                        Thread.sleep(1000);
                        betElement.click();
                        Thread.sleep(1000);
                        driver.findElement(By.cssSelector("a.remove-all-bets")).click();
                        isPossibleToMakeBet = true;
                    } catch (Exception e) {
                        isPossibleToMakeBet = false;
                    }

                    MatchLiveBetting matchLive;
                    if (matchLiveMap.containsKey(nameOfMatch)) {
                        matchLive = matchLiveMap.get(nameOfMatch);
                        matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
                        if (currentPeriod != null) {
                            matchLive.setCurrentPeriod(currentPeriod);
                        }
                    } else {
                        matchLive = new MatchLiveBetting(betting, nameOfMatch, margin, minBet);
                        matchLiveMap.put(nameOfMatch, matchLive);
                    }

                    if (matchLive.isPossibleToMakeBet() && !matchLive.isBetIsDone()) {
                        if (!matchLive.isEventMade()) {
                            if (clickToMatch(driver, matchLive.getNameOfMatch())) {
                                checkGameScore(driver, matchLive);
                                checkMaximumBetSizeAndPossibleBetSelection(driver, matchLive);
                                goBackToOverview(driver);
                                matchLive.makeNextEvent();
                            }
                        }
                    } else {
                        System.out.println("Нельзя делать ставку");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//            for (WebElement element : elements) {
//                try {
//                    text = element.findElement(By.cssSelector("span.section-header-label")).getText();
//                    if (text != null) {
//                        if (text.contains("In-Play")) {
//                            List<WebElement> matchesElements = element.findElements(By.cssSelector("li.com-coupon-line-new-layout"));
//                            int size = matchesElements.size();
//                            for (int i = 0; i < size; i++) {
//                                WebElement matchElement = element.findElements(By.cssSelector("li.com-coupon-line-new-layout")).get(i);
//
//                                if (i > 2) {
//                                    JavascriptExecutor js = (JavascriptExecutor) driver;
//                                    js.executeScript("arguments[0].scrollIntoView();", matchesElements.get(i - 2));
//                                }
//
//                                String nameOfMatch = null;
//                                SelectionBet currentPeriod = null;
//                                boolean isPossibleToMakeBet;
//                                List<WebElement> teamNamesElements = matchElement.findElement(By.cssSelector("div.teams-container")).findElements(By.cssSelector("span.team-name"));
//                                String text1 = teamNamesElements.get(0).getText();
//                                String text2 = teamNamesElements.get(1).getText();
//                                if (text1 != null && text2 != null) {
//                                    nameOfMatch = text1 + " vs " + text2;
//                                }
//
//                                String currentPeriodString = matchElement.findElement(By.cssSelector("span.ui-status-format")).getText();
//                                if (currentPeriodString != null) {
//                                    switch (currentPeriodString) {
//                                        case "Q1":
//                                            currentPeriod = SelectionBet.Q1;
//                                            break;
//                                        case "Q2":
//                                            currentPeriod = SelectionBet.Q2;
//                                            break;
//                                        case "Q3":
//                                            currentPeriod = SelectionBet.Q3;
//                                            break;
//                                        case "Q4":
//                                            currentPeriod = SelectionBet.Q4;
//                                            break;
//                                        case "Live":
//                                            currentPeriod = SelectionBet.Q1;
//                                            break;
//                                        case "HT":
//                                            currentPeriod = SelectionBet.Q3;
//                                            break;
//                                        default:
//                                            currentPeriod = SelectionBet.Q1;
//                                            break;
//                                    }
//                                }
//
//                                try {
//                                    WebElement betElement = matchElement.findElement(By.cssSelector("li.selection"));
//                                    Thread.sleep(1000);
//                                    betElement.click();
//                                    Thread.sleep(1000);
//                                    driver.findElement(By.cssSelector("a.remove-all-bets")).click();
//                                    isPossibleToMakeBet = true;
//                                } catch (Exception e) {
//                                    isPossibleToMakeBet = false;
//                                }
//
//                                MatchLiveBetting matchLive;
//                                if (matchLiveMap.containsKey(nameOfMatch)) {
//                                    matchLive = matchLiveMap.get(nameOfMatch);
//                                    matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
//                                    if (currentPeriod != null) {
//                                        matchLive.setCurrentPeriod(currentPeriod);
//                                    }
//                                } else {
//                                    matchLive = new MatchLiveBetting(betting, nameOfMatch, margin, minBet);
//                                    matchLiveMap.put(nameOfMatch, matchLive);
//                                }
//
//                                if (matchLive.isPossibleToMakeBet() && !matchLive.isBetIsDone()) {
//                                    if (!matchLive.isEventMade()) {
//                                        if (clickToMatch(driver, matchLive.getNameOfMatch())) {
//                                            checkGameScore(driver, matchLive);
//                                            checkMaximumBetSizeAndPossibleBetSelection(driver, matchLive);
//                                            goBackToOverview(driver);
//                                            matchLive.makeNextEvent();
//                                        }
//                                    }
//                                } else {
//                                    System.out.println("Нельзя делать ставку");
//                                }
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            System.out.println("Обновили все матчи, смотрим можно ли в каком то матче сделать событие");
//            for (String name : matchLiveMap.keySet()) {
//                MatchLiveBetting matchLive = matchLiveMap.get(name);
//                System.out.println("Проверяем матч - " + matchLive.getNameOfMatch());
//                System.out.println("Четверть - " + matchLive.getCurrentPeriod());
//                System.out.println("Ставить ставки - " + matchLive.isPossibleToMakeBet());
//                if (matchLive.isPossibleToMakeBet() && !matchLive.isBetIsDone()) {
//                    if (!matchLive.isEventMade()) {
//                        if (clickToMatch(driver, matchLive.getNameOfMatch())) {
//                            checkGameScore(driver, matchLive);
//                            checkMaximumBetSizeAndPossibleBetSelection(driver, matchLive);
//                            matchLive.makeNextEvent();
//                        }
//                    }
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clickOnInvisibleElement(WebElement element, WebDriver driver) {
        if(element!=null){
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
    }

    @Override
    public boolean clickToMatch(WebDriver driver, String nameOfMatch) {
        try {
            goBackToOverview(driver);
            String[] split = nameOfMatch.split(" vs ");
            System.out.println("Пытаемся кликнуть на матч - " + nameOfMatch);
            WebElement element;
            try {
                element = driver.findElement(By.xpath("//span[contains(text(),'" + split[0] + "')]"));//[contains(text(),'" + arr[i] + "')]
            } catch (Exception e) {
                element = driver.findElement(By.xpath("//span[contains(text(),'" + split[1] + "')]"));
            }
            clickOnInvisibleElement(element, driver);
            Thread.sleep(2000);

            try {
                driver.findElement(By.xpath("//span[contains(text(),'Ok, I get it')]")).click();//Ok, I get it
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean checkGameScore(WebDriver driver, MatchLiveBetting matchLive) {
        try {
            Map<SelectionBet, Integer> matchScoreStatistic = matchLive.getMatchScoreStatistic();
            List<WebElement> home = driver.findElements(By.cssSelector("td.ui-quarter-by-quarter-home"));
            List<WebElement> away = driver.findElements(By.cssSelector("td.ui-quarter-by-quarter-away"));
            for (int i = 1; i < 5; i++) {
                String firstTeamScoreString = home.get(i - 1).getText();
                int firstTeamScore = 0;
                try {
                    firstTeamScore = Integer.parseInt(firstTeamScoreString);
                } catch (Exception ignored) {
                }

                String secondTeamScoreString = away.get(i - 1).getText();
                int secondTeamScore = 0;
                try {
                    secondTeamScore = Integer.parseInt(secondTeamScoreString);
                } catch (Exception ignored) {
                }
                int score = firstTeamScore + secondTeamScore;
                matchScoreStatistic.put(SelectionBet.valueOf("Q" + i), score);
            }

            if (matchScoreStatistic.get(SelectionBet.Q1) != null && matchScoreStatistic.get(SelectionBet.Q2) != null) {
                matchScoreStatistic.put(SelectionBet.Half1st, matchScoreStatistic.get(SelectionBet.Q1) + matchScoreStatistic.get(SelectionBet.Q2));
            }

            for (SelectionBet selectionBet : matchLive.getMatchScoreStatistic().keySet()) {
                Integer score = matchLive.getMatchScoreStatistic().get(selectionBet);
//                System.out.println(selectionBet + " : " + score);
                if (score == 0) {
                    continue;
                } else if (score % 2 == 0) {
                    matchLive.getMatchOddEvenStatistic().put(selectionBet, OddEven.Even);//Even
                } else if (score % 2 == 1) {
                    matchLive.getMatchOddEvenStatistic().put(selectionBet, OddEven.Odd);//Odd
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean checkMaximumBetSizeAndPossibleBetSelection(WebDriver driver, MatchLiveBetting matchLive) {
        try {
            List<SelectionBet> possibleBetSelectionBet = matchLive.getPossibleBetSelectionBet();
            possibleBetSelectionBet.clear();
            List<WebElement> elements = driver.findElements(By.cssSelector("div.mod-minimarketview"));
            for (WebElement element : elements) {
                String title = element.findElement(By.cssSelector("span.title")).getText();
                if (title.contains("Total Points Odd/")) {
                    possibleBetSelectionBet.add(SelectionBet.Match);
                    if (matchLive.getMaxBet() == 0) {
                        Double maxBetDouble = null;
                        try {
                            WebElement oddElement = element.findElement(By.cssSelector("span.ui-runner-price"));
                            JavascriptExecutor js = (JavascriptExecutor) driver;
                            js.executeScript("arguments[0].scrollIntoView();", element);
                            oddElement.click();
                            WebElement betField = driver.findElement(By.cssSelector("input.ui-market-action"));
                            betField.click();
                            Thread.sleep(1000);
                            betField.sendKeys("9999999");
                            driver.findElement(By.cssSelector("div.ui-runner-price")).click();
                            Thread.sleep(1000);
                            String maxBetString = driver.findElement(By.cssSelector("span.set-max-stake")).getText();//Max €403.20
                            driver.findElement(By.cssSelector("a.remove-all-bets")).click();
                            Thread.sleep(1000);
                            if (maxBetString.contains("€")) {
                                String[] maxBetSplit = maxBetString.split("€");
                                maxBetDouble = Double.parseDouble(maxBetSplit[1]);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (maxBetDouble != null) {
                            matchLive.setMaxBet(maxBetDouble);
                        }
                    }
                }
            }

            List<WebElement> oddEvenElements = driver.findElements(By.cssSelector("li.yesnomarkets-market"));
            for (WebElement element : oddEvenElements) {
                String text = element.findElement(By.cssSelector("span.market-name")).getText();
                if (text.contains("1st Quarter")) {
                    possibleBetSelectionBet.add(SelectionBet.Q1);
                } else if (text.contains("2nd Quarter")) {
                    possibleBetSelectionBet.add(SelectionBet.Q2);
                } else if (text.contains("3rd Quarter")) {
                    possibleBetSelectionBet.add(SelectionBet.Q3);
                } else if (text.contains("4th Quarter")) {
                    possibleBetSelectionBet.add(SelectionBet.Q4);
                } else if (text.contains("1st Half")) {
                    possibleBetSelectionBet.add(SelectionBet.Half1st);
                } else if (text.contains("2nd Half")) {
                    possibleBetSelectionBet.add(SelectionBet.Half2nd);
                } else if (text.contains("Odd or Even Total Points") && !possibleBetSelectionBet.contains(SelectionBet.Match)) {
                    possibleBetSelectionBet.add(SelectionBet.Match);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean goBackToOverview(WebDriver driver) {
        try {
            driver.get("https://www.betfair.com/sport/basketball");
            Thread.sleep(1000);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean betAlreadyNotPlayed(Bet lastBet, WebDriver driver) {
        Boolean betIsNotPlayed = true;
        try {
            updatePage(driver);
            driver.findElement(By.xpath("//a[text()='My Bets']")).click();
            driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
            try {
                WebElement element = driver.findElement(By.xpath("//strong[contains(text(),'You have no bets in this time period')]"));//Ok, I get it
                if (element.isDisplayed()) {
                    return true;
                }
            } catch (Exception ignored) {
            }
            driver.manage().timeouts().implicitlyWait(7, TimeUnit.SECONDS);

            Thread.sleep(1000);
            List<WebElement> currentBets = driver.findElements(By.cssSelector("tr.bet--row"));

            for (WebElement betElement : currentBets) {
                try {
                    String nameOfMatch;
                    SelectionBet selectionBet;
                    String nameFromSite = betElement.findElement(By.cssSelector("span.bet__main-description")).getText();
                    nameOfMatch = nameFromSite.replace(" v ", " vs ");
                    String dataString = betElement.findElement(By.cssSelector("span[ng-if='bet.marketName']")).findElement(By.cssSelector("span")).getText();
                    if (dataString.contains("1st Quarter")) {
                        selectionBet = SelectionBet.Q1;
                    } else if (dataString.contains("2nd Quarter")) {
                        selectionBet = SelectionBet.Q2;
                    } else if (dataString.contains("3rd Quarter")) {
                        selectionBet = SelectionBet.Q3;
                    } else if (dataString.contains("4th Quarter")) {
                        selectionBet = SelectionBet.Q4;
                    } else if (dataString.contains("1st Half")) {
                        selectionBet = SelectionBet.Half1st;
                    } else if (dataString.contains("2nd Half")) {
                        selectionBet = SelectionBet.Half2nd;
                    } else {
                        selectionBet = SelectionBet.Match;
                    }

                    if (nameOfMatch.contains(lastBet.getMatchName())) {
                        if (Bet.compareBetSelection(selectionBet, lastBet.getSelectionBet())) {
                            lastBet.zeroCountCheck();
                            betIsNotPlayed = false;
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return betIsNotPlayed;
    }

    @Override
    public Bet makeBet(Bet bet, WebDriver driver, MatchLiveBetting matchLive) {
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
            clickToMatch(driver, bet.getMatchName());
            bet.setMatchPeriod(matchLive.getCurrentPeriodInt());
            try {
                WebElement elementToMakeBet = null;
                String textToSearch = null;
                switch (bet.getSelectionBet()) {
                    case Q1:
                        textToSearch = "1st Quarter";
                        break;
                    case Q2:
                        textToSearch = "2nd Quarter";
                        break;
                    case Q3:
                        textToSearch = "3rd Quarter";
                        break;
                    case Q4:
                        textToSearch = "4th Quarter";
                        break;
                    case Half1st:
                        textToSearch = "1st Half";
                        break;
                    case Half2nd:
                        textToSearch = "2nd Half";
                        break;
                    case Match:
                        textToSearch = "Odd or Even Total Points";
                }

                List<WebElement> oddEvenElements = driver.findElements(By.cssSelector("li.yesnomarkets-market"));
                for (WebElement element : oddEvenElements) {
                    String text = element.findElement(By.cssSelector("span.market-name")).getText();
                    if (textToSearch != null && text.contains(textToSearch)) {
                        List<WebElement> a = element.findElements(By.cssSelector("a"));
                        if (bet.getOddEven() == OddEven.Odd) {
                            elementToMakeBet = a.get(0);
                        } else {
                            elementToMakeBet = a.get(1);
                        }

                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        js.executeScript("arguments[0].scrollIntoView();", element);
                        break;
                    }
                }
                clickOnInvisibleElement(elementToMakeBet, driver);
                WebElement betField = driver.findElement(By.cssSelector("input.ui-market-action"));
                betField.click();
                Thread.sleep(1000);
                betField.sendKeys(String.valueOf(bet.getPrice()));
                Thread.sleep(3000);
                driver.findElement(By.cssSelector("button.place-bets-button")).click();
                Thread.sleep(7000);
                try {
                    driver.findElement(By.xpath("//span[contains(text(),'Your bet(s) have been placed.')]"));
                    driver.findElement(By.cssSelector("button.close-betslip")).click();
                    betAlreadyNotDone = false;
                } catch (Exception e) {
                    try {
                        driver.findElement(By.cssSelector("a.remove-all-bets")).click();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (betAlreadyNotDone) {
                    Boolean b = betAlreadyNotPlayed(bet, driver);
                    if (b != null) {
                        betAlreadyNotDone = b;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!betAlreadyNotDone) {
            bet.setTimeBet(System.currentTimeMillis());
        }
        return bet;
    }

    @Override
    public Bet checkBet(Bet bet, WebDriver driver) {
        try {
            updatePage(driver);
            driver.findElement(By.xpath("//a[text()='My Bets']")).click();
            Thread.sleep(1000);
            driver.findElement(By.xpath("//button[text()='Past']")).click();
            Thread.sleep(1000);
            List<WebElement> historyBets = driver.findElements(By.cssSelector("tr.bet--row"));

            for (WebElement betElement : historyBets) {
                try {
                    SelectionBet selectionBet = null;
                    Boolean isWin;
                    String nameFromSite = betElement.findElement(By.cssSelector("span.bet__main-description")).getText();
                    String[] split = nameFromSite.split(" v ");

                    String dataString = betElement.findElement(By.cssSelector("span[ng-if='bet.marketName']")).findElement(By.cssSelector("span")).getText();
                    if (dataString.contains("1st Quarter")) {
                        selectionBet = SelectionBet.Q1;
                    } else if (dataString.contains("2nd Quarter")) {
                        selectionBet = SelectionBet.Q2;
                    } else if (dataString.contains("3rd Quarter")) {
                        selectionBet = SelectionBet.Q3;
                    } else if (dataString.contains("4th Quarter")) {
                        selectionBet = SelectionBet.Q4;
                    } else if (dataString.contains("1st Half")) {
                        selectionBet = SelectionBet.Half1st;
                    } else if (dataString.contains("2nd Half")) {
                        selectionBet = SelectionBet.Half2nd;
                    } else {
                        selectionBet = SelectionBet.Match;
                    }

                    String isWinText = betElement.findElement(By.cssSelector("td.td-status-row")).findElements(By.cssSelector("span")).get(1).getText();
                    isWin = isWinText.contains("Won");
                    if (bet.getMatchName().contains(split[0])) {
                        if (selectionBet == bet.getSelectionBet()) {
                            bet.setWin(isWin);
                            System.out.println("Bet is setted win to " + isWin);
                        }
                    }
                    if (bet.getWin() != null) {
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bet;
    }

    @Override
    public String getNameOfSite() {
        return "betfair.com";
    }

    @Override
    public String toString() {
        return "www.betfair.com";
    }

    private void updatePage(WebDriver driver) {
        try {
            System.out.println("Обновляем страницу.");
            WebElement element1 = driver.findElement(By.xpath("//a[text()='Responsible Gambling']"));//Last Login   By.xpath("//a[text()='Responsible Gambling']")
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView();", element1);
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
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            driver.switchTo().window(driver.getWindowHandles().toArray()[0].toString());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.get("https://www.betfair.com/sport");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

