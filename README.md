# SalesforceFTPConnector
## Download files from FTP using Salesforce HTTPRequest

In order to use this application take care to have:
1) Heroku free account
2) JDK 8 installed locally
3) Maven 3 installed locally
4) Heroku CLI
5) Git

After you have everything required, run commands listed below:
1) git clone https://github.com/Nara7788/SalesforceFTPConnector.git
2) cd SalesforceFTPConnector
3) heroku login
4) heroku create
   - Go to heroku dashboard and copy your new application name (it will be something similar to "infinite-mesa-47733").
   - Go to cloned repo and in pom.xml and in element <appName></appName> replace app name to yours.
5) mvn clean heroku:deploy-war
6) After successful deployment go to Salesforce `Remote Site Settings` in Setup and add new endpoint with link to your heroku app.

Thats it. You may test your app by running next script in Anonymous Apex:

```java
String url = 'https://infinite-mesa-47733.herokuapp.com/SalesforceFTPConnector'; //replace with your app URL
Http h = new Http();
HttpRequest req = new HttpRequest();
req.setEndpoint(url);
req.setMethod('POST');
req.setHeader('server', 'test.rebex.net');
req.setHeader('port', '21');
req.setHeader('user', 'demo');
req.setHeader('pass', 'password');
req.setHeader('filedir', '/pub/example/ConsoleClient.png');
req.setTimeout(120000);
HttpResponse res = h.send(req);

ContentVersion cont = new ContentVersion();
cont.Title = 'ConsoleClient';
cont.PathOnClient = 'ConsoleClient.png';
cont.VersionData = res.getBodyAsBlob();
cont.Origin = 'H';
insert cont;
```

Go to Salesforce -> Files -> All files, and check out new image you have recently downloaded from FTP! Have fun. ;)
