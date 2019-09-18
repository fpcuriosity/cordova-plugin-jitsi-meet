package com.cordova.plugin.jitsi;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.util.Map;
import java.net.MalformedURLException;
import java.net.URL;
import android.os.Bundle;

import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate;
import android.view.View;

import org.apache.cordova.CordovaWebView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class JitsiPlugin extends CordovaPlugin implements JitsiMeetActivityInterface{
  private JitsiMeetView view;
  private static final String TAG = "cordova-plugin-jitsi";

  final static String[] permissions = { Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
  public static final int TAKE_PIC_SEC = 0;
  public static final int REC_MIC_SEC = 1;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    // CB-10120: The CAMERA permission does not need to be requested unless it is declared
    // in AndroidManifest.xml. This plugin does not declare it, but others may and so we must
    // check the package info to determine if the permission is present.

    checkPermission();

    if (action.equals("loadURL")) {
      String url = args.getString(0);
      String key = args.getString(1);
      this.loadURL(url, key, callbackContext);
      return true;
    } else if (action.equals("destroy")) {
      this.destroy(callbackContext);
      return true;
    }
    return false;
  }


  private void checkPermission(){
    boolean takePicturePermission = PermissionHelper.hasPermission(this, Manifest.permission.CAMERA);
    boolean micPermission = PermissionHelper.hasPermission(this, Manifest.permission.RECORD_AUDIO);

    // CB-10120: The CAMERA permission does not need to be requested unless it is declared
    // in AndroidManifest.xml. This plugin does not declare it, but others may and so we must
    // check the package info to determine if the permission is present.

    Log.e(TAG, "tp : " + takePicturePermission);
    Log.e(TAG, "mp : " + micPermission);

    if (!takePicturePermission) {
      takePicturePermission = true;

      try {
        PackageManager packageManager = this.cordova.getActivity().getPackageManager();
        String[] permissionsInPackage = packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;

        if (permissionsInPackage != null) {
          for (String permission : permissionsInPackage) {
            if (permission.equals(Manifest.permission.CAMERA)) {
              takePicturePermission = false;
              break;
            }
          }
        }
        Log.e(TAG, "10 : ");
      } catch (NameNotFoundException e) {
        // We are requesting the info for our package, so this should
        // never be caught
        Log.e(TAG, e.getMessage());
      }
    }

    if(!takePicturePermission){
      PermissionHelper.requestPermissions(this, TAKE_PIC_SEC,
        new String[]{ Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO});
    }
  }

  private void loadURL(final String url, final String key, final CallbackContext callbackContext) {
    Log.e(TAG, "loadURL called : "+url);

    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "INIT"+url+"/"+key);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    
    cordova.getActivity().runOnUiThread(new Runnable() {
      public void run() {
        view = new JitsiMeetView(this);
      //  Initialize default options for Jitsi Meet conferences.
        URL serverURL;
        try {
            serverURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }
        
        JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
          .setServerURL(serverURL)
          .setRoom(url)
          .setAudioMuted(false)
          .setVideoMuted(true)
          .setAudioOnly(false)
          .setWelcomePageEnabled(false)
          .build();
                
//         view.join(options);
//         setJitsiListener(view, callbackContext);
//         view.setWelcomePageEnabled(false);
//         Bundle config = new Bundle();
//         config.putBoolean("startWithAudioMuted", false);
//         config.putBoolean("startWithVideoMuted", false);
//         Bundle urlObject = new Bundle();
//         urlObject.putBundle("config", config);
//         urlObject.putString("url", url);
//         view.loadURLObject(urlObject);
        //cordova.getActivity().setContentView(view);
        JitsiMeetActivity.launch(cordova.getActivity(), options);
      }
    });
  }

//   private void setJitsiListener(JitsiMeetView view, final CallbackContext callbackContext) {

//     view.setListener(new JitsiMeetViewListener() {
//       PluginResult pluginResult;

//       private void on(String name, Map<String, Object> data) {
//         Log.d("ReactNative", JitsiMeetViewListener.class.getSimpleName() + " " + name + " " + data);
//       }

//       @Override
//       public void onConferenceFailed(Map<String, Object> data) {
//         on("CONFERENCE_FAILED", data);
//         pluginResult = new PluginResult(PluginResult.Status.OK, new JSONObject(data));
//         pluginResult.setKeepCallback(true);
//         callbackContext.sendPluginResult(pluginResult);
//       }

//       @Override
//       public void onConferenceJoined(Map<String, Object> data) {
//         on("CONFERENCE_JOINED", data);
//         pluginResult = new PluginResult(PluginResult.Status.OK, "CONFERENCE_JOINED");
//         pluginResult.setKeepCallback(true);
//         callbackContext.sendPluginResult(pluginResult);
//       }

//       @Override
//       public void onConferenceLeft(Map<String, Object> data) {
//         on("CONFERENCE_LEFT", data);
//         pluginResult = new PluginResult(PluginResult.Status.OK, "CONFERENCE_LEFT");
//         pluginResult.setKeepCallback(true);
//         callbackContext.sendPluginResult(pluginResult);
//       }

//       @Override
//       public void onConferenceWillJoin(Map<String, Object> data) {
//         on("CONFERENCE_WILL_JOIN", data);
//         pluginResult = new PluginResult(PluginResult.Status.OK, "CONFERENCE_WILL_JOIN");
//         pluginResult.setKeepCallback(true);
//         callbackContext.sendPluginResult(pluginResult);
//       }

//       @Override
//       public void onConferenceWillLeave(Map<String, Object> data) {
//         on("CONFERENCE_WILL_LEAVE", data);
//         pluginResult = new PluginResult(PluginResult.Status.OK, "CONFERENCE_WILL_LEAVE");
//         pluginResult.setKeepCallback(true);
//         callbackContext.sendPluginResult(pluginResult);
//       }

//       @Override
//       public void onLoadConfigError(Map<String, Object> data) {
//         on("LOAD_CONFIG_ERROR", data);
//         pluginResult = new PluginResult(PluginResult.Status.OK, "LOAD_CONFIG_ERROR");
//         pluginResult.setKeepCallback(true);
//         callbackContext.sendPluginResult(pluginResult);
//       }
//     });
//   }

  private void destroy(final CallbackContext callbackContext) {
    cordova.getActivity().runOnUiThread(new Runnable() {
      public void run() {
        //view.dispose();
        //view = null;
        //JitsiMeetView.onHostDestroy(cordova.getActivity());
        JitsiMeetActivityDelegate.onHostDestroy(cordova.getActivity());
        cordova.getActivity().setContentView(getView());
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "DESTROYED");
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
      }
    });
  }

  private View getView() {
    try {
      return (View) webView.getClass().getMethod("getView").invoke(webView);
    } catch (Exception e) {
      return (View) webView;
    }
  }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {
        JitsiMeetActivityDelegate.onActivityResult(
                this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        JitsiMeetActivityDelegate.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = new JitsiMeetView(this);
        JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
            .setRoom("https://meet.telemed.tn/1234")
            .build();
        view.join(options);

        setContentView(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        view.dispose();
        view = null;

        JitsiMeetActivityDelegate.onHostDestroy(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        JitsiMeetActivityDelegate.onNewIntent(intent);
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode,
            final String[] permissions,
            final int[] grantResults) {
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();

        JitsiMeetActivityDelegate.onHostResume(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        JitsiMeetActivityDelegate.onHostPause(this);
    }

}
