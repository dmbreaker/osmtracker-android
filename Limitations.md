# Limitations #

  * Photo taken with the application are limited to 512x384 size on Android 1.x devices.
    * It's due to a bug in the Android built-in camera application on which OSMTracker relies (http://code.google.com/p/android/issues/detail?id=1480). Seems to be fixed in Android 2.x.
  * Supports only 3GPP format for voice recording.
    * This is a limitation of Android SDK which is unable to record in mp3 (See http://developer.android.com/reference/android/media/MediaRecorder.OutputFormat.html).
  * "Hardware" GPS logging interval cannot be set.
    * Due to the way it's implemented in Android, there is no suitable way for OSMTracker to get GPS updates at a specific interval. We're forced to use the shortest possible which seems to be every second on most devices.
    * Technical reasons: Using the `minTime` parameter of `requestLocationUpdates()` causes the GPS to actually sleep for this time, instead of notifying of a location change every `minTime`. See [issue #94](https://code.google.com/p/osmtracker-android/issues/detail?id=#94) for the details.
    * The current GPS logging interval setting in OSMTracker is "simulated" because it still receive frequent locations updates, but only records them at the specified interval.

# Previous limitations now fixed #
  * Doesn't support backgrounding: If you return to home screen, click on back, or receive a phone call, it will exit. Your track will still be saved, though.
    * Side effect:  Settings screen doesn't work well when tracking ;-) Try to use it only when you're not currently tracking.
    * **Fixed in v0.2.x** : If you exit the UI while recording a track, it continues recording. Launch the UI again to get control back and stop tracking.
  * On long tracks, the GPX export can be long and block the UI. GPX exports take about 1s per 1000 gps points on a HTC Magic.
    * **Fixed in v0.2.x** : As the service is now really backgrounding, the export isn't done anymore in the UI Thread. However it can have side effects if you start a new track while the background service is still exporting the previous one...
  * Doesn't support screen orientation: Works only in portrait mode.
    * **Fixed in v0.3.5**, but some buttons needs redesign to fit better in landscape mode.
  * The display track screen is not (yet) updated in real time.
    * **Fixed in v0.4.0**