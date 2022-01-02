package betting.UI;

import UI.startFrame.StartFrame;
import betting.Betting;
import betting.parserBetting.BettingParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllDataPanel extends JPanel {

    private Map<BettingParser, List<JPanel>> accountPaneMap;
    private Map<BettingParser, List<JCheckBox>> checkBoxMap;
    private Map<BettingParser, List<JLabel>> userNameMap;
    private Map<BettingParser, List<JLabel>> balanceMap;
    private Map<BettingParser, List<JLabel>> currentBetCountMap;
    private Map<BettingParser, List<JLabel>> currentBetPriceMap;
    private Map<BettingParser, JLabel> currentMatchesMap;

    public AllDataPanel() {
        accountPaneMap = new HashMap<>();
        checkBoxMap = new HashMap<>();
        userNameMap = new HashMap<>();
        balanceMap = new HashMap<>();
        currentBetCountMap = new HashMap<>();
        currentBetPriceMap = new HashMap<>();
        currentMatchesMap = new HashMap<>();

        this.setPreferredSize(new Dimension(1080, 540));
        this.setLayout(new FlowLayout());

        List<BettingParser> parsers = StartFrame.getParsers();
        for (BettingParser parser : parsers) {
            JPanel betPanel = new JPanel(new FlowLayout());
            betPanel.setBorder(BorderFactory.createEtchedBorder());
            betPanel.setPreferredSize(new Dimension(211, 400));

            JLabel mainLabel = new JLabel(parser.getNameOfSite());
            mainLabel.setPreferredSize(new Dimension(100, 15));

            List<JPanel> accountPaneList = new ArrayList<>();
            List<JCheckBox> checkBoxList = new ArrayList<>();
            List<JLabel> userNameList = new ArrayList<>();
            List<JLabel> balanceList = new ArrayList<>();
            List<JLabel> currentBetCountList = new ArrayList<>();
            List<JLabel> currentBetPriceList = new ArrayList<>();

            JCheckBox mainCheckBox = new JCheckBox();
            mainCheckBox.addActionListener((event) -> {
                for (JCheckBox checkBox : checkBoxList) {
                    checkBox.setSelected(mainCheckBox.isSelected());
                }
            });

            JLabel currentMatchesWordLabel = new JLabel("Сейчас матчей: ");
            currentMatchesWordLabel.setPreferredSize(new Dimension(150, 10));

            JLabel currentMatchesNumberLabel = new JLabel("0");
            currentMatchesMap.put(parser, currentMatchesNumberLabel);
            currentMatchesNumberLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            currentMatchesNumberLabel.setPreferredSize(new Dimension(40, 10));

            betPanel.add(mainLabel);
            betPanel.add(Box.createRigidArea(new Dimension(70, 10)));
            betPanel.add(mainCheckBox);
            betPanel.add(currentMatchesWordLabel);
            betPanel.add(currentMatchesNumberLabel);

            for (int i = 0; i < 5; i++) {
                JPanel accountPanel = new JPanel(new FlowLayout());
                accountPaneList.add(accountPanel);
                accountPanel.setBorder(BorderFactory.createEtchedBorder());
                accountPanel.setPreferredSize(new Dimension(205, 66));
                String text = BettingSetting.getBettingSetting().getBettingParserUsernameMap().get(parser).get(i).getText();
                JLabel userNameLabel = new JLabel();
                if (text.length() > 2) {
                    userNameLabel.setText(text);
                } else {
                    userNameLabel.setText("Нет имени");
                }
                userNameLabel.setPreferredSize(new Dimension(165, 10));
                userNameList.add(userNameLabel);

                JCheckBox checkBox = new JCheckBox();
                checkBox.setBackground(new Color(168, 218, 139));
                checkBoxList.add(checkBox);

                JLabel balanceLabel = new JLabel();
                balanceList.add(balanceLabel);
                balanceLabel.setPreferredSize(new Dimension(80, 30));
                balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);

                JLabel currentBetLabel = new JLabel();
                currentBetCountList.add(currentBetLabel);
                currentBetLabel.setPreferredSize(new Dimension(20, 30));
                currentBetLabel.setHorizontalAlignment(SwingConstants.CENTER);

                JLabel currentTotalBetPrice = new JLabel();
                currentBetPriceList.add(currentTotalBetPrice);
                currentTotalBetPrice.setPreferredSize(new Dimension(80, 30));
                currentTotalBetPrice.setHorizontalAlignment(SwingConstants.CENTER);

                accountPanel.add(userNameLabel);
                accountPanel.add(checkBox);
                accountPanel.add(balanceLabel);
                accountPanel.add(currentBetLabel);
                accountPanel.add(currentTotalBetPrice);
                betPanel.add(accountPanel);
            }

            accountPaneMap.put(parser, accountPaneList);
            checkBoxMap.put(parser, checkBoxList);
            userNameMap.put(parser, userNameList);
            balanceMap.put(parser, balanceList);
            currentBetCountMap.put(parser, currentBetCountList);
            currentBetPriceMap.put(parser, currentBetPriceList);
            this.add(betPanel);
        }

        JPanel southPane = new JPanel(new FlowLayout());
        southPane.setPreferredSize(new Dimension(1075, 122));
        southPane.setBorder(BorderFactory.createEtchedBorder());

        JButton startButton = new JButton("START/STOP");
        startButton.setPreferredSize(new Dimension(300, 100));
        startButton.addActionListener((event) -> {

            boolean canStart = true;
//            Document document;
//            try {
//                document = Jsoup.connect("https://time.is/ru/Kyiv").get();
//                Element dd = document.getElementById("dd");
//                String text = dd.text();//четверг, 26 октябрь 2017 г, неделя 43
//                if (text.contains("октябрь") || text.contains("ноябрь")) {
//                    canStart = true;
//                    System.out.println("Можем начинать.");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            canStart = true;
            if (canStart) {
                StartFrame.getInstance().getSettingPanel().createBettings();
                Thread thread = new Thread(() -> {
                    for (BettingParser parser : checkBoxMap.keySet()) {
                        Map<BettingParser, Betting> allBettings = StartFrame.getAllBettings();
                        Betting betting = allBettings.get(parser);
                        Map<Integer, Map<String, String>> betMakersData = betting.getBetMakersData();
                        List<JCheckBox> checkBoxes = checkBoxMap.get(parser);
                        for (int i = 0; i < checkBoxes.size(); i++) {
                            JCheckBox checkBox = checkBoxes.get(i);
                            Map<String, String> map = betMakersData.get(i);
                            map.put("start", String.valueOf(checkBox.isSelected()));
                        }
                        boolean containUserNameAndPassword = false;
                        String userName;
                        String password;
                        String start;
                        for (int i = 0; i < 5; i++) {
                            Map<String, String> map = betMakersData.get(i);
                            if (map != null) {
                                userName = map.get("username");
                                password = map.get("password");
                                start = map.get("start");
                                if (Boolean.valueOf(start)) {
                                    if (userName != null && password != null && userName.length() > 2 && password.length() > 2) {
                                        containUserNameAndPassword = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (containUserNameAndPassword) {
                            BettingUI bettingUI = StartFrame.getBettingUIMap().get(parser);
                            if (!betting.isAllIsStarted()) {
                                betting.start(bettingUI);
                                while (!betting.isAllIsStarted()) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                betting.reCheckBetMakers();
                            }
                        } else {
                            betting.stopBetting();
                            allBettings.put(parser, null);
                        }
                    }
                });
                thread.start();
            }
        });

        southPane.add(startButton);
        this.add(southPane);
    }

    public void setAccountPaneColor(BettingParser parser, int i, Color color) {
        accountPaneMap.get(parser).get(i).setBackground(color);
    }

    void updateMainPanel() {
        for (BettingParser parser : userNameMap.keySet()) {
            List<JLabel> jLabels = userNameMap.get(parser);
            for (int i = 0; i < jLabels.size(); i++) {
                JLabel label = jLabels.get(i);
                label.setText(BettingSetting.getBettingSetting().getBettingParserUsernameMap().get(parser).get(i).getText());
            }
        }
    }

    void updateData(BettingParser parser, int number, int currentMatchesCount,
                    Double currentBalance, int currentBetCount, Double currentBetPrice) {
        balanceMap.get(parser).get(number).setText(String.valueOf(currentBalance));
        currentBetCountMap.get(parser).get(number).setText(String.valueOf(currentBetCount));
        currentBetPriceMap.get(parser).get(number).setText(String.valueOf(currentBetPrice));
        currentMatchesMap.get(parser).setText(String.valueOf(currentMatchesCount));
    }
}
