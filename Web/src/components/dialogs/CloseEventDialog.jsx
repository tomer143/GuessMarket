import { useState } from 'react';
import Modal from '../Modal.jsx';
import OptionChoice from '../OptionChoice.jsx';
import { api } from '../../api.js';
import { useAction } from './useAction.js';

export default function CloseEventDialog({ event, onClose, onDone }) {
  const [winner, setWinner] = useState(0);
  const { busy, error, run } = useAction();

  const submit = () =>
    run(async () => {
      await api.closeEvent(event.id, winner);
      onDone('Event closed', `"${event.name}" has been closed.`);
    });

  return (
    <Modal
      title="Close Event"
      header={`Close "${event.name}" and declare the winning option`}
      onOk={submit}
      onCancel={onClose}
      busy={busy}
      error={error}
    >
      <div className="form-grid">
        <label>Winning option:</label>
        <OptionChoice name="winning-option" options={event.optionNames} value={winner} onChange={setWinner} />
      </div>
    </Modal>
  );
}
