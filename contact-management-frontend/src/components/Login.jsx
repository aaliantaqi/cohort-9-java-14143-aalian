import React, { useState } from 'react'
import {
    Typography,
    Button,
    Box,
    Container,
    Card,
    CardActions,
    CardHeader,
    CardContent,
    TextField,
    InputAdornment,
    IconButton
} from '@mui/material';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { getCsrfToken } from '../csrf';
import { useAuth } from './AuthContext';
import { toastInfo } from '../api/ToastService';

export default function Login() {
    const [identifier, setIdentifier] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const { login } = useAuth();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');

        const loginData = { identifier, password };

        try {
            const response = await axios.post('/api/login', loginData, {
                withCredentials: true,
                headers: { 'X-XSRF-TOKEN': getCsrfToken() }
            });

            if (response.status === 200) {
                login();
                navigate('/contacts');
            }
        }
        catch (error) {
            console.log('Login failed with status:', error.response?.status);
            if (error.response?.status === 401 || error.response?.status === 403) {
                setError('Invalid email/phone or password. Please retry!');
            } else if (error.response) {
                setError('Something went wrong on our end. Please try again shortly.');
            } else {
                setError('Unable to reach the server. Please check your connection.');
            }
        }
    };

    const handleForgotPassword = () => {
        toastInfo('Password reset is coming soon. Please contact support for now.');
    };

    return (
        <Container maxWidth="xs">
            <Card sx={{ mt: 8, borderRadius: 2, boxShadow: 2 }}>
                <CardHeader
                    title="Login"
                    sx={{ textAlign: 'center', pb: 0 }}
                    slotProps={{ title: { variant: 'h5', fontWeight: 500 } }}
                />
                <CardContent>
                    <Box
                        component="form"
                        onSubmit={handleLogin}
                        sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}
                    >
                        {error && (
                            <Typography variant="body2" color="error" align="center">
                                {error}
                            </Typography>
                        )}

                        <TextField
                            label="Email or Phone Number"
                            variant="outlined"
                            size="small"
                            fullWidth
                            value={identifier}
                            onChange={(e) => setIdentifier(e.target.value)}
                            required
                        />

                        <TextField
                            label="Password"
                            variant="outlined"
                            size="small"
                            type={showPassword ? 'text' : 'password'}
                            fullWidth
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            slotProps={{
                                input: {
                                    endAdornment: (
                                        <InputAdornment position="end">
                                            <IconButton
                                                onClick={() => setShowPassword(!showPassword)}
                                                edge="end"
                                                size="small"
                                                aria-label={showPassword ? 'Hide password' : 'Show password'}
                                            >
                                                <i className={showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'} aria-hidden="true"></i>
                                            </IconButton>
                                        </InputAdornment>
                                    )
                                }
                            }}
                        />

                        <Typography
                            variant="body2"
                            align="right"
                            component="button"
                            type="button"
                            onClick={handleForgotPassword}
                            sx={{
                                cursor: 'pointer',
                                color: 'primary.main',
                                mt: -1,
                                background: 'none',
                                border: 'none',
                                padding: 0,
                                alignSelf: 'flex-end',
                                font: 'inherit'
                            }}
                        >
                            Forgot password?
                        </Typography>

                        <Button type="submit" variant="contained" fullWidth>
                            Login
                        </Button>
                    </Box>
                </CardContent>
                <CardActions sx={{ justifyContent: 'center', pb: 2 }}>
                    <Link to="/registration" style={{ fontSize: '0.9rem' }}>
                        Don't have an account? Register
                    </Link>
                </CardActions>
            </Card>
        </Container>
    );
}