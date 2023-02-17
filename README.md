# Tubes1_Artificial-Ilham

Tugas Besar I IF2211 Strategi Algoritma Semester II Tahun 2022/2023 Implementasi Algoritma Greedy dalam bot Permainan "Galaxio"

## Authors

| Nama                  | NIM      |
| --------------------- | -------- |
| Angger Ilham Amanullah  | 13521001 |
| Ditra Rizqa Amadia| 13521019 |
| Muhammad Haidar Akita Tresnadi          | 13521025 |

## Deskripsi

 Galaxio adalah sebuah game battle royale di mana pemain berkompetisi dengan bot kapal mereka melawan bot kapal lain. Tujuan permainan adalah menjaga agar bot kapal milik pemain tetap hidup hingga akhir permainan. Permainan ini merupakan game yang dibuat untuk acara Entelect Challenge tahun 2021. Untuk penjelasan lebih lanjut dapat dilihat pada link berikut : [Entelect Challenge](https://github.com/EntelectChallenge/2021-Galaxio)

Pembuatan bot penulis mengguanakan pendekatan algoritma greedy dalam melakukan pengambilan keputusan. Algoritma greedy yang digunakan berfokus pada keputusan saat menyerang dan bertahan hidup. Bot ini diimplementasikan menggunakan bahasa java.

## Requirements

- JDK
- Apache Maven
- Net Core versi 3.1 dan 5.0

## Program structure
```bash
.
│   Dockerfile
│   pom.xml
│   README.md
│
├───doc
│       temp.txt
│
├───src
│   └───main
│       └───java
│           │   Main.java
│           │
│           ├───Enums
│           │       ObjectTypes.java
│           │       PlayerActions.java
│           │
│           ├───Models
│           │       GameObject.java
│           │       GameState.java
│           │       GameStateDto.java
│           │       PlayerAction.java
│           │       Position.java
│           │       World.java
│           │
│           └───Services
│                   BotService.java
│
└───target
    │   IlhamBot.jar
    │
    ├───classes
    │   │   Main.class
    │   │
    │   ├───Enums
    │   │       ObjectTypes.class
    │   │       PlayerActions.class
    │   │
    │   ├───Models
    │   │       GameObject.class
    │   │       GameState.class
    │   │       GameStateDto.class
    │   │       PlayerAction.class
    │   │       Position.class
    │   │       World.class
    │   │
    │   └───Services
    │           BotService.class
    │
    ├───libs
    │       azure-core-1.13.0.jar
    │       gson-2.8.5.jar
    │       jackson-annotations-2.11.3.jar
    │       jackson-core-2.11.3.jar
    │       jackson-databind-2.11.3.jar
    │       jackson-dataformat-xml-2.11.3.jar
    │       jackson-datatype-jsr310-2.11.3.jar
    │       jackson-module-jaxb-annotations-2.11.3.jar
    │       jakarta.activation-api-1.2.1.jar
    │       jakarta.xml.bind-api-2.3.2.jar
    │       netty-tcnative-boringssl-static-2.0.35.Final.jar
    │       okhttp-3.11.0.jar
    │       okio-1.14.0.jar
    │       reactive-streams-1.0.2.jar
    │       reactor-core-3.3.12.RELEASE.jar
    │       rxjava-2.2.2.jar
    │       signalr-1.0.0.jar
    │       slf4j-api-1.7.25.jar
    │       slf4j-simple-1.7.25.jar
    │       stax2-api-4.2.1.jar
    │       woodstox-core-6.2.1.jar
    │
    ├───maven-archiver
    │       pom.properties
    │
    └───maven-status
        └───maven-compiler-plugin
            └───compile
                └───default-compile
                        createdFiles.lst
                        inputFiles.lst
```

## How to run program
- Instal dan download semua requirement yang diperlukan 
- Download starter pack pada link berikut : [Starter Pack](https://github.com/EntelectChallenge/2021-Galaxio/releases/tag/2021.3.2)
- Buat file run.bat (untuk pengguna windows) dengan konfigurasi seperti di bawah, atau langsung jalankan run.sh untuk di linux. Pastikan mengganti path bot dengan path file bot yang akan digunakan.

```bash
"""
@echo off
:: Game Runner
cd ./runner-publish/
start "" dotnet GameRunner.dll

:: Game Engine
cd ../engine-publish/
timeout /t 1
start "" dotnet Engine.dll

:: Game Logger
cd ../logger-publish/
timeout /t 1
start "" dotnet Logger.dll

:: Bots
cd ../reference-bot-publish/
timeout /t 3
start "" dotnet ReferenceBot.dll
timeout /t 3
start "" dotnet ReferenceBot.dll
timeout /t 3
start "" dotnet ReferenceBot.dll
timeout /t 3
start "" java -jar (Masukkan Path dari bot jar di sini...)
cd ../

pause
"""
```

- Clone atau download zip repository ini
- Jalankan run.bat
- Tunggu hingga permainan berakhir
- Buka visualizer untuk melihat log dari pertandingan yang telah selesai









