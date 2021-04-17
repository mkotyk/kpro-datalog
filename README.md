# kpro-datalog
Kotlin/Java Library to collect realtime data from a Hondata KPro ECU

This library is a reverse engineering of the Hondata KPro 2/3 USB datalog wire protocol,
as well as KManager 1.2.x (yes old), datalog format.

**NOTICE**: Use this library at your own risk!  The author, nor Hondata, Inc. shall he held
liable for any damages caused by use, or mis-use of this software.  **DO NOT** contact Hondata, Inc
for support with this software.

### What can it do?
Currently, the library can read and write `.kdl` datalog files, and supports KPro v2 USB messages 
`0x40` (status), `0x60`, `0x61`, and `0x62` (datalog).  The library is able to convert between
the two formats.  The purpose of most fields are known, as are the units.

### How was this done?
The data format was reverse engineered by comparing USB packets to datalog files.

### Why was this done?
In more recent times, KPro 4 has included bluetooth protocol with a full specification, but for
legacy KPro models, there is no ideal way to obtain a real time stream of sensor data, at least without
sacrificing other features.

This project aims to be the base for an Android head unit digital instrumentation app.

### How do I use this?
There is a utility program to capture and replay datalog files.  The main value of this library 
is to provide information on the structures and conversions for data logging.  It's up to the user
to decide how to use it.  At the moment, this is not a full-blown SDK, packaged and ready to consume
in a project, but mainly just a proof of concept.

### Others:
 - https://github.com/pablobuenaposada/HonDash - Python based instrumentation to run on a Raspberry Pi
