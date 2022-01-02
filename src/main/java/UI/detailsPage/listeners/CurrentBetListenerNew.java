package UI.detailsPage.listeners;

public class CurrentBetListenerNew {
    private static CurrentBetListenerNew currentBetListenerNew;

    private CurrentBetListenerNew(){

    }

    public static CurrentBetListenerNew getCurrentBetListenerNew() {

        if(currentBetListenerNew!=null) {
            return currentBetListenerNew;
        }else {
            currentBetListenerNew = new CurrentBetListenerNew();
            return currentBetListenerNew;
        }
    }


}
