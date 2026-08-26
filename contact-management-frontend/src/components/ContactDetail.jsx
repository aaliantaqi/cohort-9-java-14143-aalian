import React, { useState, useEffect, useRef } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getContact } from '../api/ContactService';
import { toastError, toastSuccess } from '../api/ToastService';

const ContactDetail = ({ updateContact, updateImage }) => {
    const inputRef = useRef();
    const [contact, setContact] = useState({
        id: '',
        firstname: '',
        lastname: '',
        address: '',
        title: '',
        status: '',
        photoUrl: '',
        emails: [],
        phones: []
    });

    const { id } = useParams();

    const fetchContact = async (id) => {
        try {
            const { data } = await getContact(id);
            // Make sure emails/phones are always arrays, even if the backend omits
            // them when empty (JsonInclude.NON_DEFAULT on the Contact entity does this)
            setContact({
                ...data,
                emails: data.emails && data.emails.length > 0 ? data.emails : [{ label: '', email: '' }],
                phones: data.phones && data.phones.length > 0 ? data.phones : [{ label: '', phone: '' }]
            });
            console.log(data);
        } catch (error) {
            console.log(error);
            toastError(error.message);
        }
    };

    const selectImage = () => {
        inputRef.current.click();
    };

    const udpatePhoto = async (file) => {
    try {
        const formData = new FormData();
        formData.append('file', file, file.name);
        formData.append('id', id);
        await updateImage(formData);

        const { data } = await getContact(id);
        setContact({
            ...data,
            photoUrl: `${data.photoUrl}?updated_at=${new Date().getTime()}`
        });

        toastSuccess('Photo updated');
    } catch (error) {
        console.log(error);
        toastError('Failed to update photo');
    }
};

    const onChange = (event) => {
        setContact({ ...contact, [event.target.name]: event.target.value });
    };

    // --- Email row helpers ---
    const onEmailChange = (index, field, value) => {
        const updatedEmails = [...contact.emails];
        updatedEmails[index] = { ...updatedEmails[index], [field]: value };
        setContact({ ...contact, emails: updatedEmails });
    };

    const addEmailRow = () => {
        setContact({ ...contact, emails: [...contact.emails, { label: '', email: '' }] });
    };

    const removeEmailRow = (index) => {
        const updatedEmails = contact.emails.filter((_, i) => i !== index);
        setContact({ ...contact, emails: updatedEmails.length > 0 ? updatedEmails : [{ label: '', email: '' }] });
    };

    // --- Phone row helpers ---
    const onPhoneChange = (index, field, value) => {
        const updatedPhones = [...contact.phones];
        updatedPhones[index] = { ...updatedPhones[index], [field]: value };
        setContact({ ...contact, phones: updatedPhones });
    };

    const addPhoneRow = () => {
        setContact({ ...contact, phones: [...contact.phones, { label: '', phone: '' }] });
    };

    const removePhoneRow = (index) => {
        const updatedPhones = contact.phones.filter((_, i) => i !== index);
        setContact({ ...contact, phones: updatedPhones.length > 0 ? updatedPhones : [{ label: '', phone: '' }] });
    };

    const onUpdateContact = async (event) => {
        event.preventDefault();
        try {
            const payload = {
              ...contact,
              emails: contact.emails.filter(e => e.email.trim() !== ''),
              phones: contact.phones.filter(p => p.phone.trim() !== ''),
            };
            await updateContact(payload);
            fetchContact(id);
            toastSuccess('Contact Updated');
        } catch (error) {
            console.log(error);
        }
    };

    useEffect(() => {
        fetchContact(id);
    }, [id]);

    // Pick the first email/phone just for the header display area
    const primaryEmail = contact.emails.find(e => e.email)?.email || '';

    return (
        <>
            <Link to={'/contacts'} className='link'><i className='bi bi-arrow-left'></i> Back to list</Link>
            <div className='profile'>
                <div className='profile__details'>
                    <img src={contact.photoUrl} alt={`Profile photo of ${contact.firstname} ${contact.lastname}`} />
                    <div className='profile__metadata'>
                        <p className='profile__name'>{contact.firstname} {contact.lastname}</p>
                        <p className='profile__muted'>JPG, GIF, or PNG. Max size of 10MG</p>
                        <button onClick={selectImage} className='btn'><i className='bi bi-cloud-upload'></i> Change Photo</button>
                    </div>
                </div>
                <div className='profile__settings'>
                    <div>
                        <form onSubmit={onUpdateContact} className="form">
                            <div className="user-details">
                                <input type="hidden" defaultValue={contact.id} name="id" required />
                                <div className="input-box">
                                    <span className="details">First Name</span>
                                    <input type="text" value={contact.firstname} onChange={onChange} name="firstname" required />
                                </div>
                                <div className="input-box">
                                    <span className="details">Last Name</span>
                                    <input type="text" value={contact.lastname} onChange={onChange} name="lastname" required />
                                </div>
                                <div className="input-box">
                                    <span className="details">Address</span>
                                    <input type="text" value={contact.address} onChange={onChange} name="address" required />
                                </div>
                                <div className="input-box">
                                    <span className="details">Title</span>
                                    <input type="text" value={contact.title} onChange={onChange} name="title" required />
                                </div>
                                <div className="input-box">
                                    <span className="details">Status</span>
                                    <input type="text" value={contact.status} onChange={onChange} name="status" required />
                                </div>
                            </div>

                            <div className="divider" style={{ margin: '1rem 0' }}></div>
                            <span className="details" style={{ fontWeight: 600, display: 'block', marginBottom: '0.5rem' }}>Email Addresses</span>
                            <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '0.35rem', fontSize: '0.8rem', color: '#888' }}>
                                <span style={{ flex: '0 0 35%' }}>Label</span>
                                <span style={{ flex: 1 }}>Email</span>
                            </div>
                            {contact.emails.map((emailRow, index) => (
                                <div key={index} style={{ marginBottom: '0.6rem' }}>
                                    <div style={{ display: 'flex', gap: '0.75rem' }}>
                                        <select
                                            style={{ flex: '0 0 35%', padding: '0.55rem', borderRadius: '6px', border: '1px solid #ccc' }}
                                            value={emailRow.label}
                                            onChange={(e) => onEmailChange(index, 'label', e.target.value)}
                                        >
                                            <option value="">Select label</option>
                                            <option value="Work">Work</option>
                                            <option value="Personal">Personal</option>
                                            <option value="Home">Home</option>
                                            <option value="Other">Other</option>
                                        </select>
                                        <input
                                            type="email"
                                            style={{ flex: 1, padding: '0.55rem', borderRadius: '6px', border: '1px solid #ccc' }}
                                            value={emailRow.email}
                                            onChange={(e) => onEmailChange(index, 'email', e.target.value)}
                                        />
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.4rem' }}>
                                        {index === contact.emails.length - 1 ? (
                                            <button
                                                type="button"
                                                className="btn"
                                                style={{ padding: '0.3rem 0.9rem', fontSize: '0.85rem' }}
                                                onClick={addEmailRow}
                                            >
                                                + Add another email
                                            </button>
                                        ) : <span></span>}
                                        <button
                                            type="button"
                                            className="btn btn-danger"
                                            style={{ padding: '0.3rem 0.9rem', fontSize: '0.85rem' }}
                                            onClick={() => removeEmailRow(index)}
                                        >
                                            Remove
                                        </button>
                                    </div>
                                </div>
                            ))}

                            <div className="divider" style={{ margin: '1rem 0' }}></div>
                            <span className="details" style={{ fontWeight: 600, display: 'block', marginBottom: '0.5rem' }}>Phone Numbers</span>
                            <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '0.35rem', fontSize: '0.8rem', color: '#888' }}>
                                <span style={{ flex: '0 0 35%' }}>Label</span>
                                <span style={{ flex: 1 }}>Phone</span>
                            </div>
                            {contact.phones.map((phoneRow, index) => (
                                <div key={index} style={{ marginBottom: '0.6rem' }}>
                                    <div style={{ display: 'flex', gap: '0.75rem' }}>
                                        <select
                                            style={{ flex: '0 0 35%', padding: '0.55rem', borderRadius: '6px', border: '1px solid #ccc' }}
                                            value={phoneRow.label}
                                            onChange={(e) => onPhoneChange(index, 'label', e.target.value)}
                                        >
                                            <option value="">Select label</option>
                                            <option value="Work">Work</option>
                                            <option value="Home">Home</option>
                                            <option value="Personal">Personal</option>
                                            <option value="Other">Other</option>
                                        </select>
                                        <input
                                            type="text"
                                            style={{ flex: 1, padding: '0.55rem', borderRadius: '6px', border: '1px solid #ccc' }}
                                            value={phoneRow.phone}
                                            onChange={(e) => onPhoneChange(index, 'phone', e.target.value)}
                                        />
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.4rem' }}>
                                        {index === contact.phones.length - 1 ? (
                                            <button
                                                type="button"
                                                className="btn"
                                                style={{ padding: '0.3rem 0.9rem', fontSize: '0.85rem' }}
                                                onClick={addPhoneRow}
                                            >
                                                + Add another phone
                                            </button>
                                        ) : <span></span>}
                                        <button
                                            type="button"
                                            className="btn btn-danger"
                                            style={{ padding: '0.3rem 0.9rem', fontSize: '0.85rem' }}
                                            onClick={() => removePhoneRow(index)}
                                        >
                                            Remove
                                        </button>
                                    </div>
                                </div>
                            ))}

                            <div className="form_footer">
                                <button type="submit" className="btn">Save</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <form style={{ display: 'none' }}>
                <input type='file' ref={inputRef} onChange={(event) => udpatePhoto(event.target.files[0])} name='file' accept='image/*' />
            </form>
        </>
    )
}

export default ContactDetail;