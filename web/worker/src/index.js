const JSON_HEADERS={"content-type":"application/json; charset=utf-8","cache-control":"no-store"};
const cors=(env)=>({"access-control-allow-origin":env.PUBLIC_SITE_ORIGIN||"*","access-control-allow-headers":"authorization,content-type","access-control-allow-methods":"GET,POST,OPTIONS"});
const json=(data,status=200,env={})=>new Response(JSON.stringify(data),{status,headers:{...JSON_HEADERS,...cors(env)}});

async function authenticatedUser(request,env){
  const auth=request.headers.get("authorization")||"";
  if(!auth.startsWith("Bearer ")) return null;
  if(!env.SUPABASE_URL||!env.SUPABASE_ANON_KEY) return null;
  const r=await fetch(`${env.SUPABASE_URL}/auth/v1/user`,{headers:{authorization:auth,apikey:env.SUPABASE_ANON_KEY}});
  if(!r.ok) return null;
  const user=await r.json();
  return user?.id?user:null;
}

export class MatchRoom{
  constructor(state,env){this.state=state;this.env=env}
  async fetch(request){
    if((request.headers.get("upgrade")||"").toLowerCase()!=="websocket") return new Response("WebSocket required",{status:426});
    const user=await authenticatedUser(request,this.env);
    if(!user) return new Response("Unauthorized",{status:401});
    const pair=new WebSocketPair();
    const [client,server]=Object.values(pair);
    this.state.acceptWebSocket(server,[`user:${user.id}`]);
    server.send(JSON.stringify({type:"connected",userId:user.id,serverTime:Date.now()}));
    this.broadcast({type:"presence",event:"join",userId:user.id,serverTime:Date.now()},server);
    return new Response(null,{status:101,webSocket:client});
  }
  broadcast(payload,except){
    const body=JSON.stringify(payload);
    for(const ws of this.state.getWebSockets()) if(ws!==except&&ws.readyState===1) ws.send(body);
  }
  webSocketMessage(ws,message){
    if(typeof message!=="string"||message.length>8192){ws.close(1009,"Message too large");return}
    let data;try{data=JSON.parse(message)}catch{ws.send(JSON.stringify({type:"error",code:"INVALID_JSON"}));return}
    const allowed=new Set(["input","state","shot","hit","powerup","ready","ping"]);
    if(!allowed.has(data?.type)){ws.send(JSON.stringify({type:"error",code:"INVALID_EVENT"}));return}
    const tags=this.state.getTags(ws)||[];
    const userId=(tags.find(x=>x.startsWith("user:"))||"user:unknown").slice(5);
    this.broadcast({type:data.type,userId,payload:data.payload??null,seq:Number(data.seq||0),serverTime:Date.now()},ws);
    if(data.type==="ping") ws.send(JSON.stringify({type:"pong",seq:Number(data.seq||0),serverTime:Date.now()}));
  }
  webSocketClose(ws){
    const tags=this.state.getTags(ws)||[];
    const userId=(tags.find(x=>x.startsWith("user:"))||"user:unknown").slice(5);
    this.broadcast({type:"presence",event:"leave",userId,serverTime:Date.now()},ws);
  }
}

async function publicStats(env){
  if(!env.DB) return json({configured:false,online:0,matches:0,sorties:0,perfectRuns:0},503,env);
  const today=new Date().toISOString().slice(0,10);
  const row=await env.DB.prepare("SELECT sorties, perfect_runs FROM daily_stats WHERE day=?").bind(today).first();
  return json({configured:true,online:0,matches:0,sorties:Number(row?.sorties||0),perfectRuns:Number(row?.perfect_runs||0)},200,env);
}

async function leaderboard(env){
  if(!env.DB) return json([],503,env);
  const q=await env.DB.prepare("SELECT display_name AS displayName, best_score AS score FROM player_stats WHERE is_public=1 ORDER BY best_score DESC LIMIT 50").all();
  return json(q.results||[],200,env);
}

async function ingest(request,env){
  const user=await authenticatedUser(request,env);
  if(!user) return json({error:"unauthorized"},401,env);
  if(!env.DB) return json({error:"database_not_configured"},503,env);
  let body;try{body=await request.json()}catch{return json({error:"invalid_json"},400,env)}
  const allowed=new Set(["sortie_complete","perfect_run","boss_defeated","multiplayer_complete"]);
  if(!allowed.has(body?.type)) return json({error:"unsupported_event"},400,env);
  const stage=Math.max(1,Math.min(24,Number(body.stage||1)));
  const score=Math.max(0,Math.min(2147483647,Number(body.score||0)));
  const displayName=String(body.displayName||"Pilot").replace(/[<>]/g,"").slice(0,32);
  const eventId=crypto.randomUUID();
  const now=new Date(); const day=now.toISOString().slice(0,10);
  await env.DB.batch([
    env.DB.prepare("INSERT INTO game_events(id,user_id,event_type,stage,score,created_at) VALUES(?,?,?,?,?,?)").bind(eventId,user.id,body.type,stage,score,now.toISOString()),
    env.DB.prepare("INSERT INTO player_stats(user_id,display_name,best_score,is_public,updated_at) VALUES(?,?,?,?,?) ON CONFLICT(user_id) DO UPDATE SET display_name=excluded.display_name,best_score=MAX(player_stats.best_score,excluded.best_score),updated_at=excluded.updated_at").bind(user.id,displayName,score,body.publicLeaderboard===false?0:1,now.toISOString()),
    env.DB.prepare("INSERT INTO daily_stats(day,sorties,perfect_runs) VALUES(?,?,?) ON CONFLICT(day) DO UPDATE SET sorties=sorties+excluded.sorties,perfect_runs=perfect_runs+excluded.perfect_runs").bind(day,body.type==="sortie_complete"?1:0,body.type==="perfect_run"?1:0)
  ]);
  return json({ok:true,eventId},202,env);
}

export default{
  async fetch(request,env){
    const url=new URL(request.url);
    if(request.method==="OPTIONS") return new Response(null,{status:204,headers:cors(env)});
    if(url.pathname==="/api/public-stats"&&request.method==="GET") return publicStats(env);
    if(url.pathname==="/api/leaderboard"&&request.method==="GET") return leaderboard(env);
    if(url.pathname==="/api/game-event"&&request.method==="POST") return ingest(request,env);
    if(url.pathname.startsWith("/ws/room/")){
      const room=url.pathname.slice("/ws/room/".length).replace(/[^a-zA-Z0-9_-]/g,"").slice(0,64);
      if(!room) return json({error:"room_required"},400,env);
      const id=env.MATCH_ROOM.idFromName(room); return env.MATCH_ROOM.get(id).fetch(request);
    }
    if(url.pathname==="/health") return json({ok:true,service:"thunder-dome-network"},200,env);
    return json({error:"not_found"},404,env);
  }
};