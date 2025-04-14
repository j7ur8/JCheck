package components;

//import jrcet.frame.Asset.Asset;

import check.Overauth.Overauth;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class DiyJButton extends JButton implements  ActionListener {

    public DiyJButton(String text) {
        setText(text);
        setFocusPainted(false);
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(120,30));
        addActionListener(this);
        setFont(new Font("微软雅黑", Font.PLAIN,14));
    }

    @Override
    public void actionPerformed(ActionEvent e){


        switch (((DiyJButton) e.getSource()).getName()) {


            case "OverauthMenuCheckButton" -> {
                if(Overauth.OverauthCheck){
                    setBackground(Color.RED);
                    setText("OFF");
                }else{
                    setBackground(Color.GREEN);
                    setText("ON");
                }
                Overauth.OverauthCheck = !Overauth.OverauthCheck;
            }

            case "OverauthMenuClearButton" -> {
                Overauth.clearOverauthTable();
            }

            case "OverauthMenuExportButton" -> {
                Overauth.exportVulData();
            }

        }

    }

}
