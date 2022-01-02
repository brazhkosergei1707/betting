package entities;

import betting.enumeration.OddEven;

public class Bet {
    public Match match;
    public OddEven score;
    public Double value;
    public Integer quarter;
    public int numberOfLoosedBets;

    public Bet(Match match, OddEven score, Double value, Integer quarter) {
        this.match = match;
        this.score = score;
        this.value = value;
        this.quarter = quarter;
        match.bets.put(quarter,this);
    }

    public boolean isWin() {
        return (match.scoreMap.get(quarter)%2==0 & score==OddEven.Even) || (match.scoreMap.get(quarter)%2!=0 & score==OddEven.Odd);
    }
}
