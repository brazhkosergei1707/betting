package parser.bet365;

import UI.startFrame.StartFrame;
import betting.entity.Bet;
import entityes.MatchLive;
import betting.enumeration.OddEven;
import betting.enumeration.SelectionBet;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import parser.Parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ParserBet365 implements Parser {


    private Logger log;
    private Appender appender;



    private int switcherId;
    private boolean isActiveHistory = false;
    private boolean isActiveDetails = false;
    private boolean haveEmergencyMatches = false;
    List<MatchLive> emergencyMatches = new ArrayList<>();
    int updateBalance = 5;
    SimpleDateFormat dateFormat = new SimpleDateFormat();
    Random random = new Random();


    @Override
    public Bet checkBet(Bet bet, WebDriver driver) {

        boolean openHistory = false;
        try {
            log.info("Проверяем последнюю ставку для матча " + bet.getMatchName() + ". Ставка была на " + bet.getSelectionBet());
            driver.findElement(By.cssSelector("div.hm-MembersInfoButton_AccountIcon")).click();
            if (!driver.findElement(By.cssSelector(".hm-MembersInfoContainer")).isDisplayed()) {
                System.out.println("finding account icon and click it...");
                driver.findElement(By.cssSelector("div.hm-MembersInfoButton_AccountIcon")).click();
            }
            log.info("Открываем историю сообщений");
            List<WebElement> links = driver.findElements(By.cssSelector("div.hm-MembersInfoGeneral_Link"));
            String history = "History";
            for (WebElement element : links) {
                if (history.compareTo(element.getText()) == 0) {
                    element.click();
                    openHistory = true;
                    break;
                }
            }
            Thread.sleep(500);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        if(openHistory){
            WebDriver viewport = null;
            WebDriver historyV3Iframe = null;
            try {
                viewport = driver.switchTo().window(driver.getWindowHandles().toArray()[1].toString());
                Thread.sleep(500);
                viewport.findElement(By.cssSelector("option[value='3']")).click();
                Thread.sleep(500);
                viewport.findElement(By.cssSelector("div.TAB_Hgo")).click();
                Thread.sleep(1000);
                historyV3Iframe = viewport.switchTo().frame("historyV3Iframe");

                boolean isBets;
                List<WebElement> betElements = null;
                try {
                    betElements = historyV3Iframe.findElements(By.cssSelector("tr[data-betstatus]"));
                    log.info("Найдено " + betElements.size() + " ставок");
                    isBets = true;
                } catch (NoSuchElementException e) {
                    log.error("Ставок нет.");
                    isBets = false;
                }

                if (isBets) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat();
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                    dateFormat.applyPattern("HH:mm:ss");
                    int countLink = 0;
                    for (int i = 0; i < betElements.size(); i++) {
                        try {
                            WebElement element = historyV3Iframe.findElements(By.cssSelector("tr[data-betstatus]")).get(i);
                            boolean isWin;
                            SelectionBet selectionBet = null;
                            String nameOfMatchBet = bet.getMatchName();
                            String checkingName = null;
                            if (nameOfMatchBet.contains(" @ ")) {
                                checkingName = nameOfMatchBet.split(" @ ")[0];
                            } else if (nameOfMatchBet.contains(" vs ")) {
                                checkingName = nameOfMatchBet.split(" vs ")[0];
                            }

                            String returnedValue = element.findElement(By.cssSelector("td.bet-summary-return")).getText();
                            if (returnedValue.contains("0.00")) {
                                isWin = false;
                            } else {
                                isWin = true;
                            }

                            String text = "text";
                            element.findElement(By.cssSelector("div.fn-sports-show-bet-confirmation")).click();

                            Thread.sleep(1000);

                            WebElement details = historyV3Iframe.findElements(By.cssSelector("tr.bet-summary-bet-confirmation-area")).get(countLink++);
                            text = details.findElement(By.cssSelector("td.bet-confirmation-table-body-event")).getText();
                            String[] split4 = text.split("\\n");
                            if (split4[1].contains("1st Quarter")) {
                                selectionBet = SelectionBet.Q1;
                            } else if (split4[1].contains("2nd Quarter")) {
                                selectionBet = SelectionBet.Q2;
                            } else if (split4[1].contains("3rd Quarter")) {
                                selectionBet = SelectionBet.Q3;
                            } else if (split4[1].contains("4th Quarter")) {
                                selectionBet = SelectionBet.Q4;
                            } else if (split4[1].contains("1st Half")) {
                                selectionBet = SelectionBet.Half1st;
                            } else if (split4[1].contains("2nd Half")) {
                                selectionBet = SelectionBet.Half2nd;
                            } else if (split4[1].contains("Game")) {
                                selectionBet = SelectionBet.Match;
                            }

                            if (text.contains(checkingName)) {
                                if (bet.getSelectionBet() == selectionBet) {
                                    log.info("Результат ставки: " + isWin);
                                    bet.setWin(isWin);
                                    i = betElements.size();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                Thread.sleep(500);
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            } finally {
                if (historyV3Iframe != null) {
                    historyV3Iframe.switchTo().defaultContent();
                }
                if (viewport != null) {
                    viewport.close();
                }
                driver.switchTo().window(driver.getWindowHandles().toArray()[0].toString());
            }
        }

        if (driver.findElement(By.cssSelector(".hm-MembersInfoContainer")).isDisplayed()) {
            System.out.println("finding account icon and click it...");
            driver.findElement(By.cssSelector("div.hm-MembersInfoButton_AccountIcon")).click();
        }

//        bet.setWin(random.nextBoolean());
        return bet;
    }

    @Override
    public Bet makeBet(Bet bet, WebDriver driver, MatchLive matchLive) {
        log.info("Ставим ставку на матч " + matchLive.getNameOfMatch() + ", выбор ставки " + bet.getSelectionBet() + ", четверть матча " + matchLive.getCurrentPeriodInt() + ", цена ставки " + bet.getPrice() + ", чет/нечет ставки " + bet.getOddEven());
        if (bet.getMatchName() == null) {
            bet.setMatchName(matchLive.getNameOfMatch());
            System.out.println("Chain did not set the match name.");
        }

        boolean betAlreadyNotDone = betAlreadyNotPlayed(bet, driver);
        if (betAlreadyNotDone) {
            log.info("Устанавливаем период матча для ставки.");
            bet.setMatchPeriod(matchLive.getCurrentPeriodInt());
            try {
                if (bet.getPrice() != null && bet.getPrice() > 0.0) {
                    List<WebElement> marketGroups = driver.findElements(By.cssSelector(".gl-MarketGroup"));
                    WebElement elementOddEven;
                    try {
                        elementOddEven = marketGroups.get(marketGroups.size() - 1);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        return bet;
                    }

                    List<WebElement> divs = elementOddEven.findElements(By.cssSelector("div"));
                    if (divs.size() < 3) {
                        elementOddEven.click();
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    String textBetPeriod = null;
                    switch (bet.getSelectionBet()) {
                        case Q1:
                            textBetPeriod = "1st Quarter";
                            break;
                        case Q2:
                            textBetPeriod = "2nd Quarter";
                            break;
                        case Q3:
                            textBetPeriod = "3rd Quarter";
                            break;
                        case Q4:
                            textBetPeriod = "4th Quarter";
                            break;
                        case Half1st:
                            textBetPeriod = "1st Half";
                            break;
                        case Half2nd:
                            textBetPeriod = "2nd Half";
                            break;
                        case Match:
                            textBetPeriod = "Match";
                    }
                    boolean click = false;
                    List<WebElement> columnsOfData = elementOddEven.findElements(By.cssSelector(".gl-Market_General"));
                    for (WebElement element1 : columnsOfData) {
                        try {
                            String textPeriod = element1.findElement(By.cssSelector(".gl-MarketColumnHeader")).getText();
                            if (textPeriod.length() < 3) {
                                System.out.println("this is first column...");
                            } else {
                                if (textBetPeriod.contains(textPeriod)) {
                                    List<WebElement> oddEven = element1.findElements(By.cssSelector(".gl-Participant_General"));
                                    if (bet.getOddEven() == OddEven.Odd) {
                                        oddEven.get(0).click();
                                    } else {
                                        oddEven.get(1).click();
                                    }
                                    click = true;
                                    Thread.sleep(1000);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            log.info(e.getMessage());
                            continue;
                        }
                    }

                    if (click) {
                        boolean clickToMadeBet = false;
                        try {
                            WebDriver betFrame = driver.switchTo().frame("bsFrame");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            WebElement priceBetField = betFrame.findElement(By.cssSelector("input.bs-Stake_TextBox"));
                            priceBetField.clear();
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            priceBetField.sendKeys(Double.toString(bet.getPrice()));
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            try{
                                WebElement element = betFrame.findElement(By.cssSelector("div.bs-BtnAccept"));

                                if(element.isDisplayed()){
                                    betFrame.findElement(By.cssSelector("a.bs-Header_RemoveAllLink")).click();
                                    return bet;
                                }

                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            betFrame.findElement(By.cssSelector(".bs-BtnHover")).click();
                            clickToMadeBet = true;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        } finally {
                            driver.switchTo().defaultContent();
                        }

                        if (clickToMadeBet) {

                            driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);

                            try {
                                driver.findElement(By.cssSelector("div.eba-AdvertWindow_NotNowButton ")).click();//TODO
                            } catch (Exception e) {
                                System.out.println("was no edit bet information on screen...");
                            }

                            try {
                                WebDriver betFrame = driver.switchTo().frame("bsFrame");
                                betFrame.findElement(By.cssSelector("a.bs-Header_RemoveAllLink")).click();
                            } catch (Exception e) {
                                System.out.println("Не было кнопки 'УБРАТЬ ВСЕ СТАВКИ'");
                            } finally {
                                driver.switchTo().defaultContent();
                            }
                            driver.manage().timeouts().implicitlyWait(7, TimeUnit.SECONDS);

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            try {
                                WebDriver betFrame = driver.switchTo().frame("bsFrame");
                                String betPlaced = betFrame.findElement(By.cssSelector("span.br-Header_TitleText")).getText();
                                if (betPlaced.compareTo("Bet Placed") == 0) {
                                    betFrame.findElement(By.cssSelector("span.br-Header_Done")).click();
                                    betAlreadyNotDone = false;
                                }
                            } catch (Exception e) {
                                log.error("Проблема при проверке - была ли поставлена ставка.");
                                log.error(e.getMessage());
                                e.printStackTrace();
                            } finally {
                                driver.switchTo().defaultContent();
                            }

                            if(betAlreadyNotDone){
                                Boolean b = betAlreadyNotPlayed(bet, driver);
                                if (b != null) {
                                    betAlreadyNotDone = b;
                                }
                            }
                        } else {
                            log.error("Не удалось поставить ставку.");
                        }
                    }
                } else {
                    log.error("Цена ставки для матча " + bet.getMatchName() + " равна нулю");
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            } finally {
                driver.switchTo().defaultContent();
            }
        }

        if (!betAlreadyNotDone) {
            Date date = null;
            try {
                String time = driver.findElement(By.cssSelector("time.hm-Clock")).getText();
                dateFormat.applyPattern("HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                try {
                    date = dateFormat.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                log.info("Ставка сделана, время ставки " + date);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            if (date == null) {
                bet.setTimeBet(0L);
            } else {
                bet.setTimeBet(date.getTime());
            }
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

    //    ==========================================================================
    //    ==========================================================================
    //    ==========================================================================
    @Override
    public boolean logIn(String username, String password, WebDriver driver) {
        log.info("Заходим на сайт и логируемся.");
        try {
            System.out.println("Opening main page https://www.bet365.com/ ...");
            driver.get("https://www.bet365.com/");
            System.out.println("waiting tree second before click to SPORT bets....");
            Thread.sleep(3000);
            try {
                System.out.println("finding SPORT bet link and click it....");
                WebElement element = driver.findElement(By.cssSelector("a[alt='Sports betting.Betting']"));
                element.click();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                driver.findElement(By.id("TopPromotionMainArea")).click();
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("waiting two second before Logging In...");
            Thread.sleep(2000);
            System.out.println("Finding Log In div element and field for edit username...");
            WebElement logInUserNameFieldDiv = (new WebDriverWait(driver, 10))
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.hm-Login_UserNameWrapper")));
            WebElement loginField = logInUserNameFieldDiv.findElement(By.tagName("input"));
            System.out.println("sending username to field....");
            loginField.sendKeys(username);
            System.out.println("waiting a second");
            Thread.sleep(1000);
            System.out.println("finding div to make visible password field and click it....");
            WebElement webElementPassword = driver.findElement(By.cssSelector("div.hm-Login_PasswordWrapper"));
            WebElement inputForMakeFielVisible = webElementPassword.findElement(By.tagName("input"));
            inputForMakeFielVisible.click();
            System.out.println("waiting a second");
            Thread.sleep(1000);
            System.out.println("finding password field div and edit password...");
            WebElement passwordField = webElementPassword.findElement(By.cssSelector("input[type='password']"));
            passwordField.sendKeys(password);
            System.out.println("finding LogIn button and click it...");
            WebElement buttonLogIn = driver.findElement(By.cssSelector("button.hm-Login_LoginBtn"));
            buttonLogIn.click();
            System.out.println("button was pressed and wait for five seconds...");
            Thread.sleep(5000);
            System.out.println("if was edit wrong username or password we should initiate method again with new data....");
            try {
                System.out.println("need to send message about wrong username or password...");
                WebElement failedLogIn = driver.findElement(By.cssSelector("input.wl-FailedLogin_Username"));
                WebElement failedPassword = driver.findElement(By.cssSelector("input.wl-FailedLogin_Password"));
                WebElement failedLodInButton = driver.findElement(By.cssSelector("button.wl-FailedLogin_LoginBtn"));
//                ================================
                if (failedLodInButton.isEnabled()) {
                    log.info("Неверный логин или пароль.");
                    System.out.println("wrong password");
                    return false;
                }
                System.out.println("in case we have this elements above, need to close driver and try again with new username and password...");
                Thread.sleep(3000);
                driver.quit();
                failedLogIn.sendKeys(username);
                failedPassword.sendKeys(password);
                failedLodInButton.click();
            } catch (NoSuchElementException e) {
                e.getLocalizedMessage();
                log.info("Логин и пароль корректны.");
                System.out.println("Username and password are correct....");
            }
            try {
                System.out.println("in case: need edit some info to you account should find button 'Remind me later' and click it......");
                WebDriver messageWindow = driver.switchTo().frame("messageWindow");
                WebElement remindMeButton = messageWindow.findElement(By.cssSelector("a#ctl00_main_kycController_ctl01_grp1CloseBtn"));
                log.info("Закрываем окно с напоминанием.");
                remindMeButton.click();
            } catch (Exception e) {
                log.info("Нет окна с напоминанием.");
                e.printStackTrace();
                System.out.println("Нет окна с напоминанием.");
            } finally {
                driver.switchTo().defaultContent();
            }
            Thread.sleep(500);
            try {
                driver.findElement(By.cssSelector("div.wl-PushTargetedMessageOverlay_CloseButton")).click();
                log.info("Закрываем окно с сообщением.");
            } catch (Exception e) {
                log.info("Нет новых сообщений.");
            }

            try{
                driver.switchTo().frame("ifunds");
                driver.findElement(By.id("ctl00_main_cDep_dvCloseBtn")).click();//ctl00_main_cDep_lkClose
                driver.switchTo().defaultContent();
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("Есть деньги на счету ");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean logOut(WebDriver driver) {
        if (driver != null) {
            try {
                try {
                    System.out.println("finding account icon and click it...");
                    WebElement userButton = driver.findElement(By.cssSelector("div.hm-MembersInfoButton_AccountIcon"));
                    userButton.click();
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                    System.out.println("User was not Logging In!!!");
                    return false;
                }
                Thread.sleep(1000);
                try {
                    System.out.println("finding LOGOUT Button and click it....");
                    WebElement logOut = driver.findElement(By.cssSelector("div.hm-MembersInfoGeneral_LoggedInLogOut"));
                    if (logOut.isDisplayed()) {
                        logOut.click();
                    } else {
                        driver.findElement(By.cssSelector("div.hm-MembersInfoButton_AccountIcon")).click();
                        System.out.println("waiting one second...");
                        Thread.sleep(1000);
                        logOut.click();
                    }

                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                    return false;
                }
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    //    ==========================================================================
    @Override
    public void setCurrentTimeAndCount(WebDriver driver) {
        try {
            String time = null;
            try {
                time = driver.findElement(By.cssSelector("time.hm-Clock")).getText();
            } catch (NoSuchElementException e) {
                time = "00:00:00";
            }
            dateFormat.applyPattern("HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
            Date date = null;
            log.info("Текущее время: " + time);
            try {
                date = dateFormat.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
//            StartFrame.getSwitcher(this, switcherId).getStorage().setCurrentTime(date.getTime());

            if (updateBalance == 5) {
                System.out.println("finding account icon and click it...");
                driver.findElement(By.cssSelector("div.hm-MembersInfoButton_AccountIcon")).click();
                Thread.sleep(1000);

                try {
                    driver.findElement(By.cssSelector("div.hm-BalanceDropDown_RefreshBalance")).click();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Thread.sleep(1000);
                if (driver.findElement(By.cssSelector(".hm-MembersInfoContainer")).isDisplayed()) {
                    System.out.println("finding account icon and click it...");
                    driver.findElement(By.cssSelector("div.hm-MembersInfoButton_AccountIcon")).click();
                }
                Thread.sleep(1000);
                String balance;
                try {
                    balance = driver.findElement(By.cssSelector("div.hm-Balance")).getText();
                } catch (NoSuchElementException e) {
                    balance = "Wait";
                }
                log.info("Текущее состояние счета: " + balance);
//                StartFrame.getSwitcher(this, switcherId).getStorage().setCurrentBalanceString(balance);

                try {
                    Double currentBalanceDouble = Double.parseDouble(balance.split(" ")[0]);
                    System.out.println("Current money count double is: " + currentBalanceDouble);
//                    StartFrame.getSwitcher(this, switcherId).getStorage().setCurrentBalance(currentBalanceDouble);
                } catch (Exception e) {
                    System.out.println("Problem during double parsing");
                }

                driver.findElement(By.cssSelector("a.hm-HeaderModule_Logo")).click();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                driver.findElement(By.cssSelector(".hm-BigButtons")).findElement(By.xpath("//a[text()='In-Play']")).click();
                updateBalance = 0;
            }
            updateBalance++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateHistoryBet(WebDriver driver) {

//        List<Bet> currentList = StartFrame.getSwitcher(this, switcherId).getStorage().getHistoryBets();
        List<Bet> list = new ArrayList<>();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        System.out.println("finding account icon and click it...");
        driver.findElement(By.cssSelector("div.hm-MembersInfoButton_AccountIcon")).click();

        if (!driver.findElement(By.cssSelector(".hm-MembersInfoContainer")).isDisplayed()) {
            System.out.println("finding account icon and click it...");
            driver.findElement(By.cssSelector("div.hm-MembersInfoButton_AccountIcon")).click();
        }
        System.out.println("finding HISTORY link and click it...");
        List<WebElement> links = driver.findElements(By.cssSelector("div.hm-MembersInfoGeneral_Link"));
        String history = "History";
        for (WebElement element : links) {
            if (history.compareTo(element.getText()) == 0) {
                element.click();
            }
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        WebDriver viewport = driver.switchTo().window(driver.getWindowHandles().toArray()[1].toString());
        WebDriver historyV3Iframe = null;
        try {
            System.out.println("select option: Settled Sports Bets.....");
            viewport.findElement(By.cssSelector("option[value='3']")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            viewport.findElement(By.cssSelector("div.TAB_Hgo")).click();
            System.out.println("waiting one second.... ");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("switch to frame....");
            historyV3Iframe = viewport.switchTo().frame("historyV3Iframe");
            System.out.println("finding all bet elements....");
            boolean isBets;
            List<WebElement> betElements = null;

//        showMore(historyV3Iframe);

            try {
                betElements = historyV3Iframe.findElements(By.cssSelector("tr[data-betstatus]"));
                System.out.println("finding current bet and open confirmation table...");
                System.out.println("For this period was find " + betElements.size() + " bets....");
                isBets = true;
            } catch (NoSuchElementException e) {
                isBets = false;
            }

            if (isBets) {
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                dateFormat.applyPattern("HH:mm:ss");
                int countLink = 0;

                for (int i = 0; i < betElements.size(); i++) {
                    try {
                        WebElement element = historyV3Iframe.findElements(By.cssSelector("tr[data-betstatus]")).get(i);
                        Bet bet;
                        long dateOfBet;
                        double priceOfBet = 0.0;
                        OddEven selection;
                        boolean isWin;
                        SelectionBet selectionBet = null;
                        String nameOfMatch = null;
                        String correctPeriodString;
                        int periodBetInt = 0;
                        String[] split1 = element.findElement(By.cssSelector("td.bet-summary-placement-date")).getText().split(" ");
                        String dateString = split1[1];
                        Date date = null;
                        try {
                            date = dateFormat.parse(dateString);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        dateOfBet = date.getTime();

                        String[] split2 = element.findElement(By.cssSelector("span.bet-summary-bet-confirmation-stake")).getText().split(" ");
                        String s2 = split2[0];
                        String priceString = s2.substring(1, s2.length() - 1);
                        try {
                            priceOfBet = Double.parseDouble(priceString);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String[] split3 = element.findElement(By.cssSelector("div.fn-sports-show-bet-confirmation")).getText().split(" ");
                        String s3 = split3[0];
                        if (s3.contains("Odd")) {
                            selection = OddEven.Odd;
                        } else {
                            selection = OddEven.Even;
                        }

                        String returnedValue = element.findElement(By.cssSelector("td.bet-summary-return")).getText();
                        if (returnedValue.contains("0.00")) {
                            isWin = false;
                        } else {
                            isWin = true;
                        }

                        element.findElement(By.cssSelector("div.fn-sports-show-bet-confirmation")).click();
                        System.out.println("waiting half second.... ");
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        WebElement details = historyV3Iframe.findElements(By.cssSelector("tr.bet-summary-bet-confirmation-area")).get(countLink++);
                        String text = details.findElement(By.cssSelector("td.bet-confirmation-table-body-event")).getText();
                        String[] split4 = text.split("\\n");
                        nameOfMatch = split4[0];

                        correctPeriodString = split4[1];

                        System.out.println("bet is for " + correctPeriodString);
                        if (correctPeriodString.contains("1st Quarter")) {//1st Quarter - Total Odd/Even
                            selectionBet = SelectionBet.Q1;
                            periodBetInt = 1;
                        } else if (correctPeriodString.contains("2nd Quarter")) {//
                            selectionBet = SelectionBet.Q2;
                            periodBetInt = 2;
                        } else if (correctPeriodString.contains("3rd Quarter")) {//3rd Quarter - Total Odd/Even
                            selectionBet = SelectionBet.Q3;
                            periodBetInt = 3;
                        } else if (correctPeriodString.contains("4th Quarter")) {
                            selectionBet = SelectionBet.Q4;
                            periodBetInt = 4;
                        } else if (correctPeriodString.contains("1st Half")) {//1st Half Total Odd/Even
                            selectionBet = SelectionBet.Half1st;
                            periodBetInt = 2;
                        } else if (correctPeriodString.contains("2nd Half")) {//2nd Half - Total Odd/Even
                            selectionBet = SelectionBet.Half2nd;
                            periodBetInt = 4;
                        } else if (correctPeriodString.contains("Game")) {//Game Total - Odd/Even,
                            selectionBet = SelectionBet.Match;
                            periodBetInt = 4;
                        }


                        boolean oldBet = false;
                        if (nameOfMatch != null) {
                            if (selection != null) {
                                if (selectionBet != null) {
//                                    for (Bet bet1 : currentList) {
//                                        if (bet1.getMatchName().contains(nameOfMatch.split("vs")[0])) {
//                                            if (bet1.getSelectionBet() == selectionBet) {
//                                                if (bet1.getOddEven() == selection) {
//                                                    oldBet = true;
//                                                }
//                                            }
//                                        }
//                                    }
                                }
                            }
                        }

                        if (!oldBet) {// new bet
                            bet = new Bet(nameOfMatch, selectionBet, priceOfBet, selection);
                            bet.setWin(isWin);
                            bet.setTimeBet(dateOfBet);
                            bet.setMatchPeriod(periodBetInt);
//                            currentList.add(bet);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.info(e.getMessage());
                    }
                }
            }

//            log.info("Найдено " + list.size() + " новых ставок в архиве.");
//            if (list.size() > 0) {
//                StartFrame.getSwitcher(this).getStorage().addBetToStorage(list);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (historyV3Iframe != null) {
                historyV3Iframe.switchTo().defaultContent();

            }
            viewport.close();
        }

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

        if (driver.findElement(By.cssSelector(".hm-MembersInfoContainer")).isDisplayed()) {
            System.out.println("finding account icon and click it...");
            driver.findElement(By.cssSelector("div.hm-MembersInfoButton_AccountIcon")).click();
        }
    }

    @Override
    public void updateCurrentMatchesLive(WebDriver driver) {

        boolean isMatchesNow = false;
        try {
            driver.findElement(By.cssSelector(".hm-BigButtons")).findElement(By.xpath("//a[text()='In-Play']")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                driver.findElements(By.cssSelector(".ip-ControlBar_BBarItem")).get(0).click();
            } catch (IndexOutOfBoundsException e) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<WebElement> elements = driver.findElements(By.cssSelector("div.ipo-ClassificationBarButtonBase_Label"));//Basketball
            for (WebElement element : elements) {
                String nameOfLink = element.getText();
                if (nameOfLink.compareTo("Basketball") == 0) {
                    element.click();
                    isMatchesNow = true;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

            Map<Integer, MatchLive> currentMatches = null;
//            Map<Integer, MatchLive> currentMatches = StartFrame.getSwitcher(this, switcherId).getStorage().getCurrentMatches();

            //if find element with basketball link
            if (isMatchesNow) {
                List<WebElement> matchesElements = driver.findElements(By.cssSelector("div.ipo-Fixture_TableRow"));
                int i = matchesElements.size();
                System.out.println("Now play " + i + " matches...");
                log.info("Сейчас проходит " + i + " матчей");
                for (int j = 0; j < i; j++) {
                    WebElement element;
                    try {
                        element = driver.findElements(By.cssSelector("div.ipo-Fixture_TableRow")).get(j);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        continue;
                    }

                    try {
                        List<WebElement> namesOfTeams = element.findElements(By.cssSelector("span.ipo-TeamStack_TeamWrapper"));
                        String team1 = namesOfTeams.get(0).getText();
                        String team2 = namesOfTeams.get(1).getText();
                        String nameOfMatch = team1 + " vs " + team2;

                        if (team1 == null || team2 == null) {
                            continue;
                        }

                        String currentPeriodString;
                        try {
                            currentPeriodString = element.findElement(By.cssSelector("div.ipo-PeriodInfo")).getText();
                        } catch (NoSuchElementException e) {
                            System.out.println("Match have not period...");
                            continue;
                        }

                        SelectionBet currentPeriod;

                        if (currentPeriodString == null) {
                            currentPeriod = SelectionBet.Match;
                        } else {
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
                                default:
                                    currentPeriod = SelectionBet.valueOf(currentPeriodString);
                                    break;
                            }
                        }

                        boolean isPossibleToMakeBet = false;

                        try {
                            isPossibleToMakeBet = element.findElements(By.cssSelector("div.ipo-MainMarketRenderer")).get(1).findElement(By.cssSelector(".gl-ParticipantCentered_Odds")).isDisplayed();
                        } catch (NoSuchElementException e) {
                            System.out.println("Can not make bet yet for this match...");
                        }

                        String timeToEndPeriod = element.findElement(By.cssSelector("div.ipo-InPlayTimer")).getText();
                        Date data = null;
                        Long time;
                        dateFormat.applyPattern("mm:ss");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                        try {
                            data = dateFormat.parse(timeToEndPeriod);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            System.out.println("Can not resolve time to quarter finish....");
                        }

                        if (data == null) {
                            time = -7020000L;
                        } else {
                            time = data.getTime();
                        }

                        MatchLive matchLive;
                        Double maxBet = null;

                        //if match already in map, update it
                        if (currentMatches.containsKey(nameOfMatch.hashCode())) {
                            log.info("Обновляем матч " + nameOfMatch + ", время ко конца периода: " + timeToEndPeriod + ", текущий период: " + currentPeriod);
                            matchLive = currentMatches.get(nameOfMatch.hashCode());
                            matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
                            matchLive.setTimeToQuarterFinish(time);
                            matchLive.setCurrentPeriod(currentPeriod);
                            if (isPossibleToMakeBet) {
                                if (matchLive.getMaxBet() == 0.0) {
                                    maxBet = getMaxBet(driver, nameOfMatch);
                                    if (maxBet != null) {
                                        matchLive.setMaxBet(maxBet);
                                    }
                                }
                            }
                        } else {
                            if (isPossibleToMakeBet) {
                                maxBet = getMaxBet(driver, nameOfMatch);
                            }
                            log.info("Добавляем новый матч " + nameOfMatch + ", время ко конца периода: " + timeToEndPeriod + ", текущий период: " + currentPeriod);
//                            matchLive = StartFrame.getSwitcher(this, switcherId).getStorage().addMatchLive(nameOfMatch, isPossibleToMakeBet, time, currentPeriod, maxBet);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        driver.switchTo().defaultContent();
                    }
                }
            } else {
                System.out.println("Сейчас нет матчей на сайте " + getNameOfSite());
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            log.info("На данный момент нет матчей 'IN-PLAY'.");
            System.out.println("For this time is not matches 'IN-PLAY'...");
        }
    }

    @Override
    public void updateCurrentBet(WebDriver driver) {
        List<Bet> list = null;
//        List<Bet> list = StartFrame.getSwitcher(this, switcherId).getStorage().getCurrentBets();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        System.out.println("finding account icon and click it...");
        driver.findElement(By.cssSelector("div.hm-MembersInfoButton_AccountIcon")).click();

        if (!driver.findElement(By.cssSelector(".hm-MembersInfoContainer")).isDisplayed()) {
            System.out.println("finding account icon and click it...");
            driver.findElement(By.cssSelector("div.hm-MembersInfoButton_AccountIcon")).click();
        }
        System.out.println("finding HISTORY link and click it...");
        List<WebElement> links = driver.findElements(By.cssSelector("div.hm-MembersInfoGeneral_Link"));
        String history = "History";
        for (WebElement element : links) {
            if (history.compareTo(element.getText()) == 0) {
                element.click();
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        WebDriver viewport = driver.switchTo().window(driver.getWindowHandles().toArray()[1].toString());
        viewport.findElement(By.cssSelector("div.TAB_Hgo")).click();
        System.out.println("waiting one second.... ");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("switch to frame....");
        WebDriver historyV3Iframe = viewport.switchTo().frame("historyV3Iframe");
        System.out.println("finding all bet elements....");
        boolean isBets;
        List<WebElement> betElements = null;
        try {
            betElements = historyV3Iframe.findElements(By.cssSelector("tr[data-betstatus]"));
            System.out.println("finding current bet and open confirmation table...");
            System.out.println("For this period was find " + betElements.size() + " bets....");
            isBets = true;
        } catch (NoSuchElementException e) {
            isBets = false;
        }

        try {
            if (isBets) {
                boolean newBet = true;
                SimpleDateFormat dateFormat = new SimpleDateFormat();
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                dateFormat.applyPattern("HH:mm:ss");
                int countLink = 0;

                for (int i = 0; i < betElements.size(); i++) {
                    WebElement element;
                    try {
                        element = historyV3Iframe.findElements(By.cssSelector("tr[data-betstatus]")).get(i);
                        Bet bet;
                        long dateOfBet;
                        double priceOfBet = 0.0;
                        OddEven selection;
                        SelectionBet selectionBet = null;
                        String nameOfMatch = null;
                        String[] split1 = element.findElement(By.cssSelector("td.bet-summary-placement-date")).getText().split(" ");
                        String dateString = split1[1];
                        Date date = null;
                        try {
                            date = dateFormat.parse(dateString);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        dateOfBet = date.getTime();
                        String[] split2 = element.findElement(By.cssSelector("span.bet-summary-bet-confirmation-stake")).getText().split(" ");
                        String s2 = split2[0];
                        String priceString = s2.substring(1, s2.length() - 1);
                        try {
                            priceOfBet = Double.parseDouble(priceString);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String[] split3 = element.findElement(By.cssSelector("div.fn-sports-show-bet-confirmation")).getText().split(" ");
                        String s3 = split3[0];
                        if (s3.contains("Odd")) {
                            selection = OddEven.Odd;
                        } else {
                            selection = OddEven.Even;
                        }

                        element.findElement(By.cssSelector("div.fn-sports-show-bet-confirmation")).click();
                        System.out.println("waiting a second.... ");
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        WebElement details = historyV3Iframe.findElements(By.cssSelector("tr.bet-summary-bet-confirmation-area")).get(countLink++);
                        String text = details.findElement(By.cssSelector("td.bet-confirmation-table-body-event")).getText();
                        String[] split4 = text.split("\\n");
                        nameOfMatch = split4[0];

                        String correctPeriodStringFirst = split4[1];
                        System.out.println("bet is for " + correctPeriodStringFirst);
                        if (correctPeriodStringFirst.contains("1st Quarter")) {//1st Quarter - Total Odd/Even
                            selectionBet = SelectionBet.Q1;
                        } else if (correctPeriodStringFirst.contains("2nd Quarter")) {//
                            selectionBet = SelectionBet.Q2;
                        } else if (correctPeriodStringFirst.contains("3rd Quarter")) {//3rd Quarter - Total Odd/Even
                            selectionBet = SelectionBet.Q3;
                        } else if (correctPeriodStringFirst.contains("4th Quarter")) {
                            selectionBet = SelectionBet.Q4;
                        } else if (correctPeriodStringFirst.contains("1st Half")) {//1st Half Total Odd/Even
                            selectionBet = SelectionBet.Half1st;
                        } else if (correctPeriodStringFirst.contains("2nd Half")) {//2nd Half - Total Odd/Even
                            selectionBet = SelectionBet.Half2nd;
                        } else if (correctPeriodStringFirst.contains("Game")) {//Game Total - Odd/Even,
                            selectionBet = SelectionBet.Match;
                        }

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
                            bet = new Bet(nameOfMatch, selectionBet, priceOfBet, selection);
                            bet.setTimeBet(dateOfBet);
                            bet.setPlay(true);
                            System.out.println("new bet was added....");
                            list.add(bet);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.info(e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.info("Problem during current bet updating....");
            System.out.println("Problem during current bet updating....");
        } finally {
            historyV3Iframe.switchTo().defaultContent();
            viewport.close();
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
            if (driver.findElement(By.cssSelector(".hm-MembersInfoContainer")).isDisplayed()) {
                System.out.println("finding account icon and click it...");
                driver.findElement(By.cssSelector("div.hm-MembersInfoButton_AccountIcon")).click();
            }
        }

        System.out.println("Checking one more time with 'Mybets' link ...");
        driver.findElement(By.xpath("//div[text()='My Bets']")).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.findElement(By.xpath("//div[text()='Live']")).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        showMoreBetsToCheck(driver);

        WebElement betItemsContainer = driver.findElement(By.cssSelector("div.mbr-OpenBetItemsContainerRhs_BetItemsContainer"));
        List<WebElement> betItemElements = betItemsContainer.findElements(By.cssSelector("div.mbr-OpenBetItemRhs "));
        System.out.println("Find " + betItemElements.size() + " bets....");
        boolean newBet = true;
        Bet bet;
        String nameOfMatch = "NULL";
        double priceOfBet = 0.0;
        OddEven selection = OddEven.Odd;
        SelectionBet selectionBet = SelectionBet.Q1;
        int periodBetInt = 0;

        for (WebElement element : betItemElements) {
            try {
                nameOfMatch = element.findElement(By.cssSelector("div.mbr-OpenBetParticipantRhs_FixtureDescriptionText")).getText();
                try {
                    priceOfBet = Double.valueOf(element.findElement(By.cssSelector("div.mbr-OpenBetItemRhsDetails_StakeText")).getText());
                } catch (NumberFormatException e) {
                    priceOfBet = 0.0;
                }
                String correctPeriodString = element.findElement(By.cssSelector("div.mbr-OpenBetParticipantRhs_MarketText")).getText();

                System.out.println("bet is for " + correctPeriodString);
                if (correctPeriodString.contains("1st Quarter")) {//1st Quarter - Total Odd/Even
                    selectionBet = SelectionBet.Q1;
                    periodBetInt = 1;
                } else if (correctPeriodString.contains("2nd Quarter")) {//
                    selectionBet = SelectionBet.Q2;
                    periodBetInt = 2;
                } else if (correctPeriodString.contains("3rd Quarter")) {//3rd Quarter - Total Odd/Even
                    selectionBet = SelectionBet.Q3;
                    periodBetInt = 3;
                } else if (correctPeriodString.contains("4th Quarter")) {
                    selectionBet = SelectionBet.Q4;
                    periodBetInt = 4;
                } else if (correctPeriodString.contains("1st Half")) {//1st Half Total Odd/Even
                    selectionBet = SelectionBet.Half1st;
                    periodBetInt = 2;
                } else if (correctPeriodString.contains("2nd Half")) {//2nd Half - Total Odd/Even
                    selectionBet = SelectionBet.Half2nd;
                    periodBetInt = 4;
                } else if (correctPeriodString.contains("Game")) {//Game Total - Odd/Even,
                    selectionBet = SelectionBet.Match;
                    periodBetInt = 4;
                }

                String selectionString = element.findElement(By.cssSelector("div.mbr-OpenBetParticipantRhs_HeaderText")).getText();
                System.out.println("Selection is " + selectionString);
                if (selectionString.contains("Odd")) {
                    selection = OddEven.Odd;
                } else {
                    selection = OddEven.Even;
                }

                for (Bet betList : list) {
                    if (nameOfMatch.contains(betList.getMatchName().split(" vs ")[0])) {
                        if (selectionBet == betList.getSelectionBet()) {
                            System.out.println("Bet for match: " + nameOfMatch + " was find...");
                            betList.setPlay(true);

                            System.out.println("This is old bet");
                            if (betList.getPrice() == 0.0 || betList.getPrice() == null) {
                                betList.setPrice(priceOfBet);
                            }
                            newBet = false;
                        }
                    }
                }

                if (newBet) {
                    System.out.println("This is new bet in storage...");
                    bet = new Bet(nameOfMatch, selectionBet, priceOfBet, selection);
                    bet.setTimeBet(bet.getTimeBet() + 1000);
                    bet.setMatchPeriod(periodBetInt);
                    bet.setPlay(true);
                    System.out.println("new bet was added....");
                    list.add(bet);
                }

            } catch (Exception e) {
                e.printStackTrace();
                log.info(e.getMessage());
            }
        }
//        StartFrame.getSwitcher(this).getStorage().checkBatsInStorage();
    }

    @Override
    public String getNameOfSite() {
        return "www.bet365.com- " + switcherId;
    }

    @Override
    public String toString() {
        return "www.bet365.com";
    }

    void showMore(WebDriver historyV3Iframe) {
        try {
            historyV3Iframe.findElement(By.id("show-more-button")).click();
            ///should be more then 30 seconds
            Thread.sleep(1000);
            showMore(historyV3Iframe);
        } catch (Exception e) {
            return;
        }
    }

    public void scanMatches(WebDriver driver) {
        log.info("Начало сканирования");
        boolean isMatchesNow = false;
        try {
            driver.switchTo().defaultContent();
            try {
                driver.findElement(By.cssSelector("div.wl-PushTargetedMessageOverlay_CloseButton")).click();
                log.info("Закрываем окно с сообщением.");
                System.out.println("Have new message.");
            } catch (Exception e) {
                log.info("Нет новых сообщений.");
                System.out.println("No new messages.");
            }

            try{
                driver.switchTo().frame("ifunds");
                driver.findElement(By.id("ctl00_main_cDep_dvCloseBtn")).click();//ctl00_main_cDep_lkClose
                driver.switchTo().defaultContent();
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("Есть деньги на счету ");
            }

            try {
                driver.findElement(By.cssSelector(".hm-BigButtons")).findElement(By.xpath("//a[text()='In-Play']")).click();
                Thread.sleep(500);
                driver.findElement(By.cssSelector(".hm-BigButtons")).findElement(By.xpath("//a[text()='In-Play']")).click();
            } catch (Exception e) {
                driver.findElement(By.cssSelector("a.hm-HeaderModule_Logo")).click();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                driver.findElement(By.cssSelector(".hm-BigButtons")).findElement(By.xpath("//a[text()='In-Play']")).click();
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                driver.findElements(By.cssSelector(".ip-ControlBar_BBarItem")).get(0).click();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                driver.findElements(By.cssSelector(".ip-ControlBar_BBarItem")).get(0).click();
            } catch (NoSuchElementException e) {
                e.printStackTrace();
                return;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<WebElement> elements = driver.findElements(By.cssSelector("div.ipo-ClassificationBarButtonBase_Label"));//Basketball

            for (WebElement element : elements) {
                String nameOfLink = element.getText();
                try {
                    if (nameOfLink.compareTo("Basketball") == 0) {
                        element.click();
                        isMatchesNow = true;
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info(e.getMessage());
                }
            }

            Map<Integer, MatchLive> currentMatches = null;
//            Map<Integer, MatchLive> currentMatches = StartFrame.getSwitcher(this, switcherId).getStorage().getCurrentMatches();
            //looking for match which is finished already
            for (Integer integer : currentMatches.keySet()) {
                MatchLive matchLive = currentMatches.get(integer);
                String s = matchLive.getNameOfMatch().split(" vs ")[0];
                try {
                    (new WebDriverWait(driver, 2)).
                            until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='" + s + "']")));
                    matchLive.setAlmostFinish(false);
                } catch (TimeoutException e) {
                    if (matchLive.getTimeToQuarterFinish() < -7020000L) {
                        if (matchLive.isAlmostFinish()) {
                            matchLive.setFinished(true);
                            log.info("Матч " + matchLive.getNameOfMatch() + " уже окончен.");
                            System.out.println("the match with name " + matchLive.getNameOfMatch() + " already finished....");
                        } else {
                            System.out.println("Возможно матч " + matchLive.getNameOfMatch() + " закончился, первая проверка.");
                            log.info("Возможно матч " + matchLive.getNameOfMatch() + " закончился, первая проверка.");
                            matchLive.setAlmostFinish(true);
                        }
                    }
                }
            }

            if (isMatchesNow) {
                List<WebElement> matchesElements = null;
                try {
                    matchesElements = driver.findElements(By.cssSelector("div.ipo-Fixture_TableRow"));
                    log.info("Найдено " + matchesElements.size() + " матчей.");
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info(e.getMessage());
                    log.info("Нет матчей сейчас.");
                }
                int i = 0;
                if (matchesElements != null) {
                    i = matchesElements.size();
                }

                for (int j = 0; j < i; j++) {
//                    if (!StartFrame.getSwitcher(this, switcherId).isAppWork()) {
//                        return;
//                    }

                    WebElement element;
                    try {
                        element = driver.findElements(By.cssSelector("div.ipo-Fixture_TableRow")).get(j);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
                        List<WebElement> namesOfTeams = element.findElements(By.cssSelector("span.ipo-TeamStack_TeamWrapper"));
                        String team1 = namesOfTeams.get(0).getText();
                        String team2 = namesOfTeams.get(1).getText();
                        String nameOfMatch = team1 + " vs " + team2;

                        String timeToEndPeriod = element.findElement(By.cssSelector("div.ipo-InPlayTimer")).getText();
                        Date data = null;
                        Long time;
                        dateFormat.applyPattern("mm:ss");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                        try {
                            data = dateFormat.parse(timeToEndPeriod);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (data == null) {
                            time = -7020000L;
                        } else {
                            time = data.getTime();
                        }

                        Thread.sleep(1000);
                        String currentPeriodString;
                        try {
                            currentPeriodString = element.findElement(By.cssSelector("div.ipo-PeriodInfo")).getText();
                        } catch (NoSuchElementException e) {
                            continue;
                        }

                        SelectionBet currentPeriod;
                        if (currentPeriodString == null) {
                            currentPeriod = SelectionBet.Match;
                        } else {
                            switch (currentPeriodString) {
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
                                default:
                                    currentPeriod = SelectionBet.valueOf(currentPeriodString);
                                    break;
                            }
                        }

                        boolean isPossibleToMakeBet = false;
                        try {
                            isPossibleToMakeBet = element.findElements(By.cssSelector("div.ipo-MainMarketRenderer")).get(1).findElement(By.cssSelector(".gl-ParticipantCentered_Odds")).isDisplayed();
                        } catch (NoSuchElementException e) {
                            System.out.println("Can not make bet yet for this match...");
                        }

                        MatchLive matchLive;
                        Double maxBet = null;

                        if (currentMatches.containsKey(nameOfMatch.hashCode())) {
                            log.info("Обновляем матч " + nameOfMatch + ", время ко конца периода: " + timeToEndPeriod + ", текущий период: " + currentPeriod);
                            matchLive = currentMatches.get(nameOfMatch.hashCode());
                            matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
                            matchLive.setTimeToQuarterFinish(time);
                            matchLive.setCurrentPeriod(currentPeriod);
                            if (isPossibleToMakeBet) {
                                if (matchLive.getMaxBet() == 0.0) {
                                    maxBet = getMaxBet(driver, nameOfMatch);
                                    if (maxBet != null) {
                                        matchLive.setMaxBet(maxBet);
                                    }
                                }
                            }
                        } else {
                            if (isPossibleToMakeBet) {
                                maxBet = getMaxBet(driver, nameOfMatch);
                            }
                            log.info("Добавляем новый матч " + nameOfMatch + ", время ко конца периода: " + timeToEndPeriod + ", текущий период: " + currentPeriod);
//                            matchLive = StartFrame.getSwitcher(this, switcherId).getStorage().addMatchLive(nameOfMatch, isPossibleToMakeBet, time, currentPeriod, maxBet);
                        }

//                        if (isPossibleToMakeBet && !matchLive.isBetIsDone()) {
//                            matchLive.makeNextEvent(driver);
//                        }
//                        if(haveEmergencyMatches){
//                            for(MatchLive matchLive1:emergencyMatches){
//                                if(!matchLive1.isBetIsDone()){//проверить период для этого матча.
//                                    matchLive1.makeNextEvent(driver);
//                                }
//                            }
//                            if(emergencyMatches.size()==0){
//                                haveEmergencyMatches = false;
//                            }
//                        }


//                        } else {
//                            if (matchLive.isMatchInWork() && !matchLive.isBetIsDone()) {
//                                matchLive.makeNextEvent(driver);
//                            }
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        driver.switchTo().defaultContent();
                        continue;
                    }
                }
            } else {
                log.info("На данные момент больше нет матчей.");
            }

            for (Integer integer : currentMatches.keySet()) {
                MatchLive matchLive = currentMatches.get(integer);
                if (matchLive.isFinished()) {
                    matchLive.makeNextEvent(driver);
                }
            }

        } catch (NoSuchElementException e) {
            e.printStackTrace();
            log.info("На данные момент больше нет матчей.");
            System.out.println("For this time is not matches 'IN-PLAY'...");
        } finally {
            driver.switchTo().defaultContent();
        }
    }

    public Double getMaxBet(WebDriver driver, String matchName) {

        Double maxBet = null;
        try {
            clickToMatch(driver, matchName);

            log.info("Проверяем максимальную ставку.");
            System.out.println("Проверяем максимальную ставку.");
            List<WebElement> marketGroups = driver.findElements(By.cssSelector("div.gl-MarketGroup"));
            WebElement elementOddEven = null;
            String oddEvenString = null;
            try {
                elementOddEven = marketGroups.get(marketGroups.size() - 1);
                oddEvenString = elementOddEven.findElement(By.cssSelector("span.gl-MarketGroupButton_Text")).getText();
            } catch (Exception e) {
                System.out.println("String must be Odd/Enen : " + oddEvenString);
            }

            if (oddEvenString.contains("Odd/Even")) {
                List<WebElement> divs = elementOddEven.findElements(By.cssSelector("div"));
                System.out.println("The size of div is: " + divs.size() + " ...");
//            String spanText = divs.get(0).findElement(By.cssSelector(".gl-MarketGroupButton_Text")).getText();
                System.out.println("compare names of groups to find Odd/Even block....");
                System.out.println("Open field if it is necessary....");
                if (divs.size() < 3) {
                    elementOddEven.click();
                }

                List<WebElement> columnsOfData = elementOddEven.findElements(By.cssSelector(".gl-Market_General"));

                String text = "Match";
                boolean click = false;
                System.out.println("textBetPeriod is: " + text);
                for (WebElement element1 : columnsOfData) {
                    String textPeriod = element1.findElement(By.cssSelector(".gl-MarketColumnHeader")).getText();
                    System.out.println("textPeriod is: " + textPeriod);
                    if (textPeriod.length() < 3) {
                        System.out.println("this is first column...");
                    } else {
                        System.out.println("The element header is:" + textPeriod + "....");
                        if (text.contains(textPeriod)) {
                            List<WebElement> oddEven = element1.findElements(By.cssSelector(".gl-Participant_General"));
                            oddEven.get(0).click();
                            click = true;
                            break;
                        }
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (click) {
                    try {
                        WebDriver bsFrame = driver.switchTo().frame("bsFrame");
                        bsFrame.findElement(By.cssSelector("span.bs-BetMax_Text")).click();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        String maxBetString = bsFrame.findElement(By.cssSelector("span.bs-TotalStake")).getText();

                        try {
                            maxBet = Double.parseDouble(maxBetString);
                        } catch (NumberFormatException e) {
                            System.out.println("Wrong max bet String....");
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bsFrame.findElement(By.cssSelector("a.bs-Header_RemoveAllLink")).click();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        driver.switchTo().defaultContent();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            goBackToOverview(driver);
        }
        return maxBet;
    }

    @Override
    public boolean clickToMatch(WebDriver driver, String nameOfMatch) {
        boolean insideMatch = false;
        List<WebElement> elements = driver.findElements(By.cssSelector("div.ipo-Fixture_TableRow"));
        String text1;
        String text2;
        for (WebElement element : elements) {
            text1 = element.findElements(By.cssSelector("span.ipo-TeamStack_TeamWrapper")).get(0).getText();
            if (nameOfMatch.contains(text1)) {
                text2 = element.findElements(By.cssSelector("span.ipo-TeamStack_TeamWrapper")).get(1).getText();
                if (nameOfMatch.contains(text2)) {
                    element.findElement(By.cssSelector("span.ipo-TeamStack_TeamWrapper")).click();
                    insideMatch = true;
                    break;
                }
            }
        }
        return insideMatch;
    }

    //    ====================================================================
    @Override
    public Boolean betAlreadyNotPlayed(Bet lastBet, WebDriver driver) {
        Boolean betAlreadyNotPlayed = true;
        try {
            System.out.println("Checking one more time with 'Mybets' link ...");
            driver.findElement(By.xpath("//div[text()='My Bets']")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.findElement(By.xpath("//div[text()='My Bets']")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.findElement(By.xpath("//div[text()='Live']")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WebElement betItemsContainer = driver.findElement(By.cssSelector("div.mbr-OpenBetItemsContainerRhs_BetItemsContainer"));

            List<WebElement> betItemElements = betItemsContainer.findElements(By.cssSelector("div.mbr-OpenBetItemRhs "));
            System.out.println("Find " + betItemElements.size() + " bets....");

            String nameOfMatch;
            SelectionBet selectionBet = SelectionBet.Q1;

            for (WebElement element : betItemElements) {
                try{
                    nameOfMatch = element.findElement(By.cssSelector("div.mbr-OpenBetParticipantRhs_FixtureDescriptionText")).getText();
                    String correctPeriodString = element.findElement(By.cssSelector("div.mbr-OpenBetParticipantRhs_MarketText")).getText();
                    System.out.println("bet is for " + correctPeriodString);
                    if (correctPeriodString.contains("1st Quarter")) {//1st Quarter - Total Odd/Even
                        selectionBet = SelectionBet.Q1;
                    } else if (correctPeriodString.contains("2nd Quarter")) {//
                        selectionBet = SelectionBet.Q2;
                    } else if (correctPeriodString.contains("3rd Quarter")) {//3rd Quarter - Total Odd/Even
                        selectionBet = SelectionBet.Q3;
                    } else if (correctPeriodString.contains("4th Quarter")) {
                        selectionBet = SelectionBet.Q4;
                    } else if (correctPeriodString.contains("1st Half")) {//1st Half Total Odd/Even
                        selectionBet = SelectionBet.Half1st;
                    } else if (correctPeriodString.contains("2nd Half")) {//2nd Half - Total Odd/Even
                        selectionBet = SelectionBet.Half2nd;
                    } else if (correctPeriodString.contains("Game")) {//Game Total - Odd/Even,
                        selectionBet = SelectionBet.Match;
                    }

                    String matchNameFromBet = lastBet.getMatchName();
                    String[] split = matchNameFromBet.split(" vs ");

                    if(nameOfMatch.contains(split[0])&&nameOfMatch.contains(split[1])){
                        if (Bet.compareBetSelection(lastBet.getSelectionBet(), selectionBet)) {
                            lastBet.zeroCountCheck();
                            betAlreadyNotPlayed = false;
                            break;
                        }
                    }
                }catch (Exception e){
                    System.out.println("NEXT BET");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            betAlreadyNotPlayed = null;
        }
        return betAlreadyNotPlayed;
    }

    @Override
    public void checkGameScore(WebDriver driver, MatchLive matchLive) {
        log.info("Проверяем счет матча " + matchLive.getNameOfMatch() + ". В периоде " + matchLive.getCurrentPeriod());
        WebElement scoreGridContainer = driver.findElement(By.cssSelector("div.ipe-ScoreGridContainer"));
        List<WebElement> scoreColumns = scoreGridContainer.findElements(By.cssSelector("div.ipe-ScoreGridColumn"));
        List<WebElement> scores = new ArrayList<>();
        WebElement firstQuarter = scoreColumns.get(3);//
        scores.add(firstQuarter);
        WebElement secondQuarter = scoreColumns.get(4);//
        scores.add(secondQuarter);
        WebElement firstHalf = scoreColumns.get(5);//
        scores.add(firstHalf);
        WebElement thirdQuarter = scoreColumns.get(6);//
        scores.add(thirdQuarter);
        WebElement foursQuarter = scoreColumns.get(7);//
        scores.add(foursQuarter);
        WebElement total = scoreColumns.get(9);//
        scores.add(total);

        for (WebElement element : scores) {
            List<WebElement> scoreDetails = element.findElements(By.cssSelector("div.ipe-ScoreGridCell"));
            String scorePeriod = scoreDetails.get(0).findElement(By.cssSelector("div.ipe-ScoreGridCell_TextHeader")).getText();
            System.out.println("Period " + scorePeriod);
            String firstTeamScoreString;
            String secondTeamScoreString;

            if (scorePeriod.compareTo("T") != 0) {
                firstTeamScoreString = scoreDetails.get(1).findElement(By.cssSelector("div.ipe-ScoreGridCell_Text")).getText();
                secondTeamScoreString = scoreDetails.get(2).findElement(By.cssSelector("div.ipe-ScoreGridCell_Text")).getText();
            } else {
                firstTeamScoreString = scoreDetails.get(1).findElement(By.cssSelector("div.ipe-ScoreGridCell_TextTotal")).getText();
                secondTeamScoreString = scoreDetails.get(2).findElement(By.cssSelector("div.ipe-ScoreGridCell_TextTotal")).getText();
            }

            int firstTeamScoreInt = 0;
            int secondTeamScoreStringInt = 0;
            if (firstTeamScoreString.compareTo(" ") != 0) {
                firstTeamScoreInt = Integer.parseInt(firstTeamScoreString);
                secondTeamScoreStringInt = Integer.parseInt(secondTeamScoreString);
            }

            switch (scorePeriod) {
                case "1":
                    matchLive.getMatchScoreStatistic().put(SelectionBet.Q1, firstTeamScoreInt + secondTeamScoreStringInt);
                    break;
                case "2":
                    matchLive.getMatchScoreStatistic().put(SelectionBet.Q2, firstTeamScoreInt + secondTeamScoreStringInt);
                    break;
                case "Half":
                    matchLive.getMatchScoreStatistic().put(SelectionBet.Half1st, firstTeamScoreInt + secondTeamScoreStringInt);
                    break;
                case "3":
                    matchLive.getMatchScoreStatistic().put(SelectionBet.Q3, firstTeamScoreInt + secondTeamScoreStringInt);
                    break;
                case "4":
                    matchLive.getMatchScoreStatistic().put(SelectionBet.Q4, firstTeamScoreInt + secondTeamScoreStringInt);
                    break;
                case "T":
                    matchLive.getMatchScoreStatistic().put(SelectionBet.Match, firstTeamScoreInt + secondTeamScoreStringInt);
                    break;
            }
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
    }

    @Override
    public void checkMaximumBetSizeAndPossibleBetSelection(WebDriver driver, MatchLive matchLive) {

        log.info("Проверяем максимальную ставку и варианты для ставки");
        System.out.println("getting all market groups and find Odd/Even group....");
        List<WebElement> marketGroups = driver.findElements(By.cssSelector("div.gl-MarketGroup"));
        WebElement elementOddEven = null;
        String oddEvenString = null;
        try {
            elementOddEven = marketGroups.get(marketGroups.size() - 1);
            oddEvenString = elementOddEven.findElement(By.cssSelector("span.gl-MarketGroupButton_Text")).getText();
        } catch (Exception e) {
            System.out.println("String must be Odd/Enen : " + oddEvenString);
        }

        if (oddEvenString.contains("Odd/Even")) {
            List<WebElement> divs = elementOddEven.findElements(By.cssSelector("div"));
            System.out.println("The size of div is: " + divs.size() + " ...");
//            String spanText = divs.get(0).findElement(By.cssSelector(".gl-MarketGroupButton_Text")).getText();
            System.out.println("compare names of groups to find Odd/Even block....");
            System.out.println("Open field if it is necessary....");
            if (divs.size() < 3) {
                elementOddEven.click();
            }
            matchLive.getPossibleBetSelectionBet().clear();
            List<WebElement> columnsOfData = elementOddEven.findElements(By.cssSelector(".gl-Market_General"));
            for (WebElement element1 : columnsOfData) {
                String textPeriod = element1.findElement(By.cssSelector(".gl-MarketColumnHeader")).getText();
                System.out.println("textPeriod is: " + textPeriod);
                if (textPeriod.length() < 3) {
                    System.out.println("this is first column...");
                } else {// 2nd Half,Match,1st Half
                    SelectionBet selectionBet1;
                    switch (textPeriod) {
                        case "1st Quarter":
                            selectionBet1 = SelectionBet.Q1;
                            break;
                        case "2nd Quarter":
                            selectionBet1 = SelectionBet.Q2;
                            break;
                        case "3rd Quarter":
                            selectionBet1 = SelectionBet.Q3;
                            break;
                        case "4th Quarter":
                            selectionBet1 = SelectionBet.Q4;
                            break;
                        case "1st Half":
                            selectionBet1 = SelectionBet.Half1st;
                            break;
                        case "2nd Half":
                            selectionBet1 = SelectionBet.Half2nd;
                            break;
                        case "Match":
                            selectionBet1 = SelectionBet.Match;
                            break;
                        default:
                            selectionBet1 = null;
                    }
                    if (selectionBet1 != null) {
                        matchLive.getPossibleBetSelectionBet().add(selectionBet1);
                    }
                }
            }

            if (matchLive.getMaxBet() == 0.0) {
                String text = "Match";
                boolean click = false;
                System.out.println("textBetPeriod is: " + text);
                for (WebElement element1 : columnsOfData) {
                    String textPeriod = element1.findElement(By.cssSelector(".gl-MarketColumnHeader")).getText();
                    System.out.println("textPeriod is: " + textPeriod);
                    if (textPeriod.length() < 3) {
                        System.out.println("this is first column...");
                    } else {
                        System.out.println("The element header is:" + textPeriod + "....");
                        if (text.contains(textPeriod)) {
                            List<WebElement> oddEven = element1.findElements(By.cssSelector(".gl-Participant_General"));
                            oddEven.get(0).click();
                            click = true;
                            break;
                        }
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (click) {
                    try {
                        WebDriver bsFrame = driver.switchTo().frame("bsFrame");
                        bsFrame.findElement(By.cssSelector("span.bs-BetMax_Text")).click();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        String maxBetString = bsFrame.findElement(By.cssSelector("span.bs-TotalStake")).getText();
                        double maxBetDouble = 0.0;
                        try {
                            maxBetDouble = Double.parseDouble(maxBetString);
                            matchLive.setMaxBet(maxBetDouble);
                        } catch (NumberFormatException e) {
                            System.out.println("Wrong max bet String....");
                        }
                        System.out.println("Max bet is: " + matchLive.getMaxBet());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bsFrame.findElement(By.cssSelector("a.bs-Header_RemoveAllLink")).click();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        driver.switchTo().defaultContent();
                    }
                }
            }
        }
    }

    private void showMoreBetsToCheck(WebDriver driver) {
        try {
            driver.findElement(By.cssSelector("div.mbr-ShowMoreBetsButtonRhs")).click();
            Thread.sleep(1000);
            showMoreBetsToCheck(driver);
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public void goBackToOverview(WebDriver driver) {
        log.info("Возвращаемся к списку матчей");
        driver.findElements(By.cssSelector(".ip-ControlBar_BBarItem")).get(0).click();
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
        this.log = Logger.getLogger(ParserBet365.class);
        this.log.addAppender(appender);
    }

    @Override
    public Appender getAppender() {
        return appender;
    }
}
