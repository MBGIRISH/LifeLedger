# Personal Diary - Android Application

A secure, production-ready Android diary application built with Java, MVVM architecture, and Firebase backend. Users can record daily life events, search entries by date/month/tag, and modify them later. **Diary notes can never be deleted.**

## Team

- **Developer:** Girish M B
- **College:** Your College Name

---

## Tech Stack

| Component       | Technology                  |
|-----------------|-----------------------------|
| Language        | Java                        |
| Architecture    | MVVM                        |
| Backend         | Firebase                    |
| Database        | Cloud Firestore             |
| Authentication  | Firebase Authentication     |
| UI              | Material Design 3           |
| Build System    | Gradle 8.4 / AGP 8.2.2     |
| Min SDK         | 24 (Android 7.0)            |
| Target SDK      | 35                          |

---

## Features

- **Splash Screen** with AndroidX SplashScreen API
- **User Authentication** — Email/Password login & signup with strong password policy
- **Forgot Password** — Firebase password reset email
- **Daily Diary** — Write/edit today's diary entry
- **Auto-load** — Existing entries load automatically for editing
- **Note Tagging** — NORMAL, SPECIAL, IMPORTANT, BAD_NEWS
- **Search by Date** — Pick a date, view & edit that day's entry
- **Search by Month** — View all entries for a month (read-only list)
- **Search by Tag** — View all entries with a specific tag (read-only list)
- **Timestamps** — Server-side created/updated timestamps displayed on entries
- **Material Design 3** — Modern UI with light/dark theme support
- **Security** — Firestore rules enforce per-user data isolation; delete is forbidden

---

## Project Structure

```
com.personaldiary
├── activities/          # All Activity classes
│   ├── MainActivity     # Welcome/splash screen
│   ├── LoginActivity    # Email + password login
│   ├── SignupActivity   # User registration
│   ├── HomeActivity     # Main diary screen
│   ├── SearchActivity   # Search by date/month/tag
│   └── MonthNotesActivity # RecyclerView list of notes
├── adapters/
│   └── DiaryNoteAdapter # RecyclerView adapter with ViewBinding
├── models/
│   ├── User             # User data model
│   └── DiaryNote        # Diary note data model
├── repository/
│   ├── AuthRepository   # Firebase Auth operations
│   └── DiaryRepository  # Firestore diary CRUD operations
├── viewmodel/
│   ├── AuthViewModel    # Auth state management
│   └── DiaryViewModel   # Diary data management
├── firebase/
│   └── FirebaseConfig   # Singleton Firebase instances
└── utils/
    ├── Constants        # App-wide constants
    ├── ValidationUtils  # Email & password validation
    └── DateUtils        # Date formatting utilities
```

---

## Firestore Data Structure

### Users Collection

```
users/{userId}
├── name: String
├── email: String
└── createdAt: Timestamp
```

### Diary Notes Collection

```
diary_notes/{userId}/notes/{date}
├── content: String
├── tag: String ("NORMAL" | "SPECIAL" | "IMPORTANT" | "BAD_NEWS")
├── date: String ("yyyy-MM-dd")
├── createdAt: Timestamp
└── updatedAt: Timestamp
```

---

## Setup Instructions

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Java 17
- Firebase account

### Step 1: Clone & Open

```bash
git clone <repository-url>
```

Open the project folder in Android Studio.

### Step 2: Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or use existing project `java-app-aede7`)
3. Add an Android app with package name: **`com.personaldiary`**
4. Download the generated `google-services.json`
5. Replace `app/google-services.json` with your downloaded file

### Step 3: Enable Firebase Services

In the Firebase Console:

1. **Authentication** → Sign-in method → Enable **Email/Password**
2. **Firestore Database** → Create database → Start in **production mode**

### Step 4: Deploy Firestore Security Rules

Copy the contents of `firestore.rules` and paste them in:
Firebase Console → Firestore Database → Rules tab → Publish

### Step 5: Create Firestore Indexes

The app uses these queries that may require composite indexes. Firestore will prompt you to create them automatically (check Logcat for index creation links):

1. **Month search:** `notes` subcollection, fields: `date ASC`
2. **Tag search:** `notes` subcollection, field: `tag`

If prompted in Logcat, click the provided URL to create the index automatically.

### Step 6: Build & Run

```bash
./gradlew assembleDebug
```

Or press **Run** in Android Studio.

---

## Password Policy

Passwords must meet ALL of the following:

- Minimum 8 characters
- At least one uppercase letter
- At least one number
- At least one special character (`!@#$%^&*()` etc.)

---

## Firestore Security Rules Summary

| Collection | Operation | Rule |
|---|---|---|
| `users/{uid}` | Read/Write | Only if `auth.uid == uid` |
| `diary_notes/{uid}/notes/{noteId}` | Read | Only if `auth.uid == uid` |
| `diary_notes/{uid}/notes/{noteId}` | Create/Update | Only if `auth.uid == uid` |
| `diary_notes/{uid}/notes/{noteId}` | **Delete** | **ALWAYS DENIED** |

---

## Release Build

### Generate Signed APK

1. In Android Studio: Build → Generate Signed Bundle / APK
2. Create a new keystore or use an existing one
3. Select **release** build type
4. The signed APK will be in `app/release/`

### ProGuard

ProGuard rules are configured in `app/proguard-rules.pro`. The release build automatically enables:
- Code minification (`minifyEnabled true`)
- Resource shrinking (`shrinkResources true`)

---

## Screens

| Screen | Description |
|---|---|
| `MainActivity` | Welcome screen with Login/Signup buttons, team info |
| `LoginActivity` | Email + password login, forgot password |
| `SignupActivity` | Registration with full validation |
| `HomeActivity` | Today's diary entry, tag selection, save |
| `SearchActivity` | Search by date (editable), month, or tag |
| `MonthNotesActivity` | Read-only RecyclerView of diary entries |

---

## Important Rules

1. **Diary notes can NEVER be deleted** — enforced both in app logic and Firestore security rules
2. Users can only access their own data
3. All timestamps use Firestore server timestamps
4. Today's note auto-loads for editing if it already exists

---

## License

This project is for educational purposes.
