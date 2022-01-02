import requests


def get_login_url(client_id: str, redirect_url: str):
    return f"https://login.live.com/oauth20_authorize.srf?client_id={client_id}&response_type=code&redirect_uri={redirect_url}&scope=XboxLive.signin%20offline_access"


def get_xbl_auth_token(client_id: str, client_secret: str, redirect_url: str, auth_code: str):
    parameters = {
        "client_id": client_id,
        "client_secret": client_secret,
        "redirect_uri": redirect_url,
        "code": auth_code,
        "grant_type": "authorization_code",
    }
    header = {
        "Content-Type": "application/x-www-form-urlencoded",
        "user-agent": "mc-msa-token-getter"
    }
    r = requests.post("https://login.live.com/oauth20_token.srf", data=parameters, headers=header)
    return r.json()["access_token"]


def auth_with_xbl(access_token: str):
    parameters = {
        "Properties": {
            "AuthMethod": "RPS",
            "SiteName": "user.auth.xboxlive.com",
            "RpsTicket": f"d={access_token}"
        },
        "RelyingParty": "http://auth.xboxlive.com",
        "TokenType": "JWT"
    }
    header = {
        "Content-Type": "application/json",
        "user-agent": "mc-msa-token-getter",
        "Accept": "application/json"
    }
    r = requests.post("https://user.auth.xboxlive.com/user/authenticate", json=parameters, headers=header)
    return r.json()


def auth_with_xsts(xbl_token: str):
    parameters = {
        "Properties": {
            "SandboxId": "RETAIL",
            "UserTokens": [xbl_token]
        },
        "RelyingParty": "rp://api.minecraftservices.com/",
        "TokenType": "JWT"
    }
    header = {
        "Content-Type": "application/json",
        "user-agent": "mc-msa-token-getter",
        "Accept": "application/json"
    }
    r = requests.post("https://xsts.auth.xboxlive.com/xsts/authorize", json=parameters, headers=header)
    return r.json()["Token"]


def auth_with_minecraft(userhash: str, xsts_token: str):
    parameters = {
        "identityToken": f"XBL3.0 x={userhash};{xsts_token}"
    }
    header = {
        "Content-Type": "application/json",
        "user-agent": "mc-msa-token-getter",
        "Accept": "application/json"
    }
    r = requests.post("https://api.minecraftservices.com/authentication/login_with_xbox", json=parameters, headers=header)
    return r.json()['access_token']
