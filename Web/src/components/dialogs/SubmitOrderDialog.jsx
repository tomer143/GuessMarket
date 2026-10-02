import { useState } from 'react';
import Modal from '../Modal.jsx';
import OptionChoice from '../OptionChoice.jsx';
import { api } from '../../api.js';
import { useAction } from './useAction.js';

export default function SubmitOrderDialog({ event, onClose, onDone }) {
  const [optionIndex, setOptionIndex] = useState(0);
  const [side, setSide] = useState('BUY');
  const [quantity, setQuantity] = useState('1');
  const [price, setPrice] = useState('');
  const { busy, error, setError, run } = useAction();

  const submit = () => {
    const qty = Number(quantity);
    if (!Number.isInteger(qty) || qty < 1) {
      setError(`"${quantity}" is not a valid quantity.`);
      return;
    }
    const parsedPrice = Number(price);
    if (price.trim() === '' || Number.isNaN(parsedPrice)) {
      setError(`"${price}" is not a valid price.`);
      return;
    }
    run(async () => {
      const result = await api.submitOrder(event.id, optionIndex, side, qty, parsedPrice);
      const fills = result.fills ?? [];
      const summary =
        fills.length === 0
          ? `No immediate match. ${result.unfilledQuantity} share(s) now resting in the order book.`
          : `${fills.length} fill(s), ${result.unfilledQuantity} share(s) still unfilled.`;
      onDone('Order submitted', summary);
    });
  };

  const sideRadio = (value, label) => (
    <label className="radio">
      <input type="radio" checked={side === value} onChange={() => setSide(value)} />
      {label}
    </label>
  );

  return (
    <Modal title="Submit Order" header={`Submit an order for "${event.name}"`} onOk={submit} onCancel={onClose} busy={busy} error={error}>
      <div className="form-grid">
        <label>Option:</label>
        <OptionChoice name="order-option" options={event.optionNames} value={optionIndex} onChange={setOptionIndex} />
        <label>Side:</label>
        <div className="radio-group">
          {sideRadio('BUY', 'Buy')}
          {sideRadio('SELL', 'Sell')}
        </div>
        <label>Quantity:</label>
        <input type="number" min="1" max="1000000" step="1" value={quantity} onChange={(e) => setQuantity(e.target.value)} />
        <label>Price per share:</label>
        <input placeholder="e.g. 0.50" value={price} onChange={(e) => setPrice(e.target.value)} autoFocus />
      </div>
    </Modal>
  );
}
