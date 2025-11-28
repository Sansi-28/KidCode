// renderer.js
// Handles drawing on the canvas, logging to the output area, and rendering
// event streams received from the KidCode execution API.

import { elements, ctx } from './ui.js';

// Store lines and Cody state for redraw
export let drawnLines = [];
export let codyState = { x: 250, y: 250, direction: 0, color: 'blue' };

// For step-mode control
let nextResolve = null;
export function waitForNextKey() {
  return new Promise((resolve) => { nextResolve = resolve; });
}

export function clearCanvas() {
  if (!ctx) return;
  ctx.clearRect(0, 0, elements.drawingCanvas.width, elements.drawingCanvas.height);
  ctx.setTransform(1, 0, 0, 1, 0, 0);
  ctx.strokeStyle = 'black';
  ctx.lineWidth = 2;
}

export function drawCody(x, y, direction, color) {
  if (!ctx) return;
  ctx.save();
  ctx.translate(x, y);
  ctx.rotate((direction * Math.PI) / 180);
  ctx.beginPath();
  ctx.moveTo(0, -18);
  ctx.lineTo(10, 7);
  ctx.lineTo(0, 0);
  ctx.lineTo(-4, 7);
  ctx.closePath();
  ctx.fillStyle = color;
  ctx.fill();
  ctx.strokeStyle = 'black';
  ctx.lineWidth = 1.5;
  ctx.stroke();
  ctx.restore();
}

export function logToOutput(message, type = 'info') {
  if (!elements.outputArea) return;
  const line = document.createElement('div');
  line.textContent = message;
  if (type === 'error') {
    line.style.color = 'red';
    line.style.fontWeight = 'bold';
  }
  elements.outputArea.appendChild(line);
}

export function redrawCanvas() {
  if (!ctx) return;
  ctx.clearRect(0, 0, elements.drawingCanvas.width, elements.drawingCanvas.height);
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

// Renders events returned from the server. Keeps behavior identical to
// the previous single-file implementation (delay/speed/step modes).
export async function renderEvents(events, getSpeedValue, stepModal) {
  try {
    if (!events || events.length === 0) return;
    const initialSpeed = parseInt(getSpeedValue(), 10);

    if (initialSpeed === 0 && stepModal && !stepModal.classList.contains('hidden')) {
      // If first-time step-mode, show the modal and wait for it to close.
      stepModal.classList.remove('hidden');
      await new Promise((resolve) => {
        const onClose = () => { stepModal.removeEventListener('closed', onClose); resolve(); };
        stepModal.addEventListener('closed', onClose, { once: true });
      });
    }

    for (const event of events) {
      const speed = parseInt(getSpeedValue(), 10);
      const delay = speed === 0 ? null : (speed === 1 ? 300 : 80);

      switch (event.type) {
        case 'ClearEvent':
          drawnLines = [];
          codyState = { x: 250, y: 250, direction: 0, color: 'blue' };
          break;
        case 'MoveEvent':
          if (event.isPenDown && (event.fromX !== event.toX || event.fromY !== event.toY)) {
            drawnLines.push({ fromX: event.fromX, fromY: event.fromY, toX: event.toX, toY: event.toY, color: event.color });
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

      redrawCanvas();

      if (speed === 0) {
        // Wait for next key (step mode)
        await waitForNextKey();
      } else {
        await new Promise((resolve) => setTimeout(resolve, delay));
      }
    }
  } catch (error) {
    logToOutput(`Rendering error: ${error.message}`, 'error');
    throw error;
  }
}

// Function used by the step modal to release the awaited next step
export function releaseNextStep() {
  if (nextResolve) {
    nextResolve();
    nextResolve = null;
  }
}
