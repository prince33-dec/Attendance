# Implementation Plan - Tablet & Phone Adaptive Support

This plan details the steps to implement adaptive UI support in the Attendance app using Jetpack Compose. This will ensure the app looks great and is functional on both phone and tablet form factors.

## User Review Required

> [!IMPORTANT]
> This change introduces a `NavigationRail` for tablets (medium/expanded width) and keeps the `NavigationBar` for phones (compact width). The layout of the `StudentsScreen` will also change to a List-Detail pattern on larger screens.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/PROJECTS%20ANDROID/attendance/gradle/libs.versions.toml)
- Add `androidx.compose.material3:material3-window-size-class` dependency.
- Add `androidx.compose.material3.adaptive:adaptive` dependency (optional but recommended for List-Detail patterns).

#### [MODIFY] [build.gradle.kts](file:///D:/PROJECTS%20ANDROID/attendance/app/build.gradle.kts)
- Add the new dependencies to the `app` module.

---

### UI Core

#### [MODIFY] [MainActivity.kt](file:///D:/PROJECTS%20ANDROID/attendance/app/src/main/java/com/example/MainActivity.kt)
- Calculate `WindowSizeClass` and pass it to `AttendanceApp`.

#### [MODIFY] [AttendanceApp.kt](file:///D:/PROJECTS%20ANDROID/attendance/app/src/main/java/com/example/ui/AttendanceApp.kt)
- Receive `WindowSizeClass` as a parameter.
- Implement logic to choose between `NavigationBar` (bottom) and `NavigationRail` (side) based on the window width.
- Adjust the layout to accommodate the side navigation when active.

---

### Features

#### [MODIFY] [StudentsScreen.kt](file:///D:/PROJECTS%20ANDROID/attendance/app/src/main/java/com/example/ui/screens/StudentsScreen.kt)
- Implement a List-Detail layout for tablets using `WindowSizeClass` or adaptive layout components.
- On tablets, the student list will be on the left, and selecting a student will show their details on the right instead of a BottomSheet.

## Verification Plan

### Automated Tests
- Run `:app:assembleDebug` to ensure the project builds correctly with new dependencies.

### Manual Verification
- Deploy the app to a phone emulator and verify bottom navigation.
- Deploy the app to a tablet emulator (or resize window in emulator) and verify navigation rail and list-detail view.
