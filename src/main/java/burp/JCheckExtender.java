package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import check.Overauth.OverauthComponent;

public class JCheckExtender implements BurpExtension {

    public static MontoyaApi BurpAPI;

    @Override
    public void initialize(MontoyaApi montoyaApi) {

        BurpAPI = montoyaApi;
        BurpAPI.extension().setName("JCheck");
        BurpAPI.userInterface().registerSuiteTab("JCheck",new OverauthComponent().component());
        BurpAPI.http().registerHttpHandler(new RegisterHttpHandler());

        BurpAPI.userInterface().registerContextMenuItemsProvider(new ItemsProvider());

        BurpAPI.extension().registerUnloadingHandler(new UnloadingHandler());


    }
}
