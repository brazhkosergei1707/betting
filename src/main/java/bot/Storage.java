package bot;

import betting.entity.Bet;
import bot.chain.ChainHolder;
import entityes.*;
import betting.enumeration.SelectionBet;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import parser.Parser;

import java.util.*;
import java.util.List;

public class Storage {

    //    private static Logger log = Logger.getLogger(Storage.class);
    private Logger log;

    private Parser parser;

    private double margin;
    private double minBet;
    private long currentTime;
    private String currentMoneyCountString = "0";
    private double currentMoneyCount = 0.0;

    private Map<Integer, MatchLive> currentMatches;
    private List<MatchLive> currentMatchesList;

    private ChainHolder chainHolder;
    private static List<Bet> allBets;
    private List<Bet> currentBets;
    private List<Bet> historyBets;

    static {
        allBets = new ArrayList<>();
    }

    private String nameOfUser;

    public Storage(String nameOfUser, Parser parser) {
        log = Logger.getLogger(Storage.class);
        log.addAppender(parser.getAppender());


        this.parser = parser;
        this.nameOfUser = nameOfUser;
        currentMatches = new HashMap<>();
        currentMatchesList = new ArrayList<>();
//        chainHolder = ChainHolder.restoreChains(parser);
        currentBets = new ArrayList<>();
        historyBets = new ArrayList<>();
//        TEST();
    }

    public void cleanStorage() {

        for (Integer integer : currentMatches.keySet()) {
            MatchLive matchLive = currentMatches.get(integer);
            MatchGroup matchGroup = matchLive.getMatchGroup();
//            if (matchGroup.getCopies().size() > 1) {//Ищем матч в группе которого копий больше чем одна, и удаляем этот матч из группы
//                matchGroup.getCopies().remove(parser);
//            }
        }

        currentMatches.clear();
        currentMatchesList.clear();
        currentBets.clear();
        historyBets.clear();
        updateInterface();
    }

    void TEST() {
        for (int i = 0; i < 5; i++) {
            Bet bet = new Bet();
            bet.setMatchName(parser.getNameOfSite() + " bet " + i);
            System.out.println(parser.getNameOfSite() + " bet " + i);
            allBets.add(bet);
        }
    }

//    public void updateCurrentTimeAndBalance() {
//        StartFrame.getInstance().updateCurrentBalance(currentMoneyCountString, parser);
//    }

    public void updateInterface() {
        System.out.println("Удаляем матчи на сайте " + parser.getNameOfSite());
        log.info("Удаляем матчи на сайте " + parser.getNameOfSite());
        deleteFinishedMatches();
        System.out.println("Проверяем зависшие ставки на сайте " + parser.getNameOfSite());
        log.info("Проверяем зависшие ставки на сайте " + parser.getNameOfSite());
        checkToOldBets();
        if (currentMatches.size() != currentMatchesList.size()) {
            currentMatchesList.clear();
            for (Integer integer : currentMatches.keySet()) {
                currentMatchesList.add(currentMatches.get(integer));
            }
        }
        log.info("Обновляем интерфейс программы на сайте " + parser.getNameOfSite());
//        StartFrame.updateInterface(parser);
    }

    public void deleteFinishedMatches() {
        int matchInt = 0;
        for (Integer integer : currentMatches.keySet()) {
            if (currentMatches.get(integer).isDeleteThisMatch()) {
                System.out.println("Матч " + currentMatches.get(integer).getNameOfMatch() + " будет удален с программы");
                log.info("Матч " + currentMatches.get(integer).getNameOfMatch() + " будет удален с программы");
                matchInt = integer;
                break;
            }
        }
        if (matchInt != 0) {
            currentMatches.remove(matchInt);
            deleteFinishedMatches();
        }
    }

//    =====================================================================================
//    ================================BETS=================================================
//    =====================================================================================

    public void addBetToStorage(Bet bet) {

        if (bet.getWin() == null) {
            boolean newCurrentBet = true;
            for (Bet currentBet : currentBets) {
                if (currentBet.getMatchName().contains(bet.getMatchName().split(" vs ")[0])) {
                    if (currentBet.getSelectionBet() == bet.getSelectionBet()) {
                        if (currentBet.getOddEven() == bet.getOddEven()) {
//                            currentBet = bet;
                            newCurrentBet = false;
                            break;
                        }
                    }
                }
            }
            if (newCurrentBet) {
                currentBets.add(bet);
            }
        } else {
            boolean newHistoryBet = true;
            for (Bet historyBet : historyBets) {
                if (historyBet.getMatchName().contains(bet.getMatchName().split(" vs ")[0])) {
                    if (historyBet.getSelectionBet() == bet.getSelectionBet()) {
                        if (historyBet.getOddEven() == bet.getOddEven()) {
//                            historyBet = bet;
                            newHistoryBet = false;
                            break;
                        }
                    }
                }
            }

            if (newHistoryBet) {
                historyBets.add(bet);
            }
        }

        addBetToCommonList(bet);
        cleanCurrentBetList();
    }


    private void addBetToCommonList(Bet bet) {
        if (bet.getPrice() > 0) {
            if (bet.getWin() == null) {
                if (!allBets.contains(bet)) {
                    System.out.println("Добавили ставку в общий лист.");
                    allBets.add(bet);
                    System.out.println("Размер листа равен: " + allBets.size());
                }
            }
        }

        int oldSize = allBets.size();
        allBets.removeIf(e -> e.getWin() != null);
        int newSize = allBets.size();
        if (oldSize != newSize) {
        }
    }

    void checkBatsInStorage() {
        log.info("Проверяем историю ставок.");
        try {
            System.out.println("Проверяем ставки на сайте " + parser.getNameOfSite());

            for (int i=historyBets.size()-1;i>=0;i--) {
                Bet bet = historyBets.get(i);
                if (bet.getMatchName().contains("@")) {
                    System.out.println("Change name...");
                    String[] split = bet.getMatchName().split(" @ ");
                    bet.setMatchName(split[0] + " vs " + split[1]);
                }

                if (currentMatches.containsKey(bet.getMatchName().hashCode())) {
                    System.out.println("Добавляем ставку в статистику матча " + bet.getMatchName());
                    log.info("Добавляем ставку в статистику матча " + bet.getMatchName());
                    MatchLive matchLive = currentMatches.get(bet.getMatchName().hashCode());
                    matchLive.addBetToMatchStatistic(bet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < currentBets.size(); i++) {
                Bet bet = currentBets.get(i);
                if (bet.isPlay()) {
                    bet.setPlay(false);
                }

                // use @ in match name of bet not  vs
                if (bet.getMatchName().contains("@")) {
                    String[] split = bet.getMatchName().split(" @ ");
                    bet.setMatchName(split[0] + " vs " + split[1]);
                }

                if (currentMatches.containsKey(bet.getMatchName().hashCode())) {
                    MatchLive matchLive = currentMatches.get(bet.getMatchName().hashCode());
                    bet.setMatchPeriod(matchLive.getCurrentPeriodInt());
                    bet.setTimeBet(currentTime + 10000L);
                    matchLive.addBetToMatchStatistic(bet);
                    System.out.println("Ставка " + bet.getSelectionBet() + " чет/нечет - " + bet.getOddEven() + ", добавлена в матч - " + matchLive.getNameOfMatch());
                    log.info("Ставка " + bet.getSelectionBet() + " чет/нечет - " + bet.getOddEven() + ", добавлена в матч - " + matchLive.getNameOfMatch());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkToOldBets() {
        out:
        for (Bet bet : currentBets) {

            for (Bet bet1 : historyBets) {
                if (bet.getMatchName().contains(bet1.getMatchName())) {
                    if (bet.getSelectionBet() == bet1.getSelectionBet()) {
                        if (bet.getOddEven() == bet1.getOddEven()) {
                            bet = bet1;
                            continue out;
                        }
                    }
                }
            }

            String matchName = bet.getMatchName();
            boolean toOldBet = true;
            if (currentMatches.containsKey(matchName.hashCode())) {
                toOldBet = false;
            }

            if (toOldBet) {
                boolean otherIsWin = false;
                boolean find = false;
                for (Bet bet1 : historyBets) {
                    if (bet.getMatchName().contains(bet1.getMatchName())) {
                        if (bet.getSelectionBet() == bet1.getSelectionBet()) {
                            if (bet.getOddEven() == bet1.getOddEven()) {
                                otherIsWin = bet1.getWin();
                                bet = bet1;
                                find = true;
                            }
                        }
                    }
                }
                bet.setWin(otherIsWin);
                if (!find) {
                    historyBets.add(bet);
                }
            }
        }
        cleanCurrentBetList();
    }

    private void cleanCurrentBetList() {
        System.out.println("Старый размер листа " + currentBets.size());
        currentBets.removeIf(e -> e.getWin() != null);
        System.out.println("Новый размер листа " + currentBets.size());
    }

    public List<Bet> getCurrentBets() {
        return currentBets;
    }

    public List<Bet> getHistoryBets() {
        return historyBets;
    }

//    =====================================================================================
//    ===============================CHAINS================================================
//    =====================================================================================

    public Chain getChain(double maxBet) {
        Chain chainToReturn = null;
//        for (Chain chain : chainHolder.getCurrentChainList()) {
//            if (chain.getMatchLiveName() == null) {
//                log.info("Посл. ставка цепочки " + chain.getIdChain() + " - " + chain.getLastBetPriceInMatch(parser, minBet) + ". Максимальная ставка матча " + maxBet);
//                if (chain.getLastBetPriceInMatch(parser, minBet) < maxBet) {
//                    System.out.println("last price of bet to resolve this chain" + chain.getLastBetPriceInMatch(parser, minBet) + ". Max bet is: " + maxBet);
//                    log.info("Всего нужно для цепочки " + chain.getIdChain() + " - " + chain.getTotalPriceOfBetsInMatch(parser, minBet) + ". На счету " + currentMoneyCount);
//                    if (chain.getTotalPriceOfBetsInMatch(parser, minBet) < currentMoneyCount) {
//                        log.info("Прикрепляем цепочку " + chain.getIdChain() + " к матчу");
//                        System.out.println("Total price of bets is" + chain.getTotalPriceOfBetsInMatch(parser, minBet) + ". Current money count " + currentMoneyCount);
//                        chainToReturn = chain;
//                        break;
//                    } else {
//                        String s = "Сумма необоходимая для продолжения цепочки номер " + chain.getIdChain() + ", равна " + chain.getTotalPriceOfBetsInMatch(parser, minBet) + ", на данный момент на счету " + getCurrentBalance();
//                        log.info(s);
//                        System.out.println(s);
//                        if (Sender.getSender() != null) {
//                            if (Sender.getSender().isSwitchOn()) {
//                                Sender.getSender().send("Не достаточно денег для продолжения цепочки", s);
//                            }
//                        }
//                    }
//                }
//            }
//        }
        if (chainToReturn != null) {
            log.info("Возвращаем цепочку " + chainToReturn.getIdChain());
        } else {
            log.info("Возвращаем NULL, так как нет подходящих цепочек.");
        }
        return chainToReturn;
    }

    public void addChain(Chain chain) {
//        chainHolder.addChain(chain);
    }

    public void rebuildChainsHistory(WebDriver driver) {
//        for (Chain chain : chainHolder.getCurrentChainList()) {
//            log.info("Восстанавливаем цепочку ставок " + chain.getIdChain());
//            Bet lastBet = chain.getLastBet();
//            if (lastBet.getWin() != null) {
//
//                boolean find = false;
//                for (Integer integer : currentMatches.keySet()) {
//                    MatchLive matchLive = currentMatches.get(integer);
//                    if (matchLive.getNameOfMatch().contains(lastBet.getMatchName())) {
//                        matchLive.setChain(chain);
//                        find = true;
//                        break;
//                    }
//                }
//
//                if (!find) {
//                    if (lastBet.getWin()) {
//                        log.info("Последняя ставка этой цепочки выиграла.");
//                        chain.setWin(true, parser);
//                    } else {
//                        log.info("Последняя ставка этой цепочки проиграла");
//                        chain.setMatchLiveName(null, parser);
//                    }
//                }

//            } else {
//                boolean find = false;
//                for (Bet bet : currentBets) {
//                    if (bet.getMatchName().compareTo(lastBet.getMatchName()) == 0) {
//                        if (bet.getSelectionBet() == lastBet.getSelectionBet()) {
//                            lastBet = bet;
//                            log.info("Цепочка номер " + chain.getIdChain() + " прикреплена к матчу " + bet.getMatchName());
//                            currentMatches.get(bet.getMatchName().hashCode()).setChain(chain);
//                            find = true;
//                            break;
//                        }
//                    }
//                }
//
//                for (Integer integer : currentMatches.keySet()) {
//                    MatchLive matchLive = currentMatches.get(integer);
//                    if (matchLive.getNameOfMatch().contains(lastBet.getMatchName())) {
//                        matchLive.setChain(chain);
//                        find = true;
//                        break;
//                    }
//                }
//
//                if (!find) {
//                    log.info("Последняя ставка не из текущих матчей. Проверяем последнюю ставку.");
//                    parser.checkBet(lastBet, driver);
//                    if (lastBet.getWin() != null) {
//                        if (lastBet.getWin()) {
//                            log.info("Последняя ставка выиграла.");
//                            chain.setWin(true, parser);
//                        } else {
//                            log.info("Последняя ставка проиграла.");
//                            chain.setMatchLiveName(null, parser);
//                        }
//                    } else {
//                        log.info("Неудача во время проверки последней ставки, поэтому считаем что последняя ставка проиграла.");
//                        lastBet.setWin(false);
//                        chain.setMatchLiveName(null, parser);
//                    }
//                }
//            }
//        }
    }

    public ChainHolder getChainHolder() {
        return chainHolder;
    }

    public List<Chain> getChainsInWork() {
//        return chainHolder.getCurrentChainList();
        return  null;
    }

    public List<Chain> getChainsHistory() {
//        return chainHolder.getHistoryChainList();
        return null;
    }

//    =====================================================================================
//    =============================MATCHES=================================================
//    =====================================================================================

    public MatchLive addMatchLive(String nameOfMatch, boolean isPossibleToMakeBet, long timeToQuarterFinish, SelectionBet currentPeriod, Double maxBet) {

        log.info("Создаем матч - " + nameOfMatch + ", на сайте - " + parser.getNameOfSite() + ". Период матча - " + currentPeriod);
        System.out.println("Создаем матч - " + nameOfMatch + ", на сайте - " + parser.getNameOfSite() + ". Период матча - " + currentPeriod);
        System.out.println("Матч создан, маржа равна " + margin);
        MatchLive matchLive = new MatchLive(nameOfMatch, margin, parser, minBet);
        matchLive.setPossibleToMakeBet(isPossibleToMakeBet);
        matchLive.setTimeToQuarterFinish(timeToQuarterFinish);
        matchLive.setCurrentPeriod(currentPeriod);
        if (maxBet != null) {
            System.out.println("Максимальная ставка для матча равна в хранилище " + maxBet);
            matchLive.setMaxBet(maxBet);
        }

        MatchesScanner.getMatchesScanner().checkMatch(matchLive, parser);
        currentMatches.put(matchLive.getNameOfMatch().hashCode(), matchLive);

        if (currentMatches.size() != currentMatchesList.size()) {
            currentMatchesList.clear();
            for (Integer integer : currentMatches.keySet()) {
                currentMatchesList.add(currentMatches.get(integer));
            }
        }

        return matchLive;
    }

    public Map<Integer, MatchLive> getCurrentMatches() {
        return currentMatches;
    }

    public List<MatchLive> getCurrentMatchesList() {
        return currentMatchesList;
    }

    //    =====================================================================================
//    =====================================================================================
//    =====================================================================================

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }

    public double getCurrentBalance() {
        return currentMoneyCount;
    }

    public double getMinBet() {
        return minBet;
    }

    public void setMinBet(double minBet) {
        this.minBet = minBet;
    }

    public void setCurrentBalanceString(String currentBalanceString) {
        if (currentMoneyCountString.compareTo(currentBalanceString) != 0) {
            currentMoneyCountString = currentBalanceString;
            System.out.println("Current money count text is: " + currentMoneyCountString);
        }
//        currentMoneyCountString = "10000.0";
    }

    public void setCurrentBalance(Double balance) {
        log.info("Баланс установлен на сайте " + parser.getNameOfSite());
        System.out.println("Баланс установлен на сайте " + parser.getNameOfSite());

        if (balance != null) {
            this.currentMoneyCount = balance;
        }

        double totalBalance = 0.0;

//        for (SwitcherMain switcherMain : StartFrame.switcherMainList) {
//            totalBalance += switcherMain.getStorage().getCurrentBalance();
//        }
//
//        try {
//            StartFrame.totalBalanceDoubleLabel.setText(String.valueOf(Math.round(totalBalance * 100.0) / 100.0));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        currentMoneyCount = 10000.0;
    }
}
