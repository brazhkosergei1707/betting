package betting.UI;

import UI.startFrame.StartFrame;
import betting.Betting;
import betting.bot.BetMaker;
import betting.parserBetting.BettingParser;
import betting.entity.Bet;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BettingUI extends JPanel {
    private Betting betting;
    private BettingParser parser;
    private JPanel mainPanel;
    private JPanel westPanel;

    private CurrentMatchesPanel currentMatchesPanel;
    private CurrentBetPanel currentBetPanel;
    private ChainsPanelNew chainsPanel;
    private JLabel makeBetLabel;
    private JLabel checkBetLabel;
    private JLabel matchesCountLabel;

    private Map<BetMaker, JLabel> namesMap;
    private Map<BetMaker, JLabel> currentBetsCountMap;
    private Map<BetMaker, JLabel> balanceMap;
    private Map<BetMaker, HistoryPanel> historyPanelsMap;
    private JPanel statisticPane;
    private JComboBox<BetMaker> betMakerJComboBox;

    public BettingUI(BettingParser parser) {
        this.parser = parser;
        this.setLayout(new BorderLayout(2, 2));
        this.setPreferredSize(new Dimension(1080, 535));
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        currentMatchesPanel = new CurrentMatchesPanel();
        currentBetPanel = new CurrentBetPanel();
        chainsPanel = new ChainsPanelNew();
        makeBetLabel = new JLabel("0");
        checkBetLabel = new JLabel("0");
        matchesCountLabel = new JLabel("0");

        statisticPane = new JPanel(new FlowLayout());
        statisticPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        statisticPane.setPreferredSize(new Dimension(150, 140));

        namesMap = new HashMap<>();
        currentBetsCountMap = new HashMap<>();
        balanceMap = new HashMap<>();
        historyPanelsMap = new HashMap<>();

        mainPanel = new JPanel(new FlowLayout());
        mainPanel.setPreferredSize(new Dimension(890, 530));

        buildCentralPanel();

        westPanel = new JPanel(new FlowLayout());
        westPanel.setPreferredSize(new Dimension(160, 530));
        westPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        buildWestPanel();

        this.add(mainPanel, BorderLayout.CENTER);
        this.add(westPanel, BorderLayout.WEST);
    }

    private void buildWestPanel() {
        JLabel mainLabel = new JLabel(parser.getNameOfSite());
        mainLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
        mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainLabel.setPreferredSize(new Dimension(150, 30));

        JButton matchButton = new JButton("Матчи");
        matchButton.setPreferredSize(new Dimension(150, 50));
        matchButton.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
        matchButton.addActionListener(e -> {
            setMainPanel(currentMatchesPanel);
        });

        JButton betButton = new JButton("Ставки");
        betButton.setPreferredSize(new Dimension(150, 50));
        betButton.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
        betButton.addActionListener(e -> {
            setMainPanel(currentBetPanel);
        });

        JButton chainButton = new JButton("Цепочки");
        chainButton.setPreferredSize(new Dimension(150, 50));
        chainButton.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
        chainButton.addActionListener(e -> {
            setMainPanel(chainsPanel);
        });

        JButton historyButton = new JButton("История");
        historyButton.setPreferredSize(new Dimension(150, 50));
        historyButton.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
        historyButton.addActionListener(e -> {
            HistoryPanel historyPanel = historyPanelsMap.get(betMakerJComboBox.getSelectedItem());
            if (historyPanel != null) {
                setMainPanel(historyPanel);
                System.out.println("Обновляем историю.");
            } else {
                System.out.println("Нечего обновлять.");
            }
        });

        westPanel.add(mainLabel);
        westPanel.add(matchButton);
        westPanel.add(betButton);
        westPanel.add(chainButton);
        westPanel.add(historyButton);

        JPanel historyPane = new JPanel();
        historyPane.setPreferredSize(new Dimension(150, 40));
        historyPane.setBorder(BorderFactory.createEtchedBorder());

        betMakerJComboBox = new JComboBox<>();
        betMakerJComboBox.setPreferredSize(new Dimension(140, 25));
        betMakerJComboBox.addActionListener((e) -> {
            HistoryPanel historyPanel = historyPanelsMap.get(betMakerJComboBox.getSelectedItem());
            if (historyPanel != null) {
                setMainPanel(historyPanel);
                System.out.println("Обновляем историю.");
            } else {
                System.out.println("Нечего обновлять.");
            }
        });

        historyPane.add(betMakerJComboBox);
        westPanel.add(historyPane);

        JPanel informPane = new JPanel(new FlowLayout());
        informPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        informPane.setPreferredSize(new Dimension(150, 73));

        JLabel makeBetLabelText = new JLabel("Ставок поставить: ");
        makeBetLabelText.setPreferredSize(new Dimension(110, 15));
        JLabel checkBetLabelText = new JLabel("Ставок проверить: ");
        checkBetLabelText.setPreferredSize(new Dimension(110, 15));
        JLabel matchesLabelText = new JLabel("Матчей сейчас: ");
        matchesLabelText.setPreferredSize(new Dimension(110, 15));
        informPane.add(makeBetLabelText);
        informPane.add(makeBetLabel);
        informPane.add(checkBetLabelText);
        informPane.add(checkBetLabel);
        informPane.add(matchesLabelText);
        informPane.add(matchesCountLabel);
        westPanel.add(informPane);
        westPanel.add(statisticPane);
    }

    private void paintStatisticPanel() {
        statisticPane.removeAll();
        Map<Integer, BetMaker> betMakerMap = betting.getBetMakerMap();
        for (Integer integer : betMakerMap.keySet()) {
            BetMaker betMaker = betMakerMap.get(integer);

            betMakerJComboBox.addItem(betMaker);

            historyPanelsMap.put(betMaker, new HistoryPanel());

            JLabel name = new JLabel(betMaker.getUserName());
            name.setPreferredSize(new Dimension(60, 20));
            name.setHorizontalAlignment(SwingConstants.CENTER);
            namesMap.put(betMaker, name);
            JLabel balance = new JLabel("0");
            balance.setPreferredSize(new Dimension(55, 20));
            balance.setHorizontalAlignment(SwingConstants.CENTER);
            balanceMap.put(betMaker, balance);
            JLabel betCount = new JLabel("0");
            betCount.setPreferredSize(new Dimension(15, 20));
            betCount.setHorizontalAlignment(SwingConstants.CENTER);

            currentBetsCountMap.put(betMaker, betCount);
            statisticPane.add(betCount);
            statisticPane.add(name);
            statisticPane.add(balance);
        }
        statisticPane.validate();
        statisticPane.repaint();
    }

    private void setMainPanel(JPanel panel) {
        mainPanel.removeAll();
        mainPanel.add(panel);
        mainPanel.validate();
        mainPanel.repaint();
    }

    private void buildCentralPanel() {
        mainPanel.add(currentMatchesPanel);
    }

    public void setBetting(Betting betting) {
        this.betting = betting;
        paintStatisticPanel();
    }

    public void updatePanel() {
        try {
            currentMatchesPanel.updateCurrentMatchesTable(betting.getMatchesList());
            currentBetPanel.updateCurrentBetTable(betting.getCurrentBetList());
            chainsPanel.updatePanel(betting.getChainList(), betting);
            makeBetLabel.setText(String.valueOf(betting.getMadeBetCount()));
            checkBetLabel.setText(String.valueOf(betting.getCheckBetCount()));
            matchesCountLabel.setText(String.valueOf(betting.getMatchesCount()));
            Map<Integer, BetMaker> betMakerMap = betting.getBetMakerMap();
            for (Integer integer : betMakerMap.keySet()) {
                BetMaker betMaker = betMakerMap.get(integer);
                if (betMaker != null) {
                    StartFrame.getInstance().getAllDataPanel().updateData(parser, integer,
                            betting.getMatchesCount(), betMaker.getStorage().getCurrentBalance(),
                            betMaker.getStorage().getCurrentBets().size(), betMaker.getStorage().getCurrentBetPrice());
                    HistoryPanel historyPanel = historyPanelsMap.get(betMaker);
                    List<Bet> historyBets = betMaker.getStorage().getHistoryBets();
                    historyPanel.updateHistory(historyBets);
                    currentBetsCountMap.get(betMaker).setText(String.valueOf(betMaker.getStorage().getCurrentBets().size()));
                    balanceMap.get(betMaker).setText(String.valueOf(betMaker.getStorage().getCurrentBalance()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }
}
