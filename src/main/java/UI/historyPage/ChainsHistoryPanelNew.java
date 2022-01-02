package UI.historyPage;

import betting.entity.ChainBetting;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class ChainsHistoryPanelNew extends JPanel {
    private JPanel mainChainHistoryPanel;
    private JPanel bet365Panel;
    private JScrollPane bet365ScrollPane;
    private JScrollPane scrollPaneDetails;

    public ChainsHistoryPanelNew() {
        mainChainHistoryPanel = new JPanel(new BorderLayout());
        JPanel headerPanel = new JPanel(new GridLayout(0, 1, 5, 10));
        JPanel chainPanelHeader = new JPanel(new FlowLayout());
        JLabel l0 = new JLabel("ID");
        l0.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
        l0.setFont(new Font("Comic Sans MS", Font.BOLD, 11));
        l0.setForeground(Color.blue);
        l0.setPreferredSize(new Dimension(30, 30));

        JLabel l1 = new JLabel("Последний матч");
        l1.setHorizontalAlignment(SwingConstants.CENTER);
        l1.setFont(new Font("Comic Sans MS", Font.BOLD, 11));
        l1.setForeground(Color.blue);
        l1.setPreferredSize(new Dimension(450, 30));

        JLabel l10 = new JLabel("Период");
        l10.setHorizontalAlignment(SwingConstants.CENTER);
        l10.setFont(new Font("Comic Sans MS", Font.BOLD, 11));
        l10.setForeground(Color.blue);
        l10.setPreferredSize(new Dimension(80, 30));

        JLabel l2 = new JLabel("Тип");
        l2.setHorizontalAlignment(SwingConstants.CENTER);
        l2.setFont(new Font("Comic Sans MS", Font.BOLD, 11));
        l2.setForeground(Color.blue);
        l2.setPreferredSize(new Dimension(80, 30));

        JLabel l3 = new JLabel("$");
        l3.setHorizontalAlignment(SwingConstants.CENTER);
        l3.setFont(new Font("Comic Sans MS", Font.BOLD, 11));
        l3.setForeground(Color.blue);
        l3.setPreferredSize(new Dimension(70, 30));

        JLabel l5 = new JLabel("Cтавок");
        l5.setHorizontalAlignment(SwingConstants.CENTER);
        l5.setFont(new Font("Comic Sans MS", Font.BOLD, 11));
        l5.setForeground(Color.blue);
        l5.setPreferredSize(new Dimension(80, 30));

        JLabel l6 = new JLabel("Детализация");
        l6.setHorizontalAlignment(SwingConstants.CENTER);
        l6.setFont(new Font("Comic Sans MS", Font.BOLD, 11));
        l6.setForeground(Color.blue);
        l6.setPreferredSize(new Dimension(90, 30));

        chainPanelHeader.add(l0);
        chainPanelHeader.add(l1);
        chainPanelHeader.add(l10);
        chainPanelHeader.add(l2);
        chainPanelHeader.add(l3);
        chainPanelHeader.add(l5);
        chainPanelHeader.add(l6);
        headerPanel.add(chainPanelHeader);
        mainChainHistoryPanel.add(headerPanel, BorderLayout.NORTH);

        bet365Panel = new JPanel();
        bet365Panel.setLayout(new BoxLayout(bet365Panel, BoxLayout.Y_AXIS));
        bet365Panel.setBackground(new Color(224, 255, 255));

        bet365ScrollPane = new JScrollPane(bet365Panel);
        bet365ScrollPane.setPreferredSize(new Dimension(1010, 400));

        mainChainHistoryPanel.add(bet365ScrollPane, BorderLayout.CENTER);

        TitledBorder chainsHistoryTitleBorder = BorderFactory.createTitledBorder("История проигранных цепочек");
        chainsHistoryTitleBorder.setTitleJustification(TitledBorder.CENTER);
        chainsHistoryTitleBorder.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 17)));
        chainsHistoryTitleBorder.setTitleColor(new Color(46, 139, 87));
        chainsHistoryTitleBorder.setBorder(new LineBorder(new Color(46, 139, 87), 2, true));
        this.setBorder(chainsHistoryTitleBorder);
        this.add(mainChainHistoryPanel);
    }

    void updatePanel(List<ChainBetting> list) {

        bet365Panel.removeAll();
        if(list==null){
            bet365Panel.revalidate();
            return;
        }

        for (ChainBetting chain : list) {
            JPanel chainBodyPanel = new JPanel(new FlowLayout());
            chainBodyPanel.setPreferredSize(new Dimension(930, 35));
            chainBodyPanel.setBackground(new Color(152, 251, 152));

            String matchName = chain.getFirstMatchName();
            String lastPeriodMatch = String.valueOf(chain.getLastBet().getMatchPeriod());
            String periodName = chain.getBetPeriod();

            JLabel idChain = new JLabel(String.valueOf(chain.getIdChain()));
            idChain.setHorizontalAlignment(SwingConstants.CENTER);
            idChain.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
            idChain.setVerticalAlignment(SwingConstants.TOP);
            idChain.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            idChain.setPreferredSize(new Dimension(30, 30));

            JLabel lastMatchChain = new JLabel(matchName);
            lastMatchChain.setHorizontalAlignment(SwingConstants.CENTER);
            lastMatchChain.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
            lastMatchChain.setVerticalAlignment(SwingConstants.TOP);
            lastMatchChain.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            lastMatchChain.setPreferredSize(new Dimension(450, 30));

            JLabel lastMatchPeriodChain = new JLabel(lastPeriodMatch);
            lastMatchPeriodChain.setHorizontalAlignment(SwingConstants.CENTER);
            lastMatchPeriodChain.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
            lastMatchPeriodChain.setVerticalAlignment(SwingConstants.TOP);
            lastMatchPeriodChain.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            lastMatchPeriodChain.setPreferredSize(new Dimension(80, 30));

            JLabel lastPeriodChain = new JLabel(periodName);
            lastPeriodChain.setHorizontalAlignment(SwingConstants.CENTER);
            lastPeriodChain.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
            lastPeriodChain.setVerticalAlignment(SwingConstants.TOP);
            lastPeriodChain.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            lastPeriodChain.setPreferredSize(new Dimension(80, 30));

            JLabel lastBetChain = new JLabel(String.valueOf(chain.getLastBetPrice()));
            lastBetChain.setHorizontalAlignment(SwingConstants.CENTER);
            lastBetChain.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
            lastBetChain.setVerticalAlignment(SwingConstants.TOP);
            lastBetChain.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            lastBetChain.setPreferredSize(new Dimension(70, 30));

            JLabel countBetChain = new JLabel(String.valueOf(chain.getCountBet()));
            countBetChain.setHorizontalAlignment(SwingConstants.CENTER);
            countBetChain.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
            countBetChain.setVerticalAlignment(SwingConstants.TOP);
            countBetChain.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            countBetChain.setPreferredSize(new Dimension(80, 30));

            JButton detailsButton = new JButton("Детали");
            detailsButton.setHorizontalAlignment(SwingConstants.CENTER);
            detailsButton.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
            detailsButton.setVerticalAlignment(SwingConstants.TOP);
            detailsButton.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            detailsButton.setPreferredSize(new Dimension(90, 30));
            detailsButton.addActionListener((e) -> {
                showDetails(chain);
            });

            chainBodyPanel.add(idChain);
            chainBodyPanel.add(lastMatchChain);
            chainBodyPanel.add(lastMatchPeriodChain);
            chainBodyPanel.add(lastPeriodChain);
            chainBodyPanel.add(lastBetChain);
            chainBodyPanel.add(countBetChain);
            chainBodyPanel.add(detailsButton);

            JPanel chainBodyPanelOut = new JPanel();
            chainBodyPanelOut.add(chainBodyPanel, BorderLayout.CENTER);
            chainBodyPanelOut.setMaximumSize(new Dimension(930, 35));
            chainBodyPanelOut.add(chainBodyPanel);
            bet365Panel.add(chainBodyPanelOut);
        }
        bet365Panel.revalidate();

        if (bet365ScrollPane != null) {
            mainChainHistoryPanel.remove(bet365ScrollPane);
        }
        if (scrollPaneDetails != null) {
            mainChainHistoryPanel.remove(scrollPaneDetails);
        }
        mainChainHistoryPanel.revalidate();
        mainChainHistoryPanel.add(bet365ScrollPane, BorderLayout.CENTER);
    }


    private void showDetails(ChainBetting chain) {
        if (bet365ScrollPane != null) {
            mainChainHistoryPanel.remove(bet365ScrollPane);
        }
        if (scrollPaneDetails != null) {
            mainChainHistoryPanel.remove(scrollPaneDetails);
        }

        mainChainHistoryPanel.revalidate();
        scrollPaneDetails = new JScrollPane(new ChainsDetailsPanelNew(chain.getList()));
        mainChainHistoryPanel.add(scrollPaneDetails, BorderLayout.CENTER);
        mainChainHistoryPanel.validate();
    }
}
