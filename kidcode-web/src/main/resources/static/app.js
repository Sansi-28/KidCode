// --- DARK MODE TOGGLE ---
const darkModeToggle = document.getElementById('dark-mode-toggle');

function setDarkMode(enabled) {
    if (enabled) {
        document.body.classList.add('dark-mode');
        // Update Monaco editor theme if available
        if (typeof monaco !== 'undefined' && editor) {
            monaco.editor.setTheme('vs-dark');
        }
    } else {
        document.body.classList.remove('dark-mode');
        // Update Monaco editor theme if available
        if (typeof monaco !== 'undefined' && editor) {
            monaco.editor.setTheme('vs');
        }
    }
}

function updateToggleButton(isDarkMode) {
    if (darkModeToggle) {
        darkModeToggle.innerHTML = isDarkMode
            ? '<span class="button-icon"><i class="fa-solid fa-sun"></i></span> Light Mode'
            : '<span class="button-icon"><i class="fa-solid fa-moon"></i></span> Dark Mode';
        darkModeToggle.setAttribute('aria-pressed', isDarkMode.toString());
    }
}

// Load preference from localStorage with namespaced key
const darkPref = localStorage.getItem('kidcode.darkMode');
if (darkPref === 'true' || (darkPref === null && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
    setDarkMode(true);
} else {
    setDarkMode(false);
}

// Add event listener with null check
if (darkModeToggle) {
    darkModeToggle.addEventListener('click', () => {
        const enabled = !document.body.classList.contains('dark-mode');
        setDarkMode(enabled);
        localStorage.setItem('kidcode.darkMode', enabled);
        updateToggleButton(enabled);
    });
}

// Set initial button text/icon
updateToggleButton(document.body.classList.contains('dark-mode'));
// File: kidcode-web/src/main/resources/static/app.js

// --- 1. GET REFERENCES TO OUR HTML ELEMENTS ---
const runButton = document.getElementById('run-button');
const editorContainer = document.getElementById('editor-container');
const drawingCanvas = document.getElementById('drawing-canvas');
const outputArea = document.getElementById('output-area');
const ctx = drawingCanvas.getContext('2d');
const helpButton = document.getElementById('help-button');
const helpModal = document.getElementById('help-modal');
const closeButton = document.querySelector('.close-button');

// --- MONACO: Global variable to hold the editor instance ---
let editor;
let validationTimeout;

// --- MONACO: Function to define and register our custom language ---
function registerKidCodeLanguage() {
    monaco.languages.register({ id: 'kidcode' });

    monaco.languages.setMonarchTokensProvider('kidcode', {
        keywords: [
            'move', 'forward', 'turn', 'left', 'right', 'say', 'repeat', 'end',
            'set', 'if', 'else', 'define', 'pen', 'up', 'down', 'color'
        ],
        tokenizer: {
            root: [
                [/[a-zA-Z_][\w_]*/, {
                    cases: {
                        '@keywords': 'keyword',
                        '@default': 'identifier'
                    }
                }],
                [/\d+/, 'number'],
                [/#.*$/, 'comment'],
                [/"([^"\\]|\\.)*$/, 'string.invalid'],
                [/"/, { token: 'string.quote', bracket: '@open', next: '@string' }]
            ],
            string: [
                [/[^\\"]+/, 'string'],
                [/\\./, 'string.escape.invalid'],
                [/"/, { token: 'string.quote', bracket: '@close', next: '@pop' }]
            ]
        }
    });
}

// --- MONACO: Use the loader to configure and create the editor ---
require.config({ paths: { 'vs': 'https://cdn.jsdelivr.net/npm/monaco-editor@0.34.1/min/vs' }});
require(['vs/editor/editor.main'], function() {
    registerKidCodeLanguage();

        editor = monaco.editor.create(editorContainer, {
                    value: [
            '# Welcome to KidCode!',
            '# Run this code to see a rainbow spiral, then try changing it!',
            '',
            'set colors = ["red", "orange", "yellow", "green", "blue", "purple"]',
            'set length = 5',
            'set color_index = 0',
            '',
            '# Repeat many times to make a large spiral',
            'repeat 75',
            '    # Set the color from the list',
            '    color colors[color_index]',
            '    ',
            '    move forward length',
            '    turn right 60',
            '    ',
            '    # Get ready for the next line',
            '    set length = length + 2',
            '    set color_index = color_index + 1',
            '    ',
            '    # Reset color index to loop through the rainbow',
            '    if color_index == 6',
            '        set color_index = 0',
            '    end if',
            'end repeat'
        ].join('\n'),
            language: 'kidcode',
            theme: document.body.classList.contains('dark-mode') ? 'vs-dark' : 'vs-light',
            automaticLayout: true,
            fontSize: 14,
            minimap: { enabled: false }
        });

        // Add an editor action / keybinding so Ctrl/Cmd+Enter triggers the Run button
        editor.addAction({
            id: 'kidcode.run',
            label: 'Run KidCode (Ctrl/Cmd+Enter)',
            keybindings: [
                monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter
            ],
            precondition: null,
            keybindingContext: null,
            run: function(ed) {
                runButton.click();
                return null;
            }
        });

    // MONACO: Live validation
    editor.onDidChangeModelContent(() => {
        clearTimeout(validationTimeout);
        validationTimeout = setTimeout(validateCode, 500);
    });
    validateCode();
});

// --- 2. ADD EVENT LISTENER TO THE RUN BUTTON ---
runButton.addEventListener('click', async () => {
    const code = editor.getValue();
    clearCanvas();
    outputArea.textContent = '';
    try {
        const response = await fetch('/api/execute', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: code })
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const events = await response.json();
        renderEvents(events);
    } catch (error) {
        logToOutput(`Network or server error: ${error.message}`, 'error');
    }
});

// --- NEW: Function to handle validation ---
async function validateCode() {
    const code = editor.getValue();
    try {
        const response = await fetch('/api/validate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: code })
        });
        const errors = await response.json();
        const markers = errors.map(err => ({
            message: err.message,
            severity: monaco.MarkerSeverity.Error,
            startLineNumber: err.lineNumber,
            endLineNumber: err.lineNumber,
            startColumn: 1,
            endColumn: 100
        }));
        monaco.editor.setModelMarkers(editor.getModel(), 'kidcode', markers);
    } catch (error) {
        console.error("Validation request failed:", error);
    }
}

// --- 5. HELPER FUNCTIONS ---
function clearCanvas() {
    ctx.clearRect(0, 0, drawingCanvas.width, drawingCanvas.height);
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.strokeStyle = 'black';
    ctx.lineWidth = 2;
}

// Draw the classic pointer at (x, y) with direction (degrees) and color
function drawCody(x, y, direction, color) {
    ctx.save();
    ctx.translate(x, y);
    ctx.rotate(direction * Math.PI / 180);
    ctx.beginPath();
    ctx.moveTo(0, -18);   // Tip
    ctx.lineTo(10, 7);    // Bottom right
    ctx.lineTo(0, 0);     // Indented base center
    ctx.lineTo(-4, 7);    // Bottom left
    ctx.closePath();
    ctx.fillStyle = color;
    ctx.fill();
    ctx.strokeStyle = 'black';
    ctx.lineWidth = 1.5;
    ctx.stroke();
    ctx.restore();
}

function logToOutput(message, type = 'info') {
    const line = document.createElement('div');
    line.textContent = message;
    if (type === 'error') {
        line.style.color = 'red';
        line.style.fontWeight = 'bold';
    }
    outputArea.appendChild(line);
}

// Store lines and Cody state for redraw
let drawnLines = [];
let codyState = { x: 250, y: 250, direction: 0, color: 'blue' };

function renderEvents(events) {
    if (!events || events.length === 0) return;

    for (const event of events) {
        switch (event.type) {
            case 'ClearEvent':
                drawnLines = [];
                codyState = { x: 250, y: 250, direction: 0, color: 'blue' };
                break;
            case 'MoveEvent':
                if (event.isPenDown && (event.fromX !== event.toX || event.fromY !== event.toY)) {
                    drawnLines.push({
                        fromX: event.fromX, fromY: event.fromY,
                        toX: event.toX, toY: event.toY,
                        color: event.color
                    });
                }
                codyState = { x: event.toX, y: event.toY, direction: event.newDirection, color: event.color };
                break;
            case 'SayEvent':
                logToOutput(`Cody says: ${event.message}`);
                break;
            case 'ErrorEvent':
                logToOutput(`ERROR: ${event.errorMessage}`, 'error');
                break;
        }
    }
    redrawCanvas();
}

function redrawCanvas() {
    ctx.clearRect(0, 0, drawingCanvas.width, drawingCanvas.height);
    drawnLines.forEach(line => {
        ctx.beginPath();
        ctx.moveTo(line.fromX, line.fromY);
        ctx.lineTo(line.toX, line.toY);
        ctx.strokeStyle = line.color;
        ctx.lineWidth = 2;
        ctx.stroke();
    });
    drawCody(codyState.x, codyState.y, codyState.direction, codyState.color);
}

helpButton.addEventListener('click', () => {
    helpModal.classList.remove('hidden');
});

closeButton.addEventListener('click', () => {
    helpModal.classList.add('hidden');
});

window.addEventListener('click', (event) => {
    if (event.target === helpModal) {
        helpModal.classList.add('hidden');
    }
}); 