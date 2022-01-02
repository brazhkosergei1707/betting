package bot;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import parser.Parser;

/* This class responsible for parsing pages, to make or check bets*/
public class Scanner {
    private static final Logger log = Logger.getLogger(Scanner.class);
    Parser parser;

    Scanner(Parser parser) {
        this.parser = parser;
    }

    public void scanMatches(WebDriver driver){
        parser.scanMatches(driver);
    }
}