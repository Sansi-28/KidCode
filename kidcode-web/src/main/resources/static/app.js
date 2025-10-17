// File: kidcode-web/src/main/resources/static/app.js

// --- 1. GET REFERENCES TO OUR HTML ELEMENTS ---
const runButton = document.getElementById("run-button");
const editorContainer = document.getElementById("editor-container");
const drawingCanvas = document.getElementById("drawing-canvas");
const outputArea = document.getElementById("output-area");
const ctx = drawingCanvas.getContext("2d");
const helpButton = document.getElementById("help-button");
const helpModal = document.getElementById("help-modal");
const closeButton = document.querySelector(".close-button");
const downloadButton = document.getElementById("download-btn");
const themeToggle = document.getElementById("theme-toggle");

// --- Key for browser's local storage ---
const KIDCODE_STORAGE_KEY = "kidcode.savedCode";
const THEME_STORAGE_KEY = "kidcode.theme";

// --- MONACO: Global variable to hold the editor instance ---
let editor;
let validationTimeout;

// --- DARK MODE: Initialize theme on page load ---
function initializeTheme() {
  const savedTheme = localStorage.getItem(THEME_STORAGE_KEY);
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  const isDarkMode = savedTheme === 'dark' || (!savedTheme && prefersDark);
  
  if (isDarkMode) {
    document.body.classList.add('dark-mode');
    updateThemeIcon(true);
  }
}

// --- DARK MODE: Toggle theme ---
function toggleTheme() {
  const isDarkMode = document.body.classList.toggle('dark-mode');
  localStorage.setItem(THEME_STORAGE_KEY, isDarkMode ? 'dark' : 'light');
  updateThemeIcon(isDarkMode);
  
  // Update Monaco editor theme
  if (editor) {
    monaco.editor.setTheme(isDarkMode ? 'vs-dark' : 'vs-light');
  }
}

// --- DARK MODE: Update icon ---
function updateThemeIcon(isDarkMode) {
  const icon = themeToggle.querySelector('i');
  icon.className = isDarkMode ? 'fa-solid fa-sun' : 'fa-solid fa-moon';
}

// Initialize theme before anything else
initializeTheme();

// --- MONACO: Function to define and register our custom language ---
function registerKidCodeLanguage() {
  monaco.languages.register({ id: "kidcode" });

  monaco.languages.setMonarchTokensProvider("kidcode", {
    keywords: [
      "move",
      "forward",
      "turn",
      "left",
      "right",
      "say",
      "repeat",
      "end",
      "set",
      "if",
      "else",
      "define",
      "pen",
      "up",
      "down",
      "color",
    ],
    tokenizer: {
      root: [
        [
          /[a-zA-Z_][\w_]*/,
          {
            cases: {
              "@keywords": "keyword",
              "@default": "identifier",
            },
          },
        ],
        [/\d+/, "number"],
        [/#.*$/, "comment"],
        [/"([^"\\]|\\.)*$/, "string.invalid"],
        [/"/, { token: "string.quote", bracket: "@open", next: "@string" }],
      ],
      string: [
        [/[^\\"]+/, "string"],
        [/\\./, "string.escape.invalid"],
        [/"/, { token: "string.quote", bracket: "@close", next: "@pop" }],
      ],
    },
  });
}

// --- MONACO: Use the loader to configure and create the editor ---
require.config({
  paths: { vs: "https://cdn.jsdelivr.net/npm/monaco-editor@0.34.1/min/vs" },
});

require(["vs/editor/editor.main"], function () {
  registerKidCodeLanguage();

  const savedCode = localStorage.getItem(KIDCODE_STORAGE_KEY);
  const defaultCode = [
    "# Welcome to KidCode!",
    "# Run this code to see a rainbow spiral, then try changing it!",
    "",
    'set colors = ["red", "orange", "yellow", "green", "blue", "purple"]',
    "set length = 5",
    "set color_index = 0",
    "",
    "# Repeat many times to make a large spiral",
    "repeat 75",
    "    # Set the color from the list",
    "    color colors[color_index]",
    "",
    "    move forward length",
    "    turn right 60",
    "",
    "    # Get ready for the next line",
    "    set length = length + 2",
    "    set color_index = color_index + 1",
    "",
    "    # Reset color index to loop through the rainbow",
    "    if color_index == 6",
    "        set color_index = 0",
    "    end if",
    "end repeat",
  ].join("\n");

  // --- Create the Monaco Editor ---
  editor = monaco.editor.create(editorContainer, {
    value: savedCode !== null ? savedCode : defaultCode,
    language: "kidcode",
    theme: document.body.classList.contains('dark-mode') ? 'vs-dark' : 'vs-light',
    automaticLayout: true,
    fontSize: 14,
    minimap: { enabled: false },
  });

  // ✅ Safely initialize examples dropdown (non-blocking)
  initializeExamples();

  // Add an editor action / keybinding so Ctrl/Cmd+Enter triggers the Run button
  editor.addAction({
    id: "kidcode.run",
    label: "Run KidCode (Ctrl/Cmd+Enter)",
    keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter],
    run: function () {
      runButton.click();
      return null;
    },
  });

  // Monaco live validation and auto-saving
  editor.onDidChangeModelContent(() => {
    // Save the current code to local storage
    localStorage.setItem(KIDCODE_STORAGE_KEY, editor.getValue());

    // Debounce validation
    clearTimeout(validationTimeout);
    validationTimeout = setTimeout(validateCode, 500);
  });
  // Validate on initial load
  validateCode();
});

// --- Initialize example dropdown (gracefully degrades if examples.js missing) ---
function initializeExamples() {
  const selector = document.getElementById("exampleSelector");
  if (!selector) {
    console.error("Example selector element not found");
    return;
  }

  if (!window.examples) {
    console.warn("examples.js not loaded - example selector disabled");
    selector.disabled = true;
    selector.innerHTML = '<option value="">Examples unavailable</option>';
    return;
  }

  Object.keys(window.examples).forEach((exampleName) => {
    const option = document.createElement("option");
    option.value = exampleName;
    option.textContent = exampleName;
    selector.appendChild(option);
  });

  selector.addEventListener("change", () => {
    const selected = selector.value;
    if (window.examples[selected]) {
      editor.setValue(window.examples[selected]);
      logToOutput(`✅ Loaded example: ${selected}`);
    }
  });
}

// --- 2. ADD EVENT LISTENER TO THE RUN BUTTON ---
// --- 1️⃣ Event listener for Run button ---
runButton.addEventListener("click", async () => {
  const code = editor.getValue();

  // ✅ Always start with a fresh canvas before execution
  clearCanvas();
  outputArea.textContent = "";

  try {
    const response = await fetch("/api/execute", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const events = await response.json();
    renderEvents(events);
  } catch (error) {
    logToOutput(`Network or server error: ${error.message}`, "error");
  }
});

// --- NEW: Event listener for Download button ---
if (downloadButton) {
  downloadButton.addEventListener("click", () => {
    try {
      const imageURL = drawingCanvas.toDataURL("image/png");
      const link = document.createElement("a");
      link.href = imageURL;
      link.download = "CodyDrawing.png";
      link.click();
      logToOutput("Drawing exported as CodyDrawing.png");
    } catch (err) {
      logToOutput(`Failed to export drawing: ${err.message}`, "error");
    }
  });
}

// --- NEW: Function to handle validation ---
async function validateCode() {
  const code = editor.getValue();
  try {
    const response = await fetch("/api/validate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code: code }),
    });
    const errors = await response.json();
    const markers = errors.map((err) => ({
      message: err.message,
      severity: monaco.MarkerSeverity.Error,
      startLineNumber: err.lineNumber,
      endLineNumber: err.lineNumber,
      startColumn: 1,
      endColumn: 100,
    }));
    monaco.editor.setModelMarkers(editor.getModel(), "kidcode", markers);
  } catch (error) {
    console.error("Validation request failed:", error);
  }
}

// --- 5. HELPER FUNCTIONS ---
function clearCanvas() {
  ctx.clearRect(0, 0, drawingCanvas.width, drawingCanvas.height);
  ctx.setTransform(1, 0, 0, 1, 0, 0);
  ctx.strokeStyle = "black";
  ctx.lineWidth = 2;
}

// Draw the classic pointer at (x, y) with direction (degrees) and color
function drawCody(x, y, direction, color) {
  ctx.save();
  ctx.translate(x, y);
  ctx.rotate((direction * Math.PI) / 180);
  ctx.beginPath();
  ctx.moveTo(0, -18); // Tip
  ctx.lineTo(10, 7); // Bottom right
  ctx.lineTo(0, 0); // Indented base center
  ctx.lineTo(-4, 7); // Bottom left
  ctx.closePath();
  ctx.fillStyle = color;
  ctx.fill();
  ctx.strokeStyle = "black";
  ctx.lineWidth = 1.5;
  ctx.stroke();
  ctx.restore();
}

function logToOutput(message, type = "info") {
  const line = document.createElement("div");
  line.textContent = message;
  if (type === "error") {
    line.style.color = "red";
    line.style.fontWeight = "bold";
  }
  outputArea.appendChild(line);
}

// Store lines and Cody state for redraw
let drawnLines = [];
let codyState = { x: 250, y: 250, direction: 0, color: "blue" };

function renderEvents(events) {
  if (!events || events.length === 0) return;

  for (const event of events) {
    switch (event.type) {
      case "ClearEvent":
        drawnLines = [];
        codyState = { x: 250, y: 250, direction: 0, color: "blue" };
        break;
      case "MoveEvent":
        if (
          event.isPenDown &&
          (event.fromX !== event.toX || event.fromY !== event.toY)
        ) {
          drawnLines.push({
            fromX: event.fromX,
            fromY: event.fromY,
            toX: event.toX,
            toY: event.toY,
            color: event.color,
          });
        }
        codyState = {
          x: event.toX,
          y: event.toY,
          direction: event.newDirection,
          color: event.color,
        };
        break;
      case "SayEvent":
        logToOutput(`Cody says: ${event.message}`);
        break;
      case "ErrorEvent":
        logToOutput(`ERROR: ${event.errorMessage}`, "error");
        break;
    }
  }
  redrawCanvas();
}

function redrawCanvas() {
  ctx.clearRect(0, 0, drawingCanvas.width, drawingCanvas.height);
  drawnLines.forEach((line) => {
    ctx.beginPath();
    ctx.moveTo(line.fromX, line.fromY);
    ctx.lineTo(line.toX, line.toY);
    ctx.strokeStyle = line.color;
    ctx.lineWidth = 2;
    ctx.stroke();
  });
  drawCody(codyState.x, codyState.y, codyState.direction, codyState.color);
}

helpButton.addEventListener("click", () => {
  helpModal.classList.remove("hidden");
});

closeButton.addEventListener("click", () => {
  helpModal.classList.add("hidden");
});

window.addEventListener("click", (event) => {
  if (event.target === helpModal) {
    helpModal.classList.add("hidden");
  }
});

// --- DARK MODE: Event listener for theme toggle ---
if (themeToggle) {
  themeToggle.addEventListener("click", toggleTheme);
}

// --- DARK MODE: Listen to system theme changes ---
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
  const savedTheme = localStorage.getItem(THEME_STORAGE_KEY);
  // Only auto-switch if user hasn't set a preference
  if (!savedTheme) {
    if (e.matches) {
      document.body.classList.add('dark-mode');
      updateThemeIcon(true);
      if (editor) {
        monaco.editor.setTheme('vs-dark');
      }
    } else {
      document.body.classList.remove('dark-mode');
      updateThemeIcon(false);
      if (editor) {
        monaco.editor.setTheme('vs-light');
      }
    }
  }
});
