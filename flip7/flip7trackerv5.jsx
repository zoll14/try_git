import { useState, useRef } from "react";

const FLIP7_BONUS = 15;

const styles = `
  @import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=DM+Mono:wght@400;500&display=swap');

  * { box-sizing: border-box; margin: 0; padding: 0; }

  body {
    background: #0d1117;
    font-family: 'DM Mono', monospace;
  }

  .app {
    min-height: 100vh;
    background: radial-gradient(ellipse at 20% 0%, #1a2a1a 0%, #0d1117 60%);
    color: #e8e0d0;
    padding: 24px 16px 48px;
  }

  .header {
    text-align: center;
    margin-bottom: 32px;
  }

  .logo {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 52px;
    letter-spacing: 6px;
    color: #f5c842;
    text-shadow: 0 0 30px rgba(245,200,66,0.3);
    line-height: 1;
  }

  .logo span {
    color: #fff;
  }

  .subtitle {
    font-size: 11px;
    letter-spacing: 3px;
    color: #556;
    margin-top: 4px;
    text-transform: uppercase;
  }

  /* Setup */
  .setup-card {
    max-width: 420px;
    margin: 0 auto;
    background: #161b22;
    border: 1px solid #2a3040;
    border-radius: 12px;
    padding: 28px;
  }

  .setup-title {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 22px;
    letter-spacing: 3px;
    color: #f5c842;
    margin-bottom: 20px;
  }

  .player-row {
    display: flex;
    gap: 8px;
    margin-bottom: 10px;
  }

  .input {
    background: #0d1117;
    border: 1px solid #2a3040;
    border-radius: 6px;
    color: #e8e0d0;
    font-family: 'DM Mono', monospace;
    font-size: 14px;
    padding: 8px 12px;
    flex: 1;
    outline: none;
    transition: border-color 0.15s;
  }

  .input:focus { border-color: #f5c842; }
  .input::placeholder { color: #334; }

  .btn {
    background: none;
    border: 1px solid #2a3040;
    border-radius: 6px;
    color: #e8e0d0;
    font-family: 'DM Mono', monospace;
    font-size: 13px;
    padding: 8px 14px;
    cursor: pointer;
    transition: all 0.15s;
    white-space: nowrap;
  }

  .btn:hover { border-color: #556; background: #1e2530; }

  .btn-primary {
    background: #f5c842;
    border-color: #f5c842;
    color: #0d1117;
    font-weight: 500;
  }
  .btn-primary:hover { background: #ffd84a; border-color: #ffd84a; }

  .btn-danger { border-color: #e05; color: #e05; }
  .btn-danger:hover { background: rgba(238,0,85,0.1); }

  .btn-sm { padding: 5px 10px; font-size: 12px; }

  /* Game layout */
  .game-layout {
    max-width: 900px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  /* Scoreboard */
  .scoreboard {
    background: #161b22;
    border: 1px solid #2a3040;
    border-radius: 12px;
    overflow: hidden;
  }

  .table-wrap { overflow-x: auto; }

  table {
    width: 100%;
    border-collapse: collapse;
    font-size: 13px;
  }

  th {
    background: #1e2530;
    padding: 10px 14px;
    text-align: center;
    font-size: 10px;
    letter-spacing: 2px;
    text-transform: uppercase;
    color: #778;
    border-bottom: 1px solid #2a3040;
    white-space: nowrap;
  }

  th.name-col { text-align: left; }

  th.name-col, td.name-col {
    position: sticky;
    left: 0;
    z-index: 2;
  }

  th.name-col {
    background: #1e2530;
  }

  td.name-col {
    background: #161b22;
  }

  tr:hover td.name-col {
    background: #1a2030;
  }

  .total-row td.name-col {
    background: #1e2530 !important;
  }

  td {
    padding: 9px 14px;
    text-align: center;
    border-bottom: 1px solid #1a2030;
    white-space: nowrap;
  }

  td.name-col {
    text-align: left;
    font-size: 14px;
    color: #e8e0d0;
  }

  tr:last-child td { border-bottom: none; }

  .score-cell { color: #aab; }
  .round-score { color: #e8e0d0; }
  .bust-cell { color: #e05; font-size: 11px; letter-spacing: 1px; }
  .flip7-cell { color: #f5c842; font-size: 11px; letter-spacing: 1px; }

  .total-row td {
    background: #1e2530;
    font-family: 'Bebas Neue', sans-serif;
    font-size: 18px;
    letter-spacing: 1px;
    color: #f5c842;
    border-top: 2px solid #2a3040;
  }

  .total-row td.name-col { color: #e8e0d0; font-size: 13px; letter-spacing: 2px; text-transform: uppercase; }

  .rank-badge {
    display: inline-block;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    font-size: 11px;
    line-height: 20px;
    text-align: center;
    margin-right: 6px;
    font-family: 'Bebas Neue', sans-serif;
  }

  .rank-1 { background: #f5c842; color: #0d1117; }
  .rank-2 { background: #aaa; color: #0d1117; }
  .rank-3 { background: #cd7f32; color: #0d1117; }

  /* Round entry */
  .round-panel {
    background: #161b22;
    border: 1px solid #2a3040;
    border-radius: 12px;
    padding: 24px;
  }

  .round-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
  }

  .round-title {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 22px;
    letter-spacing: 3px;
    color: #e8e0d0;
  }

  .round-title span { color: #f5c842; }

  .players-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 14px;
    margin-bottom: 20px;
  }

  .player-entry {
    background: #0d1117;
    border: 1px solid #2a3040;
    border-radius: 8px;
    padding: 14px;
    transition: border-color 0.15s;
  }

  .player-entry.is-bust { border-color: rgba(238,0,85,0.3); }
  .player-entry.is-flip7 { border-color: rgba(245,200,66,0.4); }

  .player-entry-name {
    font-size: 12px;
    letter-spacing: 2px;
    text-transform: uppercase;
    color: #778;
    margin-bottom: 10px;
  }

  .cards-input {
    width: 100%;
    margin-bottom: 10px;
  }

  .input-hint {
    font-size: 10px;
    color: #445;
    margin-bottom: 10px;
  }

  .toggle-row {
    display: flex;
    gap: 6px;
  }

  .toggle-btn {
    flex: 1;
    padding: 5px;
    font-size: 11px;
    letter-spacing: 1px;
    border-radius: 4px;
    cursor: pointer;
    border: 1px solid #2a3040;
    background: none;
    color: #778;
    font-family: 'DM Mono', monospace;
    transition: all 0.15s;
  }

  .toggle-btn.active-bust {
    background: rgba(238,0,85,0.15);
    border-color: #e05;
    color: #e05;
  }

  .toggle-btn.active-flip7 {
    background: rgba(245,200,66,0.15);
    border-color: #f5c842;
    color: #f5c842;
  }

  .preview-score {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 28px;
    color: #f5c842;
    margin-top: 8px;
    letter-spacing: 2px;
  }

  .preview-score.bust { color: #e05; }

  .action-row {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
    margin-top: 4px;
  }

  .divider {
    border: none;
    border-top: 1px solid #1e2530;
    margin: 16px 0;
  }

  .empty-state {
    text-align: center;
    padding: 40px;
    color: #334;
    font-size: 13px;
    letter-spacing: 1px;
  }

  /* Confirm dialog */
  .dialog-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 100;
    padding: 24px;
  }

  .dialog-box {
    background: #161b22;
    border: 1px solid #2a3040;
    border-radius: 12px;
    padding: 28px 24px 20px;
    max-width: 320px;
    width: 100%;
  }

  .dialog-msg {
    font-size: 14px;
    color: #e8e0d0;
    line-height: 1.6;
    margin-bottom: 20px;
  }

  .dialog-btns {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
  }
`;

function parseCards(str) {
  return str.split(/[\s,]+/).map(s => parseInt(s)).filter(n => !isNaN(n));
}

function calcScore(cards, bust, flip7, plusCards, multiplier) {
  if (bust) return 0;
  const numSum = cards.reduce((a, b) => a + b, 0);
  const plusSum = plusCards.reduce((a, b) => a + b, 0);
  const base = (numSum + plusSum) * (multiplier ? 2 : 1);
  return base + (flip7 ? FLIP7_BONUS : 0);
}

function getRank(players, totals) {
  const sorted = [...players].sort((a, b) => (totals[b] || 0) - (totals[a] || 0));
  return (name) => sorted.indexOf(name) + 1;
}

export default function Flip7Tracker() {
  const [phase, setPhase] = useState("setup"); // setup | game
  const [playerName, setPlayerName] = useState("");
  const [players, setPlayers] = useState([]);
  const [rounds, setRounds] = useState([]); // [{name: score}, ...]
  const [entry, setEntry] = useState({}); // {name: {cards, bust, flip7}}
  const [newPlayerName, setNewPlayerName] = useState("");
  const [dialog, setDialog] = useState(null); // {msg, onConfirm}

  const showConfirm = (msg, onConfirm) => setDialog({ msg, onConfirm });
  const closeDialog = () => setDialog(null);

  const addPlayerInGame = () => {
    const n = newPlayerName.trim();
    if (!n || players.includes(n)) return;
    setPlayers(prev => [...prev, n]);
    setEntry(prev => ({ ...prev, [n]: { cards: "", plusCards: "", bust: false, flip7: false, multiplier: false } }));
    setNewPlayerName("");
  };

  // Setup
  const addPlayer = () => {
    const n = playerName.trim();
    if (n && !players.includes(n)) {
      setPlayers([...players, n]);
      setPlayerName("");
    }
  };

  const removePlayer = (n) => setPlayers(players.filter(p => p !== n));

  const startGame = () => {
    if (players.length < 2) return;
    const init = {};
    players.forEach(p => { init[p] = { cards: "", plusCards: "", bust: false, flip7: false, multiplier: false }; });
    setEntry(init);
    setPhase("game");
  };

  // Entry helpers
  const updateEntry = (name, field, value) => {
    setEntry(prev => ({ ...prev, [name]: { ...prev[name], [field]: value } }));
  };

  const toggleBust = (name) => {
    const cur = entry[name];
    if (!cur.bust) updateEntry(name, "bust", true);
    else updateEntry(name, "bust", false);
    if (!entry[name].bust) updateEntry(name, "flip7", false);
  };

  const toggleFlip7 = (name) => {
    const cur = entry[name];
    if (!cur.flip7) { updateEntry(name, "flip7", true); updateEntry(name, "bust", false); }
    else updateEntry(name, "flip7", false);
  };

  const confirmRound = () => {
    const roundResult = {};
    players.forEach(p => {
      const e = entry[p];
      const cards = parseCards(e.cards);
      const plusCards = parseCards(e.plusCards);
      roundResult[p] = {
        score: calcScore(cards, e.bust, e.flip7, plusCards, e.multiplier),
        bust: e.bust,
        flip7: e.flip7,
        multiplier: e.multiplier,
      };
    });
    setRounds([...rounds, roundResult]);
    const init = {};
    players.forEach(p => { init[p] = { cards: "", plusCards: "", bust: false, flip7: false, multiplier: false }; });
    setEntry(init);
  };

  const undoRound = () => setRounds(rounds.slice(0, -1));

  const resetAll = () => {
    showConfirm("Mindent törlünk?\n(játékosok + körök)", () => {
      setPlayers([]);
      setRounds([]);
      setEntry({});
      setPlayerName("");
      setPhase("setup");
    });
  };

  const newGame = () => {
    showConfirm("Új játék?\n(körök törlődnek, játékosok maradnak)", () => {
      setRounds([]);
      const init = {};
      players.forEach(p => { init[p] = { cards: "", plusCards: "", bust: false, flip7: false, multiplier: false }; });
      setEntry(init);
    });
  };

  // Totals
  const totals = {};
  players.forEach(p => {
    totals[p] = rounds.reduce((sum, r) => sum + (r[p]?.score || 0), 0);
  });

  const getPreview = (name) => {
    if (!entry[name]) return null;
    const e = entry[name];
    if (e.bust) return { score: 0 };
    const cards = parseCards(e.cards);
    const plusCards = parseCards(e.plusCards);
    const score = calcScore(cards, false, e.flip7, plusCards, e.multiplier);
    return { score };
  };

  const rankOf = getRank(players, totals);

  return (
    <>
      <style>{styles}</style>
      <div className="app">
        <div className="header">
          <div className="logo">FLIP<span> 7</span></div>
          <div className="subtitle">Score Tracker</div>
        </div>

        {phase === "setup" && (
          <div className="setup-card">
            <div className="setup-title">JÁTÉKOSOK</div>
            {players.map(p => (
              <div key={p} className="player-row">
                <div className="input" style={{display:'flex',alignItems:'center',color:'#e8e0d0'}}>{p}</div>
                <button className="btn btn-danger btn-sm" onClick={() => removePlayer(p)}>✕</button>
              </div>
            ))}
            <div className="player-row">
              <input
                className="input"
                placeholder="Játékos neve..."
                value={playerName}
                onChange={e => setPlayerName(e.target.value)}
                onKeyDown={e => e.key === "Enter" && addPlayer()}
              />
              <button className="btn" onClick={addPlayer}>+ Add</button>
            </div>
            <hr className="divider" />
            <button
              className="btn btn-primary"
              style={{width:'100%',padding:'12px'}}
              disabled={players.length < 2}
              onClick={startGame}
            >
              JÁTÉK INDÍTÁSA →
            </button>
          </div>
        )}

        {phase === "game" && (
          <div className="game-layout">

            {/* Scoreboard */}
            <div className="scoreboard">
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th className="name-col">Játékos</th>
                      {rounds.map((_, i) => <th key={i}>{i + 1}. kör</th>)}
                      <th>Összesen</th>
                    </tr>
                  </thead>
                  <tbody>
                    {[...players].sort((a, b) => totals[b] - totals[a]).map(p => {
                      const rank = rankOf(p);
                      return (
                        <tr key={p}>
                          <td className="name-col">
                            {rank <= 3 && rounds.length > 0 && (
                              <span className={`rank-badge rank-${rank}`}>{rank}</span>
                            )}
                            {p}
                          </td>
                          {rounds.map((r, i) => {
                            const rd = r[p];
                            return (
                              <td key={i} className="round-score">
                                {rd?.bust ? <span className="bust-cell">BUST</span>
                                  : rd?.flip7 ? <span className="flip7-cell">★ {rd.score}</span>
                                  : <span className="score-cell">{rd?.score ?? "–"}</span>}
                              </td>
                            );
                          })}
                          <td style={{fontFamily:"'Bebas Neue',sans-serif",fontSize:20,color:'#f5c842',letterSpacing:1}}>
                            {totals[p]}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
              {rounds.length === 0 && <div className="empty-state">Még nem volt kör</div>}
            </div>

            {/* Round entry */}
            <div className="round-panel">
              <div className="round-header">
                <div className="round-title">{rounds.length + 1}. <span>KÖR</span> bevitele</div>
                <div style={{display:'flex',gap:8}}>
                  {rounds.length > 0 && (
                    <button className="btn btn-sm" onClick={undoRound}>↩ Undo</button>
                  )}
                  <button className="btn btn-sm" onClick={newGame}>↺ Új játék</button>
                  <button className="btn btn-sm btn-danger" onClick={resetAll}>✕ Reset</button>
                </div>
              </div>

              <div className="players-grid">
                {players.map(p => {
                  const e = entry[p] || { cards: "", plusCards: "", bust: false, flip7: false, multiplier: false };
                  const preview = getPreview(p);
                  return (
                    <div key={p} className={`player-entry${e.bust ? " is-bust" : e.flip7 ? " is-flip7" : ""}`}>
                      <div className="player-entry-name">{p}</div>

                      <div className="input-hint">Számkártyák</div>
                      <input
                        className="input cards-input"
                        placeholder="pl. 3 7 12 0 5"
                        value={e.cards}
                        disabled={e.bust}
                        onChange={ev => updateEntry(p, "cards", ev.target.value)}
                      />

                      <div className="input-hint" style={{marginTop:6}}>+N kártyák (összeadós)</div>
                      <input
                        className="input cards-input"
                        placeholder="pl. 2 4"
                        value={e.plusCards}
                        disabled={e.bust}
                        onChange={ev => updateEntry(p, "plusCards", ev.target.value)}
                      />

                      <div className="toggle-row" style={{marginTop:10}}>
                        <button
                          className={`toggle-btn${e.bust ? " active-bust" : ""}`}
                          onClick={() => toggleBust(p)}
                        >BUST</button>
                        <button
                          className={`toggle-btn${e.flip7 ? " active-flip7" : ""}`}
                          onClick={() => toggleFlip7(p)}
                        >FLIP 7 ★</button>
                        <button
                          className={`toggle-btn${e.multiplier ? " active-flip7" : ""}`}
                          style={e.multiplier ? {borderColor:'#4af',color:'#4af',background:'rgba(68,170,255,0.12)'} : {}}
                          disabled={e.bust}
                          onClick={() => updateEntry(p, "multiplier", !e.multiplier)}
                        >×2</button>
                      </div>

                      {preview && (
                        <div className={`preview-score${e.bust ? " bust" : ""}`}>
                          {e.bust ? "0" : preview.score}
                          {!e.bust && (e.flip7 || e.multiplier) && (
                            <span style={{fontSize:11,color:'#778',marginLeft:8,fontFamily:"'DM Mono'"}}>
                              {[e.multiplier && "×2", e.flip7 && `+${FLIP7_BONUS}`].filter(Boolean).join(" ")}
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>

              <div className="action-row">
                <input
                  className="input"
                  style={{flex:1, maxWidth: 200}}
                  placeholder="+ Új játékos neve..."
                  value={newPlayerName}
                  onChange={e => setNewPlayerName(e.target.value)}
                  onKeyDown={e => e.key === "Enter" && addPlayerInGame()}
                />
                <button className="btn" onClick={addPlayerInGame}>Hozzáad</button>
                <button className="btn btn-primary" onClick={confirmRound}>
                  KÖR RÖGZÍTÉSE →
                </button>
              </div>
            </div>

          </div>
        )}
      </div>

      {dialog && (
        <div className="dialog-overlay" onClick={closeDialog}>
          <div className="dialog-box" onClick={e => e.stopPropagation()}>
            <div className="dialog-msg">{dialog.msg.split('\n').map((l, i) => <div key={i}>{l}</div>)}</div>
            <div className="dialog-btns">
              <button className="btn" onClick={closeDialog}>Mégse</button>
              <button className="btn btn-danger" onClick={() => { dialog.onConfirm(); closeDialog(); }}>Igen</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
