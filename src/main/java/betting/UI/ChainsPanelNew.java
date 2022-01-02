package betting.UI;

import betting.Betting;
import betting.entity.ChainBetting;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class ChainsPanelNew extends JPanel {
    private JPanel mainPanel;

    public ChainsPanelNew() {

        this.setLayout(new FlowLayout());
        this.setPreferredSize(new Dimension(910, 525));
        JPanel chainPanelHeader = new JPanel(new FlowLayout());
        chainPanelHeader.setPreferredSize(new Dimension(890, 30));//new Dimension(895,520)

        JLabel l0 = new JLabel("ID");
        l0.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
        l0.setHorizontalAlignment(SwingConstants.RIGHT);
        l0.setForeground(Color.blue);
        l0.setPreferredSize(new Dimension(20, 30));

        JLabel l1 = new JLabel("Матч");
        l1.setHorizontalAlignment(SwingConstants.CENTER);
        l1.setFont(new Font("Comic Sans MS", Font.BOLD, 11));
        l1.setForeground(Color.blue);
        l1.setPreferredSize(new Dimension(420, 30));

        JLabel l2 = new JLabel("Выбор");
        l2.setHorizontalAlignment(SwingConstants.CENTER);
        l2.setFont(new Font("Comic Sans MS", Font.BOLD, 11));
        l2.setForeground(Color.blue);

        JLabel l21 = new JLabel("Период");
        l21.setHorizontalAlignment(SwingConstants.CENTER);
        l21.setFont(new Font("Comic Sans MS", Font.BOLD, 11));
        l21.setForeground(Color.blue);

        JLabel l3 = new JLabel("Посл.ставка");
        l3.setHorizontalAlignment(SwingConstants.CENTER);
        l3.setFont(new Font("Comic Sans MS", Font.BOLD, 11));
        l3.setForeground(Color.blue);

        JLabel l4 = new JLabel("След/необх");
        l4.setHorizontalAlignment(SwingConstants.CENTER);
        l4.setFont(new Font("Comic Sans MS", Font.BOLD, 11));
        l4.setForeground(Color.blue);

        JLabel l5 = new JLabel("Проиграно");
        l5.setHorizontalAlignment(SwingConstants.CENTER);
        l5.setFont(new Font("Comic Sans MS", Font.BOLD, 11));
        l5.setForeground(Color.blue);
        JLabel l6 = new JLabel("STOP");
        l6.setHorizontalAlignment(SwingConstants.CENTER);
        l6.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
        l6.setForeground(Color.blue);

        chainPanelHeader.add(l0);
        chainPanelHeader.add(l1);
        chainPanelHeader.add(l2);
        chainPanelHeader.add(l21);
        chainPanelHeader.add(l3);
        chainPanelHeader.add(l4);
        chainPanelHeader.add(l5);
        chainPanelHeader.add(l6);
        this.add(chainPanelHeader);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setPreferredSize(new Dimension(890, 455));
        this.add(scrollPane);

        TitledBorder currentChainsTitleBorder = BorderFactory.createTitledBorder("Цепочки проигранных ставок");
        currentChainsTitleBorder.setTitleJustification(TitledBorder.CENTER);
        currentChainsTitleBorder.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 12)));
        this.setBorder(currentChainsTitleBorder);
    }

    void updatePanel(List<ChainBetting> list, Betting betting) {

        if (list == null) {
            mainPanel.removeAll();
            mainPanel.validate();
            mainPanel.repaint();
            return;
        }

        mainPanel.removeAll();
        JLabel idChain;
        JLabel matchChain;
        JLabel selectionChain;
        JLabel matchPeriodChain;
        JLabel lastBetChain;
        JLabel nextBetChain;
        JLabel countLooseChain;
        JButton button;

        for (ChainBetting chain : list) {
            JPanel chainBodyPanel = new JPanel(new FlowLayout());
            chainBodyPanel.setMaximumSize(new Dimension(890, 30));

            if (chain.isWin()) {
                chainBodyPanel.setBackground(new Color(46, 139, 87));
            } else {
                chainBodyPanel.setBackground(Color.LIGHT_GRAY);
            }
            String matchName;
            String selectionName;
            if (chain.getMatchLiveName() != null) {
                matchName = chain.getMatchLiveName();
                selectionName = chain.getBetPeriod();
            } else {
                matchName = chain.getFirstMatchName();
                selectionName = "ИЩЕМ";
            }
            String matchPeriod = "mistake";
            try {
                matchPeriod = String.valueOf(chain.getLastBet().getMatchPeriod());
            } catch (Exception e) {
                e.printStackTrace();
            }

            idChain = new JLabel(String.valueOf(chain.getIdChain()));
            idChain.setVerticalAlignment(SwingConstants.TOP);
            idChain.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            idChain.setPreferredSize(new Dimension(30, 30));

            matchChain = new JLabel(matchName);
            matchChain.setHorizontalAlignment(SwingConstants.CENTER);
            matchChain.setVerticalAlignment(SwingConstants.TOP);
            matchChain.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            matchChain.setPreferredSize(new Dimension(410, 30));

            selectionChain = new JLabel(selectionName);
            selectionChain.setVerticalAlignment(SwingConstants.TOP);
            selectionChain.setPreferredSize(new Dimension(50, 30));
            selectionChain.setHorizontalAlignment(SwingConstants.RIGHT);
            selectionChain.setVerticalAlignment(SwingConstants.TOP);
            matchChain.setFont(new Font("Comic Sans MS", Font.BOLD, 12));

            matchPeriodChain = new JLabel(matchPeriod);
            matchPeriodChain.setHorizontalAlignment(SwingConstants.RIGHT);
            matchPeriodChain.setVerticalAlignment(SwingConstants.TOP);
            matchPeriodChain.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            matchPeriodChain.setPreferredSize(new Dimension(60, 30));

            String s = null;
            try {
                s = String.valueOf(chain.getLastBet().getPrice());
            } catch (Exception e) {
                e.printStackTrace();
            }

            lastBetChain = new JLabel(s);
            lastBetChain.setHorizontalAlignment(SwingConstants.RIGHT);
            lastBetChain.setVerticalAlignment(SwingConstants.TOP);
            lastBetChain.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            lastBetChain.setPreferredSize(new Dimension(70, 30));

//            nextBetChain=new JLabel(String.valueOf(chain.getNextBetPrice(StartFrame.getSwitcher(parser).getStorage().getMinBet())));
            nextBetChain = new JLabel("TEST");

            nextBetChain.setHorizontalAlignment(SwingConstants.RIGHT);
            nextBetChain.setVerticalAlignment(SwingConstants.TOP);
            nextBetChain.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            nextBetChain.setPreferredSize(new Dimension(80, 30));

            countLooseChain = new JLabel(String.valueOf(chain.getCountBet()));
            countLooseChain.setHorizontalAlignment(SwingConstants.RIGHT);
            countLooseChain.setVerticalAlignment(SwingConstants.TOP);
            countLooseChain.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
            countLooseChain.setPreferredSize(new Dimension(60, 30));

            button = new JButton("stop");
            button.setHorizontalAlignment(SwingConstants.RIGHT);
            button.setVerticalAlignment(SwingConstants.TOP);
            button.setFont(new Font("Comic Sans MS", Font.BOLD, 10));
            button.setForeground(Color.red);
            button.setPreferredSize(new Dimension(55, 30));
            button.addActionListener((e) -> {
                chainBodyPanel.setBackground(new Color(46, 139, 87));
                chain.setWin(true, betting);
            });

            chainBodyPanel.add(idChain);
            chainBodyPanel.add(matchChain);
            chainBodyPanel.add(selectionChain);
            chainBodyPanel.add(matchPeriodChain);
            chainBodyPanel.add(lastBetChain);
            chainBodyPanel.add(nextBetChain);
            chainBodyPanel.add(countLooseChain);
            chainBodyPanel.add(Box.createHorizontalStrut(20));
            chainBodyPanel.add(button);
            mainPanel.add(chainBodyPanel);
        }
        mainPanel.validate();
        mainPanel.repaint();
    }
}
