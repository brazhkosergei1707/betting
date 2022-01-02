package parser.betsbc;

import UI.startFrame.StartFrame;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class ParserBetSBC implements Parser {
    private Logger log;
    private Appender appender;

    private int switcherId;

    private boolean isActiveHistory = false;
    private boolean isActiveDetails = false;
    private boolean haveEmergencyMatches = false;
    List<MatchLive> emergencyMatches = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat();

    Random random = new Random();


    @Override
    public Bet checkBet(Bet bet, WebDriver driver) {
        try {

            try {
                driver.findElement(By.xpath("//button[text()='Refresh page']")).click();
            } catch (Exception e) {
                System.out.println("Нет сообщения об обновлении страницы");
            }

            log.info("Проверяем ставку для матча " + bet.getMatchName());
            driver.findElement(By.cssSelector("span.logo-ico")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("");
            driver.findElement(By.cssSelector("a[href='/v3/en/account/current/']")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean isBet = true;

            List<WebElement> historyBets = null;
            try {
                driver.findElements(By.cssSelector("li.account-menu-container__item")).get(1).click();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                historyBets = driver.findElements(By.cssSelector("div.account-current-table__container"));
                System.out.println("Найдено " + historyBets.size() + " ставок при проверке ставки");
                log.info("Найдено " + historyBets.size() + " ставок при проверке ставки");
            } catch (Exception e) {
                isBet = false;
            }



            try {

            } catch (Exception e) {
                e.printStackTrace();
                log.info("Ставок не найдено.");

            }

            if (isBet) {
                for (WebElement element : historyBets) {
                    element.click();
                    OddEven oddEven = null;
                    SelectionBet selectionBet = null;

                    Boolean isWin = null;
                    String isWinString = null;

                    List<WebElement> betData = null;
                    log.info("Находим информацию о ставке.");
                    try {
                        betData = element.findElements(By.cssSelector("div.account-current-table__add-item"));
                        System.out.println("Was finded " + betData.size() + " bets");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    log.info("Вся информация " + betData);

                    String mathcInformation = betData.get(1).getText();
                    System.out.println("Match name information is: " + mathcInformation);
                    log.info("Информация о матче ставки " + mathcInformation);
                    String[] split = mathcInformation.split(" - ");

                    mathcInformation = betData.get(2).getText();
                    System.out.println("Bet information is: " + mathcInformation);
                    if (mathcInformation.contains("Odd")) {
                        oddEven = OddEven.Odd;
                    } else if (mathcInformation.contains("Even")) {
                        oddEven = OddEven.Even;
                    }

                    if (mathcInformation.contains("1st quarter")) {//1st Quarter - Total Odd/Even
                        selectionBet = SelectionBet.Q1;
                    } else if (mathcInformation.contains("2nd quarter")) {//
                        selectionBet = SelectionBet.Q2;
                    } else if (mathcInformation.contains("3rd quarter")) {//3rd Quarter - Total Odd/Even
                        selectionBet = SelectionBet.Q3;
                    } else if (mathcInformation.contains("4th quarter")) {
                        selectionBet = SelectionBet.Q4;
                    } else if (mathcInformation.contains("1st half")) {//1st Half Total Odd/Even
                        selectionBet = SelectionBet.Half1st;
                    } else if (mathcInformation.contains("2nd half")) {//2nd Half - Total Odd/Even
                        selectionBet = SelectionBet.Half2nd;
                    } else {//Game Total - Odd/Even,
                        selectionBet = SelectionBet.Match;
                    }

                    System.out.println("Selection bet is: " + selectionBet);

                    try {
                        isWinString = element.findElements(By.cssSelector("span.account-current-table-row__currency")).get(1).getText();
                        System.out.println("This bet win " + isWinString);
                        log.info("Сколько выиграла эта ставка " + isWinString);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (isWinString != null) {
                        if (parseDouble(isWinString) > 0.0) {
                            isWin = true;
                        } else {
                            isWin = false;
                        }
                    }
                    System.out.println("Bet isWin is: " + isWin);

                    if (isWin != null) {
                        if (bet.getMatchName().contains(split[0])) {
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
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            System.out.println("ERROR DURING CHAKING ");
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
        boolean betAlreadyNotDone;
        Boolean b1 = betAlreadyNotPlayed(bet, driver);
        if (b1 != null) {
            betAlreadyNotDone = b1;
        } else {
            betAlreadyNotDone = true;
        }

        try {
            List<WebElement> elements1 = driver.findElements(By.cssSelector("span.cart-base-item-title__remove"));
            System.out.println("Сейчас есть в списке " + elements1.size() + " ставок");
            int size = elements1.size();
            for (int i = 0; i < size; i++) {
                WebElement element = driver.findElement(By.cssSelector("span.cart-base-item-title__remove"));
                if (!element.isDisplayed()) {
                    driver.findElement(By.xpath("//div[text()='Cart']")).click();
                    element.click();
                } else {
                    element.click();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Сейчас нет ставок.");
        }

        if (betAlreadyNotDone) {
            bet.setMatchPeriod(matchLive.getCurrentPeriodInt());
            boolean ok = false;
            boolean click = false;
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector("div.content_dop"));
                if (bet.getSelectionBet() == SelectionBet.Match) {
                    for (WebElement element : elements) {
                        try {
                            String span = element.findElement(By.cssSelector("div.title_dop")).findElement(By.cssSelector("span")).getText();
                            System.out.println("Find " + span);
                            if (span.contains("EVEN/ODD TOTAL")) {
                                if (bet.getOddEven() == OddEven.Even) {
                                    element.findElements(By.cssSelector("span.kef_dop")).get(0).click();
                                    click = true;
                                } else {
                                    element.findElements(By.cssSelector("span.kef_dop")).get(1).click();
                                    click = true;
                                }
                            }
                            if (click) {
                                break;
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }
                } else if (bet.getSelectionBet() == SelectionBet.Half1st ||
                        bet.getSelectionBet() == SelectionBet.Half2nd) {
                    for (WebElement element : elements) {
                        System.out.println("find " + elements.size() + " elements");
                        String span = element.findElement(By.cssSelector("div.title_dop")).findElement(By.cssSelector("span")).getText();
                        System.out.println("Find " + span);
                        if (span.contains("HALVES RESULT")) {
                            String halfResultText = element.findElement(By.cssSelector("div.sub_title_dop__")).getText();
                            if (bet.getSelectionBet() == SelectionBet.Half1st) {
                                if (halfResultText.contains("1st half")) {
                                    ok = true;
                                }
                            } else if (bet.getSelectionBet() == SelectionBet.Half2nd) {
                                if (halfResultText.contains("2nd half")) {
                                    ok = true;
                                }
                            }
                            if (ok) {
                                try {
                                    List<WebElement> elements1 = element.findElements(By.cssSelector("div.content_box_dop"));
                                    for (WebElement element1 : elements1) {
                                        if (bet.getOddEven() == OddEven.Even) {
                                            if (element1.findElement(By.cssSelector("div.inline")).getText().contains("Even")) {
                                                element1.findElement(By.cssSelector("span.kef_dop")).click();
                                                click = true;
                                            }
                                        } else {
                                            if (element1.findElement(By.cssSelector("div.inline")).getText().contains("Odd")) {
                                                element1.findElement(By.cssSelector("span.kef_dop")).click();
                                                click = true;
                                            }
                                        }
                                        if (click) {
                                            System.out.println("Inside break 1254");
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (click) {
                            System.out.println("Inside break 1264");
                            break;
                        }
                    }
                } else if (bet.getSelectionBet() == SelectionBet.Q4 ||
                        bet.getSelectionBet() == SelectionBet.Q3 ||
                        bet.getSelectionBet() == SelectionBet.Q2 ||
                        bet.getSelectionBet() == SelectionBet.Q1) {
                    for (WebElement element : elements) {
                        String span = element.findElement(By.cssSelector("div.title_dop")).findElement(By.cssSelector("span")).getText();
                        if (span.contains("QUARTERS RESULT")) {//HALVES RESULT= = 1st half
                            String quarterResultText = element.findElement(By.cssSelector("div.sub_title_dop__")).getText();
                            if (bet.getSelectionBet() == SelectionBet.Q1) {
                                if (quarterResultText.contains("1st")) {
                                    ok = true;
                                }
                            } else if (bet.getSelectionBet() == SelectionBet.Q2) {
                                if (quarterResultText.contains("2nd")) {
                                    ok = true;
                                }
                            } else if (bet.getSelectionBet() == SelectionBet.Q3) {
                                if (quarterResultText.contains("3rd")) {
                                    ok = true;
                                }
                            } else if (bet.getSelectionBet() == SelectionBet.Q4) {
                                if (quarterResultText.contains("4th")) {
                                    ok = true;
                                }
                            }

                            if (ok) {
                                try {
                                    List<WebElement> elements1 = element.findElements(By.cssSelector("div.content_box_dop"));
                                    for (WebElement element1 : elements1) {
                                        if (bet.getOddEven() == OddEven.Even) {
                                            if (element1.findElement(By.cssSelector("div.inline")).getText().contains("Even")) {
                                                element1.findElement(By.cssSelector("span.kef_dop")).click();
                                                click = true;
                                            }
                                        } else {
                                            if (element1.findElement(By.cssSelector("div.inline")).getText().contains("Odd")) {
                                                element1.findElement(By.cssSelector("span.kef_dop")).click();
                                                click = true;
                                            }
                                        }
                                        if (click) {
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                if (click) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    WebElement inputBet = driver.findElement(By.cssSelector("input.cart-base-item-sum__input"));
                    inputBet.click();
                    inputBet.clear();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    inputBet.sendKeys(String.valueOf(bet.getPrice()));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    String valueOfBetString = inputBet.getAttribute("value");
                    double priceOfBet = 0.0;
                    if (valueOfBetString != null) {
                        try {
                            priceOfBet = Double.parseDouble(valueOfBetString);
                        } catch (Exception e) {
                            System.out.println("ParserBetSBC ОШИБКА: 358");
                        }
                    }

                    if (priceOfBet == bet.getPrice()) {
                        try {
                            driver.findElement(By.cssSelector("input.account-details__save-button")).click();
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.info("Проблема во время клика при установке ставки");
                        }
                    }

                    try {
                        String text = driver.findElement(By.cssSelector("div.cart-base-success__title")).getText();
                        System.out.println("Текст для установки ставок-" + text);
                        if (text.contains("YOUR BET IS ACCEPTED")) {//Your bet is accepted
                            //  cart-base-success__title  MAKING BETS IS COMPLETED
                            betAlreadyNotDone = false;
                            System.out.println("Ставка сделана, первая проверка, имя матча " + bet.getMatchName());
                            log.info("Ставка сделана, первая проверка, имя матча " + bet.getMatchName());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error(e.getLocalizedMessage());
                        if (!betAlreadyNotDone) {
                            log.info("Ставка сделана, время ставки " + new Date(System.currentTimeMillis()));
                            System.out.println("Ставка сделана, время ставки " + new Date(System.currentTimeMillis()));
                            bet.setTimeBet(System.currentTimeMillis());
                        }
                        return bet;
                    }

                    if (betAlreadyNotDone) {
                        Boolean b = betAlreadyNotPlayed(bet, driver);
                        if (b != null) {
                            betAlreadyNotDone = b;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getLocalizedMessage());
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

    @Override
    public boolean logIn(String username, String password, WebDriver driver) {
        try {
            log.info("Заходим на сайт http://www.betsbc.com/v3/en/...");
            driver.get("http://www.betsbc.com/v3/en/");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Находим кнопку логирования.");
            driver.findElement(By.xpath("//a[text()='Sign in']")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            log.info("Находим и вводим логин.");
            WebElement loginField = driver.findElement(By.cssSelector("input#login"));
            loginField.clear();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            loginField.click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            loginField.sendKeys(username);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            log.info("Находим и вводим пароль.");
            WebElement passwordField = driver.findElement(By.cssSelector("input#password"));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            passwordField.clear();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            passwordField.click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            passwordField.sendKeys(password);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Вход.");
            driver.findElement(By.cssSelector("button.authorization-form-body__submit")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean logOut(WebDriver driver) {
        try {
            log.info("Находим кнопку выхода, и выходим.");
            driver.findElement(By.cssSelector("a.img_exit")).click();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void setCurrentTimeAndCount(WebDriver driver) {
//        if (StartFrame.getSwitcher(this, switcherId) != null) {
//            StartFrame.getSwitcher(this, switcherId).getStorage().setCurrentTime(System.currentTimeMillis());
//        }
        try {
            log.info("Считываем состояние счета.");
//            driver.findElement(By.cssSelector("a.img_user_refresh")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String balance = driver.findElement(By.cssSelector("div.data_balance")).getText();
            log.info("Текущее состояние счета STRING " + balance);
            Double doubleBalance = parseDouble(balance);
//            if (StartFrame.getSwitcher(this, switcherId) != null) {
//                StartFrame.getSwitcher(this, switcherId).getStorage().setCurrentBalance(doubleBalance);
//            }
//            System.out.println(" Current balance is: " + balance);
//            StartFrame.getSwitcher(this, switcherId).getStorage().setCurrentBalanceString(balance);
            log.info("Текущее состояние счета " + doubleBalance);
            System.out.println("Current balance is: " + balance);
            System.out.println("DoubleStringBalance is: " + doubleBalance);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    private double parseDouble(String balance) {
        System.out.println("Новый баланс - " + balance);

        double doubleBalance = 0.0;
        String correctString = "";

        String[] split1 = balance.split(" ");
        correctString = split1[0];//10461.33 rouble
//        if (balance.contains("rouble")) {
//            int e = balance.lastIndexOf("e");
//            String doubleStringBalance = balance.substring(e + 1, balance.length() - 1);
//            if (doubleStringBalance.contains(",")) {
//                String[] split = doubleStringBalance.split(",");
//                for (int i = 0; i < split.length; i++) {
//                    correctString = correctString + split[i];
//                }
//            } else {
//                correctString = doubleStringBalance;
//            }
//            System.out.println(correctString);
//
//        } else if (balance.contains("euro")) {
//            int e = balance.lastIndexOf("o");
//            String doubleStringBalance = balance.substring(e + 1, balance.length() - 1);
//            if (doubleStringBalance.contains(",")) {
//                String[] split = doubleStringBalance.split(",");
//                for (int i = 0; i < split.length; i++) {
//                    correctString = correctString + split[i];
//                }
//            } else {
//                correctString = doubleStringBalance;
//            }
//            System.out.println("Correct string for dollar account" + correctString);
//        } else {
//            correctString = balance;
//        }
        try {
            doubleBalance = Double.parseDouble(correctString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return doubleBalance;
    }

    @Override
    public void updateHistoryBet(WebDriver driver) {
        try {
            driver.findElement(By.cssSelector("span.logo-ico")).click();

            WebElement element1 = driver.findElement(By.cssSelector("span.logo-ico"));
            Actions actions = new Actions(driver);
            actions.keyDown(Keys.LEFT_CONTROL)
                    .click(element1)
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

            driver.switchTo().window(driver.getWindowHandles().toArray()[0].toString());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            driver.findElement(By.cssSelector("a[href='/v3/en/account/current/']")).click();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            driver.findElements(By.cssSelector("li.account-menu-container__item")).get(1).click();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<Bet> currentList = null;//StartFrame.getSwitcher(this, switcherId).getStorage().getHistoryBets();

            boolean isBet = true;
            List<WebElement> historyBets = null;
            try {
                historyBets = driver.findElements(By.cssSelector("div.account-current-table__container"));
                System.out.println("Найдено " + historyBets.size() + " ставок. Начальное сканирование.");
                log.info("Найдено " + historyBets.size() + " ставок. Начальное сканирование.");
            } catch (Exception e) {
                e.printStackTrace();
                log.info("Ставок не найдено.");
                isBet = false;
            }

            if (isBet) {
                for (WebElement element : historyBets) {
                    try {
                        element.click();
                        Bet bet;
                        long dateOfBet = 0L;
                        String dateString = null;
                        Date date = null;
                        String nameOfMatch = null;
                        OddEven oddEven = null;
                        SelectionBet selectionBet = null;
                        int periodBetInt = 0;
                        double priceOfBet = 0.0;
                        String priceOfBetString = null;
                        Boolean isWin = null;
                        String isWinString = null;
                        List<WebElement> betData = null;
                        try {
                            betData = element.findElements(By.cssSelector("div.account-current-table__add-item"));
                            System.out.println("Was finded " + betData.size() + " elements");
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }

                        if (betData != null) {
                            dateString = betData.get(0).getText();
                        }

                        if (dateString != null) {
                            String[] split = dateString.split(", ");
                            System.out.println("The time of bet is " + split[1]);
                            try {
                                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                                dateFormat.applyPattern("HH:mm");
                                date = dateFormat.parse(split[1]);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            dateOfBet = date.getTime();
                            System.out.println("Date of bet is: " + date);
                            System.out.println("Date of bet is: " + dateOfBet);
                        }
                        String matchInformation;

                        try {
                            matchInformation = betData.get(1).getText();
                            System.out.println("Match name information is: " + matchInformation);
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                        if (matchInformation != null) {
                            String[] split = matchInformation.split(" - ");
                            nameOfMatch = split[0] + " vs " + split[1];
                        }
                        matchInformation = null;
                        try {
                            matchInformation = betData.get(2).getText();
                            System.out.println("Bet information is: " + matchInformation);
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }

                        if (matchInformation != null) {
                            if (matchInformation.contains("Odd")) {
                                oddEven = OddEven.Odd;
                            } else if (matchInformation.contains("Even")) {
                                oddEven = OddEven.Even;
                            }
                            if (matchInformation.contains("1st quarter")) {//1st Quarter - Total Odd/Even
                                selectionBet = SelectionBet.Q1;
                                periodBetInt = 1;
                            } else if (matchInformation.contains("2nd quarter")) {//
                                selectionBet = SelectionBet.Q2;
                                periodBetInt = 2;
                            } else if (matchInformation.contains("3rd quarter")) {//3rd Quarter - Total Odd/Even
                                selectionBet = SelectionBet.Q3;
                                periodBetInt = 3;
                            } else if (matchInformation.contains("4th quarter")) {
                                selectionBet = SelectionBet.Q4;
                                periodBetInt = 4;
                            } else if (matchInformation.contains("1st half")) {//1st Half Total Odd/Even
                                selectionBet = SelectionBet.Half1st;
                                periodBetInt = 2;
                            } else if (matchInformation.contains("2nd half")) {//2nd Half - Total Odd/Even
                                selectionBet = SelectionBet.Half2nd;
                                periodBetInt = 4;
                            } else {//Game Total - Odd/Even,
                                selectionBet = SelectionBet.Match;
                                periodBetInt = 4;
                            }
                        }


                        try {
                            priceOfBetString = element.findElements(By.cssSelector("span.account-current-table-row__currency")).get(0).getText();
                            System.out.println("Price of bet is " + priceOfBetString);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (priceOfBetString != null) {
                            priceOfBet = parseDouble(priceOfBetString);
                        }

                        try {
                            isWinString = element.findElements(By.cssSelector("span.account-current-table-row__currency")).get(1).getText();
                            System.out.println("Win of bet is " + priceOfBetString);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (isWinString != null) {
                            if (parseDouble(isWinString) > 0.0) {
                                isWin = true;
                            } else {
                                isWin = false;
                            }
                        }

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
            }
//        }
//            ===============================
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateCurrentMatchesLive(WebDriver driver) {
        boolean isMatches = false;
        try {
            driver.findElement(By.cssSelector("span.logo-ico")).click();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Открываем список матчей LIVE");
            driver.findElement(By.cssSelector("div.menu")).findElement(By.cssSelector("a[href='/v3/en/live/']")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.findElement(By.cssSelector("ul.live-menu-ul")).findElement(By.xpath("//a[text()='Live-bets']")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.findElement(By.xpath("//a[text()='All']")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            WebElement carouselItems = driver.findElement(By.cssSelector("ul.carousel-items"));
            try {
                List<WebElement> carouselBlocks = carouselItems.findElements(By.cssSelector("li.carousel-block"));
                for (WebElement element : carouselBlocks) {
                    String text = element.findElement(By.cssSelector("label.text")).findElement(By.cssSelector("span.sport_name")).getText();
                    if (text.compareTo("Basketball") == 0) {
                        System.out.println("Find basketball link");
                        element.click();
                        Thread.sleep(1000);
                        String basketLinkClassString = element.getAttribute("class");
                        System.out.println("Ссылка на матчи по баскетболу активна - " + basketLinkClassString);
                        if (basketLinkClassString.contains("active")) {
                            isMatches = true;
                        }
                        break;
                    }
                }
            } catch (NoSuchElementException e) {
                e.printStackTrace();
                log.info("Сейчас нет матчей.");
                System.out.println("Сейчас нет матчей");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (isMatches) {
                Map<Integer, MatchLive> currentMatches = null;
//                if (StartFrame.getSwitcher(this, switcherId) != null) {
//                    currentMatches = StartFrame.getSwitcher(this, switcherId).getStorage().getCurrentMatches();
//                }

                List<WebElement> matches = driver.findElements(By.cssSelector("div.live-list__championship-event"));
                for (WebElement matchElement : matches) {
                    try {
                        if (matchElement.isDisplayed()) {
                            String nameOfMatch;
                            SelectionBet currentPeriod = null;
                            String timeString = null;
                            String timeToQuarterFinishString = "00:00";
                            boolean isPossibleToMakeBet = true;

                            String firstTeamName = matchElement.findElement(By.cssSelector("span.name_ht")).getText();
                            String secondTeamName = matchElement.findElement(By.cssSelector("span.name_at")).getText();
                            if (firstTeamName == null || secondTeamName == null || firstTeamName.length() < 1 || secondTeamName.length() < 1) {
                                continue;
                            }

                            nameOfMatch = firstTeamName + " vs " + secondTeamName;//++++++++++++++++++++++++++++++++++
                            String lastPeriodsScore = null;
                            try {
                                lastPeriodsScore = matchElement.findElement(By.cssSelector("span.pruning")).getText();
                            } catch (NoSuchElementException e) {
                                log.info("Возможно первый период");
                                System.out.println("First period.");
                            }

                            String currentPeriodScore;
                            String totalPeriodScore = null;
                            try {
                                currentPeriodScore = matchElement.findElement(By.cssSelector("span.last-pruning")).getText();
                            } catch (NoSuchElementException e) {
                                currentPeriodScore = null;
                                log.info("Первый период снова убедились");
                                System.out.println("First period again");
                            }

                            if (currentPeriodScore == null) {
                                totalPeriodScore = lastPeriodsScore;
                            } else {
                                totalPeriodScore = lastPeriodsScore + currentPeriodScore;
                            }

                            System.out.println("Total periods - " + totalPeriodScore);
                            if (totalPeriodScore == null) {
                                currentPeriod = SelectionBet.Q1;
                            } else if (!totalPeriodScore.contains("(")) {
                                currentPeriod = null;
                            } else {
                                String[] split = totalPeriodScore.split(",");
                                switch (split.length) {
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
                            }

                            try {
                                timeString = matchElement.findElement(By.cssSelector("span.black")).getAttribute("data-time");
                            } catch (NoSuchElementException e) {
                                ;
                            }

                            if (timeString == null) {
                                timeToQuarterFinishString = "15:00";
                            } else {
                                timeToQuarterFinishString = timeString;
                            }

                            if (timeString == null && totalPeriodScore != null) {
                                timeString = "Break";
                            }

                            if (timeString != null) {
                                if (timeString.compareTo("Break") == 0) {
                                    if (currentPeriod == SelectionBet.Q1) {
                                        currentPeriod = SelectionBet.Q2;
                                    } else if (currentPeriod == SelectionBet.Q2) {
                                        currentPeriod = SelectionBet.Q3;
                                    } else if (currentPeriod == SelectionBet.Q3) {
                                        currentPeriod = SelectionBet.Q4;
                                    } else if (currentPeriod == SelectionBet.Q4) {
                                        currentPeriod = SelectionBet.OT;
                                    }
                                }
                            }


                            Date data = null;
                            dateFormat.applyPattern("mm:ss");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                            try {
                                data = dateFormat.parse(timeToQuarterFinishString);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            try {
                                matchElement.findElement(By.cssSelector("div.live-list__championship-event-dop"));
                                isPossibleToMakeBet = true;
                                System.out.println("Возможно делать ставку.");
                            } catch (Exception e) {
                                System.out.println("Невозможно делать ставку.");
                                isPossibleToMakeBet = false;
                            }

                            MatchLive matchLive;
                            Double maxBet = null;
                            if (currentMatches.containsKey(nameOfMatch.hashCode())) {
                                matchLive = currentMatches.get(nameOfMatch.hashCode());
                                matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
                                matchLive.setTimeToQuarterFinish(data.getTime());
                                if (currentPeriod != null) {
                                    matchLive.setCurrentPeriod(currentPeriod);
                                }

                                if (matchLive.getMaxBet() == 0 && isPossibleToMakeBet) {
                                    maxBet = setMaxBetForMatch(matchElement, driver);
                                    if (maxBet != null) {
                                        matchLive.setMaxBet(maxBet);
                                    }
                                }
                            } else {
                                if (isPossibleToMakeBet) {
                                    maxBet = setMaxBetForMatch(matchElement, driver);
                                }
//                                matchLive = StartFrame.getSwitcher(this, switcherId).getStorage().addMatchLive(nameOfMatch, isPossibleToMakeBet, data.getTime(), currentPeriod, maxBet);
                            }
                        }
                    } catch (NoSuchElementException e) {
                        e.printStackTrace();
                        log.info(e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.info(e.getMessage());
                    }
                }
            }
        } catch (NoSuchElementException e) {
            log.info("Ошибка");
            e.printStackTrace();
        }
    }

    @Override
    public void updateCurrentBet(WebDriver driver) {
        log.info("Обновляем список текущих ставок");
        List<Bet> list = null;//StartFrame.getSwitcher(this, switcherId).getStorage().getCurrentBets();
        try {
            driver.findElement(By.cssSelector("span.logo-ico")).click();

//            ================================
//            ================================
//            ================================
//            ================================


            WebElement element1 = driver.findElement(By.cssSelector("span.logo-ico"));
            Actions actions = new Actions(driver);
            actions.keyDown(Keys.LEFT_CONTROL)
                    .click(element1)
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
            driver.switchTo().window(driver.getWindowHandles().toArray()[0].toString());

//            ================================
//            ================================
//            ================================
//            ================================


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.findElement(By.cssSelector("a[href='/v3/en/account/current/']")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean isBet = true;
            List<WebElement> historyBets = null;
            try {
                historyBets = driver.findElements(By.cssSelector("div.account-current-table__container"));
                System.out.println("Найдено " + historyBets.size() + " ставок.");
                log.info("Найдено " + historyBets.size() + " ставок.");
            } catch (Exception e) {
                e.printStackTrace();
                log.info("Ставок не найдено.");
                isBet = false;
            }
            if (isBet) {
                for (WebElement element : historyBets) {
                    element.click();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Bet bet;
                    long dateOfBet = 0L;
                    String dateString = null;
                    Date date = null;
                    String nameOfMatch = null;
                    OddEven oddEven = null;
                    SelectionBet selectionBet = null;
                    int periodBetInt = 0;
                    double priceOfBet = 0.0;
                    String priceOfBetString = null;

                    List<WebElement> betData = null;
                    try {
                        betData = element.findElements(By.cssSelector("div.account-current-table__add-item"));
                        System.out.println("Was finded " + betData.size() + " elements");
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (betData != null) {
                        dateString = betData.get(0).getText();
                        System.out.println("The string data is: " + dateString);
                        System.out.println("The string data is: " + betData.get(1).getText());
                        System.out.println("The string data is: " + betData.get(2).getText());
                        System.out.println("The string data is: " + betData.get(3).getText());
                        System.out.println("The string data is: " + betData.get(4).getText());
                    } else {
                        continue;
                    }

                    if (dateString != null) {
                        String[] split = dateString.split(", ");
                        System.out.println("The time of bet is " + split[1]);
                        try {
                            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                            dateFormat.applyPattern("HH:mm");
                            date = dateFormat.parse(split[1]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        dateOfBet = date.getTime();
                        System.out.println("Date of bet is: " + date);
                        System.out.println("Date of bet is: " + dateOfBet);
                    } else {
                        continue;
                    }

                    String mathcInformation = betData.get(1).getText();
                    System.out.println("Match name information is: " + mathcInformation);
                    String[] split = mathcInformation.split(" - ");
                    nameOfMatch = split[0] + " vs " + split[1];

                    mathcInformation = betData.get(2).getText();
                    System.out.println("Bet information is: " + mathcInformation);
                    if (mathcInformation.contains("Odd")) {
                        oddEven = OddEven.Odd;
                    } else if (mathcInformation.contains("Even")) {
                        oddEven = OddEven.Even;
                    }


                    if (mathcInformation.contains("1st quarter")) {//1st Quarter - Total Odd/Even
                        selectionBet = SelectionBet.Q1;
                        periodBetInt = 1;
                    } else if (mathcInformation.contains("2nd quarter")) {//
                        selectionBet = SelectionBet.Q2;
                        periodBetInt = 2;
                    } else if (mathcInformation.contains("3rd quarter")) {//3rd Quarter - Total Odd/Even
                        selectionBet = SelectionBet.Q3;
                        periodBetInt = 3;
                    } else if (mathcInformation.contains("4th quarter")) {
                        selectionBet = SelectionBet.Q4;
                        periodBetInt = 4;
                    } else if (mathcInformation.contains("1st half")) {//1st Half Total Odd/Even
                        selectionBet = SelectionBet.Half1st;
                        periodBetInt = 2;
                    } else if (mathcInformation.contains("2nd half")) {//2nd Half - Total Odd/Even
                        selectionBet = SelectionBet.Half2nd;
                        periodBetInt = 4;
                    } else {//Game Total - Odd/Even,
                        selectionBet = SelectionBet.Match;
                        periodBetInt = 4;
                    }

                    try {
                        priceOfBetString = element.findElements(By.cssSelector("span.account-current-table-row__currency")).get(0).getText();
                        System.out.println("Price of bet is " + priceOfBetString);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (priceOfBetString != null) {
                        priceOfBet = parseDouble(priceOfBetString);
                    }

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
                        bet.setTimeBet(dateOfBet);
                        bet.setPlay(true);
                        bet.setMatchPeriod(periodBetInt);
                        System.out.println("new bet was added....");
                        list.add(bet);
                    }
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNameOfSite() {
        return "www.betsbc.com - " + switcherId;
    }

    @Override
    public String toString() {
        return "www.betsbc.com";
    }

    @Override
    public void scanMatches(WebDriver driver) {
        boolean isMatches = false;
        try {
            try {
                WebElement element1 = driver.findElement(By.cssSelector("span.logo-ico"));
                Actions actions = new Actions(driver);
                actions.keyDown(Keys.LEFT_CONTROL)
                        .click(element1)
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
                driver.switchTo().window(driver.getWindowHandles().toArray()[0].toString());
            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Открываем список матчей LIVE");
            driver.findElement(By.cssSelector("div.menu")).findElement(By.cssSelector("a[href='/v3/en/live/']")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.findElement(By.cssSelector("ul.live-menu-ul")).findElement(By.xpath("//a[text()='Live-bets']")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Map<Integer, MatchLive> currentMatches = null;
//            if (StartFrame.getSwitcher(this, switcherId) != null) {
//                currentMatches = StartFrame.getSwitcher(this, switcherId).getStorage().getCurrentMatches();
//            }

            if (currentMatches != null) {
                for (Integer integer : currentMatches.keySet()) {
                    MatchLive matchLive = currentMatches.get(integer);
                    if (matchLive.isFinished()) {
                        continue;
                    }
                    String[] split = matchLive.getNameOfMatch().split(" vs ");
                    try {
                        driver.findElement(By.xpath("//span[text()='" + split[0] + "']"));
                        driver.findElement(By.xpath("//span[text()='" + split[1] + "']"));
                        matchLive.setAlmostFinish(false);
                    } catch (Exception e) {
                        if (matchLive.isAlmostFinish()) {
                            matchLive.setFinished(true);
                            log.info("Матч " + matchLive.getNameOfMatch() + " уже окончен.");
                            System.out.println("Матч " + matchLive.getNameOfMatch() + " уже закончился.");
                        } else {
                            System.out.println("Возможно матч " + matchLive.getNameOfMatch() + " закончился, первая проверка.");
                            log.info("Возможно матч " + matchLive.getNameOfMatch() + " закончился, первая проверка.");
                            matchLive.setAlmostFinish(true);
                        }
                    }
                }
            }

            for (Integer integer : currentMatches.keySet()) {
                MatchLive matchLive = currentMatches.get(integer);
                if (matchLive.isFinished()) {
                    matchLive.makeNextEvent(driver);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            driver.findElement(By.cssSelector("div.menu")).findElement(By.cssSelector("a[href='/v3/en/live/']")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.findElement(By.cssSelector("ul.live-menu-ul")).findElement(By.xpath("//a[text()='Live-bets']")).click();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            driver.findElement(By.xpath("//a[text()='All']")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WebElement carouselItems = driver.findElement(By.cssSelector("ul.carousel-items"));
            try {
                List<WebElement> carouselBlocks = carouselItems.findElements(By.cssSelector("li.carousel-block"));
                for (WebElement element : carouselBlocks) {
                    String text = element.findElement(By.cssSelector("label.text")).findElement(By.cssSelector("span.sport_name")).getText();
                    if (text.compareTo("Basketball") == 0) {
                        System.out.println("Find basketball link");
                        element.click();
                        Thread.sleep(1000);
                        String basketLinkClassString = element.getAttribute("class");
                        System.out.println("Ссылка на матчи по баскетболу активна - " + basketLinkClassString);
                        if (basketLinkClassString.contains("active")) {
                            isMatches = true;
                        }
                        break;
                    }
                }
            } catch (NoSuchElementException e) {
                e.printStackTrace();
                log.info("Сейчас нет матчей.");
                System.out.println("Сейчас нет матчей");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (isMatches) {
                try {
                    List<WebElement> elements1 = driver.findElements(By.cssSelector("span.cart-base-item-title__remove"));
                    System.out.println("Сейчас есть в списке " + elements1.size() + " ставок");
                    int size = elements1.size();
                    for (int i = 0; i < size; i++) {
                        driver.findElement(By.cssSelector("span.cart-base-item-title__remove")).click();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Сейчас нет ставок.");
                }

                List<WebElement> matches = driver.findElements(By.cssSelector("div.live-list__championship-event"));
                int sizeOFMatches = matches.size();
                WebElement matchElement = null;
                MatchLive matchLive = null;
                String nameOfMatch;
                SelectionBet currentPeriod = null;
                String timeString = null;
                String timeToQuarterFinishString;
                boolean isPossibleToMakeBet;
                for (int i = 0; i < sizeOFMatches; i++) {
                    try {
                        if (matchLive != null) {
                            if (matchLive.isPossibleToMakeBet() && !matchLive.isBetIsDone()) {
                                System.out.println("Делаем событие для матча " + matchLive.getNameOfMatch());
                                matchLive.makeNextEvent(driver);
//                                if (matchLive.isBetIsDone()) {
//                                    if(emergencyMatches.contains(matchLive)){
//                                        emergencyMatches.remove(matchLive);
//                                    }
//                                }
                            }
                        }
                        matchLive = null;

//                        if (!StartFrame.getSwitcher(this, switcherId).isAppWork()) {
//                            return;
//                        }

                        matchElement = driver.findElements(By.cssSelector("div.live-list__championship-event")).get(i);
                        if (matchElement.isDisplayed()) {
                            String firstTeamName = matchElement.findElement(By.cssSelector("span.name_ht")).getText();
                            String secondTeamName = matchElement.findElement(By.cssSelector("span.name_at")).getText();
                            if (firstTeamName == null || secondTeamName == null || firstTeamName.length() < 1 || secondTeamName.length() < 1) {
                                continue;
                            }

                            nameOfMatch = firstTeamName + " vs " + secondTeamName;
                            String lastPeriodsScore = null;

                            driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
                            try {
                                lastPeriodsScore = matchElement.findElement(By.cssSelector("span.pruning")).getText();
                            } catch (NoSuchElementException e) {
                                log.info("Возможно первый период");
                                System.out.println("First period.");
                            }

                            String currentPeriodScore;
                            String totalPeriodScore = null;
                            try {
                                currentPeriodScore = matchElement.findElement(By.cssSelector("span.last-pruning")).getText();
                            } catch (NoSuchElementException e) {
                                currentPeriodScore = null;
                                log.info("Первый период снова убедились");
                                System.out.println("First period again");
                            }

                            if (currentPeriodScore == null) {
                                totalPeriodScore = lastPeriodsScore;
                            } else {
                                totalPeriodScore = lastPeriodsScore + currentPeriodScore;
                            }

                            System.out.println("Total periods - " + totalPeriodScore);
                            if (totalPeriodScore == null) {
                                currentPeriod = SelectionBet.Q1;
                            } else if (!totalPeriodScore.contains("(")) {
                                currentPeriod = null;
                            } else {
                                String[] split = totalPeriodScore.split(",");
                                switch (split.length) {
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
                            }

                            try {
                                timeString = matchElement.findElement(By.cssSelector("span.black")).getAttribute("data-time");
                            } catch (NoSuchElementException e) {
                                System.out.println("Первая четверть");
                            }

                            if (timeString == null) {
                                timeToQuarterFinishString = "15:00";
                            } else {
                                timeToQuarterFinishString = timeString;
                            }

                            if (timeString == null && totalPeriodScore != null) {
                                try {
                                    matchElement.findElement(By.xpath("//span[text()='Break']"));
                                    timeString = "Break";
                                    System.out.println("Break сработал.");
                                } catch (Exception e) {
                                    System.out.println("Не нашли ссылку на перерыв.");
                                }
                            }

                            if (timeString != null) {
                                if (timeString.compareTo("Break") == 0) {
                                    if (currentPeriod == SelectionBet.Q1) {
                                        currentPeriod = SelectionBet.Q2;
                                    } else if (currentPeriod == SelectionBet.Q2) {
                                        currentPeriod = SelectionBet.Q3;
                                    } else if (currentPeriod == SelectionBet.Q3) {
                                        currentPeriod = SelectionBet.Q4;
                                    } else if (currentPeriod == SelectionBet.Q4) {
                                        currentPeriod = SelectionBet.OT;
                                    }
                                }
                            }
                            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

                            Date data = null;
                            dateFormat.applyPattern("mm:ss");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                            try {
                                data = dateFormat.parse(timeToQuarterFinishString);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            try {
                                matchElement.findElement(By.cssSelector("div.live-list__championship-event-dop"));
                                isPossibleToMakeBet = true;
                                System.out.println("Возможно делать ставку.");
                            } catch (Exception e) {
                                System.out.println("Невозможно делать ставку.");
                                isPossibleToMakeBet = false;
                            }

                            Double maxBet = null;
                            if (currentMatches.containsKey(nameOfMatch.hashCode())) {
                                matchLive = currentMatches.get(nameOfMatch.hashCode());
                                matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
                                matchLive.setTimeToQuarterFinish(data.getTime());
                                if (currentPeriod != null) {
                                    matchLive.setCurrentPeriod(currentPeriod);
                                }

                                if (matchLive.getMaxBet() == 0 && isPossibleToMakeBet) {
                                    maxBet = setMaxBetForMatch(matchElement, driver);
                                    if (maxBet != null) {
                                        matchLive.setMaxBet(maxBet);
                                    }
                                }
                            } else {
                                if (isPossibleToMakeBet) {
                                    maxBet = setMaxBetForMatch(matchElement, driver);
                                }
//                                matchLive = StartFrame.getSwitcher(this, switcherId).getStorage().addMatchLive(nameOfMatch, isPossibleToMakeBet, data.getTime(), currentPeriod, maxBet);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.info(e.getMessage());
                    }
                }
            }
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private Double setMaxBetForMatch(WebElement matchElement, WebDriver driver) {
        System.out.println("Определяем максимальную ставку.");
        Double maxBet = null;
        try {
            try {
                matchElement.findElement(By.cssSelector("div.live-list__championship-event-dop")).click();
            } catch (Exception e) {
                return maxBet;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<WebElement> elements = driver.findElements(By.cssSelector("div.content_dop"));
            System.out.println("find " + elements.size() + " elements");
            for (WebElement element : elements) {
                try {
                    String span = element.findElement(By.cssSelector("div.title_dop")).findElement(By.cssSelector("span")).getText();
                    if (span.contains("EVEN/ODD TOTAL")) {
                        try {
                            element.findElements(By.cssSelector("span.kef_dop")).get(0).click();
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            try {
                                String text = driver.findElement(By.cssSelector("span.cart-base-item-sum__input-container"))
                                        .findElement(By.cssSelector("input")).getAttribute("placeholder");
                                String[] split = text.split(" ");
                                maxBet = parseDouble(split[1]);
                                System.out.println("Максимальная ставка равна " + maxBet);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                List<WebElement> elements1 = driver.findElements(By.cssSelector("span.cart-base-item-title__remove"));
                                System.out.println("Сейчас есть в списке " + elements1.size() + " ставок");
                                int size = elements1.size();
                                for (int i = 0; i < size; i++) {
                                    driver.findElement(By.cssSelector("span.cart-base-item-title__remove")).click();
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("Сейчас нет ставок.");
                            }
                            element.findElement(By.cssSelector("div.title_dop")).findElement(By.cssSelector("span")).click();
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                WebElement element = driver.findElement(By.xpath("//h2[contains(text(),'Basketball.')]"));
                Actions actions = new Actions(driver);
                actions.moveToElement(element);
                actions.perform();
                Thread.sleep(500);
                matchElement.findElement(By.cssSelector("div.live-list__championship-event-dop")).click();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maxBet;
    }

    @Override
    public boolean clickToMatch(WebDriver driver, String nameOfMatch) {
        boolean insideMatch = false;
        String firstTeam = nameOfMatch.split(" vs ")[0];
        String secondTeam = nameOfMatch.split(" vs ")[1];
        try {
            driver.findElement(By.xpath("//a[text()='All']")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WebElement carouselItems = driver.findElement(By.cssSelector("ul.carousel-items"));
            try {
                List<WebElement> carouselBlocks = carouselItems.findElements(By.cssSelector("li.carousel-block"));
                for (WebElement element : carouselBlocks) {
                    String text = element.findElement(By.cssSelector("label.text")).findElement(By.cssSelector("span.sport_name")).getText();
                    if (text.compareTo("Basketball") == 0) {
                        System.out.println("Find basketball link");
                        element.click();
                        Thread.sleep(1000);
                        break;
                    }
                }
            } catch (NoSuchElementException e) {
                e.printStackTrace();
                log.info("Сейчас нет матчей.");
                System.out.println("Сейчас нет матчей");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<WebElement> teamLinks = driver.findElements(By.cssSelector("div.live-list__championship-event "));
            for (WebElement element : teamLinks) {
                try {
                    String text1 = element.findElement(By.cssSelector("span.name_ht")).getText();
                    if (text1.compareTo(firstTeam) == 0) {
                        String text = element.findElement(By.cssSelector("span.name_at")).getText();
                        if (text.compareTo(secondTeam) == 0) {
                            Actions actions = new Actions(driver);
                            actions.moveToElement(element);
                            actions.perform();
                            element.click();
                            insideMatch = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return insideMatch;
    }

    @Override
    public Boolean betAlreadyNotPlayed(Bet lastBet, WebDriver driver) {
        Boolean betIsNotPlayed = true;
        log.info("Проверяем есть ли ставка в списке текущих ставок на сайте " + getNameOfSite());
        boolean isBet = false;
        List<WebElement> currentBets = null;


        try {
            WebElement element = driver.findElement(By.cssSelector("a[href='/v3/en/account/current/']"));
            element.click();

            Thread.sleep(1500);
            isBet = !driver.findElement(By.xpath("//div[text()='No data']")).isDisplayed();
            if (isBet) {
                currentBets = driver.findElements(By.cssSelector("div.account-current-table__container"));
                if (currentBets.size() == 0) {
                    log.info("Ставок нет в списке текущих ставок. Не смогли найти ставки на сайте " + getNameOfSite());
                    isBet = false;
                }
            }
        } catch (Exception e) {
            log.info("Сейчас нет ставок в списке текущих ставок на сайте " + getNameOfSite());
            System.out.println("Ставок нет.");
        }

        if (isBet) {
            String nameOfMatch = null;
            SelectionBet selectionBet;
            List<WebElement> betData = null;

            for (WebElement element : currentBets) {
                try {
                    try {
                        betData = null;
                        element.click();
                        if (element != null) {
                            betData = element.findElements(By.cssSelector("div.account-current-table__add-item"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error(e.getMessage());
                    }

                    if (betData != null) {
                        String matchInformation = betData.get(1).getText();
                        if (matchInformation != null) {
                            String[] split = matchInformation.split(" - ");
                            nameOfMatch = split[0] + " vs " + split[1];
                        }

                        matchInformation = betData.get(2).getText();
                        if (matchInformation.contains("1st quarter")) {
                            selectionBet = SelectionBet.Q1;
                        } else if (matchInformation.contains("2nd quarter")) {
                            selectionBet = SelectionBet.Q2;
                        } else if (matchInformation.contains("3rd quarter")) {
                            selectionBet = SelectionBet.Q3;
                        } else if (matchInformation.contains("4th quarter")) {
                            selectionBet = SelectionBet.Q4;
                        } else if (matchInformation.contains("1st half")) {
                            selectionBet = SelectionBet.Half1st;
                        } else if (matchInformation.contains("2nd half")) {
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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info(e.getMessage());
                }
            }
        }

        if (lastBet.getTimeBet() == -7200000L) {
            try {
                driver.findElement(By.cssSelector("span.logo-ico")).click();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("Возвращаемся к списку матчей, после проверки ставки на сайте " + getNameOfSite());
                driver.findElement(By.cssSelector("a[href='/v3/en/live/']")).click();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                driver.findElement(By.cssSelector("a[href='/v3/en/live/']")).click();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                clickToMatch(driver, lastBet.getMatchName());
            } catch (Exception e) {
                e.printStackTrace();
                log.info(e.getMessage());
            }
        }
        log.info("Возвращаем - " + betIsNotPlayed);
        return betIsNotPlayed;
    }

    @Override
    public void checkGameScore(WebDriver driver, MatchLive matchLive) {
        try {
            String totalScore = driver.findElement(By.cssSelector("div.secondary-score")).getText();
            log.info("Результаты матча " + matchLive.getNameOfMatch() + " - " + totalScore);
            System.out.println("Total score is: " + totalScore);
            Map<SelectionBet, Integer> matchScoreStatistic = matchLive.getMatchScoreStatistic();
            if (totalScore != null) {
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
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("First quarter////");
        }
    }

    @Override
    public void checkMaximumBetSizeAndPossibleBetSelection(WebDriver driver, MatchLive matchLive) {
        matchLive.getPossibleBetSelectionBet().clear();
        try {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<WebElement> elements = driver.findElements(By.cssSelector("div.content_dop"));
            for (WebElement element : elements) {
                String span = element.findElement(By.cssSelector("div.title_dop")).findElement(By.cssSelector("span")).getText();
                System.out.println("Текст элемента :" + span);
                if (span.contains("EVEN/ODD TOTAL")) {
                    matchLive.getPossibleBetSelectionBet().add(SelectionBet.Match);
                    if (matchLive.getMaxBet() == 0.0) {
                        try {
                            element.findElements(By.cssSelector("span.kef_dop")).get(0).click();
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            String text = driver.findElements(By.cssSelector("span.cart-base-item-sum__subtitle")).get(0).getText();
                            String[] split = text.split(" ");
                            matchLive.setMaxBet(parseDouble(split[1]));
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                    }
                    break;
                }
            }

            String[] arr = new String[2];
            arr[0] = "Halves result";
            arr[1] = "Quarters result";

            driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
            for (int i = 0; i < arr.length; i++) {
                try {
                    WebElement quarterRes = driver.findElement(By.xpath("//span[text()='" + arr[i] + "']/../.."));
                    List<WebElement> elements2 = quarterRes.findElements(By.cssSelector("div.sub_title_dop__"));
                    String s;
                    for (WebElement el : elements2) {
                        s = el.getText();
                        System.out.println("Текст из сайта можно ставить на: " + s);
                        if (s.contains("1st half")) {
                            matchLive.getPossibleBetSelectionBet().add(SelectionBet.Half1st);
                        } else if (s.contains("2nd half")) {
                            matchLive.getPossibleBetSelectionBet().add(SelectionBet.Half2nd);
                        } else if (s.contains("4th")) {
                            matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q4);
                        } else if (s.contains("3rd")) {
                            matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q3);
                        } else if (s.contains("2nd")) {
                            matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q2);
                        } else if (s.contains("1st")) {
                            matchLive.getPossibleBetSelectionBet().add(SelectionBet.Q1);
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

            System.out.println("Можно ставить на: ");
            for (SelectionBet sel : matchLive.getPossibleBetSelectionBet()) {
                System.out.println("ставка на " + sel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void goBackToOverview(WebDriver driver) {
        try {
            log.info("Возвращаемся к списку матчей на сайте " + getNameOfSite());
            driver.findElement(By.cssSelector("a[href='/v3/en/live/']")).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.findElement(By.xpath("//a[text()='All']")).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WebElement carouselItems = driver.findElement(By.cssSelector("ul.carousel-items"));
            try {
                List<WebElement> carouselBlocks = carouselItems.findElements(By.cssSelector("li.carousel-block"));
                for (WebElement element : carouselBlocks) {
                    String text = element.findElement(By.cssSelector("label.text")).findElement(By.cssSelector("span.sport_name")).getText();
                    if (text.compareTo("Basketball") == 0) {
                        element.click();
                        Thread.sleep(500);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.info("Сейчас нет матчей.");
                System.out.println("Сейчас нет матчей");
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getMinBet(WebDriver driver) {

        double minBet = 0.0;

        try {
            String balance = driver.findElement(By.cssSelector("div.data_balance")).getText();
            log.info("Текущее состояние счета STRING " + balance);

            if (balance != null && balance.contains("rouble")) {
                minBet = 10.0;
            } else {
                minBet = 1.0;
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }

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
        try {
            System.out.println("Делаем срочное событие для матча " + matchLive.getNameOfMatch());
            log.info("Делаем срочное событие для матча " + matchLive.getNameOfMatch());
//            driver.findElement(By.cssSelector("span.logo-ico")).click();
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            log.info("Открываем список матчей LIVE");
//            driver.findElement(By.cssSelector("div.menu")).findElement(By.cssSelector("a[href='/v3/en/live/']")).click();
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            driver.findElement(By.cssSelector("ul.live-menu-ul")).findElement(By.xpath("//a[text()='Live-bets']")).click();
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            driver.findElement(By.cssSelector("ul.live-menu-ul")).findElement(By.xpath("//a[text()='Live-bets']")).click();
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            WebElement carouselItems = driver.findElement(By.cssSelector("ul.carousel-items"));
//            try {
//                List<WebElement> carouselBlocks = carouselItems.findElements(By.cssSelector("li.carousel-block"));
//                for (WebElement element : carouselBlocks) {
//                    String text = element.findElement(By.cssSelector("label.text")).findElement(By.cssSelector("span.sport_name")).getText();
//                    if (text.compareTo("Basketball") == 0) {
//                        element.click();
//                        System.out.println("Find link duaring scaning/// Click input check box");
//                        try {
//                            Thread.sleep(2000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        String basketLinkClassString = element.getAttribute("class");
//                        System.out.println("Ссылка на матчи по баскетболу активна - " + basketLinkClassString);
//                        if (basketLinkClassString.contains("active")) {
//                            isMatches = true;
//                        }
//                        break;
//                    }
//                }
//            } catch (NoSuchElementException e) {
//                e.printStackTrace();
//                log.info("Сейчас нет матчей.");
//                System.out.println("Сейчас нет матчей");
//            }
//
            List<WebElement> matches = driver.findElements(By.cssSelector("div.live-list__championship-event"));
            System.out.println("matches size is: " + matches.size());
            log.info("В группе найдено " + matches.size() + " матчей");

            for (int i = 0; i < matches.size(); i++) {

                WebElement matchElement;
                try {
                    matchElement = driver.findElements(By.cssSelector("div.live-list__championship-event")).get(i);
                } catch (Exception e) {
                    System.out.println("Next Match");
                    continue;
                }
                try {
                    if (matchElement != null && matchElement.isDisplayed()) {

                        String nameOfEmergencyMatch = matchLive.getNameOfMatch();

                        String nameOfMatch;

                        String firstTeamName = matchElement.findElement(By.cssSelector("span.name_ht")).getText();
                        String secondTeamName = matchElement.findElement(By.cssSelector("span.name_at")).getText();
                        if (firstTeamName == null || secondTeamName == null || firstTeamName.length() < 1 || secondTeamName.length() < 1) {
                            continue;
                        }

                        nameOfMatch = firstTeamName + " vs " + secondTeamName;//++++++++++++++++++++++++++++++++++
                        if (nameOfEmergencyMatch.contains(firstTeamName) && nameOfEmergencyMatch.contains(secondTeamName)) {

                            String lastPeriodsScore = null;
                            SelectionBet currentPeriod = null;
                            String timeString = null;
                            String timeToQuarterFinishString = "00:00";
                            boolean isPossibleToMakeBet = true;

                            try {
                                lastPeriodsScore = matchElement.findElement(By.cssSelector("span.pruning")).getText();
                            } catch (NoSuchElementException e) {
                                log.info("Возможно первый период");
                                System.out.println("First period.");
                            }

                            String currentPeriodScore;
                            String totalPeriodScore = null;
                            try {
                                currentPeriodScore = matchElement.findElement(By.cssSelector("span.last-pruning")).getText();
                            } catch (NoSuchElementException e) {
                                currentPeriodScore = null;
                                log.info("Первый период снова убедились");
                                System.out.println("First period again");
                            }

                            if (currentPeriodScore == null) {
                                totalPeriodScore = lastPeriodsScore;
                            } else {
                                totalPeriodScore = lastPeriodsScore + currentPeriodScore;
                            }
                            System.out.println("Total periods - " + totalPeriodScore);
                            if (totalPeriodScore == null) {
                                currentPeriod = SelectionBet.Q1;
                            } else if (!totalPeriodScore.contains("(") || !totalPeriodScore.contains(")")) {
                                currentPeriod = null;
                            } else {
                                String[] split = totalPeriodScore.split(",");
                                switch (split.length) {
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
                                System.out.println("total periods score is " + split.length);
                            }

                            try {
                                timeString = matchElement.findElement(By.cssSelector("span.black")).getAttribute("data-time");
                                System.out.println("first scan for time is: " + timeString);
                            } catch (NoSuchElementException e) {
                                e.printStackTrace();
                            }

                            if (timeString == null) {
                                timeToQuarterFinishString = "15:00";
                            } else {
                                timeToQuarterFinishString = timeString;
                            }

                            if (timeString == null && totalPeriodScore != null) {
                                timeString = "Break";
                            }

                            if (timeString != null) {
                                if (timeString.compareTo("Break") == 0) {
                                    System.out.println("Set next current period");
                                    if (currentPeriod == SelectionBet.Q1) {
                                        currentPeriod = SelectionBet.Q2;
                                    } else if (currentPeriod == SelectionBet.Q2) {
                                        currentPeriod = SelectionBet.Q3;
                                    } else if (currentPeriod == SelectionBet.Q3) {
                                        currentPeriod = SelectionBet.Q4;
                                    } else if (currentPeriod == SelectionBet.Q4) {
                                        currentPeriod = SelectionBet.OT;
                                    }
                                }
                            }

                            Date data = null;
                            dateFormat.applyPattern("mm:ss");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                            try {
                                data = dateFormat.parse(timeToQuarterFinishString);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                System.out.println("Can not resolve time to quarter finish....");
                            }

                            try {
                                matchElement.findElement(By.cssSelector("div.live-list__championship-event-scoreboard-message"));
                                isPossibleToMakeBet = false;
                                System.out.println("Dont possible to make bet for match " + nameOfMatch);
                            } catch (Exception e) {
                                System.out.println("Possible to make bet.");
                            }
                            if (isPossibleToMakeBet) {
                                try {
                                    matchElement.findElement(By.cssSelector("div.live-list__championship-event-dop"));
                                    isPossibleToMakeBet = true;
                                    System.out.println("Возможно делать ставку.");
                                } catch (Exception e) {
                                    System.out.println("Невозможно делать ставку.");
                                    isPossibleToMakeBet = false;
                                }
                            }

                            matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
                            matchLive.setTimeToQuarterFinish(data.getTime());
                            if (currentPeriod != null) {
                                matchLive.setCurrentPeriod(currentPeriod);
                            }

                            if (matchLive.isPossibleToMakeBet() && !matchLive.isBetIsDone()) {
                                System.out.println(" Making Event for match " + nameOfMatch);
                                matchLive.makeNextEvent(driver);
                                if (matchLive.isBetIsDone()) {
                                    emergencyMatches.remove(matchLive);
                                }
                            }
                        }
                    }
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                    log.info(e.getMessage());
                } catch (StaleElementReferenceException e) {
                    log.info(e.getMessage());
                    System.out.println("NEXT MATCH");
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info(e.getMessage());
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
    public void setAppender(Appender logAppender) {
        this.appender = appender;
        this.log = Logger.getLogger(ParserBetSBC.class);
        this.log.addAppender(logAppender);
    }

    @Override
    public Appender getAppender() {
        return appender;
    }
}
