package betting.UI;

import UI.startFrame.StartFrame;
import betting.Betting;
import betting.parserBetting.BettingParser;
import entityes.Chain;
import parser.Parser;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BettingSetting extends JPanel {
    private static JLabel currentMargin;
    private int numberBetInt;
    public static Parser parser;
    private static BettingSetting bettingSetting;

    private JPanel northBettingSettingPane;
    private JPanel centralBettingSettingPane;
    private JPanel westBettingSettingPane;
    private JTextField driverTextField;

    private static List<BettingParser> parserList;
    private Map<BettingParser, JPanel> bettingParserJPanelMap;
    private Map<BettingParser, List<JTextField>> bettingParserUsernameMap;
    private Map<BettingParser, List<JTextField>> bettingParserPasswordMap;

    private BettingSetting() {
        parserList = StartFrame.getParsers();
        bettingParserJPanelMap = new HashMap<>();
        bettingParserUsernameMap = new HashMap<>();
        bettingParserPasswordMap = new HashMap<>();

        northBettingSettingPane = new JPanel(new FlowLayout());
        northBettingSettingPane.setBorder(BorderFactory.createEtchedBorder());
        northBettingSettingPane.setPreferredSize(new Dimension(1055, 45));

        centralBettingSettingPane = new JPanel(new FlowLayout());
        centralBettingSettingPane.setPreferredSize(new Dimension(710, 450));
        centralBettingSettingPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        westBettingSettingPane = new JPanel(new FlowLayout());
        westBettingSettingPane.setPreferredSize(new Dimension(340, 450));
        westBettingSettingPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        createNorthPanel();
        createWestPanel();
        createCentralPanel();

        this.add(northBettingSettingPane, BorderLayout.NORTH);
        this.add(centralBettingSettingPane, BorderLayout.CENTER);
        this.add(westBettingSettingPane, BorderLayout.WEST);

        TitledBorder titleMainSetting = BorderFactory.createTitledBorder("Настройки");
        titleMainSetting.setTitleJustification(TitledBorder.CENTER);
        titleMainSetting.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 13)));
        this.setBorder(titleMainSetting);
        this.setPreferredSize(new Dimension(1080, 535));//490
    }

    public static BettingSetting getBettingSetting() {
        if (bettingSetting != null) {
            return bettingSetting;
        } else {
            bettingSetting = new BettingSetting();
            return bettingSetting;
        }
    }

    private void createNorthPanel() {
        JLabel driverLabel = new JLabel("CHROME DRIVER");
        driverLabel.setPreferredSize(new Dimension(150, 30));
        driverLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));

        driverTextField = new JTextField("C:\\chromedriver\\chromedriver.exe");
        driverTextField.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
        driverTextField.setPreferredSize(new Dimension(350, 30));

        JButton driverButton = new JButton("OK");
        driverButton.setPreferredSize(new Dimension(100, 30));
        driverButton.addActionListener(e -> {
            String path = driverTextField.getText();
            StartFrame.setPathToDriver(path);
        });

        northBettingSettingPane.add(driverLabel);
        northBettingSettingPane.add(Box.createRigidArea(new Dimension(50, 30)));
        northBettingSettingPane.add(driverTextField);
        northBettingSettingPane.add(Box.createRigidArea(new Dimension(150, 30)));
        northBettingSettingPane.add(driverButton);
    }

    private void createWestPanel() {
        JLabel westLabel = new JLabel("Выберите биржу");
        westLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
        JComboBox<BettingParser> box = new JComboBox<>();
        box.setBackground(Color.WHITE);
        box.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
        for (BettingParser parser : parserList) {
            box.addItem(parser);
        }

        box.addActionListener((e) -> {
            JPanel panel = bettingParserJPanelMap.get(box.getSelectedItem());
            changeCentralPanel(panel);
        });

        JPanel panel = new JPanel(new FlowLayout());
        panel.setPreferredSize(new Dimension(330, 355));
        panel.setBorder(BorderFactory.createEtchedBorder());

        JPanel marginPanel = new JPanel(new FlowLayout());
        marginPanel.setPreferredSize(new Dimension(325, 45));
        marginPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel marginLabel = new JLabel("Маржа");
        marginLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
        marginLabel.setPreferredSize(new Dimension(90, 30));

        currentMargin = new JLabel("0.1");
        currentMargin.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
        currentMargin.setHorizontalAlignment(SwingConstants.CENTER);
        currentMargin.setForeground(Color.red);
        currentMargin.setPreferredSize(new Dimension(70, 30));

        JTextField marginSetField = new JTextField("0.1");
        marginSetField.setPreferredSize(new Dimension(50, 30));

        JButton setMarginButton = new JButton("OK");
        setMarginButton.addActionListener((e) -> {
            String newMargin = marginSetField.getText();
            try {
                Double margin = Double.valueOf(newMargin);
                StartFrame.getInstance().setMargin(margin);
                showMargin(String.valueOf(StartFrame.getMargin()));
            } catch (Exception e1) {
                showMargin("Wrong");
            }
        });

        marginPanel.add(marginLabel);
        marginPanel.add(currentMargin);
        marginPanel.add(marginSetField);
        marginPanel.add(Box.createRigidArea(new Dimension(20, 30)));
        marginPanel.add(setMarginButton);
        panel.add(marginPanel);


        JPanel minBetPanel = new JPanel();
        minBetPanel.setPreferredSize(new Dimension(325, 45));
        minBetPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel labelMinBet = new JLabel("Мин.ставка: ");
        labelMinBet.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
        labelMinBet.setPreferredSize(new Dimension(90, 30));

        JLabel currentMinBet = new JLabel("0.2");
        ;
        currentMinBet.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
        currentMinBet.setHorizontalAlignment(SwingConstants.CENTER);
        currentMinBet.setForeground(Color.red);
        currentMinBet.setPreferredSize(new Dimension(70, 30));

        JTextField minBetSetField = new JTextField("0.0");
        minBetSetField.setPreferredSize(new Dimension(50, 30));

        JButton minBetButton = new JButton("OK");
        minBetButton.addActionListener(e -> {
            double minBet = 0.0;
            try {
                minBet = Double.parseDouble(minBetSetField.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            StartFrame.setMinBet(minBet);
            currentMinBet.setText(String.valueOf(StartFrame.getMinBet()));
            currentMinBet.repaint();
        });

        minBetPanel.add(labelMinBet);
        minBetPanel.add(currentMinBet);
        minBetPanel.add(minBetSetField);
        minBetPanel.add(Box.createRigidArea(new Dimension(20, 30)));
        minBetPanel.add(minBetButton);
        panel.add(minBetPanel);

        JPanel betPricePane = new JPanel(new FlowLayout());
        betPricePane.setPreferredSize(new Dimension(325, 243));
        betPricePane.setBorder(BorderFactory.createEtchedBorder());

        JLabel betNumberLabel = new JLabel("Ставка номер:");
        betNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);
        betNumberLabel.setPreferredSize(new Dimension(300, 40));
        betNumberLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 20));

        JLabel numberBet = new JLabel("0");
        numberBet.setHorizontalAlignment(SwingConstants.CENTER);
        numberBet.setPreferredSize(new Dimension(100, 40));
        numberBet.setFont(new Font("Comic Sans MS", Font.BOLD, 25));

        JLabel priceBetWord = new JLabel("Цена:");
        priceBetWord.setPreferredSize(new Dimension(100, 30));
        priceBetWord.setFont(new Font("Comic Sans MS", Font.BOLD, 18));

        JLabel priceBet = new JLabel("0.00");
        priceBet.setHorizontalAlignment(SwingConstants.CENTER);
        priceBet.setPreferredSize(new Dimension(200, 30));
        priceBet.setFont(new Font("Comic Sans MS", Font.BOLD, 20));

        JLabel totalWord = new JLabel("Всего:");
        totalWord.setPreferredSize(new Dimension(100, 30));
        totalWord.setFont(new Font("Comic Sans MS", Font.BOLD, 18));

        JLabel totalBankToMakeBet = new JLabel("0.00");
        totalBankToMakeBet.setHorizontalAlignment(SwingConstants.CENTER);
        totalBankToMakeBet.setPreferredSize(new Dimension(200, 30));
        totalBankToMakeBet.setFont(new Font("Comic Sans MS", Font.BOLD, 20));

        JButton minusButton = new JButton("-");
        minusButton.setFont((new Font("Comic Sans MS", Font.BOLD, 20)));
        minusButton.setPreferredSize(new Dimension(100, 50));
        minusButton.addActionListener((e -> {
            double minBet = 0.0;
            try {
                minBet = Double.parseDouble(minBetSetField.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            String newMargin = marginSetField.getText();
            Double margin = 0.0;
            try {
                margin = Double.valueOf(newMargin);
            } catch (Exception e1) {
                StartFrame.getInstance().showInformMessage("Wrong", Color.red, false);
            }

            numberBetInt = Integer.parseInt(numberBet.getText());
            if (numberBetInt != 0) {
                numberBet.setText(String.valueOf(numberBetInt - 1));
                if (numberBetInt == 1) {
                    priceBet.setText(String.valueOf(0.00));
                    totalBankToMakeBet.setText("0.00");
                } else {
                    double res = Chain.calcBetForMatchLive(minBet, margin, numberBetInt - 2);
                    priceBet.setText(String.valueOf(res));
                    totalBankToMakeBet.setText(String.valueOf(totalPriceOfBets(minBet, margin, numberBetInt - 1)));
                }
            }
        }));

        JButton plusButton = new JButton("+");
        plusButton.setFont((new Font("Comic Sans MS", Font.BOLD, 20)));
        plusButton.setPreferredSize(new Dimension(100, 50));
        plusButton.addActionListener((e) -> {
            double minBet = 0.0;
            try {
                minBet = Double.parseDouble(minBetSetField.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            String newMargin = marginSetField.getText();
            Double margin = 0.0;
            try {
                margin = Double.valueOf(newMargin);
            } catch (Exception e1) {
                StartFrame.getInstance().showInformMessage("Wrong", Color.red, false);
            }

            numberBetInt = Integer.parseInt(numberBet.getText());
            numberBet.setText(String.valueOf(numberBetInt + 1));
            double res = Chain.calcBetForMatchLive(minBet, margin, numberBetInt);
            priceBet.setText(String.valueOf(res));
            totalBankToMakeBet.setText(String.valueOf(totalPriceOfBets(minBet, margin, numberBetInt + 1)));
        });


        betPricePane.add(betNumberLabel);
        betPricePane.add(minusButton);
        betPricePane.add(numberBet);
        betPricePane.add(plusButton);
        betPricePane.add(Box.createRigidArea(new Dimension(300, 20)));
        betPricePane.add(priceBetWord);
        betPricePane.add(priceBet);
        betPricePane.add(Box.createRigidArea(new Dimension(300, 20)));
        betPricePane.add(totalWord);
        betPricePane.add(totalBankToMakeBet);
        panel.add(betPricePane);

        JButton startAppButton = new JButton("SAVE");
        startAppButton.setPreferredSize(new Dimension(330, 45));
        startAppButton.setFont(new Font("Comic Sans MS", Font.BOLD, 35));
        startAppButton.addActionListener((e) -> {
            createBettings();
            StartFrame.savePassword();
            StartFrame.getInstance().getAllDataPanel().updateMainPanel();
        });

        westBettingSettingPane.add(westLabel);
        westBettingSettingPane.add(Box.createRigidArea(new Dimension(20, 20)));
        westBettingSettingPane.add(box);
        westBettingSettingPane.add(panel);
        westBettingSettingPane.add(startAppButton);
    }

    void createBettings() {
        Map<BettingParser, Betting> storageForBettings = StartFrame.getAllBettings();
        for (int parserNumber = 0; parserNumber < parserList.size(); parserNumber++) {
            BettingParser parser = parserList.get(parserNumber);
            Betting betting;
            if (storageForBettings.containsKey(parser) && storageForBettings.get(parser) != null) {
                betting = storageForBettings.get(parser);
            } else {
                betting = new Betting(driverTextField.getText(), parser);
                storageForBettings.put(parser, betting);
            }

            Map<Integer, Map<String, String>> betMakersData = betting.getBetMakersData();
            List<JTextField> jTextFields = bettingParserUsernameMap.get(parser);

            for (int i = 0; i < jTextFields.size(); i++) {
                Map<String, String> map = new HashMap<>();
                String username = jTextFields.get(i).getText();
                String password = bettingParserPasswordMap.get(parser).get(i).getText();
                StartFrame.getPasswordSaver().savePasswords(parserNumber, i, username, password);
                map.put("username", username);
                map.put("password", password);
                betMakersData.put(i, map);
            }
        }
    }

    private void createCentralPanel() {
        for (BettingParser parser : parserList) {
            JLabel bettingLabel = new JLabel(parser.toString());
            bettingLabel.setPreferredSize(new Dimension(200, 45));
            bettingLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
            List<JTextField> userNames = new ArrayList<>();
            List<JTextField> passwords = new ArrayList<>();
            JPanel mainCentralPane = new JPanel(new FlowLayout());
            mainCentralPane.setPreferredSize(new Dimension(705, 360));
            mainCentralPane.add(bettingLabel);

            for (int i = 0; i < 5; i++) {
                JPanel mainAccountPanel = new JPanel(new FlowLayout());
                mainAccountPanel.setPreferredSize(new Dimension(700, 40));

                JLabel loginLabel = new JLabel("Логин:");
                loginLabel.setPreferredSize(new Dimension(80, 25));
                loginLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
                JTextField loginField = new JTextField();
                loginField.setPreferredSize(new Dimension(150, 25));
                loginField.setFont((new Font("Comic Sans MS", Font.BOLD, 13)));
                userNames.add(loginField);

                JLabel passwordLabel = new JLabel("Пароль");
                passwordLabel.setPreferredSize(new Dimension(80, 25));
                passwordLabel.setFont((new Font("Comic Sans MS", Font.BOLD, 15)));
                JTextField passwordField = new JTextField();
                passwordField.setPreferredSize(new Dimension(150, 25));
                passwordField.setFont((new Font("Comic Sans MS", Font.BOLD, 13)));
                passwords.add(passwordField);

                mainAccountPanel.add(loginLabel);
                mainAccountPanel.add(loginField);
                mainAccountPanel.add(Box.createRigidArea(new Dimension(30, 22)));
                mainAccountPanel.add(passwordLabel);
                mainAccountPanel.add(passwordField);
                mainAccountPanel.add(Box.createRigidArea(new Dimension(30, 25)));
                mainAccountPanel.setBorder(BorderFactory.createEtchedBorder());
                mainCentralPane.add(mainAccountPanel);
            }

            bettingParserUsernameMap.put(parser, userNames);
            bettingParserPasswordMap.put(parser, passwords);
            bettingParserJPanelMap.put(parser, mainCentralPane);
        }

        changeCentralPanel(bettingParserJPanelMap.get(parserList.get(0)));
    }

    private void changeCentralPanel(JPanel panel) {
        if (panel != null) {
            centralBettingSettingPane.removeAll();
            centralBettingSettingPane.add(panel);
            centralBettingSettingPane.validate();
            centralBettingSettingPane.repaint();
        } else {
            System.out.println("Панель равно НУЛЛ");
        }
    }

    public void setField(int parserNumber, int i, String username, String password) {
        BettingParser parser = parserList.get(parserNumber);
        bettingParserUsernameMap.get(parser).get(i).setText(username);
        bettingParserPasswordMap.get(parser).get(i).setText(password);
    }

    private void showMargin(String margin) {
        currentMargin.setText(margin);
    }

    private double totalPriceOfBets(double minBet, double margin, int betCount) {
        double res = Chain.calcBetForMatchLive(minBet, margin, betCount - 1);
        if (betCount == 1) {
            return Chain.calcBetForMatchLive(minBet, margin, betCount - 1);
        } else {
            double temp = totalPriceOfBets(minBet, margin, --betCount);
            res = res + temp;
            return Math.round(res * 100.0) / 100.0;
        }
    }

    public Map<BettingParser, List<JTextField>> getBettingParserUsernameMap() {
        return bettingParserUsernameMap;
    }
}
