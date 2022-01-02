package parser;

import betting.entity.Bet;
import entityes.MatchLive;
import org.apache.log4j.Appender;
import org.openqa.selenium.WebDriver;

public interface Parser {
    boolean logIn(String username, String password, WebDriver driver);

    boolean logOut(WebDriver driver);

    void setCurrentTimeAndCount(WebDriver driver);

    void updateHistoryBet(WebDriver driver);

    void updateCurrentMatchesLive(WebDriver driver);

    void updateCurrentBet(WebDriver driver);

    String getNameOfSite();

    void scanMatches(WebDriver driver);

    boolean clickToMatch(WebDriver driver, String nameOfMatch);

    Boolean betAlreadyNotPlayed(Bet lastBet, WebDriver driver);

    void checkGameScore(WebDriver driver, MatchLive matchLive);

    Bet checkBet(Bet bet, WebDriver driver);

    void checkMaximumBetSizeAndPossibleBetSelection(WebDriver driver, MatchLive matchLive);

    Bet makeBet(Bet bet, WebDriver driver, MatchLive matchLive);

    void goBackToOverview(WebDriver driver);

    double getMinBet(WebDriver driver);

    void setActiveHistory(boolean isActive);

    boolean isActiveHistory();

    void setActiveDetails(boolean isActive);

    boolean isActiveDetails();

    void addEmergencyEvent(MatchLive matchLive);

    void makeEmergencyEvent(MatchLive matchLive, WebDriver driver);

    void setSwitcherId(int switcherId);

    int getSwitcherId();

    void setAppender(Appender appender);

    Appender getAppender();
}
