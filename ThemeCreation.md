# About application custom theme creation #



# Prerequisites #

  * Knowledge of Android SDK & dev tools - Sorry, theme creation is not user-friendly for the moment at is relies on the Android SDK theme framework.
  * 9-patch graphic for button.
  * 9-patch graphic + toggle on/off for the toggle button, if you're brave.

# Background #

The theme system relies on the Android theme framework, so if you've already made themes for other applications there is no particular difficulty.

However, if it's your first application theme creation as it was for me, there are some subtle things to figure out. The difficult part was to understand the general theme organization:
  * A theme files references styles for each component type (button, textview, etc.)
    * Styles can reference a `drawable`, for example for background
      * This `drawable` can be an image (PNG), but can also be an XML file ! XML files in this case are kind of "meta drawables" that allow to define different drawable depending of the state of the button, for example.
      * There are several kind of XML files, the two that I'm aware are `<selector>` and `<layer-list>`
        * `<selector>` allows to specify multiple drawables depending on View attributes value (button state for example: has focus, is disabled, is pressed, etc.)
        * `<layer-list>` allows to make a composite `drawable` by layering multiple image (or other XML files !)

As far as I know this is not really documented, I had to go to Android sources to understand that.

## The Theme file ##

The Theme file defines global Theme metadata, as some global colors and properties, but also defines a list of styles that composes the Theme. Let's take an example with the default Android Theme file:

http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;f=core/res/res/values/themes.xml

```
<resources>
 <style name="Theme"> <!-- Name of the theme -->
  <!-- Some global color and style attributes -->
  <item name="colorForeground">@android:color/bright_foreground_dark</item>
  ...

  <!-- Some style definitions. In this case, it indicates that the style of the buttons is defined by the "Widget.Button" style, which is not in this file -->
  <item name="buttonStyle">@android:style/Widget.Button</item>
  ...
```

So the theme file is just a "map" indicating which styles to apply to each component. The real styles attributes (color, drawables, etc.) is in the styles file.

## The Styles file ##

The Styles files contain the style attributes like text color, background drawables, etc. for each defined style. Let's have a look at the Android Style file :

http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;f=core/res/res/values/styles.xml

```
<resources>
 ...
 <style name="Widget.Button">
  <item name="android:background">@android:drawable/btn_default</item>
  <item name="android:textAppearance">?android:attr/textAppearanceSmallInverse</item>
  ...
 </style>
```

The style **Widget.Button** uses a `drawable` as background. But if you search for the corresponding PNG file (btn\_default.9.png) in the `drawable/` directory you'll not find it ! Instead you'll find an XML file with the following contents:

http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;f=core/res/res/drawable/btn_default.xml

```
<selector xmlns:android="http://schemas.android.com/apk/res/android">
 <item android:state_window_focused="false" android:state_enabled="true"
        android:drawable="@drawable/btn_default_normal" />
 <item android:state_window_focused="false" android:state_enabled="false"
        android:drawable="@drawable/btn_default_normal_disable" />
 ...
```

This XML file is a "meta drawable", allowing the system to pick the right drawable regarding of of the state of the button. Then, if you look for a bitmap named `btn\_default\_normal.9.png', you'll eventually find it.

## The layer-list system ##

This principles applies for the Toggle button too, but as a toggle buttons display a little bitmap to show its status (on/off), it's a little bit different. Let's have a look at the Toggle button style, as defined in the Styles file:
```
<style name="Widget.Button.Toggle">
 <item name="android:background">@android:drawable/btn_toggle_bg</item>
 <item name="android:textOn">@android:string/capital_on</item>
 ...
```

As you can see the backround uses a `btn_toggle_bg` drawable, which is also an XML file with the following contents:

http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;f=core/res/res/drawable/btn_toggle_bg.xml

```
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+android:id/background" android:drawable="@android:drawable/btn_default_small" />
    <item android:id="@+android:id/toggle" android:drawable="@android:drawable/btn_toggle" />
</layer-list>
```

This files allows to compose a drawable, by using two layers: The first is a classic button, and the second is the small green on/off toggle light.

In fact the system is recursive, because `btn_default_small` and `btn_toggle` are also XML files with `<selector>` tags !

# Steps #

Now that you know as much as me, let's try to create a custom theme.

## Create theme file ##

First, create a new theme file in `values/theme_custom.xml` for example. In this file, define the main `<style>` corresponding to your theme:
```
<style name="Custom" parent="@android:style/Theme">
 <item name="android:buttonStyle">@style/Widget.Button</item>
 <item name="android:buttonStyleToggle">@style/Widget.Button.Toggle</item>
</style>
```

The `parent` attribute is important, because they make your styles inherit of Android default ones. That's a good way of beginning, without having to redefine each single style attribute.

## Create styles definition ##

Once your Theme is created, you have to create the corresponding styles definitions, like **Widget.Button** and **Widget.Button.Toggle** that you defined in your theme file (I reused the Android names, but you could have named them as you liked).

You can create a `values/style_custom.xml` file, or insert your styles in the same file as your theme:
```
<style name="Widget.Button" parent="@android:style/Widget.Button">
 <item name="android:background">@drawable/theme_custom_btn</item>
 <item name="android:textColor">#ffffff</item>
</style>

<style name="Widget.Button.Toggle" parent="@android:style/Widget.Button.Toggle">
 <item name="android:textColor">#ffffff</item>
</style>
```

As you can see, we've set a drawable as background for the button. This drawable will be an XML file with `<selector>` tag, allowing us to define a bitmap for each button state.

## The composite button drawable file ##

Create a file `drawable/theme_custom_btn.xml` with the following contents:
```
<selector xmlns:android="http://schemas.android.com/apk/res/android">
 <item android:state_window_focused="false" android:state_enabled="true" android:drawable="@drawable/theme_custom_btn_normal" />
 <item android:state_window_focused="false" android:state_enabled="false" android:drawable="@drawable/theme_custom_btn_focused" />
 ...
```

That's in this file that you define the true bitmaps that will be used, depending of the state of the buttons.

## Create bitmaps ##

Eventually, create 9-patch bitmaps, and place them in `drawable/theme_custom_btn_normal`, `drawable/theme_custom_btn_focused`, etc.

# The end #

I think the simple thing to do is to cut/past the high-contrast theme I made, and try to reproduce the thing. ;-)

Feel free to contact me for any help !