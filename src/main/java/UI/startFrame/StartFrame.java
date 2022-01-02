package UI.startFrame;

import betting.UI.AllDataPanel;
import betting.UI.BettingSetting;
import betting.Betting;
import betting.entity.StorageBetting;
import betting.UI.BettingUI;
import betting.parserBetting.*;
import betting.entity.PasswordSaver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartFrame extends JFrame {

    private AllDataPanel allDataPanel;
    private static String pathToDriver = "C:\\chromedriver\\chromedriver.exe";
    private static StartFrame startFrame;
    private static PasswordSaver passwordSaver;

    private JPanel northPanel;
    private JPanel centerPanel;
    private JPanel southPanel;
    private BettingSetting settingPanel;

    private JLabel informLabel;
    private JProgressBar informationMessageProgressBar;

    private static double margin = 0.1;
    private static double minBet = 0.2;

    private static List<BettingParser> parsers;
    private static Map<BettingParser,Betting> allBettings;
    private static Map<BettingParser,StorageBetting> storages;
    private static Map<BettingParser,BettingUI> bettingUIMap;
    private JLabel centralMainLabel;

    private StartFrame() {
        super("Betting");
        parsers = new ArrayList<>();
        parsers.add(new BettingParserBetSBC());
        parsers.add(new BettingParserBet365());
        parsers.add(new BettingParserPariMatchOldVersion());
        parsers.add(new BettingParserWilliamHill());
        parsers.add(new BettingBetFair());

        allBettings = new HashMap<>();
        storages = new HashMap<>();
        bettingUIMap = new HashMap<>();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mainPanel.setMaximumSize(new Dimension(1120,670));

        northPanel = new JPanel(new FlowLayout());
        northPanel.setPreferredSize(new Dimension(1095,55));
        northPanel.setBorder(BorderFactory.createEtchedBorder());
        buildNorthPanel();

        centerPanel = new JPanel(new FlowLayout());
        centerPanel.setPreferredSize(new Dimension(1095,550));
        centerPanel.setBorder(BorderFactory.createEtchedBorder());

        southPanel = new JPanel(new FlowLayout());
        southPanel.setPreferredSize(new Dimension(1095,65));
        southPanel.setBorder(BorderFactory.createEtchedBorder());
        buildSouthPanel();

        settingPanel = BettingSetting.getBettingSetting();
        passwordSaver = PasswordSaver.restorePasswords();
        passwordSaver.setPasswordsToFields();

        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

               allDataPanel = new AllDataPanel();

        setCentralPanel(allDataPanel);
        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.add(mainPanel);
        setVisible(true);
        pack();
    }

    public static StartFrame getInstance() {
        if (startFrame == null) {
            startFrame = new StartFrame();
            return startFrame;
        } else {
            return startFrame;
        }
    }

    public static void addStorage(BettingParser parser,StorageBetting storage){
        storages.put(parser,storage);
    }

    private void buildNorthPanel(){
        JButton allBetsButton = new JButton("Все ставки");
        allBetsButton.setPreferredSize(new Dimension(150,40));
        allBetsButton.addActionListener(e->{
            centerPanel.removeAll();
            centerPanel.revalidate();
            centerPanel.add(allDataPanel);
            centralMainLabel.setText("Все ставки");
            revalidate();
            repaint();
        });

        JPanel centralHeaderPanel = new JPanel();
        centralHeaderPanel.setPreferredSize(new Dimension(770,30));

        centralMainLabel = new JLabel("Главная");
        centralMainLabel.setFont(new Font(null,Font.BOLD,20));
        centralHeaderPanel.add(centralMainLabel);

        JButton settingsButton = new JButton("Настройки");
        settingsButton.setPreferredSize(new Dimension(150,40));
        settingsButton.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
        settingsButton.addActionListener((ActionEvent e) -> {
            centerPanel.removeAll();
            centerPanel.add(settingPanel);
            centerPanel.revalidate();
            centralMainLabel.setText("Настройки");
            revalidate();
            repaint();
        });

        northPanel.add(allBetsButton, BorderLayout.WEST);
        northPanel.add(centralHeaderPanel, BorderLayout.CENTER);
        northPanel.add(settingsButton, BorderLayout.EAST);
    }

    private void setCentralPanel(JPanel panel){
        centerPanel.removeAll();
        centerPanel.add(panel);
        centerPanel.validate();
        centerPanel.repaint();
    }

    private void buildSouthPanel(){
                JPanel bettingButtonsPanel = new JPanel(new FlowLayout());
                bettingButtonsPanel.setPreferredSize(new Dimension(640,54));
                bettingButtonsPanel.setBorder(BorderFactory.createEtchedBorder());

                for(int i = 0;i<parsers.size();i++){
                    BettingParser bettingParser = parsers.get(i);
                    BettingUI bettingUI = new BettingUI(bettingParser);
                    bettingUIMap.put(bettingParser,bettingUI);
                    JButton button = new JButton(bettingParser.getNameOfSite());
                    int finalI = i;
                    button.addActionListener((e)->{
                        BettingParser parser = parsers.get(finalI);
                        setCentralPanel(bettingUI);
                centralMainLabel.setText(parser.getNameOfSite());
            });
            button.setPreferredSize(new Dimension(120,40));
            bettingButtonsPanel.add(button);
        }

        informLabel = new JLabel("Information");
        informLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
        informLabel.setHorizontalAlignment(SwingConstants.CENTER);
        informLabel.setPreferredSize(new Dimension(320, 35));

        informationMessageProgressBar = new JProgressBar();
        informationMessageProgressBar.setPreferredSize(new Dimension(100, 15));

        southPanel.add(bettingButtonsPanel);
        southPanel.add(informLabel);
        southPanel.add(informationMessageProgressBar);
    }

    public static void savePassword() {
        passwordSaver.savePasswordSaverToFile();
    }

    public static Map<BettingParser, BettingUI> getBettingUIMap() {
        return bettingUIMap;
    }

    public BettingSetting getSettingPanel() {
        return settingPanel;
    }

    public void showInformMessage(String message, Color color, boolean progressBarSwitchOn) {
        if (message != null) {
            informLabel.setText(message);
            informLabel.setForeground(color);
            informationMessageProgressBar.setIndeterminate(progressBarSwitchOn);
        }
    }

    public AllDataPanel getAllDataPanel() {
        return allDataPanel;
    }

    public static List<BettingParser> getParsers() {
        return parsers;
    }

    public static Map<BettingParser, Betting> getAllBettings() {
        return allBettings;
    }

    public static double getMinBet() {
        return minBet;
    }

    public static void setMinBet(double minBet) {
        StartFrame.minBet = minBet;
    }

    public static StorageBetting getStorage(BettingParser parser){
        return storages.get(parser);
    }

    public static String getPathToDriver() {
        return pathToDriver;
    }

    public static void setPathToDriver(String pathToDriver) {
        StartFrame.pathToDriver = pathToDriver;
    }

    public static double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        StartFrame.margin = margin;
    }

    public static PasswordSaver getPasswordSaver() {
        return passwordSaver;
    }
}
