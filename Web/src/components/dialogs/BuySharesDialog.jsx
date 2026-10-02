import { useState } from 'react';
import Modal from '../Modal.jsx';
import OptionChoice from '../OptionChoice.jsx';
import { api } from '../../api.js';
import { decimal } from '../../format.js';
import { useAction } from './useAction.js';

export default function BuySharesDialog({ event, onClose, onDone }) {
  const [optionIndex, setOptionIndex] = useState(0);
  const [amount, setAmount] = useState('1');
  const { busy, error, setError, run } = useAction();

  const submit = () => {
    const shares = Number(amount);
    if (!Number.isInteger(shares) || shares < 1) {
      setError(`"${amount}" is not a valid amount.`);
      return;
    }
    run(async () => {
      const result = await api.buyShares(event.id, optionIndex, shares);
      onDone(
        'Purchase successful',
        `Shares cost: ${decimal(result.sharesCost)}\nFee: ${decimal(result.feeAmount)}\nTotal paid: ${decimal(result.totalPaid)}`
      );
    });
  };

  return (
    <Modal title="Buy Shares" header={`Buy shares of "${event.name}"`} onOk={submit} onCancel={onClose} busy={busy} error={error}>
      <div className="form-grid">
        <label>Option:</label>
        <OptionChoice name="buy-option" options={event.optionNames} value={optionIndex} onChange={setOptionIndex} />
        <label>Amount:</label>
        <input type="number" min="1" max="1000000" step="1" value={amount} onChange={(e) => setAmount(e.target.value)} autoFocus />
      </div>
    </Modal>
  );
}
