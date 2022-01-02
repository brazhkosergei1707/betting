package UI.detailsPage;

import UI.detailsPage.listeners.CurrentBetListenerNew;
import UI.detailsPage.listeners.CurrentMatchesListenerNew;
import betting.UI.ChainsPanelNew;
import betting.UI.CurrentBetPanel;
import betting.UI.CurrentMatchesPanel;
import bot.Storage;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class DetailsPanel extends JPanel {
    Storage storage;
    private static DetailsPanel detailsPanel;
    public static TitledBorder detailsPanelTitleBorder;

    CurrentMatchesPanel currentMatchesPanel;
    CurrentBetPanel currentBetPanel;
    ChainsPanelNew chainsPanelNew;

    CurrentBetListenerNew currentBetListenerNew = CurrentBetListenerNew.getCurrentBetListenerNew();
    CurrentMatchesListenerNew currentMatchesListenerNew = CurrentMatchesListenerNew.getCurrentMatchesListener();

    private DetailsPanel() {
        JPanel allDataPanel = new JPanel();
        allDataPanel.setLayout(new BoxLayout(allDataPanel, BoxLayout.Y_AXIS));
        allDataPanel.add(currentMatchesPanel);
        allDataPanel.add(currentBetPanel);
        allDataPanel.add(chainsPanelNew);

        JScrollPane mainScrollPane = new JScrollPane(allDataPanel);
        mainScrollPane.setPreferredSize(new Dimension(1070, 480));
        detailsPanelTitleBorder = BorderFactory.createTitledBorder("Детали биржи");
        detailsPanelTitleBorder.setTitleJustification(TitledBorder.CENTER);
        detailsPanelTitleBorder.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 17)));
        detailsPanelTitleBorder.setTitleColor(new Color(46, 139, 87));
        detailsPanelTitleBorder.setBorder(new LineBorder(Color.GRAY, 2, true));
        this.setBorder(detailsPanelTitleBorder);
//        this.setBackground(new Color(240, 255, 240));
        this.add(mainScrollPane);
    }

    public static DetailsPanel getDetailsPanel() {
        if (detailsPanel != null) {
            return detailsPanel;
        } else {
            detailsPanel = new DetailsPanel();
            return detailsPanel;
        }
    }

    public void updateDetails(int number) {
//        if (StartFrame.getSwitcher(BettingSetting.parser,number) != null) {
//            storage = StartFrame.getSwitcher(BettingSetting.parser,number).getStorage();
//            currentMatchesListenerNew.updateCurrentMatchesTable(storage.getCurrentMatchesList());
//            currentBetListenerNew.updateCurrentBetTable(storage.getCurrentBets());
//            chainsPanelNew.updatePanel(storage.getChainsInWork(), BettingSetting.parser);
//        } else {
//            currentMatchesListenerNew.updateCurrentMatchesTable(null);
//            currentBetListenerNew.updateCurrentBetTable(null);
//            chainsPanelNew.updatePanel(null, BettingSetting.parser);
////            StartFrame.getInstance().showInformMessageError("Биржа не подлючена.");
//        }
    }
}
