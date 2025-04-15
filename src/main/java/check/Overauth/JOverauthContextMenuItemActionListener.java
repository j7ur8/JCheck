package check.Overauth;

import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.Range;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import static burp.JCheckExtender.BurpAPI;

public class JOverauthContextMenuItemActionListener implements ActionListener {
    private final ContextMenuEvent event;
    public JOverauthContextMenuItemActionListener(ContextMenuEvent event) {
        this.event = event;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getActionCommand().equals("BatchCheck")){
            new Thread(()->{
                for(HttpRequestResponse httpRequestResponse : event.selectedRequestResponses()){
                    if(httpRequestResponse.request().httpService().host().contains(Overauth.getOverauthMenuHostField().getText())){

                        HttpRequest httpRequest = httpRequestResponse.request().withHeader(HttpHeader.httpHeader("BatchCheck","1"));
                        BurpAPI.http().sendRequest(httpRequest);
                        BurpAPI.logging().logToOutput(httpRequestResponse.request().url());
                    }
                }
            }).start();
            return;
        }
        MessageEditorHttpRequestResponse messageEditorHttpRequestResponse = event.messageEditorRequestResponse().orElseThrow();
        HttpRequest request = messageEditorHttpRequestResponse.requestResponse().request();

        Range range = messageEditorHttpRequestResponse.selectionOffsets().orElseThrow();
        ByteArray selectBytes = request.toByteArray().subArray(range);
        String selectValue = selectBytes.toString();
        for(HttpParameter parameter:request.parameters()){
            String parameterName = parameter.name();
            HttpParameterType parameterType = parameter.type();
            if (e.getActionCommand().equals("SetLowAuth")) {
                if ("Cookie".equals(selectValue) && parameterType.equals(HttpParameterType.COOKIE)) {
                    Overauth.OverAuthLowParameterList.add(parameter);
                } else if (parameterName.equals(selectValue)) {
                    Overauth.OverAuthLowParameterList.add(parameter);
                }
            } else {
                if (parameterName.equals(selectValue)) {
                    Overauth.OverAuthIgnoreParameterList.add(parameter);
                }
            }
        }
        for(HttpHeader httpHeader:request.headers()){
            String headerName = httpHeader.name();
            if (e.getActionCommand().equals("SetLowAuth")) {
                if (headerName.equals(selectValue)) {
                    Overauth.OverAuthLowHeaderList.add(httpHeader);
                }

            }
        }
    }
}
