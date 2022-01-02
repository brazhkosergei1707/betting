package UI.detailsPage;

import betting.entity.MatchLiveBetting;
import betting.enumeration.SelectionBet;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CurrentMatchesTableModel implements TableModel {
    private List<MatchLiveBetting> currentMatchesList;
    private SimpleDateFormat dateFormat;

    public CurrentMatchesTableModel(List<MatchLiveBetting> currentMatchesList) {
        this.currentMatchesList = currentMatchesList;
        dateFormat = new SimpleDateFormat();
        dateFormat.applyPattern("mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
    }

    @Override
    public int getRowCount() {
        return currentMatchesList.size();
    }

    @Override
    public int getColumnCount() {
        return 10;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "    Имя матча    ";
            case 1:
                return "Четверть";
            case 2:
                return "Время";
            case 3:
                return "Последняя ставка";
            case 4:
                return "Max";
            case 5:
                return "Варианты";
            case 6:
                return "Q1";
            case 7:
                return "Q2";
            case 8:
                return "Q3";
            case 9:
                return "Q4";
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Object.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MatchLiveBetting matchLive = currentMatchesList.get(rowIndex);
        String variants = "";
        switch (columnIndex) {
            case 0: {
                return matchLive.getNameOfMatch();
            }
            case 1:
                return matchLive.getCurrentPeriod();
            case 2: {
                if (matchLive.getTimeToQuarterFinish() == -6300000L) {
                    return "Break";
                } else {
                    return dateFormat.format(new Date(matchLive.getTimeToQuarterFinish()));
                }
            }
            case 3:
                return matchLive.getLasBetPriseString();
            case 4:
                return matchLive.getMaxBet();
            case 5: {
                for (SelectionBet selectionBet : matchLive.getPossibleBetSelectionBet()) {
                    variants = variants + selectionBet + " ";
                }
                return variants;
            }
            case 6:
                return matchLive.getMatchScoreStatistic().get(SelectionBet.Q1);
            case 7:
                return matchLive.getMatchScoreStatistic().get(SelectionBet.Q2);
            case 8:
                return matchLive.getMatchScoreStatistic().get(SelectionBet.Q3);
            case 9:
                return matchLive.getMatchScoreStatistic().get(SelectionBet.Q4);
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }
}
