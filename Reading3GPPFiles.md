# Introduction #

Unfortunately due to an Android SDK limitation, OSMTracker can only record audio file in [3GP](http://en.wikipedia.org/wiki/3GP) format. Here is how to play 3GP files under different OSes.

## Windows ##

  * Install [ffmpeg-tryouts](http://ffdshow-tryout.sourceforge.net/)
  * Install [Haali Media Splitter](http://haali.su/mkv/)
    * Be sure to enable MP4 support when installing HMS.

After that you can play 3GP files with your preferred media player.

## Mac OS X ##

QuickTime have native support for 3GP files.

## Linux ##

As suggested by misc2006, the following script can be used to convert 3GP into WAV and update the GPX file with new file names (assuming you've ffmpeg installed):

```
#!/bin/bash
for file in *.3gpp
do
  ffmpeg -y -i $file -f wav "$file.wav"
  touch "$file.wav" -r $file
done

rename 's/\.3gpp//' *.wav

sed 's/\.3gpp<\/link>/.wav<\/link>/' *.gpx > ready-to-map.gpx
```