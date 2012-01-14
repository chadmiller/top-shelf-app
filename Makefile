
note=$(shell bzr tags |sort -n |tail -1 |cut -d\  -f1)
destinations=res/drawable/icon.png res/drawable-xhdpi/icon.png res/drawable-hdpi/icon.png res/drawable-ldpi/icon.png

launcher-icons: $(destinations)

clean:
	rm $(destinations)

res/drawable-xhdpi/icon.png: images/olive-on-rim.svg
	convert -background none $< -scale 96x96 -pointsize 8 -draw "gravity southeast fill white text 0,1 '$(note)'" $@

res/drawable-hdpi/icon.png: images/olive-on-rim.svg
	convert -background none $< -scale 72x72 -pointsize 8 -draw "gravity southeast fill white text 0,1 '$(note)'" $@

res/drawable/icon.png: images/olive-on-rim.svg
	convert -background none $< -scale 48x48 -pointsize 8 -draw "gravity southeast fill white text 0,1 '$(note)'" $@

res/drawable-ldpi/icon.png: images/olive-on-rim.svg
	convert -background none $< -scale 36x36 -pointsize 8 -draw "gravity southeast fill white text 0,1 '$(note)'" $@
