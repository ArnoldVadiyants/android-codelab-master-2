# android-codelab
Android project that serves as a base for code challenges implemented by applicants.
The base is written in kotlin. 

(!)If you have the NDK plugin installed, please disable it for the project, as errors may occur.

## What was implemented

- Location picker screen with OpenStreetMap (OSMDroid)
- Location UI in CreateMemo: empty state, selected state with inline map preview, Change/Clear actions
- Landscape layout for CreateMemo: two-column view with form on the left and map on the right
- Location-based notifications via Android Geofencing API (200 m radius)
- Background delivery via `BroadcastReceiver` + `WorkManager`
- Geofence restore on device boot and app update
- Geofence removal when a memo is marked as done
- Abstraction layer for map and geofencing libraries (swappable via interfaces)
- MVVM applied to CreateMemo: `CreateMemoViewModel` with `StateFlow`-based UI state
- Feature-based package structure (`core`, `create`, `detail`, `home`)
- Manual dependency injection via `AppDependencies`, `AppViewModelFactory`, `AppWorkerFactory`
- Unit tests: ViewModels, NotificationManager, LocationReminderManager, Worker, Memo model
- UI tests: CreateMemo, ViewMemo
- Migrated dependencies to `libs.versions.toml` (Version Catalog)
- Target SDK updated to 37, edge-to-edge support
- `DiffUtil` in `MemoAdapter`
- Screen-rotation state retention for CreateMemo, ViewMemo, and map picker

# Android Coding Challenges
Coding challenges are useful when the applicant does not provide a github repository or any work samples. Even if a github repository has been provided it is generally a good idea to give the applicant a task to solve and have him present his solution in a separate session. 

## General Instructions
The following instructions/conditions are valid independently of the actual coding challenge

- The code base has been tested with Android Studio Narwhal Feature Drop which is the recommended version, however feel free to try a higher version and adjust the configuration as needed
- The task should be implemented in kotlin
- Approach this task as if it was a real-world implementation - i.e. exactly how you would approach the task if you were working for a company
- 3rd party libraries may be used
- 3rd party libraries must be wrapped: They should be abstracted out, so any other library could be plugged into the solution
- The base project for this task will be provided by us
- Once completed, please send us your solution and presents it to us, followed by a discussion about the implementation and design decisions made
- The solution can be sent as a zip file or as a publicly accessible github/gitlub etc project link
- The solution sent to us must be complete, i.e. can be opened directly via Android Studio without additional configuration

## Location Based Notifications
In this challenge the applicant has to implement location-based notifications/reminders, the following conditions are given:

- When creating a new memo, the user provides a location by selecting a point on a map (for instance: google maps or open street maps)
- The memo is then saved
- Once the user physically reaches that location, a notification should be displayed in the phone's status bar, that contains the title and the first 140 characters of the note text
- "Reaching the location" is defined as follows: The user is within 200 meters of the location he initially selected during the memo creation
- The notification should also contain an icon (the icon choice is up to you)
- Some form of location tracking will be required to achieve the desired result, i.e. to know when a user is close to the given location of a memo
- The feature must also work, when the app is running in the background (or possibly not running at all)
