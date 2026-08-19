import React from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from './AuthContext'
import axios from 'axios'
import { getCsrfToken } from '../csrf'
import { toastSuccess, toastError } from '../api/ToastService'

const Header = ({toggleModal, nbOfContacts, onSearch}) => {
  const navigate = useNavigate();
  const { logout } = useAuth();

  const handleLogout = async () => {
    try {
      await axios.post('/api/logout', {}, {
        withCredentials: true,
        headers: { 'X-XSRF-TOKEN': getCsrfToken() }
      });
      logout();
      toastSuccess('Logged out successfully');
      navigate('/login');
    } catch (error) {
      console.log(error);
      toastError('Failed to logout');
    }
  };

  return (
        <header className='header'>
            <div className='container'>
                <div className='header__brand'>
                    <i className='bi bi-person-lines-fill header__logo'></i>
                    <div>
                        <h2 className='header__title'>Contact Management System</h2>
                        <p className='header__subtitle'>Contact List ({nbOfContacts})</p>
                    </div>
                </div>
                <div className='header__search'>
                    <i className='bi bi-search'></i>
                    <input
                        type='text'
                        placeholder='Search by name...'
                        onKeyDown={(event) => {
                            if (event.key === 'Enter') {
                                onSearch(event.target.value);
                            }
                        }}
                    />
                </div>
                <div className='header__actions'>
                    <button onClick={() => toggleModal(true)} className='btn'>
                        <i className='bi bi-plus-square'></i> Add New Contact
                    </button>
                   <Link to='/profile' className='btn'>
    <i className='bi bi-person-circle'></i> Profile
</Link>
                    <button onClick={handleLogout} className='btn btn-danger'>
                        <i className='bi bi-box-arrow-right'></i> Logout
                    </button>
                </div>
            </div>
        </header>    
)
}

export default Header