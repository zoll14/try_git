import { useState, useRef, useEffect } from "react";

const FLIP7_BONUS = 15;

const TRANSLATIONS = {
  hu: {
    setupTitle: "JÁTÉKOSOK",
    playerNamePlaceholder: "Játékos neve...",
    addPlayer: "+ Add",
    startGame: "JÁTÉK INDÍTÁSA →",
    playerCol: "Játékos",
    roundN: (n) => `${n}. kör`,
    total: "Összesen",
    noRounds: "Még nem volt kör",
    roundTitleParts: (n) => [`${n}. `, "KÖR", " bevitele"],
    undo: "↩ Undo",
    newGame: "↺ Új játék",
    reset: "✕ Reset",
    numCardsLabel: "Számkártyák (0–12)",
    numCardsPlaceholder: "pl. 3 7 12 0 5",
    plusCardsLabel: "+N kártyák (2, 4, 6, 8, 10)",
    plusCardsPlaceholder: "pl. 2 4",
    confirmRound: "KÖR RÖGZÍTÉSE →",
    newPlayerPlaceholder: "+ Új játékos neve...",
    addInGame: "Hozzáad",
    invalidNum: (bad) => `Érvénytelen: ${bad} — csak 0–12`,
    invalidPlus: (bad) => `Érvénytelen: ${bad} — csak 2, 4, 6, 8, 10`,
    resetConfirm: "Mindent törlünk?\n(játékosok + körök)",
    newGameConfirm: "Új játék?\n(körök törlődnek, játékosok maradnak)",
    cancel: "Mégse",
    yes: "Igen",
    ok: "OK",
    scanning: "LAPOK FELISMERÉSE...",
    noCardsDetected: "Nem sikerült lapokat felismerni a képen. Kérlek töltsd ki manuálisan.",
    apiError: (msg) => `API hiba: ${msg}`,
    processingError: "Feldolgozási hiba",
    androidUnavail: "Android interfész nem elérhető",
    apiKeyTitle: "CLAUDE API KULCS",
    apiKeyDesc: "A lapfelismeréshez szükséges. Csak egyszer kell megadni, az alkalmazás elmenti.",
    apiKeyPlaceholder: "sk-ant-api03-...",
    save: "Mentés →",
    pickerNumTitle: "SZÁMKÁRTYÁK",
    pickerPlusTitle: "+N KÁRTYÁK",
    pickerDone: "Kész",
    pickerClear: "Törlés",
  },
  en: {
    setupTitle: "PLAYERS",
    playerNamePlaceholder: "Player name...",
    addPlayer: "+ Add",
    startGame: "START GAME →",
    playerCol: "Player",
    roundN: (n) => `Round ${n}`,
    total: "Total",
    noRounds: "No rounds yet",
    roundTitleParts: (n) => ["ROUND ", String(n), " ENTRY"],
    undo: "↩ Undo",
    newGame: "↺ New game",
    reset: "✕ Reset",
    numCardsLabel: "Number cards (0–12)",
    numCardsPlaceholder: "e.g. 3 7 12 0 5",
    plusCardsLabel: "+N cards (2, 4, 6, 8, 10)",
    plusCardsPlaceholder: "e.g. 2 4",
    confirmRound: "CONFIRM ROUND →",
    newPlayerPlaceholder: "+ New player name...",
    addInGame: "Add",
    invalidNum: (bad) => `Invalid: ${bad} — only 0–12`,
    invalidPlus: (bad) => `Invalid: ${bad} — only 2, 4, 6, 8, 10`,
    resetConfirm: "Reset everything?\n(players + rounds)",
    newGameConfirm: "New game?\n(rounds deleted, players remain)",
    cancel: "Cancel",
    yes: "Yes",
    ok: "OK",
    scanning: "RECOGNIZING CARDS...",
    noCardsDetected: "Could not recognize cards in the photo. Please fill in manually.",
    apiError: (msg) => `API error: ${msg}`,
    processingError: "Processing error",
    androidUnavail: "Android interface unavailable",
    apiKeyTitle: "CLAUDE API KEY",
    apiKeyDesc: "Needed for card recognition. Only needs to be entered once, the app saves it.",
    apiKeyPlaceholder: "sk-ant-api03-...",
    save: "Save →",
    pickerNumTitle: "NUMBER CARDS",
    pickerPlusTitle: "+N CARDS",
    pickerDone: "Done",
    pickerClear: "Clear",
  },
};

const NUM_CARDS  = [0,1,2,3,4,5,6,7,8,9,10,11,12];
const PLUS_CARDS = [2,4,6,8,10];

const styles = `
  @import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=DM+Mono:wght@400;500&display=swap');

  * { box-sizing: border-box; margin: 0; padding: 0; }

  :root {
    --bg:        #0d1117;
    --bg-2:      #161b22;
    --bg-3:      #1e2530;
    --bg-4:      #1a2030;
    --bg-overlay: rgba(0,0,0,0.72);
    --bg-scan:   rgba(13,17,23,0.94);
    --border:    #2a3040;
    --border-2:  #556;
    --text:      #e8e0d0;
    --text-2:    #778;
    --text-3:    #445;
    --text-4:    #334;
    --accent:    #f5c842;
    --accent-h:  #ffd84a;
    --accent-on: #0d1117;
    --danger:    #ee0055;
    --danger-a:  rgba(238,0,85,0.08);
    --danger-b:  rgba(238,0,85,0.15);
    --danger-c:  rgba(238,0,85,0.3);
    --flip-a:    rgba(245,200,66,0.15);
    --flip-b:    rgba(245,200,66,0.4);
    --info:      #44aaff;
    --info-a:    rgba(68,170,255,0.12);
    --score:     #aab;
    --logo-glow: rgba(245,200,66,0.3);
    --grad-1:    #1a2a1a;
    --grad-2:    #0d1117;
  }

  .light-mode {
    --bg:        #f4f1ec;
    --bg-2:      #ffffff;
    --bg-3:      #ece8e0;
    --bg-4:      #e4e0d8;
    --bg-overlay: rgba(30,20,10,0.45);
    --bg-scan:   rgba(244,241,236,0.96);
    --border:    #ccc8be;
    --border-2:  #888;
    --text:      #1c1a16;
    --text-2:    #6a6560;
    --text-3:    #8c887e;
    --text-4:    #aaa89e;
    --accent:    #a07800;
    --accent-h:  #8c6800;
    --accent-on: #ffffff;
    --danger:    #cc0044;
    --danger-a:  rgba(204,0,68,0.07);
    --danger-b:  rgba(204,0,68,0.12);
    --danger-c:  rgba(204,0,68,0.22);
    --flip-a:    rgba(160,120,0,0.12);
    --flip-b:    rgba(160,120,0,0.28);
    --info:      #0077cc;
    --info-a:    rgba(0,119,204,0.1);
    --score:     #888;
    --logo-glow: rgba(160,120,0,0.22);
    --grad-1:    #e8ede8;
    --grad-2:    #f4f1ec;
  }

  body {
    background: var(--bg);
    font-family: 'DM Mono', monospace;
    transition: background 0.2s;
  }

  .app {
    min-height: 100vh;
    background: radial-gradient(ellipse at 20% 0%, var(--grad-1) 0%, var(--grad-2) 60%);
    color: var(--text);
    padding: 24px 16px 48px;
    transition: background 0.2s, color 0.2s;
  }

  .header { text-align: center; margin-bottom: 32px; }

  .logo {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 52px;
    letter-spacing: 6px;
    color: var(--accent);
    text-shadow: 0 0 30px var(--logo-glow);
    line-height: 1;
  }
  .logo span { color: var(--text); }

  .subtitle {
    font-size: 11px;
    letter-spacing: 3px;
    color: var(--text-2);
    margin-top: 4px;
    text-transform: uppercase;
  }

  .header-controls {
    display: flex;
    gap: 6px;
    justify-content: center;
    align-items: center;
    margin-top: 10px;
  }

  .ctrl-divider { width: 1px; height: 14px; background: var(--border); }

  .ctrl-btn {
    background: none;
    border: 1px solid var(--border);
    border-radius: 4px;
    color: var(--text-3);
    font-family: 'DM Mono', monospace;
    font-size: 11px;
    letter-spacing: 2px;
    padding: 3px 10px;
    cursor: pointer;
    transition: all 0.15s;
  }
  .ctrl-btn:hover       { border-color: var(--border-2); color: var(--text-2); }
  .ctrl-btn.active      { border-color: var(--accent); color: var(--accent); }
  .ctrl-btn.active:hover{ border-color: var(--accent); color: var(--accent); }

  /* Setup */
  .setup-card {
    max-width: 420px;
    margin: 0 auto;
    background: var(--bg-2);
    border: 1px solid var(--border);
    border-radius: 12px;
    padding: 28px;
  }

  .setup-title {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 22px;
    letter-spacing: 3px;
    color: var(--accent);
    margin-bottom: 20px;
  }

  .player-row { display: flex; gap: 8px; margin-bottom: 10px; }

  .input {
    background: var(--bg);
    border: 1px solid var(--border);
    border-radius: 6px;
    color: var(--text);
    font-family: 'DM Mono', monospace;
    font-size: 14px;
    padding: 8px 12px;
    flex: 1;
    outline: none;
    transition: border-color 0.15s;
  }
  .input:focus       { border-color: var(--accent); }
  .input::placeholder{ color: var(--text-4); }
  .input.invalid     { border-color: var(--danger); }

  .btn {
    background: none;
    border: 1px solid var(--border);
    border-radius: 6px;
    color: var(--text);
    font-family: 'DM Mono', monospace;
    font-size: 13px;
    padding: 8px 14px;
    cursor: pointer;
    transition: all 0.15s;
    white-space: nowrap;
  }
  .btn:hover { border-color: var(--border-2); background: var(--bg-3); }

  .btn-primary { background: var(--accent); border-color: var(--accent); color: var(--accent-on); font-weight: 500; }
  .btn-primary:hover { background: var(--accent-h); border-color: var(--accent-h); }

  .btn-danger { border-color: var(--danger); color: var(--danger); }
  .btn-danger:hover { background: var(--danger-a); }

  .btn-sm { padding: 5px 10px; font-size: 12px; }

  /* Game layout */
  .game-layout { max-width: 900px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }

  /* Scoreboard */
  .scoreboard { background: var(--bg-2); border: 1px solid var(--border); border-radius: 12px; overflow: hidden; }
  .table-wrap { overflow-x: auto; }

  table { width: 100%; border-collapse: collapse; font-size: 13px; }

  th {
    background: var(--bg-3);
    padding: 10px 14px;
    text-align: center;
    font-size: 10px;
    letter-spacing: 2px;
    text-transform: uppercase;
    color: var(--text-2);
    border-bottom: 1px solid var(--border);
    white-space: nowrap;
  }
  th.name-col { text-align: left; }

  th.name-col, td.name-col { position: sticky; left: 0; z-index: 2; }
  th.name-col              { background: var(--bg-3); }
  td.name-col              { background: var(--bg-2); }
  tr:hover td.name-col     { background: var(--bg-4); }
  .total-row td.name-col   { background: var(--bg-3) !important; }

  td {
    padding: 9px 14px;
    text-align: center;
    border-bottom: 1px solid var(--bg-4);
    white-space: nowrap;
  }
  td.name-col { text-align: left; font-size: 14px; color: var(--text); }
  tr:last-child td { border-bottom: none; }

  .score-cell  { color: var(--score); }
  .round-score { color: var(--text); }
  .bust-cell   { color: var(--danger); font-size: 11px; letter-spacing: 1px; }
  .flip7-cell  { color: var(--accent); font-size: 11px; letter-spacing: 1px; }

  .total-row td {
    background: var(--bg-3);
    font-family: 'Bebas Neue', sans-serif;
    font-size: 18px;
    letter-spacing: 1px;
    color: var(--accent);
    border-top: 2px solid var(--border);
  }
  .total-row td.name-col { color: var(--text); font-size: 13px; letter-spacing: 2px; text-transform: uppercase; }
  .total-score { font-family: 'Bebas Neue', sans-serif; font-size: 20px; color: var(--accent); letter-spacing: 1px; }

  .rank-badge { display: inline-block; width: 20px; height: 20px; border-radius: 50%; font-size: 11px; line-height: 20px; text-align: center; margin-right: 6px; font-family: 'Bebas Neue', sans-serif; }
  .rank-1 { background: #f5c842; color: #0d1117; }
  .rank-2 { background: #aaa;    color: #0d1117; }
  .rank-3 { background: #cd7f32; color: #0d1117; }

  /* Round entry */
  .round-panel { background: var(--bg-2); border: 1px solid var(--border); border-radius: 12px; padding: 24px; }

  .round-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 20px;
  }

  .round-title { font-family: 'Bebas Neue', sans-serif; font-size: 22px; letter-spacing: 3px; color: var(--text); flex-shrink: 0; }
  .round-title span { color: var(--accent); }

  .players-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 14px; margin-bottom: 20px; }

  .player-entry { background: var(--bg); border: 1px solid var(--border); border-radius: 8px; padding: 14px; transition: border-color 0.15s; }
  .player-entry.is-bust  { border-color: var(--danger-c); }
  .player-entry.is-flip7 { border-color: var(--flip-b); }

  .player-entry-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
  .player-entry-name   { font-size: 12px; letter-spacing: 2px; text-transform: uppercase; color: var(--text-2); }

  .btn-camera {
    background: none;
    border: 1px solid var(--border);
    border-radius: 4px;
    color: var(--text-3);
    font-size: 15px;
    padding: 2px 8px;
    cursor: pointer;
    line-height: 1.5;
    transition: all 0.15s;
  }
  .btn-camera:hover    { border-color: var(--border-2); color: var(--text-2); background: var(--bg-3); }
  .btn-camera:disabled { opacity: 0.25; cursor: not-allowed; }

  .input-label-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
  .input-hint      { font-size: 10px; color: var(--text-3); }

  .btn-pick {
    background: none;
    border: 1px solid var(--border);
    border-radius: 4px;
    color: var(--text-3);
    font-size: 13px;
    padding: 1px 7px;
    cursor: pointer;
    line-height: 1.5;
    transition: all 0.15s;
  }
  .btn-pick:hover    { border-color: var(--border-2); color: var(--text-2); background: var(--bg-3); }
  .btn-pick:disabled { opacity: 0.25; cursor: not-allowed; }

  .cards-input { width: 100%; margin-bottom: 4px; }
  .input-error { font-size: 10px; color: var(--danger); margin-bottom: 8px; min-height: 14px; }

  .toggle-row { display: flex; gap: 6px; }
  .toggle-btn {
    flex: 1;
    padding: 5px;
    font-size: 11px;
    letter-spacing: 1px;
    border-radius: 4px;
    cursor: pointer;
    border: 1px solid var(--border);
    background: none;
    color: var(--text-2);
    font-family: 'DM Mono', monospace;
    transition: all 0.15s;
  }
  .toggle-btn.active-bust  { background: var(--danger-b); border-color: var(--danger); color: var(--danger); }
  .toggle-btn.active-flip7 { background: var(--flip-a);   border-color: var(--accent); color: var(--accent); }
  .toggle-btn.active-x2   { background: var(--info-a);   border-color: var(--info);   color: var(--info);   }

  .preview-score       { font-family: 'Bebas Neue', sans-serif; font-size: 28px; color: var(--accent); margin-top: 8px; letter-spacing: 2px; }
  .preview-score.bust  { color: var(--danger); }
  .preview-detail      { font-size: 11px; color: var(--text-2); margin-left: 8px; font-family: 'DM Mono', monospace; }

  @keyframes spin { to { transform: rotate(360deg); } }

  .scan-overlay { position: fixed; inset: 0; background: var(--bg-scan); display: flex; flex-direction: column; align-items: center; justify-content: center; z-index: 200; gap: 20px; }
  .scan-spinner { width: 44px; height: 44px; border: 3px solid var(--border); border-top-color: var(--accent); border-radius: 50%; animation: spin 0.7s linear infinite; }
  .scan-label   { font-family: 'Bebas Neue', sans-serif; font-size: 18px; letter-spacing: 4px; color: var(--accent); }

  .action-row     { display: flex; flex-direction: column; gap: 10px; margin-top: 4px; }
  .action-add-row { display: flex; gap: 10px; }
  .btn-confirm    { width: 100%; padding: 13px; font-size: 14px; letter-spacing: 2px; }

  .divider       { border: none; border-top: 1px solid var(--bg-3); margin: 16px 0; }
  .empty-state   { text-align: center; padding: 40px; color: var(--text-4); font-size: 13px; letter-spacing: 1px; }

  /* Dialogs */
  .dialog-overlay { position: fixed; inset: 0; background: var(--bg-overlay); display: flex; align-items: center; justify-content: center; z-index: 100; padding: 24px; }
  .dialog-box     { background: var(--bg-2); border: 1px solid var(--border); border-radius: 12px; padding: 28px 24px 20px; max-width: 320px; width: 100%; }
  .dialog-title   { font-family: 'Bebas Neue', sans-serif; font-size: 18px; letter-spacing: 3px; color: var(--accent); margin-bottom: 12px; }
  .dialog-desc    { font-size: 11px; color: var(--text-3); margin-bottom: 14px; }
  .dialog-msg     { font-size: 14px; color: var(--text); line-height: 1.6; margin-bottom: 20px; }
  .dialog-btns    { display: flex; gap: 10px; justify-content: flex-end; }

  /* Card picker bottom sheet */
  .picker-overlay {
    position: fixed;
    inset: 0;
    background: var(--bg-overlay);
    display: flex;
    align-items: flex-end;
    justify-content: center;
    z-index: 150;
  }

  .picker-sheet {
    background: var(--bg-2);
    border: 1px solid var(--border);
    border-top-left-radius: 18px;
    border-top-right-radius: 18px;
    padding: 12px 16px 36px;
    width: 100%;
    max-width: 500px;
  }

  .picker-handle {
    width: 36px;
    height: 4px;
    background: var(--border);
    border-radius: 2px;
    margin: 0 auto 14px;
  }

  .picker-title {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 17px;
    letter-spacing: 3px;
    color: var(--accent);
    text-align: center;
    margin-bottom: 14px;
  }

  .picker-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
    margin-bottom: 14px;
  }

  .picker-grid-5 { grid-template-columns: repeat(5, 1fr); }

  .card-btn {
    aspect-ratio: 1;
    border: 2px solid var(--border);
    border-radius: 10px;
    background: var(--bg);
    color: var(--text);
    font-family: 'Bebas Neue', sans-serif;
    font-size: 22px;
    letter-spacing: 1px;
    cursor: pointer;
    transition: all 0.12s;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    -webkit-tap-highlight-color: transparent;
  }
  .card-btn:active  { transform: scale(0.9); }
  .card-btn.selected {
    background: var(--accent);
    border-color: var(--accent);
    color: var(--accent-on);
  }

  .picker-actions { display: flex; gap: 10px; }
`;

function parseCards(str) {
  return str.split(/[\s,]+/).map(s => parseInt(s)).filter(n => !isNaN(n));
}

const PLUS_ALLOWED = new Set([2, 4, 6, 8, 10]);

function validateNumCards(str, t) {
  if (!str.trim()) return null;
  const bad = str.trim().split(/[\s,]+/).filter(s => s !== '').filter(tok => {
    const n = parseInt(tok); return isNaN(n) || n < 0 || n > 12;
  });
  return bad.length ? t.invalidNum(bad.join(', ')) : null;
}

function validatePlusCards(str, t) {
  if (!str.trim()) return null;
  const bad = str.trim().split(/[\s,]+/).filter(s => s !== '').filter(tok => {
    const n = parseInt(tok); return isNaN(n) || !PLUS_ALLOWED.has(n);
  });
  return bad.length ? t.invalidPlus(bad.join(', ')) : null;
}

function calcScore(cards, bust, flip7, plusCards, multiplier) {
  if (bust) return 0;
  const base = (cards.reduce((a,b)=>a+b,0) + plusCards.reduce((a,b)=>a+b,0)) * (multiplier ? 2 : 1);
  return base + (flip7 ? FLIP7_BONUS : 0);
}

function getRank(players, totals) {
  const sorted = [...players].sort((a, b) => (totals[b]||0) - (totals[a]||0));
  return (name) => sorted.indexOf(name) + 1;
}

function resizeImage(file, maxPx = 1024) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (re) => {
      const img = new Image();
      img.onload = () => {
        const scale = Math.min(1, maxPx / Math.max(img.width, img.height));
        const canvas = document.createElement('canvas');
        canvas.width  = Math.round(img.width  * scale);
        canvas.height = Math.round(img.height * scale);
        canvas.getContext('2d').drawImage(img, 0, 0, canvas.width, canvas.height);
        resolve(canvas.toDataURL('image/jpeg', 0.78));
      };
      img.onerror = reject;
      img.src = re.target.result;
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

export default function Flip7Tracker() {
  const [lang,   setLang]   = useState(() => localStorage.getItem('flip7_lang')  || 'hu');
  const [theme,  setTheme]  = useState(() => localStorage.getItem('flip7_theme') || 'dark');
  const [phase,  setPhase]  = useState("setup");
  const [playerName, setPlayerName] = useState("");
  const [players, setPlayers] = useState([]);
  const [rounds,  setRounds]  = useState([]);
  const [entry,   setEntry]   = useState({});
  const [newPlayerName, setNewPlayerName] = useState("");
  const [dialog,       setDialog]       = useState(null);
  const [cameraLoading, setCameraLoading] = useState(false);
  const [cameraError,   setCameraError]   = useState(null);
  const [apiKeyModal, setApiKeyModal] = useState(false);
  const [apiKeyDraft, setApiKeyDraft] = useState("");
  // cardPicker: null | { player, type: 'num'|'plus', selected: number[] }
  const [cardPicker, setCardPicker] = useState(null);

  const fileInputRef    = useRef(null);
  const pendingPlayerRef = useRef(null);
  const playerInputRef  = useRef(null);

  const t = TRANSLATIONS[lang];

  useEffect(() => { localStorage.setItem('flip7_lang', lang); }, [lang]);
  useEffect(() => {
    localStorage.setItem('flip7_theme', theme);
    document.body.className = theme === 'light' ? 'light-mode' : '';
  }, [theme]);

  // ── Card picker ──────────────────────────────────────────────
  const openPicker = (player, type) => {
    const e = entry[player] || {};
    const current = [...new Set(parseCards(type === 'num' ? e.cards : e.plusCards))];
    setCardPicker({ player, type, selected: current });
  };

  const toggleCard = (value) => {
    setCardPicker(prev => {
      const already = prev.selected.includes(value);
      return {
        ...prev,
        selected: already
          ? prev.selected.filter(v => v !== value)
          : [...prev.selected, value],
      };
    });
  };

  const confirmPicker = () => {
    const { player, type, selected } = cardPicker;
    const sorted = [...selected].sort((a, b) => a - b);
    updateEntry(player, type === 'num' ? 'cards' : 'plusCards', sorted.join(' '));
    setCardPicker(null);
  };

  // ── Camera ───────────────────────────────────────────────────
  const showConfirm = (msg, onConfirm) => setDialog({ msg, onConfirm });
  const closeDialog = () => setDialog(null);

  const handleCameraClick = (playerName) => {
    const key = localStorage.getItem('flip7_claude_key');
    if (!key) { pendingPlayerRef.current = playerName; setApiKeyModal(true); return; }
    pendingPlayerRef.current = playerName;
    fileInputRef.current.value = '';
    fileInputRef.current.click();
  };

  const saveApiKey = () => {
    const k = apiKeyDraft.trim();
    if (!k) return;
    localStorage.setItem('flip7_claude_key', k);
    setApiKeyDraft(''); setApiKeyModal(false);
    setTimeout(() => { fileInputRef.current.value = ''; fileInputRef.current.click(); }, 100);
  };

  const handleFileChange = async (ev) => {
    const file = ev.target.files[0];
    if (!file) return;
    ev.target.value = '';
    const player = pendingPlayerRef.current;
    const apiKey = localStorage.getItem('flip7_claude_key');
    setCameraLoading(true);
    try {
      const dataUrl = await resizeImage(file);
      const comma = dataUrl.indexOf(',');
      const mimeType = dataUrl.slice(0, comma).match(/:(.*?);/)[1];
      const base64Data = dataUrl.slice(comma + 1);
      const requestBody = JSON.stringify({
        model: 'claude-haiku-4-5-20251001', max_tokens: 300,
        messages: [{ role: 'user', content: [
          { type: 'image', source: { type: 'base64', media_type: mimeType, data: base64Data } },
          { type: 'text', text: 'Flip 7 kártyajáték lapokat azonosítasz a képen. Számkártyák: 0-12 közötti egész számok. Plusz kártyák: +2, +4, +6, +8 vagy +10 (+ jellel jelölve). Válaszolj CSAK JSON-ban: {"numberCards":[egész számok 0-12],"plusCards":[2/4/6/8/10 értékek]}. Ha nem látszanak lapok: {"error":"no_cards_detected"}' }
        ]}]
      });
      window.__onClaudeResult = (raw) => {
        setCameraLoading(false);
        try {
          const resp = JSON.parse(raw);
          if (resp.error && typeof resp.error === 'object') { setCameraError('api_error:' + (resp.error.message || '?')); return; }
          const text = resp.content?.[0]?.text ?? '';
          const m = text.match(/\{[\s\S]*\}/);
          if (!m) { setCameraError('no_cards_detected'); return; }
          const cards = JSON.parse(m[0]);
          if (cards.error === 'no_cards_detected') { setCameraError('no_cards_detected'); return; }
          if (cards.numberCards?.length) updateEntry(player, 'cards',     cards.numberCards.join(' '));
          if (cards.plusCards?.length)   updateEntry(player, 'plusCards', cards.plusCards.join(' '));
          if (!cards.numberCards?.length && !cards.plusCards?.length) setCameraError('no_cards_detected');
        } catch { setCameraError('processing_error'); }
      };
      if (window.Android) { window.Android.analyzeImage(apiKey, requestBody); }
      else { setCameraLoading(false); setCameraError('android_unavail'); }
    } catch { setCameraLoading(false); setCameraError('processing_error'); }
  };

  // ── Setup ────────────────────────────────────────────────────
  const addPlayerInGame = () => {
    const n = newPlayerName.trim();
    if (!n || players.includes(n)) return;
    setPlayers(prev => [...prev, n]);
    setEntry(prev => ({ ...prev, [n]: { cards: "", plusCards: "", bust: false, flip7: false, multiplier: false } }));
    setNewPlayerName("");
  };

  const addPlayer = () => {
    const n = playerName.trim();
    if (n && !players.includes(n)) {
      setPlayers([...players, n]);
      setPlayerName("");
      setTimeout(() => playerInputRef.current?.focus(), 0);
    }
  };

  const removePlayer = (n) => setPlayers(players.filter(p => p !== n));

  const startGame = () => {
    const pending = playerName.trim();
    const all = (pending && !players.includes(pending)) ? [...players, pending] : [...players];
    if (all.length < 2) return;
    const init = {};
    all.forEach(p => { init[p] = { cards: "", plusCards: "", bust: false, flip7: false, multiplier: false }; });
    setPlayers(all);
    setPlayerName("");
    setEntry(init);
    setPhase("game");
  };

  // ── Round entry ──────────────────────────────────────────────
  const updateEntry = (name, field, value) =>
    setEntry(prev => ({ ...prev, [name]: { ...prev[name], [field]: value } }));

  const toggleBust = (name) => {
    const cur = entry[name];
    if (!cur.bust) updateEntry(name, "bust", true); else updateEntry(name, "bust", false);
    if (!entry[name].bust) updateEntry(name, "flip7", false);
  };

  const toggleFlip7 = (name) => {
    if (!entry[name].flip7) { updateEntry(name, "flip7", true); updateEntry(name, "bust", false); }
    else updateEntry(name, "flip7", false);
  };

  const confirmRound = () => {
    const roundResult = {};
    players.forEach(p => {
      const e = entry[p];
      roundResult[p] = {
        score: calcScore(parseCards(e.cards), e.bust, e.flip7, parseCards(e.plusCards), e.multiplier),
        bust: e.bust, flip7: e.flip7, multiplier: e.multiplier,
      };
    });
    setRounds([...rounds, roundResult]);
    const init = {};
    players.forEach(p => { init[p] = { cards: "", plusCards: "", bust: false, flip7: false, multiplier: false }; });
    setEntry(init);
  };

  const undoRound = () => setRounds(rounds.slice(0, -1));

  const resetAll = () => showConfirm(t.resetConfirm, () => {
    setPlayers([]); setRounds([]); setEntry({}); setPlayerName(""); setPhase("setup");
  });

  const newGame = () => showConfirm(t.newGameConfirm, () => {
    setRounds([]);
    const init = {};
    players.forEach(p => { init[p] = { cards: "", plusCards: "", bust: false, flip7: false, multiplier: false }; });
    setEntry(init);
  });

  // ── Derived ──────────────────────────────────────────────────
  const totals = {};
  players.forEach(p => { totals[p] = rounds.reduce((sum, r) => sum + (r[p]?.score || 0), 0); });

  const getPreview = (name) => {
    if (!entry[name]) return null;
    const e = entry[name];
    if (e.bust) return { score: 0 };
    return { score: calcScore(parseCards(e.cards), false, e.flip7, parseCards(e.plusCards), e.multiplier) };
  };

  const rankOf = getRank(players, totals);

  const cameraErrorMsg =
      cameraError === 'no_cards_detected'      ? t.noCardsDetected
    : cameraError === 'processing_error'       ? t.processingError
    : cameraError === 'android_unavail'        ? t.androidUnavail
    : cameraError?.startsWith('api_error:')    ? t.apiError(cameraError.slice(10))
    : cameraError;

  // ── Render ───────────────────────────────────────────────────
  return (
    <>
      <style>{styles}</style>
      <div className="app">

        {/* Header */}
        <div className="header">
          <div className="logo">FLIP<span> 7</span></div>
          <div className="subtitle">Score Tracker</div>
          <div className="header-controls">
            <button className={`ctrl-btn${lang==='hu'?' active':''}`} onClick={() => setLang('hu')}>HU</button>
            <button className={`ctrl-btn${lang==='en'?' active':''}`} onClick={() => setLang('en')}>EN</button>
            <div className="ctrl-divider" />
            <button className={`ctrl-btn${theme==='dark'?' active':''}`}  onClick={() => setTheme('dark')}>☾</button>
            <button className={`ctrl-btn${theme==='light'?' active':''}`} onClick={() => setTheme('light')}>☀</button>
          </div>
        </div>

        {/* Setup */}
        {phase === "setup" && (
          <div className="setup-card">
            <div className="setup-title">{t.setupTitle}</div>
            {players.map(p => (
              <div key={p} className="player-row">
                <div className="input" style={{display:'flex',alignItems:'center'}}>{p}</div>
                <button className="btn btn-danger btn-sm" onClick={() => removePlayer(p)}>✕</button>
              </div>
            ))}
            <div className="player-row">
              <input ref={playerInputRef} className="input"
                placeholder={t.playerNamePlaceholder} value={playerName}
                onChange={e => setPlayerName(e.target.value)}
                onKeyDown={e => e.key==="Enter" && addPlayer()} />
              <button className="btn" onClick={addPlayer}>{t.addPlayer}</button>
            </div>
            <hr className="divider" />
            {(() => {
                const p = playerName.trim();
                const total = players.length + (p && !players.includes(p) ? 1 : 0);
                return (
                  <button type="button" className="btn btn-primary" style={{width:'100%',padding:'12px'}}
                    disabled={total < 2} onClick={startGame}>{t.startGame}</button>
                );
              })()}
          </div>
        )}

        {/* Game */}
        {phase === "game" && (
          <div className="game-layout">
            <input ref={fileInputRef} type="file" accept="image/*" capture="environment"
              style={{display:'none'}} onChange={handleFileChange} />

            {/* Scoreboard */}
            <div className="scoreboard">
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th className="name-col">{t.playerCol}</th>
                      {rounds.map((_, i) => <th key={i}>{t.roundN(i+1)}</th>)}
                      <th>{t.total}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {[...players].sort((a,b) => totals[b]-totals[a]).map(p => {
                      const rank = rankOf(p);
                      return (
                        <tr key={p}>
                          <td className="name-col">
                            {rank<=3 && rounds.length>0 && <span className={`rank-badge rank-${rank}`}>{rank}</span>}
                            {p}
                          </td>
                          {rounds.map((r,i) => {
                            const rd = r[p];
                            return (
                              <td key={i} className="round-score">
                                {rd?.bust  ? <span className="bust-cell">BUST</span>
                                : rd?.flip7 ? <span className="flip7-cell">★ {rd.score}</span>
                                :             <span className="score-cell">{rd?.score ?? "–"}</span>}
                              </td>
                            );
                          })}
                          <td><span className="total-score">{totals[p]}</span></td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
              {rounds.length===0 && <div className="empty-state">{t.noRounds}</div>}
            </div>

            {/* Round entry */}
            <div className="round-panel">
              <div className="round-header">
                <div className="round-title">
                  {(() => { const [pre,hi,post]=t.roundTitleParts(rounds.length+1); return <>{pre}<span>{hi}</span>{post}</>; })()}
                </div>
                <div style={{display:'flex',gap:8}}>
                  {rounds.length>0 && <button className="btn btn-sm" onClick={undoRound}>{t.undo}</button>}
                  <button className="btn btn-sm" onClick={newGame}>{t.newGame}</button>
                  <button className="btn btn-sm btn-danger" onClick={resetAll}>{t.reset}</button>
                </div>
              </div>

              <div className="players-grid">
                {players.map(p => {
                  const e = entry[p] || { cards:"", plusCards:"", bust:false, flip7:false, multiplier:false };
                  const preview = getPreview(p);
                  const numErr  = e.bust ? null : validateNumCards(e.cards, t);
                  const plusErr = e.bust ? null : validatePlusCards(e.plusCards, t);
                  return (
                    <div key={p} className={`player-entry${e.bust?" is-bust":e.flip7?" is-flip7":""}`}>
                      <div className="player-entry-header">
                        <div className="player-entry-name">{p}</div>
                        <button className="btn-camera" disabled={e.bust||cameraLoading} onClick={() => handleCameraClick(p)}>📷</button>
                      </div>

                      {/* Number cards */}
                      <div className="input-label-row">
                        <span className="input-hint">{t.numCardsLabel}</span>
                        <button className="btn-pick" disabled={e.bust} onClick={() => openPicker(p,'num')}>⊞</button>
                      </div>
                      <input className={`input cards-input${numErr?" invalid":""}`}
                        placeholder={t.numCardsPlaceholder} value={e.cards} disabled={e.bust}
                        onChange={ev => updateEntry(p,"cards",ev.target.value)} />
                      <div className="input-error">{numErr}</div>

                      {/* Plus cards */}
                      <div className="input-label-row">
                        <span className="input-hint">{t.plusCardsLabel}</span>
                        <button className="btn-pick" disabled={e.bust} onClick={() => openPicker(p,'plus')}>⊞</button>
                      </div>
                      <input className={`input cards-input${plusErr?" invalid":""}`}
                        placeholder={t.plusCardsPlaceholder} value={e.plusCards} disabled={e.bust}
                        onChange={ev => updateEntry(p,"plusCards",ev.target.value)} />
                      <div className="input-error">{plusErr}</div>

                      <div className="toggle-row">
                        <button className={`toggle-btn${e.bust?" active-bust":""}`}    onClick={() => toggleBust(p)}>BUST</button>
                        <button className={`toggle-btn${e.flip7?" active-flip7":""}`}  onClick={() => toggleFlip7(p)}>FLIP 7 ★</button>
                        <button className={`toggle-btn${e.multiplier?" active-x2":""}`} disabled={e.bust}
                          onClick={() => updateEntry(p,"multiplier",!e.multiplier)}>×2</button>
                      </div>

                      {preview && (
                        <div className={`preview-score${e.bust?" bust":""}`}>
                          {e.bust ? "0" : preview.score}
                          {!e.bust && (e.flip7||e.multiplier) && (
                            <span className="preview-detail">
                              {[e.multiplier&&"×2", e.flip7&&`+${FLIP7_BONUS}`].filter(Boolean).join(" ")}
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>

              {(() => {
                const hasErrors = players.some(p => {
                  const e = entry[p]||{};
                  return !e.bust && (validateNumCards(e.cards,t) || validatePlusCards(e.plusCards,t));
                });
                return (
                  <div className="action-row">
                    <div className="action-add-row">
                      <input className="input" style={{flex:1}} placeholder={t.newPlayerPlaceholder}
                        value={newPlayerName} onChange={e=>setNewPlayerName(e.target.value)}
                        onKeyDown={e=>e.key==="Enter"&&addPlayerInGame()} />
                      <button className="btn" onClick={addPlayerInGame}>{t.addInGame}</button>
                    </div>
                    <button className="btn btn-primary btn-confirm" onClick={confirmRound}
                      disabled={hasErrors} style={hasErrors?{opacity:0.4,cursor:'not-allowed'}:{}}>
                      {t.confirmRound}
                    </button>
                  </div>
                );
              })()}
            </div>
          </div>
        )}
      </div>

      {/* Card picker bottom sheet */}
      {cardPicker && (
        <div className="picker-overlay" onClick={() => setCardPicker(null)}>
          <div className="picker-sheet" onClick={e => e.stopPropagation()}>
            <div className="picker-handle" />
            <div className="picker-title">
              {cardPicker.type==='num' ? t.pickerNumTitle : t.pickerPlusTitle}
            </div>
            <div className={`picker-grid${cardPicker.type==='plus'?' picker-grid-5':''}`}>
              {(cardPicker.type==='num' ? NUM_CARDS : PLUS_CARDS).map(v => (
                <button
                  key={v}
                  className={`card-btn${cardPicker.selected.includes(v)?' selected':''}`}
                  onClick={() => toggleCard(v)}
                >
                  {cardPicker.type==='plus' ? `+${v}` : v}
                </button>
              ))}
            </div>
            <div className="picker-actions">
              <button className="btn" onClick={() => setCardPicker(prev=>({...prev,selected:[]}))}>
                {t.pickerClear}
              </button>
              <button className="btn btn-primary" style={{flex:1}} onClick={confirmPicker}>
                {t.pickerDone}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Confirm dialog */}
      {dialog && (
        <div className="dialog-overlay" onClick={closeDialog}>
          <div className="dialog-box" onClick={e=>e.stopPropagation()}>
            <div className="dialog-msg">{dialog.msg.split('\n').map((l,i)=><div key={i}>{l}</div>)}</div>
            <div className="dialog-btns">
              <button className="btn" onClick={closeDialog}>{t.cancel}</button>
              <button className="btn btn-danger" onClick={()=>{dialog.onConfirm();closeDialog();}}>{t.yes}</button>
            </div>
          </div>
        </div>
      )}

      {/* Camera loading */}
      {cameraLoading && (
        <div className="scan-overlay">
          <div className="scan-spinner" />
          <div className="scan-label">{t.scanning}</div>
        </div>
      )}

      {/* Camera error */}
      {cameraError && (
        <div className="dialog-overlay" onClick={()=>setCameraError(null)}>
          <div className="dialog-box" onClick={e=>e.stopPropagation()}>
            <div className="dialog-msg">{cameraErrorMsg}</div>
            <div className="dialog-btns">
              <button className="btn btn-primary" onClick={()=>setCameraError(null)}>{t.ok}</button>
            </div>
          </div>
        </div>
      )}

      {/* API key modal */}
      {apiKeyModal && (
        <div className="dialog-overlay" onClick={()=>setApiKeyModal(false)}>
          <div className="dialog-box" onClick={e=>e.stopPropagation()}>
            <div className="dialog-msg">
              <div className="dialog-title">{t.apiKeyTitle}</div>
              <div className="dialog-desc">{t.apiKeyDesc}</div>
              <input className="input" style={{width:'100%',fontSize:12}}
                placeholder={t.apiKeyPlaceholder} value={apiKeyDraft}
                onChange={e=>setApiKeyDraft(e.target.value)}
                onKeyDown={e=>e.key==='Enter'&&saveApiKey()} autoFocus />
            </div>
            <div className="dialog-btns">
              <button className="btn" onClick={()=>setApiKeyModal(false)}>{t.cancel}</button>
              <button className="btn btn-primary" onClick={saveApiKey} disabled={!apiKeyDraft.trim()}>{t.save}</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
