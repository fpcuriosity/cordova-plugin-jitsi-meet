# cordova-plugin-jitsi-meet
Cordova plugin for jitsi meet native sdk

---

## update note

permision helper added ( for android ) 

## how to use

let room_url = 'https://meet.jit.si/room_name';

```javascript
plugins.JitsiPlugin.loadURL(room_url, null, function(data) {
  if(data === "CONFERENCE_WILL_LEAVE"){
    plugins.JitsiPlugin.destroy( function(data) {
      // on Destroy
      console.log("Destroy");
    }, function (err) {
      // on Error
      console.log("Error");
    });
  }
}, function(err) {
  // on load url was not work
  console.log("Error : ", err);
});
```

---

## how to build for ionic

```bash
ionic cordova plugin add https://github.com/sesangsokuro/cordova-plugin-jitsi-meet

ionic cordova plugin add cordova-plugin-compat

( for cordova permision helper )

ionic cordova build andorid
```
