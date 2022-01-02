package betting.UI;

import UI.AppTableRenderer;
import UI.historyPage.HistoryBetsTableModel;
import betting.entity.Bet;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class HistoryPanel extends JPanel {

    private JTable historyTable;

    HistoryPanel() {
        historyTable = new JTable();
        historyTable.setFont(new Font("Comic Sans MS", Font.ITALIC, 12));
        historyTable.setDefaultRenderer(Object.class, new AppTableRenderer());
        JScrollPane mainScrollPane = new JScrollPane(historyTable);
        mainScrollPane.setBorder(BorderFactory.createEtchedBorder());
        mainScrollPane.setPreferredSize(new Dimension(890, 490));

        TitledBorder currentBetTitleBorder = BorderFactory.createTitledBorder("История ставок");
        currentBetTitleBorder.setTitleJustification(TitledBorder.CENTER);
        currentBetTitleBorder.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 12)));

        this.setBorder(currentBetTitleBorder);
        this.add(mainScrollPane);
    }

    void updateHistory(List<Bet> historyBets) {
        List<Bet> list;
        if (historyBets != null) {
            list = historyBets;
        } else {
            list = new ArrayList<>();
        }
//        List<Bet> todayBets = new ArrayList<>();
//        List<Bet> yesterdayBets = new ArrayList<>();
//        long currentTime = Storage.getCurrentTime();
//        for(Bet bet:historyBets){
//            if(bet.getTimeBet()>currentTime){
//              yesterdayBets.add(bet);
//            } else {
//                todayBets.add(bet);
//            }
//        }
//
//        Collections.sort(yesterdayBets);
//        Collections.sort(todayBets);
//
//        historyBets.clear();
//        for(Bet bet:yesterdayBets){
//            historyBets.add(bet);
//        }
//
//        for(Bet bet:todayBets){
//            historyBets.add(bet);
//        }
        Collections.sort(list);

        historyTable.setModel(new HistoryBetsTableModel(list));
        historyTable.getColumnModel().setColumnSelectionAllowed(true);
//        historyTable.getColumnModel().getColumn(0).
        historyTable.getColumnModel().getColumn(0).setMinWidth(300);
        historyTable.getColumnModel().getColumn(5).setMinWidth(10);
    }
}
