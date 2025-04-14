package burp;

import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import check.Overauth.JOverauthContextMenuItemActionListener;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ItemsProvider implements ContextMenuItemsProvider {
    @Override
    public java.util.List<Component> provideMenuItems(ContextMenuEvent event) {

        List<Component> jMenuItemList= new ArrayList<>();

        //设置发送验证码请求包的JMenuItem和相关操作

        JMenuItem jOverAuthLowAuth = new JMenuItem("SetLowAuth");
        jOverAuthLowAuth.addActionListener(new JOverauthContextMenuItemActionListener(event));
        jMenuItemList.add(jOverAuthLowAuth);

        JMenuItem jOverAuthIgnore = new JMenuItem("Ignore");
        jOverAuthIgnore.addActionListener(new JOverauthContextMenuItemActionListener(event));
        jMenuItemList.add(jOverAuthIgnore);

        JMenuItem jOverAuthBatchCheck = new JMenuItem("BatchCheck");
        jOverAuthBatchCheck.addActionListener(new JOverauthContextMenuItemActionListener(event));
        jMenuItemList.add(jOverAuthBatchCheck);


        return jMenuItemList;
    }
}
