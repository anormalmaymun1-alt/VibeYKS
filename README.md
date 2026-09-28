# 🎯 YKS AI Sınav Koçu (VibeYKS)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?style=flat&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Gemini AI](https://img.shields.io/badge/Google%20Gemini-AI%20Powered-8E75C2.svg?style=flat&logo=google&logoColor=white)](https://ai.google.dev/)
[![Room Database](https://img.shields.io/badge/Room-Local%20Persistence-3DDC84.svg?style=flat&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-brightgreen.svg?style=flat&logo=android&logoColor=white)](https://www.android.com)

**YKS AI Sınav Koçu**, YKS (TYT ve AYT) sınavlarına hazırlanan öğrenciler için geliştirilmiş akıllı bir deneme takip, sonuç analizi ve kişisel yapay zeka koçluk uygulamasıdır. Google Gemini modelleri ile güçlendirilmiş koçluk motoru ve optik/belge tarama özellikleri sayesinde hazırlık sürecinizi veriye dayalı ve stratejik bir şekilde yönetmenizi sağlar.

---

## ✨ Öne Çıkan Özellikler

### 🤖 1. Gemini Destekli AI Sınav Koçu
- **Kişiselleştirilmiş Koçluk:** Deneme geçmişinizi, net trendlerinizi ve zayıf olduğunuz konuları gerçek zamanlı inceleyerek stratejik tavsiyeler üretir.
- **Dinamik Bağlam Yönetimi (Context Limiter & Builder):** Öğrencinin son sınav performanslarını ve kritik konu eksikliklerini otomatik olarak istem (prompt) bağlamına ekler.
- **Çoklu Model Desteği:** İhtiyacınıza göre Gemini 2.5 Flash, Gemini 1.5 Flash veya Gemini 1.5 Pro modelleri arasında geçiş yapabilme.
- **Akıllı Token ve Oturum Yönetimi:** Sohbet geçmişini yerel Room veritabanında saklar, token tüketimini optimize eder.
- **Zengin Metin (Rich Text / Markdown) Desteği:** Kod blokları, formüller, listeler ve vurgulu ifadeler şık formatlanmış olarak görüntülenir.

### 📸 2. Akıllı Deneme Tarama ve OCR
- **Görsel & PDF Desteği:** Kamera ile çekilen veya galeriden seçilen deneme sonuç belgelerini doğrudan tarayabilme.
- **Otomatik Sonuç Ayrıştırma:** Gemini Vision API yetenekleri ile ders adlarını, doğru, yanlış ve boş sayılarını otomatik algılama.
- **İnceleme & Doğrulama:** Taranan verileri kaydetmeden önce tek dokunuşla düzenleme ve teyit etme ekranı.
- **Manuel Hızlı Giriş:** İsteğe bağlı olarak sınav sonuçlarını elle hızlıca girme seçeneği.

### 📊 3. TYT & AYT Analitikleri ve Performans Grafikleri
- **Ders & Konu Bazlı İstatistikler:** Türkçe, Matematik, Fen, Sosyal vb. dersler için net gelişim grafikleri.
- **Zayıf Konu Tespiti:** Başarı oranının %50'nin altına düştüğü konuları tespit edip öne çıkaran akıllı algoritma.
- **Sınav Türü Filtreleme:** TYT ve AYT denemelerini ayrı ayrı veya birlikte filtreleyip inceleme.

### 📋 4. Deneme Yönetimi
- Kaydedilen tüm denemelerin listesi, net özetleri, tarih bazlı sıralama ve silme/düzenleme imkanı.

### 🎨 5. Modern ve Akıcı Tasarım (Vibe UI)
- Jetpack Compose & Material 3 standartlarında, modern koyu/açık tema uyumu.
- Cam efekti (Glassmorphism), pürüzsüz gradyanlar, animasyonlu geçişler ve modern tipografi.

### 🔒 6. Güvenlik ve Gizlilik
- **Şifreli API Anahtarı Saklama:** Google Gemini API anahtarınız AndroidX `EncryptedSharedPreferences` kullanılarak cihazınızda güvenli bir şekilde saklanır.
- **Çevrimdışı ve Yerel Veri:** Deneme sınavı ve sohbet kayıtlarınız cihazınızdaki SQLite/Room veritabanında güvende kalır; üçüncü parti sunuculara aktarılmaz.

---

## 🛠️ Teknoloji Yığını ve Mimari

- **Mimari:** MVVM (Model-View-ViewModel) + Repository Pattern + Clean Architecture
- **Dil:** [Kotlin](https://kotlinlang.org/) (2.0+)
- **Kullanıcı Arayüzü:** [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material Design 3](https://m3.material.io/)
- **Yapay Zeka:** [Google Generative AI Client SDK](https://github.com/google/generative-ai-android) (Gemini API)
- **Veritabanı:** [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- **Asenkron Programlama:** Kotlin Coroutines & Kotlin Flow (`StateFlow`, `SharedFlow`)
- **Navigasyon:** AndroidX Navigation Compose
- **Güvenlik:** AndroidX Security Crypto (`EncryptedSharedPreferences`)
- **Görsel/Doküman İşleme:** Android `PdfRenderer`, Coil

---

## 📂 Proje Dizin Yapısı

```
yks-ai-koc/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/yksaisinavkocu/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/          # Room DB, Entity'ler, DAO'lar, TypeConverters
│   │   │   │   │   ├── preferences/    # Güvenli SharedPreferences & Ayarlar
│   │   │   │   │   └── repository/     # Exam & Chat Repository katmanı
│   │   │   │   ├── service/
│   │   │   │   │   ├── gemini/         # Gemini Model Manager, Prompt Şablonları, ContextBuilder
│   │   │   │   │   ├── parser/         # Eylem & JSON ayrıştırıcılar
│   │   │   │   │   └── scanner/        # Görsel işleme & PDF Bitmap render
│   │   │   │   ├── theme/              # Renk paleti, Tema ve Tipografi
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/     # Ortak bileşenler (GlassCard, MetricCards vb.)
│   │   │   │   │   └── screen/         # Koç, Analitik, Tarama, Denemeler, Ayarlar
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── Navigation.kt
│   │   │   │   └── YksApp.kt
│   │   │   └── res/                    # İkonlar, mipmap, renk ve kaynak dosyaları
│   │   └── test/                       # Kapsamlı birim (Unit) testleri
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Kurulum ve Başlangıç

### Gereksinimler
- **Android Studio:** Ladybug / Koala veya daha yeni sürüm
- **JDK:** Java 17 veya üzeri
- **Android SDK:** Min SDK 26 (Android 8.0), Target SDK 34 (Android 14)

### Adımlar

1. **Depoyu Klonlayın:**
   ```bash
   git clone https://github.com/anormalmaymun1-alt/VibeYKS.git
   cd VibeYKS
   ```

2. **Projeyi Android Studio ile Açın:**
   - Android Studio'da `Open Project` seçeneğiyle proje kök dizinini seçin.
   - Gradle senkronizasyonunun tamamlanmasını bekleyin.

3. **Gemini API Anahtarı Alın:**
   - [Google AI Studio](https://aistudio.google.com/) üzerinden ücretsiz bir Gemini API anahtarı edinin.

4. **Uygulamayı Çalıştırın ve API Anahtarını Tanımlayın:**
   - Bir emülatör veya fiziksel Android cihaz bağlayarak `Run 'app'` düğmesine basın.
   - Uygulama açıldığında **Ayarlar** sekmesine gidin.
   - API anahtarınızı yapıştırıp **Kaydet** butonuna tıklayın.

---

## 🧪 Testleri Çalıştırma

Projeye ait tüm birim testlerini terminalden çalıştırmak için:

```bash
./gradlew test
```

Windows ortamında:
```powershell
.\gradlew.bat test
```

---

## 📄 Lisans

Bu proje kişisel gelişim ve eğitim amaçlı hazırlanmıştır. Tüm hakları saklıdır.
