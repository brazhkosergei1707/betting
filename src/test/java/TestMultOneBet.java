import UI.startFrame.StartFrame;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.opera.OperaDriver;
//org.openqa.selenium.opera
//
//import com.opera.core.systems.OperaDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import parser.Parser;
import parser.bet365.ParserBet365;

import java.io.IOException;

public class TestMultOneBet {

    public static void main(String[] args) {
        //https://www.google.com.ua/search?q=%D0%B2%D1%80%D0%B5%D0%BC%D1%8F+%D0%BA%D0%B8%D0%B5%D0%B2
        Document document = null;
        try {
            document = Jsoup.connect("https://time.is/ru/Kyiv").get();
            Element dd = document.getElementById("dd");
            String text = dd.text();//четверг, 26 октябрь 2017 г, неделя 43
            if(text.contains("октябрь")||text.contains("ноябрь")){
                System.out.println(text);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
