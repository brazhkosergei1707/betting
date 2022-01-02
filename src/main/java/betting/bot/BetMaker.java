package betting.bot;

import UI.startFrame.StartFrame;
import betting.Betting;
import betting.entity.MatchLiveBetting;
import betting.entity.StorageBetting;
import betting.parserBetting.BettingParser;
import betting.entity.Bet;
import org.openqa.selenium.WebDriver;
import service.DriverService;

import java.awt.*;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BetMaker extends Thread {
    private boolean work = true;

    private String userName;
    private String password;
    private String path;

    private Betting betting;
    private WebDriver driver;
    private Deque<Bet> makeBetDeque;
    private Deque<Bet> checkBetDeque;
    private StorageBetting storage;
    private List<MatchLiveBetting> mainMatches;

    private BettingParser parser;

    private boolean start;

    public BetMaker(BettingParser parser, String path, String userName, String password, Betting betting) {
        this.betting = betting;
        this.path = path;
        this.parser = parser;
        this.userName = userName;
        this.password = password;
        storage = new StorageBetting(betting, userName, this);
        StartFrame.addStorage(parser, storage);
        makeBetDeque = new ConcurrentLinkedDeque<>();
        checkBetDeque = new ConcurrentLinkedDeque<>();
        mainMatches = new ArrayList<>();
    }

    @Override
    public void run() {
        startBetMaker();
//        parser.updateHistoryBet(driver, storage.getHistoryBets());
//        parser.updateCurrentBet(driver, storage.getCurrentBets());
//        storage.checkBatsInStorage();

        int updateCurrentCount = 0;
        while (work) {
            if (start) {
                try {
                    if (makeBetDeque.size() > 0 || checkBetDeque.size() > 0) {
                        if (makeBetDeque.size() > 0) {
                            int size = makeBetDeque.size();
                            for (int i = 0; i < size; i++) {
                                Bet bet = makeBetDeque.getLast();
                                MatchLiveBetting matchLive = betting.getMatchLive(bet);
                                System.out.println("Имя матча, для которого СТАВИМ ставку - " +matchLive.getNameOfMatch());
                                parser.makeBet(bet, driver, matchLive);
                                if (bet.getTimeBet() > -7200000L) {
                                    if (mainMatches.contains(matchLive)) {
                                        matchLive.addBetToMatchStatistic(bet);
                                    } else {
                                        storage.addBetToStorage(bet);
                                        matchLive.addBetToMultiStatistic(this);
                                    }
                                } else {
                                    matchLive.setEventMade(false);
                                }
                                makeBetDeque.pollLast();
                                parser.setCurrentBalance(driver);
                            }
                        }

                        if (checkBetDeque.size() > 0) {
                            int size = checkBetDeque.size();
                            for (int i = 0; i < size; i++) {
                                Bet bet = checkBetDeque.getLast();
                                MatchLiveBetting matchLive = betting.getMatchLive(bet);
                                System.out.println("Имя матча, для которого ПРОВЕРЯЕМ ставку - " +matchLive.getNameOfMatch());
                                parser.checkBet(bet, driver);
                                if (mainMatches.contains(matchLive)) {
                                    if (bet.getWin() != null) {
                                        matchLive.addBetToMatchStatistic(bet);
                                        mainMatches.remove(matchLive);
                                    } else {
                                        bet.increaseCountCheck();
                                        if (bet.getCountCheck() > 5) {
                                            bet.setWin(false);
                                            matchLive.addBetToMatchStatistic(bet);
                                            mainMatches.remove(matchLive);
                                        } else {
                                            matchLive.setEventMade(false);
                                        }
                                    }
                                    matchLive.setEventMade(false);
                                }
                                checkBetDeque.pollLast();
                            }
                        }
                    } else {
                        if (updateCurrentCount == 10) {
                            parser.setCurrentBalance(driver);
                            updateCurrentCount = 0;
                        } else {
                            storage.checkToOldBets();
                            updateCurrentCount++;
                        }
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    startBetMaker();
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String toString() {
        return userName;
    }

    private void startBetMaker() {
        try {
            if (driver != null) {
                driver.quit();
            }
            Thread.sleep(2000);
            driver = DriverService.getInstance().getWebDriver(path);
            boolean logIn = parser.logIn(userName, password, driver);
            if (!logIn) {
                startBetMaker();
                System.out.println("Заходим заново");
            }
        } catch (Exception e) {
            startBetMaker();
        }
        start = true;
    }

    public void continueWork() {
        try {
            if (driver != null) {
                driver.quit();
            }
            Thread.sleep(1000);
            driver = DriverService.getInstance().getWebDriver(path);
            boolean logIn = parser.logIn(userName, password, driver);
            if (!logIn) {
                continueWork();
            }
        } catch (Exception e) {
            continueWork();
        }
        start = true;
    }

    public void waitBetMaker() {
        try {
            parser.logOut(driver);
            if (driver != null) {
                driver.quit();
                driver = null;
            }
            start = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopBetMaker(int integer) {
        for (MatchLiveBetting matchLiveBetting : mainMatches) {
//            matchLiveBetting.TODO Обработать вариант когда перезапускаем биржу.
        }

        StartFrame.getInstance().getAllDataPanel().setAccountPaneColor(betting.getParser(), integer, Color.LIGHT_GRAY);
        work = false;
        parser.logOut(driver);
        driver.quit();
    }

    public boolean isWork() {
        return work;
    }

    public void setWork(boolean work) {
        this.work = work;
    }

    public int getCountMadeBet() {
        return makeBetDeque.size();
    }

    public int getCountCheckBet() {
        return checkBetDeque.size();
    }

    public void addBet(Bet bet) {
        if (bet.getTimeBet() > -7200000L) {
            boolean oldBet = false;

            for (Bet betFromStorage : checkBetDeque) {
                if (betFromStorage.getMatchName().compareTo(bet.getMatchName()) == 0) {
                    if (betFromStorage.getSelectionBet() == bet.getSelectionBet()) {
                        oldBet = true;
                        break;
                    }
                }
            }

            if (!oldBet) {
                checkBetDeque.addFirst(bet);
            }
        } else {
            boolean oldBet = false;
            for (Bet betFromStorage : makeBetDeque) {
                if (betFromStorage.getMatchName().compareTo(bet.getMatchName()) == 0) {
                    if (betFromStorage.getSelectionBet() == bet.getSelectionBet()) {
                        oldBet = true;
                        break;
                    }
                }
            }

            if (!oldBet) {
                makeBetDeque.addFirst(bet);
            }
        }
    }

    public String getUserName() {
        return userName;
    }

    public StorageBetting getStorage() {
        return storage;
    }

    public void setMain(MatchLiveBetting bettingMatchLive) {
        mainMatches.add(bettingMatchLive);
        bettingMatchLive.setMainBetMaker(this);
    }

    public boolean isStart() {
        return start;
    }

    public boolean isMain(MatchLiveBetting bettingMatchLive) {
        boolean isMain = false;
        for (MatchLiveBetting bettingMatchLive1 : mainMatches) {
            if (bettingMatchLive.getNameOfMatch().contains(bettingMatchLive1.getNameOfMatch())) {
                isMain = true;
                break;
            }
        }
        return isMain;
    }
}

