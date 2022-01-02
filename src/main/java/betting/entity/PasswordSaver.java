package betting.entity;

import betting.UI.BettingSetting;
import UI.startFrame.StartFrame;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.io.IOException;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "passwords")
public class PasswordSaver {

    @XmlElement(name = "data")
    private
    String[][] map;


    private PasswordSaver() {
        map= new String[5][];
        for(int k=0;k<map.length;k++){
            map[k]=new String[10];
            for(int i=0;i<map[k].length;i++){
                map[k][i]= "";
            }
        }
    }

    public void savePasswords(int parserNumber,int fieldNumber, String username, String password) {
        map[parserNumber][fieldNumber] = username;
        map[parserNumber][fieldNumber+5] = password;
    }

    public void savePasswordSaverToFile() {
        String pathDriver = StartFrame.getPathToDriver();
        System.out.println(pathDriver);
        int i = pathDriver.lastIndexOf(File.separator);
        String pathFile = pathDriver.substring(0, i + 1) + "userData.txt";
        File file = new File(pathFile);
        try {
            boolean ok = file.exists();
            if (!ok) {
                ok = file.createNewFile();
            }

            if (ok) {
                JAXBContext context = JAXBContext.newInstance(PasswordSaver.class);
                Marshaller marshaller = context.createMarshaller();
                marshaller.marshal(this, file);
            }
        } catch (IOException | JAXBException e) {
            e.printStackTrace();
        }
    }

    public static PasswordSaver restorePasswords() {
        String pathDriver = StartFrame.getPathToDriver();
        System.out.println(pathDriver);
        int i = pathDriver.lastIndexOf(File.separator);
        String pathFile = pathDriver.substring(0, i + 1) + "userData.txt";
        File file = new File(pathFile);
        Object passwordsSaverObject = null;
        if (file.canRead()) {
            try {
                JAXBContext context = JAXBContext.newInstance(PasswordSaver.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                passwordsSaverObject = unmarshaller.unmarshal(file);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        PasswordSaver passwordSaver = null;
        if (passwordsSaverObject != null) {
            try {
                passwordSaver = (PasswordSaver) passwordsSaverObject;
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
        if(passwordSaver!= null){
            return passwordSaver;
        }else {
            passwordSaver = new PasswordSaver();
            return passwordSaver;
        }
    }

    public void setPasswordsToFields() {
        for(int parserNumber=0;parserNumber<map.length;parserNumber++){
            for(int i = 0;i<5;i++ ){
                String username = map[parserNumber][i];
                String password = map[parserNumber][i+5];
                BettingSetting.getBettingSetting().setField(parserNumber,i,username,password);
            }
        }
    }

    public String[][] getMap() {
        return map;
    }

    public  void setMap(String[][] map) {
        this.map = map;
    }
}
