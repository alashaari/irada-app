# 🔧 حل مشكلة أذونات Firebase Firestore

## 🚨 **المشكلة:**
```
PERMISSION_DENIED: Missing or insufficient permissions.
```

## 🔍 **السبب:**
Firebase Firestore يرفض الكتابة في collection `partnerRequests` بسبب قواعد الأمان.

## 🔧 **الحلول:**

### **الحل 1: تحديث قواعد الأمان في Firebase Console**

#### **1. اذهب إلى Firebase Console:**
- افتح [Firebase Console](https://console.firebase.google.com/)
- اختر مشروعك
- اذهب إلى **Firestore Database**

#### **2. اذهب إلى Rules:**
- اضغط على **Rules** في القائمة الجانبية
- ستجد قواعد مثل:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

#### **3. أضف قاعدة للـ partnerRequests:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
    
    // قاعدة خاصة لـ partnerRequests
    match /partnerRequests/{requestId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

#### **4. احفظ القواعد:**
- اضغط **Publish**

### **الحل 2: استخدام collection مختلف (مطبق)**

#### **تم تغيير الكود لاستخدام:**
```kotlin
// بدلاً من:
firestore.collection("partnerRequests")

// استخدم:
firestore.collection("users").document(userId).collection("partnerRequests")
```

## 🚀 **الخطوات التالية:**

### **1. جرب إضافة شريك مرة أخرى:**
- يجب أن يعمل الآن
- راقب السجلات

### **2. إذا استمر الخطأ:**
- اتبع الحل 1 (تحديث قواعد الأمان)
- أو أخبرني وسأجد حل آخر

### **3. راقب السجلات:**
ابحث عن:
```
IRADA_PARTNER: Firestore save SUCCESS!
IRADA_PARTNER: ✅ Partner invitation sent successfully!
```

## 🎯 **النتيجة المتوقعة:**

### **إذا نجح:**
```
IRADA_PARTNER: Firestore save SUCCESS!
IRADA_PARTNER: AppPreferences updated!
IRADA_PARTNER: ✅ Partner invitation sent successfully!
```

### **إذا فشل:**
```
IRADA_PARTNER: ERROR in addPartner: PERMISSION_DENIED
```

## 🚨 **إذا استمرت المشكلة:**

### **أخبرني:**
- ما هي رسالة الخطأ الجديدة؟
- هل جربت تحديث قواعد الأمان؟
- ما هي السجلات التي تظهر؟

---

**جرب الآن وأخبرني بالنتيجة!** 🚀

