package parser.betFair;

import UI.startFrame.StartFrame;
import betting.entity.Bet;
import entityes.MatchLive;
import betting.enumeration.SelectionBet;
import org.apache.log4j.Appender;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import parser.Parser;

import java.util.List;

public class ParserBetFair implements Parser {
    private int switcherId;
    private boolean isActiveHistory = false;
    private boolean isActiveDetails = false;

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
            driver.findElement(By.cssSelector("input.ssc-lis")).click();
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
    public void setCurrentTimeAndCount(WebDriver driver) {
//        if (StartFrame.getSwitcher(this,switcherId) != null) {
//            StartFrame.getSwitcher(this,switcherId).getStorage().setCurrentTime(System.currentTimeMillis());
//        }
//        try {
//            String balanceString = driver.findElement(By.cssSelector("td[rel='main']")).getText();//€10.01
//            if (StartFrame.getSwitcher(this,switcherId) != null) {
//                StartFrame.getSwitcher(this,switcherId).getStorage().setCurrentBalance(parseDouble(balanceString));
//                StartFrame.getSwitcher(this,switcherId).getStorage().setCurrentBalanceString(balanceString);
//            }
//
//        } catch (NoSuchElementException e) {
//            e.printStackTrace();
//        }
    }

    private double parseDouble(String balance) {
        double doubleBalance = 0.0;
        String correctString = "";
        if (balance.contains("€")) {
            correctString = balance.substring(1, balance.length() - 1);
        }
        doubleBalance = Double.parseDouble(correctString);
        return doubleBalance;
    }


    @Override
    public void updateHistoryBet(WebDriver driver) {

    }

    @Override
    public void updateCurrentMatchesLive(WebDriver driver) {
        try {
            driver.get("https://www.betfair.com/sport/basketball");
            Thread.sleep(1000);
            List<WebElement> elements = driver.findElements(By.cssSelector("li.section"));
            String text;
            for (WebElement element : elements) {
                text = element.findElement(By.cssSelector("span.section-header-label")).getText();
                if (text != null) {
                    if (text.contains("In-Play")) {
                        List<WebElement> matchesElements = element.findElements(By.cssSelector("li.com-coupon-line-new-layout"));

                        String nameOfMatch = null;
                        SelectionBet currentPeriod = null;
                        Double maxBet = null;
                        boolean isPossibleToMakeBet = true;
                        WebElement matchElement = null;
                        for (int i = 0; i < matchesElements.size(); i++) {
                            for (WebElement element1 : elements) {
                                text = element1.findElement(By.cssSelector("span.section-header-label")).getText();
                                if (text != null) {
                                    if (text.contains("In-Play")) {
                                        matchElement = element.findElements(By.cssSelector("li.com-coupon-line-new-layout")).get(i);
                                    }
                                }
                            }

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
                                        currentPeriod = SelectionBet.Q4;//////////////////
                                        break;
                                }
                            }

                            try {
                                element.findElement(By.cssSelector("div.market-2-runners")).findElement(By.cssSelector("span.status-text"));
                                isPossibleToMakeBet = false;
                            } catch (Exception e) {
                                isPossibleToMakeBet = true;
                            }

                            String maxBetString = null;
                            if (isPossibleToMakeBet) {
                                teamNamesElements.get(0).click();
                                Thread.sleep(1000);
                                WebElement element1 = driver.findElement(By.cssSelector("li[title='Odd or Even Total Points']"));
                                element1.findElement(By.cssSelector("a.com-bet-button")).click();
                                WebElement betField = driver.findElement(By.cssSelector("input.ui-market-action"));
                                betField.click();
                                Thread.sleep(1000);
                                betField.sendKeys("9999999");
                                driver.findElement(By.cssSelector("div.ui-runner-price")).click();
                                Thread.sleep(1000);
                                maxBetString = driver.findElement(By.cssSelector("span.set-max-stake")).getText();

                                driver.findElement(By.cssSelector("a.remove-all-bets")).click();
                                Thread.sleep(1000);
                                goBackToOverview(driver);
                                Thread.sleep(1000);
                            }

                            System.out.println("Максимальная ставка равна - " + maxBetString);
                            System.out.println(nameOfMatch);
                            System.out.println(currentPeriod);
                            System.out.println(isPossibleToMakeBet);
                            System.out.println("=========================================");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateCurrentBet(WebDriver driver) {

    }

    @Override
    public String getNameOfSite() {
        return "https://www.betfair.com/";
    }


    @Override
    public String toString() {
        return "www.betfair.com";
    }

    @Override
    public void scanMatches(WebDriver driver) {

    }

    @Override
    public boolean clickToMatch(WebDriver driver, String nameOfMatch) {
        return false;
    }

    @Override
    public Boolean betAlreadyNotPlayed(Bet lastBet, WebDriver driver) {
        return false;
    }

    @Override
    public void checkGameScore(WebDriver driver, MatchLive matchLive) {

    }

    @Override
    public Bet checkBet(Bet bet, WebDriver driver) {
        return null;
    }

    @Override
    public void checkMaximumBetSizeAndPossibleBetSelection(WebDriver driver, MatchLive matchLive) {

    }

    @Override
    public Bet makeBet(Bet bet, WebDriver driver, MatchLive matchLive) {
        return null;
    }





    @Override
    public void goBackToOverview(WebDriver driver) {
        try {
            driver.get("https://www.betfair.com/sport/basketball");
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
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
//        if (!emergencyMatches.contains(matchLive)) {
//            emergencyMatches.add(matchLive);
//        }
//        haveEmergencyMatches = true;
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

    }

    @Override
    public Appender getAppender() {
        return null;
    }
}
