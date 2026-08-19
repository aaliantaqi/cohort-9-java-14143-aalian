import React from 'react'
import { Link } from 'react-router-dom'

const Contact = ({contact, deleteContact}) => {

  const handleDelete = (event) => {
    event.preventDefault();
    event.stopPropagation();
    deleteContact(contact.id);
  };

  return (
    <Link to={`/contacts/${contact.id}`} className="contact__item">
        <div className="contact__left">
            <div className="contact__image">
                <img src={contact.photoUrl} alt={contact.name}/>
            </div>
            <p className="contact_name"> {contact.name.substring(0,15)} </p>
            <p className="contact_title"> {contact.title}</p>
        </div>
        <div className="contact__content">
            <div className="contact__body">
                <p><i className="bi bi-envelope"></i> {contact.email.substring(0, 20)}</p>
                <p><i className="bi bi-geo"></i> {contact.address}</p>
                <p><i className="bi bi-telephone"></i> {contact.phone}</p>
                <p className="contact__status-row">
                    <span>
                        {contact.status === 'Active' ? <i className="bi bi-check-circle"></i> : <i className="bi bi-x-circle"></i>} {contact.status}
                    </span>
                    <i onClick={handleDelete} className="bi bi-trash contact__delete" title="Delete contact"></i>
                </p>
            </div>
        </div>
    </Link>
  )
}

export default Contact