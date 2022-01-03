import betting.enumeration.OddEven;
import entities.Bet;
import entities.Match;
import entities.Quarter;
import service.BetService;

import java.util.Deque;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

public class AlgorithmTest {
    public static void main(String[] args) {

        Deque<Bet> queue = new ConcurrentLinkedDeque();

        int maxLooseBetCount = 6;

        double totalToUse = 100000.0;
        double total = 0.0;
        double minBet = 1.0;
        double margin = 200;

        int numberOfIterations = 0;
        while (numberOfIterations<10) {
            total = totalToUse;
            numberOfIterations++;

        int numberOfMatches = 0;
        Random random = new Random();
        BetService betService = new BetService();
        boolean isCreditBet = false;
        while (numberOfMatches < 100000 & total > 0) {
            if (total > 0) {
                numberOfMatches++;
                Match match = new Match("Match " + numberOfMatches, "Match " + numberOfMatches);
//                System.out.println(match.matchName + " =========================" + queue.size());
                int countLooseBet = 0;
                for (Integer q : match.scoreMap.keySet()) {
                    if (queue.size() > 0 & q == 1) {
                        Bet prevBet = queue.pollLast();
                        if (prevBet != null) {
                            countLooseBet = prevBet.numberOfLoosedBets;
                        }
                    }

                    OddEven nextOddEven = betService.getNextOddEven(match, q);
                    double nextBetValue = betService.getNextBetValue(match, q, minBet, margin, countLooseBet);
                    total -= nextBetValue;

                    Bet bet = new Bet(match, nextOddEven, nextBetValue, q);

                    int score = random.nextInt(200);
                    match.scoreMap.put(q, score);
                    String result = "";
                    if (bet.isWin()) {
                        double benefit = bet.value * 1.9;
                        total += benefit;
                        countLooseBet = 0;
                        result = "WIN ";
                    } else {
                        countLooseBet++;
                        result = "LOOSE";
                        bet.numberOfLoosedBets = countLooseBet;
                        if (q == 4) {
                            if (bet.numberOfLoosedBets >= maxLooseBetCount) {
                                countLooseBet = 0;
                                for (int i = 0; i < 10; i++) {
                                    Double nextBetValue1 = betService.getNextBetValue(match, q, minBet, margin, bet.numberOfLoosedBets - 2);
                                    Bet bet1 = new Bet(match, nextOddEven, nextBetValue1, q);
                                    bet1.numberOfLoosedBets = bet.numberOfLoosedBets - 3;
                                    queue.addFirst(bet1);
                                }
                            } else {
                                queue.addFirst(bet);
                            }
                        }
                    }
//                    System.out.println("Quoter " + q + ", Bet " + +nextBetValue + " - " + nextOddEven + "(" + bet.numberOfLoosedBets + ")" + ", Score " + score + ", RES " + result);

//                    System.out.println("Total " + total);
                    isCreditBet = false;

                }
//                System.out.println("============================================");
            }
        }
        System.out.println("NumberOfIterations " + numberOfIterations+", Total - " + total);

        double totalQueue = 0.0;
        for (Bet b : queue) {
            totalQueue += b.value;
        }
//        System.out.println("Total queue- " + queue.size() + ", " + totalQueue);

    }


    }

}
