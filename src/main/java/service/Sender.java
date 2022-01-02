package service;

import UI.startFrame.StartFrame;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.awt.*;
import java.util.Properties;

public class Sender {
    private boolean switchOn;
    private String username;
    private String password;
    private Properties props;
    private static Sender sender;
    private String addressFor;
    private double maxBet;

    private Sender(String username, String password){
        this.username=username;
        this.password=password;
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

    }

    public static void createSender(String username, String password){
        sender = new Sender(username,password);
    }

    public static Sender getSender(){
            return sender;
    }

    public static void delSender(){
        sender=null;
    }

    public void setAddressFor(String addressFor) {
        this.addressFor = addressFor;
    }

    public boolean setMaxBet(double maxBet, String addressFor){
        this.addressFor = addressFor;
       this.maxBet = maxBet;
       switchOn = true;
        try{
            String s = "Этот адрес будет использован для уведомлений, при соввершении ставки больше "+maxBet+" на бирже";
            send("BETTING",s);
            return  true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void send(String subject, String text){
        StartFrame.getInstance().showInformMessage("Отправляем сообщение ",new Color(46, 139, 87),true);
        Session session=Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username,password);
            }
        });
        try{
            Message message=new MimeMessage(session);
            //from
            message.setFrom(new InternetAddress(username));
            //to
            message.setRecipient(Message.RecipientType.TO, InternetAddress.parse(addressFor)[0]);
            //subject
            message.setSubject(subject);
            message.setText(text);
            //send message
            Transport.send(message);
            StartFrame.getInstance().showInformMessage("Сообщение было отправлено",new Color(144, 238, 144),false);
        }catch (MessagingException e){
            StartFrame.getInstance().showInformMessage("Ошибка во время отправки сообщения",new Color(220, 20, 60),false);
        }
    }

    public double getMaxBet() {
        return maxBet;
    }

    public boolean isSwitchOn() {
        return switchOn;
    }
}
