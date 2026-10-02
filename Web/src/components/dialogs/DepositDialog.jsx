import { useState } from 'react';
import Modal from '../Modal.jsx';
import { api } from '../../api.js';
import { useAction } from './useAction.js';

export default function DepositDialog({ username, onClose, onDone }) {
  const [amount, setAmount] = useState('');
  const { busy, error, setError, run } = useAction();

  const submit = () => {
    const value = Number(amount);
    if (amount.trim() === '' || Number.isNaN(value)) {
      setError(`"${amount}" is not a valid amount.`);
      return;
    }
    run(async () => {
      await api.deposit(username, value);
      onDone('Deposit successful', `Deposited ${amount.trim()} into your account.`);
    });
  };

  return (
    <Modal title="Deposit Funds" header="Deposit funds into your account" onOk={submit} onCancel={onClose} busy={busy} error={error}>
      <div className="form-grid">
        <label>Amount:</label>
        <input value={amount} onChange={(e) => setAmount(e.target.value)} autoFocus />
      </div>
    </Modal>
  );
}
