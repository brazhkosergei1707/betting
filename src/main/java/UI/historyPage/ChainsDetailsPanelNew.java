package UI.historyPage;

import UI.AppTableRenderer;
import betting.entity.Bet;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;

public class ChainsDetailsPanelNew extends JPanel{
    private JTable table;
    public ChainsDetailsPanelNew(List<Bet> list){
        TableModel model=new ChainsDetailsTableModel(list);
        table=new JTable(model);
        table.setFont(new Font("Comic Sans MS",Font.ITALIC,13));
        table.setDefaultRenderer(Object.class,new AppTableRenderer());
        table.getColumnModel().getColumn(0).setMinWidth(300);
        JScrollPane scrollPane=new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(950,390));
        this.add(scrollPane, BorderLayout.CENTER);
    }
}
