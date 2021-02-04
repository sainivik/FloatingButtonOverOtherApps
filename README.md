# FloatingButtonOverOtherApps

### Screenshot
<img src="https://github.com/sainivik/FloatingButtonOverOtherApps/blob/master/app/screenshots/overlay_demo.gif" width="300px" height="632px"/>

#### Usage

use the following code to give perission

```java
 public fun requestOverlayPermission(context: Activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.packageName)
                )
                context.startActivityForResult(intent, 1234)

            }else{
                Toast.makeText(this, "Allready have permission.", Toast.LENGTH_SHORT).show()
            }
        }
    }
```

add following permission in menifests
```menifest

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.REORDER_TASKS" />

    ```

