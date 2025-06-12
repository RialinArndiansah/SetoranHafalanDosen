# 📱 Aplikasi Mobile Setoran Hafalan

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" width="150"/>
</p>

<p align="center">
  <a href="#"><img src="https://img.shields.io/badge/Kotlin-100%25-purple.svg?style=flat" alt="Kotlin 100%"/></a>
  <a href="#"><img src="https://img.shields.io/badge/Platform-Android-green.svg?style=flat" alt="Platform Android"/></a>
  <a href="#"><img src="https://img.shields.io/badge/License-MIT-blue.svg?style=flat" alt="License MIT"/></a>
  <a href="#"><img src="https://img.shields.io/badge/Version-1.0.0-red.svg?style=flat" alt="Version 1.0.0"/></a>
  <a href="#"><img src="https://img.shields.io/badge/Build-Passing-success.svg?style=flat" alt="Build Passing"/></a>
</p>

## 🌟 About | Tentang

[English]  
A cutting-edge Android application revolutionizing how lecturers manage and track student Quran memorization submissions. Built with modern Android development practices, it offers a seamless, intuitive interface powered by Jetpack Compose and Material Design 3. The app emphasizes real-time tracking, detailed analytics, and secure authentication to enhance the memorization monitoring process.

[Indonesia]  
Aplikasi Android mutakhir yang merevolusi cara dosen mengelola dan melacak setoran hafalan Al-Quran mahasiswa. Dibangun dengan praktik pengembangan Android modern, menawarkan antarmuka yang mulus dan intuitif menggunakan Jetpack Compose dan Material Design 3. Aplikasi ini menekankan pada pelacakan real-time, analisis terperinci, dan autentikasi yang aman untuk meningkatkan proses pemantauan hafalan.

## ✨ Features | Fitur

### 🔐 Authentication & Security
- Multi-factor authentication (Email/Fingerprint/Face ID)
- Role-based access control (Admin/Dosen/Mahasiswa)
- End-to-end data encryption
- Secure session management
- Automated logout on inactivity

### 📊 Dashboard & Analytics
- Real-time submission tracking
- Interactive progress charts
- Performance metrics visualization
- Custom reporting tools
- Export data functionality

### 👥 Student Management
- Comprehensive student profiles
- Batch grouping system
- Individual progress tracking
- Achievement monitoring
- Historical data analysis

### 📝 Submission System
- Voice recording integration
- Real-time submission validation
- Quality assessment metrics
- Progress calculation
- Feedback system

## 🚀 Technology Stack | Teknologi

### Frontend Development
```kotlin
// Core Technologies
implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.9.0'
implementation 'androidx.compose.ui:ui:1.5.0'
implementation 'com.google.android.material:material:1.9.0'

// Architecture Components
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
implementation 'androidx.navigation:navigation-compose:2.7.0'
implementation 'androidx.room:room-runtime:2.6.0'

// Networking & Data
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.moshi:moshi-kotlin:1.14.0'

// Security
implementation 'androidx.security:security-crypto:1.1.0-alpha06'
implementation 'androidx.biometric:biometric:1.2.0-alpha05'
```

## 📱 Screenshots | Tangkapan Layar

<p align="center">
  <img src="screenshots/login.jpg" width="200" alt="Login Screen - Secure authentication with biometrics"/>
  <img src="screenshots/dashboard.jpg" width="200" alt="Dashboard - Comprehensive statistics and metrics"/>
  <img src="screenshots/submission.jpg" width="200" alt="Submission Management - Easy recording interface"/>
  <img src="screenshots/profile.jpg" width="200" alt="Profile & Settings - Detailed user management"/>
</p>

## ⚙️ Installation | Instalasi 

### System Requirements
- Android Studio Hedgehog | 2023.1.1 or newer
- JDK 17
- Android SDK 34
- Minimum supported Android version: 7.0 (API 24)
- 4GB RAM minimum, 8GB RAM recommended
- 2.5GB disk space + 1GB for Android SDK and emulator

### Setup Instructions
1. Clone repository:
```bash
git clone https://github.com/username/setoran-hafalan.git
cd setoran-hafalan
```

2. Install dependencies:
```bash
./gradlew build
```

3. Run the application:
```bash
./gradlew installDebug
```

## 📄 License | Lisensi

```
MIT License

Copyright (c) 2024 kelompok1

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```


<p align="center">
  Made with ❤️ by Kelompok 1<br>
  Copyright © 2024 Kelompok 1. All rights reserved.
</p>
