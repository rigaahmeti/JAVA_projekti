# 🎓 Sistemi për Aplikim dhe Menaxhim të Bursave Studentore

> Projekt akademik për lëndën **Komunikimi Njeri-Kompjuter (KNK)**  
> Universiteti — Viti akademik 2024/2025

---

## 📋 Përshkrimi

Sistemi për Aplikim dhe Menaxhim të Bursave Studentore është një aplikacion desktop i zhvilluar me **JavaFX**, i cili mundëson:

- Aplikimin e studentëve për bursa akademike përmes një portali të dedikuar
- Menaxhimin administrativ të aplikimeve nga komisioni
- Vlerësimin automatik të aplikimeve me sistem pikëzimi **BI/AI**
- Mbështetje të plotë në dy gjuhë: **Shqip** dhe **Anglisht**

---

## 👥 Anëtarët e Grupit

| # | Emri   | Detyra |
|---|--------|--------|
| 1 | Arsa   | Dritarja kryesore, Menu, Toolbar, Status Bar, Shortcut Menu |
| 2 | Vesa   | Forma e aplikimit për bursë |
| 3 | Rinesa | Database, Repository dhe TableView |
| 4 | Riga   | UI/UX, Aksesueshmëria dhe Shortcuts |
| 5 | Paola  | Dashboard, Statistika dhe BI/AI |
| 6 | Andi   | Lokalizimi, Help dhe Dokumentimi |

---

## 🛠️ Teknologjitë e Përdorura

| Teknologjia | Versioni | Qëllimi |
|-------------|----------|---------|
| Java | 17+ | Gjuha kryesore e programimit |
| JavaFX | 21 | Ndërfaqja grafike (GUI) |
| SQLite | 3.x | Databaza lokale |
| Maven | 3.x | Menaxhimi i varësive |
| IntelliJ IDEA | 2023+ | Mjedisi i zhvillimit (IDE) |

---

## 🔐 Kredencialet Demo

| Roli | Username | Password |
|------|----------|----------|
| Student | `student` | `student123` |
| Admin | `admin` | `admin123` |

> Fjalëkalimet ruhen si hash **SHA-256** në databazën SQLite.

---

## 📁 Struktura e Projektit

```
src/
└── main/
    ├── java/
    │   ├── app/
    │   │   ├── App.java                  # Entrypoint kryesor
    │   │   ├── Router.java               # Navigimi
    │   │   ├── ViewEnum.java             # Enum i pamjeve
    │   │   └── SessionManager.java       # Menaxhimi i sesionit
    │   └── com/hci/scholarship/
    │       ├── app/
    │       │   ├── Launcher.java
    │       │   └── MainApp.java          # Aplikacioni kryesor JavaFX
    │       ├── db/
    │       │   └── Database.java         # Lidhja me SQLite
    │       ├── model/
    │       │   ├── ScholarshipApplication.java
    │       │   └── UserAccount.java
    │       ├── repository/
    │       │   ├── ApplicationRepository.java
    │       │   └── UserRepository.java
    │       └── service/
    │           ├── LocalizationService.java  # Shërbimi i lokalizimit
    │           └── ScoringService.java       # Pikëzimi BI/AI
    └── resources/
        ├── i18n/
        │   ├── messages_sq.properties    # Tekstet në Shqip
        │   └── messages_en.properties    # Tekstet në Anglisht
        └── styles.css                    # Stilizimi JavaFX
```

---

## ✨ Funksionalitetet Kryesore

### 👨‍🎓 Portali i Studentit
- Hyrje e sigurt me autentikim
- Forma e plotë e aplikimit me validim të fushave
- Checklist i dokumenteve të detyrueshme
- Shfaqja e kritereve të bursës

### 🖥️ Paneli Administrativ
- **TableView** me të gjitha aplikimet
- Aprovim, refuzim dhe fshirje e aplikimeve
- Kërkim dhe filtrim i aplikimeve
- Menu kontekstuale me klikim të djathtë

### 📊 Dashboard BI/AI
- Statistika në kohë reale (totali, aprovuar, refuzuar, në pritje)
- **PieChart** dhe **BarChart** për vizualizim
- Sistem pikëzimi automatik:
    - Mesatarja akademike: **45%**
    - Nevoja financiare: **45%**
    - Viti i studimeve: **10%**
- Radha e komisionit sipas prioritetit

### 🌐 Shumë-gjuhësia
- Ndërrimi i gjuhës **Shqip ↔ Anglisht** në kohë reale
- Të gjitha tekstet e ndërfaqes të lokalizuara

---

## ⌨️ Shkurtesat e Tastierës

| Shkurtesa | Veprimi |
|-----------|---------|
| `Ctrl + N` | Aplikim i ri |
| `Ctrl + T` | Tabela e aplikimeve |
| `Ctrl + D` | Dashboard |
| `Ctrl + B` | Lidhja me databazën |
| `F1` | Ndihma (Help) |
| `Ctrl + Q` | Dalje |

---

## 📏 Rregullat e Bursës

- ✅ Mesatarja minimale për aplikim është **8.0**
- ✅ Studenti duhet të jetë aktiv në vitin akademik aktual
- ✅ Nuk lejohet bursë e dyfishtë institucionale për të njëjtën periudhë
- ✅ Të gjitha dokumentet duhet të jenë të kompletuara
- ✅ Numri i indeksit është unik — nuk mund të përdoret për dy aplikime
- ✅ Vendimin final e merr komisioni (Aprovo / Refuzo)

---

## 🗄️ Databaza

Databaza **SQLite** krijohet automatikisht si file `scholarships.db` kur aplikacioni starton për herë të parë. Tre aplikime demo shtohen automatikisht për prezantim.

Tabelat kryesore: `users`, `applications`

---

## 🔧 Konfigurimi i Run Configuration

Projekti vjen me dy konfigurime të gatshme:

- **Scholarship App** — Nis projektin nga `app.App` (rekomandohet)
- **Scholarship Fast Run** — Nis `app.App` direkt nga IntelliJ, më i shpejtë pas importimit

---

## 📌 Shënim për GitHub

Repository-ja është **private**. Vetëm anëtarët e grupit dhe mësimdhenësi kanë qasje.

Për të shtuar bashkëpunëtorë:  
`Settings → Collaborators → Add people`
