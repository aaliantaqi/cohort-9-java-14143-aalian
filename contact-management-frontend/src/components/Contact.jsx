import React from 'react'
import { Link } from 'react-router-dom'

const Contact = ({contact, deleteContact}) => {

  const handleDelete = (event) => {
    event.preventDefault();
    event.stopPropagation();
    deleteContact(contact.id);
  };

  const fullName = `${contact.firstname ?? ''} ${contact.lastname ?? ''}`.trim();

  // Contact can now have multiple emails/phones - just show the first one in the list card,
  // prefixed with its label if one was given. Full list is visible on the contact's detail page.
  const primaryEmail = contact.emails?.[0];
  const primaryPhone = contact.phones?.[0];

  return (
    <Link to={`/contacts/${contact.id}`} className="contact__item">
        <div className="contact__left">
            <div className="contact__image">
                <img src={contact.photoUrl} alt={fullName}/>
            </div>
            <p className="contact_name"> {fullName.substring(0, 15)} </p>
            <p className="contact_title"> {contact.title}</p>
        </div>
        <div className="contact__content">
            <div className="contact__body">
                <p><i className="bi bi-envelope"></i> {primaryEmail ? `${primaryEmail.label ? primaryEmail.label + ': ' : ''}${primaryEmail.email.substring(0, 20)}` : ''}</p>
                <p><i className="bi bi-geo"></i> {contact.address}</p>
                <p><i className="bi bi-telephone"></i> {primaryPhone ? `${primaryPhone.label ? primaryPhone.label + ': ' : ''}${primaryPhone.phone}` : ''}</p>
                <p className="contact__status-row">
                    <span>
                        {contact.status === 'Active' ? <i className="bi bi-check-circle"></i> : <i className="bi bi-x-circle"></i>} {contact.status}
                    </span>
                    <button
                        type="button"
                        onClick={handleDelete}
                        className="contact__delete"
                        aria-label="Delete contact"
                    >
                        <i className="bi bi-trash"></i>
                    </button>
                </p>
            </div>
        </div>
    </Link>
  )
}

export default Contact