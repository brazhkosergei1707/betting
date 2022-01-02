package bot.chain;

import UI.startFrame.StartFrame;
import betting.Betting;
import betting.entity.ChainBetting;
import betting.entity.MatchLiveBetting;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "chains")
public class ChainHolder {
    @XmlTransient
    private static Logger log = Logger.getLogger(ChainHolder.class);
    @XmlElement(name = "SITE")
    private String nameOfSite;

    @XmlElement(name = "currentChain")
    private List<ChainBetting> currentChainList;

    @XmlElement(name = "oldChain")
    private List<ChainBetting> historyChainList;

    public ChainHolder() {
        currentChainList = new ArrayList<>();
        historyChainList = new ArrayList<>();
    }

    private ChainHolder(Betting betting) {
        this.nameOfSite = betting.getNameOfSite();
        currentChainList = new ArrayList<>();
        historyChainList = new ArrayList<>();
    }

    public void saveChains() {
        String pathDriver = StartFrame.getPathToDriver();
        System.out.println(pathDriver);
        int i = pathDriver.lastIndexOf(File.separator);
        String pathFile = pathDriver.substring(0, i + 1) + nameOfSite + "-Chains.txt";
        log.info("Путь к файлу с информацией о текущих цепочках: " + pathFile);
        File file = new File(pathFile);

        try {
            boolean ok = file.exists();
            if (!ok) {
                ok = file.createNewFile();
            }
            if (ok) {
                log.info("Создаем новый файл и записываем информацию о текущих цепочках");
                JAXBContext context = JAXBContext.newInstance(ChainHolder.class);
                Marshaller marshaller = context.createMarshaller();
                marshaller.marshal(this, file);
            }
        } catch (IOException | JAXBException e) {
            log.info("Не удалось сохранить цепочки в файл.");
            e.printStackTrace();
        }
    }

    public static ChainHolder restoreChains(Betting betting) {
        String pathDriver = StartFrame.getPathToDriver();
        System.out.println(pathDriver);
        int i = pathDriver.lastIndexOf(File.separator);
        String pathFile = pathDriver.substring(0, i + 1) + betting.getNameOfSite() + "-Chains.txt";
        File file = new File(pathFile);
        Object chainHold = null;
        if (file.exists()) {
            try {
                JAXBContext context = JAXBContext.newInstance(ChainHolder.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                chainHold = unmarshaller.unmarshal(file);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        ChainHolder chainHolder = null;
        if (chainHold != null) {
            try {
                chainHolder = (ChainHolder) chainHold;
                System.out.println(chainHold);
            } catch (ClassCastException e) {
                e.printStackTrace();
                System.out.println("Проблема во время восстановления цепочек");
                log.error("Проблема во время восстановления цепочек");
            }
        }

        if (chainHolder == null) {
            chainHolder = new ChainHolder(betting);
            return chainHolder;
        } else {
            return chainHolder;
        }
    }

    public void connectMatchesToChain(Betting betting) {
        Map<String, MatchLiveBetting> bettingMatchLive = betting.getBettingMatchLive();
        for (ChainBetting chainBetting : currentChainList) {
            String matchLiveName = chainBetting.getMatchLiveName();
            if (matchLiveName != null) {
                if (bettingMatchLive.containsKey(matchLiveName)) {
                    bettingMatchLive.get(matchLiveName).setChain(chainBetting);
                } else {
                    chainBetting.setMatchLiveName(null, betting);
                }
            }
        }
    }

    public void addChain(ChainBetting chain) {
        boolean newChain = true;
        try {
            for (ChainBetting chainFromList : currentChainList) {
                if (chain.getMatchLiveName() == null) {
                    if (chainFromList.getMatchLiveName() == null) {
                        if (chain.getFirstMatchName().compareTo(chainFromList.getFirstMatchName()) == 0) {//косяк, нул поинтер
                            chainFromList = chain;
                            newChain = false;
                            break;
                        }
                    }
                } else {
                    if (chainFromList.getMatchLiveName() == null) {
                        if (chain.getFirstMatchName().compareTo(chainFromList.getFirstMatchName()) == 0) {
                            chainFromList = chain;
                            newChain = false;
                            break;
                        }
                    } else {
                        if (chain.getMatchLiveName().compareTo(chainFromList.getMatchLiveName()) == 0) {
                            chainFromList = chain;
                            newChain = false;
                            break;
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            log.info("Косяк с цепочками. ");
            System.out.println("Косяк с цепочками ");
        }


        for (ChainBetting chain1 : currentChainList) {
            if (chain1.getIdChain() == chain.getIdChain()) {
                newChain = false;
            }
        }

        if (newChain) {
            log.info("Добавлена цепочка с номером " + chain.getIdChain());
            System.out.println("Добавлена цепочка с номером " + chain.getIdChain());
            currentChainList.add(chain);
        }

        cleanCurrentChains();
        saveChains();
    }

    public void cleanCurrentChains() {
        boolean newHistoryChain = true;
        for (ChainBetting chain : currentChainList) {
            if (chain.isWin()) {
                log.info("Ищем нет ли такой же цепочки в хранилище. Матч цепочки: " + chain.getMatchLiveName());
                System.out.println("Ищем нет ли такой же цепочки в хранилище. Матч цепочки: " + chain.getMatchLiveName());
                for (ChainBetting historyChain : historyChainList) {
                    if (chain.getMatchLiveName() == null) {
                        if (historyChain.getMatchLiveName() == null) {
                            if (chain.getFirstMatchName().compareTo(historyChain.getFirstMatchName()) == 0) {
                                if (chain.getList().size() == historyChain.getList().size()) {
                                    newHistoryChain = false;
                                    break;
                                }
                            }
                        }
                    } else {
                        if (chain.getMatchLiveName().compareTo(historyChain.getFirstMatchName()) == 0) {
                            if (chain.getList().size() == historyChain.getList().size()) {
                                newHistoryChain = false;
                                break;
                            }
                        }
                    }
                }

                if (newHistoryChain) {
                    log.info("Удаляем цепочку с номером " + chain.getIdChain());
                    historyChainList.add(chain);
                }
            }
        }
        currentChainList.removeIf((e) -> e.isWin());
    }

    public List<ChainBetting> getCurrentChainList() {
        return currentChainList;
    }

    public List<ChainBetting> getHistoryChainList() {
        return historyChainList;
    }

    public void setCurrentChainList(List<ChainBetting> currentChainList) {
        this.currentChainList = currentChainList;
    }

    public void setHistoryChainList(List<ChainBetting> historyChainList) {
        this.historyChainList = historyChainList;
    }

    public String getNameOfSite() {
        return nameOfSite;
    }

    public void setNameOfSite(String nameOfSite) {
        this.nameOfSite = nameOfSite;
    }
}
