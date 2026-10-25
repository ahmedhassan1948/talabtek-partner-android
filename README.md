# طلبتك — تطبيق المطاعم

<p align="center">
  <img src="store/icon-512.png" width="140" alt="Talabtek Partner" />
</p>

<p align="center">
  <b>Talabtek Partner</b> — استقبل طلباتك وأدر مطعمك من جيبك<br/>
  أندرويد 6.0+ · الإصدار 1.0.0 · com.talabtek.partner
</p>

<p align="center">
  <a href="apk/talabtek-partner-1.0.0.apk"><b>⬇️ تحميل APK</b></a> ·
  <a href="https://github.com/ahmedhassan1948/talabtek-partner-android/releases/latest">أحدث إصدار</a>
</p>

---

## عن التطبيق

تطبيق أصحاب المطاعم على منصة طلبتك: إشعار فوري لكل طلب جديد، قبول أو رفض بضغطة، إدارة المنيو والأسعار، ومتابعة مبيعاتك اليومية.

هذا التطبيق **غلاف WebView احترافي** لواجهة الويب الخاصة بـ [طلبتك](https://talabtek.com) — خفيف جداً (أقل من 0.1 ميجابايت)،
ويعمل دائماً بأحدث نسخة من الموقع بدون تحديث من المتجر.

## المزايا

- إشعارات لحظية للطلبات الجديدة
- قبول / رفض / تحضير / تجهيز الطلب
- إدارة الأقسام والأصناف والأسعار
- إيقاف/تشغيل استقبال الطلبات
- تعديل رسوم التوصيل والحد الأدنى
- تقارير مبيعات اليوم والأسبوع
- الأسعار بالريال العماني بثلاث خانات

## لقطات الشاشة

<img src="screenshots/om-partner-dash.png" width="260" alt="partner dash" />
<img src="screenshots/om-partner-orders.png" width="260" alt="partner orders" />
<img src="screenshots/om-partner-menu.png" width="260" alt="partner menu" />
<img src="screenshots/om-partner-reports.png" width="260" alt="partner reports" />
<img src="screenshots/om-partner-settings.png" width="260" alt="partner settings" />

## التثبيت

1. حمّل ملف APK من [هنا](apk/talabtek-partner-1.0.0.apk) أو من صفحة الإصدارات.
2. فعّل «تثبيت من مصادر غير معروفة» لتطبيق المتصفح/الملفات.
3. افتح الملف وثبّت التطبيق.

> التطبيق موقّع بمفتاح تجريبي مرفق في `keystore/`. **قبل النشر على Google Play أنشئ مفتاحاً جديداً واحتفظ به سرّياً.**

## الحساب التجريبي

| الدور | الهاتف | كلمة المرور |
| --- | --- | --- |
| تطبيق المطاعم | `92345678` | `123456` |

التطبيق يفتح المسار `/partner` مباشرة من الخادم.

## رابط الخادم

الرابط الافتراضي هو `https://talabtek.com`. لتجربة التطبيق على خادم محلي:

1. شغّل خادم طلبتك (المنفذ 4000).
2. افتح التطبيق بدون إنترنت أو أوقف الخادم مؤقتاً لتظهر شاشة «تعذّر الاتصال».
3. اضغط **«تغيير رابط الموقع»** وأدخل رابط الخادم، مثل:
   - محاكي أندرويد: `http://10.0.2.2:4000`
   - جهاز على نفس الشبكة: `http://192.168.1.20:4000`

## البناء من المصدر

المتطلبات: **JDK 17** و **Android SDK** (build-tools 34.0.0 + platform android-34). لا يحتاج Gradle.

```powershell
powershell -ExecutionPolicy Bypass -File build.ps1
```

الناتج: `releases/talabtek-partner-1.0.0.apk` موقّع وجاهز للتثبيت.

## بنية المشروع

```
AndroidManifest.xml      # الحزمة، الصلاحيات، النشاط الرئيسي
java/com/talabtek/partner/MainActivity.java
res/values/              # الاسم، الألوان، الثيم
res/drawable/            # شاشة البداية + أيقونة تكيّفية
res/mipmap-*/            # أيقونات mdpi → xxxhdpi
keystore/                # مفتاح التوقيع التجريبي (كلمة المرور: talabtek123)
apk/                     # ملف APK الجاهز
screenshots/             # لقطات من الواجهة
build.ps1                # بناء وتوقيع بدون Gradle
```

## الأيقونة

أيقونة التطبيق بتدرّج داكن ورمز **متجر**، بأحجام mdpi (48) حتى xxxhdpi (192)
مع أيقونة تكيّفية (Adaptive Icon) لأندرويد 8+ وأيقونة متجر 512×512 في `store/icon-512.png`.

## الرخصة

© 2026 طلبتك — جميع الحقوق محفوظة. الاستخدام التجريبي مسموح مع الإشارة للمصدر.
