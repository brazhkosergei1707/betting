package betting.UI;

import UI.AppTableRenderer;
import UI.detailsPage.CurrentMatchesTableModel;
import betting.entity.MatchLiveBetting;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CurrentMatchesPanel extends JPanel {

    private JTable matchesTable;

    CurrentMatchesPanel(){
        matchesTable = new JTable();
        matchesTable.setFont(new Font("Comic Sans MS",Font.ITALIC,12));
        matchesTable.setDefaultRenderer(Object.class,new AppTableRenderer());

        JScrollPane scrollPane=new JScrollPane(matchesTable);
        scrollPane.setPreferredSize(new Dimension(890,490));
        TitledBorder currentMatchesTitleBorder = BorderFactory.createTitledBorder("Матчи");
        currentMatchesTitleBorder.setTitleJustification(TitledBorder.CENTER);
        currentMatchesTitleBorder.setTitleFont((new Font("Comic Sans MS",Font.BOLD,12)));
        this.setBorder(currentMatchesTitleBorder);
        this.add(scrollPane, BorderLayout.CENTER);
    }



    void updateCurrentMatchesTable(java.util.List<MatchLiveBetting> listStorage) {
        List<MatchLiveBetting> list;
        if(listStorage!=null){
            list = listStorage;
        } else {
            list = new ArrayList<>();
        }

        matchesTable.setModel(new CurrentMatchesTableModel(list));
        matchesTable.getColumnModel().getColumn(0).setMinWidth(300);
        matchesTable.getColumnModel().getColumn(1).setMinWidth(15);
        matchesTable.getColumnModel().getColumn(2).setMinWidth(20);
        matchesTable.getColumnModel().getColumn(3).setMinWidth(100);
        matchesTable.getColumnModel().getColumn(4).setMinWidth(25);
        matchesTable.getColumnModel().getColumn(5).setMinWidth(150);
        matchesTable.getColumnModel().getColumn(6).setMinWidth(10);
        matchesTable.getColumnModel().getColumn(7).setMinWidth(10);
        matchesTable.getColumnModel().getColumn(8).setMinWidth(10);
        matchesTable.getColumnModel().getColumn(9).setMinWidth(10);
    }



}
