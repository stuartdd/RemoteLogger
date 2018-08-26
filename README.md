# Mock Server with Remote Logger and UI

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
  "servers" : {
    "5002" : {
      "expectationsFile" : "expectations.json",
      "autoStart" : true,
      "showPort" : true,
      "timeToClose" : 1,
      "verbose" : true,
      "logProperties" : true
    },
    "1998" : {
      "expectationsFile" : "exp.json",
      "autoStart" : false,
      "showPort" : true,
      "timeToClose" : 1,
      "verbose" : true,
      "logProperties" : true
    }
  },
  "logDateFormat" : "",
  "timeFormat" : "HH:mm:ss.SSS",
  "defaultPort" : 5002,
  "includeHeaders" : true,
  "includeBody" : true,
  "includeEmpty" : false,
  "showTime" : true,
  "showPort" : true,
  "x" : 0.0,
  "y" : 0.0,
  "width" : 1471.0,
  "height" : 817.0,
  "expDividerPos" : [ 0.1686166551961459, 0.5898141775636614 ]
}
```
This config file is updated each time the UI exits normally with the changes selected.

If the UI was on another screen and cannot now be seen. Set x and y to 0 and reload.

The expectation file is yet to be documented.

Note the expectations file will be re-read if the last updated time of the file changes.
