package betting.UI;

import UI.AppTableRenderer;
import UI.detailsPage.CurrentBetsTableModel;
import betting.entity.Bet;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurrentBetPanel extends JPanel {
    private JTable betTable;

    public CurrentBetPanel(){

        betTable = new JTable();
        betTable.setFont(new Font("Comic Sans MS",Font.ITALIC,12));
        betTable.setDefaultRenderer(Object.class,new AppTableRenderer());

        JScrollPane scrollPane=new JScrollPane(betTable);
        scrollPane.setPreferredSize(new Dimension(890,490));

        TitledBorder currentBetTitleBorder = BorderFactory.createTitledBorder("Ставки");
        currentBetTitleBorder.setTitleJustification(TitledBorder.CENTER);
        currentBetTitleBorder.setTitleFont((new Font("Comic Sans MS",Font.BOLD,12)));
        this.setBorder(currentBetTitleBorder);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    public void updateCurrentBetTable(java.util.List<Bet> listStorage) {
        List<Bet> list;
        if(listStorage!=null){
            list = listStorage;
        } else {
            list = new ArrayList<>();
        }

        Collections.sort(list);
        betTable.setModel(new CurrentBetsTableModel(list));
        betTable.getColumnModel().getColumn(0).setMinWidth(300);
        betTable.getColumnModel().getColumn(4).setMinWidth(10);
    }

}
