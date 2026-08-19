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

function Profile() {
  const modalRef = useRef();
  const [profile, setProfile] = useState({ firstname: '', lastname: '', username: '' });
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
      toastError(error.response?.data || 'Failed to change password');
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
            <p className="profile__muted">@{profile.username}</p>
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
              <span className="details">Username</span>
              <input type="text" value={profile.username} readOnly />
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
          <i onClick={() => toggleModal(false)} className="bi bi-x-lg"></i>
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
  const [pendingDeleteId, setPendingDeleteId] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [values, setValues] = useState({
    name: '',
    email: '',
    phone: '',
    address: '',
    title: '',
    status: '',
  });

  const location = useLocation();
  const hideChrome = ['/login', '/registration'].includes(location.pathname);

  const getAllContacts = async (page = 0, size = 10, search = searchTerm) => {
    try {
      setCurrentPage(page);
      const { data } = await getContacts(page, size, search);
      setData(data);
      console.log(data);
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
      deleteModalRef.current.close();
      setPendingDeleteId(null);
      getAllContacts(currentPage);
    } catch (error) {
      console.log(error);
      toastError(error.message);
    }
  };

  const cancelDeleteContact = () => {
    deleteModalRef.current.close();
    setPendingDeleteId(null);
  };

  const onChange = (event) => {
    setValues({ ...values, [event.target.name]: event.target.value });
  };

  const handleNewContact = async (event) => {
    event.preventDefault();
    try {
      const { data } = await saveContact(values);
      const formData = new FormData();
      formData.append('file', file, file.name);
      formData.append('id', data.id);
      await updatePhoto(formData);
      toggleModal(false);
      resetNewContactForm();
      getAllContacts();
    } catch (error) {
      console.log(error);
      toastError(error.message);
    }
  };


  const resetNewContactForm = () => {
    setValues({
      name: '',
      email: '',
      phone: '',
      address: '',
      title: '',
      status: '',
    });
    setFile(undefined);
    if (fileRef.current) {
      fileRef.current.value = null;
    }
  };

  const handleCancelNewContact = () => {
    toggleModal(false);
    resetNewContactForm();
  };

  const updateContact = async (contact) => {
    try {
      const { data } = await updateContactApi(contact.id, contact);
      console.log(data);
    } catch (error) {
      console.log(error);
      toastError(error.message);
    }
  };

  const updateImage = async (formData) => {
    try {
      await updatePhoto(formData);
    } catch (error) {
      console.log(error);
      toastError(error.message);
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
              <i onClick={handleCancelNewContact} className="bi bi-x-lg"></i>
            </div>
            <div className="divider"></div>
            <div className="modal__body">
              <form onSubmit={handleNewContact}>
                <div className="user-details">
                  <div className="input-box">
                    <span className="details">Name</span>
                    <input type="text" value={values.name} onChange={onChange} name='name' required />
                  </div>
                  <div className="input-box">
                    <span className="details">Email</span>
                    <input type="text" value={values.email} onChange={onChange} name='email' required />
                  </div>
                  <div className="input-box">
                    <span className="details">Title</span>
                    <input type="text" value={values.title} onChange={onChange} name='title' required />
                  </div>
                  <div className="input-box">
                    <span className="details">Phone Number</span>
                    <input type="text" value={values.phone} onChange={onChange} name='phone' required />
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
                    <input type="file" onChange={(event) => setFile(event.target.files[0])} ref={fileRef} name='photo' required />
                  </div>
                </div>
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
              <i onClick={cancelDeleteContact} className="bi bi-x-lg"></i>
            </div>
            <div className="divider"></div>
            <div className="modal__body">
              <p style={{ margin: '1rem 0' }}>Are you sure you want to delete this contact? This action cannot be undone.</p>
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