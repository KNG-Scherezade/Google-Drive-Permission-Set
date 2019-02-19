# Google-Drive-Permission-Set
Automates Setting permissions on google drive
## How to use
1) Download Java if you don't have it.
2) Get files from build/libs
3) Run file and follow instructions


### Tutorial

In google drive say you have a bunch of folders and within them identically named files.<br/>
<img src="http://puu.sh/CAYYo/52a487e195.png"/>
<br/>
<img src="http://puu.sh/CAYZV/8bc7e3ad0e.png"/>
<br/>
This application will go through each of them based off of a name and a specified top level folder and give them a permission setting of  Writer(an editor) or a reader. You can specify users by their email account, group or make everyone possesing the link have that permission. <br/>

## FAQ

How to find the google drive ID for your top level folder?<br/>
<ul><li><img src='http://puu.sh/CAZ4q/ed23250a23.png'> <br/> Within the URL of any google drive folder there is a set of numbers and letters that look random. This is the ID you must enter. Google drive and this application can not view folders as unique items so it must use IDs.</li></ul>

What if I mess up?<br/>
<ul><li>Restart the program</li></ul>

How do I change accounts?<br/>
<ul><li>In the directory containing the JAR file you should see a credential file. Delete that, rerun and you should see a new window to change accounts</li></ul>

This is really slow...<br/>
<ul><li>The Google Drive API is heavily used so the program makes many HTTP requests on a single thread. Also the API will limit you even if you use their services within the specified restrictions. Because of this each HTTP request needs a cooldown period of a few seconds. Future fixes may involve understanding how the Java framework works to better implement the queries, but the cooldown period is nescicary for batch requests</li></ul>
