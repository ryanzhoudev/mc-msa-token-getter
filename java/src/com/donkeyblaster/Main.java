package com.donkeyblaster;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // TODO: Set the variables for your Azure app here. More information: https://wiki.vg/Microsoft_Authentication_Scheme
        String clientId = "";
        String secret = "";
        String redirectUrl = "";

        if (clientId.equals("") || secret.equals("") || redirectUrl.equals("")) {
            System.out.println("You must set the three Azure app variables before using this script.");
            return;
        }

        System.out.println("Open the following link in your browser.");
        System.out.println(Functions.getLoginUrl(clientId, redirectUrl));
        System.out.println("Paste the URL you were redirected to below.");

        Scanner scanner = new Scanner(System.in);
        String returnedUrl = scanner.next();
        if (!returnedUrl.contains("code=")) {
            System.out.println("URL did not contain a valid code. Try again.");
            return;
        }
        String[] urlParts = returnedUrl.split("code=");
        String authCode = urlParts[urlParts.length - 1];

        try {
            String xblAuthToken = Functions.getXblAuthToken(clientId, secret, redirectUrl, authCode);
            if (xblAuthToken.equals("")) {
                System.out.println("Failed to get XBL auth token.");
                return;
            }

            HashMap<String, String> xblValues = Functions.authWithXbl(xblAuthToken);
            if (xblValues.get("xblToken").equals("") || xblValues.get("uhs").equals("")) {
                System.out.println("Failed to authenticate with XBL.");
                return;
            }
            String xblToken = xblValues.get("xblToken");
            String uhs = xblValues.get("uhs");

            String xstsToken = Functions.authWithXsts(xblToken);
            if (xstsToken.equals("")) {
                System.out.println("Failed to get XSTS auth token.");
                return;
            }

            System.out.println(Functions.authWithMinecraft(uhs, xstsToken));

        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
