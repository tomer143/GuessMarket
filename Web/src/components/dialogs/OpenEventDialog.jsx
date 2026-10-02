import Modal from '../Modal.jsx';
import { api } from '../../api.js';
import { useAction } from './useAction.js';

export default function OpenEventDialog({ event, onClose, onDone }) {
  const { busy, error, run } = useAction();

  const submit = () =>
    run(async () => {
      await api.openEvent(event.id);
      onDone('Event opened', `"${event.name}" is now active.`);
    });

  return (
    <Modal
      title="Open Event"
      header={`Open "${event.name}" as its market maker (${event.mmUsername})`}
      onOk={submit}
      onCancel={onClose}
      busy={busy}
      error={error}
    >
      <div>Once opened, users can start trading on this event.</div>
    </Modal>
  );
}
