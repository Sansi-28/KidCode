// app.js (module)
// Entrypoint that wires up the smaller modules: ui, monaco setup and renderer.

import { elements, wireSpeedControl } from './js/ui.js';
import { KIDCODE_STORAGE_KEY, API_BASE, speedText } from './js/constants.js';
import { initMonaco, validateCodeRequest } from './js/monaco-setup.js';
import { clearCanvas, drawnLines, codyState, redrawCanvas, renderEvents, logToOutput, releaseNextStep } from './js/renderer.js';

let editor = null;
let isExecuting = false;

// Wire speed UI
wireSpeedControl(speedText);

// Examples dropdown initializer
function initializeExamples(ed) {
  const selector = elements.exampleSelector;
  if (!selector) {
    console.error('Example selector element not found');
    return;
  }

  if (!window.examples) {
    console.warn('examples.js not loaded - example selector disabled');
    selector.disabled = true;
    selector.innerHTML = '<option value="">Examples unavailable</option>';
    return;
  }

  Object.keys(window.examples).forEach((exampleName) => {
    const option = document.createElement('option');
    option.value = exampleName;
    option.textContent = exampleName;
    selector.appendChild(option);
  });

  selector.addEventListener('change', () => {
    const selected = selector.value;
    if (window.examples[selected]) {
      ed.setValue(window.examples[selected]);
      logToOutput(`✅ Loaded example: ${selected}`);
    }
  });
}

// Hook up Monaco, then wire UI actions that depend on the editor
initMonaco(validateCodeRequest, initializeExamples).then((ed) => {
  editor = ed;

  // Step modal behavior (close button + keyboard)
  const stepModal = elements.stepModal;
  const closeStepModalBtn = elements.closeStepModalBtn;
  if (closeStepModalBtn && stepModal) {
    const closeModal = () => { stepModal.classList.add('hidden'); stepModal.dispatchEvent(new Event('closed')); };
    closeStepModalBtn.addEventListener('click', closeModal);
    window.addEventListener('keydown', (e) => {
      if ((e.key === 'Enter' || e.key === 'Escape') && !stepModal.classList.contains('hidden')) closeModal();
    });
  }

  // Global key handler for step mode (Enter to advance)
  window.addEventListener('keydown', (e) => {
    const isInMonaco = document.activeElement?.closest('.monaco-editor');
    if (e.key === 'Enter' && !isInMonaco) {
      // Release a single step if waiting
      releaseNextStep();
    }
  });

  // Run button
  if (elements.runButton) {
    elements.runButton.addEventListener('click', async () => {
      if (isExecuting) { logToOutput('Execution already in progress. Please wait.', 'error'); return; }
      isExecuting = true;
      elements.runButton.disabled = true;
      if (elements.clearButton) elements.clearButton.disabled = true;

      const code = editor.getValue();
      // reset canvas and state
      clearCanvas();
      // reset renderer state
      // (mutating module-level values from renderer module is fine)
      // eslint-disable-next-line no-unused-expressions
      drawnLines.length = 0;
      Object.assign(codyState, { x: 250, y: 250, direction: 0, color: 'blue' });
      elements.outputArea.textContent = '';

      try {
        const response = await fetch(`${API_BASE}/api/execute`, {
          method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ code }),
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        const events = await response.json();
        await renderEvents(events, () => elements.speedRange.value, elements.stepModal);
      } catch (err) {
        logToOutput(`Network or server error: ${err.message}`, 'error');
      } finally {
        isExecuting = false;
        elements.runButton.disabled = false;
        if (elements.clearButton) elements.clearButton.disabled = false;
        editor.focus();
      }
    });
  }

  // Clear button
  if (elements.clearButton) {
    elements.clearButton.addEventListener('click', () => {
      try {
        clearCanvas();
        elements.outputArea.textContent = '';
        drawnLines.length = 0;
        Object.assign(codyState, { x: 250, y: 250, direction: 0, color: 'blue' });
        logToOutput('Canvas cleared');
      } catch (error) {
        logToOutput(`Error while clearing: ${error.message}`, 'error');
      }
    });
  }

  // Download
  if (elements.downloadButton) {
    elements.downloadButton.addEventListener('click', () => {
      try {
        const imageURL = elements.drawingCanvas.toDataURL('image/png');
        const link = document.createElement('a');
        link.href = imageURL; link.download = 'CodyDrawing.png'; link.click();
        logToOutput('Drawing exported as CodyDrawing.png');
      } catch (err) { logToOutput(`Failed to export drawing: ${err.message}`, 'error'); }
    });
  }

  // Help modal
  if (elements.helpButton) elements.helpButton.addEventListener('click', () => elements.helpModal.classList.remove('hidden'));
  if (elements.closeButton) elements.closeButton.addEventListener('click', () => elements.helpModal.classList.add('hidden'));
  window.addEventListener('click', (event) => { if (event.target === elements.helpModal) elements.helpModal.classList.add('hidden'); });

  // Panel resizing
  if (elements.dragHandle) {
    elements.dragHandle.addEventListener('mousedown', (e) => { e.preventDefault(); document.addEventListener('mousemove', handleMouseMove); document.addEventListener('mouseup', handleMouseUp); });
  }
  function handleMouseMove(e) {
    const containerRect = elements.editorPanel.parentElement.getBoundingClientRect();
    const containerStart = containerRect.left; const containerWidth = containerRect.width; const mouseX = e.clientX; const handleWidth = elements.dragHandle.offsetWidth;
    let desiredEditorWidthPx = mouseX - containerStart - handleWidth / 2;
    const minPanelWidthPx = 535;
    desiredEditorWidthPx = Math.max(desiredEditorWidthPx, minPanelWidthPx);
    desiredEditorWidthPx = Math.min(desiredEditorWidthPx, containerWidth - handleWidth - minPanelWidthPx);
    const newEditorWidthPercent = (desiredEditorWidthPx / containerWidth) * 100;
    elements.editorPanel.style.flexBasis = `${newEditorWidthPercent}%`;
    elements.visualPanel.style.flexBasis = `${100 - newEditorWidthPercent}%`;
  }
  function handleMouseUp() { document.removeEventListener('mousemove', handleMouseMove); document.removeEventListener('mouseup', handleMouseUp); }
});
