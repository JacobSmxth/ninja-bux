export interface ModalOptions {
  title: string;
  content: string;
  onConfirm?: () => void | Promise<void>;
  showConfirm?: boolean;
  confirmText?: string;
}

export function showModal(title: string, content: string, onConfirm?: () => void): void;
export function showModal(options: ModalOptions): void;
export function showModal(
  titleOrOptions: string | ModalOptions,
  content?: string,
  onConfirm?: () => void
): void {
  const options: ModalOptions =
    typeof titleOrOptions === 'string'
      ? { title: titleOrOptions, content: content!, onConfirm }
      : titleOrOptions;

  const existingModal = document.querySelector('.modal-overlay');
  if (existingModal) existingModal.remove();

  const showConfirmBtn = options.showConfirm ?? !!options.onConfirm;
  const confirmText = options.confirmText ?? 'Confirm';

  const modal = document.createElement('div');
  modal.className = 'modal-overlay';
  modal.innerHTML = `
    <div class="modal">
      <h2>${options.title}</h2>
      <div class="modal-content">${options.content}</div>
      <div class="modal-actions">
        <button class="btn btn-secondary modal-cancel">Cancel</button>
        ${showConfirmBtn ? `<button class="btn btn-primary modal-confirm">${confirmText}</button>` : ''}
      </div>
    </div>
  `;

  document.body.appendChild(modal);

  modal.querySelector('.modal-cancel')?.addEventListener('click', () => modal.remove());
  modal.addEventListener('click', (e) => {
    if (e.target === modal) modal.remove();
  });

  if (options.onConfirm) {
    modal.querySelector('.modal-confirm')?.addEventListener('click', async () => {
      try {
        await options.onConfirm!();
        modal.remove();
      } catch {
        // Don't close modal on error - let the callback handle showing errors
      }
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
