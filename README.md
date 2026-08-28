# Marketing — Android App (Prototype)

A native Android app (Kotlin + Jetpack Compose) implementing the Version 1 spec:

- Login with username/email + password, logout
- Home: search, add customer, customer list, filter by status
- Customer record: name, phone, call, SMS, status, date added
- Multi-user accounts: Admin sees everyone, staff see only customers assigned to them
- Records who added each customer
- "Successful" status to flag converted customers

## How to open and build

1. Install [Android Studio](https://developer.android.com/studio) (free) if you don't have it.
2. Unzip this project.
3. In Android Studio: **File → Open** → select the unzipped `MarketingApp` folder.
4. Let Android Studio sync Gradle (first sync downloads dependencies — needs internet).
   If it asks to update the Gradle wrapper, accept — it will fetch the wrapper jar automatically.
5. Connect an Android phone (with USB debugging on) or start an emulator.
6. Click **Run ▶** to install and launch the app.

## Building an installable APK

In Android Studio: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
The APK will appear under `app/build/outputs/apk/debug/app-debug.apk` — copy it to
your phone and install it (you may need to allow "install from unknown sources").

## Demo accounts

| Username | Password    | Role  |
|----------|-------------|-------|
| admin    | admin123    | Admin |
| aisha    | pass123     | Staff |
| ravi     | pass123     | Staff |
| fayjes   | fayjes1234  | Staff |

## Notes on this prototype

- Data (customers, users) is stored **in memory only** (`AppData.kt`) — it resets
  every time the app restarts. This keeps the prototype simple to build and run.
- To make data persist and sync across multiple staff phones, the next step is
  to connect `AppData` to a real backend (e.g. Firebase Firestore, or a REST API
  backed by a database). That's a natural "Version 2" step once the flow here
  is confirmed to be right.
- Calling and texting use the phone's own dialer/messaging apps (`ACTION_DIAL`
  / `ACTION_SENDTO`), so no special permissions are required.
