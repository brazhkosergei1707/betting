package entityes;

import betting.entity.Bet;
import betting.enumeration.OddEven;
import betting.enumeration.SelectionBet;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import parser.Parser;

import java.text.SimpleDateFormat;
import java.util.*;

/*The match entity. */
public class MatchLive {
//    private static Logger log = Logger.getLogger(MatchLive.class);
    private Logger log;

    MatchGroup matchGroup;
    private boolean isMainMatch;

    private boolean insideMatch;
    private boolean deleteThisMatch;
    private boolean isFinished;
    private boolean almostFinish;
    private boolean possibleToMakeBet;
    private boolean betIsDone;
    private boolean startMakeEvent;

    private int countLoose;
    private double margin;
    private double maxBet = 0.0;
    private double minBet;
    SimpleDateFormat dateFormat;
    private String nameOfMatch;
    boolean scheduleIsOn = false;

    private long scheduleTime = -7200000L;
    private long timeToQuarterFinish;
    private int currentPeriodInt;
    private SelectionBet currentPeriod;
    private Chain chain;
    private Map<Integer, Bet> betStatistic;
    private Map<SelectionBet, Integer> matchScoreStatistic;
    private Map<SelectionBet, OddEven> matchOddEvenStatistic;
    private List<SelectionBet> possibleBetSelectionBet;
    private Parser parser;

    public MatchLive(String nameOfMatch, double margin, Parser parser, double minBet) {
        this.log = Logger.getLogger(MatchLive.class);
        log.addAppender(parser.getAppender());


        this.minBet = minBet;
        this.parser = parser;
        chain = new Chain(nameOfMatch, margin);
        this.margin = margin;
        dateFormat = new SimpleDateFormat();
        betStatistic = new HashMap<>();
        possibleBetSelectionBet = new ArrayList<>();
        this.nameOfMatch = nameOfMatch;
        matchScoreStatistic = new HashMap<>();
        matchOddEvenStatistic = new HashMap<>();
//        matchGroup = new MatchGroup(this);
    }

    public void makeNextEvent(WebDriver driver) {
        boolean makeEvent = true;

//        if (matchGroup.getCopies().size() > 1) {//если есть копии матча, проверяем что делать этому матчу.
//            makeEvent = MatchesScanner.getMatchesScanner().makeEventForMatch(this);
//        }

        if (makeEvent) {
            startMakeEvent = true;
            log.info("Делаем событие для матча " + nameOfMatch + ". На сайте номер: " + parser.getSwitcherId());
            System.out.println("Делаем событие для матча " + nameOfMatch + ". На сайте номер: " + parser.getSwitcherId());
            if (!betIsDone && !isFinished) {
                insideMatch = parser.clickToMatch(driver, nameOfMatch);
            }

//            if (isFinished || currentPeriod == SelectionBet.Q3) {
//                if (matchGroup.getCopies().size() > 0 && !isMainMatch) {
//                    System.out.println("Матч - копия, не устанавливаем отложенную проверку.");
//                } else {
//                    if (betStatistic.size() > 0) {
//                        if (!scheduleIsOn) {
//                            log.info("Проверка ставки для матча " + nameOfMatch + " будте отложена на полторы минуты");
//                            System.out.println("Scheduler is installed ");
//                            scheduleIsOn = true;
//                            scheduleTime = StartFrame.getSwitcher(parser, parser.getSwitcherId()).getStorage().getCurrentTime() + 90000;
//                        }
//                    }
//                }
//            }

            if (isFinished && !deleteThisMatch) {
//                if (scheduleTime < StartFrame.getSwitcher(parser, parser.getSwitcherId()).getStorage().getCurrentTime()) {
                if (true) {
                    Bet lastBet = getLastBetFromStatistic();
                    if (lastBet != null && lastBet.getPrice() > 0) {
                        if (lastBet.getWin() == null) {
                            log.info("Будет проверена последняя ставка для матча " + nameOfMatch);

                            if (betAlreadyNotPlayed(lastBet, driver)) {
                                checkBet(lastBet, driver);
                            }

                            if (lastBet.getWin() != null || lastBet.getCountCheck() > 5) {
                                if (lastBet.getCountCheck() > 5) {
                                    log.error("Ставка так и не была проверена. Считаем что ставка проиграла. И добавляем в статистику матча.");
                                    lastBet.setWin(false);
                                }
                                log.info("Ставка успешно проверена и будет добавлена в статистику матча.");
                                addBetToMatchStatistic(lastBet);
                            } else {
                                log.error("Последняя ставка не была проверена будет выполнена еще одна попытка проверить эту ставку.");
                                System.out.println("Последняя ставка не была проверена будет выполнена еще одна попытка проверить эту ставку.");
                                lastBet.increaseCountCheck();
                            }
                        } else {
                            addBetToMatchStatistic(lastBet);
                            deleteThisMatch = true;
                            scheduleTime = -7200000L;
                            scheduleIsOn = false;
                            System.out.println("Scheduler is set to zero one, bet is checked...");
                        }
                    } else {
                        if (lastBet != null) {
                            if (lastBet.getPrice() < 0) {
                                lastBet.setWin(true);
                                addBetToMatchStatistic(lastBet);
                            }
                        }

                        deleteThisMatch = true;
                        scheduleTime = -7200000L;
                        scheduleIsOn = false;
                        System.out.println("Scheduler is set to zero with out checking last bet... ");
                    }
                }
            }

            if (insideMatch && !isFinished) {
                System.out.println("Check game score for match " + nameOfMatch + "...");
                checkGameScore(driver);
                checkMaximumBetSizeAndPossibleBetSelection(driver);
//                if (scheduleTime < StartFrame.getSwitcher(parser, parser.getSwitcherId()).getStorage().getCurrentTime()) {
                if (true) {
                    System.out.println("Checking bet for match " + nameOfMatch + "...");
                    checkBet();
                    System.out.println("Making bet for match " + nameOfMatch + "...");
                    makeBet(driver);
                    goBackToOverview(driver);
                    System.out.println("Scheduler is set to zero ");
                    scheduleTime = -7200000L;
                    scheduleIsOn = false;
                } else {
                    goBackToOverview(driver);
                }
            }
            startMakeEvent = false;
        }
    }

    private boolean betAlreadyNotPlayed(Bet lastBet, WebDriver driver) {
        Boolean b = parser.betAlreadyNotPlayed(lastBet, driver);
        if (b != null) {
            return b;
        } else {
            return true;
        }
    }

    public void checkGameScore(WebDriver driver) {
        parser.checkGameScore(driver, this);
    }

    public void checkMaximumBetSizeAndPossibleBetSelection(WebDriver driver) {
        parser.checkMaximumBetSizeAndPossibleBetSelection(driver, this);
    }

    public void checkBet() {
        if (betStatistic.size() == 0) {
            System.out.println("Bet statistic is empty");
            log.info("Статистика для матча " + nameOfMatch + " пуста");
            return;
        }

        Bet lastBet = getLastBetFromStatistic();
        log.info("Проверяем ставку для матча " + nameOfMatch + ". Ставка была на " + lastBet.getSelectionBet());
        System.out.println("Проверяем ставку для матча " + nameOfMatch + ". Ставка была на " + lastBet.getSelectionBet());
        if (lastBet.getWin() == null) {
            SelectionBet selectionBet = lastBet.getSelectionBet();
            OddEven oddEvenMatch = matchOddEvenStatistic.get(selectionBet);
            lastBet.setScore(matchScoreStatistic.get(lastBet.getSelectionBet()));
            if (oddEvenMatch == lastBet.getOddEven()) {
                lastBet.setWin(true);
            } else {
                lastBet.setWin(false);
            }
        }

        log.info("Добавляем ставку в статистику матча.");
        if (lastBet.getWin() != null || lastBet.getCountCheck() > 5) {
            if (lastBet.getCountCheck() > 5) {
                log.error("Ставка так и не была проверена. Считаем что ставка проиграла. И добавляем в статистику матча.");
                lastBet.setWin(false);
            }
            log.info("Ставка успешно проверена и будет добавлена в статистику матча.");
            addBetToMatchStatistic(lastBet);
        } else {
            log.error("Ошибка во время проверки ставки, будет выполнена еще одна попытка проверить эту ставку.");
            lastBet.increaseCountCheck();
        }
    }

    private Bet checkBet(Bet bet, WebDriver driver) {
        return parser.checkBet(bet, driver);
    }

    private void makeBet(WebDriver driver) {
        Bet lastBetFromStatistic = getLastBetFromStatistic();
        if (lastBetFromStatistic == null) {//first bet...
            System.out.println("First bet");
            Bet newBet = null;
            SelectionBet selectionBet = getPossibleSelection();
            System.out.println("Selection bet is: " + selectionBet);
            if (selectionBet != null) {
                OddEven oddEven = getOddEvenForBet(selectionBet, null);
                if (oddEven != null) {
                    newBet = chain.getNextBet(selectionBet, oddEven, parser, minBet);
                    newBet.setMatchPeriod(currentPeriodInt);
                }
                if (newBet != null && newBet.getPrice() > 0.0) {
                    log.info("Создана первая ставка для матча " + nameOfMatch + ". Ставка будет поставлена на" + newBet.getSelectionBet() + ". Выбор ставки " + newBet.getOddEven());
                    makeBet(newBet, driver);
                    if (newBet.getTimeBet() > -7200000L) {
                        log.info("Ставка для матча " + nameOfMatch + " успешно поставлена и будет добавлена в статистику");
                        addBetToMatchStatistic(newBet);
                    }
                } else {
                    System.out.println("Ставки или NULL или цена ставки меньше нуля.");
                    log.info("Ставки или NULL или цена ставки меньше нуля.");
                }
            } else {
                System.out.println("Selection Bet is NULL");
                log.info("Выбор для ставки равен NULL");
            }
        } else {
            System.out.println("Not first bet....");
            System.out.println("Last bet is " + lastBetFromStatistic.getWin() + " out the block");
            if (lastBetFromStatistic.getWin() != null) {

                Bet newBetOne = null;
                SelectionBet selectionBet = getPossibleSelection();
                if (selectionBet != null) {
                    OddEven oddEven = getOddEvenForBet(getPossibleSelection(), lastBetFromStatistic);
                    if (oddEven != null) {
                        newBetOne = chain.getNextBet(getPossibleSelection(), oddEven, parser, minBet);
                        newBetOne.setMatchPeriod(currentPeriodInt);
                    }

                    if (newBetOne != null && newBetOne.getPrice() > 0.0) { //TODO Here can be problem//
                        log.info("Цена для следующей ставки " + newBetOne.getPrice());

//                        if (isMainMatch && matchGroup.getCopies().size() > 1) {
//                            if (!lastBetFromStatistic.getWin()) {
//                                System.out.println("Прошлая ставка главного матча проиграла, разбиваем следующую ставку: " + newBetOne.getPrice());
//                                log.info("Прошлая ставка главного матча проиграла, разбиваем следующую ставку: " + newBetOne.getPrice());
//                                matchGroup.makePricesForOtherParsers(newBetOne.getPrice(), null);
//                            } else {
//                                System.out.println("Прошлая ставка главного матча выиграла, проверяем все ли биржи поставили.");
//                                log.info("Прошлая ставка главного матча выиграла, проверяем все ли биржи поставили.");
////                                +==================================
////                                        +++=========================
////                              ====================================
//                            }
//                        }
//
//                        if (matchGroup.getCopies().size() > 1 && !lastBetFromStatistic.getWin()) {
//                            Double betPriceForParser = matchGroup.getBetPriceForParser(parser);
//                            System.out.println("Устанавливаем новую цену для ставки: " + betPriceForParser);
//                            log.info("Устанавливаем новую цену для ставки: " + betPriceForParser);
//                            newBetOne.setPrice(betPriceForParser);
//                        }

//                        boolean canMakeBet = true;
//                        if (matchGroup.getCopies().size() > 1) {
//                            for (Integer parserId : matchGroup.getCopies().keySet()) {
//                                if (parserId == parser.getSwitcherId()) {
//                                    continue;
//                                }
//                                if (matchGroup.getMatchLive(parserId).isStartMakeEvent()) {
//                                    canMakeBet = false;
//                                    break;
//                                }
//                            }
//                        }

//                        if (isMainMatch) {
//                            canMakeBet = true;
//                        }
//
//                        if (canMakeBet) {
//                            if (newBetOne.getPrice() > 0) {
//                                log.info("Создана очередная ставка для матча " + nameOfMatch + ". Ставка будет поставлена на" + newBetOne.getSelectionBet() + ". Выбор ставки " + newBetOne.getOddEven() + ". Цена ставки " + newBetOne.getPrice());
//                                makeBet(newBetOne, driver);
//                                if (newBetOne.getTimeBet() > -7200000L) {
//                                    log.info("Ставка для матча " + nameOfMatch + " успешно поставлена и будет добавлена в статистику");
//                                    addBetToMatchStatistic(newBetOne);
//                                }
//                            } else {
//                                System.out.println("Ставки или NULL или цена ставки меньше нуля.");
//                                log.info("Ставки или NULL или цена ставки меньше нуля.");
//                            }
//                        }
                    }
                }
            }
        }
    }

    public Bet makeBet(Bet bet, WebDriver driver) {
        return parser.makeBet(bet, driver, this);
    }

    public void goBackToOverview(WebDriver driver) {
        parser.goBackToOverview(driver);
    }

    public void addBetToMatchStatistic(Bet bet) {

//        if (matchGroup.getCopies().size() > 1) {
//            chain.setCopyChain(true);
//        }

        Integer period = bet.getMatchPeriod();
        System.out.println("Bet period integer is: " + period);
        if (bet.getTimeBet() > -7200000L && bet.getWin() == null) {//add bet
            if (period == currentPeriodInt) {
//                if (matchGroup.getCopies().size() > 1) {
//                    bet.setCopyBetGroupId(matchGroup.getIdGroup());
//                    //подтверждаем, что ставка поставлена на этом сайте.
//                    if (bet.getPrice() > 0) {
//                        matchGroup.confirmSettedBet(parser, bet.getPrice());
//                    }
//                }
                betIsDone = true;
                chain.addBet(bet);
                if (!betStatistic.containsKey(period)) {
                    log.info("Ставка для матча " + nameOfMatch + " успешно добавлена в статистику как новая ставка.");
                    System.out.println("Ставка для матча " + nameOfMatch + " успешно добавлена в статистику как новая ставка.");
                    betStatistic.put(period, bet);
                }
            }
        } else {
            // check bet
            if (bet.getWin()) {
                log.info("Ставка для матча " + bet.getMatchName() + ", период " + bet.getSelectionBet() + " выиграла.");
                System.out.println("Ставка для матча " + bet.getMatchName() + ", период " + bet.getSelectionBet() + " выиграла.");

                //TODO Здесь будут проверены ставки, которые не играли.
//                if (matchGroup.getCopies().size() > 1 && isMainMatch) {
//                    matchGroup.checkPriceWhichWasNotSet(this);
//                }

                if (chain.containBet(bet)) {
                    System.out.println("Матч " + nameOfMatch + " завершил цепочку номер " + chain.getIdChain() + ". последняя ставка была " + bet.getSelectionBet() + " - " + bet.getOddEven());
                    log.info("Матч " + nameOfMatch + " завершил цепочку номер " + chain.getIdChain() + ". последняя ставка была " + bet.getSelectionBet() + " - " + bet.getOddEven());
//                    chain.setWin(true, parser);
                } else {
//                    chain.setWin(true, parser);
                    log.error("Последняя ставка не была добавлена в цепочку.");
                    log.error("Матч " + nameOfMatch + " завершил цепочку номер " + chain.getIdChain() + ". последняя ставка была " + bet.getSelectionBet() + " - " + bet.getOddEven());
                }

                if (!isFinished) {
                    log.info(" Старая цепочка удалена.");
                    System.out.println("Старая цепочка удалена.");
                    if (chain.isWin()) {
                        log.info("Создана новая цепочка");
                        System.out.println("Создана новая цепочка.");
                        chain = new Chain(nameOfMatch, margin);
                    }
                }
            } else {
                if (isFinished) {
                    log.info("Матч " + nameOfMatch + " закончился, текущая цепочка ID " + chain.getIdChain() + " остается в хранилище. Цена следующей ставки должна быть: " + chain.getNextBetPrice(minBet));
                    System.out.println("Chain with ID " + chain.getIdChain() + " - name setted to NULL...");
                    chain.addBet(bet);
//                    if (matchGroup.getCopies().size() == 1) {//if have not copies
//                        chain.setMatchLiveName(null, parser);
//                    } else {
//                        if (isMainMatch) {
//                            chain.setMatchLiveName(null, parser);
//                        } else {
//                            System.out.println("Матч " + nameOfMatch + " завершил цепочку номер " + chain.getIdChain() + ". последняя ставка была " + bet.getSelectionBet() + " - " + bet.getOddEven());
//                            log.info("Матч " + nameOfMatch + " завершил цепочку номер " + chain.getIdChain() + ". последняя ставка была " + bet.getSelectionBet() + " - " + bet.getOddEven());
//                            chain.setMatchLiveName("COPY", parser);
//                            chain.setWin(true, parser);
//                        }
//                    }
//                    StartFrame.getSwitcher(parser, parser.getSwitcherId()).getStorage().addChain(chain);
                } else {
//                    log.info("Ставка для матча " + bet.getMatchName() + ", период " + bet.getSelectionBet() + " проиграла.");
//                    System.out.println("Ставка для матча " + bet.getMatchName() + ", период " + bet.getSelectionBet() + " проиграла.");
//                    if (!chain.isWin()) {
//                        chain.addBet(bet);
//                    } else {
//                        chain = new Chain(nameOfMatch, margin);
//                    }
//                    StartFrame.getSwitcher(parser, parser.getSwitcherId()).getStorage().addChain(chain);
                }
            }
        }

//        StartFrame.getSwitcher(parser, parser.getSwitcherId()).getStorage().addBetToStorage(bet);//save bet to common storage
//        checkCountLooseBet();

        System.out.println("=======Статистика на сайте " + parser.getNameOfSite() + "=======");
        System.out.println("===========" + nameOfMatch + "===============");
        log.info("Статистика для матча =======" + nameOfMatch + ":");
        for (Integer integer : betStatistic.keySet()) {
            log.info("Период: " + integer + ", " +
                    "выиграла ли ставка - " + betStatistic.get(integer).getWin() + ", " +
                    "цена ставки - " + betStatistic.get(integer).getPrice() + ", " +
                    "ставка была на - " + betStatistic.get(integer).getSelectionBet() + ", выбор был - " + betStatistic.get(integer).getOddEven());

            System.out.println("=================================");
            System.out.println("==Period - " + integer + "" +
                    "==is win - " + betStatistic.get(integer).getWin() + "" +
                    "==price is - " + betStatistic.get(integer).getPrice() + "" +
                    "==Selection is - " + betStatistic.get(integer).getSelectionBet());
            System.out.println("=================================");
        }

//        StartFrame.updateInterface(parser);
    }

    public OddEven getOddEvenForBet(SelectionBet selectionBet, Bet lastBet) {
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
//                if (StartFrame.getSwitcher(parser, parser.getSwitcherId()).getStorage().getChain(maxBet) != null) {
//                    System.out.println("Найдена цепочка, пробуем прикрепить к матчу.");
//                    log.info("Найдена цепочка, пробуем прикрепить к матчу.");
//                    if (chain.getList().size() == 0) {
//                        System.out.println("Цепочка прикреплена к матчу.");
//                        log.info("Цепочка прикреплена к матчу.");
//                        chain = StartFrame.getSwitcher(parser, parser.getSwitcherId()).getStorage().getChain(maxBet);
//                        chain.setMatchLiveName(nameOfMatch, parser);
//                        return chain.getLastBetOddEven();
//                    } else {
//                        return OddEven.Odd;
//                    }
//                } else {
//                    log.info("Нет подходящих цепочек, что бы прикрепить к данному матчу.");
//                    System.out.println("Нет подходящих цепочек, что бы прикрепить к данному матчу.");
//                    return OddEven.Odd;//ставим нечет. если в хранилище нет цепочек
//                }
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
//                    if (lastBet.getSelectionBet() == SelectionBet.Q2) {// последняя ставка была на вторую четверть.
//                        if (lastBet.getWin()) {// если выигарала
//                            return lastBet.getOtherOddEven();// ставим на противоположный выбор
//                        } else {//если проиграла
//                            return lastBet.getOddEven();// ставим на тот же выбор
//                        }
//                    } else if (lastBet.getSelectionBet() == SelectionBet.Half1st) {// последняя ставка была на первую половину
//                        Integer secondQuarterScore = matchScoreStatistic.get(SelectionBet.Q2);
//                        if (lastBet.getWin()) {// если выиграла смотрим на результат второй четверти и ставим противоположный
//                            if (secondQuarterScore % 2 == 0) { //результат чет.
//                                return OddEven.Odd;//возвращаем нечет.
//                            } else {
//                                return OddEven.Even;
//                            }
//                        } else {//если ставка проиграла, смотрим на результат второй четверти, и ставим такой же.
//                            if (secondQuarterScore % 2 == 0) {//ставка была чет.
//                                return OddEven.Odd;  //возвращаем чет.
//                            } else {//ставка была нечет.
//                                return OddEven.Even;// возвращаем нечет.
//                            }
//                        }
//                    } else {//в случае если последняя ставка была на первую четверть.
//                        Integer secondQuarterScore = matchScoreStatistic.get(SelectionBet.Q2);
//                        if (lastBet.getWin()) {//если выиграла, смотрим на вторую четверть, которая ставилась бы  четная.
//                            if (secondQuarterScore % 2 == 0) { // если результат чет., все по плану ставка могла бы выиграть,
//                                return OddEven.Odd;//возвращаем нечет.
//                            } else {// если результат нечет, наша ставка проиграла бы, и нужно было бы возвращать чет снова
//                                return OddEven.Even;// возвращаем чет.
//                            }
//                        } else {//если ставка проиграла, смотрим на результат второй четверти, которая ставилась бы нечет.
//                            if (secondQuarterScore % 2 == 0) { // если результат чет., ставка проиграла бы, и нужно было бы снова ставить на нечет.
//                                return OddEven.Odd;//возвращаем нечет
//                            } else {// если результат нечет, наша ставка выиграла, и ставим чет на третью четверть.
//                                return OddEven.Even;// возвращаем чет.
//                            }
//                        }
//                    }
                }

                if (selectionBet == SelectionBet.Q4) {
                    Integer thirdQuarterScore = matchScoreStatistic.get(SelectionBet.Q3);
                    if (thirdQuarterScore % 2 == 0) {//если третья четверть была чет. ставка выиграла бы
                        return OddEven.Odd;// и четвертую ставим на нечет
                    } else {//если третья четверть нечет, ставка проиграла бы
                        return OddEven.Even;// и четвертую снова ставим на чет.
                    }
//                    if (lastBet.getSelectionBet() == SelectionBet.Q3) {
//                        if (lastBet.getWin()) {
//                            return lastBet.getOtherOddEven();
//                        } else {
//                            return lastBet.getOddEven();
//                        }
//                    } else if (lastBet.getSelectionBet() == SelectionBet.Half1st) {
//                        Integer thirdQuarterScore = matchScoreStatistic.get(SelectionBet.Q3);
//                        switch (lastBet.getOddEven()) {
//                            case Odd: {
//                                if (lastBet.getWin()) {//если первая половина ставила на нечет, и выиграла(вторая была чет), смотрим на третью четверть,ставили бы на нечет.
//                                    if (thirdQuarterScore % 2 == 0) {//если ставка чет. ставка проиграла бы
//                                        return OddEven.Odd; // и снова ставили бы на нечет четвертой
//                                    } else {//если ставка нечет. ставка выиграла бы
//                                        return OddEven.Even;// и ставили бы в четвертой четверти на чет.
//                                    }
//                                } else {//если первая половина матча ставина на нечет, и проиграла(вторая была нечет.), смотри на третью четверть, ставили бы на снова на нечет(как должно было быть во второй).
//                                    if (thirdQuarterScore % 2 == 0) {//если ставка чет. ставка проиграла бы
//                                        return OddEven.Odd; // и снова ставили бы на нечет в четвертой
//                                    } else {//если ставка нечет. ставка выиграла бы
//                                        return OddEven.Even;// и ставили бы в четвертой четверти на чет.
//                                    }
//                                }
//                            }
//                            case Even: {
//                                if (lastBet.getWin()) {//если первая половина ставила на чет, и выиграла(вторая была нечет), смотрим на третью четверть, ставили бы на чет.
//                                    if (thirdQuarterScore % 2 == 0) {//если ставка чет. ставка выиграла бы
//                                        return OddEven.Odd; // и ставили бы на нечет в четвертой
//                                    } else {//если ставка нечет. ставка проиграла бы
//                                        return OddEven.Even;// и снова ставили бы в четвертой четверти на чет.
//                                    }
//                                } else {//если первая половина матча ставина на чет, и проиграла(вторая была чет.), смотрим на третью четверть, ставили бы на снова на нечет (как должно было быть во второй).
//                                    if (thirdQuarterScore % 2 == 0) {//если ставка чет. ставка проиграла бы
//                                        return OddEven.Odd; // и снова ставили бы на нечет в четвертой
//                                    } else {//если ставка нечет. ставка выиграла бы
//                                        return OddEven.Even;// и ставили бы в четвертой четверти на чет.
//                                    }
//                                }
//                            }
//                        }
//                    } else if (lastBet.getSelectionBet() == SelectionBet.Q2) {
//                        Integer thirdQuarterScore = matchScoreStatistic.get(SelectionBet.Q3);
//                        switch (lastBet.getOddEven()) {
//                            case Odd: {//если прошлая четверть ставила на нечет.
//                                if (lastBet.getWin()) {//и выиграла, смотрим на третью четверть, ставили бы ее на чет.
//                                    if (thirdQuarterScore % 2 == 0) {//если третья четверть была чет. ставка выиграла бы
//                                        return OddEven.Odd;// и четвертую ставим на нечет
//                                    } else {//если третья четверть нечет, ставка проиграла бы
//                                        return OddEven.Even;// и четвертую снова ставим на чет.
//                                    }
//                                } else { // если ставка на вторую четверть проиграла, смотрим на третью четверть, ставили бы на нечет.
//                                    if (thirdQuarterScore % 2 == 0) {//если третья четверть была чет. ставка проиграла бы
//                                        return OddEven.Odd;// и четвертую снова ставим на нечет
//                                    } else {//если третья четверть нечет, ставка выиграла бы
//                                        return OddEven.Even;// и четвертую ставим на чет.
//                                    }
//                                }
//                            }
//                            case Even: {//если прошлая ставка ставила на чет
//                                if (lastBet.getWin()) {//и выиграла, смотрим на третью четверть, ставили бы ее на нечет.
//                                    if (thirdQuarterScore % 2 == 0) {//если третья четверть была чет. ставка проиграла бы
//                                        return OddEven.Odd;// и четвертую ставим снова на нечет
//                                    } else {//если третья четверть нечет, ставка выиграла бы
//                                        return OddEven.Even;// и четвертую ставим на чет.
//                                    }
//                                } else { // если ставка на вторую четверть проиграла, смотрим на третью четверть, ставили бы на чет снова.
//                                    if (thirdQuarterScore % 2 == 0) {//если третья четверть была чет. ставка выиграла бы
//                                        return OddEven.Odd;// и четвертую ставим на нечет
//                                    } else {//если третья четверть нечет, ставка снова проиграла бы
//                                        return OddEven.Even;// и четвертую снова ставим на чет.
//                                    }
//                                }
//                            }
//                        }
//                    } else if (lastBet.getSelectionBet() == SelectionBet.Q1) {
//                        Integer thirdQuarterScore = matchScoreStatistic.get(SelectionBet.Q3);
//                        if (thirdQuarterScore % 2 == 0) {
//                            return OddEven.Odd;
//                        } else {
//                            return OddEven.Even;
//                        }
//                    }
                }
            } else {
                System.out.println("LAST BET IS NUUUUUUUUULL");
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

    public void checkCountLooseBet() {
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
        this.currentPeriod = currentPeriod;
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

        if (currentPeriodInt != oldCurrentPeriod) {
            betIsDone = false;
        }
    }

    public Bet getLastBetFromStatistic() {
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

    public int getCountLoose() {
        checkCountLooseBet();
        return countLoose;
    }

    public SelectionBet getPossibleSelection() {
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

    public void setChain(Chain chain) {
        this.chain = chain;
        if (chain != null) {
            chain.setMatchLiveName(nameOfMatch, parser);
//            StartFrame.updateInterface(parser);
        } else {
            System.out.println("Storage return a NULL as chain for match " + nameOfMatch + "...");
        }
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
        System.out.println("Установлена максимальная ставка для матча " + getNameOfMatch() + ". равна - " + maxBet);
        log.info("Установлена максимальная ставка для матча " + getNameOfMatch() + ". равна - " + maxBet);
//        if (matchGroup.getCopies().size() > 1) {
//            if (maxBet > 0.0) {
//                matchGroup.setMaxBet(maxBet);
//            }
//        } else {
//            this.maxBet = maxBet;
//        }
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

    public Chain getChain() {
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

    public Parser getParser() {
        return parser;
    }

    public boolean isStartMakeEvent() {
        return startMakeEvent;
    }

    public double getMargin() {
        return margin;
    }

    public boolean isMainMatch() {
        return isMainMatch;
    }

    public void setMainMatch(boolean mainMatch) {
        isMainMatch = mainMatch;
    }

    public void setDeleteThisMatch(boolean deleteThisMatch) {
        this.deleteThisMatch = deleteThisMatch;
    }

    public MatchGroup getMatchGroup() {
        return matchGroup;
    }

    public void setMatchGroup(MatchGroup matchGroup) {
        this.matchGroup = matchGroup;
    }

    public boolean isAlmostFinish() {
        return almostFinish;
    }

    public void setAlmostFinish(boolean almostFinish) {
        this.almostFinish = almostFinish;
    }
}
