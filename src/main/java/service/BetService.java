package service;

import betting.enumeration.OddEven;
import entities.Bet;
import entities.Match;

public class BetService {
    public OddEven getNextOddEven(Match match, Integer quarter) {
        Integer prevScore = match.scoreMap.get(quarter-1);
        if(prevScore == null) {
           return OddEven.Odd;
        } else if(prevScore%2==0){
            return OddEven.Odd;
        } else {
            return OddEven.Even;
        }
    }
    public double getNextBetValue(Match match, Integer quarter, double minBet, double margin, int countLooseBet) {

            return calcBetForMatchLive(minBet,margin,countLooseBet);

    }
//
//    private double calcNextBet(double minBet, double margin, Match match,Bet prevBet, Integer quarter) {
////        prevBet
//
//    }


    public static Double calcBetForMatchLive(double minBet, double margin, int countBet) {
        Double bet;
        if (countBet == 0) {
            bet = margin / 0.83;
        } else {
            Double res = (margin * (countBet + 1));
            for (int i = countBet; i > 0; ) {
                res = res + calcBetForMatchLive(minBet, margin, --i);
            }
            bet = res / 0.83;
        }

        if (bet < minBet) {
            return minBet;
        } else {
            return Math.round(bet * 100.0) / 100.0;
        }
    }
}
