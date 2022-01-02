package UI.historyPage;

import betting.entity.Bet;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.text.SimpleDateFormat;
import java.util.*;

public class HistoryBetsTableModel implements TableModel {
    List<Bet> betList;
    private Set<TableModelListener> listeners = new HashSet<TableModelListener>();

    public HistoryBetsTableModel(List<Bet> list) {
        betList = list;
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    //==========================================
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    //=====================================
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Матч ставки";
            case 1:
                return "Сумма";
            case 2:
                return "Odd/Even";
            case 3:
                return "Вид";
            case 4:
                return "Период";
            case 5:
                return "Win/Loose";
            case 6:
                return "ID";
            case 7:
                return "Время";
            case 8:
                return "Счет";
            case 9:
                return "Копия";
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return betList.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Bet bet = betList.get(rowIndex);
        switch (columnIndex) {
            case 0: return bet.getMatchName();
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
                    return "ERROR";
                }
            case 6:
                return bet.getIdChain();
            case 7:
                return getTime(bet.getTimeBet());
            case 8:
                return bet.getScore();
        }
        return null;
    }

    public String getTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.applyPattern("HH:mm:ss ");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String format = dateFormat.format(new Date(time));
        return format;
    }

    @Override
    public int getColumnCount() {
        return 9;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }
}
