import { useEffect, useRef, useState } from 'react';
import 'react-toastify/dist/ReactToastify.css';
import Header from './components/Header'
import ContactList from './components/ContactList'
import { getContacts, saveContact, updatePhoto, updateContact as updateContactApi, deleteContact, getProfile, changePassword } from './api/ContactService';
import { Routes, Route, Navigate, useLocation, Link } from 'react-router-dom';
import ContactDetail from './components/ContactDetail';
import { toastError, toastSuccess } from './api/ToastService';
import { ToastContainer } from 'react-toastify';
import { AuthProvider } from './components/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './components/Login';
import Registration from './components/Registration';

const makeId = () => (typeof crypto !== 'undefined' && crypto.randomUUID
  ? crypto.randomUUID()
  : `row-${Date.now()}-${Math.random().toString(36).slice(2)}`);

function Profile() {
  const modalRef = useRef();
  const [profile, setProfile] = useState({ firstname: '', lastname: '', email: '', phone: '' });
  const [passwords, setPasswords] = useState({
    currentPassword: '',
    newPassword: '',
    confirmNewPassword: ''
  });

  const fetchProfile = async () => {
    try {
      const { data } = await getProfile();
      setProfile(data);
    } catch (error) {
      console.log(error);
      toastError('Failed to load profile');
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const toggleModal = (show) => show ? modalRef.current.showModal() : modalRef.current.close();

  const onChange = (event) => {
    setPasswords({ ...passwords, [event.target.name]: event.target.value });
  };

  const handleChangePassword = async (event) => {
    event.preventDefault();

    if (passwords.newPassword !== passwords.confirmNewPassword) {
      toastError('New passwords do not match');
      return;
    }

    try {
      await changePassword(passwords.currentPassword, passwords.newPassword);
      toastSuccess('Password changed successfully');
      toggleModal(false);
      setPasswords({ currentPassword: '', newPassword: '', confirmNewPassword: '' });
    } catch (error) {
      console.log(error);
      const message = typeof error.response?.data === 'string'
        ? error.response.data
        : error.response?.data?.message || 'Failed to change password';
      toastError(message);
    }
  };

  return (
    <>
      <Link to={'/contacts'} className='link'><i className='bi bi-arrow-left'></i> Back to list</Link>

      <div className="profile">
        <div className="profile__details">
          <div className="profile__avatar">
            {profile.firstname ? profile.firstname.charAt(0).toUpperCase() : ''}
            {profile.lastname ? profile.lastname.charAt(0).toUpperCase() : ''}
          </div>
          <div>
            <p className="profile__name">{profile.firstname} {profile.lastname}</p>
            <p className="profile__muted">{profile.email || profile.phone}</p>
          </div>
        </div>

        <div className="profile__settings">
          <h3 style={{ marginBottom: '1.25rem' }}>Account Information</h3>
          <div className="user-details">
            <div className="input-box">
              <span className="details">First Name</span>
              <input type="text" value={profile.firstname} readOnly />
            </div>
            <div className="input-box">
              <span className="details">Last Name</span>
              <input type="text" value={profile.lastname} readOnly />
            </div>
            <div className="input-box">
              <span className="details">Email</span>
              <input type="text" value={profile.email || '—'} readOnly />
            </div>
            <div className="input-box">
              <span className="details">Phone Number</span>
              <input type="text" value={profile.phone || '—'} readOnly />
            </div>
          </div>

          <div className="form_footer" style={{ justifyContent: 'flex-start' }}>
            <button onClick={() => toggleModal(true)} className='btn'>
              <i className='bi bi-key'></i> Change Password
            </button>
          </div>
        </div>
      </div>

      <dialog ref={modalRef} className="modal" id="changePasswordModal">
        <div className="modal__header">
          <h3>Change Password</h3>
          <button type="button" onClick={() => toggleModal(false)} className="modal__close" aria-label="Close">
            <i className="bi bi-x-lg"></i>
          </button>
        </div>
        <div className="divider"></div>
        <div className="modal__body">
          <form onSubmit={handleChangePassword}>
            <div className="user-details" style={{ gridTemplateColumns: '1fr' }}>
              <div className="input-box">
                <span className="details">Current Password</span>
                <input type="password" value={passwords.currentPassword} onChange={onChange} name='currentPassword' required />
              </div>
              <div className="input-box">
                <span className="details">New Password</span>
                <input type="password" value={passwords.newPassword} onChange={onChange} name='newPassword' required />
              </div>
              <div className="input-box">
                <span className="details">Confirm New Password</span>
                <input type="password" value={passwords.confirmNewPassword} onChange={onChange} name='confirmNewPassword' required />
              </div>
            </div>
            <div className="form_footer">
              <button onClick={() => toggleModal(false)} type='button' className="btn btn-danger">Cancel</button>
              <button type='submit' className="btn">Reset</button>
            </div>
          </form>
        </div>
      </dialog>
    </>
  )
}

function App() {
  const modalRef = useRef();
  const fileRef = useRef();
  const deleteModalRef = useRef();
  const [data, setData] = useState({});
  const [currentPage, setCurrentPage] = useState(0);
  const [file, setFile] = useState(undefined);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [pendingDeleteId, setPendingDeleteId] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [values, setValues] = useState({
    firstname: '',
    lastname: '',
    title: '',
    address: '',
    status: '',
    emails: [{ label: '', email: '', _key: makeId() }],
    phones: [{ label: '', phone: '', _key: makeId() }],
  });

  const location = useLocation();
  const hideChrome = ['/login', '/registration'].includes(location.pathname);

  const getAllContacts = async (page = 0, size = 10, search = searchTerm) => {
    try {
      setCurrentPage(page);
      const { data } = await getContacts(page, size, search);
      setData(data);
    } catch (error) {
      console.log(error);
      toastError(error.message);
    }
  };

  const handleSearch = (term) => {
    setSearchTerm(term);
    getAllContacts(0, 10, term);
  };

  const requestDeleteContact = (id) => {
    setPendingDeleteId(id);
    deleteModalRef.current.showModal();
  };

  const confirmDeleteContact = async () => {
    try {
      await deleteContact(pendingDeleteId);
      toastSuccess('Contact deleted');
      getAllContacts(currentPage);
    } catch (error) {
      console.log(error);
      toastError(error.message);
    } finally {
      deleteModalRef.current.close();
      setPendingDeleteId(null);
    }
  };

  const cancelDeleteContact = () => {
    deleteModalRef.current.close();
    setPendingDeleteId(null);
  };

  const onChange = (event) => {
    setValues({ ...values, [event.target.name]: event.target.value });
  };

  const onEmailChange = (index, field, value) => {
    const updatedEmails = [...values.emails];
    updatedEmails[index] = { ...updatedEmails[index], [field]: value };
    setValues({ ...values, emails: updatedEmails });
  };

  const addEmailRow = () => {
    setValues({ ...values, emails: [...values.emails, { label: '', email: '', _key: makeId() }] });
  };

  const removeEmailRow = (index) => {
    const updatedEmails = values.emails.filter((_, i) => i !== index);
    setValues({ ...values, emails: updatedEmails.length > 0 ? updatedEmails : [{ label: '', email: '', _key: makeId() }] });
  };

  const onPhoneChange = (index, field, value) => {
    const updatedPhones = [...values.phones];
    updatedPhones[index] = { ...updatedPhones[index], [field]: value };
    setValues({ ...values, phones: updatedPhones });
  };

  const addPhoneRow = () => {
    setValues({ ...values, phones: [...values.phones, { label: '', phone: '', _key: makeId() }] });
  };

  const removePhoneRow = (index) => {
    const updatedPhones = values.phones.filter((_, i) => i !== index);
    setValues({ ...values, phones: updatedPhones.length > 0 ? updatedPhones : [{ label: '', phone: '', _key: makeId() }] });
  };

  const handleNewContact = async (event) => {
    event.preventDefault();
    try {
      const payload = {
        ...values,
        emails: values.emails.filter(e => e.email.trim() !== '').map(({ _key, ...rest }) => rest),
        phones: values.phones.filter(p => p.phone.trim() !== '').map(({ _key, ...rest }) => rest),
      };
      const { data } = await saveContact(payload);
      toggleModal(false);
      resetNewContactForm();

      try {
        const formData = new FormData();
        formData.append('file', file, file.name);
        formData.append('id', data.id);
        await updatePhoto(formData);
        toastSuccess('Contact created');
      } catch (photoError) {
        console.log(photoError);
        toastError('Contact created, but photo upload failed');
      }

      getAllContacts();
    } catch (error) {
      console.log(error);
      toastError(error.message);
    }
  };

  const resetNewContactForm = () => {
    setValues({
      firstname: '',
      lastname: '',
      title: '',
      address: '',
      status: '',
      emails: [{ label: '', email: '', _key: makeId() }],
      phones: [{ label: '', phone: '', _key: makeId() }],
    });
    setFile(undefined);
    if (fileRef.current) {
      fileRef.current.value = null;
    }
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }
    setPreviewUrl(null);
  };

  const handleCancelNewContact = () => {
    toggleModal(false);
    resetNewContactForm();
  };

  const updateContact = async (contact) => {
    try {
      await updateContactApi(contact.id, contact);
    } catch (error) {
      console.log(error);
      toastError(error.message);
      throw error;
    }
  };

  const updateImage = async (formData) => {
    try {
      await updatePhoto(formData);
    } catch (error) {
      console.log(error);
      toastError(error.message);
      throw error;
    }
  };

  const toggleModal = show => show ? modalRef.current.showModal() : modalRef.current.close();

  useEffect(() => {
    if (location.pathname === '/contacts') {
      getAllContacts();
    }
  }, [location.pathname]);

  return (
    <AuthProvider>
      {!hideChrome && <Header toggleModal={toggleModal} nbOfContacts={data.totalElements} onSearch={handleSearch} />}
      <main className='main'>
        <div className='container'>
          <Routes>
            <Route path='/' element={<Navigate to={'/contacts'} />} />
            <Route path='/login' element={<Login />} />
            <Route path='/registration' element={<Registration />} />
            <Route path="/contacts" element={
              <ProtectedRoute><ContactList data={data} currentPage={currentPage} getAllContacts={getAllContacts} deleteContact={requestDeleteContact} /></ProtectedRoute>
            } />
            <Route path="/contacts/:id" element={
              <ProtectedRoute><ContactDetail updateContact={updateContact} updateImage={updateImage} /></ProtectedRoute>
            } />
            <Route path="/profile" element={
              <ProtectedRoute><Profile /></ProtectedRoute>
            } />
          </Routes>
        </div>
      </main>

      {!hideChrome && (
        <>
          <dialog ref={modalRef} className="modal" id="modal">
            <div className="modal__header">
              <h3>New Contact</h3>
              <button type="button" onClick={handleCancelNewContact} className="modal__close" aria-label="Close">
                <i className="bi bi-x-lg"></i>
              </button>
            </div>
            <div className="divider"></div>
            <div className="modal__body">
              <form onSubmit={handleNewContact}>
                <div className="user-details">
                  <div className="input-box">
                    <span className="details">First Name</span>
                    <input type="text" value={values.firstname} onChange={onChange} name='firstname' required />
                  </div>
                  <div className="input-box">
                    <span className="details">Last Name</span>
                    <input type="text" value={values.lastname} onChange={onChange} name='lastname' required />
                  </div>
                  <div className="input-box">
                    <span className="details">Title</span>
                    <input type="text" value={values.title} onChange={onChange} name='title' required />
                  </div>
                  <div className="input-box">
                    <span className="details">Address</span>
                    <input type="text" value={values.address} onChange={onChange} name='address' required />
                  </div>
                  <div className="input-box">
                    <span className="details">Account Status</span>
                    <input type="text" value={values.status} onChange={onChange} name='status' required />
                  </div>
                  <div className="file-input">
                    <span className="details">Profile Photo</span>
                    {previewUrl && (
                      <img
                        src={previewUrl}
                        alt="Selected profile preview"
                        style={{ width: '80px', height: '80px', objectFit: 'cover', borderRadius: '8px', marginBottom: '0.5rem' }}
                      />
                    )}
                    <input
                      type="file"
                      onChange={(event) => {
                        const selectedFile = event.target.files[0];
                        setFile(selectedFile);
                        if (selectedFile) {
                          setPreviewUrl(URL.createObjectURL(selectedFile));
                        }
                      }}
                      ref={fileRef}
                      name='photo'
                      required
                    />
                  </div>
                </div>

                <div className="divider" style={{ margin: '1rem 0' }}></div>
                <span className="details" style={{ fontWeight: 600, display: 'block', marginBottom: '0.5rem' }}>Email Addresses</span>
                <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '0.35rem', fontSize: '0.8rem', color: '#888' }}>
                  <span style={{ flex: '0 0 35%' }}>Label</span>
                  <span style={{ flex: 1 }}>Email</span>
                </div>
                {values.emails.map((emailRow, index) => (
                  <div key={emailRow._key} style={{ marginBottom: '0.6rem' }}>
                    <div style={{ display: 'flex', gap: '0.75rem' }}>
                      <select
                        style={{ flex: '0 0 35%', padding: '0.55rem', borderRadius: '6px', border: '1px solid #ccc' }}
                        value={emailRow.label}
                        onChange={(e) => onEmailChange(index, 'label', e.target.value)}
                        aria-label={`Email label for entry ${index + 1}`}
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
                        aria-label={`Email address for entry ${index + 1}`}
                      />
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.4rem' }}>
                      {index === values.emails.length - 1 ? (
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
                {values.phones.map((phoneRow, index) => (
                  <div key={phoneRow._key} style={{ marginBottom: '0.6rem' }}>
                    <div style={{ display: 'flex', gap: '0.75rem' }}>
                      <select
                        style={{ flex: '0 0 35%', padding: '0.55rem', borderRadius: '6px', border: '1px solid #ccc' }}
                        value={phoneRow.label}
                        onChange={(e) => onPhoneChange(index, 'label', e.target.value)}
                        aria-label={`Phone label for entry ${index + 1}`}
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
                        aria-label={`Phone number for entry ${index + 1}`}
                      />
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.4rem' }}>
                      {index === values.phones.length - 1 ? (
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
                  <button onClick={handleCancelNewContact} type='button' className="btn btn-danger">Cancel</button>
                  <button type='submit' className="btn">Save</button>
                </div>
              </form>
            </div>
          </dialog>

          <dialog ref={deleteModalRef} className="modal" id="deleteConfirmModal" style={{ maxWidth: '400px' }}>
            <div className="modal__header">
              <h3>Delete Contact</h3>
              <button type="button" onClick={cancelDeleteContact} className="modal__close" aria-label="Close">
                <i className="bi bi-x-lg"></i>
              </button>
            </div>
            <div className="divider"></div>
            <div className="modal__body">
              <p style={{ margin: '1rem 0' }}>Are you sure you want to delete this contact?</p>
            </div>
            <div className="form_footer">
              <button onClick={cancelDeleteContact} type='button' className="btn btn-danger">Cancel</button>
              <button onClick={confirmDeleteContact} type='button' className="btn">Confirm Delete</button>
            </div>
          </dialog>
        </>
      )}
      <ToastContainer />
    </AuthProvider>
  );
}

export default App;