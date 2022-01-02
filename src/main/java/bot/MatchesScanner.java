package bot;

import betting.entity.Bet;
import entityes.MatchGroup;
import entityes.MatchLive;
import org.apache.log4j.Logger;
import parser.Parser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchesScanner {

    private static Logger log = Logger.getLogger(MatchesScanner.class);
    private static MatchesScanner matchesScanner;
    private Map<Integer, Map<Integer, MatchLive>> allMatches;

    private MatchesScanner() {
        allMatches = new HashMap<>();
    }

    public static MatchesScanner getMatchesScanner() {
        if (matchesScanner == null) {
            matchesScanner = new MatchesScanner();
            return matchesScanner;
        } else {
            return matchesScanner;
        }
    }

    public MatchLive getMatchLive(Integer parserId, Integer integer) {
        return allMatches.get(parserId).get(integer);
    }

    public boolean makeEventForMatch(MatchLive matchLive) {

        log.info("Создаем паралельное событие для матча " + matchLive.getNameOfMatch() + " на сайте номер " + matchLive.getParser().getSwitcherId());
        System.out.println("Создаем паралельное событие для матча " + matchLive.getNameOfMatch() + " на сайте номер " + matchLive.getParser().getSwitcherId());

        if(matchLive.isMainMatch()){
            System.out.println("Это главный матч, возвращаем управление матчу");
            log.info("Это главный матч, возвращаем управление матчу");
            return true;
        }

        if (matchLive.isFinished()) {
            Bet lastBetFromStatistic = matchLive.getLastBetFromStatistic();
            if(lastBetFromStatistic!=null){
                if(lastBetFromStatistic.getPrice()<0){
                    lastBetFromStatistic.setWin(true);
                }
            }

            System.out.println("Матч уже закончился, возвращаем управление матчу");
            log.info("Матч уже закончился, возвращаем управление матчу");
            return true;
        }

        Map<Integer, Integer> copies = null;// matchLive.getMatchGroup().getCopies();
        MatchLive matchLiveAnother;

        MatchLive mainMatch = null;
        //Проверяем есть ли главный матч
        for (Integer parserId : copies.keySet()) {
            if (parserId != matchLive.getParser().getSwitcherId()) {
                matchLiveAnother = allMatches.get(parserId).get(copies.get(parserId));
            } else {
                continue;
            }

            if (matchLiveAnother.isStartMakeEvent()) {
                System.out.println("Один из матчей начал делать событие.");
                log.info("Один из матчей начал делать событие.");
                return false;
            }
            if (matchLiveAnother.isMainMatch()) {
                mainMatch = matchLiveAnother;
                System.out.println("Нашли главный матч " + mainMatch.getNameOfMatch() + ", на сайте номер " + mainMatch.getParser().getSwitcherId());
                log.info("Нашли главный матч " + mainMatch.getNameOfMatch() + ", на сайте номер " + mainMatch.getParser().getSwitcherId());
                break;
            }
        }

        //Если не нашли главный матч смотрим есть ли матч который уже поставил ставку.




        if (mainMatch == null) {
//            matchLive.getMatchGroup().findMainMatch();

            for (Integer parserId : copies.keySet()) {
                if (parserId != matchLive.getParser().getSwitcherId()) {
                    matchLiveAnother = allMatches.get(parserId).get(copies.get(parserId));
                } else {
                    continue;
                }
                if (matchLiveAnother.isMainMatch()) {
                    mainMatch = matchLiveAnother;
                    System.out.println("Нашли главный матч " + mainMatch.getNameOfMatch() + ", на сайте номер " + mainMatch.getParser().getSwitcherId());
                    log.info("Нашли главный матч " + mainMatch.getNameOfMatch() + ", на сайте номер " + mainMatch.getParser().getSwitcherId());
                    break;
                }
            }
//            for (Integer parserId : copies.keySet()) {
//                if (parserId != matchLive.getParser().getSwitcherId()) {
//                    matchLiveAnother = allMatches.get(parserId).get(copies.get(parserId));
//                } else {
//                    continue;
//                }
//                //Если есть, делаем этот матч главным.
//                if (matchLiveAnother.getBetStatistic().size()>0) {
//                    mainMatch = matchLiveAnother;
//                    mainMatch.setMainMatch(true);
//                    System.out.println("Нашли главный матч, вторая проверка " + mainMatch.getNameOfMatch() + ", на сайте номер " + mainMatch.getParser().getSwitcherId());
//                    log.info("Нашли главный матч, вторая проверка " + mainMatch.getNameOfMatch() + ", на сайте номер " + mainMatch.getParser().getSwitcherId());
//                    break;
//                }
//            }
        }



        Bet betCurrentMatch;
        Bet lastMainBet;
        //Если нет еще матчей, которые поставили ставку уже, возвращаем управление матчу, пускай ставит ставку.
        if (mainMatch != null) {
            //первая ставка в матче.
            if (matchLive.getBetStatistic().size() == 0) {
                System.out.println("Статистика матча пуста. Будет добавлена первая ставка в матче.");
                log.info("Статистика матча пуста. Будет добавлена первая ставка в матче.");
                if (mainMatch.getCurrentPeriodInt() == matchLive.getCurrentPeriodInt()) {
                    lastMainBet = mainMatch.getBetStatistic().get(mainMatch.getCurrentPeriodInt());//возьмем ставку за этот период, и скопируем себе.
                    if (lastMainBet != null) {
                        betCurrentMatch = new Bet();
                        betCurrentMatch.setMatchName(matchLive.getNameOfMatch());
                        betCurrentMatch.setTimeBet(lastMainBet.getTimeBet());
                        betCurrentMatch.setMatchPeriod(lastMainBet.getMatchPeriod());
                        betCurrentMatch.setSelectionBet(lastMainBet.getSelectionBet());
                        betCurrentMatch.setOddEven(lastMainBet.getOddEven());
                        betCurrentMatch.setPrice(-1.0);
                        matchLive.addBetToMatchStatistic(betCurrentMatch);
                        log.info("Скопировали в текущий матч ставку с главного матча, с ценой -1,0");
                        System.out.println("Скопировали в текущий матч ставку с главного матча, с ценой -1,0");
                        return false;
                    } else {
                        System.out.println("Ставка на текущий период в главном матче равна - NULL");
                        log.info("Ставка на текущий период в главном матче равна - NULL");
                        return false;
                    }
                } else {
                    System.out.println("Периоды главного матча и текущего матча не равны. Ждем когда программа обработает матч.");
                    log.info("Периоды главного матча и текущего матча не равны. Ждем когда программа обработает матч.");
                    return false;
                }
            } else {
                System.out.println("Статистика текущего матча не пуста. Определяем дальнейшие действиия.");
                log.info("Статистика текущего матча не пуста. Определяем дальнейшие действиия.");
                betCurrentMatch = matchLive.getLastBetFromStatistic();//Последняя ставка текущего матча.
                lastMainBet = mainMatch.getBetStatistic().get(betCurrentMatch.getMatchPeriod());//Ставка на эту же четверть главного матча.
                if (lastMainBet.getWin() != null) {
                    betCurrentMatch.setWin(lastMainBet.getWin());
                    matchLive.addBetToMatchStatistic(betCurrentMatch);
                    if (mainMatch.getBetStatistic().get(mainMatch.getCurrentPeriodInt()) != null) {
                        if (lastMainBet.getWin()) {
                            System.out.println("Прошлая ставка выиграла, копируем в текущий матч ставку с главного матча с ценой -1,0.");
                            log.info("Прошлая ставка выиграла, копируем в текущий матч ставку с главного матча с ценой -1,0.");
                            lastMainBet = mainMatch.getBetStatistic().get(mainMatch.getCurrentPeriodInt());
                            betCurrentMatch = new Bet();
                            betCurrentMatch.setMatchName(matchLive.getNameOfMatch());
                            betCurrentMatch.setTimeBet(lastMainBet.getTimeBet());
                            betCurrentMatch.setMatchPeriod(lastMainBet.getMatchPeriod());
                            betCurrentMatch.setSelectionBet(lastMainBet.getSelectionBet());
                            betCurrentMatch.setOddEven(lastMainBet.getOddEven());
                            betCurrentMatch.setPrice(-1.0);
                            matchLive.addBetToMatchStatistic(betCurrentMatch);
                            return false;
                        } else {
                            System.out.println("Последняя ставка проиграла, главный матч уже поставил ставку, и расчитал возможные суммы ставок. Возвращаем управление матчу для совершениия ставки");
                            log.info("Последняя ставка проиграла, главный матч уже поставил ставку, и расчитал возможные суммы ставок. Возвращаем управление матчу для совершениия ставки");
                            return true;
                        }
                    }else {
                        System.out.println("Главный матч еще не поставил ставку, и не расчитал возможные ставки для других бирж. Ждем пока главный матч сделает это");
                        log.info("Главный матч еще не поставил ставку, и не расчитал возможные ставки для других бирж. Ждем пока главный матч сделает это");
                        return false;
                    }
                } else {
                    System.out.println("Последняя ставка еще не проверена, ждем когда главный матч проверит ставку.");
                    log.info("Последняя ставка еще не проверена, ждем когда главный матч проверит ставку.");
                    return false;
                }
            }
        } else {
            //Главный матч так и не нашли, возвращаем управление текущему матчу
            System.out.println("Еще нет главных матчей, возвращем управление текущему матчу");
            log.info("Еще нет главных матчей, возвращем управление текущему матчу");
            return true;
        }
    }

    void checkMatch(MatchLive matchLive, Parser parser) {

        double totalMaxBet;
        double maxBetOfOneMatch = 0.0;
        if (matchLive.getMaxBet() != 0.0) {
            maxBetOfOneMatch = matchLive.getMaxBet();
        }

        for (Integer parserId : allMatches.keySet()) {
            if (parserId == parser.getSwitcherId()) {
                continue;
            }

            Map<Integer, MatchLive> integerMatchLiveMap = allMatches.get(parserId);

            for (Integer integer : integerMatchLiveMap.keySet()) {
                MatchLive matchLive1 = integerMatchLiveMap.get(integer);
                boolean b = compareMatch(matchLive1, matchLive);
                if (b) {
                    log.info("Найдена копия матчей. Матч " + matchLive.getNameOfMatch() + " на сайте номер " + matchLive.getParser().getSwitcherId()
                            + " и матч " + matchLive1.getNameOfMatch() + " на сайте номер " + matchLive1.getParser().getSwitcherId());
                    System.out.println("Найдена копия матчей. Матч " + matchLive.getNameOfMatch() + " на сайте номер " + matchLive.getParser().getSwitcherId()
                            + " и матч " + matchLive1.getNameOfMatch() + " на сайте номер " + matchLive1.getParser().getSwitcherId());

                    MatchGroup matchGroup = matchLive1.getMatchGroup();
//                    matchGroup.addMatch(matchLive.getParser(), matchLive.getNameOfMatch().hashCode());
//                    int idGroup = matchGroup.getIdGroup();
//                    matchLive.setMatchGroup(matchGroup);

//                    System.out.println("Матч " + matchLive.getNameOfMatch() + " с сайта номер " + matchLive.getParser().getSwitcherId() + " добавлен в группу " + idGroup);
                }
            }
        }

        allMatches.computeIfAbsent(parser.getSwitcherId(), k -> new HashMap<Integer, MatchLive>());
        allMatches.get(parser.getSwitcherId()).put(matchLive.getNameOfMatch().hashCode(), matchLive);

//        if (maxBetOfOneMatch != 0.0) {
//            totalMaxBet = maxBetOfOneMatch * matchLive.getMatchGroup().getCopies().size();
//            System.out.println("Максимальная ставка для одного матча равна - " + maxBetOfOneMatch);
//            log.info("Максимальная ставка для одного матча равна - " + maxBetOfOneMatch);
//            System.out.println("Максимальная ставка для группы матчей " + matchLive.getNameOfMatch() + ". равна - " + totalMaxBet);
//            log.info("Максимальная ставка для группы матчей " + matchLive.getNameOfMatch() + ". равна - " + totalMaxBet);
//            for (Integer parserId : matchLive.getMatchGroup().getCopies().keySet()) {
//                Map<Integer, MatchLive> integerMatchLiveMap = allMatches.get(parserId);
//                if (integerMatchLiveMap != null) {
//                    MatchLive matchLive1 = integerMatchLiveMap.get(matchLive.getMatchGroup().getCopies().get(parserId));
//                    if (matchLive1 != null) {
//                        matchLive1.setMaxBetForGroup(totalMaxBet);
//                    }
//                }
//            }
//        }

    }

    private boolean compareMatch(MatchLive matchLiveFirst, MatchLive matchLiveSecond) {

        boolean copies;

        String[] splitNamesFirstMatch = matchLiveFirst.getNameOfMatch().split(" vs ");
        String[] splitNamesSecondMatch = matchLiveSecond.getNameOfMatch().split(" vs ");

        String firstMatchFirstTeam = null;
        String firstMatchSecondTeam = null;
        if (splitNamesFirstMatch.length > 1) {
            firstMatchFirstTeam = splitNamesFirstMatch[0];
            firstMatchSecondTeam = splitNamesFirstMatch[1];
        }

        String secondMatchFirstTeam = null;
        String secondMatchSecondTeam = null;
        if (splitNamesSecondMatch.length > 1) {
            secondMatchFirstTeam = splitNamesSecondMatch[0];
            secondMatchSecondTeam = splitNamesSecondMatch[1];
        }
        List<Boolean> list = new ArrayList<>();

        list.add(compareMatchNames(firstMatchFirstTeam, secondMatchFirstTeam));
        list.add(compareMatchNames(firstMatchSecondTeam, secondMatchSecondTeam));
        list.add(matchLiveFirst.getCurrentPeriod() == matchLiveSecond.getCurrentPeriod());
        int trueInt = 0;
        int falseInt = 0;
        for (Boolean b : list) {
            if (b) {
                trueInt++;
            } else {
                falseInt++;
            }
        }

        System.out.println("Count true is " + trueInt + ", Count false is: " + falseInt);

        if (trueInt == 3) {
            copies = true;
        } else {
            copies = false;
        }

        return copies;
    }

    public static boolean compareMatchNames(String firstMatchTeamName, String secondMatchTeamName) {

        boolean equal = false;
        if (firstMatchTeamName == null || secondMatchTeamName == null) {
            return equal;
        }

        firstMatchTeamName = correctString(firstMatchTeamName);
        secondMatchTeamName = correctString(secondMatchTeamName);


        System.out.println("Первая команда " + firstMatchTeamName);
        System.out.println("Вторая команда " + secondMatchTeamName);

        List<Boolean> list = new ArrayList<>();

        String[] splitFirstTeam = firstMatchTeamName.split(" ");
        String[] splitSecondTeam = secondMatchTeamName.split(" ");

        int numberSplitFirst = 0;
        for (int i = 0; i < splitFirstTeam.length; i++) {//take the biggest word from first name, and compare it to second one
//            if (splitFirstTeam[i].contains("-")) {
//                splitFirstTeam[i] = splitFirstTeam[i].split("-")[0] + splitFirstTeam[i].split("-")[1];
//            }
//            System.out.println("Compare '" + secondMatchTeamName + "' and '" + splitFirstTeam[i] + "'");
            if (secondMatchTeamName.contains(splitFirstTeam[i])) {
//                System.out.println("result is TRUE");
                list.add(true);
            } else {
//                System.out.println("result is FALSE");
                list.add(false);
            }
        }

        for (int i = 0; i < splitSecondTeam.length; i++) {//take the biggest word from first name, and compare it to second one
//            if (splitSecondTeam[i].contains("-")) {
//                splitSecondTeam[i] = splitSecondTeam[i].split("-")[0] + splitSecondTeam[i].split("-")[1];
//            }

//            System.out.println("Compare '" + firstMatchTeamName + "' and '" + splitSecondTeam[i] + "'");
            if (firstMatchTeamName.contains(splitSecondTeam[i])) {
                list.add(true);
//                System.out.println("result is TRUE");
            } else {
//                System.out.println("result is False");
                list.add(false);
            }
        }

        int trueInt = 0;
        int falseInt = 0;
        for (Boolean b : list) {
            if (b) {
                trueInt++;
            } else {
                falseInt++;
            }
        }

        if (trueInt > falseInt) {
            equal = true;
        }

        return equal;
    }

    private static String correctString(String oldString) {
        if (oldString.contains(" (Univ-w)")) {
            oldString = oldString.replace("(Univ-w)", "");
        } else if (oldString.contains(" Uni. Women")) {
            oldString = oldString.replace("Uni. Women", "");
        } else if (oldString.contains(" W (univ)")) {
            oldString = oldString.replace("W (univ)", "");
        } else if (oldString.contains(" (univ)")) {
            oldString = oldString.replace("(univ)", "");
        } else if (oldString.contains("(Univ.)")) {
            oldString = oldString.replace("(Univ.)", "");
        } else if (oldString.contains(" Uni")) {
            oldString = oldString.replace("Uni", "");
        } else if (oldString.contains(" Uni.")) {
            oldString = oldString.replace("Uni.", "");
        } else if (oldString.contains("(w)")) {
            oldString = oldString.replace("(w)", "");
        } else if (oldString.contains(" W ")) {
            oldString = oldString.replace(" W", "");
        } else if (oldString.contains("U-16")) {
            oldString = oldString.replace("U-16", "");
        } else if (oldString.contains("U16")) {
            oldString = oldString.replace("U16", "");
        } else if (oldString.contains("(deaf)")) {
            oldString = oldString.replace("(deaf)", "");
        } else if (oldString.contains("Women")) {
            oldString = oldString.replace("Women", "");
        }else if(oldString.contains("-")){
            oldString = oldString.replace("-", "");
        }

        //Sweden (Univ-w) vs Chinese Taipei (Univ-w)
        //Spain U-20 (deaf) - Lithuania U-20 (deaf)
        //  Chinese Taipei (univ) vs Hungary (univ)  ||Chinese Taipei W (univ) vs Chile W (univ)
        //  Chinese Taipei (Univ.) vs Hungary (Univ.) ||Chinese Taipei (Univ-w) vs Chile (Univ-w)||
        //  Chinese Taipei Uni vs Hungary Uni.       ||Chinese Taipei Uni. Women vs Chile Uni. Women
        // Israel U-16 (w)     | Israel W U16    |
        // Hungary (Univ-w)    | Hungary W(univ) | Hungary Uni. Woman
        // Germany (Univ)      | Germany (univ)  | Germany Uni
        // Albania U-16 (w)    | Albania W U16   |

        return oldString;
    }
}
