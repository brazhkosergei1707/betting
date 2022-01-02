package bot;

import org.openqa.selenium.*;
import parser.Parser;

/* This class responsible for parsing pages to update current time, schedule of matches and current matches list*/
public class Updater {

    Parser parser;

    public Updater(Parser parser) {
        this.parser = parser;
    }

    public void setCurrentTimeAndCount(WebDriver driver) {
        parser.setCurrentTimeAndCount(driver);
    }

    public void updateHistoryBet(WebDriver driver) {
      parser.updateHistoryBet(driver);
    }

    public void updateCurrentMatchesLive(WebDriver driver) {
        parser.updateCurrentMatchesLive(driver);
    }

    public void updateCurrentBet(WebDriver driver) {
       parser.updateCurrentBet(driver);
    }
}
