package bot;

import org.openqa.selenium.WebDriver;
import parser.Parser;

public class LogIner {
    private Parser parser;
    public LogIner(Parser parser){
        this.parser = parser;
    }

    public boolean logIn(String username, String password, WebDriver driver) {
      return parser.logIn(username,password,driver);
    }

    public boolean logOut(WebDriver driver) {
      return parser.logOut(driver);
    }
}