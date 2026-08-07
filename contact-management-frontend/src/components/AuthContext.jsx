import React, { createContext, useContext, useState, useEffect } from 'react'
import axios from 'axios';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        checkAuthStatus();
    }, []);

    const checkAuthStatus = async () => {
        try {
            const response = await axios.get('/api/me', { withCredentials: true });
            setIsAuthenticated(response.status === 200);
        } catch (error) {
            setIsAuthenticated(false);
        } finally {
            setLoading(false);
        }
    };

    const login = () => {
        setIsAuthenticated(true);
        setLoading(false);
    };

    const logout = () => {
        setIsAuthenticated(false);
        setLoading(false);
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated, loading, login, logout, checkAuthStatus }}>
            {children}
        </AuthContext.Provider>
    );
};