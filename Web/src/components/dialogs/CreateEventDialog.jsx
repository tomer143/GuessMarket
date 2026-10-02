import { useState } from 'react';
import Modal from '../Modal.jsx';
import { api } from '../../api.js';
import { useAction } from './useAction.js';

export default function CreateEventDialog({ onClose, onDone }) {
  const [form, setForm] = useState({
    name: '',
    description: '',
    feePercent: 5,
    feeCollection: 'OnPurchase',
    optionAName: '',
    optionBName: '',
    method: 'LMSR',
    liquidityB: 100,
    baseValue: 1,
    initialAmount: 0,
    allowMint: false,
  });
  const { busy, error, run } = useAction();
  const setValue = (key, value) => setForm((f) => ({ ...f, [key]: value }));
  const bind = (key) => ({ value: form[key], onChange: (e) => setValue(key, e.target.value) });

  const submit = () =>
    run(async () => {
      const common = {
        name: form.name.trim(),
        description: form.description.trim(),
        feePercent: form.feePercent,
        feeCollection: form.feeCollection,
        optionAName: form.optionAName.trim(),
        optionBName: form.optionBName.trim(),
      };
      const result =
        form.method === 'LMSR'
          ? await api.createLmsrEvent(common, form.liquidityB)
          : await api.createOrderBookEvent(common, form.baseValue, form.initialAmount, form.allowMint);
      onDone('Event created', `"${common.name}" was created (id ${result.eventId}). You are now its market maker.`);
    });

  const radio = (key, value, label) => (
    <label className="radio">
      <input type="radio" checked={form[key] === value} onChange={() => setValue(key, value)} />
      {label}
    </label>
  );

  return (
    <Modal
      title="Create Event"
      header="Create a new event and become its market maker"
      onOk={submit}
      onCancel={onClose}
      busy={busy}
      error={error}
    >
      <div className="form-grid">
        <label>Event name:</label>
        <input {...bind('name')} autoFocus />
        <label>Description:</label>
        <input {...bind('description')} />
        <label>Fee percent:</label>
        <input type="number" min="0" max="90" step="1" {...bind('feePercent')} />
        <label>Fee collected:</label>
        <div className="radio-group">
          {radio('feeCollection', 'OnPurchase', 'on purchase')}
          {radio('feeCollection', 'OnClose', 'on close')}
        </div>
        <label>Option A name:</label>
        <input {...bind('optionAName')} />
        <label>Option B name:</label>
        <input {...bind('optionBName')} />
        <label>Trading method:</label>
        <div className="radio-group">
          {radio('method', 'LMSR', 'LMSR')}
          {radio('method', 'ORDER_BOOK', 'Order Book')}
        </div>
        {form.method === 'LMSR' ? (
          <>
            <label>Liquidity (b):</label>
            <input type="number" min="1" max="1000000" step="1" {...bind('liquidityB')} />
          </>
        ) : (
          <>
            <label>Base value (d):</label>
            <input type="number" min="1" max="1000000" step="1" {...bind('baseValue')} />
            <label>Initial amount:</label>
            <input type="number" min="0" max="1000000" step="1" {...bind('initialAmount')} />
            <label>Minting:</label>
            <label className="radio">
              <input type="checkbox" checked={form.allowMint} onChange={(e) => setValue('allowMint', e.target.checked)} />
              Allow minting
            </label>
          </>
        )}
      </div>
    </Modal>
  );
}
