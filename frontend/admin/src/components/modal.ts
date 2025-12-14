export function showModal(title: string, content: string, onConfirm?: () => void): void {
  const existingModal = document.querySelector('.modal-overlay');
  if (existingModal) existingModal.remove();

  const modal = document.createElement('div');
  modal.className = 'modal-overlay';
  modal.innerHTML = `
    <div class="modal">
      <h2>${title}</h2>
      <div class="modal-content">${content}</div>
      <div class="modal-actions">
        <button class="btn btn-secondary modal-cancel">Cancel</button>
        ${onConfirm ? '<button class="btn btn-primary modal-confirm">Confirm</button>' : ''}
      </div>
    </div>
  `;

  document.body.appendChild(modal);

  modal.querySelector('.modal-cancel')?.addEventListener('click', () => modal.remove());
  modal.querySelector('.modal-overlay')?.addEventListener('click', (e) => {
    if (e.target === modal) modal.remove();
  });

  if (onConfirm) {
    modal.querySelector('.modal-confirm')?.addEventListener('click', () => {
      onConfirm();
      modal.remove();
    });
  }
}

export function closeModal(): void {
  document.querySelector('.modal-overlay')?.remove();
}

export function showAlert(message: string, type: 'success' | 'error' = 'success'): void {
  const existingAlert = document.querySelector('.alert-toast');
  if (existingAlert) existingAlert.remove();

  const alert = document.createElement('div');
  alert.className = `alert-toast alert-${type}`;
  alert.textContent = message;
  document.body.appendChild(alert);

  setTimeout(() => alert.remove(), 3000);
}
