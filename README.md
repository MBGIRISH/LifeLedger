# Personal Diary (LifeLedger) — Android (Java + MVVM + Firebase)

A secure Android diary app built with **Java**, **MVVM**, and **Firebase**. Users can write diary notes for **any date**, attach files (images/documents/audio/video), search by date/month/tag, and update entries later.

**Hard rule:** diary notes must **never** be deleted (enforced by Firestore Security Rules).

## Team

- **Developer**: Girish M B  
- **College**: Dayananda Sagar College of Engineering

---

## Tech stack

- **Language**: Java
- **Architecture**: MVVM (ViewModel + LiveData + Repository)
- **UI**: Material Design 3 + ViewBinding + RecyclerView
- **Backend**: Firebase
  - **Auth**: Email/Password + Google Sign‑In
  - **Database**: Cloud Firestore
  - **Files**: Firebase Storage (attachments)
- **Min SDK**: 24
- **Compile/Target SDK**: 36
- **Build**: Gradle Wrapper (8.9) + Android Gradle Plugin (8.7.x)

---

## Package name (Google Play)

The **applicationId (package name)** used for Play Store uploads is:

- **`com.passfamily`**

> Note: Source code package remains `com.personaldiary` (this is normal; `applicationId` is what matters for Play Store uniqueness).

---

## Features

- **Auth**
  - Email/Password signup + login
  - Strong password policy
  - Forgot password (reset email)
  - Google Sign‑In

- **Diary**
  - Write for **any specific date** (future dates disabled in picker)
  - Notes can be **empty** and still saved/updated
  - Tags: `NORMAL`, `SPECIAL`, `IMPORTANT`, `BAD_NEWS`
  - Created/Updated timestamps

- **Attachments (WhatsApp-style)**
  - Attach **images, documents, audio, video**
  - Upload to **Firebase Storage**
  - Display as **thumbnails / file tiles** on:
    - Home screen (current day note)
    - Search results list (Month/Tag)
    - Search by Date result

- **Search**
  - By **Date** (view + edit)
  - By **Month + Year** (read‑only list)
  - By **Tag** (read‑only list)

- **Memories**
  - **On This Day (1 year ago)** card shown on Home screen when a note exists for the same date last year

- **Security**
  - Users can read/write only their own notes
  - **Delete is blocked** at Firestore rules level

---

## App screens

- `MainActivity`: Welcome screen (Login/Signup + credits)
- `LoginActivity`: Email/Password login + Google Sign‑In + Forgot Password
- `SignupActivity`: Registration + Google Sign‑In
- `HomeActivity`: Create/update note for selected date + attachments + “On This Day”
- `SearchActivity`: Search by Date/Month/Tag
- `MonthNotesActivity`: RecyclerView list for Month/Tag results

---

## Project structure

```
com.personaldiary
├── activities/
├── adapters/
├── firebase/
├── models/
├── repository/
├── utils/
└── viewmodel/
```

---

## Firestore schema

### Users

```
users/{userId}
  name: string
  email: string
  createdAt: timestamp
```

### Diary notes (by date)

Document ID is the **date** in storage format: `yyyy-MM-dd`

```
diary_notes/{userId}/notes/{date}
  content: string
  tag: string
  date: string
  createdAt: timestamp
  updatedAt: timestamp
  attachments: [
    { url: string, name: string, type: string }
  ]
```

`type` is one of: `image | document | audio | video`

---

## Firebase setup (required)

### 1) Add Android app in Firebase

Firebase Console → Project Settings → Your Apps → Add app (Android)

- **Package name**: `com.passfamily`
- Add **SHA‑1** for:
  - Debug keystore (for local testing)
  - Release keystore (for Play Store builds)

Download the generated `google-services.json` and place it here:

- `app/google-services.json`

### 2) Enable Authentication methods

Firebase Console → Authentication → Sign‑in method:

- Enable **Email/Password**
- Enable **Google**

### 3) Enable Firestore

Firebase Console → Firestore Database → Create database  
Use **Production mode** (recommended).

### 4) Enable Storage (for attachments)

Firebase Console → Storage → Get started

### 5) Deploy Security Rules

- Firestore Rules: copy/paste `firestore.rules` into Firestore → Rules → Publish
- Storage Rules: copy/paste `storage.rules` into Storage → Rules → Publish

> Storage rules enforce per-user access under `attachments/{userId}/...`.

### 6) Indexes

If Firestore asks for indexes (Logcat provides links), create them.

Common queries:
- Month search by `date` (range + order)
- Tag search by `tag`

---

## Build & run

### Android Studio

Open the project in Android Studio and press **Run**.

### Command line

```bash
./gradlew assembleDebug
```

---

## Password policy

Passwords must include:

- Min **8** characters
- At least **1 uppercase** letter
- At least **1 number**
- At least **1 special character**

---

## Release / Play Store

### Build release APK

```bash
./gradlew assembleRelease
```

### Build release AAB (Play Store upload)

```bash
./gradlew bundleRelease
```

Upload the generated `.aab` from:

- `app/build/outputs/bundle/release/app-release.aab`

### Notes

- Package name must be unique on Play Store → this project uses **`com.passfamily`**.
- Keep `local.properties` **out of git** (it contains machine-specific paths/secrets).

---

## Non-deletable notes rule

Diary notes must never be deleted. This is enforced by:

- Firestore security rule: `allow delete: if false;`

---

## Troubleshooting

- **Google Sign‑In fails**: make sure SHA‑1 is added in Firebase and the correct `google-services.json` for `com.passfamily` is placed in `app/`.
- **Attachments not uploading**: ensure Firebase Storage is enabled and `storage.rules` are published.

---

## License

Educational project.
