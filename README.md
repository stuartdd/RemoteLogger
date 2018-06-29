# Remote Logger

A GUI that Echos/Logs http requests sent to it.

It also Logs all interactions.

Logs are on screen

It can use a set of rules (expectations) to define responses if required

* This module is dependent on json_tools module.

This is a JavaFX project built using gradle.

The generated JAR file contains ALL dependencies and can be run using 
```
java -jar RemoteLogger.jar configfile.json
```

Example config file (annotated):
```
{
  "port" : 1999,                  Listens on this port
  "logDateFormat" : "",           The date format for logging
  "autoConnect" : true,           Connect on load. This will be remembered for each session
  "timeFormat" : "HH:mm:ss.SSS",  Format for On Screen log time
  "expectationsFile" : "expectations.json", Rules for generating response files
  "verbose" : true,               Lots more logging
  "includeHeaders" : true,        Flag to include headers on screen
  "includeBody" : true,           Flag to include the bodu text on screen
  "includeEmpty" : false,         Flag to include empty lines on screen
  "showTime" : true,              Flag to show timeFormat on screen
  "x" : 338.0,                    The UI position on screen
  "y" : 93.0,     
  "width" : 1548.0,
  "height" : 845.0
}
```
This config file is updated each time the UI exits normally.

The expectation file is yet to be documented.

Note the expectations file will be re-read if the last updated time of the file changes.
