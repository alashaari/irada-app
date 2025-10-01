const functions = require("firebase-functions");
const admin = require("firebase-admin");

// تهيئة Firebase Admin مع Firestore الحقيقي
admin.initializeApp();

const db = admin.firestore();

// دالة للموافقة على الشراكة
exports.approvePartner = functions.https.onRequest(async (req, res) => {
  try {
    const {requestId, action} = req.query;

    if (!requestId || !action) {
      return res.status(400).json({error: "Missing parameters"});
    }

    const requestRef = db.collection("partner_requests").doc(requestId);

    if (action === "approve") {
      // تحديث حالة الطلب
      await requestRef.update({
        status: "approved",
        respondedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // إضافة الشريك للمستخدم
      const requestDoc = await requestRef.get();
      const requestData = requestDoc.data();

      if (requestData) {
        // البحث عن المستخدم بالإيميل
        const usersSnapshot = await db.collection("users")
            .where("email", "==", requestData.fromUserEmail)
            .limit(1)
            .get();

        if (!usersSnapshot.empty) {
          const userDoc = usersSnapshot.docs[0];
          await userDoc.ref.update({
            partnerEmail: requestData.toUserEmail,
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
          });
        }
      }

      return res.send(`
        <html dir="rtl" lang="ar">
        <head>
          <meta charset="UTF-8">
          <title>تمت الموافقة</title>
          <style>
            body { 
              font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
              text-align: center; 
              padding: 0;
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              color: white;
              margin: 0;
              height: 100vh;
              display: flex;
              align-items: center;
              justify-content: center;
            }
            .container {
              background: rgba(255, 255, 255, 0.1);
              padding: 40px;
              border-radius: 20px;
              backdrop-filter: blur(10px);
              box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
              max-width: 400px;
            }
            .success { 
              font-size: 28px; 
              font-weight: bold;
              margin-bottom: 20px;
            }
            .subtitle {
              font-size: 16px;
              opacity: 0.9;
              margin-bottom: 20px;
            }
            .loading {
              font-size: 14px;
              opacity: 0.7;
            }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="success">✅ تمت الموافقة على الشراكة بنجاح!</div>
            <div class="subtitle">تم إضافتك كشريك في تطبيق إرادة</div>
            <div class="loading">جاري إغلاق الصفحة...</div>
          </div>
          <script>
            setTimeout(() => {
              window.close();
            }, 3000);
          </script>
        </body>
        </html>
      `);
    } else if (action === "reject") {
      await requestRef.update({
        status: "rejected",
        respondedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      return res.send(`
        <html dir="rtl" lang="ar">
        <head>
          <meta charset="UTF-8">
          <title>تم الرفض</title>
          <style>
            body { 
              font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
              text-align: center; 
              padding: 0;
              background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
              color: white;
              margin: 0;
              height: 100vh;
              display: flex;
              align-items: center;
              justify-content: center;
            }
            .container {
              background: rgba(255, 255, 255, 0.1);
              padding: 40px;
              border-radius: 20px;
              backdrop-filter: blur(10px);
              box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
              max-width: 400px;
            }
            .reject { 
              font-size: 28px; 
              font-weight: bold;
              margin-bottom: 20px;
            }
            .subtitle {
              font-size: 16px;
              opacity: 0.9;
              margin-bottom: 20px;
            }
            .loading {
              font-size: 14px;
              opacity: 0.7;
            }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="reject">❌ تم رفض الشراكة</div>
            <div class="subtitle">لم يتم إضافتك كشريك</div>
            <div class="loading">جاري إغلاق الصفحة...</div>
          </div>
          <script>
            setTimeout(() => {
              window.close();
            }, 3000);
          </script>
        </body>
        </html>
      `);
    }

  } catch (error) {
    console.error('Error:', error);
    res.status(500).json({error: error.message});
  }
});

// دالة للموافقة على إلغاء الميزة
exports.approveFeatureDisable = functions.https.onRequest(async (req, res) => {
  try {
    const {requestId, action} = req.query;

    if (!requestId || !action) {
      return res.status(400).json({error: "Missing parameters"});
    }

    const requestRef = db.collection("partner_requests").doc(requestId);

    if (action === "approve") {
      // تحديث حالة الطلب
      await requestRef.update({
        status: "approved",
        respondedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // إلغاء تفعيل الميزة
      const requestDoc = await requestRef.get();
      const requestData = requestDoc.data();

      if (requestData) {
        // البحث عن المستخدم بالإيميل
        const usersSnapshot = await db.collection("users")
            .where("email", "==", requestData.fromUserEmail)
            .limit(1)
            .get();

        if (!usersSnapshot.empty) {
          const userDoc = usersSnapshot.docs[0];
          await userDoc.ref.update({
            [`features.${requestData.featureName}`]: false,
            [`lastDisabled.${requestData.featureName}`]: 
                admin.firestore.FieldValue.serverTimestamp(),
          });
        }
      }

      return res.send(`
        <html dir="rtl" lang="ar">
        <head>
          <meta charset="UTF-8">
          <title>تمت الموافقة على إلغاء الميزة</title>
          <style>
            body { 
              font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
              text-align: center; 
              padding: 0;
              background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
              color: white;
              margin: 0;
              height: 100vh;
              display: flex;
              align-items: center;
              justify-content: center;
            }
            .container {
              background: rgba(255, 255, 255, 0.1);
              padding: 40px;
              border-radius: 20px;
              backdrop-filter: blur(10px);
              box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
              max-width: 400px;
            }
            .success { 
              font-size: 28px; 
              font-weight: bold;
              margin-bottom: 20px;
            }
            .subtitle {
              font-size: 16px;
              opacity: 0.9;
              margin-bottom: 20px;
            }
            .loading {
              font-size: 14px;
              opacity: 0.7;
            }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="success">✅ تمت الموافقة على إلغاء الميزة!</div>
            <div class="subtitle">تم إلغاء تفعيل الميزة بنجاح</div>
            <div class="loading">جاري إغلاق الصفحة...</div>
          </div>
          <script>
            setTimeout(() => {
              window.close();
            }, 3000);
          </script>
        </body>
        </html>
      `);
    } else if (action === "reject") {
      await requestRef.update({
        status: "rejected",
        respondedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      return res.send(`
        <html dir="rtl" lang="ar">
        <head>
          <meta charset="UTF-8">
          <title>تم رفض إلغاء الميزة</title>
          <style>
            body { 
              font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
              text-align: center; 
              padding: 0;
              background: linear-gradient(135deg, #f44336 0%, #d32f2f 100%);
              color: white;
              margin: 0;
              height: 100vh;
              display: flex;
              align-items: center;
              justify-content: center;
            }
            .container {
              background: rgba(255, 255, 255, 0.1);
              padding: 40px;
              border-radius: 20px;
              backdrop-filter: blur(10px);
              box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
              max-width: 400px;
            }
            .reject { 
              font-size: 28px; 
              font-weight: bold;
              margin-bottom: 20px;
            }
            .subtitle {
              font-size: 16px;
              opacity: 0.9;
              margin-bottom: 20px;
            }
            .loading {
              font-size: 14px;
              opacity: 0.7;
            }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="reject">❌ تم رفض إلغاء الميزة</div>
            <div class="subtitle">لم يتم إلغاء تفعيل الميزة</div>
            <div class="loading">جاري إغلاق الصفحة...</div>
          </div>
          <script>
            setTimeout(() => {
              window.close();
            }, 3000);
          </script>
        </body>
        </html>
      `);
    }

  } catch (error) {
    console.error('Error:', error);
    res.status(500).json({error: error.message});
  }
});

// دالة للموافقة على الشراكة
exports.approvePartner = functions.https.onRequest(async (req, res) => {
  try {
    const {requestId, action} = req.query;

    if (!requestId || !action) {
      return res.status(400).json({error: "Missing parameters"});
    }

    const db = admin.firestore();
    // نحتاج معرفة userId أولاً - سنستخدم طريقة مختلفة
    const requestRef = db.collection("partner_requests").doc(requestId);

    if (action === "approve") {
      await requestRef.update({
        status: "approved",
        respondedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // إضافة الشريك للمستخدم
      const requestDoc = await requestRef.get();
      const requestData = requestDoc.data();

      if (requestData) {
        // البحث عن المستخدم بالإيميل
        const usersSnapshot = await db.collection("users")
            .where("email", "==", requestData.fromUserEmail)
            .limit(1)
            .get();

        if (!usersSnapshot.empty) {
          const userDoc = usersSnapshot.docs[0];
          await userDoc.ref.update({
            partnerEmail: requestData.toUserEmail,
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
          });
        }
      }

      return res.send(`
                <html dir="rtl" lang="ar">
                <head>
                    <meta charset="UTF-8">
                    <title>تمت الموافقة</title>
                    <style>
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, 
                            Verdana, sans-serif; 
                            text-align: center; 
                            padding: 0;
                            background: linear-gradient(135deg, 
                                #667eea 0%, #764ba2 100%);
                            color: white;
                            margin: 0;
                            height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }
                        .container {
                            background: rgba(255, 255, 255, 0.1);
                            padding: 40px;
                            border-radius: 20px;
                            backdrop-filter: blur(10px);
                            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
                        }
                        .success { 
                            font-size: 28px; 
                            font-weight: bold;
                            margin-bottom: 20px;
                            animation: fadeIn 1s ease-in-out;
                        }
                        .subtitle {
                            font-size: 16px;
                            opacity: 0.9;
                            animation: fadeIn 1.5s ease-in-out;
                        }
                        @keyframes fadeIn {
                            0% { opacity: 0; transform: scale(0.8); }
                            100% { opacity: 1; transform: scale(1); }
                        }
                        .loading {
                            margin-top: 20px;
                            font-size: 14px;
                            opacity: 0.7;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="success">✅ تمت الموافقة على الشراكة بنجاح!</div>
                        <div class="subtitle">تم إضافتك كشريك في تطبيق إرادة</div>
                        <div class="loading">جاري إغلاق الصفحة...</div>
                    </div>
                    <script>
                        setTimeout(() => {
                            window.close();
                        }, 3000);
                    </script>
                </body>
                </html>
            `);
    } else if (action === "reject") {
      await requestRef.update({
        status: "rejected",
        respondedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      return res.send(`
                <html dir="rtl" lang="ar">
                <head>
                    <meta charset="UTF-8">
                    <title>تم الرفض</title>
                    <style>
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, 
                            Verdana, sans-serif; 
                            text-align: center; 
                            padding: 0;
                            background: linear-gradient(135deg, 
                                #ff6b6b 0%, #ee5a24 100%);
                            color: white;
                            margin: 0;
                            height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }
                        .container {
                            background: rgba(255, 255, 255, 0.1);
                            padding: 40px;
                            border-radius: 20px;
                            backdrop-filter: blur(10px);
                            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
                        }
                        .reject { 
                            font-size: 28px; 
                            font-weight: bold;
                            margin-bottom: 20px;
                            animation: fadeIn 1s ease-in-out;
                        }
                        .subtitle {
                            font-size: 16px;
                            opacity: 0.9;
                            animation: fadeIn 1.5s ease-in-out;
                        }
                        @keyframes fadeIn {
                            0% { opacity: 0; transform: scale(0.8); }
                            100% { opacity: 1; transform: scale(1); }
                        }
                        .loading {
                            margin-top: 20px;
                            font-size: 14px;
                            opacity: 0.7;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="reject">❌ تم رفض الشراكة</div>
                        <div class="subtitle">لم يتم إضافتك كشريك</div>
                        <div class="loading">جاري إغلاق الصفحة...</div>
                    </div>
                    <script>
                        setTimeout(() => {
                            window.close();
                        }, 3000);
                    </script>
                </body>
                </html>
            `);
    }
  } catch (error) {
    console.error("Error:", error);
    return res.status(500).json({error: "Internal server error"});
  }
});

// دالة للموافقة على إلغاء تفعيل الميزة
exports.approveFeatureDisable = functions.https.onRequest(async (req, res) => {
  try {
    const {requestId, action} = req.query;

    if (!requestId || !action) {
      return res.status(400).json({error: "Missing parameters"});
    }

    const db = admin.firestore();
    // نحتاج معرفة userId أولاً - سنستخدم طريقة مختلفة
    const requestRef = db.collection("partner_requests").doc(requestId);

    if (action === "approve") {
      await requestRef.update({
        status: "approved",
        respondedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // إلغاء تفعيل الميزة
      const requestDoc = await requestRef.get();
      const requestData = requestDoc.data();

      if (requestData) {
        // البحث عن المستخدم بالإيميل
        const usersSnapshot = await db.collection("users")
            .where("email", "==", requestData.fromUserEmail)
            .limit(1)
            .get();

        if (!usersSnapshot.empty) {
          const userDoc = usersSnapshot.docs[0];
          await userDoc.ref.update({
            [`features.${requestData.featureName}`]: false,
            [`lastDisabled.${requestData.featureName}`]: 
                admin.firestore.FieldValue.serverTimestamp(),
          });
        }
      }

      return res.send(`
                <html dir="rtl" lang="ar">
                <head>
                    <meta charset="UTF-8">
                    <title>تم إلغاء الميزة</title>
                    <style>
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, 
                            Verdana, sans-serif; 
                            text-align: center; 
                            padding: 0;
                            background: linear-gradient(135deg, 
                                #667eea 0%, #764ba2 100%);
                            color: white;
                            margin: 0;
                            height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }
                        .container {
                            background: rgba(255, 255, 255, 0.1);
                            padding: 40px;
                            border-radius: 20px;
                            backdrop-filter: blur(10px);
                            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
                        }
                        .success { 
                            font-size: 28px; 
                            font-weight: bold;
                            margin-bottom: 20px;
                            animation: fadeIn 1s ease-in-out;
                        }
                        .subtitle {
                            font-size: 16px;
                            opacity: 0.9;
                            animation: fadeIn 1.5s ease-in-out;
                        }
                        @keyframes fadeIn {
                            0% { opacity: 0; transform: scale(0.8); }
                            100% { opacity: 1; transform: scale(1); }
                        }
                        .loading {
                            margin-top: 20px;
                            font-size: 14px;
                            opacity: 0.7;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="success">✅ تم إلغاء الميزة بنجاح!</div>
                        <div class="subtitle">تم إلغاء تفعيل الميزة 
                            بناءً على موافقتك</div>
                        <div class="loading">جاري إغلاق الصفحة...</div>
                    </div>
                    <script>
                        setTimeout(() => {
                            window.close();
                        }, 3000);
                    </script>
                </body>
                </html>
            `);
    } else if (action === "reject") {
      await requestRef.update({
        status: "rejected",
        respondedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      return res.send(`
                <html dir="rtl" lang="ar">
                <head>
                    <meta charset="UTF-8">
                    <title>تم رفض إلغاء الميزة</title>
                    <style>
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, 
                            Verdana, sans-serif; 
                            text-align: center; 
                            padding: 0;
                            background: linear-gradient(135deg, 
                                #ff6b6b 0%, #ee5a24 100%);
                            color: white;
                            margin: 0;
                            height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }
                        .container {
                            background: rgba(255, 255, 255, 0.1);
                            padding: 40px;
                            border-radius: 20px;
                            backdrop-filter: blur(10px);
                            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
                        }
                        .reject { 
                            font-size: 28px; 
                            font-weight: bold;
                            margin-bottom: 20px;
                            animation: fadeIn 1s ease-in-out;
                        }
                        .subtitle {
                            font-size: 16px;
                            opacity: 0.9;
                            animation: fadeIn 1.5s ease-in-out;
                        }
                        @keyframes fadeIn {
                            0% { opacity: 0; transform: scale(0.8); }
                            100% { opacity: 1; transform: scale(1); }
                        }
                        .loading {
                            margin-top: 20px;
                            font-size: 14px;
                            opacity: 0.7;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="reject">❌ تم رفض إلغاء الميزة</div>
                        <div class="subtitle">لم يتم إلغاء تفعيل الميزة</div>
                        <div class="loading">جاري إغلاق الصفحة...</div>
                    </div>
                    <script>
                        setTimeout(() => {
                            window.close();
                        }, 3000);
                    </script>
                </body>
                </html>
            `);
    }
  } catch (error) {
    console.error("Error:", error);
    return res.status(500).json({error: "Internal server error"});
  }
});