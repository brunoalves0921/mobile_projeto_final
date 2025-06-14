import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";
import * as logger from "firebase-functions/logger"; // Adicionando logger para mais detalhes

admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();

export const checarQuedaDePreco = onDocumentUpdated(
  "produtos/{produtoId}",
  async (event) => {
    const dadosAntigos = event.data?.before.data();
    const dadosNovos = event.data?.after.data();

    if (!dadosAntigos || !dadosNovos || dadosNovos.precoEmCentavos >= dadosAntigos.precoEmCentavos) {
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
        const notificationBody = `O produto "${alerta.produtoNome}" baixou para R$${(dadosNovos.precoEmCentavos / 100).toFixed(2)}!`;

        logger.info(`(DEBUG) Alerta ativado para userId: ${userId}`);

        // Tarefa 1: Criar o registro de notificação no histórico
        const saveNotificationTask = db
          .collection("users")
          .doc(userId)
          .collection("userNotifications")
          .add({
            title: notificationTitle,
            body: notificationBody,
            productId: alerta.productId,
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
          });
        tasks.push(saveNotificationTask);
        logger.info("(DEBUG) Tarefa de salvar histórico adicionada.");

        // Tarefa 2: Enviar a notificação Push
        const sendPushTask = db.collection("users").doc(userId).get().then((userDoc) => {
            const fcmToken = userDoc.data()?.fcmToken;
            if (fcmToken) {
              const message = {
                notification: {title: notificationTitle, body: notificationBody},
                token: fcmToken,
              };
              logger.info(`(DEBUG) Enviando notificação para o token: ${fcmToken}`);
              return messaging.send(message);
            }
            logger.warn(`(DEBUG) Token FCM não encontrado para userId: ${userId}`);
            return null;
          });
        tasks.push(sendPushTask);
        logger.info("(DEBUG) Tarefa de enviar push adicionada.");

        // --- MUDANÇA PRINCIPAL PARA O TESTE ---
        // Tarefa 3: Deletar o alerta temporário (DESATIVADA)
        logger.warn(`(DEBUG) A exclusão do priceAlert ${doc.id} foi DESATIVADA para este teste.`);
        tasks.push(doc.ref.delete()); // <--- LINHA COMENTADA
      }
    });

    return Promise.all(tasks);
  }
);