// File: kidcode-web/src/main/resources/static/app.js

// --- 1. GET REFERENCES TO OUR HTML ELEMENTS ---
const runButton = document.getElementById("run-button");
const clearButton = document.getElementById("clear-btn");
const editorContainer = document.getElementById("editor-container");
const drawingCanvas = document.getElementById("drawing-canvas");
const outputArea = document.getElementById("output-area");
const ctx = drawingCanvas.getContext("2d");
const helpButton = document.getElementById("help-button");
const helpModal = document.getElementById("help-modal");
const closeButton = document.querySelector(".close-button");
const downloadButton = document.getElementById("download-btn");
const dragHandle = document.getElementById("drag-handle");
const editorPanel = document.querySelector(".editor-panel");
const visualPanel = document.querySelector(".visual-panel");
const themeSelect = document.getElementById("themeSelect");
const themeToggle = document.getElementById("themeToggle");

// --- Key for browser's local storage ---
const KIDCODE_STORAGE_KEY = "kidcode.savedCode";

const speedRange = document.getElementById("speedRange");
const speedLabel = document.getElementById("speedLabel");

const speedText = {
  "0": "Step-by-Step",
  "1": "Normal",
  "2": "Fast",
};

// --- Theme handling ---
const THEME_KEY = "kidcode.theme";

function getSavedTheme() {
  const saved = localStorage.getItem(THEME_KEY);
  if (saved === "dark" || saved === "light") return saved;
  return "light";
}

function applyTheme(theme) {
  const isDark = theme === "dark";
  document.body.classList.toggle("dark-theme", isDark);
  if (themeSelect) themeSelect.value = theme; // fallback support
  if (themeToggle) themeToggle.checked = isDark;
  try {
    if (typeof monaco !== "undefined") {
      monaco.editor.setTheme(isDark ? "vs-dark" : "vs-light");
    }
  } catch (_) {}
}

// Initialize theme ASAP to avoid flash
const initialTheme = getSavedTheme();
applyTheme(initialTheme);
// New toggle listener
if (themeToggle) {
  themeToggle.checked = initialTheme === "dark";
  themeToggle.addEventListener("change", () => {
    const newTheme = themeToggle.checked ? "dark" : "light";
    localStorage.setItem(THEME_KEY, newTheme);
    applyTheme(newTheme);
  });
}
// Fallback select listener (if present in DOM)
if (themeSelect) {
  themeSelect.value = initialTheme;
  themeSelect.addEventListener("change", () => {
    const newTheme = themeSelect.value === "dark" ? "dark" : "light";
    localStorage.setItem(THEME_KEY, newTheme);
    applyTheme(newTheme);
  });
}

function updateSpeedUI() {
  if (!speedRange || !speedLabel) return;
  speedLabel.textContent = speedText[speedRange.value] || "Normal";
}

if (speedRange) {
  speedRange.addEventListener("input", updateSpeedUI);
  updateSpeedUI();
}

// Step-by-step control using keyboard
let nextResolve = null;
let stepModalShown = false;


function waitForNextKey() {
  return new Promise((resolve) => {
    nextResolve = resolve;
  });
}


// Step modal elements
const stepModal = document.getElementById("step-modal");
const closeStepModalBtn = document.getElementById("close-step-modal");

if (closeStepModalBtn) {
  const closeModal = () => {
    stepModal.classList.add("hidden");
    stepModal.dispatchEvent(new Event("closed"));
  };

  closeStepModalBtn.addEventListener("click", closeModal);

  window.addEventListener("keydown", (e) => {
    if ((e.key === "Enter" || e.key === "Escape") && !stepModal.classList.contains("hidden")) {
      closeModal();
    }
  });
}


window.addEventListener("keydown", (e) => {
  const isInMonaco = document.activeElement?.closest('.monaco-editor'); // detect if typing in editor

  if (e.key === "Enter" && nextResolve && !isInMonaco) {
    e.preventDefault();
    nextResolve();
    nextResolve = null;
  }
});


// --- MONACO: Global variable to hold the editor instance ---
let editor;
let validationTimeout;

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
      "home",
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
    theme: (localStorage.getItem(THEME_KEY) === "dark") ? "vs-dark" : "vs-light",
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

let isExecuting = false;

// --- 2. ADD EVENT LISTENER TO THE RUN BUTTON ---
// --- 1️⃣ Event listener for Run button ---
runButton.addEventListener("click", async () => {
  if (isExecuting) {
    logToOutput("Execution already in progress. Please wait.", "error");
    return;
  }
  isExecuting = true;
  runButton.disabled = true;
  if (clearButton) clearButton.disabled = true;

  const code = editor.getValue();

  // Always start with a fresh canvas before execution
  clearCanvas();
  drawnLines = [];
  codyState = { x: 250, y: 250, direction: 0, color: "blue" };
  stepModalShown = false;
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
    await renderEvents(events);
  } catch (error) {
    logToOutput(`Network or server error: ${error.message}`, "error");
  }finally {
     isExecuting = false;
     nextResolve = null;
     runButton.disabled = false;
     if (clearButton) clearButton.disabled = false;
     editor.focus();
    }
});

clearButton.addEventListener("click", () => {
  try {
    clearCanvas(); // wipes Cody's canvas
    outputArea.textContent = ""; // clears the log area
    drawnLines = [];
    codyState = { x: 250, y: 250, direction: 0, color: "blue" };
    logToOutput("Canvas cleared");
  } catch (error) {
    logToOutput(`Error while clearing: ${error.message}`, "error");
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



async function renderEvents(events) {

  try {
    if (!events || events.length === 0) return;
    const initialSpeed = parseInt(speedRange.value, 10);
    if (initialSpeed === 0 && stepModal && !stepModalShown) {
      stepModalShown = true;
      stepModal.classList.remove("hidden");

      // Wait for the modal to be closed (event-driven)
      await new Promise((resolve) => {
        const onClose = () => {
          stepModal.removeEventListener("closed", onClose);
          resolve();
        };
        stepModal.addEventListener("closed", onClose, { once: true });
      });
    }

    for (const event of events) {
      const speed = parseInt(speedRange.value, 10);
      const delay = speed === 0 ? null : (speed === 1 ? 300 : 80);
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

      redrawCanvas();

 if (speed === 0) {
 // Show step modal if user switched to step mode mid-execution
       if (stepModal && !stepModalShown) {
            stepModalShown = true;
            stepModal.classList.remove("hidden");
            await new Promise((resolve) => {
              const onClose = () => {
                stepModal.removeEventListener("closed", onClose);
                resolve();
              };
              stepModal.addEventListener("closed", onClose, { once: true });
            });
          }
          await waitForNextKey(); // step mode
 } else {
   await new Promise((resolve) => setTimeout(resolve, delay));
 }
  }
  }
  catch (error) {
      logToOutput(`Rendering error: ${error.message}`, "error");
      throw error;
    }
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

// --- 6. PANEL RESIZING LOGIC ---
if (dragHandle) {
  dragHandle.addEventListener("mousedown", (e) => {
    // Prevent text selection while dragging
    e.preventDefault();

    // Add event listeners to the whole document to track mouse movement
    // even if the cursor leaves the handle.
    document.addEventListener("mousemove", handleMouseMove);
    document.addEventListener("mouseup", handleMouseUp);
  });
}

function handleMouseMove(e) {
  // Get the bounding rectangle of the main container (which is the parent of editorPanel)
  const containerRect = editorPanel.parentElement.getBoundingClientRect();
  const containerStart = containerRect.left;
  const containerWidth = containerRect.width;

  // Calculate the position of the mouse relative to the container
  const mouseX = e.clientX;

  const handleWidth = dragHandle.offsetWidth;

  // Calculate the desired new width for the editor panel in pixels
  // We subtract half the handle's width to center the divider on the cursor visually
  let desiredEditorWidthPx = mouseX - containerStart - handleWidth / 2;

  // Define the minimum width for each panel (matching style.css)
  const minPanelWidthPx = 535;

  // Clamp the desired editor width to respect min-width for both panels
  // 1. Editor panel cannot be smaller than minPanelWidthPx
  desiredEditorWidthPx = Math.max(desiredEditorWidthPx, minPanelWidthPx);
  // 2. Visual panel cannot be smaller than minPanelWidthPx
  //    This means editorWidthPx cannot be larger than (containerWidth - handleWidth - minPanelWidthPx)
  desiredEditorWidthPx = Math.min(desiredEditorWidthPx, containerWidth - handleWidth - minPanelWidthPx);

  // Convert the clamped pixel width back to a percentage
  const newEditorWidthPercent = (desiredEditorWidthPx / containerWidth) * 100;

  editorPanel.style.flexBasis = `${newEditorWidthPercent}%`;
  visualPanel.style.flexBasis = `${100 - newEditorWidthPercent}%`;
}

function handleMouseUp() {
  // Clean up by removing the event listeners when the mouse is released
  document.removeEventListener("mousemove", handleMouseMove);
  document.removeEventListener("mouseup", handleMouseUp);
}
