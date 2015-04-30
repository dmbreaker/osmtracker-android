# To do #

  * Better landscape screen support. Application can go landscape, but some buttons should be redesigned to fit better in landscape.

# Done #
  * Have a true background service capable of keep tracking when the ui is gone, and connect back to the UI when it's launched again.
  * Component are oddly organized. Service should not have reference to the UI (?)
  * Implements a notification system while tracking in background.
  * Add a shortcut to Android GPS settings, and maybe detect if the GPS is off when starting application
  * Add user-configurable buttons.
  * Ability to deal with multiple track, and re-export previous tracks. It should help when app. crashes and data has not been exported, but is still in the SQlite DB. Will need kind of "track manager" screen.
  * Add a user notification while saving GPX track. Actually this is done in background without any indication for the user.