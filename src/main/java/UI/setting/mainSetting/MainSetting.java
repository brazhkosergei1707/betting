package UI.setting.mainSetting;

import UI.startFrame.StartFrame;
import entityes.Chain;
import bot.Storage;
import service.Sender;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class MainSetting extends JPanel {

//    private static JLabel currentMargin;

    private int numberBetInt;
    private double priceBet;
    private double totalBetPrice;

    private static MainSetting mainSetting;

    private static JLabel currentBetCountBeforeSendMail=new JLabel();

    private MainSetting(){
        this.setPreferredSize(new Dimension(1070,450));
        this.setLayout(new FlowLayout());
        createMainSettingPage();
    }

    public static MainSetting getMainSetting(){
        if(mainSetting!=null){
            return mainSetting;
        }else {
            mainSetting=new MainSetting();
            return mainSetting;
        }
    }

    private void createMainSettingPage(){

//        JLabel mainSettingLabel = new JLabel("Основные настройки");
//        mainSettingLabel.setPreferredSize(new Dimension(1065,30));
//        mainSettingLabel.setHorizontalTextPosition(SwingConstants.CENTER);
//        mainSettingLabel.setAlignmentX(CENTER_ALIGNMENT);
//        mainSettingLabel.setFont((new Font("Comic Sans MS",Font.BOLD,17)));
//        this.add(mainSettingLabel);
//
//        JPanel marginPanel=new JPanel(new FlowLayout());
//        marginPanel.setPreferredSize(new Dimension(1070,70));
////        currentMargin = new JLabel("0.1");;
////        currentMargin.setFont(new Font("Comic Sans MS",Font.BOLD,15));
////        currentMargin.setHorizontalAlignment(SwingConstants.CENTER);
////        currentMargin.setForeground(Color.red);
////        currentMargin.setPreferredSize(new Dimension(50,30));
//
//        JLabel label=new JLabel("Маржа");
//        label.setFont(new Font("Comic Sans MS",Font.BOLD,15));
//        label.setPreferredSize(new Dimension(50,30));
//
//        JTextField marginSetField = new JTextField("0.1");
//        marginSetField.setPreferredSize(new Dimension(40,30));
//
//        JButton setMarginButton=new JButton("ок");
//        setMarginButton.setPreferredSize(new Dimension(50,30));
//        setMarginButton.setFont((new Font("Comic Sans MS",Font.BOLD,14)));
//        setMarginButton.addActionListener((e)->{
//            String newMargin=marginSetField.getText();
//            try{
//            Double margin = Double.valueOf(newMargin);
//            StartFrame.getInstance().setMargin(margin);
//            showMargin(String.valueOf(StartFrame.getMargin()));
//            }catch (Exception e1){
//                showMargin("Wrong");
//            }
//        });
//
//        JLabel labelMinBet=new JLabel("Мин.ставка: ");
//        labelMinBet.setFont(new Font("Comic Sans MS",Font.BOLD,15));
//        labelMinBet.setPreferredSize(new Dimension(100,30));
//
//
//        JLabel currentMinBet = new JLabel("0.0");;
//        currentMinBet.setFont(new Font("Comic Sans MS",Font.BOLD,15));
//        currentMinBet.setHorizontalAlignment(SwingConstants.CENTER);
//        currentMinBet.setForeground(Color.red);
//        currentMinBet.setPreferredSize(new Dimension(50,30));
//
//        JTextField minBetSetField = new JTextField("0.0");
//        minBetSetField.setPreferredSize(new Dimension(30,30));
//
//        JButton minBetButton = new JButton("OK");
//        minBetButton.addActionListener(e->{
//            double minBet=0.0;
//            try{
//                minBet = Double.parseDouble(minBetSetField.getText());
//            }catch (Exception ex){
//                ex.printStackTrace();
//            }
//            StartFrame.setMinBet(minBet);
//            currentMinBet.setText(String.valueOf(StartFrame.getMinBet()));
//            currentMinBet.repaint();
//        });
//
//        JLabel betNumberLabel=new JLabel("Ставка:");
//        betNumberLabel.setPreferredSize(new Dimension(60,30));
//        betNumberLabel.setFont(new Font("Comic Sans MS",Font.BOLD,15));
//
//        JLabel numberBet=new JLabel("0");
//        numberBet.setHorizontalAlignment(SwingConstants.CENTER);
//        numberBet.setPreferredSize(new Dimension(40,30));
//        numberBet.setFont(new Font("Comic Sans MS",Font.BOLD,16));
//
//        JLabel priceBet=new JLabel("0.00");
//        priceBet.setHorizontalAlignment(SwingConstants.CENTER);
//        priceBet.setPreferredSize(new Dimension(100,30));
//        priceBet.setFont(new Font("Comic Sans MS",Font.BOLD,14));
//
//        JLabel totalWord=new JLabel("Total:");
//        totalWord.setPreferredSize(new Dimension(70,30));
//        totalWord.setFont(new Font("Comic Sans MS",Font.BOLD,15));
//
//        JLabel totalBankToMakeBet=new JLabel("0.00");
//        totalBankToMakeBet.setPreferredSize(new Dimension(100,30));
//        totalBankToMakeBet.setFont(new Font("Comic Sans MS",Font.BOLD,15));
//
//        JButton minusButton=new JButton("-");
//        minusButton.setFont((new Font("Comic Sans MS",Font.BOLD,12)));
//        minusButton.setPreferredSize(new Dimension(50,30));
//        minusButton.addActionListener((e -> {
//            double minBet=0.0;
//            try{
//                minBet = Double.parseDouble(minBetSetField.getText());
//            }catch (Exception ex){
//                ex.printStackTrace();
//            }
//
//            String newMargin=marginSetField.getText();
//            Double margin = 0.0;
//            try{
//                margin = Double.valueOf(newMargin);
//            }catch (Exception e1){
//                StartFrame.getInstance().showInformMessage("Wrong", Color.red,false);
//            }
//
//            numberBetInt=Integer.parseInt(numberBet.getText());
//            if(numberBetInt!=0){
//                numberBet.setText(String.valueOf(numberBetInt-1));
//                if(numberBetInt==1) {
//                    priceBet.setText(String.valueOf(0.00));
//                    totalBankToMakeBet.setText("0.00");
//                } else {
//                    double res = Chain.calcBetForMatchLive(minBet,margin,numberBetInt-2);
//                    priceBet.setText(String.valueOf(res));
//                    totalBankToMakeBet.setText(String.valueOf(totalPriceOfBets(minBet,margin,numberBetInt-1)));
//                }
//            }
//        }));
//
//        JButton plusButton=new JButton("+");
//        plusButton.setFont((new Font("Comic Sans MS",Font.BOLD,12)));
//        plusButton.setPreferredSize(new Dimension(50,30));
//        plusButton.addActionListener((e)->{
//            double minBet=0.0;
//            try{
//               minBet = Double.parseDouble(minBetSetField.getText());
//            }catch (Exception ex){
//                ex.printStackTrace();
//            }
//
//            String newMargin=marginSetField.getText();
//            Double margin = 0.0;
//            try{
//                margin = Double.valueOf(newMargin);
//            }catch (Exception e1){
//                StartFrame.getInstance().showInformMessage("Wrong", Color.red,false);
//            }
//
//            numberBetInt=Integer.parseInt(numberBet.getText());
//                numberBet.setText(String.valueOf(numberBetInt+1));
//                double res= Chain.calcBetForMatchLive(minBet,margin,numberBetInt);
//                priceBet.setText(String.valueOf(res));
//                totalBankToMakeBet.setText(String.valueOf(totalPriceOfBets(minBet,margin,numberBetInt+1)));
//        });
//
//        marginPanel.add(label);
////        marginPanel.add(currentMargin);
//        marginPanel.add(marginSetField);
//        marginPanel.add(setMarginButton);
//        marginPanel.add(Box.createRigidArea(new Dimension(30,30)));
//        marginPanel.add(labelMinBet);
//        marginPanel.add(currentMinBet);
//        marginPanel.add(minBetSetField);
//        marginPanel.add(minBetButton);
//        marginPanel.add(Box.createRigidArea(new Dimension(30,30)));
//        marginPanel.add(betNumberLabel);
//        marginPanel.add(minusButton);
//        marginPanel.add(numberBet);
//        marginPanel.add(plusButton);
//        marginPanel.add(priceBet);
//        marginPanel.add(totalWord);
//        marginPanel.add(totalBankToMakeBet);
//
//
//        TitledBorder marginPanelBorder = BorderFactory.createTitledBorder("Расчет ставок");
//        marginPanelBorder.setTitleFont((new Font("Comic Sans MS",Font.BOLD,14)));
//        marginPanelBorder.setTitleColor(new Color(46, 139, 87));
//        marginPanelBorder.setBorder(new LineBorder(new Color(46, 139, 87),1,true));
//        marginPanel.setBorder(marginPanelBorder);
//
//        this.add(marginPanel);
////=========================================================
//
//        JLabel maxBetMailFromLabel=new JLabel("Почтовый ящик для отравления сообщений");
//        maxBetMailFromLabel.setFont(new Font("Comic Sans MS",Font.BOLD,15));
//        maxBetMailFromLabel.setForeground(Color.blue);
//
//        JLabel currentMailFromLabel=new JLabel("Почтовый ящик не установлен");
//        currentMailFromLabel.setFont(new Font("Comic Sans MS",Font.BOLD,12));
//        currentMailFromLabel.setForeground(Color.RED);
//
//        JPanel mailFromPanel=new JPanel(new FlowLayout());
//        mailFromPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
//        mailFromPanel.setPreferredSize(new Dimension(1060,40));
//
//        JLabel emailFromLabel=new JLabel("Email:");
//        emailFromLabel.setPreferredSize(new Dimension(80,30));
//        emailFromLabel.setFont(new Font("Comic Sans MS",Font.BOLD,15));
//
//        JTextField emailFromField=new JTextField("Ваша почта");
//        emailFromField.setPreferredSize(new Dimension(250,30));
//        emailFromField.setFont((new Font("Comic Sans MS",Font.BOLD,13)));
//
//        JLabel emailFromPasswordLabel=new JLabel("Пароль");
//        emailFromPasswordLabel.setPreferredSize(new Dimension(80,30));
//        emailFromPasswordLabel.setFont((new Font("Comic Sans MS",Font.BOLD,15)));
//
//        JTextField emailFromPasswordField=new JTextField("Ваш пароль");
//        emailFromPasswordField.setPreferredSize(new Dimension(250,30));
//        emailFromPasswordField.setFont((new Font("Comic Sans MS",Font.BOLD,13)));
//
//        JButton mailFromConfirmButton=new JButton("OK");
//        mailFromConfirmButton.setPreferredSize(new Dimension(100,30));
//        mailFromConfirmButton.setFont((new Font("Comic Sans MS",Font.BOLD,14)));
//        mailFromConfirmButton.addActionListener((e)->{
//            String mail=emailFromField.getText();
//            String password=emailFromPasswordField.getText();
//            Sender.createSender(mail,password);
//            currentMailFromLabel.setForeground(new Color(46, 139, 87));
//            currentMailFromLabel.setText("Оповещения будут отправлены с "+mail);
//        });
//
//        mailFromPanel.add(emailFromLabel);
//        mailFromPanel.add(emailFromField);
//        mailFromPanel.add(Box.createRigidArea(new Dimension(100,30)));
//        mailFromPanel.add(emailFromPasswordLabel);
//        mailFromPanel.add(emailFromPasswordField);
//        mailFromPanel.add(Box.createRigidArea(new Dimension(100,30)));
//        mailFromPanel.add(mailFromConfirmButton);
//
//        JLabel maxBetMailToLabel=new JLabel("Почтовый ящик для получения сообщений");
//        maxBetMailToLabel.setFont(new Font("Comic Sans MS",Font.BOLD,15));
//        maxBetMailToLabel.setForeground(Color.blue);
//
//        JLabel currentMaxBetLabel=new JLabel("Почтовый ящик не установлен");
//        currentMaxBetLabel.setFont(new Font("Comic Sans MS",Font.BOLD,12));
//        currentMaxBetLabel.setForeground(Color.RED);
//
//        JPanel mailToPanel=new JPanel(new FlowLayout());
//        mailToPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
//        mailToPanel.setPreferredSize(new Dimension(1060,40));
//
//        JLabel emailToLabel=new JLabel("Email:");
//        emailToLabel.setPreferredSize(new Dimension(80,30));
//        emailToLabel.setFont((new Font("Comic Sans MS",Font.BOLD,15)));
//
//        JTextField emailToField=new JTextField("Ваша почта");
//        emailToField.setPreferredSize(new Dimension(250,30));
//        emailToField.setFont((new Font("Comic Sans MS",Font.BOLD,13)));
//
//        JLabel maxBetLabel=new JLabel("Максимальная ставка $:");
//        maxBetLabel.setPreferredSize(new Dimension(190,30));
//        maxBetLabel.setAlignmentX(RIGHT_ALIGNMENT);
//        maxBetLabel.setFont((new Font("Comic Sans MS",Font.BOLD,15)));
//
//        JTextField maxBetField=new JTextField();
//        maxBetField.setPreferredSize(new Dimension(140,30));
//        maxBetField.setFont((new Font("Comic Sans MS",Font.BOLD,13)));
//
//
//        JButton maxBetConfirmButton=new JButton("OK");
//        maxBetConfirmButton.setPreferredSize(new Dimension(100,30));
//        maxBetConfirmButton.setFont((new Font("Comic Sans MS",Font.BOLD,14)));
//        maxBetConfirmButton.addActionListener((e)->{
//            String email=emailToField.getText();
//            String maxBetString = maxBetField.getText();
//            try {
//                double maxBet = Double.parseDouble(maxBetString);
//                if(Sender.getSender()!=null){
//                    Sender.getSender().setMaxBet(maxBet,email);
//                    currentMaxBetLabel.setForeground(new Color(46, 139, 87));
//                    currentMaxBetLabel.setText("Сообщение отправлено на "+email);
//                    revalidate();
//                } else {
//                    currentMailFromLabel.setText("Установите почту для отправки сообщений");
//                }
//            }catch (Exception ex){
//                currentMaxBetLabel.setForeground(Color.red);
//                currentMaxBetLabel.setText("Неверно введена максимальная ставка: "+maxBetString+". Или ошибка в настройке ящика для отправления");
//                currentMailFromLabel.setForeground(Color.red);
//                currentMailFromLabel.setText("Переустановите параметры");
//                Sender.delSender();
//            }
//        });
//
//        mailToPanel.add(emailToLabel);
//        mailToPanel.add(emailToField);
//        mailToPanel.add(Box.createRigidArea(new Dimension(100,30)));
//        mailToPanel.add(maxBetLabel);
//        mailToPanel.add(maxBetField);
//        mailToPanel.add(Box.createRigidArea(new Dimension(100,30)));
//        mailToPanel.add(maxBetConfirmButton);
//
//        JPanel mailSenderPanel = new JPanel(new FlowLayout());
//        mailSenderPanel.setPreferredSize(new Dimension(1070,190));
//        TitledBorder mailSenderBorder = BorderFactory.createTitledBorder("Настройки уведомлений");
//        mailSenderBorder.setTitleFont((new Font("Comic Sans MS",Font.BOLD,14)));
//        mailSenderBorder.setTitleColor(new Color(46, 139, 87));
//        mailSenderBorder.setBorder(new LineBorder(new Color(46, 139, 87),1,true));
//        mailSenderPanel.setBorder(mailSenderBorder);
//
//        mailSenderPanel.add(Box.createRigidArea(new Dimension(250,30)));
//        mailSenderPanel.add(maxBetMailFromLabel);
//        mailSenderPanel.add(Box.createRigidArea(new Dimension(150,30)));
//        mailSenderPanel.add(currentMailFromLabel);
//        mailSenderPanel.add(mailFromPanel);
//        mailSenderPanel.add(Box.createRigidArea(new Dimension(250,30)));
//        mailSenderPanel.add(maxBetMailToLabel);
//        mailSenderPanel.add(Box.createRigidArea(new Dimension(100,30)));
//        mailSenderPanel.add(currentMaxBetLabel);
//        mailSenderPanel.add(mailToPanel);

//        this.add(mailSenderPanel);
    }


    private double totalPriceOfBets(double minBet,double margin,int betCount){
        double res = Chain.calcBetForMatchLive(minBet,margin,betCount-1);
        if(betCount==1){
            return Chain.calcBetForMatchLive(minBet,margin,betCount-1);
        } else {
            double temp=totalPriceOfBets(minBet,margin,--betCount);
            res=res+temp;
            return Math.round(res * 100.0) / 100.0;
        }
    }


    public void showMargin(String margin){
//        currentMargin.setText(margin);
    }

}
