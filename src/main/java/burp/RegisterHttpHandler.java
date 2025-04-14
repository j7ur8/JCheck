package burp;

import burp.api.montoya.core.Annotations;
import burp.api.montoya.extension.ExtensionUnloadingHandler;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.requests.HttpRequest;
import check.Overauth.Overauth;
import utils.StringUtil;

import java.util.Objects;

import static burp.JCheckExtender.BurpAPI;


public class RegisterHttpHandler implements HttpHandler {

    public static Thread OverAuthTabelRefreshThread;

    public String[] BlackExtensionList = new String[]{
            "png","jpg","jpeg","gif","txt","html","pdf","xls","xlsx","word","ppt","zip","xml","gif","css","svg","otf","woff","woff2","ico","tff","js"
    };

    public RegisterHttpHandler(){

    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {

        String requestPath = requestToBeSent.path().split("\\?")[0];

        //不接受Extensions和Intruder的流量
        HttpRequest httpRequest = requestToBeSent;
        String requestTool = requestToBeSent.toolSource().toolType().toolName();
        switch (requestTool) {
            case "Intruder" -> {
                return RequestToBeSentAction.continueWith(requestToBeSent);
            }
            case "Extensions" -> {
                if(requestToBeSent.hasHeader("BatchCheck")){
                    httpRequest = httpRequest.withRemovedHeader(HttpHeader.httpHeader("BatchCheck","1"));
                }else{
                    return RequestToBeSentAction.continueWith(requestToBeSent);
                }
            }
        }

        //不接受OPTIONS请求
        if(Objects.equals(requestToBeSent.method(), "OPTIONS")){
            return RequestToBeSentAction.continueWith(requestToBeSent);
        };

        //不接受静态文件的请求
        for(String ext: BlackExtensionList){
            if(requestPath.endsWith(ext)){
                return RequestToBeSentAction.continueWith(requestToBeSent);
            }
        }

        //javascript检查
//        if(requestPath.endsWith("js")){
//            if( JavascriptCheck && !JavascriptCheckedUrlList.contains(requestUrl)){
//                return RequestToBeSentAction.continueWith(
//                        requestToBeSent.withRemovedHeader("If-Modified-Since"),
//                        requestToBeSent.annotations().withNotes(JAVASCRIPT+"¥¥"+requestUrl)
//                );
//            }
//            return RequestToBeSentAction.continueWith(requestToBeSent);
//        }
//
//        //sprintboot检查
        String springbootNote = "";
//        if( SpringbootCheck && !SpringbootCheckedUrlList.contains(requestUrl)){
//            springbootNote = springbootCheckRequest(requestToBeSent);
//        }
//
//
//        //fastjson检查
        String fastjsonNote = "";
//
//        if(FastjsonCheck && !FastjsonCheckUrlList.contains(requestUrl)){
//            fastjsonNote = fastjsonCheckRequest(requestToBeSent);
//        }

        //Overauth检查过的Url
        String authCheckNote = "";
        if(Overauth.OverauthCheck){
            authCheckNote = Overauth.authCheckRequest(requestToBeSent);
        }

        Annotations requestAnnotations = requestToBeSent.annotations().withNotes(
                fastjsonNote+","+authCheckNote+","+springbootNote
        );

        return RequestToBeSentAction.continueWith(httpRequest, requestAnnotations);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {

        String requestNotes = responseReceived.annotations().notes();

        //判断是否为OverAuth期望的返回包
        String overAuthRequestNumber = getRequestNumber(Overauth.AUTH, requestNotes);
        if(!overAuthRequestNumber.equals("") && !overAuthRequestNumber.equals("null")){
            Overauth.authCheckResponse(responseReceived,overAuthRequestNumber);
        }

        //判断是否为Sprintboot期望的返回包
//        String springbootRequestNumber = getRequestNumber(SPRINGBOOT, requestNotes);
//        if(!StringUtil.isBlank(springbootRequestNumber)){
//            springbootCheckResponse(responseReceived,springbootRequestNumber);
//        }

        return ResponseReceivedAction.continueWith(responseReceived);
    }


    public static String getRequestNumber(String type,String notes){
        if(StringUtil.isBlank(notes)){
            return "";
        }
        String[] noteArray = notes.split(",");
        for(String note: noteArray){
            if(note.startsWith(type)){
                return note.replace(type,"");
            }
        }
        return "";
    }

}
