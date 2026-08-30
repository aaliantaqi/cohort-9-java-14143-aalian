import React, { createContext, useContext, useState, useEffect, useMemo } from 'react'
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
            // Any error (401 unauthenticated, network failure, etc.) is treated
            // as "not logged in" — the specific error doesn't change the outcome.
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

    const value = useMemo(
        () => ({ isAuthenticated, loading, login, logout, checkAuthStatus }),
        [isAuthenticated, loading]
    );

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};