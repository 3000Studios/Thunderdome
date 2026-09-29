// Cloudflare Worker + Durable Object Server for Thunder Dome 1v1 Dogfights
// Deploy with Wrangler: wrangler deploy

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname === "/ws") {
      const id = env.DOGFIGHT_ROOMS.idFromName("global_matchmaker");
      const obj = env.DOGFIGHT_ROOMS.get(id);
      return obj.fetch(request);
    }
    return new Response("Thunder Dome Multiplayer Edge Server Active", { status: 200 });
  }
};

export class DogfightRoom {
  constructor(state, env) {
    this.state = state;
    this.env = env;
    this.waitingPlayer = null;
    this.rooms = new Map();
  }

  async fetch(request) {
    const webSocketPair = new WebSocketPair();
    const [client, server] = Object.values(webSocketPair);

    server.accept();

    server.addEventListener("message", async (event) => {
      try {
        const msg = JSON.parse(event.data);
        if (msg.action === "join_match") {
          if (!this.waitingPlayer) {
            // First player waiting for match
            this.waitingPlayer = { ws: server, callsign: msg.callsign, aircraftId: msg.aircraftId };
          } else {
            // Match found! Pair with waiting player
            const roomId = "room_" + Date.now();
            const player1 = this.waitingPlayer;
            const player2 = { ws: server, callsign: msg.callsign, aircraftId: msg.aircraftId };
            this.waitingPlayer = null;

            this.rooms.set(roomId, { p1: player1, p2: player2 });

            player1.ws.send(JSON.stringify({
              event: "match_found",
              roomId: roomId,
              isHost: true,
              opponent: { callsign: player2.callsign, aircraftId: player2.aircraftId }
            }));

            player2.ws.send(JSON.stringify({
              event: "match_found",
              roomId: roomId,
              isHost: false,
              opponent: { callsign: player1.callsign, aircraftId: player1.aircraftId }
            }));
          }
        } else if (msg.action === "state_update") {
          const room = this.rooms.get(msg.roomId);
          if (room) {
            const targetWs = (server === room.p1.ws) ? room.p2.ws : room.p1.ws;
            msg.event = "state_update";
            targetWs.send(JSON.stringify(msg));
          }
        }
      } catch (e) {
        console.error(e);
      }
    });

    return new Response(null, { status: 101, webSocket: client });
  }
}
