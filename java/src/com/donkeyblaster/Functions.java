package com.donkeyblaster;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Functions {

    private static String getJsonStringValue(String json, String field) {
        Matcher m = Pattern.compile(".+\"" + field + "\":\"([^\"]+)\".+").matcher(json);
        if (m.matches()) {
            return m.group(1);
        }
        return "";
    }

    public static String getLoginUrl(String clientId, String redirectUrl) {
        return "https://login.live.com/oauth20_authorize.srf?client_id=" + clientId +
                "&response_type=code&redirect_uri=" + redirectUrl +
                "&scope=XboxLive.signin%20offline_access";
    }

    public static String getXblAuthToken(String clientId, String clientSecret, String redirectUrl, String authCode) throws IOException {
        URL url = new URL("https://login.live.com/oauth20_token.srf");
        URLConnection connection = url.openConnection();
        HttpsURLConnection https = (HttpsURLConnection) connection;
        https.setRequestMethod("POST");
        https.setDoOutput(true);

        Map<String, String> arguments = new HashMap<>();
        arguments.put("client_id", clientId);
        arguments.put("client_secret", clientSecret);
        arguments.put("redirect_uri", redirectUrl);
        arguments.put("code", authCode);
        arguments.put("grant_type", "authorization_code");

        StringJoiner sj = new StringJoiner("&");
        for(Map.Entry<String, String> entry : arguments.entrySet())
            sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));

        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);

        https.setFixedLengthStreamingMode(out.length);
        https.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        https.setRequestProperty("user-agent", "mc-msa-token-getter");
        https.connect();
        OutputStream outputStream = https.getOutputStream();
        outputStream.write(out);

        BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = reader.readLine()) != null) {
            sb.append(output);
        }

        String results = sb.toString();
        return getJsonStringValue(results, "access_token");
    }

    public static HashMap<String, String> authWithXbl(String accessToken) throws IOException {
        URL url = new URL("https://user.auth.xboxlive.com/user/authenticate");
        URLConnection connection = url.openConnection();
        HttpsURLConnection https = (HttpsURLConnection) connection;
        https.setRequestMethod("POST");
        https.setDoOutput(true);

        byte[] out = ("{\"Properties\": {\"AuthMethod\": \"RPS\", \"SiteName\": \"user.auth.xboxlive.com\", \"RpsTicket\": \"d=" + accessToken + "\"}, \"RelyingParty\": \"http://auth.xboxlive.com\", \"TokenType\": \"JWT\"}").getBytes(StandardCharsets.UTF_8);

        https.setFixedLengthStreamingMode(out.length);
        https.setRequestProperty("Content-Type", "application/json");
        https.setRequestProperty("user-agent", "mc-msa-token-getter");
        https.setRequestProperty("Accept", "application/json");
        https.connect();
        OutputStream outputStream = https.getOutputStream();
        outputStream.write(out);

        BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = reader.readLine()) != null) {
            sb.append(output);
        }

        String results = sb.toString();
        HashMap<String, String> returnable = new HashMap<>();
        returnable.put("xblToken", getJsonStringValue(results, "Token"));
        returnable.put("uhs", getJsonStringValue(results, "uhs"));
        return returnable;
    }

    public static String authWithXsts(String xblToken) throws IOException {
        URL url = new URL("https://xsts.auth.xboxlive.com/xsts/authorize");
        URLConnection connection = url.openConnection();
        HttpsURLConnection https = (HttpsURLConnection) connection;
        https.setRequestMethod("POST");
        https.setDoOutput(true);

        byte[] out = ("{\"Properties\": {\"SandboxId\": \"RETAIL\", \"UserTokens\": [\"" + xblToken + "\"]}, \"RelyingParty\": \"rp://api.minecraftservices.com/\", \"TokenType\": \"JWT\"}").getBytes(StandardCharsets.UTF_8);

        https.setFixedLengthStreamingMode(out.length);
        https.setRequestProperty("Content-Type", "application/json");
        https.setRequestProperty("user-agent", "mc-msa-token-getter");
        https.setRequestProperty("Accept", "application/json");
        https.connect();
        OutputStream outputStream = https.getOutputStream();
        outputStream.write(out);

        BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = reader.readLine()) != null) {
            sb.append(output);
        }

        String results = sb.toString();
        return getJsonStringValue(results, "Token");
    }

    public static String authWithMinecraft(String userHash, String xstsToken) throws IOException {
        URL url = new URL("https://api.minecraftservices.com/authentication/login_with_xbox");
        URLConnection connection = url.openConnection();
        HttpsURLConnection https = (HttpsURLConnection) connection;
        https.setRequestMethod("POST");
        https.setDoOutput(true);

        byte[] out = ("{\"identityToken\": \"XBL3.0 x=" + userHash + ";" + xstsToken + "\"}").getBytes(StandardCharsets.UTF_8);

        https.setFixedLengthStreamingMode(out.length);
        https.setRequestProperty("Content-Type", "application/json");
        https.setRequestProperty("user-agent", "mc-msa-token-getter");
        https.setRequestProperty("Accept", "application/json");
        https.connect();
        OutputStream outputStream = https.getOutputStream();
        outputStream.write(out);

        BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = reader.readLine()) != null) {
            sb.append(output);
        }

        String results = sb.toString();
        Matcher accessTokenMatcher = Pattern.compile(".+\"access_token\" : \"([^\"]+)\".+").matcher(results);
        if (accessTokenMatcher.matches()) {
            return accessTokenMatcher.group(1);
        }
        return "Failed to get access token.";
    }
}
