1. Make sure you have [NodeJS](https://nodejs.org) installed. If you already have [NodeJS](https://nodejs.org) installed make sure you `npm install -g plugman`
2. Go to **Android Studio** > `Preferences` > `Plugins` and click on _install JetBrains Plugin_ button.
3. Search for `PhoneGap` and install it. Make sure you don't install the **PhoneGap/Cordova Plugin**
4. Restart **Android Studio** 
5. Go to `Tools` > `PhoneGap` > `Initialize Project`
6. Copy everything from www-shared/www to this newly created assets/www
7. Go to `Tools` > `PhoneGap` > `Install Plugin from npm`
8. Type in `cordova-plugin-device`
9. Go to `Tools` > `PhoneGap` > `Install Plugin from npm`
10. Type in `cordova-plugin-console`
11. Go to `Tools` > `PhoneGap` > `Install Plugin from filesystem`
12. Select `cordova-plugin-pgdayeu16` which can be found at [cordova-plugin-pgdayeu16](https://github.com/imhotep/PGDayEUWs2016/tree/master/cordova-plugin-pgdayeu16)
