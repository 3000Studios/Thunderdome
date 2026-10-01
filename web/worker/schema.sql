CREATE TABLE IF NOT EXISTS game_events (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  event_type TEXT NOT NULL,
  stage INTEGER NOT NULL CHECK(stage BETWEEN 1 AND 24),
  score INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_game_events_user_created ON game_events(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_game_events_type_created ON game_events(event_type, created_at DESC);

CREATE TABLE IF NOT EXISTS player_stats (
  user_id TEXT PRIMARY KEY,
  display_name TEXT NOT NULL,
  best_score INTEGER NOT NULL DEFAULT 0,
  is_public INTEGER NOT NULL DEFAULT 1 CHECK(is_public IN (0,1)),
  updated_at TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_player_stats_score ON player_stats(best_score DESC);

CREATE TABLE IF NOT EXISTS daily_stats (
  day TEXT PRIMARY KEY,
  sorties INTEGER NOT NULL DEFAULT 0,
  perfect_runs INTEGER NOT NULL DEFAULT 0
);