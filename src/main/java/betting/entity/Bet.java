package betting.entity;

import betting.enumeration.OddEven;
import betting.enumeration.SelectionBet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/* The bet entity*/

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "bet")
public class Bet implements Comparable<Bet> {

    private String userName;
    private boolean play;
    private int countCheck;
    private int score;
    private String matchName;
    private int matchPeriod;
    private int idChain;
    private String error;
    private SelectionBet selectionBet;
    private OddEven oddEven;
    private Double price;

    private Long timeBet=-7200000L;

    private Boolean win;

    public Bet(){}

    public Bet(String matchName, SelectionBet selectionBet, double price, OddEven oddEven) {
        this.matchName = matchName;
        this.selectionBet = selectionBet;
        this.price = price;
        this.oddEven = oddEven;
    }

    public void increaseCountCheck(){
        countCheck++;
    }

    public void zeroCountCheck(){
        countCheck=0;
    }

    public int getCountCheck() {
        return countCheck;
    }

    public OddEven getOtherOddEven(){
        if(oddEven == OddEven.Odd){
            return OddEven.Even;
        }else {
            return OddEven.Odd;
        }
    }

    public static boolean compareBetSelection(SelectionBet s0, SelectionBet s1){
        boolean equal = false;
        if(s0==s1){
            equal = true;
        }

        if((s0==SelectionBet.Q2&&s1==SelectionBet.Half1st)||(s0==SelectionBet.Half1st&&s1==SelectionBet.Q2)){
            equal = true;
        }

        if((s0==SelectionBet.Q4&&s1==SelectionBet.Half2nd)||(s0==SelectionBet.Half2nd&&s1==SelectionBet.Q4)){
            equal = true;
        }
        if((s0==SelectionBet.Q4&&s1==SelectionBet.Match)||(s0==SelectionBet.Match&&s1==SelectionBet.Q4)){
            equal = true;
        }
        if((s0==SelectionBet.Match&&s1==SelectionBet.Half2nd)||(s0==SelectionBet.Half2nd&&s1==SelectionBet.Match)){
            equal = true;
        }

        return equal;
    }

    public Boolean getWin() {
        return win;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public OddEven getOddEven() {
        return oddEven;
    }

    public void setOddEven(OddEven oddEven) {
        this.oddEven = oddEven;
    }

    public long getTimeBet() {
        return timeBet;
    }

    public void setTimeBet(long timeBet) {
        this.timeBet = timeBet;
    }

    public void setWin(Boolean win) {
        this.win = win;
    }

    public String getMatchName() {
        return matchName;
    }

    public void setMatchName(String matchName) {
        this.matchName = matchName;
    }

    public SelectionBet getSelectionBet() {
        return selectionBet;
    }

    public void setSelectionBet(SelectionBet selectionBet) {
        this.selectionBet = selectionBet;
    }

    public int getIdChain() {
        return idChain;
    }

    public void setIdChain(int idChain) {
        this.idChain = idChain;
    }

    public boolean isPlay() {
        return play;
    }

    public void setPlay(boolean play) {
        this.play = play;
    }

    public int getMatchPeriod() {
        return matchPeriod;
    }

    public void setMatchPeriod(int matchPeriod) {
        this.matchPeriod = matchPeriod;
    }

    @Override
    public int compareTo(Bet bet) {

        return bet.getMatchName().compareTo(getMatchName());

//            if(bet.getTimeBet()>timeBet){
//                return 1;
//            }else if(bet.getTimeBet()<timeBet){
//                return -1;
//            } else {
//                return 0;
//            }
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getScore() {
        return score;
    }

    public void setScore(Integer score) {
        if(score==null){
            this.score = -1;
        }else {
            this.score = score;
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
