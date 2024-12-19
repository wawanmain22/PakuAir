# PakuAir - Aplikasi Android untuk Monitoring Kualitas Air

## Deskripsi Project

PakuAir adalah aplikasi Android untuk monitoring kualitas air yang menggunakan Firebase sebagai backend. Aplikasi ini memungkinkan pengguna untuk:

- Melakukan pengecekan kualitas air
- Mengelola data depot air
- Melihat riwayat pengecekan kualitas air

## Teknologi yang Digunakan

- Kotlin
- Firebase Authentication
- Firebase Realtime Database

## Struktur Firebase

### Authentication

- Method: Email/Password
- Region: asia-southeast2 (Singapore)
- Email verification: Optional

### Realtime Database Structure

```
pakuair-db/
├── Users/
│   └── [userId]/
│       ├── username (string)
│       └── email (string)
│
├── Depots/
│   └── [depotId]/
│       ├── name (string)
│       ├── address (string)
│       ├── owner (string: userId)
│       └── description (string)
│
└── WaterQualities/
    └── [qualityId]/
        ├── userId (string)
        ├── timestamp (number)
        └── results/
            ├── ph (number: 0-14)
            ├── hardness (number: >= 0)
            ├── solids (number: >= 0)
            ├── chloramines (number: >= 0)
            ├── sulfate (number: >= 0)
            ├── organicCarbon (number: >= 0)
            ├── trihalomethanes (number: >= 0)
            └── turbidity (number: >= 0)
```

### Database Rules

```javascript
{
  "rules": {
    "Users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid",
        ".validate": "newData.hasChildren(['username', 'email'])",
        "username": { ".validate": "newData.isString()" },
        "email": { ".validate": "newData.isString()" }
      }
    },

    "Depots": {
      ".read": "auth != null",
      "$depotId": {
        ".write": "auth != null && (!data.exists() || data.child('owner').val() === auth.uid)",
        ".validate": "newData.hasChildren(['name', 'address', 'owner', 'description'])",
        "name": { ".validate": "newData.isString() && newData.val().length > 0" },
        "address": { ".validate": "newData.isString() && newData.val().length > 0" },
        "owner": { ".validate": "newData.isString()" },
        "description": { ".validate": "newData.isString()" }
      }
    },

    "WaterQualities": {
      ".read": "auth != null",
      "$qualityId": {
        ".write": "auth != null && (!data.exists() || data.child('userId').val() === auth.uid)",
        ".validate": "newData.hasChildren(['userId', 'timestamp', 'results'])",
        "userId": { ".validate": "newData.isString()" },
        "timestamp": { ".validate": "newData.isNumber()" },
        "results": {
          ".validate": "newData.hasChildren(['ph', 'hardness', 'solids', 'chloramines', 'sulfate', 'organicCarbon', 'trihalomethanes', 'turbidity'])",
          "ph": { ".validate": "newData.isNumber() && newData.val() >= 0 && newData.val() <= 14" },
          "hardness": { ".validate": "newData.isNumber() && newData.val() >= 0" },
          "solids": { ".validate": "newData.isNumber() && newData.val() >= 0" },
          "chloramines": { ".validate": "newData.isNumber() && newData.val() >= 0" },
          "sulfate": { ".validate": "newData.isNumber() && newData.val() >= 0" },
          "organicCarbon": { ".validate": "newData.isNumber() && newData.val() >= 0" },
          "trihalomethanes": { ".validate": "newData.isNumber() && newData.val() >= 0" },
          "turbidity": { ".validate": "newData.isNumber() && newData.val() >= 0" }
        }
      }
    }
  }
}
```

### Database Indexing

```javascript
{
  "WaterQualities": {
    ".indexOn": ["userId", "timestamp"]
  },
  "Depots": {
    ".indexOn": ["owner"]
  }
}
```

## Fitur Utama

1. **Autentikasi**

   - Login dengan email/password
   - Register akun baru
   - Reset password

2. **Manajemen Depot**

   - Tambah depot baru
   - Edit informasi depot
   - Lihat daftar depot

3. **Pengecekan Kualitas Air**
   - Input parameter kualitas air:
     - pH (0-14)
     - Hardness
     - Solids
     - Chloramines
     - Sulfate
     - Organic Carbon
     - Trihalomethanes
     - Turbidity
   - Simpan hasil pengecekan
   - Lihat riwayat pengecekan

## Setup Project Baru

1. Buat project di Firebase Console
2. Aktifkan Authentication (Email/Password)
3. Buat Realtime Database di region asia-southeast2
4. Set Database Rules dan Indexing
5. Download dan tempatkan `google-services.json` di folder `app/`

## Catatan Penting

- Project menggunakan Firebase Realtime Database tanpa Storage
- Tidak ada penyimpanan foto/gambar
- Semua data disimpan dalam format text dan angka
- Validasi data dilakukan di level database rules
- Database persistence diaktifkan untuk offline capability

## Monitoring

- Set budget alerts di Firebase Console
- Monitor Authentication usage
- Monitor Database usage
