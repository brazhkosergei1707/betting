package bot;

import UI.startFrame.StartFrame;
import org.apache.log4j.*;
import org.openqa.selenium.WebDriver;
import parser.Parser;
import service.DriverService;

import java.awt.*;

public class SwitcherMain extends Thread {
    public Logger log;


    private int switcherId;
    private boolean appWork = true;
    private LogIner logIner;
    private Scanner betMaker;
    private Updater updater;
    private WebDriver driver;
    private String pathDriver;
    private Parser parser;
    public String username = "TEST";
    private String password;

    private String path;

    public Storage storage;

    public SwitcherMain(Parser parser) {
        storage = new Storage(parser.getNameOfSite(), parser);
    }

    public SwitcherMain(String username, String password, String pathDriver, Parser parser, int switcherId) {

        this.switcherId = switcherId;
        this.parser = parser;
        parser.setSwitcherId(switcherId);
        this.username = username;
        this.password = password;
        this.pathDriver = pathDriver;
        this.path = "C:\\chromedriver\\log_file_"+parser.getNameOfSite()+".log";

        FileAppender appender = new FileAppender();
//        FileAppender appender = new RollingFileAppender();
        appender.setFile(path);
        appender.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"));
        appender.activateOptions();
//        appender.setEncoding("UTF-8");

        log = Logger.getLogger(SwitcherMain.class);
        log.setAdditivity(false);
        log.removeAllAppenders();
        log.addAppender(appender);
        parser.setAppender(appender);

        betMaker = new Scanner(parser);
        updater = new Updater(parser);
        storage = new Storage(username, parser);
        logIner = new LogIner(parser);
    }

    public boolean isAppWork() {
        return appWork;
    }

    @Override
    public void run() {
//        driver = DriverService.getInstance().getWebDriver(pathDriver);
//        try {
//            log.info("Логируемся на сайте " + parser.getNameOfSite());
//            appWork = logIner.logIn(username, password, driver);
//        } catch (Exception e) {
//            log.error("Проблема во время логирования");
//            e.printStackTrace();
//            appWork = false;
//            StartFrame.getInstance().showInformMessage("Проблемы во время логирования, попробуйте еще раз", new Color(220, 20, 60), false);
//        }
//
//        if (appWork) {
////            StartFrame.getInstance().startBrowser();
//            try {
//                log.info("Определяем минимальную ставку ");
//                if (storage.getMinBet() == 0.0) {
//                    storage.setMinBet(setMinBet(driver));
//                }
//                log.info("Минимальная ставка " + storage.getMinBet());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            log.info("Обновляем время и состояние счета ");
//            updater.setCurrentTimeAndCount(driver);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            log.info("Обновляем историю ставок");
//            try {
//                updater.updateHistoryBet(driver);
//                log.info("История ставок успешно обновлена");
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error("Ошибка во время обновления истории ставок");
//                System.out.println("Error 1");
//            }
//
//            try {
//                log.info("Обновляем список текущих матчей");
//                updater.updateCurrentMatchesLive(driver);
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error("Ошибка во время обновления списка текущих матчей");
//                System.out.println("Error 2");
//            }
//
//            try {
//                log.info("Обновляем список текущих ставок");
//                updater.updateCurrentBet(driver);
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error("Ошибка во время обновления списка текущих ставок");
//                System.out.println("Error 3");
//            }
//
//            try {
//                storage.checkBatsInStorage();
//            } catch (Exception e) {
//                e.printStackTrace();
//                System.out.println("Проб");
//            }
//
//            try {
//                storage.rebuildChainsHistory(driver);
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error("Ошибка во время восстановления цепочек");
//                System.out.println("Error 4");
//            }
//
////            BettingSetting.getBettingSetting().connect(parser.getSwitcherId());
//            log.info("Обновляем интерфейс");
//            storage.updateInterface();
//        } else {
//            restart();
//        }
//
//        StartFrame.getInstance().showInformMessage("Биржа " + parser.getNameOfSite() + " успешно запущена.", new Color(46, 139, 87), false);
//        boolean wait;
//        while (appWork) {
//            if (storage.getCurrentMatches().size() == 0 && storage.getCurrentBets().size() == 0) {
//                wait = true;
//            } else {
//                wait = false;
//            }
//            if (wait) {
//                log.info("Сейчас нет матчей, выходим из сайта. Проверим через минуту, есть ли матчи");
//                try {
//
//                    logIner.logOut(driver);
//                    driver.quit();
//                    driver = null;
//                    try {
//                        Thread.sleep(90000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    if (appWork) {
//                        if (driver == null) {
//                            driver = DriverService.getInstance().getWebDriver(pathDriver);
//                        }
//                        log.info("Заходим снова на сайт " + parser.getNameOfSite() + ".");
//                        logIner.logIn(username, password, driver);
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        log.info("Проверяем есть ли матчи на сайте " + parser.getNameOfSite());
//                        updater.updateCurrentMatchesLive(driver);
//                        log.info("Найдено '" + storage.getCurrentMatches().size() + "' матчей.");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    log.error(e.getMessage());
//                    StartFrame.getInstance().showInformMessage("Перезапускаем браузер сайта " + parser.getNameOfSite(), new Color(220, 20, 60), true);
//                    log.error("Перезапускаем браузер");
//                    restart();
//                }
//            } else {
//                try {
//                    log.info("Обновляем время и состояние счета " + parser.getNameOfSite());
//                    updater.setCurrentTimeAndCount(driver);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
////                    storage.updateCurrentTimeAndBalance();
//                    log.info("Сканируем матчи " + parser.getNameOfSite());
//                    betMaker.scanMatches(driver);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    log.info("Обновляем интерфейс " + parser.getNameOfSite());
//                    storage.updateInterface();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    log.error("Перезапускаем браузер");
//                    log.error(e.getMessage());
//                    restart();
//                }
//            }
//        }
////        StartFrame.removeSwitcher(switcherId);
//        log.info("Выходим из сайта " + parser.getNameOfSite());
//        if (driver != null) {
//            if (logIner.logOut(driver)) {
//                log.info("Успешно вышли из сайта и закрыли драйвер.");
//                driver.quit();
//            } else {
//                log.error("Ошибка во время выхода из сайта, закрываем драйвер.");
//                driver.quit();
//            }
//        }
//        storage.cleanStorage();
    }

    private void restart() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        driver = DriverService.getInstance().getWebDriver(pathDriver);

        try {
            log.info("Логируемся на сайте");
            appWork = logIner.logIn(username, password, driver);
        } catch (Exception e) {
            log.error("Проблема во время логирования");
            log.error(e.getLocalizedMessage());
            e.printStackTrace();
            appWork = false;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException w) {
                w.printStackTrace();
            }
        }

        if (appWork) {
            if (storage.getMinBet() == 0.0) {
                storage.setMinBet(setMinBet(driver));
            }
            log.info("Обновляем историю ставок");
            updater.updateHistoryBet(driver);
            log.info("Обновляем список текущих матчей");
            updater.updateCurrentMatchesLive(driver);
            log.info("Обновляем список текущих ставок");
            updater.updateCurrentBet(driver);
        } else {
          restart();
        }
    }

    public void stopSwitcher() {
        appWork = false;
    }

    public Storage getStorage() {
        return storage;
    }

    private double setMinBet(WebDriver driver) {
        double res = parser.getMinBet(driver);
        if (res > 0.0) {
            return res;
        } else {
            return setMinBet(driver);
        }
    }

    public int getSwitcherId() {
        return switcherId;
    }

    public Parser getParser() {
        return parser;
    }
}
