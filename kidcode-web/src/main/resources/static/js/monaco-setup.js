// monaco-setup.js
// Responsible for registering the KidCode language, creating the Monaco
// editor instance and providing validation helper.

import { KIDCODE_STORAGE_KEY, API_BASE } from './constants.js';
import { elements } from './ui.js';

export let editor = null;
let validationTimeout = null;

export function registerKidCodeLanguage() {
  monaco.languages.register({ id: 'kidcode' });

  monaco.languages.setMonarchTokensProvider('kidcode', {
    keywords: [
      'move','forward','turn','left','right','say','repeat','end','set','if','else','define','pen','up','down','home','color',
    ],
    tokenizer: {
      root: [
        [/[a-zA-Z_][\w_]*/, { cases: { '@keywords': 'keyword', '@default': 'identifier' } }],
        [/\d+/, 'number'],
        [/#.*$/, 'comment'],
        [/"([^"\\]|\\.)*$/, 'string.invalid'],
        [/"/, { token: 'string.quote', bracket: '@open', next: '@string' }],
      ],
      string: [
        [/[^\\"]+/, 'string'],
        [/\\./, 'string.escape.invalid'],
        [/"/, { token: 'string.quote', bracket: '@close', next: '@pop' }],
      ],
    },
  });
}

// Initialize Monaco editor; returns a Promise that resolves with the editor
export function initMonaco(validateCode, initializeExamples) {
  // Configure the loader path
  require.config({ paths: { vs: 'https://cdn.jsdelivr.net/npm/monaco-editor@0.34.1/min/vs' } });

  return new Promise((resolve) => {
    require(['vs/editor/editor.main'], function () {
      registerKidCodeLanguage();

      const savedCode = localStorage.getItem(KIDCODE_STORAGE_KEY);
      const defaultCode = [
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
        '',
        '    move forward length',
        '    turn right 60',
        '',
        '    # Get ready for the next line',
        '    set length = length + 2',
        '    set color_index = color_index + 1',
        '',
        '    # Reset color index to loop through the rainbow',
        '    if color_index == 6',
        '        set color_index = 0',
        '    end if',
        'end repeat',
      ].join('\n');

      editor = monaco.editor.create(elements.editorContainer, {
        value: savedCode !== null ? savedCode : defaultCode,
        language: 'kidcode',
        theme: 'vs-light',
        automaticLayout: true,
        fontSize: 14,
        minimap: { enabled: false },
      });

      // Wire examples dropdown and actions
      try { initializeExamples && initializeExamples(editor); } catch (e) { console.warn('examples init failed', e); }

      // Add a keybinding for Run
      editor.addAction({
        id: 'kidcode.run',
        label: 'Run KidCode (Ctrl/Cmd+Enter)',
        keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter],
        run: function () { elements.runButton.click(); return null; },
      });

      // Auto-save + debounce validation
      editor.onDidChangeModelContent(() => {
        localStorage.setItem(KIDCODE_STORAGE_KEY, editor.getValue());
        clearTimeout(validationTimeout);
        validationTimeout = setTimeout(() => validateCode && validateCode(editor.getValue()), 500);
      });

      // Run an initial validation
      validateCode && validateCode(editor.getValue());

      resolve(editor);
    });
  });
}

// Validation helper which sends the code to the server and sets Monaco markers
export async function validateCodeRequest(code) {
  try {
    const response = await fetch(`${API_BASE}/api/validate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code }),
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
    monaco.editor.setModelMarkers(editor.getModel(), 'kidcode', markers);
  } catch (error) {
    console.error('Validation request failed:', error);
  }
}
