import requests
import json

url = "http://localhost:8082/authenticate"

payload = json.dumps({
  "username": "citydata",
  "password": "citydata"
})
headers = {
  'Content-Type': 'application/json'
}

response = requests.request("POST", url, headers=headers, data=payload)
token = response.text

print(token)