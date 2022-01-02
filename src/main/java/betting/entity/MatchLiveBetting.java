package betting.entity;

import UI.startFrame.StartFrame;
import betting.Betting;
import betting.bot.BetMaker;
import betting.enumeration.OddEven;
import betting.enumeration.SelectionBet;
import org.apache.log4j.Logger;

import java.util.*;

public class MatchLiveBetting {
    private Logger log;

    private boolean deleteThisMatch;
    private boolean isFinished;
    private boolean almostFinish;
    private boolean possibleToMakeBet;
    private boolean betIsDone = false;

    private int countLoose;
    private double margin;
    private double maxBet = 0.0;
    private double minBet;
    private String nameOfMatch;
    private String linkToMatch;
    boolean scheduleIsOn = false;

    private long scheduleTime = -7200000L;
    private long timeToQuarterFinish;
    private int currentPeriodInt;
    private SelectionBet currentPeriod;
    private ChainBetting chain;
    private Map<Integer, Bet> betStatistic;
    private Map<SelectionBet, Integer> matchScoreStatistic;
    private Map<SelectionBet, OddEven> matchOddEvenStatistic;
    private List<SelectionBet> possibleBetSelectionBet;

    private Betting betting;
//    private List<BetMaker> betMakers;

    private Map<Integer, BetMaker> betMakerMap;

    private BetMaker mainBetMaker;
    private Map<BetMaker, Integer> multiMap;
    private boolean eventMade;

    public MatchLiveBetting(Betting betting, String nameOfMatch, double margin, double minBet) {

    }
    public MatchLiveBetting(Betting betting, String nameOfMatch, String linkToMatch, double margin, double minBet) {
        this.betting = betting;
        betMakerMap = betting.getBetMakerMap();
        multiMap = new HashMap<>();

        while (mainBetMaker == null) {
            Random random = new Random();
            mainBetMaker = betMakerMap.get(random.nextInt(4));
        }

        mainBetMaker.setMain(this);
        chain = new ChainBetting(nameOfMatch, margin);
        this.log = Logger.getLogger(MatchLiveBetting.class);
        this.minBet = minBet;
        this.margin = margin;
        betStatistic = new HashMap<>();
        possibleBetSelectionBet = new ArrayList<>();
        this.nameOfMatch = nameOfMatch;
        this.linkToMatch = linkToMatch;
        matchScoreStatistic = new HashMap<>();
        matchOddEvenStatistic = new HashMap<>();
    }

    public void setMainBetMaker(BetMaker mainBetMaker) {
        this.mainBetMaker = mainBetMaker;
    }

    public void makeNextEvent() {
        System.out.println("Делаем событие для матча " + nameOfMatch);
        log.info("Делаем событие для матча " + nameOfMatch);
        if (isFinished) {
            Bet lastBet = getLastBetFromStatistic();
            if (lastBet != null) {
                for (Integer integer : betMakerMap.keySet()) {
                    BetMaker betMaker = betMakerMap.get(integer);
                    if (betMaker != null) {
                        betMaker.addBet(lastBet);
                        eventMade = true;
                    }
                }
            } else {
                deleteThisMatch = true;
            }
        } else {
            if (checkBet()) {
                makeBet();
            }
        }
    }

    public boolean checkBet() {
        Bet lastBet = getLastBetFromStatistic();
        if (lastBet != null) {
            try {
                log.info("Проверяем ставку для матча " + nameOfMatch + ". Ставка была на " + lastBet.getSelectionBet());
                if (lastBet.getWin() == null) {

                    Integer score = matchScoreStatistic.get(lastBet.getSelectionBet());
                    System.out.println("Счет четверти - " + score);
                    if (score > 0) {
                        SelectionBet selectionBet = lastBet.getSelectionBet();
                        OddEven oddEvenMatch = matchOddEvenStatistic.get(selectionBet);
                        lastBet.setScore(score);
                        if (oddEvenMatch == lastBet.getOddEven()) {
                            lastBet.setWin(true);
                        } else {
                            lastBet.setWin(false);
                        }
                    }
                }
                if (lastBet.getWin() != null) {
                    addBetToMatchStatistic(lastBet);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return true;
        }
    }

    public void setChain(ChainBetting chain) {
        this.chain = chain;
    }

    private void makeBet() {
        Bet lastBetFromStatistic = getLastBetFromStatistic();
        if (lastBetFromStatistic == null) {//first bet...
            Bet firstBet = null;
            SelectionBet selectionBet = getPossibleSelection();
            if (selectionBet != null) {
                OddEven oddEven = getOddEvenForBet(selectionBet, null);
                if (oddEven != null) {
                    firstBet = chain.getNextBet(selectionBet, oddEven, minBet, mainBetMaker);
                }

                if (firstBet != null && firstBet.getPrice() > 0.0) {
                    log.info("Создана первая ставка для матча " + nameOfMatch + ". Ставка будет поставлена на" + firstBet.getSelectionBet() + ". Выбор ставки " + firstBet.getOddEven());
                    mainBetMaker.addBet(firstBet);
                    eventMade = true;
                } else {
                    log.info("Ставки или NULL или цена ставки меньше нуля.");
                }
            } else {
                log.info("Выбор для ставки равен NULL");
            }
        } else {
            if (lastBetFromStatistic.getWin() != null) {
                Bet nextBet = null;
                SelectionBet selectionBet = getPossibleSelection();
                if (selectionBet != null) {
                    OddEven oddEven = getOddEvenForBet(getPossibleSelection(), lastBetFromStatistic);
                    if (oddEven != null) {
                        nextBet = chain.getNextBet(getPossibleSelection(), oddEven, minBet, mainBetMaker);
                    }
                    if (nextBet != null && nextBet.getPrice() > 0.0) {
                        log.info("Цена для следующей ставки " + nextBet.getPrice());
                        if (!lastBetFromStatistic.getWin()) {
                            log.info("Прошлая ставка главного матча проиграла, разбиваем следующую ставку: " + nextBet.getPrice());
                            Double nextBetPrice = nextBet.getPrice();

                            int betMakersCount = 0;
                            for (Integer integer : betMakerMap.keySet()) {
                                BetMaker betMaker = betMakerMap.get(integer);
                                if (betMaker != null && betMaker.getStorage().getCurrentBalance() > nextBetPrice) {
                                    betMakersCount++;
                                }
                            }

                            double minBet = StartFrame.getMinBet();
                            int countBetMakersToMakeBet = 0;
                            Double possibleBet = Math.round((nextBetPrice / (betMakersCount - countBetMakersToMakeBet++)) * 100.0) / 100.0;
                            if (possibleBet < minBet) {
                                possibleBet = Math.round((nextBetPrice / (betMakersCount - countBetMakersToMakeBet++)) * 100.0) / 100.0;
                                if (possibleBet < minBet) {
                                    possibleBet = Math.round((nextBetPrice / (betMakersCount - countBetMakersToMakeBet++)) * 100.0) / 100.0;
                                    if (possibleBet < minBet) {
                                        possibleBet = Math.round((nextBetPrice / (betMakersCount - countBetMakersToMakeBet++)) * 100.0) / 100.0;
                                        if (possibleBet < minBet) {
                                            possibleBet = Math.round((nextBetPrice / (betMakersCount - countBetMakersToMakeBet++)) * 100.0) / 100.0;
                                        }
                                    }
                                }
                            }
                            nextBet.setPrice(possibleBet);
                            mainBetMaker.addBet(nextBet);

                            int countBets = betMakersCount - countBetMakersToMakeBet;
                            int i = 0;
                            while (true) {
                                if (countBets == 0) {
                                    break;
                                }
                                for (Integer integer : betMakerMap.keySet()) {
                                    BetMaker b = betMakerMap.get(integer);
                                    if (b != null) {
                                        if (!b.isMain(this)) {
                                            if (b.getStorage().getCurrentBalance() > nextBet.getPrice()) {
                                                b.addBet(nextBet);
                                                multiMap.put(b, currentPeriodInt);
                                                countBets--;
                                            }
                                        }
                                    }
                                }
                            }
                            System.out.println("Могут поставить - " + countBets);
                            System.out.println("Ставка была " + nextBetPrice + ". Разбили ее на - " + multiMap.size());
                            eventMade = true;
                        } else {
                            mainBetMaker.addBet(nextBet);
                            eventMade = true;
                            log.info("Прошлая ставка главного матча выиграла, проверяем все ли биржи поставили.");
                            int countBetWasNotSetted = 0;
                            for (Integer integer : betMakerMap.keySet()) {
                                BetMaker b = betMakerMap.get(integer);
                                if (b != null) {
                                    if (multiMap.containsKey(b)) {
                                        countBetWasNotSetted++;
                                    }
                                }
                            }

                            multiMap.clear();
                            log.info("Не было поставлено ставок - " + countBetWasNotSetted);
                        }
                    }
                }
            }
        }
    }

    public void addBetToMultiStatistic(BetMaker betMaker) {
        System.out.println("Добавляет ставочник в матч " + betMaker.getUserName());
        multiMap.remove(betMaker);
    }

    public void addBetToMatchStatistic(Bet bet) {
        System.out.println("Добавляем ставку в матч - " + bet.getSelectionBet() +
                ". Время ставки - " + bet.getTimeBet() +
                ". Выиргала ставка - " + bet.getWin());
        Integer period = bet.getMatchPeriod();
        if (bet.getTimeBet() > -7200000L && bet.getWin() == null) {//add bet
            if (period == currentPeriodInt) {
//                if (matchGroup.getCopies().size() > 1) {
//                    bet.setCopyBetGroupId(matchGroup.getIdGroup());
//                    //подтверждаем, что ставка поставлена на этом сайте.
//                    if (bet.getPrice() > 0) {
//                        matchGroup.confirmSettedBet(parser, bet.getPrice());
//                    }
//
                betIsDone = true;
                chain.addBet(bet, betting);
                betStatistic.put(period, bet);
                log.info("Ставка для матча " + nameOfMatch + " успешно добавлена в статистику как новая ставка.");
                System.out.println("Ставка для матча " + nameOfMatch + " успешно добавлена в статистику как новая ставка.");
            }
        } else {
            if (bet.getWin()) {
                log.info("Ставка для матча " + bet.getMatchName() + ", период " + bet.getSelectionBet() + " выиграла.");
                log.info("Матч " + nameOfMatch + " завершил цепочку номер " + chain.getIdChain() + ". последняя ставка была " + bet.getSelectionBet() + " - " + bet.getOddEven());
                chain.setWin(true, betting);
//                if (chain.containBet(bet)) {
//                    System.out.println("Матч " + nameOfMatch + " завершил цепочку номер " + chain.getIdChain() + ". последняя ставка была " + bet.getSelectionBet() + " - " + bet.getOddEven());
//                    log.info("Матч " + nameOfMatch + " завершил цепочку номер " + chain.getIdChain() + ". последняя ставка была " + bet.getSelectionBet() + " - " + bet.getOddEven());
//                    chain.setWin(true, mainBetMaker);
//                } else {
//                    chain.setWin(true, mainBetMaker);
//                    log.error("Последняя ставка не была добавлена в цепочку.");
//                    log.error("Матч " + nameOfMatch + " завершил цепочку номер " + chain.getIdChain() + ". последняя ставка была " + bet.getSelectionBet() + " - " + bet.getOddEven());
//                }
                if (!isFinished) {
                    log.info(" Старая цепочка удалена.");
                    System.out.println("Старая цепочка удалена.");
                    if (chain.isWin()) {
                        log.info("Создана новая цепочка");
                        System.out.println("Создана новая цепочка.");
                        chain = new ChainBetting(nameOfMatch, margin);
                    }
                }
            } else {
                if (isFinished) {
                    log.info("Матч " + nameOfMatch + " закончился, текущая цепочка ID " + chain.getIdChain() + " остается в хранилище. ");
                    chain.addBet(bet, betting);
                    chain.setMatchLiveName(null, betting);
                    betting.addChain(chain);
                } else {
                    log.info("Ставка для матча " + bet.getMatchName() + ", период " + bet.getSelectionBet() + " проиграла.");
                    if (!chain.isWin()) {
                        chain.addBet(bet, betting);
                    } else {
                        chain = new ChainBetting(nameOfMatch, margin);
                    }
                    betting.addChain(chain);
                }
            }
            if (isFinished) {
                deleteThisMatch = true;
            }
        }

        mainBetMaker.getStorage().addBetToStorage(bet);
        checkCountLooseBet();
    }

    void addBetToStatisticStartBetMaker(Bet bet) {
//               if (bet.getWin() != null) {
//            if (!bet.getWin()) {
//                chain.addBet(bet,betting);
//            }
//        } else {
//            chain.addBet(bet, betting);
//        }
        betStatistic.put(bet.getMatchPeriod(), bet);
    }

    private OddEven getOddEvenForBet(SelectionBet selectionBet, Bet lastBet) {
        if (selectionBet == SelectionBet.Half1st) {// если ставим на первую половину          ============= 1-ST HALF ==========================
            if (lastBet == null) {// и в случае если ПЕРВАЯ ставка в матче,
                if (currentPeriodInt == 1) {//и  если ставим в первом периоде
                    return OddEven.Odd;
                    // ставка на 1-st Half будет просто нечет.(хотя эта ситуация очень редко может быть, так как первая ставка ставиться в начале периода всегда)
                } else { //если же ставим во втором периоде
                    if (matchScoreStatistic.get(SelectionBet.Q1) % 2 == 0) {//смотрим счет первого периода, если чет, следовательно второй период должен быть нечет,
                        return OddEven.Odd;//тоесть половина чет+нечет=нечет
                    } else {// если первый период был нечет. нам нужен чет во втором
                        return OddEven.Odd;// и чет + нечет = нечет в половине
                    }
                }
            } else {// если ВТОРАЯ ставка в матче будет на первую половину матча
                if (lastBet.getOddEven() == OddEven.Odd) {//если первая ставка была на нечет.
                    if (lastBet.getWin()) {// - прошлая ставка на первую четверть выиграла, тоесть ставка на вторую четверть должна была быть чет.
                        return OddEven.Odd;// ставка на половину матча будет чет+нечет = нечет.
                    } else {// - прошлая ставка на первую четверть проиграла, тоесть ставка на вторую должная была быть нечет
                        return OddEven.Odd;// а ставка на половину будет чет+нечет=нечет.
                    }
                } else {//если первая ставка была с цепочки, и на чет.
                    if (lastBet.getWin()) {// - прошлая ставка на первую четверть выиграла, тоесть ставка на вторую четверть должна была быть нечет.
                        return OddEven.Odd;// ставка на половину матча будет чет+нечет = нечет.
                    } else {// - прошлая ставка на первую четверть проиграла, тоесть ставка на вторую должная была быть чет
                        return OddEven.Odd;// а ставка на половину будет нечет+чет = чет.
                    }
                }
            }
        } else if (selectionBet == SelectionBet.Half2nd) {// если ставим на вторую половину матча  ================2-ND HALF========================
            if (lastBet == null) {// в случае если ПЕРВАЯ ставка в матче
                if (currentPeriodInt == 3) {// если ставим в третьем периоде
                    return OddEven.Odd;  // ставка на 2-nd Half будет просто нечет.
                } else { //если ставим в четвертом периоде
                    if (matchScoreStatistic.get(SelectionBet.Q3) % 2 == 0) {//смотрим счет третьего периода, если чет, следовательно четвертый период должен быть нечет,
                        return OddEven.Odd;//тоесть половина матча чет+нечет=нечет
                    } else {// если первый период был нечет. нам нужен чет во втором
                        return OddEven.Odd;// и чет + нечет = нечет в половине
                    }
                }
            } else {// если ставка НЕ ПЕРВАЯ
                if (lastBet.getSelectionBet() == SelectionBet.Q3) {// если последняя ставка была в третьей четверти, тоесть сейчас четвертая
                    if (lastBet.getWin()) {// и ставка втретьей чеверти выграла, тоесть в четвертой должна быть противоположной
                        return OddEven.Odd;//и при сумме двух противоположных нам нужен нечет, ставим Half 2 - нечет.
                    } else {// если в третьей четверти ставка проиграла, в четвертой была бы такая же
                        return OddEven.Even;// тоесть при сумме всегда будет чет в половине матча.
                    }
                } else {//если последняя ставка была во второй четверти, тоесть ставим в третьей четверти на 2-ND HALF, в третьей и четвертой четвертях должны быть разные значения
                    return OddEven.Odd;// тоесть на вторую ставим возвращаем чет+нечет=нечет.
                }
            }
        } else if (selectionBet == SelectionBet.Match) {//если ставим на матч =================MATCH=====================
            if (lastBet == null) {//если ПЕРВАЯ ставка в матче----------------------------------------------------------------------
                if (currentPeriodInt == 3) {//Если ставим в ТРЕТЬЕЙ четверти, то ставки в 3 и 4 четверти должны быть разные результаты, в сумме нечет.
                    int first = matchScoreStatistic.get(SelectionBet.Q1);
                    int second = matchScoreStatistic.get(SelectionBet.Q2);
                    int total = first + second;
                    if (total % 2 == 0) {//если сумма первые двух была чет
                        return OddEven.Odd;  // на матч ставим чет+нечет = нечет
                    } else { //если сумма первых двух была нечет
                        return OddEven.Even;// на матч ставим нечет+нечет = чет
                    }
                } else { //если ставим в ЧЕТВЕРТОЙ четверти
                    int first = 0;
                    int second = 0;
                    int third = 0;
                    if (matchScoreStatistic.containsKey(SelectionBet.Q1)) {
                        first = matchScoreStatistic.get(SelectionBet.Q1);
                    }
                    if (matchScoreStatistic.containsKey(SelectionBet.Q2)) {
                        second = matchScoreStatistic.get(SelectionBet.Q2);
                    }
                    if (matchScoreStatistic.containsKey(SelectionBet.Q3)) {
                        third = matchScoreStatistic.get(SelectionBet.Q3);
                    }
                    int totalThree = first + second + third;

                    if (third % 2 == 0) {//если третья четверть закончилась с четным счетом. четвертая должна быть с нечет
                        if (totalThree % 2 == 0) {// и если три предыдущих имею четный счет
                            return OddEven.Odd;//ставка на матч будет нечет+чет=нечет
                        } else {//если три предидущих имеют нечетный счет
                            return OddEven.Even;//ставка на матч будет нечет+нечет = чет
                        }
                    } else {//если же третья четверть закончилась с нечетным счетом, четвертая должна быть чет
                        if (totalThree % 2 == 0) {// и если три предыдущих имеют чет.
                            return OddEven.Even;//ставка на матч будет чет+чет=чет
                        } else {//если три предидущих имеют нечет.
                            return OddEven.Odd;//ставка на матч будет чет+нечет = нечет
                        }
                    }
                }
            } else {//-НЕ ПЕРВАЯ СТАВКА В МАТЧЕ---------------------------------------------------------------------------
                int first = 0;
                int second = 0;
                int third = 0;
                if (matchScoreStatistic.containsKey(SelectionBet.Q1)) {
                    first = matchScoreStatistic.get(SelectionBet.Q1);
                }
                if (matchScoreStatistic.containsKey(SelectionBet.Q2)) {
                    second = matchScoreStatistic.get(SelectionBet.Q2);
                }
                if (matchScoreStatistic.containsKey(SelectionBet.Q3)) {
                    third = matchScoreStatistic.get(SelectionBet.Q3);
                }
                int totalThree = first + second + third;
                if (lastBet.getSelectionBet() == SelectionBet.Q3) {// если последняя ставка была в третьей четверти, тоесть ставим в четвертой
                    switch (lastBet.getOddEven()) {//определяем на что была последняя ставка
                        case Odd: {//LAST BET bet was Odd - '1', \если последняя ставка была НЕЧЕТ.
                            if (lastBet.getWin()) {// last bet WIN next bet for quarter should be Even - '2',\ и выиграла, в четвертой четверти должна быть чет.
                                if (totalThree % 2 == 0) {     //total three quarters is Even - '2'. \ Если за три четверти сумма - чет
                                    return OddEven.Even;   //bet for Match will be Even - '2'     ============================= на матч ставим чет+чет = чет
                                } else {                        //total three quarters is Odd - '1' \ Если за три четветри сумма - нечет
                                    return OddEven.Odd;    //bet for Match will be Odd  - '1'     ============================= на матч ставим чет+нечет = нечет.
                                }
                            } else {          // last bet LOOSE next bet for quarter should be Odd - '1' \ если последняя ставка проиграла, (была нечет), в четвертой должен быть нечет
                                if (totalThree % 2 == 0) {      //total three quarter is Even - '2'\ Если за три четветри сумма - чет
                                    return OddEven.Odd;    //bet for Match will be Odd  - '1'     ============================= на матч ставим нечет+чет = нечет.
                                } else {                        //total three quarter is Odd  - '1'\ Если за три четветри сумма - нечет
                                    return OddEven.Even;   //bet for Match will be Even - '2'     ============================= на матч ставим нечет+нечет = чет.
                                }
                            }
                        }
                        case Even: {//LAST BET was '2' \последняя ставка была ЧЕТ
                            if (lastBet.getWin()) {//last bet WIN next bet for quarter should be Odd - '1'\ и выиграла, тоесть на четвертую ставить нужно нечет.
                                if (totalThree % 2 == 0) {//total three quarters is Even - '2'\ и сумма за три четветри равна чет
                                    return OddEven.Odd;//bet for Match will be Odd  - '1'  ============================ на матч ставим нечет+чет=нечет
                                } else {       //total three quarters is Odd  - '1' \\ ставка проиграла, тоесть на четвертую ставить нужно чет
                                    return OddEven.Even;       //bet for Match will be Even - '2'  ============================ на матч ставим чет + чет = чет
                                }
                            } else {        //last bet LOOSE next bet for quarter should be Even - '2'\\ последняя ставка проиграла(была ЧЕТ), нужно ставить на четв. - чет
                                if (totalThree % 2 == 0) {                //total three quarters is Even - '2'// сумма за три матча - чет
                                    return OddEven.Even;        //bet for Match will be Even  - '2'  ============================ на матч ставим чет+чет = чет
                                } else {                            //total three quarters is Odd  - '1' //сумма за три матча - нечет
                                    return OddEven.Odd;       //bet for Match will be Odd - '1'  ============================ на матч ставим чет+нечет = нечет
                                }
                            }
                        }
                    }
                } else {//Если ставим в ТРЕТЬЕЙ четверти, то ставки в 3 и 4 четверти должны быть разные результаты, в сумме нечет.
                    first = matchScoreStatistic.get(SelectionBet.Q1);
                    second = matchScoreStatistic.get(SelectionBet.Q2);
                    int total = first + second;
                    if (total % 2 == 0) {//если сумма первые двух была чет
                        return OddEven.Odd;  // на матч ставим чет+нечет = нечет
                    } else { //если сумма первых двух была нечет
                        return OddEven.Even;// на матч ставим нечет+нечет = чет
                    }
                }
            }
        } else {
            return getOddEvenForQuarterBet(selectionBet, lastBet);// метод возвращает выбор для четвертей
        }
        return null;
    }

    private OddEven getOddEvenForQuarterBet(SelectionBet selectionBet, Bet lastBet) {
        if (selectionBet != null) {
            if (selectionBet == SelectionBet.Q1) {//если ставим на ПЕРВУЮ четверть первую ставку.===============================
                System.out.println("Ставка первая в матче, проверяем нет ли цепочек для продолжения, что бы прикрепить к этому матчу.");
                log.info("Ставка первая в матче, проверяем нет ли цепочек для продолжения, что бы прикрепить к этому матчу.");

                ChainBetting chainFromBetting = betting.getChain(maxBet, mainBetMaker);
                if (chainFromBetting != null) {
                    log.info("Найдена цепочка, пробуем прикрепить к матчу.");
                    if (chain.getList().size() == 0) {
                        log.info("Цепочка прикреплена к матчу.");
                        chain = chainFromBetting;
                        chain.setMatchLiveName(nameOfMatch, betting);
                        return chain.getLastBetOddEven();
                    } else {
                        return OddEven.Odd;
                    }
                } else {
                    log.info("Нет подходящих цепочек, что бы прикрепить к данному матчу.");
                    return OddEven.Odd;//ставим нечет. если в хранилище нет цепочек
                }
            }

            if (lastBet != null) {// если НЕ ПЕРВАЯ ставка ======================================================
                if (selectionBet == SelectionBet.Q2) {// если ставим на вторую четверть. смотрим результаты перевой
                    if (lastBet.getWin()) {// если прошлая ставка выигала, ставим на противоположный
                        return lastBet.getOtherOddEven();// ставим на противоположный
                    } else {// если проиграла
                        return lastBet.getOddEven();// ставим на то же самое
                    }
                }

                if (selectionBet == SelectionBet.Q3) {// если ставим на третью четверть
                    Integer secondQuarterScore = matchScoreStatistic.get(SelectionBet.Q2);
                    if (secondQuarterScore % 2 == 0) { // если результат чет., все по плану ставка могла бы выиграть,
                        return OddEven.Odd;//возвращаем нечет.
                    } else {// если результат нечет, наша ставка проиграла бы, и нужно было бы возвращать чет снова
                        return OddEven.Even;// возвращаем чет.
                    }
                }

                if (selectionBet == SelectionBet.Q4) {
                    Integer thirdQuarterScore = matchScoreStatistic.get(SelectionBet.Q3);
                    if (thirdQuarterScore % 2 == 0) {//если третья четверть была чет. ставка выиграла бы
                        return OddEven.Odd;// и четвертую ставим на нечет
                    } else {//если третья четверть нечет, ставка проиграла бы
                        return OddEven.Even;// и четвертую снова ставим на чет.
                    }
                }
            } else {
                switch (selectionBet) {
                    case Q2: {
                        if (matchScoreStatistic.get(SelectionBet.Q1) % 2 == 0) {//если четный
                            return OddEven.Odd;
                        } else {
                            return OddEven.Even;
                        }
                    }
                    case Q3: {
                        if (matchScoreStatistic.get(SelectionBet.Q2) % 2 == 0) {
                            return OddEven.Odd;
                        } else {
                            return OddEven.Even;
                        }
                    }
                    case Q4: {
                        if (matchScoreStatistic.get(SelectionBet.Q3) % 2 == 0) {
                            return OddEven.Odd;
                        } else {
                            return OddEven.Even;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void checkCountLooseBet() {
        countLoose = 0;
        if (betStatistic.containsKey(1)) {
            if (betStatistic.get(1) != null && betStatistic.get(1).getWin() != null) {
                if (betStatistic.get(1).getWin()) {
                    countLoose = 0;
                } else {
                    countLoose++;
                }
            }
        }

        if (betStatistic.containsKey(2)) {
            if (betStatistic.get(2) != null && betStatistic.get(2).getWin() != null) {
                if (betStatistic.get(2).getWin()) {
                    countLoose = 0;
                } else {
                    countLoose++;
                }
            }
        }

        if (betStatistic.containsKey(3)) {
            if (betStatistic.get(3) != null && betStatistic.get(3).getWin() != null) {
                if (betStatistic.get(3).getWin()) {
                    countLoose = 0;
                } else {
                    countLoose++;
                }
            }
        }

        if (betStatistic.containsKey(4)) {
            if (betStatistic.get(4) != null && betStatistic.get(4).getWin() != null) {
                if (betStatistic.get(4).getWin()) {
                    countLoose = 0;
                } else {
                    countLoose++;
                }
            }
        }
    }

    public void setCurrentPeriod(SelectionBet currentPeriod) {
        int oldCurrentPeriod = currentPeriodInt;
        switch (currentPeriod) {
            case Q1:
                currentPeriodInt = 1;
                break;
            case Q2:
                currentPeriodInt = 2;
                break;
            case Q3:
                currentPeriodInt = 3;
                break;
            case Q4:
                currentPeriodInt = 4;
                break;
            case OT:
                currentPeriodInt = 4;
                break;
        }

        if (currentPeriodInt > oldCurrentPeriod) {
            this.currentPeriod = currentPeriod;
            betIsDone = false;
            eventMade = false;
        } else {
            currentPeriodInt = oldCurrentPeriod;
        }

        if (betStatistic.containsKey(currentPeriodInt)) {
            betIsDone = true;
        }
    }

    public void setEventMade(boolean eventMade) {
        this.eventMade = eventMade;
    }

    private Bet getLastBetFromStatistic() {
        Bet bet;

        if (betStatistic.containsKey(4)) {
            bet = betStatistic.get(4);
            return bet;
        } else if (betStatistic.containsKey(3)) {
            bet = betStatistic.get(3);
            return bet;
        } else if (betStatistic.containsKey(2)) {
            bet = betStatistic.get(2);
            return bet;
        } else if (betStatistic.containsKey(1)) {
            bet = betStatistic.get(1);
            return bet;
        } else {
            return null;
        }
    }

    private SelectionBet getPossibleSelection() {
        if (possibleBetSelectionBet.contains(SelectionBet.Q1)) {
            if (currentPeriodInt == 1) {
                return SelectionBet.Q1;
            }
        } else if (possibleBetSelectionBet.contains(SelectionBet.Q2)) {
            if (currentPeriodInt == 2) {
                return SelectionBet.Q2;
            }
        } else if (possibleBetSelectionBet.contains(SelectionBet.Q3)) {
            if (currentPeriodInt == 3) {
                return SelectionBet.Q3;
            }
        } else if (possibleBetSelectionBet.contains(SelectionBet.Q4)) {
            if (currentPeriodInt == 4) {
                return SelectionBet.Q4;
            }
        } else if (possibleBetSelectionBet.contains(SelectionBet.Half1st)) {
            if (currentPeriodInt == 2) {
                return SelectionBet.Half1st;
            } else {
                return null;
            }
        } else if (possibleBetSelectionBet.contains(SelectionBet.Half2nd)) {
            if (currentPeriodInt == 4) {
                return SelectionBet.Half2nd;
            } else {
                return null;
            }
        } else if (possibleBetSelectionBet.contains(SelectionBet.Match)) {
            if (currentPeriodInt == 4) {
                return SelectionBet.Match;
            } else {
                return null;
            }
        } else {
            return null;
        }
        return null;
    }

    public String getNameOfMatch() {
        return nameOfMatch;
    }

    public long getTimeToQuarterFinish() {
        return timeToQuarterFinish;
    }

    public void setTimeToQuarterFinish(long timeToQuarterFinish) {
        this.timeToQuarterFinish = timeToQuarterFinish;
    }

    public SelectionBet getCurrentPeriod() {
        return currentPeriod;
    }

    public double getMaxBet() {
        return maxBet;
    }

    public void setMaxBet(double maxBet) {
        log.info("Установлена максимальная ставка для матча " + getNameOfMatch() + ". равна - " + maxBet);
        this.maxBet = maxBet;
    }

    public void setMaxBetForGroup(double maxBet) {
        this.maxBet = maxBet;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isDeleteThisMatch() {
        return deleteThisMatch;
    }

    public void setFinished(boolean finished) {
        betIsDone = false;
        eventMade = false;
        isFinished = finished;
    }

    public boolean isBetIsDone() {
        return betIsDone;
    }

    public boolean isPossibleToMakeBet() {
        return possibleToMakeBet;
    }

    public void setPossibleToMakeBet(boolean possibleToMakeBet) {
        this.possibleToMakeBet = possibleToMakeBet;
    }

    public int getCurrentPeriodInt() {
        return currentPeriodInt;
    }

    public ChainBetting getChain() {
        return chain;
    }

    public String getLasBetPriseString() {
        String selection = null;
        String price = null;
        String oddEven = null;
        if (betStatistic.containsKey(currentPeriodInt)) {
            price = String.valueOf(betStatistic.get(currentPeriodInt).getPrice());
            selection = String.valueOf(betStatistic.get(currentPeriodInt).getSelectionBet());
            oddEven = String.valueOf(betStatistic.get(currentPeriodInt).getOddEven());
            return selection + " " + price + " " + oddEven;
        } else if (betStatistic.containsKey(currentPeriodInt - 1)) {
            price = String.valueOf(betStatistic.get(currentPeriodInt - 1).getPrice());
            selection = String.valueOf(betStatistic.get(currentPeriodInt - 1).getSelectionBet());
            oddEven = String.valueOf(betStatistic.get(currentPeriodInt - 1).getOddEven());
            return selection + " " + price + " " + oddEven;
        } else if (betStatistic.containsKey(currentPeriodInt - 2)) {
            price = String.valueOf(betStatistic.get(currentPeriodInt - 2).getPrice());
            selection = String.valueOf(betStatistic.get(currentPeriodInt - 2).getSelectionBet());
            oddEven = String.valueOf(betStatistic.get(currentPeriodInt - 2).getOddEven());
            return selection + " " + price + " " + oddEven;
        } else if (betStatistic.containsKey(currentPeriodInt - 3)) {
            price = String.valueOf(betStatistic.get(currentPeriodInt - 3).getPrice());
            selection = String.valueOf(betStatistic.get(currentPeriodInt - 3).getSelectionBet());
            oddEven = String.valueOf(betStatistic.get(currentPeriodInt - 3).getOddEven());
            return selection + " " + price + " " + oddEven;
        } else {
            return "Нет Ставки";
        }
    }

    public Map<SelectionBet, Integer> getMatchScoreStatistic() {
        return matchScoreStatistic;
    }

    public Map<Integer, Bet> getBetStatistic() {
        return betStatistic;
    }

    public List<SelectionBet> getPossibleBetSelectionBet() {
        return possibleBetSelectionBet;
    }

    public Map<SelectionBet, OddEven> getMatchOddEvenStatistic() {
        return matchOddEvenStatistic;
    }

    public boolean isEventMade() {
        return eventMade;
    }

    public double getMargin() {
        return margin;
    }

    public boolean isAlmostFinish() {
        return almostFinish;
    }

    public void setAlmostFinish(boolean almostFinish) {
        this.almostFinish = almostFinish;
    }
}
