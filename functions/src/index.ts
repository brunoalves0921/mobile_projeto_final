import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";

admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();

export const checarQuedaDePreco = onDocumentUpdated(
  "produtos/{produtoId}",
  async (event) => {
    const dadosAntigos = event.data?.before.data();
    const dadosNovos = event.data?.after.data();

    if (
      !dadosAntigos ||
      !dadosNovos ||
      dadosNovos.precoEmCentavos >= dadosAntigos.precoEmCentavos
    ) {
      return;
    }

    const alertasSnapshot = await db
      .collection("priceAlerts")
      .where("productId", "==", event.params.produtoId)
      .get();

    if (alertasSnapshot.empty) {
      return;
    }

    const tasks: Promise<unknown>[] = [];

    alertasSnapshot.forEach((doc) => {
      const alerta = doc.data();
      const precoDesejado = alerta.desiredPriceInCents;

      if (dadosNovos.precoEmCentavos <= precoDesejado) {
        const userId = alerta.userId;
        const notificationTitle = "Alerta de Preço! 📉";
        const notificationBody = `O produto "${
          alerta.produtoNome
        }" baixou para R$${(dadosNovos.precoEmCentavos / 100).toFixed(2)}!`;

        // Tarefa 1: Salva a notificação no histórico com isRead = false
        const saveNotificationTask = db
          .collection("users")
          .doc(userId)
          .collection("userNotifications")
          .add({
            title: notificationTitle,
            body: notificationBody,
            productId: alerta.productId,
            isRead: false, // <-- CAMPO ADICIONADO
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
          });
        tasks.push(saveNotificationTask);

        // Tarefa 2: Envia a notificação Push
        const sendPushTask = db
          .collection("users")
          .doc(userId)
          .get()
          .then((userDoc) => {
            const fcmToken = userDoc.data()?.fcmToken;
            if (fcmToken) {
              const message = {
                notification: {
                  title: notificationTitle,
                  body: notificationBody,
                },
                token: fcmToken,
              };
              return messaging.send(message);
            }
            return null;
          });
        tasks.push(sendPushTask);

        // Tarefa 3: Deleta o alerta temporário
        tasks.push(doc.ref.delete());
      }
    });

    return Promise.all(tasks);
  }
);