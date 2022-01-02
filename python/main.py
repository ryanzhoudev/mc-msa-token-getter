import functions
import sys

# TODO: Set the variables for your Azure app here. More information: https://wiki.vg/Microsoft_Authentication_Scheme
client_id = ""
secret = ""
redirect_url = ""

if client_id == "" or secret == "" or redirect_url == "":
    print("You must set the three Azure app variables before using this script.")
    sys.exit()

print("Open the following link in your browser.")
print(functions.get_login_url(client_id, redirect_url))
print("Paste the URL you were redirected to below.")

returned_url = input()
if "code=" not in returned_url:
    print("URL did not contain a valid code. Try again.")
    sys.exit()
auth_code = returned_url.split("code=")[1]

xbl_auth_token = functions.get_xbl_auth_token(client_id, secret, redirect_url, auth_code)

xbl_values = functions.auth_with_xbl(xbl_auth_token)
xbl_token = xbl_values["Token"]
uhs = xbl_values["DisplayClaims"]["xui"][0]["uhs"]

xsts_token = functions.auth_with_xsts(xbl_token)

print(functions.auth_with_minecraft(uhs, xsts_token))
