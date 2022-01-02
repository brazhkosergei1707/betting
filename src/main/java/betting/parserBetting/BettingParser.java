package betting.parserBetting;

import betting.Betting;
import betting.entity.MatchLiveBetting;
import betting.entity.Bet;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Map;

public interface BettingParser {
    boolean logIn(String username, String password, WebDriver driver);
    boolean logOut(WebDriver driver);
    double getMinBet(WebDriver driver);
    void updateCurrentMatchesLive(Map<String, MatchLiveBetting> currentMatches, WebDriver driver, double margin, double minBet, Betting betting);
    void updateHistoryBet(WebDriver driver,List<Bet> historyList);
    void updateCurrentBet(WebDriver driver,List<Bet> list);
    void setCurrentBalance(WebDriver driver);
    void lookingForMatches(WebDriver driver, Map<String, MatchLiveBetting> matchLiveMap, double margin, double minBet, Betting betting);
    boolean clickToMatch(WebDriver driver, String nameOfMatch);
    boolean checkGameScore(WebDriver driver, MatchLiveBetting matchLive);
    boolean checkMaximumBetSizeAndPossibleBetSelection(WebDriver driver, MatchLiveBetting matchLive);
    boolean goBackToOverview(WebDriver driver);
    Boolean betAlreadyNotPlayed(Bet lastBet, WebDriver driver);
    Bet makeBet(Bet bet, WebDriver driver, MatchLiveBetting matchLive);
    Bet checkBet(Bet bet, WebDriver driver);
    String getNameOfSite();
}
