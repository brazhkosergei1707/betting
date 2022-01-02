package betting;

import UI.startFrame.StartFrame;
import betting.UI.BettingUI;
import betting.bot.BetMaker;
import betting.entity.ChainBetting;
import betting.entity.MatchLiveBetting;
import betting.parserBetting.BettingParser;
import bot.chain.ChainHolder;
import betting.entity.Bet;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import betting.parserBetting.ParserFactory;
import service.DriverService;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Betting {
    private boolean work = true;
    private String path;
    private BettingParser parser;
    private double margin;
    private double minBet;
    private Map<Integer, BetMaker> betMakerMap;
    private Map<String, MatchLiveBetting> bettingMatchLive;
    private Map<Integer, Map<String, String>> betMakersData;

    private Thread scannerThread;
    private boolean allIsStarted;
    private ChainHolder chainHolder;
    private BettingParser scannerParser;
    private BettingUI bettingUI;

    public Betting(String path, BettingParser parser) {
        this.path = path;
        this.parser = parser;
        bettingMatchLive = new HashMap<>();
        betMakerMap = new HashMap<>();
        betMakersData = new HashMap<>();
        Thread updateThread = new Thread(() -> {
            while (work) {
                try {
                    deleteFinishedMatches();
                    updateUserInterface();
                    chainHolder.cleanCurrentChains();
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        scannerThread = new Thread(() -> {
            if (betMakersData.size() > 0) {
                scannerParser = ParserFactory.getParser(parser);
                String userName = null;
                String password = null;
                for (int i = 0; i < 5; i++) {
                    Map<String, String> map = betMakersData.get(i);
                    if (map != null) {
                        userName = map.get("username");
                        password = map.get("password");
                        if (userName != null && password != null) {
                            break;
                        }
                    }
                }

                WebDriver driver = restartScanner(scannerParser, userName, password, path);
                margin = StartFrame.getMargin();
                minBet = scannerParser.getMinBet(driver);
                scannerParser.updateCurrentMatchesLive(bettingMatchLive, driver, margin, minBet, this);

                chainHolder = ChainHolder.restoreChains(this);
                chainHolder.connectMatchesToChain(this);
                startBetMakers();
                updateThread.start();
                boolean wait = false;

                while (work) {
                    if (wait) {
                        logOut(driver);
                        for (Integer integer : betMakerMap.keySet()) {
                            BetMaker betMaker = betMakerMap.get(integer);
                            betMaker.waitBetMaker();
                        }

                        try {
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        driver = restartScanner(scannerParser, userName, password, path);
                        margin = StartFrame.getMargin();
                        minBet = scannerParser.getMinBet(driver);
                        scannerParser.updateCurrentMatchesLive(bettingMatchLive, driver, margin, minBet, this);
                    } else {
                        for (Integer integer : betMakerMap.keySet()) {
                            BetMaker betMaker = betMakerMap.get(integer);
                            if (betMaker != null && !betMaker.isStart()) {
                                betMaker.continueWork();
                                while (!betMaker.isStart()) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        margin = StartFrame.getMargin();
                        minBet = scannerParser.getMinBet(driver);
                        try {
                            scannerParser.lookingForMatches(driver, bettingMatchLive, margin, minBet, this);
                        } catch (NoSuchSessionException | NoSuchWindowException e) {
                            e.printStackTrace();
                            driver = restartScanner(scannerParser, userName, password, path);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    wait = bettingMatchLive.size() == 0;
                }
                logOut(driver);
            }
        });
    }

    private WebDriver restartScanner(BettingParser parser, String username, String password, String driverPath) {
        WebDriver driver = DriverService.getInstance().getWebDriver(driverPath);
        try {
            boolean logIn = parser.logIn(username, password, driver);
            if (!logIn) {
                driver.quit();
                Thread.sleep(5000);
                restartScanner(parser, username, password, driverPath);
            }
        } catch (Exception e) {
            driver.quit();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            restartScanner(parser, username, password, driverPath);
            e.printStackTrace();
        }
        return driver;
    }

    public void start(BettingUI bettingUI) {
        for (Integer integer : betMakersData.keySet()) {
            Map<String, String> map = betMakersData.get(integer);
            String userName = map.get("username");
            String password = map.get("password");
            String start = map.get("start");

            if (Boolean.valueOf(start)) {
                if (userName != null
                        && password != null
                        && userName.length() > 2
                        && password.length() > 2) {
                    BettingParser betMakerParser = ParserFactory.getParser(parser);
                    BetMaker betMaker = new BetMaker(betMakerParser, path, userName, password, this);
                    betMakerMap.put(integer, betMaker);
                }
            }
        }

        this.bettingUI = bettingUI;
        bettingUI.setBetting(this);
        scannerThread.start();
    }

    public void stopBetting() {
        for (Integer integer : betMakerMap.keySet()) {
            BetMaker betMaker = betMakerMap.get(integer);
            betMaker.stopBetMaker(integer);
        }
        work = false;
    }

    public void reCheckBetMakers() {
        for (Integer integer : betMakersData.keySet()) {
            Map<String, String> map = betMakersData.get(integer);
            BetMaker betMaker = betMakerMap.get(integer);

            String userName = map.get("username");
            String password = map.get("password");
            String start = map.get("start");

//            if(betMaker.isWork()){
//                betMaker.getUserName();
//                betMaker.setWork(Boolean.valueOf(start));
//            } else {
//
//            }

            if (Boolean.valueOf(start)) {
                if (userName.length() > 2 && password.length() > 2) {
                    if (betMaker == null) {
                        BettingParser betMakerParser = ParserFactory.getParser(parser);
                        betMaker = new BetMaker(betMakerParser, path, userName, password, this);
                        betMakerMap.put(integer, betMaker);
                    } else {
                        if (betMaker.getUserName().compareTo(userName) != 0) {
                            betMaker.stopBetMaker(integer);
                            betMaker = new BetMaker(ParserFactory.getParser(parser), path, userName, password, this);
                            betMakerMap.put(integer, betMaker);
                        }
                    }
                } else {
                    if (betMaker != null) {
                        betMaker.stopBetMaker(integer);
                        betMakerMap.put(integer, null);
                    }
                }
            } else {
                if (betMaker != null) {
                    betMaker.stopBetMaker(integer);
                    betMakerMap.put(integer, null);
                }
            }
        }
        startBetMakers();
    }

    public void logOut(WebDriver driver) {
        scannerParser.logOut(driver);
        driver.quit();
    }

    private void startBetMakers() {
        for (Integer integer : betMakerMap.keySet()) {
            BetMaker betMaker = betMakerMap.get(integer);
            if (betMaker != null && !betMaker.isAlive()) {
                betMaker.start();
                while (!betMaker.isStart()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                StartFrame.getInstance().getAllDataPanel().setAccountPaneColor(parser, integer, new Color(168, 218, 139));
            }
        }
        allIsStarted = true;
    }

    public ChainBetting getChain(double maxBet, BetMaker betMaker) {
        ChainBetting chainToReturn = null;
        for (ChainBetting chain : chainHolder.getCurrentChainList()) {
            if (chain.getMatchLiveName() == null) {
                double lastBetPriceInMatch = chain.getLastBetPriceInMatch(minBet);
                if (lastBetPriceInMatch < maxBet) {
                    System.out.println("last price of bet to resolve this chain" + lastBetPriceInMatch + ". Max bet is: " + maxBet);

                    double totalPriceOfBetsInMatch = chain.getTotalPriceOfBetsInMatch(minBet);
                    if (totalPriceOfBetsInMatch < betMaker.getStorage().getCurrentBalance()) {
                        chainToReturn = chain;
                        break;
                    } else {
                        String s = "Сумма необоходимая для продолжения цепочки номер " + chain.getIdChain() + ", равна " + totalPriceOfBetsInMatch;
                    }
                }
            }

        }
        return chainToReturn;
    }

    public Map<Integer, Map<String, String>> getBetMakersData() {
        return betMakersData;
    }

    private void deleteFinishedMatches() {
        String matchInt = null;
        for (String name : bettingMatchLive.keySet()) {
            if (bettingMatchLive.get(name).isDeleteThisMatch()) {
                System.out.println("Матч " + bettingMatchLive.get(name).getNameOfMatch() + " будет удален с программы");
                matchInt = name;
                break;
            }
        }
        if (matchInt != null) {
            bettingMatchLive.remove(matchInt);
            deleteFinishedMatches();
        }
    }

    public void addChain(ChainBetting chain) {
        chainHolder.addChain(chain);
    }

    public ChainHolder getChainHolder() {
        return chainHolder;
    }

    public String getNameOfSite() {
        return scannerParser.getNameOfSite();
    }

    public List<MatchLiveBetting> getMatchesList() {
        List<MatchLiveBetting> list = new ArrayList<>();

        for (String na : bettingMatchLive.keySet()) {
            list.add(bettingMatchLive.get(na));
        }

        return list;
    }

    public List<Bet> getCurrentBetList() {
        List<Bet> list = new ArrayList<>();
        for (Integer integer : betMakerMap.keySet()) {
            BetMaker betMaker = betMakerMap.get(integer);
            if (betMaker != null) {
                list.addAll(betMaker.getStorage().getCurrentBets());
            }
        }
        return list;
    }

    public List<ChainBetting> getChainList() {
        return chainHolder.getCurrentChainList();
    }

    private void updateUserInterface() {
        bettingUI.updatePanel();
    }

    public BettingParser getParser() {
        return parser;
    }

    public boolean isAllIsStarted() {
        return allIsStarted;
    }

    public MatchLiveBetting getMatchLive(Bet bet) {
        MatchLiveBetting matchLive = null;
        for (String name : bettingMatchLive.keySet()) {
            if (name.compareTo(bet.getMatchName()) == 0) {
                matchLive = bettingMatchLive.get(name);
            }
        }

        return matchLive;
    }

    public Map<Integer, BetMaker> getBetMakerMap() {
        return betMakerMap;
    }

    public Map<String, MatchLiveBetting> getBettingMatchLive() {
        return bettingMatchLive;
    }

    public int getMadeBetCount() {
        int count = 0;
        for (Integer integer : betMakerMap.keySet()) {
            BetMaker betMaker = betMakerMap.get(integer);
            count += betMaker.getCountMadeBet();
        }
        return count;
    }

    public int getCheckBetCount() {
        int count = 0;
        for (Integer integer : betMakerMap.keySet()) {
            BetMaker betMaker = betMakerMap.get(integer);
            count += betMaker.getCountCheckBet();
        }
        return count;
    }

    public int getMatchesCount() {
        return bettingMatchLive.size();
    }
}
