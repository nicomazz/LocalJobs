const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp()

const db = admin.firestore();

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
// todo rendere bella questa funzione. In particolare togliere i livelli nested
exports.sendNotification = functions.firestore.document("/jobs/{job_id}/requests/{requester_id}")
   .onCreate( (snap,context) => {
      const request = snap.data()
      console.log('notification',request,context.params.job_id, context.params.requester_id);
      const name = request.name;
      const job_publisher = request.job_publisher_id;
      const query = db.collection("user_messaging_tokens").doc(job_publisher);
      query.get().then( doc => {
         console.log("document: ",doc.data());
         const fcm_id = doc.data().id;
         const payload = {
            notification: {
               title: "New interest from "+name,
               body: "Go to Localjob and find out how to contact him",
            }
         };
         admin.messaging().sendToDevice(fcm_id,payload)
            .then(function(response){
               console.log("successfully sent message: ",response);
            })
            .catch(function(error){
               console.log("error in sending message: ",error);
            });

      }).catch(err => {
         console.log("error getting document",err);
      });
      //todo do send notification
   });
