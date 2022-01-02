package UI.historyPage;

import betting.entity.Bet;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChainsDetailsTableModel implements TableModel {
    List<Bet> betList;
    private Set<TableModelListener> listeners = new HashSet<TableModelListener>();

    public ChainsDetailsTableModel(List<Bet> list){
        betList=list;
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch(columnIndex){
            case 0:return "Матч";
            case 1:return "Сумма ставки";
            case 2:return "Odd/Even";
            case 3:return "Вид";
            case 4:return "Период";
            case 5:return "Win/Loose";
            case 6:return "ID цепочки";
        }
        return null;
    }


    @Override
    public int getRowCount() {
        return betList.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Bet bet=betList.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return bet.getMatchName();
            case 1:
                return bet.getPrice();
            case 2:
                return bet.getOddEven();
            case 3:
                return bet.getSelectionBet();
            case 4:
                return bet.getMatchPeriod();
            case 5:
                if (bet.getWin() != null) {
                    if (bet.getWin()) {
                        return "WIN";
                    } else {
                        return "LOOSE";
                    }
                } else {
                    return "ХЗ";
                }
            case 6:
                return bet.getIdChain();
        }
        return null;
    }

    @Override
    public int getColumnCount() {
        return 7;
    }
    //    =================================================
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }
}
