# Openflights Angular UI

This adds an angular UI with a JEE7 Backend to openflights, which is my favourte flight tracking service.
It also makes use of the Lufthansa Open API (https://developer.lufthansa.com) to automagically complete your flight info based upon the fight number.

You will need to provide a Lufthansa API-Key as system property to your server to make this work properly.
   
    org.openflights.lufthansa.api.key = your_api_key
    org.openflights.lufthansa.api.secret = your_api_secret
   
   
I am currently playing around with the APIs a bit and hope to have flight entry working soon.