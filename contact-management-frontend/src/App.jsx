import './App.css';
import { useEffect } from 'react'
import axios from 'axios'
import ButtonAppBar from './components/Appbar';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Registration from './components/Registration';
import RegistrationSuccess from './components/RegistrationSuccess';
import Login from './components/Login';
import LoginSuccess from './components/LoginSuccess';
import { AuthProvider } from './components/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

function App() {

    useEffect(() => {
        axios.get('/api/csrf-token', { withCredentials: true }).catch((error) => {
            console.error('Failed to initialize CSRF token:', error);
        });
    }, []);

  return (
    <div className="App">
      <AuthProvider>
        <BrowserRouter>
          <ButtonAppBar></ButtonAppBar>
          <Routes>
            <Route path='/registration' element={<Registration></Registration>} />
            <Route path='/registrationSuccess' element={<RegistrationSuccess></RegistrationSuccess>} />
            <Route path='/login' element={<Login></Login>} />
            <Route path='/loginSuccess' element={
              <ProtectedRoute><LoginSuccess /></ProtectedRoute>
            } />
          </Routes>
        </BrowserRouter>       
      </AuthProvider>
    </div>
  );
}

export default App;