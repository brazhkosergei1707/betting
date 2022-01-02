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

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BettingParserPariMatchOldVersion implements BettingParser {

    private static Logger log = Logger.getLogger(BettingParserPariMatchOldVersion.class);

    private SimpleDateFormat dateFormat = new SimpleDateFormat();
    @Override
    public boolean logIn(String username, String password, WebDriver driver) {
        try {
            log.info("Заходим на сайт " + getNameOfSite());
            driver.get("https://www.parimatch.com/en/");
            Thread.sleep(3000);
            driver.findElement(By.cssSelector("a.login")).click();
            Thread.sleep(8000);
            WebElement userName = driver.findElement(By.cssSelector("input[name='username']"));
            userName.click();
            Thread.sleep(1000);
            userName.sendKeys(username);
            Thread.sleep(1000);
            WebElement passwd = driver.findElement(By.cssSelector("input[name='passwd']"));
            passwd.click();
            Thread.sleep(1000);
            passwd.sendKeys(password);
            Thread.sleep(2000);
            driver.findElement(By.cssSelector("button.ok")).click();
            Thread.sleep(3000);
            String windowHandles = driver.getWindowHandle();
            driver.findElement(By.cssSelector("a.head-section__link-oldstyle")).click();
            Thread.sleep(3000);
            driver.get("https://classic.parimatch.com/en/live.html");
            Thread.sleep(2000);
            WebDriver window = driver.switchTo().window(driver.getWindowHandles().toArray()[1].toString());
            Thread.sleep(1000);
            window.close();
            Thread.sleep(1000);
            driver.switchTo().window(windowHandles);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Проблемы во время логирования на сайте " + getNameOfSite());
            return false;
        }
        log.info("Логирование успешно на сайте " + getNameOfSite());
        return true;
    }
    @Override
    public boolean logOut(WebDriver driver) {
        try {
            driver.findElement(By.cssSelector("a.logout")).click();
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    @Override
    public void updateCurrentMatchesLive(Map<String, MatchLiveBetting> currentMatches, WebDriver driver, double margin, double minBet, Betting betting) {
        try {
            driver.findElement(By.cssSelector("a[href='./live.html']")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            log.info("Сканируем матчи на сайте " + getNameOfSite() + ". Первое сканирование.");

            boolean isMatches;
            List<WebElement> basketBallElement = null;
            try {
                basketBallElement = driver.findElement(By.id("inplay")).findElement(By.cssSelector("div.wrapper")).findElements(By.cssSelector("div.basketball"));
                isMatches = true;
            } catch (Exception e) {
                isMatches = false;
                log.info("Сейчас на сайте " + getNameOfSite() + " нет матчей");
            }
            if (isMatches) {
                List<WebElement> matchesList = new ArrayList<>();
                for (WebElement element : basketBallElement) {
                    matchesList.addAll(element.findElements(By.cssSelector("table.processed")));
                }

                if (matchesList.size() > 0) {
                    String nameOfMatch = null;
                    WebElement element;
                    int size = matchesList.size();
                    for (int i = 0; i < size; i++) {
                        try {
                            matchesList.clear();
                            basketBallElement = driver.findElement(By.id("inplay")).findElement(By.cssSelector("div.wrapper")).findElements(By.cssSelector("div.basketball"));
                            for (WebElement element1 : basketBallElement) {
                                matchesList.addAll(element1.findElements(By.cssSelector("table.processed")));
                            }

                            element = matchesList.get(i);
                            if (element != null) {
                                String matchDataString = element.findElement(By.cssSelector("td.td_n")).findElement(By.cssSelector("a")).getText();
                                if (matchDataString.contains("*")) {
                                    continue;
                                }
                                String[] split = matchDataString.split("\\n");
                                if (split[0].length() < 2) {
                                    continue;
                                }

                                nameOfMatch = split[0].replace(" - ", " vs ");

                                MatchLiveBetting matchLive;
                                if (currentMatches.containsKey(nameOfMatch)) {
                                    matchLive = currentMatches.get(nameOfMatch);
                                } else {
                                    matchLive = new MatchLiveBetting(betting, nameOfMatch, margin, minBet);
                                    currentMatches.put(nameOfMatch, matchLive);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Double parseDouble(String balance) {
        Double doubleBalance = null;
        try {
            doubleBalance = Double.parseDouble(balance.split(" ")[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doubleBalance;
    }
    private Double getMaxBet(String nameOfMatch, WebDriver driver) {
        Double maxBet = null;
        try {
            boolean click = clickToMatch(driver, nameOfMatch);
            if (click) {
                Thread.sleep(1000);
                WebElement totalOddEven = driver.findElement(By.xpath("//i[text()='Total:']/.."));
                totalOddEven.findElements(By.cssSelector("i")).get(1).click();
                driver.findElement(By.xpath("//button[text()='PLACE BET >']")).click();

                WebDriver robots = driver.switchTo().window(driver.getWindowHandles().toArray()[1].toString());
                List<WebElement> elements = robots.findElement(By.id("wb")).findElements(By.cssSelector("td"));
                String text = elements.get(elements.size() - 1).getText();
                try {
                    maxBet = Double.parseDouble(text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                robots.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.switchTo().window(driver.getWindowHandles().toArray()[0].toString());
            driver.findElement(By.cssSelector("a[href='./live.html']")).click();
        }
        return maxBet;
    }
    @Override
    public void setCurrentBalance(WebDriver driver) {
        try {
            driver.findElement(By.id("balance-button")).click();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String balance;
            balance = driver.findElement(By.cssSelector("span#ros")).getText();
            if (balance == null) {
                return;
            }
            Double balanceDouble = parseDouble(balance);
            log.info("Текущий баланс на сайте " + getNameOfSite() + " равен: " + balanceDouble);
            StartFrame.getStorage(this).setCurrentBalance(balanceDouble);
            StartFrame.getStorage(this).setCurrentBalanceString(balance);
            driver.findElement(By.id("balance-button")).click();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void updateHistoryBet(WebDriver driver, List<Bet> historyList) {

        try {
            WebElement elementLive = driver.findElement(By.cssSelector("a[href='./live.html']"));
            try {
                Actions actions = new Actions(driver);
                actions.keyDown(Keys.LEFT_CONTROL)
                        .click(elementLive)
                        .keyUp(Keys.LEFT_CONTROL)
                        .build()
                        .perform();
                try {
                    Thread.sleep(1000);
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
            } catch (Exception e) {
                return;
            } finally {
                driver.switchTo().window(driver.getWindowHandles().toArray()[0].toString());
            }

            Thread.sleep(1000);
            driver.findElement(By.cssSelector("a[href='./history.html']")).click();
            Thread.sleep(3000);
            //To open 40 bet on page

            try {
                driver.findElement(By.cssSelector("div.select")).findElements(By.cssSelector("option")).get(2).click();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(1000);

            List<WebElement> historyBets = driver.findElements(By.cssSelector("div.lr"));
            Bet bet;
            long dateOfBet = 0L;
            String dateString;
            Date date;
            String nameOfMatch = null;
            OddEven oddEven = null;
            SelectionBet selectionBet = null;
            int periodBetInt = 0;
            Double priceOfBet = 0.0;
            String priceOfBetString;
            Boolean isWin;
            String isWinString = null;
            if (historyBets != null) {
                System.out.println("Найдено " + historyBets.size() + " ставок.");
                for (WebElement betElement : historyBets) {
                    try {
                        try {
                            isWinString = betElement.findElement(By.cssSelector("span.ss")).getText();//0,00 EUR LOOOSSSEEEE
                        } catch (Exception ignored) {
                        }
                        if (isWinString != null) {
                            if (isWinString.length() < 2) {
                                continue;
                            }
                            if (isWinString.contains("0.00")) {
                                isWin = false;
                            } else {
                                isWin = true;
                            }
                        } else {
                            continue;
                        }

                        dateString = betElement.findElement(By.cssSelector("div.col-1")).getText();//31.08.2017 09:30
                        if (dateString != null) {
                            String[] split = dateString.split(" ");
                            dateString = split[1];
                            dateFormat.applyPattern("mm:ss");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                            date = dateFormat.parse(dateString);
                            dateOfBet = date.getTime();
                        }
                        String betDataString = betElement.findElement(By.cssSelector("div.col-2")).getText();
                        if (betDataString != null) {
                            String[] splitAllData = betDataString.split("\\. ");
                            String s;
                            String selectionBetString = null;
                            String oddEvenString = null;
                            for (int i = 0; i < splitAllData.length; i++) {
                                s = splitAllData[i];
                                if (s.contains(" - ")) {
                                    String[] split = s.split(" - ");
                                    nameOfMatch = split[0] + " vs " + split[1];
                                    Thread.sleep(500);
                                    if (i < splitAllData.length - 1) {
                                        selectionBetString = splitAllData[i + 1];
                                    }

                                    oddEvenString = splitAllData[splitAllData.length - 1];
                                    break;
                                }
                            }

                            if (selectionBetString != null) {
                                if (selectionBetString.contains("Quarter 1")) {
                                    selectionBet = SelectionBet.Q1;
                                    periodBetInt = 1;
                                } else if (selectionBetString.contains("Quarter 2")) {
                                    selectionBet = SelectionBet.Q2;
                                    periodBetInt = 2;
                                } else if (selectionBetString.contains("Quarter 3")) {
                                    selectionBet = SelectionBet.Q3;
                                    periodBetInt = 3;
                                } else if (selectionBetString.contains("Quarter 4")) {
                                    selectionBet = SelectionBet.Q4;
                                    periodBetInt = 4;
                                } else {
                                    selectionBet = SelectionBet.Match;
                                    periodBetInt = 4;
                                }
                            }

                            if (oddEvenString != null) {
                                if (oddEvenString.contains("odd")) {
                                    oddEven = OddEven.Odd;
                                } else if (oddEvenString.contains("even")) {
                                    oddEven = OddEven.Even;
                                }
                            }
                        }

                        priceOfBetString = betElement.findElement(By.cssSelector("div.lrHeader")).findElements(By.cssSelector("span")).get(4).getText();//1,00 EUR
                        if (priceOfBetString != null) {
                            String[] split = priceOfBetString.split(" ");
                            priceOfBet = Double.parseDouble(split[0]);
                        }
//                        System.out.println("Имя матча - " + nameOfMatch);
//                        System.out.println("Чет/нечет - " + oddEven);
//                        System.out.println("Выбор ставки - " + selectionBet);
//                        System.out.println("Выбор ставки INT - " + periodBetInt);
//                        System.out.println("Цена ставки - " + priceOfBet);
//                        System.out.println("Выиграла ли ставка - " + isWin);
//                        System.out.println("===================================================");
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
            driver.findElement(By.cssSelector("a[href='./live.html']")).click();
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void updateCurrentBet(WebDriver driver, List<Bet> currentBetsList) {
        try {

            WebElement elementLive = driver.findElement(By.cssSelector("a[href='./live.html']"));
            try {
                Actions actions = new Actions(driver);
                actions.keyDown(Keys.LEFT_CONTROL)
                        .click(elementLive)
                        .keyUp(Keys.LEFT_CONTROL)
                        .build()
                        .perform();
                try {
                    Thread.sleep(1000);
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
            } catch (Exception e) {
                return;
            } finally {
                driver.switchTo().window(driver.getWindowHandles().toArray()[0].toString());
            }


            driver.findElement(By.cssSelector("a[href='./history.html']")).click();
            Thread.sleep(1000);
            driver.findElement(By.id("unresolved")).click();
            Thread.sleep(1000);
            driver.findElement(By.id("unresolved")).click();
            Thread.sleep(3000);

            List<WebElement> historyBets = null;
            driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
            try {
                driver.findElement(By.xpath("//font[text()='Record not found']"));
            } catch (Exception e) {
                historyBets = driver.findElements(By.cssSelector("div.lr"));
            }
            driver.manage().timeouts().implicitlyWait(7, TimeUnit.SECONDS);
            if (historyBets != null) {
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

                for (WebElement betElement : historyBets) {
                    try {
                        dateString = betElement.findElement(By.cssSelector("div.col-1")).getText();//31.08.2017 09:30
                        if (dateString != null) {
                            String[] split = dateString.split(" ");
                            dateString = split[1];
                            dateFormat.applyPattern("mm:ss");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                            date = dateFormat.parse(dateString);
                            dateOfBet = date.getTime();
                        }
                        String betDataString = betElement.findElement(By.cssSelector("div.col-2")).getText();

                        if (betDataString != null) {
                            String[] splitAllData = betDataString.split("\\. ");

                            String s;
                            String selectionBetString = null;
                            String oddEvenString = null;
                            for (int i = 0; i < splitAllData.length; i++) {
                                s = splitAllData[i];
                                if (s.contains(" - ")) {
                                    String[] split = s.split(" - ");
                                    nameOfMatch = split[0] + " vs " + split[1];
                                    Thread.sleep(500);
                                    if (i < splitAllData.length - 1) {
                                        selectionBetString = splitAllData[i + 1];
                                    }
                                    oddEvenString = splitAllData[splitAllData.length - 1];
                                    break;
                                }
                            }

                            if (selectionBetString != null) {
                                if (selectionBetString.contains("Quarter 1")) {
                                    selectionBet = SelectionBet.Q1;
                                    periodBetInt = 1;
                                } else if (selectionBetString.contains("Quarter 2")) {
                                    selectionBet = SelectionBet.Q2;
                                    periodBetInt = 2;
                                } else if (selectionBetString.contains("Quarter 3")) {
                                    selectionBet = SelectionBet.Q3;
                                    periodBetInt = 3;
                                } else if (selectionBetString.contains("Quarter 4")) {
                                    selectionBet = SelectionBet.Q4;
                                    periodBetInt = 4;
                                } else {//Game Total - Odd/Even,
                                    selectionBet = SelectionBet.Match;
                                    periodBetInt = 4;
                                }
                            }

                            if (oddEvenString != null) {
                                if (oddEvenString.contains("odd")) {
                                    oddEven = OddEven.Odd;
                                } else if (oddEvenString.contains("even")) {
                                    oddEven = OddEven.Even;
                                }
                            }
                        }
                        priceOfBetString = betElement.findElement(By.cssSelector("div.lrHeader")).findElements(By.cssSelector("span")).get(4).getText();//1,00 EUR
                        if (priceOfBetString != null) {
                            String[] split = priceOfBetString.split(" ");
                            priceOfBet = Double.parseDouble(split[0]);
                        }
//                        System.out.println("Имя матча - " + nameOfMatch);
//                        System.out.println("Чет/нечет - " + oddEven);
//                        System.out.println("Выбор ставки - " + selectionBet);
//                        System.out.println("Выбор ставки INT - " + periodBetInt);
//                        System.out.println("Цена ставки - " + priceOfBet);
//                        System.out.println("===================================================");
                        boolean newBet = true;
                        for (Bet betList : currentBetsList) {
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
                            currentBetsList.add(bet);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            driver.findElement(By.cssSelector("a[href='./live.html']")).click();
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void lookingForMatches(WebDriver driver, Map<String, MatchLiveBetting> matchLiveMap, double margin, double minBet, Betting betting) {
        try {
            try {
                WebElement elementLive = driver.findElement(By.cssSelector("a[href='./live.html']"));
                Actions actions = new Actions(driver);
                actions.keyDown(Keys.LEFT_CONTROL)
                        .click(elementLive)
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
            } catch (Exception e) {
                return;
            } finally {
                driver.switchTo().window(driver.getWindowHandles().toArray()[0].toString());
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                driver.findElement(By.xpath("//a[text()='Clear']")).click();
            } catch (Exception ignored) {
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean isMatches;
            List<WebElement> basketBallElement = null;

            driver.findElement(By.cssSelector("a[href='./live.html']")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                basketBallElement = driver.findElement(By.id("inplay")).findElement(By.cssSelector("div.wrapper")).findElements(By.cssSelector("div.basketball"));
                isMatches = true;
            } catch (Exception e) {
                isMatches = false;
                log.info("Сейчас на сайте " + getNameOfSite() + " нет матчей");
            }

            List<WebElement> matchesList = new ArrayList<>();

            if (basketBallElement != null) {
                for (WebElement element : basketBallElement) {
                    matchesList.addAll(element.findElements(By.cssSelector("table.processed")));//TODO org.openqa.selenium.StaleElementReferenceException: stale element reference: element is not attached to the page document
                }
            }

            driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
            if (matchLiveMap != null) {
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
                        String xpath = "//a[contains(text(),'" + split[0] + "')]";
//                        System.out.println("Ищем - " + xpath);
                        driver.findElement(By.xpath(xpath));
                        matchFinish = false;
                    } catch (Exception e) {
                        try {
                            String xpath = "//a[contains(text(),'" + split[1] + "')]";
//                            System.out.println("Ищем - " + xpath);
                            driver.findElement(By.xpath(xpath));
                            matchFinish = false;
                        } catch (Exception ex) {
                            try {
                                String xpath = "//small[contains(text(),'" + split[0] + "')]";
//                                System.out.println("Ищем - " + xpath);
                                driver.findElement(By.xpath(xpath));//Rain or Shine Elasto Painters  //Rain or Shine Elasto Painters
                                matchFinish = false;
                            } catch (Exception e1) {
                                try {
                                    String xpath = "//small[contains(text(),'" + split[1] + "')]";
//                                    System.out.println("Ищем - " + xpath);
                                    driver.findElement(By.xpath(xpath));//Rain or Shine Elasto Painters  //Rain or Shine Elasto Painters
                                    matchFinish = false;
                                } catch (Exception exept) {
                                    System.out.println("Не нашли матч: " + matchLive.getNameOfMatch());
                                }
                            }
                        }
                    }

                    if (matchFinish) {
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
            }
            driver.manage().timeouts().implicitlyWait(7, TimeUnit.SECONDS);

            if (isMatches) {
                matchesList.clear();
                basketBallElement = driver.findElement(By.id("inplay")).findElement(By.cssSelector("div.wrapper")).findElements(By.cssSelector("div.basketball"));
                List<WebElement> elements;
                for (WebElement element : basketBallElement) {
                    try {
                        elements = element.findElements(By.cssSelector("table.processed"));
                    } catch (Exception e) {
                        continue;
                    }
                    if (elements != null) {
                        matchesList.addAll(elements);
                        elements = null;
                    }
                }

                if (matchesList.size() > 0) {
                    MatchLiveBetting matchLive = null;
                    String nameOfMatch = null;
                    SelectionBet currentPeriod = null;
                    Double maxBet = null;
                    boolean isPossibleToMakeBet;
                    log.info("Найдено " + matchesList.size() + " матчей.");
                    System.out.println("Найдено " + matchesList.size() + " матчей.");
                    WebElement element;
                    int size = matchesList.size();
                    for (int i = 0; i < size; i++) {
                        try {
                            if (matchLive != null) {
                                if (matchLive.isPossibleToMakeBet() && !matchLive.isBetIsDone()) {
                                    if (!matchLive.isEventMade()) {
                                        if (clickToMatch(driver, matchLive.getNameOfMatch())) {
                                            checkGameScore(driver, matchLive);
                                            checkMaximumBetSizeAndPossibleBetSelection(driver, matchLive);
                                            goBackToOverview(driver);
                                            matchLive.makeNextEvent();
                                        }
                                    } else {
                                        System.out.println("Событие уже есть для этого матча");
                                    }
                                } else {
                                    System.out.println("Или ставка сделана, или нельзя ставить ставку.");
                                }
                            }
                            matchLive = null;
                            matchesList.clear();
                            basketBallElement = driver.findElement(By.id("inplay")).findElement(By.cssSelector("div.wrapper")).findElements(By.cssSelector("div.basketball"));
                            for (WebElement element1 : basketBallElement) {
                                matchesList.addAll(element1.findElements(By.cssSelector("table.processed")));
                            }

                            element = matchesList.get(i);
                            if (element != null) {
                                String matchDataString = element.findElement(By.cssSelector("td.td_n")).findElement(By.cssSelector("a")).getText();
//                                System.out.println("Вся информация о матче:" + matchDataString);//SBC Red Lions - CSB Blazers

                                if (matchDataString.contains("*")) {
                                    continue;
                                }

                                String[] split = matchDataString.split("\\n");
                                if (split[0].length() < 2) {
                                    continue;
                                }

                                nameOfMatch = split[0].replace(" - ", " vs ");

                                String currentPeriodString = element.findElement(By.cssSelector("td.td_n")).findElement(By.cssSelector("span.score")).getText();
//                                System.out.println("Текущий период матча: " + currentPeriodString);
                                if (currentPeriodString != null) {
                                    int a = currentPeriodString.indexOf("(");
                                    if (a > 0) {
                                        String correctCurrentPeriodString = currentPeriodString.substring(a, currentPeriodString.length());
//                                        System.out.println("Правильный счет матча " + correctCurrentPeriodString);

                                        String[] splitPeriod = correctCurrentPeriodString.split(",");
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
                                            default:
                                                currentPeriod = SelectionBet.OT;
                                        }
                                    } else {
                                        currentPeriod = SelectionBet.Q1;
                                    }
                                }
                            }

                            try {
                                String td = element.findElements(By.cssSelector("td")).get(9).getText();
                                if (td.length() > 1) {
                                    isPossibleToMakeBet = true;
                                } else {
                                    isPossibleToMakeBet = false;
                                }
                            } catch (Exception e) {
                                isPossibleToMakeBet = false;
                            }


                            if (matchLiveMap.containsKey(nameOfMatch)) {
                                matchLive = matchLiveMap.get(nameOfMatch);
                                matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
                                if (currentPeriod != null) {
                                    matchLive.setCurrentPeriod(currentPeriod);
                                }
                            } else {
                                matchLive = new MatchLiveBetting(betting, nameOfMatch, margin, minBet);
                                if (maxBet != null) {
                                    matchLive.setMaxBet(maxBet);
                                }
                                matchLiveMap.put(nameOfMatch, matchLive);
                            }

                            if (i == size - 1) {
                                if (matchLive != null) {
                                    if (matchLive.isPossibleToMakeBet() && !matchLive.isBetIsDone()) {
                                        if (!matchLive.isEventMade()) {
                                            if (clickToMatch(driver, matchLive.getNameOfMatch())) {
                                                checkGameScore(driver, matchLive);
                                                checkMaximumBetSizeAndPossibleBetSelection(driver, matchLive);
                                                goBackToOverview(driver);
                                                matchLive.makeNextEvent();
                                            }
                                        } else {
                                            System.out.println("Событие уже есть для этого матча");
                                        }
                                    } else {
                                        System.out.println("Или ставка сделана, или нельзя ставить ставку.");
                                    }
                                    matchLive = null;
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

    @Override
    public boolean clickToMatch(WebDriver driver, String nameOfMatch) {
        boolean click = false;
        System.out.println("Имя матча на который будем кликать " + nameOfMatch);
        String nameOfMatchNew = nameOfMatch.replace(" vs ", " - ");
        try {
            WebElement element;
            try {
                element = driver.findElement(By.xpath("//a[contains(text(),'" + nameOfMatchNew + "')]"));
            } catch (Exception e) {
                String[] split = nameOfMatchNew.split(" - ");
                try {
                    element = driver.findElement(By.xpath("//small[contains(text(),'" + split[0] + "')]"));
                } catch (Exception ex) {
                    element = driver.findElement(By.xpath("//small[contains(text(),'" + split[1] + "')]"));
                }
            }

            if (element != null) {
                System.out.println("Элемент на который пытаемся кликнуть - " + element.getText());
//                Actions actions = new Actions(driver);
//                actions.moveToElement(element).click().build().perform();
                clickOnInvisibleElement(element, driver);
                click = true;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            click = false;
        }
        return click;
    }

    public void clickOnInvisibleElement(WebElement element, WebDriver driver) {
        String script = "var object = arguments[0];"
                + "var theEvent = document.createEvent(\"MouseEvent\");"
                + "theEvent.initMouseEvent(\"click\", true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
                + "object.dispatchEvent(theEvent);";

        ((JavascriptExecutor) driver).executeScript(script, element);
    }

    @Override
    public Boolean betAlreadyNotPlayed(Bet lastBet, WebDriver driver) {
        Boolean betAlreadyNotPlayed = true;
        try {
            driver.findElement(By.cssSelector("a[href='./history.html']")).click();
            Thread.sleep(1000);
            driver.findElement(By.id("unresolved")).click();
            Thread.sleep(500);
            driver.findElement(By.id("unresolved")).click();
            Thread.sleep(1000);

            List<WebElement> historyBets = null;
            driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
            try {
                driver.findElement(By.xpath("//font[text()='Record not found']"));///
            } catch (Exception e) {
                historyBets = driver.findElements(By.cssSelector("div.lr"));
            }
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            OddEven oddEven = null;
            SelectionBet selectionBet = null;
            if (historyBets != null) {
                System.out.println("Найдено " + historyBets.size() + " ставок.");
                for (WebElement betElement : historyBets) {
                    try {
                        Thread.sleep(1000);
                        String betDataString = betElement.findElement(By.cssSelector("div.col-2")).getText();
                        System.out.println("Информация с сайта:" + betDataString);

                        if (betDataString != null) {
//                            String[] splitAllData = betDataString.split("\\. ");
//                            String s;
//                            String selectionBetString = null;
//                            String oddEvenString =null;
//                            for(int i=0;i<splitAllData.length;i++){
//                                s = splitAllData[i];
//                                if(s.contains(" - ")){
//                                    String[] split = s.split(" - ");
//                                    nameOfMatch = split[0] + " vs " + split[1];
//                                    if(i<splitAllData.length-1){
//                                        selectionBetString = splitAllData[i+1];
//                                    }
//                                    oddEvenString = splitAllData[splitAllData.length-1];
//                                    break;
//                                }
//                            }
                            if (betDataString.contains("Quarter 1")) {
                                selectionBet = SelectionBet.Q1;
                            } else if (betDataString.contains("Quarter 2")) {
                                selectionBet = SelectionBet.Q2;
                            } else if (betDataString.contains("Quarter 3")) {
                                selectionBet = SelectionBet.Q3;
                            } else if (betDataString.contains("Quarter 4")) {
                                selectionBet = SelectionBet.Q4;
                            } else {
                                selectionBet = SelectionBet.Match;
                            }

                            if (betDataString.contains(" odd")) {
                                oddEven = OddEven.Odd;
                            } else if (betDataString.contains(" even")) {
                                oddEven = OddEven.Even;
                            }
                        }

                        String matchName = lastBet.getMatchName();
                        String[] split = matchName.split(" vs ");
                        if (split.length < 2) {
                            continue;
                        }

                        if (betDataString.contains(split[0]) && betDataString.contains(split[1])) {
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
//        if (lastBet.getTimeBet() == -7200000L) {
//            clickToMatch(driver, lastBet.getMatchName());
//        } else {
//            driver.findElement(By.cssSelector("a[href='./live.html']")).click();
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        return betAlreadyNotPlayed;
    }

    @Override
    public boolean checkGameScore(WebDriver driver, MatchLiveBetting matchLive) {
        try {
            String currentPeriodString;
            String totalScore;
            currentPeriodString = driver.findElement(By.cssSelector("td.l")).findElement(By.cssSelector("span.l")).getText();

            if (currentPeriodString != null) {
                int a = currentPeriodString.indexOf("(");
                String correctCurrentPeriodString = null;
                if (a > 0) {
                    correctCurrentPeriodString = currentPeriodString.substring(a, currentPeriodString.length());
                }
                totalScore = correctCurrentPeriodString.replace(",", ", ");
                totalScore = totalScore.replace("-", ":");
            } else {
                return false;
            }

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
                            try {
                                int a = Integer.parseInt(substring.split(":")[0]);
                                int a1 = Integer.parseInt(substring.split(":")[1]);
                                matchScoreStatistic.put(SelectionBet.Q2, a + a1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            String substring = split[1].substring(1, split[1].length() - 1);
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
                if (score % 2 == 0) {
                    matchLive.getMatchOddEvenStatistic().put(selectionBet, OddEven.Even);//Even
                } else if (score % 2 == 1) {
                    matchLive.getMatchOddEvenStatistic().put(selectionBet, OddEven.Odd);//Odd
                }
            }
        } catch (Exception e) {
            System.out.println("First quarter");
        }
        return false;
    }

    @Override
    public Bet checkBet(Bet bet, WebDriver driver) {
        try {
            driver.findElement(By.cssSelector("a[href='./history.html']")).click();
            Thread.sleep(500);

            //To open 40 bet on page
            try {
                driver.findElement(By.cssSelector("div.select")).findElements(By.cssSelector("option")).get(1).click();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);

            List<WebElement> historyBets = driver.findElements(By.cssSelector("div.lr"));
            String nameOfMatch = null;
            OddEven oddEven = null;
            SelectionBet selectionBet = null;
            Boolean isWin = null;
            String isWinString = null;
            for (WebElement betElement : historyBets) {
                try {
                    Thread.sleep(500);
                    String betDataString = betElement.findElement(By.cssSelector("div.col-2")).getText();//name of teams -  Slovakia U-16 - Switzerland U-16
                    if (betDataString != null) {//Iran U-17 - Syria U-17. Quarter 1. Total : odd
                        String[] splitAllData = betDataString.split("\\. ");

                        String s;
                        String selectionBetString = null;
                        String oddEvenString = null;
                        for (int i = 0; i < splitAllData.length; i++) {
                            s = splitAllData[i];
                            if (s.contains(" - ")) {
                                String[] split = s.split(" - ");
                                nameOfMatch = split[0] + " vs " + split[1];
                                if (i < splitAllData.length - 1) {
                                    selectionBetString = splitAllData[i + 1];
                                }
                                oddEvenString = splitAllData[splitAllData.length - 1];
                                break;
                            }
                        }

                        if (selectionBetString != null) {
                            if (selectionBetString.contains("Quarter 1")) {
                                selectionBet = SelectionBet.Q1;
                            } else if (selectionBetString.contains("Quarter 2")) {
                                selectionBet = SelectionBet.Q2;
                            } else if (selectionBetString.contains("Quarter 3")) {
                                selectionBet = SelectionBet.Q3;
                            } else if (selectionBetString.contains("Quarter 4")) {
                                selectionBet = SelectionBet.Q4;
                            } else if (selectionBetString.contains("1st half")) {//1st Half Total Odd/Even
                                selectionBet = SelectionBet.Half1st;
                            } else if (selectionBetString.contains("2nd half")) {//2nd Half - Total Odd/Even
                                selectionBet = SelectionBet.Half2nd;
                            } else {//Game Total - Odd/Even,
                                selectionBet = SelectionBet.Match;
                            }
                        }

                        if (oddEvenString != null) {
                            if (oddEvenString.contains("odd")) {
                                oddEven = OddEven.Odd;
                            } else if (oddEvenString.contains("even")) {
                                oddEven = OddEven.Even;
                            }
                        }
                    }

                    try {
                        Thread.sleep(500);
                        isWinString = betElement.findElement(By.cssSelector("span.ss")).getText();//0,00 EUR LOOOSSSEEEE
                    } catch (Exception e) {
                        System.out.println("Нельзя делать ставки сейчас.");
                    }
                    if (isWinString != null) {
                        if (isWinString.contains("0.00")) {
                            isWin = false;
                        } else {
                            isWin = true;
                        }
                    }
                    if (bet.getMatchName().compareTo(nameOfMatch) == 0) {
                        if (bet.getSelectionBet() == selectionBet) {
                            if (bet.getOddEven() == oddEven) {
                                bet.setWin(isWin);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            driver.findElement(By.cssSelector("a[href='./live.html']")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bet;
    }

    @Override
    public Bet makeBet(Bet bet, WebDriver driver, MatchLiveBetting matchLive) {
        try {
            if (bet.getMatchName() == null) {
                bet.setMatchName(matchLive.getNameOfMatch());
            }

            boolean betAlreadyNoPlay;

            Boolean b1 = betAlreadyNotPlayed(bet, driver);

            goBackToOverview(driver);
            clickToMatch(driver, bet.getMatchName());
            if (b1 != null) {
                betAlreadyNoPlay = b1;
            } else {
                betAlreadyNoPlay = true;
            }
            if (betAlreadyNoPlay) {
                bet.setMatchPeriod(matchLive.getCurrentPeriodInt());
                String text = null;
                switch (bet.getSelectionBet()) {
                    case Q1:
                        text = "Quarter 1. Total:";
                        break;
                    case Q2:
                        text = "Quarter 2. Total:";
                        break;
                    case Q3:
                        text = "Quarter 3. Total:";
                        break;
                    case Q4:
                        text = "Quarter 4. Total:";
                        break;
                    case Match:
                        text = "Total:";
                        break;
                }

                WebDriver robots = null;
                try {
                    WebElement totalOddEven = driver.findElement(By.xpath("//i[text()='" + text + "']/.."));
                    WebElement betMakeElement;
                    if (bet.getOddEven() == OddEven.Even) {
//                        betMakeElement = totalOddEven.findElements(By.cssSelector("a")).get(0);
                        betMakeElement = totalOddEven.findElements(By.cssSelector("i")).get(1);
                    } else {
//                        betMakeElement = totalOddEven.findElements(By.cssSelector("a")).get(1);
                        betMakeElement = totalOddEven.findElements(By.cssSelector("i")).get(2);
                    }
                    System.out.println("Текст куда будем кликать, что бы поставить ставку - " + betMakeElement.getText());
//                    Actions actions = new Actions(driver);
//                    actions.moveToElement(betMakeElement).click().build().perform();
                    clickOnInvisibleElement(betMakeElement, driver);
                    driver.findElement(By.xpath("//button[text()='PLACE BET >']")).click();
                    Thread.sleep(1000);
                    robots = driver.switchTo().window(driver.getWindowHandles().toArray()[1].toString());
                    List<WebElement> elements = robots.findElement(By.id("wb")).findElements(By.cssSelector("td"));
                    Thread.sleep(1000);
                    WebElement input = elements.get(elements.size() - 2).findElement(By.cssSelector("input"));
                    input.click();
                    Thread.sleep(500);
                    input.sendKeys(String.valueOf(bet.getPrice()));
                    Thread.sleep(1000);
                    String valueOfBetString = input.getAttribute("value");
                    double priceOfBet = 0.0;
                    if (valueOfBetString != null) {
                        try {
                            priceOfBet = Double.parseDouble(valueOfBetString);
                        } catch (Exception e) {
                            System.out.println("ParserOldVersion ОШИБКА: 1369");
                        }
                        System.out.println("Программа попыталась поставить ставку ценой: " + priceOfBet);
                    }

                    if (priceOfBet == bet.getPrice()) {
                        try {
                            robots.findElement(By.cssSelector("button.btn_orange")).click();
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.info("Проблема во время клика при установке ставки");
                        }
                    }

                    Thread.sleep(3000);
                    try {
                        robots.findElement(By.xpath("//p[text()='Your bet has been accepted.']"));
                        betAlreadyNoPlay = false;
                    } catch (Exception e) {
                        System.out.println("Ставка не поставлена.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("Проблема при установке ставки - 1546. " + this.getNameOfSite());
                    System.out.println("Проблема при установке ставки - 1546. " + this.getNameOfSite());
                } finally {
                    if (robots != null) {
                        robots.close();
                    }
                    driver.switchTo().window(driver.getWindowHandles().toArray()[0].toString());
                }

                if (betAlreadyNoPlay) {
                    driver.findElement(By.cssSelector("a[href='./live.html']")).click();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Boolean b = betAlreadyNotPlayed(bet, driver);
                    if (b != null) {
                        betAlreadyNoPlay = b;
                    }
                }
            }

            if (!betAlreadyNoPlay) {
                log.info("Ставка сделана, время ставки " + new Date(System.currentTimeMillis()));
                System.out.println("Ставка сделана, время ставки " + new Date(System.currentTimeMillis()));
                bet.setTimeBet(System.currentTimeMillis());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bet;
    }

    @Override
    public boolean checkMaximumBetSizeAndPossibleBetSelection(WebDriver driver, MatchLiveBetting matchLive) {
        try {
            if (matchLive.getMaxBet() == 0.0) {
                try {
                    WebElement totalOddEven = driver.findElement(By.xpath("//i[text()='Total:']/.."));
                    totalOddEven.findElements(By.cssSelector("i")).get(1).click();
                    driver.findElement(By.xpath("//button[text()='PLACE BET >']")).click();
                    WebDriver robots = driver.switchTo().window(driver.getWindowHandles().toArray()[1].toString());//TODO java.lang.ArrayIndexOutOfBoundsException: 1
                    List<WebElement> elements = robots.findElement(By.id("wb")).findElements(By.cssSelector("td"));
                    Thread.sleep(1000);
                    String text = elements.get(elements.size() - 1).getText();
                    Double maxBet = null;

                    maxBet = Double.parseDouble(text);
                    robots.close();
                    driver.switchTo().window(driver.getWindowHandles().toArray()[0].toString());

                    if (maxBet != null) {
                        matchLive.setMaxBet(maxBet);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            matchLive.getPossibleBetSelectionBet().clear();
            List<WebElement> possibleBetSelectionsElements = driver.findElements(By.cssSelector("i.p2r"));
            for (WebElement webElement : possibleBetSelectionsElements) {
                String text = webElement.getText();
                if (text.contains("Quarter 1. Total:")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q1);
                } else if (text.contains("Quarter 2. Total:")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q2);
                } else if (text.contains("Quarter 3. Total:")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q3);
                } else if (text.contains("Quarter 4. Total:")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q4);
                } else if (text.contains("Total:")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Match);
                }
            }

            for (SelectionBet selectionBet : matchLive.getPossibleBetSelectionBet()) {
                System.out.println(selectionBet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean goBackToOverview(WebDriver driver) {
        boolean goBack = false;
        try {
            driver.findElement(By.cssSelector("a[href='./live.html']")).click();
            Thread.sleep(1000);
            goBack = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return goBack;
    }

    @Override
    public double getMinBet(WebDriver driver) {
        double minBet = 1.0;
        if (minBet > StartFrame.getMinBet()) {
            return minBet;
        } else {
            return StartFrame.getMinBet();
        }
    }

    @Override
    public String toString() {
        return "parimatch.com";
    }

    @Override
    public String getNameOfSite() {
        return "parimatch.com";
    }
}
