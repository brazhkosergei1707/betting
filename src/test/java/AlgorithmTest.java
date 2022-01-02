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

        Deque <Bet> queue = new ConcurrentLinkedDeque();
        int maxLooseBetCount = 0;
        double total = 10000.0;
        double credit = 0;
        double minBet = 10.0;
        double margin = 100.;
        double maxMargin = 50;

        int numberOfMatches = 0;
        Random random = new Random();
        BetService betService = new BetService();
        boolean isCreditBet = false;
        while (numberOfMatches<100) {
            numberOfMatches++;
            Match match = new Match("Match " +numberOfMatches,"Match " +numberOfMatches);
            System.out.println(match.matchName + " ===========================");
            int countLooseBet = 0;
            for (Integer q:match.scoreMap.keySet()) {
                if(total > 0 ) {

                    if(queue.size()>0){
                        Bet prevBet = queue.pollLast();
                        if(prevBet !=null) {
                           countLooseBet = prevBet.numberOfLoosedBets;
                        }
                    }

                    System.out.println("Quoter " + q+ " -------------------");
                    if (q==1 & credit >20) {
                        margin = maxMargin;
                        isCreditBet = true;
                    }
                    if(isCreditBet) {
                        credit -= maxMargin;
                    }
                    OddEven nextOddEven = betService.getNextOddEven(match, q);
                    double nextBetValue = betService.getNextBetValue(match, q, minBet, margin, countLooseBet);
                    total -= nextBetValue;

                    Bet bet = new Bet(match,nextOddEven, nextBetValue,q);
                    System.out.println("Bet " + nextBetValue + " - " + nextOddEven);
                    int score = random.nextInt(200);
                    match.scoreMap.put(q,score);
                    System.out.println("Score " + score);
                    if(bet.isWin()) {
                        double benefit = bet.value*1.9;
                        total+=benefit;
                        countLooseBet=0;
                        System.out.println("WIN ");
                    } else {
                        countLooseBet++;
                        System.out.println("LOOSE");
                        bet.numberOfLoosedBets = countLooseBet;
                        if(q==4){
                            queue.addFirst(bet);
                            credit +=bet.value;
                        }
                    }
                    System.out.println("Total " + total);
                    System.out.println("Credit " + credit);
                    isCreditBet = false;
                }
            }
            System.out.println("============================================");
            System.out.println("============================================");
        }
        System.out.println("Total - " + total);
        System.out.println("Credit - " + credit);














    }

}
