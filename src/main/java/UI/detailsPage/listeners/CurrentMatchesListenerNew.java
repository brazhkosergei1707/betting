package UI.detailsPage.listeners;

import entityes.MatchLive;

import java.util.ArrayList;
import java.util.List;

public class CurrentMatchesListenerNew {

    private static CurrentMatchesListenerNew currentMatchesListener;

    private CurrentMatchesListenerNew() {
    }

    public static CurrentMatchesListenerNew getCurrentMatchesListener() {
        if (currentMatchesListener != null) {
            return currentMatchesListener;
        } else {
            currentMatchesListener = new CurrentMatchesListenerNew();
            return currentMatchesListener;
        }
    }

    public void updateCurrentMatchesTable(List<MatchLive> listStorage) {


        List<MatchLive> list;
        if(listStorage!=null){
            list = listStorage;
        } else {
            list = new ArrayList<>();
        }

        System.out.println("Размер листа " + list.size());
//        CurrentMatchesPanel.matchesTable.setModel(new CurrentMatchesTableModel(list));
//        CurrentMatchesPanel.matchesTable.getColumnModel().getColumn(0).setMinWidth(200);
//        CurrentMatchesPanel.matchesTable.getColumnModel().getColumn(3).setMinWidth(120);
//        CurrentMatchesPanel.matchesTable.getColumnModel().getColumn(4).setMinWidth(15);
//        CurrentMatchesPanel.matchesTable.getColumnModel().getColumn(5).setMinWidth(120);
//        CurrentMatchesPanel.matchesTable.getColumnModel().getColumn(7).setMinWidth(10);
//        CurrentMatchesPanel.matchesTable.getColumnModel().getColumn(8).setMinWidth(10);
//        CurrentMatchesPanel.matchesTable.getColumnModel().getColumn(9).setMinWidth(20);
//        CurrentMatchesPanel.matchesTable.getColumnModel().getColumn(10).setMinWidth(10);
//        CurrentMatchesPanel.matchesTable.getColumnModel().getColumn(12).setMinWidth(30);
//        CurrentMatchesPanel.getCurrentMatchesPanel().revalidate();
    }
}
