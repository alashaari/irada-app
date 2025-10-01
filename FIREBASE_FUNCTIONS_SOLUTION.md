# 🔧 Firebase Functions - حل الموافقة على الشراكة

## 📁 **إنشاء Firebase Functions:**

### **1. إنشاء مجلد functions:**
```bash
# في مجلد المشروع الرئيسي
mkdir functions
cd functions
npm init -y
```

### **2. تثبيت Firebase Functions:**
```bash
npm install firebase-functions firebase-admin
```

### **3. إنشاء ملف functions/index.js:**
```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

// دالة للموافقة على الشراكة
exports.approvePartner = functions.https.onRequest(async (req, res) => {
    try {
        const { requestId, action } = req.query;
        
        if (!requestId || !action) {
            return res.status(400).json({ error: 'Missing parameters' });
        }
        
        const db = admin.firestore();
        const requestRef = db.collection('partnerRequests').doc(requestId);
        
        if (action === 'approve') {
            await requestRef.update({
                status: 'approved',
                respondedAt: admin.firestore.FieldValue.serverTimestamp()
            });
            
            // إضافة الشريك للمستخدم
            const requestDoc = await requestRef.get();
            const requestData = requestDoc.data();
            
            if (requestData) {
                const userRef = db.collection('users').doc(requestData.fromUserId);
                await userRef.update({
                    partners: admin.firestore.FieldValue.arrayUnion(requestData.toUserEmail)
                });
            }
            
            return res.send(`
                <html dir="rtl" lang="ar">
                <head>
                    <meta charset="UTF-8">
                    <title>تمت الموافقة</title>
                    <style>
                        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                        .success { color: green; font-size: 24px; }
                    </style>
                </head>
                <body>
                    <div class="success">✅ تمت الموافقة على الشراكة بنجاح!</div>
                    <p>يمكنك الآن إغلاق هذه الصفحة.</p>
                </body>
                </html>
            `);
        } else if (action === 'reject') {
            await requestRef.update({
                status: 'rejected',
                respondedAt: admin.firestore.FieldValue.serverTimestamp()
            });
            
            return res.send(`
                <html dir="rtl" lang="ar">
                <head>
                    <meta charset="UTF-8">
                    <title>تم الرفض</title>
                    <style>
                        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                        .reject { color: red; font-size: 24px; }
                    </style>
                </head>
                <body>
                    <div class="reject">❌ تم رفض الشراكة</div>
                    <p>يمكنك الآن إغلاق هذه الصفحة.</p>
                </body>
                </html>
            `);
        }
        
    } catch (error) {
        console.error('Error:', error);
        return res.status(500).json({ error: 'Internal server error' });
    }
});
```

### **4. تحديث PartnerManager.kt:**
```kotlin
// في دالة sendPartnerInvitation
put("approve_url", "https://us-central1-YOUR_PROJECT_ID.cloudfunctions.net/approvePartner?requestId=${requestId}&action=approve")
put("reject_url", "https://us-central1-YOUR_PROJECT_ID.cloudfunctions.net/approvePartner?requestId=${requestId}&action=reject")
```

## 🚀 **الخطوات:**

### **1. أنشئ Firebase Functions:**
- اتبع الخطوات أعلاه
- استبدل `YOUR_PROJECT_ID` بـ Project ID الخاص بك

### **2. حدث PartnerManager.kt:**
- أضف requestId للـ JSON
- حدث الروابط لتشير لـ Firebase Functions

### **3. اختبر النظام:**
- أضف شريك جديد
- اضغط على "موافق" في الإيميل
- يجب أن يتم إضافة الشريك مباشرة

---

**هل تريد أن أطبق هذا الحل؟** 🚀

