import Modal from './Modal.jsx';

export default function MessageBox({ title, text, onClose }) {
  return (
    <Modal title="Guess Market" header={title} onOk={onClose} onCancel={onClose} hideCancel>
      <div className="message-text">{text}</div>
    </Modal>
  );
}
