package entityes;

import UI.startFrame.StartFrame;
import betting.Betting;
import betting.entity.Bet;
import betting.enumeration.OddEven;
import betting.enumeration.SelectionBet;
import org.apache.log4j.Logger;
import parser.Parser;
import service.Sender;

import javax.xml.bind.annotation.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/*The chain entity, NOT IMPLEMENTED FINALLY YET*/

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "chain")
@XmlSeeAlso(Bet.class)
public class Chain {
    @XmlTransient
    private static Logger log = Logger.getLogger(Chain.class);
    @XmlTransient
    Random random = new Random();

    boolean copyChain;

    private int idChain = 0;
    private boolean win;
    private double margin;
    private int countBet;
    @XmlElement(name = "betFromList")
    private List<Bet> list;

    private Double nextBetPrice = 0.0;

    private Double lastQuarterBetPrice = 0.0;

    private String matchLiveName;

    private String firstMatchName;

    public Chain() {

    }

    public Chain(String matchLiveName, double betMargin) {
        idChain = random.nextInt(1000)+random.nextInt(1000);
        this.matchLiveName = matchLiveName;
        this.firstMatchName = matchLiveName;
        this.margin = betMargin;
        list = new ArrayList<>();
    }

    public void addBet(Bet bet) {

        boolean newBet = true;
        log.info("Добавляем ставку в цепочку.");
        for(Bet betChain:list){
            if(betChain.getMatchName().compareTo(bet.getMatchName())==0){
                if(betChain.getSelectionBet()==bet.getSelectionBet()){
                    if(betChain.getOddEven()==bet.getOddEven()){
                        betChain = bet;
                        newBet = false;
                        System.out.println("The bet is already in chain....");
                        log.info("Ставка на "+bet.getMatchName()+". Период "+bet.getSelectionBet()+" уже в списке ставок цепочки");
                        break;
                    }
                }
            }
        }

        if(newBet){
            if (!list.contains(bet)) {
                log.info("Ставка на "+bet.getMatchName()+". Период "+bet.getSelectionBet()+" добавлена в лист ставок номер "+idChain);
                System.out.println("Ставка на "+bet.getMatchName()+". Период "+bet.getSelectionBet()+" is added to chain....");
                list.add(bet);
            } else {
                System.out.println("The bet is already in chain....");
            }
        }

        for(Bet bet1:list){
            System.out.println("Ставка "+bet1.getMatchName()+", Период: "+bet1.getSelectionBet()+", ПОБЕДА is: "+bet1.getWin());
        }

        if (list.size() > 0) {
            countBet = 0;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getWin()!=null){
                    if(!list.get(i).getWin()){
                        countBet++;
                    }
                }
            }
            System.out.println("Count bet for chain ID: "+idChain+ ", is "+countBet);
            log.info("Число проигранных ставок в цепочке с ID: "+idChain+ ", равно "+countBet+".");
        }
//       TODO StartFrame.getSwitcher(parser).getStorage().getChainHolder().saveChains();
    }

    public Bet getNextBet(SelectionBet selectionBet, OddEven oddEven,Parser parser, double minBet) {

        if (list.size() > 0) {
            countBet = 0;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getWin()!=null){
                    if(!list.get(i).getWin()){
                        countBet++;
                    }
                }
            }
        }

        Double betPrice = calcBet(minBet,margin ,countBet ,parser);
//        if (betPrice > StartFrame.getSwitcher(parser,parser.getSwitcherId()).getStorage().getCurrentBalance()) {
//            betPrice = 0.0;
//            System.out.println("Change bet price to ZERROOO, Current money count is: " + StartFrame.getSwitcher(parser,parser.getSwitcherId()).getStorage().getCurrentBalance());
//        }

        if (betPrice != null) {
            Bet bet = new Bet(matchLiveName, selectionBet, betPrice, oddEven);
            bet.setIdChain(idChain);
            return bet;
        } else {
            System.out.println("Return bet as null CHAIN CLASS");
            return null;
        }
    }

    private static Double calcBet(double minBet,double margin, int countBet,Parser parser) {
        Double currentPrice = 0.0;
        double price = calcBetForMatchLive(minBet,margin, countBet);

//        System.out.println("Состояние счета " + StartFrame.getSwitcher(parser,parser.getSwitcherId()).getStorage().getCurrentBalance()+", ставка "+price);
//        StartFrame.getInstance().showInformMessageError("Состояние счета " + StartFrame.getSwitcher(parser,parser.getSwitcherId()).getStorage().getCurrentBalance());
//        if (price < StartFrame.getSwitcher(parser,parser.getSwitcherId()).getStorage().getCurrentBalance()) {
//            currentPrice = price;
//            System.out.println("Возвращаем цену " + currentPrice);
//        } else {
//            StartFrame.getInstance().showInformMessageError("Нет денег для ставки");
//            if (Sender.getSender() != null) {
//                if (Sender.getSender().isSwitchOn()) {
//                    String message = "У вас не достаточно средств на счету что бы сделать ставку. " +
//                            "Сейчас на вашем счету " + StartFrame.getSwitcher(parser,parser.getSwitcherId()).getStorage().getCurrentBalance() + " долларов. " +
//                            "Следующая ставка " + price + " долларов.";
//
//                    System.out.println("У вас не достаточно средств на счету что бы сделать ставку. " +
//                            "Сейчас на вашем счету " + StartFrame.getSwitcher(parser,parser.getSwitcherId()).getStorage().getCurrentBalance() + " долларов. " +
//                            "Следующая ставка " + price + " долларов.");
//
//                    Sender.getSender().send("Не достаточно средств на счету", message);
//                }
//            }
//        }

        double nextBetPrice = calcBetForMatchLive(minBet,margin, countBet + 1);
//        if (nextBetPrice > StartFrame.getSwitcher(parser,parser.getSwitcherId()).getStorage().getCurrentBalance()) {
//            StartFrame.getInstance().showInformMessageError("Нет денег на следующую ставку!");
//        }

        if (Sender.getSender() != null) {
            if (Sender.getSender().isSwitchOn()) {
                System.out.println("Need to send message here... MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
                if (nextBetPrice > Sender.getSender().getMaxBet()) {
                    StartFrame.getInstance().showInformMessage("Следующая ставка больше лимита!", Color.red,false);
                    String message = "Ваша следующая ставка может быть больше указанного лимита. Следующая ставка " + nextBetPrice + ". Лимит равен - " + Sender.getSender().getMaxBet();
                    Sender.getSender().send("Следующая ставка ", message);
                    System.out.println("Was send a message: " + message);
                } else {
                    System.out.println("Bet price is less then MAX USING PRICE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
            }
        }
        return currentPrice;
    }

    public static Double calcBetForMatchLive(double minBet,double margin, int countBet) {
        Double bet;
        if (countBet == 0) {
            bet = margin / 0.83;
        } else {
            Double res = (margin * (countBet + 1));
            for (int i = countBet; i > 0; ) {
                res = res + calcBetForMatchLive(minBet,margin, --i);
            }
            bet = res / 0.83;
        }

        if (bet < minBet) {
            return minBet;
        } else {
            return Math.round(bet * 100.0) / 100.0;
        }
    }

    public List<Bet> getList() {
        return list;
    }

    public boolean containBet(Bet bet){
        boolean contain = false;
        for(Bet betInChain:list){
            if(betInChain.getMatchName().compareTo(bet.getMatchName())==0){
                if(betInChain.getSelectionBet()==bet.getSelectionBet()){
                    if(betInChain.getOddEven()==bet.getOddEven()){
                        contain = true;
                        break;
                    }
                }
            }
        }
        return contain;
    }

    public int getCountBet() {
        if (list.size() > 0) {
            countBet = 0;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getWin()!=null){
                    if(!list.get(i).getWin()){
                        countBet++;
                    }
                }
            }
        }
        return countBet;
    }

    public double getNextBetPrice(double minBet) {

        if (list.size() > 0) {
            countBet = 0;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getWin()!=null){
                    if(!list.get(i).getWin()){
                        countBet++;
                    }
                }
            }

            System.out.println("Count bet for chain ID: "+idChain+ ", is "+countBet);
            log.info("Число проигранных ставок в цепочке с ID: "+idChain+ ", равно "+countBet+".");
        }

        nextBetPrice = calcBetForMatchLive(minBet,margin, countBet);
        return nextBetPrice;
    }

    public double getLastBetPriceInMatch(Parser parser, double minBet) {
        lastQuarterBetPrice = calcBetForMatchLive(minBet,margin, countBet + 3);
        return lastQuarterBetPrice;
    }

    public double getTotalPriceOfBetsInMatch(Parser parser,double minBet) {
        double res = 0.0;
        for (int i = countBet; i < countBet + 4; i++) {
            res = res + calcBetForMatchLive(minBet,margin, i);
        }
        return res;
    }

    public double getLastBetPrice() {
        return list.get(list.size() - 1).getPrice();
    }

    public String getMatchLiveName() {
        return matchLiveName;
    }

    public void setMatchLiveName(String matchLiveName,Parser parser) {
        this.matchLiveName = matchLiveName;
//        StartFrame.getSwitcher(parser,parser.getSwitcherId()).getStorage().getChainHolder().saveChains();
    }

    public String getFirstMatchName() {
        return firstMatchName;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win, Betting betting) {
        this.win = win;
        betting.getChainHolder().saveChains();
    }

    public String getBetPeriod() {
        if (matchLiveName != null) {
            Bet bet = null;
            if(list.size()>0){
                bet = list.get(list.size() - 1);//TODO java.lang.ArrayIndexOutOfBoundsException: -1
            }
            if(bet!=null){
                if(bet.getSelectionBet()!=null){
                    return bet.getSelectionBet().toString();
                } else {
                    return "TEST NULL";
                }
            } else {
                return "TEST NULL";
            }

        } else {
            return "NULL";
        }
    }

    public OddEven getLastBetOddEven() {
        if (list.size() != 0) {
            return list.get(list.size() - 1).getOddEven();
        } else {
            return OddEven.Odd;
        }
    }

    public Bet getLastBet() {
        Bet bet = null;
        if (list.size() > 0) {
            bet = list.get(list.size() - 1);
        }
        return bet;
    }

    public int getIdChain() {
        return idChain;
    }

    public double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }

    public void setCountBet(int countBet) {
        this.countBet = countBet;
    }

    public void setList(List<Bet> list) {
        this.list = list;
    }

    public void setNextBetPrice(Double nextBetPrice) {
        this.nextBetPrice = nextBetPrice;
    }

    public Double getLastQuarterBetPrice() {
        return lastQuarterBetPrice;
    }

    public void setLastQuarterBetPrice(Double lastQuarterBetPrice) {
        this.lastQuarterBetPrice = lastQuarterBetPrice;
    }

    public void setFirstMatchName(String firstMatchName) {
        this.firstMatchName = firstMatchName;
    }

    public boolean isCopyChain() {
        return copyChain;
    }

    public void setCopyChain(boolean copyChain) {
        this.copyChain = copyChain;
    }
}
