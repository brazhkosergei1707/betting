package betting.entity;

import betting.Betting;
import betting.bot.BetMaker;

import java.util.*;

public class StorageBetting {

    private String nameOfUser;
    private Betting betting;
    private double minBet;
    private String currentBalanceString = "0";
    private double currentBalance = 0.0;

    private List<Bet> currentBets;
    private List<Bet> historyBets;

    private BetMaker betMaker;

    public StorageBetting(Betting betting, String nameOfUser, BetMaker betMaker) {
        this.nameOfUser = nameOfUser;
        this.betting = betting;
        this.betMaker = betMaker;
        currentBets = new ArrayList<>();
        historyBets = new ArrayList<>();
    }

    public void addBetToStorage(Bet bet) {
        bet.setUserName(nameOfUser);
        if (bet.getWin() == null) {
            boolean newCurrentBet = true;
            for (Bet currentBet : currentBets) {
                if (currentBet.getMatchName().contains(bet.getMatchName().split(" vs ")[0])) {
                    if (currentBet.getSelectionBet() == bet.getSelectionBet()) {
                        if (currentBet.getOddEven() == bet.getOddEven()) {
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
        cleanCurrentBetList();
    }

    public void checkToOldBets() {
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

            if (betting.getBettingMatchLive().containsKey(matchName)) {
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

    public void checkBatsInStorage() {
        try {
            Map<String, MatchLiveBetting> bettingMatchLive = betting.getBettingMatchLive();
            System.out.println("Сейчас есть матчей в главном беттинге - " + bettingMatchLive.size());

            for (String nameOfMatch : bettingMatchLive.keySet()) {
                MatchLiveBetting matchLiveBetting = bettingMatchLive.get(nameOfMatch);

                Map<Integer, Bet> matchPossibleStatistic = new HashMap<>();
                List<Integer> list = new ArrayList<>();

                System.out.println("Ищем ставки для матча - " + matchLiveBetting.getNameOfMatch());

                for (Bet bet : historyBets) {
                    System.out.println("Сравниваем ставку - " + bet.getMatchName());
                    if (bet.getMatchName().compareTo(matchLiveBetting.getNameOfMatch()) == 0) {
                        System.out.println("Добавляем эту ставку в возможную статистику, номер периода равен - " + bet.getMatchPeriod());
                        matchPossibleStatistic.put(bet.getMatchPeriod(), bet);
                        list.add(bet.getMatchPeriod());
                    }
                }

                for (Bet bet : currentBets) {
                    System.out.println("Сравниваем ставку - " + bet.getMatchName());
                    if (bet.getMatchName().compareTo(matchLiveBetting.getNameOfMatch()) == 0) {
                        System.out.println("Добавляем эту ставку в возможную статистику, номер периода равен - " + bet.getMatchPeriod());
                        matchPossibleStatistic.put(bet.getMatchPeriod(), bet);
                        list.add(bet.getMatchPeriod());
                    }
                }

                System.out.println("Размер статистики матча - " + matchLiveBetting.getMatchOddEvenStatistic().size());
                System.out.println("Размер возможной статистики матча - " + matchPossibleStatistic.size());
                if (matchLiveBetting.getMatchOddEvenStatistic().size() < matchPossibleStatistic.size()) {
                    Collections.sort(list);

                    for (Integer matchPeriod : list) {
                        Bet bet = matchPossibleStatistic.get(matchPeriod);
                        System.out.println("Добавляем ставку в матч, период - " + bet.getMatchPeriod());
                        matchLiveBetting.addBetToStatisticStartBetMaker(bet);
                    }

                    System.out.println("Делаем славным ставочник номер - " + betMaker.getUserName());
                    matchLiveBetting.setMainBetMaker(betMaker);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//            TODO for (int i = historyBets.size() - 1; i >= 0; i--) {
//            TODO    Bet bet = historyBets.get(i);
//            TODO    if (bet.getMatchName().contains("@")) {
//            TODO        String[] split = bet.getMatchName().split(" @ ");
//            TODO        bet.setMatchName(split[0] + " vs " + split[1]);
//            TODO    }
//            TODO
//                Map<String, MatchLiveBetting> bettingMatchLive = betting.getBettingMatchLive();
//                if (bettingMatchLive.containsKey(bet.getMatchName())) {
//
//                    System.out.println("Добавляем ставку в статистику матча " + bet.getMatchName());
//                    MatchLiveBetting matchLiveBetting = bettingMatchLive.get(bet.getMatchName());
//                    matchLiveBetting.addBetToMatchStatistic(bet);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        try {
//            for (int i = 0; i < currentBets.size(); i++) {
//                Bet bet = currentBets.get(i);
//                if (bet.isPlay()) {
//                    bet.setPlay(false);
//                }
//
//                // use @ in match name of bet not  vs
//                if (bet.getMatchName().contains("@")) {
//                    String[] split = bet.getMatchName().split(" @ ");
//                    bet.setMatchName(split[0] + " vs " + split[1]);
//                }
//
//                if (currentMatches.containsKey(bet.getMatchName().hashCode())) {
//                    MatchLive matchLive = currentMatches.get(bet.getMatchName().hashCode());
//                    bet.setMatchPeriod(matchLive.getCurrentPeriodInt());
//                    bet.setTimeBet(currentTime + 10000L);
//                    matchLive.addBetToMatchStatistic(bet);
//                    System.out.println("Ставка " + bet.getSelectionBet() + " чет/нечет - " + bet.getOddEven() + ", добавлена в матч - " + matchLive.getNameOfMatch());
//                    log.info("Ставка " + bet.getSelectionBet() + " чет/нечет - " + bet.getOddEven() + ", добавлена в матч - " + matchLive.getNameOfMatch());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void cleanCurrentBetList() {
        currentBets.removeIf(e -> e.getWin() != null);
    }

    public List<Bet> getCurrentBets() {
        return currentBets;
    }

    public Double getCurrentBetPrice() {
        Double price = 0.0;

        for (Bet bet : currentBets) {
            price += bet.getPrice();
        }
        return price;
    }

    public List<Bet> getHistoryBets() {
        return historyBets;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public double getMinBet() {
        return minBet;
    }

    public void setMinBet(double minBet) {
        this.minBet = minBet;
    }

    public void setCurrentBalanceString(String currentBalanceString) {
        if (this.currentBalanceString.compareTo(currentBalanceString) != 0) {
            this.currentBalanceString = currentBalanceString;
            System.out.println("Current money count text is: " + this.currentBalanceString);
        }
//        currentBalanceString = "10000.0";
    }

    public void setCurrentBalance(Double balance) {
//        currentBalance = 10000;
        if (balance != null) {
            this.currentBalance = balance;
        }
    }
}


