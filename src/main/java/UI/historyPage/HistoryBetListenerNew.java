package UI.historyPage;

import betting.entity.Bet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryBetListenerNew {
    private static HistoryBetListenerNew historyBetListenerNew;

    private HistoryBetListenerNew() {
    }

    public static HistoryBetListenerNew getHistoryBetListenerNew() {
        if (historyBetListenerNew != null) {
            return historyBetListenerNew;
        } else {
            historyBetListenerNew = new HistoryBetListenerNew();
            return historyBetListenerNew;
        }
    }

    public void updateHistoryBetTable(List<Bet> listStorage) {
        List<Bet> list;
        if(listStorage!=null){
            list = listStorage;
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

        System.out.println("History bet list size " + list.size());
        Collections.sort(list);

//        HistoryBetPanelNew.bet365Table.setModel(new HistoryBetsTableModel(list));
//        HistoryBetPanelNew.bet365Table.getColumnModel().getColumn(0).setMinWidth(300);
//        HistoryBetPanelNew.bet365Table.getColumnModel().getColumn(5).setMinWidth(10);
    }
}
