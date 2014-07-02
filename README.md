#EventTriggeredSkypeCaller
Skype caller which is triggered by proximity to NFC tags or iBeacons

##Description
- This application needs a Skype installation to function. If one is not found, it will prompt the user to go to the Store and get it.
- Skype contacts can be paired with either an NFC Tag or an iBeacon.
- Each contact can be associated an action to be triggered when in proximity with its trigger device : Video Call, Call, Chat and Prompt User each time.
- On detection of the device, the application will execute the relevant action.

##Requirements
This is intended to be used in conjunction with alt236's "Bluetooth LE Library for Android" library project which can be found here:
https://github.com/alt236/Bluetooth-LE-Library---Android

##Target Platforms
Android devices running OS version 18+ and which can support receiving of Bluetooth LE signal.

##Usage
The Main Activity is currently providing a simple visualisation of results.

##Play Store Project
https://play.google.com/store/apps/details?id=com.michaelfotiadis.ibeaconscanner
