# 🔧 دليل حل مشاكل إرسال الإيميل للشريك - EmailJS

## 📋 المشاكل المحتملة والحلول

### 1. **فحص مفاتيح EmailJS** ✅
تم إضافة التحقق من صحة المفاتيح في الكود. تأكد من:

```kotlin
// في PartnerManager.kt
private const val EMAILJS_SERVICE_ID = "service_3n9xri9"
private const val EMAILJS_TEMPLATE_ID = "template_vhnznme" 
private const val EMAILJS_PUBLIC_KEY = "1i4TjwaVGz2HQXcBe"
private const val EMAILJS_PRIVATE_KEY = "7GBHimZSVAs5mpSOVeP-Z"
```

**⚠️ مهم**: يجب تحديث هذه المفاتيح من لوحة تحكم EmailJS الخاصة بك.

### 2. **فحص قالب الإيميل في EmailJS**
تأكد من أن القالب `template_vhnznme` يحتوي على المتغيرات التالية:
- `{{to_email}}`
- `{{from_email}}`
- `{{feature_name}}`
- `{{action}}`
- `{{message}}`
- `{{approve_url}}`
- `{{reject_url}}`

### 3. **فحص خدمة الإيميل**
تأكد من أن الخدمة `service_3n9xri9` مفعلة ومربوطة بحساب إيميل صحيح.

### 4. **فحص الأذونات**
تأكد من أن التطبيق لديه أذونات الإنترنت:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 🔍 خطوات التشخيص

### 1. **فحص السجلات (Logs)**
ابحث في Logcat عن:
```
IRADA_PARTNER: ========== addPartner STARTED ==========
IRADA_PARTNER: ========== sendPartnerInvitation STARTED ==========
IRADA_PARTNER: Response Code: XXX
IRADA_PARTNER: Response Body: XXX
```

### 2. **أكواد الاستجابة الشائعة**
- **200**: نجح الإرسال ✅
- **400**: بيانات غير صحيحة ❌
- **401**: مفاتيح غير صحيحة ❌
- **403**: ممنوع الوصول ❌
- **429**: تجاوز الحد المسموح ❌

### 3. **فحص البيانات المرسلة**
تأكد من أن JSON المرسل يحتوي على:
```json
{
  "service_id": "service_3n9xri9",
  "template_id": "template_vhnznme", 
  "user_id": "1i4TjwaVGz2HQXcBe",
  "template_params": {
    "to_email": "partner@example.com",
    "from_email": "user@example.com",
    "feature_name": "إضافة شريك",
    "action": "دعوة",
    "message": "تمت إضافتك كشريك...",
    "approve_url": "https://irada-app.com",
    "reject_url": "https://irada-app.com"
  }
}
```

## 🛠️ التحسينات المطبقة

### ✅ **التحقق من صحة البيانات**
- فحص تنسيق الإيميل
- منع إضافة النفس كشريك
- التحقق من مفاتيح EmailJS

### ✅ **معالجة الأخطاء المحسنة**
- رسائل خطأ واضحة
- إرجاع قيم boolean صحيحة
- تسجيل مفصل للأخطاء

### ✅ **تحسين واجهة المستخدم**
- رسائل نجاح/فشل واضحة
- إشعارات مناسبة للمستخدم

## 🚨 مشاكل شائعة وحلولها

### المشكلة: "EmailJS configuration is invalid"
**الحل**: تأكد من أن جميع المفاتيح الأربعة غير فارغة

### المشكلة: "Invalid email format"
**الحل**: تأكد من تنسيق الإيميل الصحيح

### المشكلة: "Cannot add yourself as partner"
**الحل**: استخدم إيميل مختلف عن إيميلك

### المشكلة: Response Code 401
**الحل**: تحقق من صحة Private Key في EmailJS

### المشكلة: Response Code 400
**الحل**: تحقق من صحة Service ID و Template ID

## 📱 اختبار الوظيفة

1. **افتح التطبيق**
2. **انتقل إلى إعدادات الشريك**
3. **أدخل إيميل صحيح**
4. **راقب السجلات في Logcat**
5. **تحقق من رسالة النجاح/الفشل**

## 🔐 نصائح الأمان

⚠️ **مهم**: لا تضع Private Key في الكود مباشرة في الإنتاج. استخدم:
- Firebase Remote Config
- Server-side API
- Environment variables

## 📞 الدعم

إذا استمرت المشكلة، تحقق من:
1. لوحة تحكم EmailJS
2. سجلات الخادم
3. إعدادات الشبكة
4. أذونات التطبيق

