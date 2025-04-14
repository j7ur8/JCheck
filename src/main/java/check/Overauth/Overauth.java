package check.Overauth;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.Http;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import components.DiyJList;
import components.DiyJLogTable;
import utils.Helper;
import utils.StringUtil;

import javax.swing.*;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static burp.JCheckExtender.BurpAPI;
import static check.Overauth.OverauthComponent.*;

import org.apache.commons.text.similarity.JaccardSimilarity;

public class Overauth {

    private static final ReentrantLock RequestIdLock = new ReentrantLock();

    private static final ReentrantLock AddLock = new ReentrantLock();

    private static Integer RequestId = -1;

    public static final String AUTH = "AUTH";
    public static boolean OverauthCheck = false;

    public static String[] Headers = {"#","Tool","Method","Host","Path","Code","Length","RequestTime","ResponseTime","LowAuth","UnAuth"};
    public static Map<String, String> OverAuthPathMap = Collections.synchronizedMap(new HashMap<>());
    public static Map<String, ArrayList<String>> OverAuthPathParameterMap = Collections.synchronizedMap(new HashMap<>());
    public static Map<String, Map<String, String>> OverAuthRowMap = Collections.synchronizedMap(new HashMap<>());
    public static Map<String, Map<String, HttpRequest>> OverAuthRequestMap = Collections.synchronizedMap(new HashMap<>());
    
    public static Map<String, Map<String, HttpResponse>> OverAuthResponseMap = Collections.synchronizedMap(new HashMap<>());

    public static Map<String, ArrayList<HttpParameter>> OverAuthParameterMap = Collections.synchronizedMap(new HashMap<>());
    public static List<HttpParameter> OverAuthLowParameterList = Collections.synchronizedList(new ArrayList<>());
    public static List<HttpHeader> OverAuthLowHeaderList = Collections.synchronizedList(new ArrayList<>());
//    public static List<HttpHeader> OverAuthIgnoreHeaderList = Collections.synchronizedList(new ArrayList<>());
    public static List<HttpParameter> OverAuthIgnoreParameterList = Collections.synchronizedList(new ArrayList<>());

    public static String authCheckRequest(HttpRequestToBeSent requestToBeSent){

        String RequestHost = requestToBeSent.httpService().host();
        String overauthTargetHost = getOverauthMenuHostField().getText();
        if(Objects.equals(overauthTargetHost, "") || !RequestHost.contains(overauthTargetHost)){
            return "";
        }

        String highAuth = getOverauthMenuHighauthField().getText();
        String lowAuth  = getOverauthMenuLowauthField().getText();

        if(StringUtil.isBlank(highAuth) && OverAuthLowParameterList.size()==0 && OverAuthLowHeaderList.size()==0){
            return "";
        }


//        String pathWithoutQuery = BurpAPI.utilities().urlUtils().decode(requestToBeSent.pathWithoutQuery());

        String pathCheck = BurpAPI.utilities().urlUtils().decode(requestToBeSent.withRemovedParameters(OverAuthIgnoreParameterList).path());
        int queryPosition = pathCheck.indexOf("?");
        String queryString = "";
        String pathWithoutQuery;
        if(queryPosition>0){
            pathWithoutQuery = pathCheck.substring(0, queryPosition);
            queryString = pathCheck.substring(queryPosition+1);
        }else{
            pathWithoutQuery = pathCheck;
        }

        String nowQueryString = Helper.replaceNumeric(queryString,"*Numeric*");
        if(OverAuthRowMap.containsKey(pathWithoutQuery)){
            BurpAPI.logging().logToOutput("已经检查过："+pathWithoutQuery);
            return "";
        } else if(OverAuthPathParameterMap.containsKey(pathWithoutQuery) && OverAuthPathParameterMap.get(pathWithoutQuery).contains(nowQueryString)){
            BurpAPI.logging().logToOutput("已经检查过："+pathWithoutQuery+"?"+nowQueryString);
            return "";
        }

        String RequestNumber = Integer.toString(getOverauthRequestId());
        String RequestTool   = requestToBeSent.toolSource().toolType().toolName();
        String RequestMethod = requestToBeSent.method();
        String RequestPath   = BurpAPI.utilities().urlUtils().decode(requestToBeSent.path());
        String RequestTime   = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());


        HttpRequest LowAuthRequest = HttpRequest.httpRequest(requestToBeSent.withUpdatedParameters(OverAuthLowParameterList).withUpdatedHeaders(OverAuthLowHeaderList).toString().replace(highAuth,lowAuth)).withService(requestToBeSent.httpService()) ;

        HttpRequest UnAuthRequest = HttpRequest.httpRequest(requestToBeSent.withRemovedParameters(OverAuthLowParameterList).withRemovedHeaders(OverAuthLowHeaderList).toString().replace(highAuth,"")).withService(requestToBeSent.httpService());

        Map<String, String> rowMap = new HashMap<>(){
            {
                put("#", RequestNumber);
                put("Tool", RequestTool);
                put("Method", RequestMethod);
                put("Host", RequestHost);
                put("Path",  RequestPath);
                put("Code", "");
                put("Length", "");
                put("RequestTime", RequestTime);
                put("ResponseTime", "");
                put("LowAuth", "");
                put("UnAuth", "");
            }
        };

        Map<String, HttpRequest> requestMap = new HashMap<>(){
            {
                put("HighAuth", requestToBeSent);
                put("LowAuth", LowAuthRequest);
                put("UnAuth", UnAuthRequest);
            }
        };


        //设置有水平越权的字段
        ArrayList<HttpParameter> parameters = new ArrayList<>();
        for(ParsedHttpParameter highAuthParameter : requestToBeSent.parameters()){
            if(highAuthParameter.type()!= HttpParameterType.COOKIE && !StringUtil.isBlank(highAuthParameter.value()) && Helper.isNumeric(highAuthParameter.value())){
                parameters.add(highAuthParameter);
            }
        }


        if(OverAuthPathParameterMap.containsKey(pathWithoutQuery)){
            OverAuthPathParameterMap.get(pathWithoutQuery).add(nowQueryString);
        }else{
            OverAuthPathParameterMap.put(pathWithoutQuery, new ArrayList<>(Collections.singleton(nowQueryString)));
        }

//        BurpAPI.logging().logToOutput(String.valueOf(OverAuthPathParameterMap.get(pathWithoutQuery).size()));
//        BurpAPI.logging().logToOutput(requestToBeSent.query());
//        BurpAPI.logging().logToOutput(pathQueryString);
        OverAuthPathMap.put(RequestNumber, RequestPath);
        OverAuthRowMap.put(RequestPath, rowMap);
        OverAuthRequestMap.put(RequestPath, requestMap);
        OverAuthParameterMap.put(RequestPath, parameters);

        refreshOverauthTable();
        return AUTH+RequestNumber;
    }

    public static void authCheckResponse(HttpResponseReceived responseReceived, String RequestNumber){

        String ResponseLength = Integer.toString(responseReceived.body().length());
        String ResponseCode   = String.valueOf(responseReceived.statusCode());
        String ResponseTime   = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

       
        String RequestPath = OverAuthPathMap.get(RequestNumber);

        Map<String, HttpResponse> responseMap = Collections.synchronizedMap(new HashMap<>(){
            {
                put("HighAuth", responseReceived);
                put("LowAuth", null);
                put("UnAuthRequest", null);
            }
        });
        OverAuthResponseMap.put(RequestPath, responseMap);

        Map<String, String> rowMap = OverAuthRowMap.get(RequestPath);
        rowMap.put("Code", ResponseCode);
        rowMap.put("Length",ResponseLength);
        rowMap.put("ResponseTime", ResponseTime);

        new Thread(() -> {
            try {
                HttpRequestResponse httpRequestResponse = BurpAPI.http().sendRequest(OverAuthRequestMap.get(RequestPath).get("LowAuth"));
                HttpResponse lowAuthResponse = httpRequestResponse.response();
                OverAuthResponseMap.get(RequestPath).put("LowAuth", httpRequestResponse.response());
                double simiInt = new JaccardSimilarity().apply(responseReceived.bodyToString(),lowAuthResponse.bodyToString());
                if(simiInt>0.9){
                    OverAuthRowMap.get(RequestPath).put("LowAuth","True");
                }
                refreshOverauthTable();
            } catch (Exception var2) {
                BurpAPI.logging().logToError(var2);
            }

        }).start();

        new Thread(() -> {
            try {
                HttpRequestResponse httpRequestResponse = BurpAPI.http().sendRequest(OverAuthRequestMap.get(RequestPath).get("UnAuth"));
                HttpResponse unAuthResponse = httpRequestResponse.response();
                OverAuthResponseMap.get(RequestPath).put("UnAuth", unAuthResponse);
                double simiInt = new JaccardSimilarity().apply(responseReceived.bodyToString(),unAuthResponse.bodyToString());
                if(simiInt>0.9){
                    OverAuthRowMap.get(RequestPath).put("UnAuth","True");
                }
                refreshOverauthTable();
            } catch (Exception var2) {
                BurpAPI.logging().logToError(var2);
            }

        }).start();

        refreshOverauthTable();
    }


    public static void refreshOverauthTable(){
        AddLock.lock();
        getOverauthLoggerTable().revalidate();
        AddLock.unlock();
    }


    public static void setRequestResponse(String requestNumber) {
        String RequestPath = OverAuthPathMap.get(requestNumber);

        Map<String, HttpRequest> requestHashMap = OverAuthRequestMap.get(RequestPath);

        OverauthAuthHighauthRequestEditor.setRequest(requestHashMap.get("HighAuth"));
        OverauthAuthLowauthRequestEditor.setRequest(requestHashMap.get("LowAuth"));
        OverauthAuthUnauthRequestEditor.setRequest(requestHashMap.get("UnAuth"));

        Map<String, HttpResponse> responseHashMap = OverAuthResponseMap.get(RequestPath);


        OverauthAuthHighauthResponseEditor.setResponse(responseHashMap.get("HighAuth"));
        OverauthAuthLowauthResponseEditor.setResponse(responseHashMap.get("LowAuth"));
        OverauthAuthUnauthResponseEditor.setResponse(responseHashMap.get("UnAuth"));

        DiyJList parameterList = getOverauthParameterList();
        parameterList.removeAllString();
        ArrayList<HttpParameter> parameters = OverAuthParameterMap.get(RequestPath);
        for(HttpParameter parameter : parameters){
            parameterList.addString(parameter.name()+"="+parameter.value());
        }

    }
    private static Integer getOverauthRequestId() {
        RequestIdLock.lock();
        RequestId = RequestId+1;
        RequestIdLock.unlock();
        return RequestId;
    }

    public static void clearOverauthTable(){
        //这里有竞争问题
        OverAuthRowMap = Collections.synchronizedMap(new HashMap<>());
        OverAuthPathMap = Collections.synchronizedMap(new HashMap<>());
        OverAuthResponseMap = Collections.synchronizedMap(new HashMap<>());
        OverAuthRequestMap = Collections.synchronizedMap(new HashMap<>());
        OverAuthParameterMap = Collections.synchronizedMap(new HashMap<>());
        getOverauthLoggerTable().setModel(new DiyJLogTable.TableModelExample(Overauth.Headers, Overauth.OverAuthRowMap, Overauth.OverAuthPathMap));
        OverAuthLowParameterList = Collections.synchronizedList(new ArrayList<>());
        RequestId = -1;
    }

    public static void exportVulData(){

        for (Map.Entry<String, Map<String,String>> entry : OverAuthRowMap.entrySet()) {
            StringBuilder result = new StringBuilder();
            if(entry.getValue().get("LowAuth").equals("True")){
                result.append("越权访问");
            }
            if(entry.getValue().get("UnAuth").equals("True")) {
                result.append(",未授权访问");
            }
            if(!result.isEmpty()){
                BurpAPI.logging().logToOutput(result.append(": ").append(entry.getKey()).toString());
            }
        }
    }

    public static DiyJLogTable getOverauthLoggerTable(){
        return (DiyJLogTable) Helper.getComponent(OverauthComponentPanel,"OverauthLoggerTable");
    }


    private static JTextField getField(String filedName){
        return (JTextField) Helper.getComponent(OverauthComponentPanel,filedName);
    }

    public static JTextField getOverauthMenuHighauthField(){
        return getField("OverauthMenuHighauthField");
    }

    public static JTextField getOverauthMenuHostField(){
        return getField("OverauthMenuHostField");
    }

    public static JTextField getOverauthMenuLowauthField(){
        return getField("OverauthMenuLowauthField");
    }

    public static DiyJList getOverauthParameterList() {
        return (DiyJList) Helper.getComponent(OverauthComponentPanel, "OverauthParameterList");
    }

}
