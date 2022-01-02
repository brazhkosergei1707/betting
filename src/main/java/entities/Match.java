package entities;

import java.util.*;

public class Match {
    public Map<Integer, Integer> scoreMap;
    String linkToMatch;
    public  String matchName;
    public Map<Integer,Bet> bets;
    boolean isActive = false;
    boolean isPossibleToMakeBet = false;


    public Match(String linkToMatch, String matchName) {
        this();
        this.linkToMatch = linkToMatch;
        this.matchName = matchName;
    }

    private Match() {
        scoreMap = new TreeMap<>();
        scoreMap.put(1, 0);
        scoreMap.put(2, 0);
        scoreMap.put(3, 0);
        scoreMap.put(4, 0);
        bets = new TreeMap<>();
    }

//    public void addBet(Bet bet){
//        bets.add(bet);
//    }

    public void startMatch(){
        isActive = true;
    }

    public boolean isPossibleToMakeBet(){
        return isPossibleToMakeBet;
    }

//    public void setScore(Quarter quarter, Integer score){
//        scoreMap.put(quarter,score);
//    }
}
