package entityes;

public class MatchGroup {

//    private static Logger log = Logger.getLogger(MatchGroup.class);
//
//    private static int idAllGroup;
//    private int idGroup;
//    private Map<Integer, Integer> copies;
//    private Map<Integer, Double> priceForBetsForOtherParser;
//
//    public MatchGroup(MatchLive matchLive) {
//        idGroup = ++idAllGroup;
//        copies = new HashMap<>();
//        priceForBetsForOtherParser = new HashMap<>();
//        addMatch(matchLive.getParser(), matchLive.getNameOfMatch().hashCode());
//    }
//
//    public void addMatch(Parser parser, Integer integer) {
//        boolean containCopy = true;
//        for (Integer number : copies.keySet()) {
//            if (number == parser.getSwitcherId()) {
//                containCopy = false;
//                break;
//            }
//        }
//
//        if (containCopy) {
//            copies.put(parser.getSwitcherId(), integer);
//        }
//        StartFrame.updateInterface(parser);
//    }
//
//    public List<Integer> clearPriceForOtherParser() {
//        log.info("Удаляем все элементы с коллекции ставок для других сайтов.");
//        List<Integer> list = new ArrayList<>();
//        for (Integer parserId : priceForBetsForOtherParser.keySet()) {
//            if (priceForBetsForOtherParser.get(parserId) == -2.0) {
//                list.add(parserId);
//            }
//        }
//        priceForBetsForOtherParser.clear();
//        return list;
//    }
//
//    public void setMaxBet(Double maxBet) {
//        MatchLive matchLive;
//        maxBet = maxBet * copies.size();
//        for (Integer parser : copies.keySet()) {
//            matchLive = MatchesScanner.getMatchesScanner().getMatchLive(parser, copies.get(parser));
//            matchLive.setMaxBetForGroup(maxBet);
//        }
//    }
//
//    public MatchLive getMatchLive(Integer parserId) {
//        Integer integer = copies.get(parserId);
//        MatchLive matchLive = MatchesScanner.getMatchesScanner().getMatchLive(parserId, integer);
//        return matchLive;
//    }
//
//    public void findMainMatch() {
//
//        System.out.println("Ищем главный матч для группы матчей " + idGroup);
//        log.info("Ищем главный матч для группы матчей " + idGroup);
//        int maxSize = 0;
//        Integer maxSizeParserNumber = null;
//
//        for (Integer integer : copies.keySet()) {
//            MatchLive matchLive = MatchesScanner.getMatchesScanner().getMatchLive(integer, copies.get(integer));
//            int size = matchLive.getBetStatistic().size();
//            if (size > maxSize) {
//                maxSize = size;
//                maxSizeParserNumber = integer;
//            }else if(size==maxSize) {
//                maxSizeParserNumber = null;
//            }
//        }
//
//        System.out.println("Главный матч поставил ставок: " + maxSize + ". Номер главного матча " + maxSizeParserNumber);
//        log.info("Главный матч поставил ставок: " + maxSize + ". Номер главного матча " + maxSizeParserNumber);
//        if (maxSizeParserNumber != null) {
//            for (Integer integer : copies.keySet()) {
//                MatchesScanner.getMatchesScanner().getMatchLive(integer, copies.get(integer)).setMainMatch(false);
//            }
//            MatchesScanner.getMatchesScanner().getMatchLive(maxSizeParserNumber,copies.get(maxSizeParserNumber)).setMainMatch(true);
//        }
//    }
//
//    public void makePricesForOtherParsers(Double betPrice, Parser parserWhichMadeBet) {
//
//        List<Integer> list = clearPriceForOtherParser();
//
//        if (parserWhichMadeBet == null) {
//            list.clear();
//        }
//
//        Map<Integer, Integer> myCopies = new HashMap<>();
//        for (Integer parserId : copies.keySet()) {
//            if (parserWhichMadeBet != null) {
//                if (parserId == parserWhichMadeBet.getSwitcherId()) {
//                    continue;
//                }
//            }
//
//            if (list.contains(parserId)) {
//                continue;
//            }
//            myCopies.put(parserId, copies.get(parserId));
//        }
//
//        priceForBetsForOtherParser.put(null, betPrice);
//
//        Double possibleBet = Math.round((betPrice / myCopies.size()) * 100.0) / 100.0;
//        if (possibleBet < StartFrame.getMinBet()) {
//            possibleBet = Math.round((betPrice / (myCopies.size() - 1)) * 100.0) / 100.0;
//            if (possibleBet < StartFrame.getMinBet()) {
//                possibleBet = Math.round((betPrice / (myCopies.size() - 2)) * 100.0) / 100.0;
//                if (possibleBet < StartFrame.getMinBet()) {
//                    possibleBet = Math.round((betPrice / (myCopies.size() - 3)) * 100.0) / 100.0;
//                    if (possibleBet < StartFrame.getMinBet()) {
//                        possibleBet = Math.round((betPrice / (myCopies.size() - 4)) * 100.0) / 100.0;
//                    }
//                }
//            }
//        }
//
//        for (Integer parserId : myCopies.keySet()) {
//            priceForBetsForOtherParser.put(parserId, possibleBet);
//        }
//
//        if (parserWhichMadeBet != null) {
//            priceForBetsForOtherParser.put(parserWhichMadeBet.getSwitcherId(), -2.0);
//        }
//
//        for (Integer parserId : list) {
//            priceForBetsForOtherParser.put(parserId, -2.0);
//        }
//
//        System.out.println("Ставка " + priceForBetsForOtherParser.get(null) + " , будет разбита следующим образом: ");
//        log.info("Ставка " + priceForBetsForOtherParser.get(null) + " , будет разбита следующим образом: ");
//        for (Integer parserId : priceForBetsForOtherParser.keySet()) {
//            if (parserId == null) {
//                continue;
//            }
//            System.out.println("На сайте номер " + parserId + " может быть поставлено: " + priceForBetsForOtherParser.get(parserId));
//            log.info("На сайте номер " + parserId + " может быть поставлено: " + priceForBetsForOtherParser.get(parserId));
//        }
//        System.out.println("======================================================================");
//        log.info("======================================================================");
//    }
//
//
//
//
//    public synchronized void confirmSettedBet(Parser parser, Double betPrice) {
//        if (priceForBetsForOtherParser.get(parser.getSwitcherId()) == null) {
//            System.out.println("Нул передали в качестве агрумента при подтверждении ставки.");
//            return;
//        }
//        Double totalBet = priceForBetsForOtherParser.get(null);
//        makePricesForOtherParsers(Math.round((totalBet - betPrice) * 100.0) / 100.0, parser);
//    }
//
//    public void checkPriceWhichWasNotSet(MatchLive mainMatch) {
//        System.out.println("Проверяем все ли ставки были поставлены на паралельных событиях.");
//        log.info("Проверяем все ли ставки были поставлены на паралельных событиях.");
//        boolean doNotBet = false;
//        Integer parserWithOutBetFirst = null;
//        Integer parserWithOutBetSecond = null;
//        Integer parserWithOutBetThird = null;
//        Integer parserWithOutBetFourth = null;
//        for (Integer parserId : priceForBetsForOtherParser.keySet()) {
//            if (parserId != null) {
//                if (priceForBetsForOtherParser.get(parserId) != 0.0) {
//                    if (parserWithOutBetFirst == null) {
//                        parserWithOutBetFirst = parserId;
//                    } else if (parserWithOutBetSecond == null) {
//                        parserWithOutBetSecond = parserId;
//                    } else if (parserWithOutBetThird == null) {
//                        parserWithOutBetThird = parserId;
//                    } else if (parserWithOutBetFourth == null) {
//                        parserWithOutBetFourth = parserId;
//                    }
//                    doNotBet = true;
//                    break;
//                }
//            }
//        }
//        if (doNotBet) {
//            List<Integer> list = new ArrayList<>();
//            list.add(parserWithOutBetFirst);
//            list.add(parserWithOutBetSecond);
//            list.add(parserWithOutBetThird);
//            list.add(parserWithOutBetFourth);
//
////            MatchLive matchLive = null;
////            for (Integer parserId : copies.keySet()) {
////                Integer integer = copies.get(parserId);
////                try {
////                    matchLive = StartFrame.getSwitcher(BettingSetting.parser,parserId).getStorage().getCurrentMatches().get(integer);//TODO NULL POINTER EXCEPTION
////                } catch (NullPointerException e) {
////                    e.printStackTrace();
////                    System.out.println("ПРоблема при создании цепочки, если один из матчей не поставил ставку.");
////                    log.info("ПРоблема при создании цепочки, если один из матчей не поставил ставку. MatchGroup.checkPriceWhichWasNotSet()");
////                    continue;
////                }
////
////                if(matchLive!=null){
////                    if (matchLive.isMainMatch()) {//TODO Null Exception
////                        break;
////                    }
////                }
////            }
//
//            if (mainMatch != null) {
//                for (Integer parserId : list) {
//                    if (parserId != null) {
//                        System.out.println("На сайте номер " + parserId + " не была поставлена ставка");
//                        log.info("На сайте номер " + parserId + " не была поставлена ставка");
//                        Chain chain = mainMatch.getChain();
//                        Chain chainForSave = new Chain("WrongBet", mainMatch.getMargin());
//                        for (int i = 0; i < chain.getList().size() - 1; i++) {
//                            chainForSave.addBet(chain.getList().get(i));
//                        }
//                        StartFrame.getSwitcher(BettingSetting.parser, parserId).getStorage().addChain(chainForSave);
//                    }
//                }
//            }
//        }
//    }
//
//    public synchronized Double getBetPriceForParser(Parser parser) {
//        Double totalPriceOfNextBet = priceForBetsForOtherParser.get(null);
//        log.info("Берем цену для сайта номер " + parser.getSwitcherId() + " . Для ставки " + totalPriceOfNextBet);
//        System.out.println("Берем цену для сайта номер " + parser.getSwitcherId() + " . Для ставки " + totalPriceOfNextBet);
//
//        Double returnedDouble = priceForBetsForOtherParser.get(parser.getSwitcherId());
//        log.info("Цена ставки равна " + returnedDouble + " .");
//        System.out.println("Цена ставки равна " + returnedDouble + " .");
//
//        return returnedDouble;
//    }
//
//    public Map<Integer, Integer> getCopies() {
//        return copies;
//    }
//
//    public int getIdGroup() {
//        return idGroup;
//    }
//
//    public Map<Integer, Double> getPriceForBetsForOtherParser() {
//        return priceForBetsForOtherParser;
//    }

}
