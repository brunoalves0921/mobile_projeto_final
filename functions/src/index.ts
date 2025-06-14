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

    if (!dadosAntigos || !dadosNovos) {
      return;
    }

    const precoAntigo = dadosAntigos.precoEmCentavos;
    const precoNovo = dadosNovos.precoEmCentavos;

    if (precoNovo >= precoAntigo) {
      return;
    }

    const alertasSnapshot = await db
      .collection("priceAlerts")
      .where("productId", "==", event.params.produtoId)
      .get();

    if (alertasSnapshot.empty) {
      return;
    }

    const promises = alertasSnapshot.docs.map(async (doc) => {
      const alerta = doc.data();
      const precoDesejado = alerta.desiredPriceInCents;

      if (precoNovo <= precoDesejado) {
        const fcmToken = (
          await db.collection("users").doc(alerta.userId).get()
        ).data()?.fcmToken;

        const notificationTitle = "Alerta de Preço! 📉";
        const notificationBody = `O produto "${
          alerta.produtoNome
        }" baixou para R$${(precoNovo / 100).toFixed(2)}!`;

        if (fcmToken) {
          const message = {
            notification: {title: notificationTitle, body: notificationBody},
            token: fcmToken,
          };
          await messaging.send(message);
        }

        // --- MUDANÇA AQUI ---
        // Salva a notificação em uma nova coleção para o histórico do usuário
        await db.collection("users")
          .doc(alerta.userId)
          .collection("userNotifications")
          .add({
            title: notificationTitle,
            body: notificationBody,
            productId: alerta.productId,
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
          });

        return doc.ref.delete();
      } else {
        return Promise.resolve();
      }
    });

    return Promise.all(promises);
  }
);