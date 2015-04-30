# Introduction #

Since v0.4.0, OSMTracker can use custom buttons layouts in addition to the default one. This allows you to have the best button layout that suits your activities, and remove unused buttons.

To choose a layout, use the corresponding setting in the Settings screen.

# Layout files #

Each layout is defined in a XML file that should be in a `/layouts` directory inside the main OSMTracker storage dir. Each layout must be defined in XML, in UTF-8, and the filename should end with **.xml**.

**Note:** You have to manually create the `layouts` directory on your SD card (it isn't created by OSMTracker) then drop your XML files in it.

Ex (Storage dir. defined in settings is /osmtracker):
  * /sdcard/osmtracker/layouts/cycling.xml
  * /sdcard/osmtracker/layouts/trek.xml
  * ...

Each file matching these criteria will be listed as a potential layout in the settings screen. If the layout is invalid (incorrect XML syntax) the application will display a black screen instead of buttons. You can then go back to settings, and choose the default layout to have the buttons displayed again.

![http://osmtracker-android.googlecode.com/svn/trunk/wiki/images/custom-layout/layout-list.png](http://osmtracker-android.googlecode.com/svn/trunk/wiki/images/custom-layout/layout-list.png)

# Defining a layout: XML syntax #

Each XML file must use the following syntax:

```
<layouts>

 <layout name="root">
  <row>
   <button type="..." label="..." (icon="...") (targetlayout="...") />
   ...
  </row>
 </layout>

 <layout name="...">
  ...
 </layout>

</layouts>
```

<font color='red'><b>Important</b></font>:
  * Each layout file must start with the `<layouts>` root tag, and define one or more `<layout>`. At least one layout with the name **root** must be defined.
  * Your file must not include XML header `<?xml version="1.0" encoding="utf-8"?>`. If you do so, please ensure that your file **doesn't** contains a [BOM](http://en.wikipedia.org/wiki/Byte_order_mark). The Android XML parser that cannot parse XML files with a header **and** a BOM (Or I didn't find how to do it !)

## 

&lt;layout&gt;

 ##

A `<layout>` corresponds to a page of buttons. The first layout displayed is the layout with `name="root"`, so `<layout name="root">` is mandatory.

Each layout can be subdivided in `<row>` of buttons:

```
<layout name="amenity">
 <row>
  <button ... />  
  <button ... />
 </row>
 <row>
  <button ... />
  ...
 </row>
</layout>
```

## 

&lt;button&gt;

 ##

This tag represents a button, and uses the following syntax: `<button type="..." label="..." (icon="...") (iconpos="...") (targetlayout="...") />`

  * **type** is mandatory, and can be:
    * _voicerec_ : For a voice recording button.
    * _picture_ : For a picture button (Launches camera application).
    * _textnote_ : For the text note button.
    * _tag_ : For a classic tag button.
    * _page_ : To allow navigation between button pages.
      * A _page_ button must define a _targetlayout_ attribute, pointing to the layout to display. The value of _targetlayout_ must match the name of another layout.

  * **label** is mandatory for _tag_ and _page_ buttons. It will be used as a label for the button, and as the waypoint tag name for tag buttons. For special buttons like _voicerec_, _picture_ or _textnote_ the label is automatically retrieved from the application string resources and cannot be changed.

  * **icon** is optional, and can be used to specify an icon for the button.
    * The icon must be a relative filename with extension (Ex: "airport.png").
    * Icons should be in the same directory as the layout file, and in PNG format.

  * **iconpos** _(Since v0.5.2)_ is optional, and can be used to specify where the icon should be drawn on the button. Possible values are _auto,top,right,bottom_ or _left_.
    * This attribute helps to prevent text to be cut off with some layout/orientation combination. Please see [issue #42](https://code.google.com/p/osmtracker-android/issues/detail?id=#42) for details.

  * **targetlayout** is mandatory for a _page_ button. Its value must match an existing layout name.

Some examples:

```
<button type="voicerec" label="Record a voice note" icon="mic.png" />
<button type="tag" label="Airport" icon="airport.png" />
<button type="tag" label="Power line" />
<button type="page" label="Amenity..." targetlayout="amenity" />
```

# Full commented example #

```
<layouts>

 <!-- Main layout, the first to be displayed -->
 <layout name="root">
  <row>
   <!-- 3 special action buttons -->
   <button type="voicerec" label="Voice record" icon="voice.png" />
   <button type="picture" label="Take picture" icon="camera.png" />
   <button type="textnote" label="Text note" icon="text.png" />
  </row>
  <row>
   <!-- "link" to 2 sub pages -->
   <button type="page" label="Amenity" targetlayout="amenity" />
   <button type="page" label="Restriction" icon="restriction.png" targetlayout="restriction" />
  </row>
  <row>
   <!-- Standard tag buttons -->
   <button type="tag" label="Airport" icon="airport.png" />
   <button type="tag" label="Marina" />
  </row>
 </layout>

 <!-- Amenity layout, will be displayed when clicking on "Amenity" previous defined button -->
 <layout name="amenity">
  <row>
   <!-- Two standard tag buttons -->
   <button type="tag" label="Bench" icon="button_amenity_bench.png" />
   <button type="tag" label="Water" icon="button_amenity_water.png" />
   <!-- A 2nd level page button -->
   <button type="page" label="More amenities..." targetlayout="amenity_more" />
   <!-- And a special action button -->
   <button type="textnote" label="Text note" icon="text.png" />
  </row>
 </layout>

 <!-- Restriction layout, will be displayed when clicking on "Restriction" previous defined button -->
 <layout name="restriction">
  <row>...</row>
 </layout>

 <!-- 2nd level Amenity page. Will be displayed when clicking on the "More amenities..." button on the Amenity page -->
 <layout name="amenity_more">
  <row>...</row>
 </layout>

</layouts>
```

# See also #

Please see also the [default layout](https://github.com/nguillaumin/osmtracker-android/blob/master/res/xml/default_buttons_layout.xml) provided with application sources.

(Please note that the "icon" attributes have no extension for this default layout because it's embedded in the application. User layouts **must** specify complete icon file names with extension).