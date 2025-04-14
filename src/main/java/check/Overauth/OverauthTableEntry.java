package check.Overauth;

import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.util.ArrayList;

public class OverauthTableEntry {
    public  String RequestNumber;
    public  String RequestTool;
    public  String RequestMethod;
    public  String RequestHost;
    public  String RequestPath;
    public  String RequestTime;

    public  String ResponseTime;


    public  String Length;

    public  HttpRequest HighAuthRequest = null;

    public  HttpResponse HighAuthResponse = null;

    public  HttpRequest LowAuthRequest = null;

    public  HttpResponse LowAuthResponse = null;


    public  HttpRequest UnAuthRequest = null;

    public  HttpResponse UnAuthResponse = null;

    public  String LowAuth;

    public  String UnAuth;

    public  ArrayList<ParsedHttpParameter> HorizontalOverAuthParameters;

    public  boolean Removed = false;

    public  String Code;


}
