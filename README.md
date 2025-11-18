# MusicKt

An Android application built with Kotlin.

## Tech Stack

- **Kotlin**: 2.2.0 (KGP 2.2.x)
- **Gradle**: 8.14
- **Android Gradle Plugin**: 8.7.3
- **Min SDK**: 24
- **Target SDK**: 35
- **Compile SDK**: 35

## Features

- Modern Android architecture
- ViewBinding enabled
- Material Design 3 components
- AndroidX libraries

## Dependencies

- AndroidX Core KTX
- AppCompat
- Material Components
- ConstraintLayout

## Build

To build the project:

```bash
./gradlew build
```

To run on a device or emulator:

```bash
./gradlew installDebug
```

## Project Structure

```
music-kt/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/musickt/
│   │       │   └── MainActivity.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   ├── values/
│   │       │   ├── mipmap-*/
│   │       │   └── xml/
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## License

This project is for educational purposes.
