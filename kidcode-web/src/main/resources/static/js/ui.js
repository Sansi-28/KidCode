// ui.js
// Centralized DOM references and small UI helpers used by other modules.

// Grab elements once and export them so other modules can use the same
// references instead of re-querying the DOM.
export const elements = {
  runButton: document.getElementById("run-button"),
  clearButton: document.getElementById("clear-btn"),
  editorContainer: document.getElementById("editor-container"),
  drawingCanvas: document.getElementById("drawing-canvas"),
  outputArea: document.getElementById("output-area"),
  helpButton: document.getElementById("help-button"),
  helpModal: document.getElementById("help-modal"),
  closeButton: document.querySelector(".close-button"),
  downloadButton: document.getElementById("download-btn"),
  dragHandle: document.getElementById("drag-handle"),
  editorPanel: document.querySelector(".editor-panel"),
  visualPanel: document.querySelector(".visual-panel"),
  speedRange: document.getElementById("speedRange"),
  speedLabel: document.getElementById("speedLabel"),
  stepModal: document.getElementById("step-modal"),
  closeStepModalBtn: document.getElementById("close-step-modal"),
  exampleSelector: document.getElementById("exampleSelector"),
};

// Canvas 2D context (may be null if canvas missing)
export const ctx = elements.drawingCanvas ? elements.drawingCanvas.getContext("2d") : null;

// Simple helper to update speed label text and attach listener
export function wireSpeedControl(speedText) {
  if (!elements.speedRange || !elements.speedLabel) return;
  const updateSpeedUI = () => {
    elements.speedLabel.textContent = speedText[elements.speedRange.value] || "Normal";
  };
  elements.speedRange.addEventListener("input", updateSpeedUI);
  updateSpeedUI();
}
